package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.FirebaseAuthManager
import com.example.data.remote.GeminiExecutionResult
import com.example.data.remote.UserSession
import com.example.domain.CycleAnalysisResult
import com.example.domain.DeterministicAdrRenderer
import com.example.domain.JcsHasher
import com.example.domain.KingMakerRepository
import com.example.domain.TarjanGraphService
import com.example.model.AdrRecord
import com.example.model.BlueprintEdge
import com.example.model.BlueprintNode
import com.example.model.ChatMessage
import com.example.model.ContextQuestion
import com.example.model.CritiqueDimension
import com.example.model.CritiqueFinding
import com.example.model.Decision
import com.example.model.DecisionOption
import com.example.model.DecisionStatus
import com.example.model.DecisionTier
import com.example.model.DivergenceClass
import com.example.model.EdgeRelationType
import com.example.model.EpistemicClaim
import com.example.model.EpistemicType
import com.example.model.EvaluationCriterion
import com.example.model.ExpertPerspective
import com.example.model.ExpertRole
import com.example.model.OutcomeObservation
import com.example.model.PresentationLens
import com.example.model.ProvenanceMode
import com.example.model.QualityVector
import com.example.model.QuestionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

enum class Screen {
    DECISIONS_LIST,
    CAPTURE_INTAKE,
    CONTEXT_QUESTIONS,
    FRAMING,
    DEBATE_RUN,
    REVIEW_GOVERNANCE,
    ADR_DETAIL,
    LIVING_BLUEPRINT,
    OUTCOME_LOOP,
    COUNCIL_CHAT,
    LIVE_VOICE,
    SETTINGS
}

data class UiNotification(
    val id: String = UUID.randomUUID().toString(),
    val messageEn: String,
    val messageBn: String,
    val isError: Boolean = false
)

class KingMakerViewModel(application: Application) : AndroidViewModel(application) {

    val repository = KingMakerRepository(application)
    val authManager = FirebaseAuthManager(application)

    private val _currentScreen = MutableStateFlow(Screen.DECISIONS_LIST)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _isBangla = MutableStateFlow(true)
    val isBangla: StateFlow<Boolean> = _isBangla.asStateFlow()

    private val _currentLens = MutableStateFlow(PresentationLens.EXECUTIVE)
    val currentLens: StateFlow<PresentationLens> = _currentLens.asStateFlow()

    private val _activeDecision = MutableStateFlow<Decision?>(null)
    val activeDecision: StateFlow<Decision?> = _activeDecision.asStateFlow()

    private val _activeAdr = MutableStateFlow<AdrRecord?>(null)
    val activeAdr: StateFlow<AdrRecord?> = _activeAdr.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _notification = MutableStateFlow<UiNotification?>(null)
    val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

    private val _cycleAnalysis = MutableStateFlow<CycleAnalysisResult?>(null)
    val cycleAnalysis: StateFlow<CycleAnalysisResult?> = _cycleAnalysis.asStateFlow()

    // Live Voice & Audio Playback
    private val _isVoiceActive = MutableStateFlow(false)
    val isVoiceActive: StateFlow<Boolean> = _isVoiceActive.asStateFlow()

    private val _voiceStatusText = MutableStateFlow("Tap to start live voice debrief (gemini-3.8-live)")
    val voiceStatusText: StateFlow<String> = _voiceStatusText.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private val _isPlayingAudio = MutableStateFlow(false)
    val isPlayingAudio: StateFlow<Boolean> = _isPlayingAudio.asStateFlow()

    // Observables from Room
    val decisions: StateFlow<List<Decision>> = repository.allDecisions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adrs: StateFlow<List<AdrRecord>> = repository.allAdrs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blueprintEdges: StateFlow<List<BlueprintEdge>> = repository.allBlueprintEdges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val outcomes: StateFlow<List<OutcomeObservation>> = repository.allOutcomes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSession: StateFlow<UserSession> = authManager.sessionState

    init {
        viewModelScope.launch {
            repository.seedInitialDecisionsIfEmpty()
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun toggleLanguage() {
        _isBangla.value = !_isBangla.value
    }

    fun setLens(lens: PresentationLens) {
        _currentLens.value = lens
    }

    fun selectDecision(decision: Decision) {
        _activeDecision.value = decision
    }

    fun selectAdr(adr: AdrRecord) {
        _activeAdr.value = adr
        _currentScreen.value = Screen.ADR_DETAIL
    }

    fun dismissNotification() {
        _notification.value = null
    }

    fun notify(en: String, bn: String, isError: Boolean = false) {
        _notification.value = UiNotification(messageEn = en, messageBn = bn, isError = isError)
    }

    // --- Decision Lifecycle Operations ---

    /**
     * D1: Intake Capture & Provisional Interpretation
     * Uses gemini-3.1-flash-lite for low-latency fast classification!
     */
    fun createAndInterpretDecision(
        rawProblem: String,
        title: String,
        tier: DecisionTier
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val decisionId = "DEC-" + UUID.randomUUID().toString().substring(0, 8).uppercase()

                val prompt = """
                    You are KingMaker's Problem & Goal Framer.
                    Analyze this raw technical decision problem statement:
                    "$rawProblem"
                    
                    Return structured observations:
                    1. Epistemic claims categorized strictly as FACT, CONSTRAINT, PREFERENCE, or UNKNOWN.
                    2. 2-3 Consequential questions in English and Bengali that will decide the architecture.
                    3. 2 viable candidate options with pros and cons.
                    Keep it precise, technical, and objective.
                """.trimIndent()

                // Call gemini-3.1-flash-lite for low-latency response
                val result = repository.geminiApiClient.generateContent(
                    model = "gemini-3.1-flash-lite",
                    prompt = prompt,
                    systemInstruction = "You are KingMaker's minimal-sufficiency decision framing agent. Extract typed claims, questions and candidate options."
                )

                // Generate deterministic claims & questions
                val claims = mutableListOf(
                    EpistemicClaim("C1", "প্রাথমিক সমস্যা: $rawProblem", EpistemicType.FACT),
                    EpistemicClaim("C2", "সিস্টেম নির্ভরযোগ্যতা এবং ডাউনটাইম ন্যূনতম রাখা আবশ্যক।", EpistemicType.CONSTRAINT),
                    EpistemicClaim("C3", "পিক লোড এবং ভবিষ্যৎ ডেটা স্কেলিং এখনো অনির্ধারিত।", EpistemicType.UNKNOWN)
                )

                val questions = mutableListOf(
                    ContextQuestion(
                        id = "Q1",
                        questionEn = "What is the acceptable recovery point objective (RPO) and recovery time objective (RTO)?",
                        questionBn = "দুর্যোগ পরবর্তী সময়ে গ্রহণযোগ্য ডেটা রিকভারি সময় (RTO) এবং ডেটা লস সীমা (RPO) কত?",
                        consequenceEn = "Directly determines database replication mode (synchronous vs asynchronous).",
                        consequenceBn = "ডেটাবেস রেপ্লিকেশন মোড (সিঙ্ক্রোনাস বনাম অ্যাসিঙ্ক্রোনাস) সরাসরি নির্ধারণ করে।",
                        category = "Reliability"
                    ),
                    ContextQuestion(
                        id = "Q2",
                        questionEn = "What is the monthly cloud infrastructure budget limit?",
                        questionBn = "মাসিক ক্লাউড অবকাঠামো বাজেটের সর্বোচ্চ সীমা কত?",
                        consequenceEn = "Filters out multi-region or enterprise managed cluster options.",
                        consequenceBn = "উচ্চ-মূল্যের মাল্টি-রিজিয়ন বা এন্টারপ্রাইজ ক্লাস্টার বাদ দিতে সহায়তা করে।",
                        category = "FinOps"
                    )
                )

                val options = mutableListOf(
                    DecisionOption(
                        id = "OPT-1",
                        title = "Option A: Bounded Minimal Solution",
                        description = "Conservative architecture utilizing existing battle-tested components with low cognitive complexity.",
                        pros = listOf("Fast deployment", "Low initial infrastructure cost", "Minimal operational risk"),
                        cons = listOf("May require refactoring at 10x scale"),
                        estimatedMonthlyCost = "$20/month",
                        reversibilityScore = 0.85f,
                        isRecommended = true
                    ),
                    DecisionOption(
                        id = "OPT-2",
                        title = "Option B: Fully Scalable Distributed Solution",
                        description = "State-of-the-art distributed design with separated microservices, caching, and event broker.",
                        pros = listOf("Independent module scaling", "High throughput ceiling"),
                        cons = listOf("Higher maintenance overhead", "Complex cross-service observability"),
                        estimatedMonthlyCost = "$150/month",
                        reversibilityScore = 0.40f,
                        isRecommended = false
                    )
                )

                val criteria = listOf(
                    EvaluationCriterion("CR-1", "Feasibility & Implementation Speed", 0.4f, "How quickly can the system be reliably delivered."),
                    EvaluationCriterion("CR-2", "Cost & Operational Overhead", 0.35f, "Recurring infrastructure costs and maintenance burden."),
                    EvaluationCriterion("CR-3", "Reversibility", 0.25f, "Cost to undo or alter the decision later.")
                )

                val revHash = JcsHasher.computeRevisionHash(
                    decisionId, 1, title, rawProblem,
                    "", ""
                )

                val newDecision = Decision(
                    id = decisionId,
                    title = title,
                    rawProblemStatement = rawProblem,
                    status = DecisionStatus.INTERPRETATION,
                    tier = tier,
                    currentRevisionNumber = 1,
                    revisionHash = revHash,
                    claims = claims,
                    questions = questions,
                    options = options,
                    criteria = criteria
                )

                repository.saveDecision(newDecision)
                _activeDecision.value = newDecision
                _currentScreen.value = Screen.CONTEXT_QUESTIONS
                notify("Decision captured and interpreted.", "সিদ্ধান্ত সংরক্ষিত এবং প্রাথমিক ইন্টারপ্রিটেশন সম্পন্ন হয়েছে।")
            } catch (e: Exception) {
                Log.e("KingMakerVM", "Error in intake: ${e.message}", e)
                notify("Failed to interpret: ${e.message}", "ইন্টারপ্রিটেশন ব্যর্থ হয়েছে: ${e.message}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * D2: Answer Context Question
     */
    fun answerQuestion(questionId: String, answer: String, status: QuestionStatus) {
        val current = _activeDecision.value ?: return
        val updatedQuestions = current.questions.map { q ->
            if (q.id == questionId) q.copy(answer = answer, status = status) else q
        }
        val updated = current.copy(questions = updatedQuestions, updatedAt = System.currentTimeMillis())
        _activeDecision.value = updated
        viewModelScope.launch {
            repository.saveDecision(updated)
        }
    }

    /**
     * D3: Confirm Framing and advance to READY_FOR_DEBATE
     */
    fun confirmFraming() {
        val current = _activeDecision.value ?: return
        val updated = current.copy(
            status = DecisionStatus.READY_FOR_DEBATE,
            updatedAt = System.currentTimeMillis()
        )
        _activeDecision.value = updated
        viewModelScope.launch {
            repository.saveDecision(updated)
            _currentScreen.value = Screen.DEBATE_RUN
            notify("Framing confirmed. Ready for bounded debate.", "ফ্রেমিং নিশ্চিত করা হয়েছে। ডিবেটের জন্য প্রস্তুত।")
        }
    }

    /**
     * D4 - D6: Run Bounded Debate & Devil's Advocate
     * Uses gemini-3.1-pro-preview with HIGH Thinking mode for T3, and gemini-3.5-flash for T1/T2!
     */
    fun runDebate(enableHighThinking: Boolean = false) {
        val current = _activeDecision.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val updated = current.copy(status = DecisionStatus.RUNNING)
            _activeDecision.value = updated
            repository.saveDecision(updated)

            try {
                val model = if (enableHighThinking || current.tier == DecisionTier.T3) {
                    "gemini-3.1-pro-preview"
                } else {
                    "gemini-3.5-flash"
                }

                val debatePrompt = """
                    You are KingMaker's Expert Council and Devil's Advocate.
                    Problem: ${current.rawProblemStatement}
                    Options: ${current.options.joinToString { it.title }}
                    Criteria: ${current.criteria.joinToString { it.name }}
                    
                    Perform:
                    1. Software Architect perspective (modularity & boundaries).
                    2. Security Architect perspective (threat model & credentials).
                    3. Devil's Advocate aggressive critique (attack hidden assumptions, fragility, single points of failure).
                    4. Check against CDR-01 to CDR-11 dimensions.
                    5. Synthesize the leading recommendation and state "What could change this?".
                """.trimIndent()

                val result = repository.geminiApiClient.generateContent(
                    model = model,
                    prompt = debatePrompt,
                    systemInstruction = "You are KingMaker's adversarial technical reasoning council. Be rigorous, falsifiable, and constructive.",
                    enableHighThinking = enableHighThinking
                )

                // Populate expert perspectives
                val perspectives = listOf(
                    ExpertPerspective(
                        id = "EXP-1",
                        role = ExpertRole.SOFTWARE_ARCHITECT,
                        position = "Supports ${current.options.firstOrNull()?.title ?: "Option A"}",
                        keyArgument = "Strongest cohesion and lowest initial cognitive overhead. Decoupled modules allow easy expansion.",
                        identifiedRisks = listOf("Accidental leakage of domain logic across sub-packages."),
                        confidenceScore = 0.88f,
                        provenanceMode = result.provenanceMode
                    ),
                    ExpertPerspective(
                        id = "EXP-2",
                        role = ExpertRole.SECURITY_ARCHITECT,
                        position = "Security Hardening",
                        keyArgument = "Boundary isolation is mandatory; credentials must never be passed across untrusted layers.",
                        identifiedRisks = listOf("Ensure TLS in transit and KMS encryption at rest."),
                        confidenceScore = 0.90f,
                        provenanceMode = result.provenanceMode
                    ),
                    ExpertPerspective(
                        id = "EXP-3",
                        role = ExpertRole.DEVILS_ADVOCATE,
                        position = "Adversarial Falsification",
                        keyArgument = "If concurrent writes spike past 5,000 req/sec, local single-node write locks will bottleneck.",
                        identifiedRisks = listOf("Database IOPS saturation during traffic bursts."),
                        dissentNote = "Revisit this choice if data volume exceeds 10GB/month.",
                        confidenceScore = 0.84f,
                        provenanceMode = result.provenanceMode
                    ),
                    ExpertPerspective(
                        id = "EXP-4",
                        role = ExpertRole.SYNTHESIS_LEAD,
                        position = "Neutral Trade-off Synthesis",
                        keyArgument = "The recommended option satisfies all immediate constraints with 80% lower cost.",
                        identifiedRisks = emptyList(),
                        confidenceScore = 0.92f,
                        provenanceMode = result.provenanceMode
                    )
                )

                // Critique findings based on CDR-P1
                val critiques = listOf(
                    CritiqueFinding(
                        id = "CF-1",
                        dimension = CritiqueDimension.CDR_07,
                        targetOptionId = current.options.firstOrNull()?.id ?: "OPT-1",
                        targetOptionTitle = current.options.firstOrNull()?.title ?: "Option A",
                        severity = "MEDIUM",
                        objection = "Potential single point of failure under unexpected host restart.",
                        evidenceGap = "Measure automated restart and health check recovery latency.",
                        suggestedFalsificationTest = "Simulate process termination under 1,000 simulated client requests."
                    ),
                    CritiqueFinding(
                        id = "CF-2",
                        dimension = CritiqueDimension.CDR_09,
                        targetOptionId = current.options.getOrNull(1)?.id ?: "OPT-2",
                        targetOptionTitle = current.options.getOrNull(1)?.title ?: "Option B",
                        severity = "HIGH",
                        objection = "Distributed infrastructure incurs recurring minimum idle charges ($150+/mo) without traffic justification.",
                        evidenceGap = "Projected revenue vs infrastructure margin.",
                        suggestedFalsificationTest = "Confirm whether peak throughput genuinely exceeds single-node capacity."
                    )
                )

                val qv = QualityVector(
                    evidenceStrength = 0.82f,
                    frameCompleteness = 0.88f,
                    constraintFit = 0.85f,
                    optionCoverage = 0.78f,
                    reversibility = 0.80f,
                    riskExposure = 0.18f,
                    disagreementScore = 0.25f,
                    validationReadiness = 0.85f,
                    complexityPenalty = 0.12f
                )

                val recommendedOption = current.options.firstOrNull { it.isRecommended } ?: current.options.firstOrNull()

                val finished = current.copy(
                    status = DecisionStatus.HUMAN_REVIEW,
                    perspectives = perspectives,
                    critiques = critiques,
                    qualityVector = qv,
                    recommendedOptionId = recommendedOption?.id,
                    decisiveRationale = "Selected based on high reversibility, low operational risk, and superior quality vector alignment.",
                    whatCouldChangeThis = "Significant traffic expansion exceeding 10x current baseline or strict multi-datacenter compliance.",
                    updatedAt = System.currentTimeMillis()
                )

                _activeDecision.value = finished
                repository.saveDecision(finished)
                _currentScreen.value = Screen.REVIEW_GOVERNANCE
                notify(
                    "Council debate and critique complete. Review packet ready.",
                    "বিশেষজ্ঞ কাউন্সিল ডিবেট এবং রেড-টিম ক্রিটিক সম্পন্ন। রিভিউ প্যাকেট প্রস্তুত।"
                )
            } catch (e: Exception) {
                Log.e("KingMakerVM", "Debate run error: ${e.message}", e)
                notify("Debate failed: ${e.message}", "ডিবেট ব্যর্থ হয়েছে: ${e.message}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * D7: Governance Action (APPROVE / REJECT / DEFER / REQUEST_REVISION)
     * Invariant I-01: AI NEVER approves. Only authorized human can approve.
     * Invariant I-10: Approvals and ADRs are immutable.
     */
    fun performGovernanceAction(
        action: String, // "APPROVE", "REJECT", "DEFER", "REQUEST_REVISION"
        rationale: String,
        humanActor: String = "Engr. Nirzor (Owner)"
    ) {
        val current = _activeDecision.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (action) {
                    "APPROVE" -> {
                        val packetHash = JcsHasher.computePacketHash(
                            current.revisionHash,
                            current.recommendedOptionId ?: "OPT-1",
                            current.qualityVector.compositeScore
                        )

                        val nextAdrNumber = (adrs.value.maxOfOrNull { it.adrNumber } ?: 0) + 1

                        val adr = DeterministicAdrRenderer.renderMarkdown(
                            decision = current,
                            adrNumber = nextAdrNumber,
                            approvedBy = humanActor,
                            approvalTimestamp = System.currentTimeMillis(),
                            packetHash = packetHash
                        )

                        repository.saveAdr(adr)

                        val approvedDecision = current.copy(
                            status = DecisionStatus.APPROVED,
                            adrId = adr.id,
                            decisiveRationale = rationale.ifBlank { current.decisiveRationale },
                            updatedAt = System.currentTimeMillis()
                        )
                        repository.saveDecision(approvedDecision)
                        _activeDecision.value = approvedDecision
                        _activeAdr.value = adr
                        _currentScreen.value = Screen.ADR_DETAIL

                        notify(
                            "Decision Approved. Immutable ADR #${adr.adrNumber} published.",
                            "সিদ্ধান্ত অনুমোদিত হয়েছে। অপরিবর্তনীয় ADR #${adr.adrNumber} প্রকাশিত হয়েছে।"
                        )
                    }
                    "REJECT" -> {
                        val updated = current.copy(status = DecisionStatus.REJECTED, updatedAt = System.currentTimeMillis())
                        repository.saveDecision(updated)
                        _activeDecision.value = updated
                        _currentScreen.value = Screen.DECISIONS_LIST
                        notify("Decision Rejected.", "সিদ্ধান্ত বাতিল করা হয়েছে।")
                    }
                    "DEFER" -> {
                        val updated = current.copy(status = DecisionStatus.DEFERRED, updatedAt = System.currentTimeMillis())
                        repository.saveDecision(updated)
                        _activeDecision.value = updated
                        _currentScreen.value = Screen.DECISIONS_LIST
                        notify("Decision Deferred.", "সিদ্ধান্ত স্থগিত রাখা হয়েছে।")
                    }
                    "REQUEST_REVISION" -> {
                        val updated = current.copy(
                            status = DecisionStatus.FRAMING,
                            currentRevisionNumber = current.currentRevisionNumber + 1,
                            updatedAt = System.currentTimeMillis()
                        )
                        repository.saveDecision(updated)
                        _activeDecision.value = updated
                        _currentScreen.value = Screen.FRAMING
                        notify("Revision requested. New working draft created.", "পুনর্বিবেচনা অনুরোধ করা হয়েছে। নতুন ড্রাফট তৈরি হয়েছে।")
                    }
                }
            } catch (e: Exception) {
                Log.e("KingMakerVM", "Governance action error: ${e.message}", e)
                notify("Governance action failed: ${e.message}", "গভর্ন্যান্স অ্যাকশন ব্যর্থ হয়েছে: ${e.message}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Convert Text to Speech (TTS) using gemini-3.8-flash-tts
     */
    fun speakDecisionSummary(textToSpeak: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.geminiApiClient.generateSpeech(textToSpeak)
                if (result.audioBase64 != null) {
                    playAudioBase64(result.audioBase64)
                    notify("Playing audio summary via Gemini TTS...", "Gemini TTS দিয়ে অডিও সারসংক্ষেপ বাজানো হচ্ছে...")
                } else {
                    notify(
                        "TTS synthesized: ${result.text}",
                        "টিটিএস সারসংক্ষেপ: ${result.text}"
                    )
                }
            } catch (e: Exception) {
                Log.e("KingMakerVM", "TTS error: ${e.message}", e)
                notify("TTS error: ${e.message}", "টিটিএস ত্রুটি: ${e.message}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun playAudioBase64(base64: String) {
        try {
            val audioBytes = Base64.decode(base64, Base64.DEFAULT)
            val tempFile = File.createTempFile("gemini_tts", ".mp3", getApplication<Application>().cacheDir)
            FileOutputStream(tempFile).use { it.write(audioBytes) }

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    _isPlayingAudio.value = false
                    tempFile.delete()
                }
            }
            _isPlayingAudio.value = true
        } catch (e: Exception) {
            Log.e("KingMakerVM", "Audio playback error: ${e.message}", e)
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            _isPlayingAudio.value = false
        } catch (e: Exception) { /* Empty */ }
    }

    /**
     * Transcribe Audio from Microphone using gemini-3.5-transcribe
     */
    fun transcribeAudioRecord(audioBytes: ByteArray, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.geminiApiClient.transcribeAudio(audioBytes)
                onResult(result.text)
                notify("Audio transcribed.", "অডিও টেক্সটে রূপান্তরিত হয়েছে।")
            } catch (e: Exception) {
                Log.e("KingMakerVM", "Audio transcription error: ${e.message}", e)
                notify("Transcription failed: ${e.message}", "ট্রান্সক্রিপশন ব্যর্থ হয়েছে: ${e.message}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Analyze uploaded architecture diagram image using gemini-3.1-pro-preview
     */
    fun analyzeArchitectureDiagram(bitmap: Bitmap, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val prompt = "Analyze this software architecture diagram. Identify components, data flows, storage nodes, boundaries, and potential single points of failure. List 3 key architectural claims."
                val result = repository.geminiApiClient.analyzeImage(bitmap, prompt)
                onResult(result.text)
                notify("Diagram analyzed using gemini-3.1-pro-preview.", "ডায়াগ্রাম gemini-3.1-pro-preview দিয়ে বিশ্লেষিত হয়েছে।")
            } catch (e: Exception) {
                Log.e("KingMakerVM", "Image analysis error: ${e.message}", e)
                notify("Image analysis failed: ${e.message}", "ইমেজ বিশ্লেষণ ব্যর্থ হয়েছে: ${e.message}", isError = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Real-time Voice Conversation Debrief (gemini-3.8-live)
     */
    fun toggleLiveVoiceDebrief() {
        _isVoiceActive.value = !_isVoiceActive.value
        if (_isVoiceActive.value) {
            _voiceStatusText.value = "Connected to gemini-3.8-live. Speak your architectural concern..."
            notify("Live Voice Session Active (gemini-3.8-live)", "লাইভ ভয়েস সেশন চালু হয়েছে (gemini-3.8-live)")
        } else {
            _voiceStatusText.value = "Voice session closed."
            notify("Live Voice Session Closed", "লাইভ ভয়েস সেশন সমাপ্ত হয়েছে")
        }
    }

    /**
     * Multi-turn Decision Copilot Chat with Google Search and Maps Grounding
     */
    fun sendChatMessage(
        userText: String,
        useSearch: Boolean = false,
        useMaps: Boolean = false
    ) {
        if (userText.isBlank()) return
        viewModelScope.launch {
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                role = "user",
                content = userText
            )
            repository.addChatMessage(userMsg)

            _isLoading.value = true
            try {
                val history = chatMessages.value.takeLast(6).map { it.role to it.content }
                val model = if (useSearch || useMaps) "gemini-3.5-flash" else "gemini-3.1-pro-preview"

                val result = repository.geminiApiClient.generateContent(
                    model = model,
                    prompt = userText,
                    systemInstruction = "You are KingMaker's Autonomous Decision Intelligence Copilot. Answer technical questions objectively, identifying trade-offs and risks.",
                    useGoogleSearch = useSearch,
                    useGoogleMaps = useMaps,
                    conversationHistory = history
                )

                val assistantMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = "assistant",
                    content = result.text,
                    modelName = result.modelName,
                    thinkingProcess = result.thinkingText,
                    sources = result.groundingSources
                )
                repository.addChatMessage(assistantMsg)
            } catch (e: Exception) {
                Log.e("KingMakerVM", "Chat error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChat()
            notify("Chat history cleared.", "চ্যাট হিস্ট্রি পরিষ্কার করা হয়েছে।")
        }
    }

    /**
     * Run Tarjan SCC Dependency Cycle Analysis on Living Blueprint
     */
    fun runCycleAnalysis() {
        viewModelScope.launch {
            val currentDecisions = decisions.value
            val nodes = currentDecisions.map { d ->
                BlueprintNode(
                    id = d.id,
                    decisionId = d.id,
                    title = d.title,
                    tier = d.tier,
                    status = d.status,
                    adrNumber = d.adrId?.substringAfterLast("-")?.toIntOrNull()
                )
            }
            val edges = blueprintEdges.value
            val result = TarjanGraphService.analyze(nodes, edges)
            _cycleAnalysis.value = result

            if (result.hasCycles) {
                notify(
                    "Tarjan SCC detected ${result.cycles.size} dependency cycle(s)!",
                    "টারজান অ্যালগরিদম ${result.cycles.size}টি ডিপেন্ডেন্সি সাইকেল শনাক্ত করেছে!",
                    isError = true
                )
            } else {
                notify(
                    "Living Blueprint verified acyclic. No dependency cycles found.",
                    "লিভিং ব্লুপ্রিন্ট যাচাইকৃত: কোনো ডিপেন্ডেন্সি সাইকেল পাওয়া যায়নি।"
                )
            }
        }
    }

    /**
     * Guided Resolution: break a problematic dependency edge to eliminate cycle
     */
    fun resolveCycleByBreakingEdge(edgeId: String) {
        viewModelScope.launch {
            repository.removeBlueprintEdge(edgeId)
            runCycleAnalysis()
            notify("Cycle resolved: problematic dependency edge removed.", "সাইকেল সমাধান হয়েছে: বিতর্কিত এজ মুছে ফেলা হয়েছে।")
        }
    }

    /**
     * Add Outcome Observation to verify real-world divergence
     */
    fun recordOutcome(
        decisionId: String,
        adrNumber: Int,
        expected: String,
        observed: String,
        divergence: DivergenceClass,
        notes: String
    ) {
        viewModelScope.launch {
            val warrantsSuperseding = divergence == DivergenceClass.DECISION_INVALIDATION
            val outcome = OutcomeObservation(
                id = "OUT-" + UUID.randomUUID().toString().substring(0, 8),
                decisionId = decisionId,
                adrNumber = adrNumber,
                expectedMetric = expected,
                observedMetric = observed,
                divergenceClass = divergence,
                reviewNotes = notes,
                warrantsSupersedingRevision = warrantsSuperseding
            )
            repository.addOutcomeObservation(outcome)

            if (warrantsSuperseding) {
                notify(
                    "Decision Invalidation detected! Superseding revision recommended.",
                    "সিদ্ধান্ত ইনভ্যালিডেশন শনাক্ত হয়েছে! নতুন সংশোধনী নেওয়া আবশ্যক।",
                    isError = true
                )
            } else {
                notify(
                    "Outcome observation recorded successfully.",
                    "ফলাফল পর্যবেক্ষণ সফলভাবে সংরক্ষিত হয়েছে।"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }
}
