package com.example.domain

import android.content.Context
import com.example.data.local.AdrEntity
import com.example.data.local.BlueprintEdgeEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.DecisionEntity
import com.example.data.local.KingMakerDatabase
import com.example.data.local.OutcomeObservationEntity
import com.example.data.remote.FirestoreSyncService
import com.example.data.remote.GeminiApiClient
import com.example.data.remote.GeminiExecutionResult
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
import com.example.model.ProvenanceMode
import com.example.model.QualityVector
import com.example.model.QuestionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class KingMakerRepository(
    private val context: Context,
    val geminiApiClient: GeminiApiClient = GeminiApiClient(),
    private val firestoreSync: FirestoreSyncService = FirestoreSyncService()
) {
    private val db = KingMakerDatabase.getInstance(context)
    private val decisionDao = db.decisionDao()
    private val adrDao = db.adrDao()
    private val blueprintDao = db.blueprintDao()
    private val outcomeDao = db.outcomeDao()
    private val chatDao = db.chatDao()

    val allDecisions: Flow<List<Decision>> = decisionDao.getAllDecisions().map { list ->
        list.map { it.toModel() }
    }

    val allAdrs: Flow<List<AdrRecord>> = adrDao.getAllAdrs().map { list ->
        list.map { it.toModel() }
    }

    val allBlueprintEdges: Flow<List<BlueprintEdge>> = blueprintDao.getAllEdges().map { list ->
        list.map { it.toModel() }
    }

    val allOutcomes: Flow<List<OutcomeObservation>> = outcomeDao.getAllOutcomes().map { list ->
        list.map { it.toModel() }
    }

    val chatMessages: Flow<List<ChatMessage>> = chatDao.getAllMessages().map { list ->
        list.map { it.toModel() }
    }

    suspend fun getDecisionById(id: String): Decision? = withContext(Dispatchers.IO) {
        decisionDao.getDecisionById(id)?.toModel()
    }

    suspend fun saveDecision(decision: Decision) = withContext(Dispatchers.IO) {
        val entity = decision.toEntity()
        decisionDao.insertOrUpdate(entity)
        firestoreSync.syncDecision(decision)
    }

    suspend fun deleteDecision(id: String) = withContext(Dispatchers.IO) {
        decisionDao.deleteDecision(id)
    }

    suspend fun saveAdr(adr: AdrRecord) = withContext(Dispatchers.IO) {
        adrDao.insertAdr(adr.toEntity())
        firestoreSync.syncAdr(adr)
    }

    suspend fun addBlueprintEdge(edge: BlueprintEdge) = withContext(Dispatchers.IO) {
        blueprintDao.insertEdge(edge.toEntity())
    }

    suspend fun removeBlueprintEdge(edgeId: String) = withContext(Dispatchers.IO) {
        blueprintDao.deleteEdge(edgeId)
    }

    suspend fun addOutcomeObservation(outcome: OutcomeObservation) = withContext(Dispatchers.IO) {
        outcomeDao.insertOutcome(outcome.toEntity())
    }

    suspend fun addChatMessage(msg: ChatMessage) = withContext(Dispatchers.IO) {
        chatDao.insertMessage(msg.toEntity())
    }

    suspend fun clearChat() = withContext(Dispatchers.IO) {
        chatDao.clearHistory()
    }

    // --- Entity / Model Mappings ---

    private fun DecisionEntity.toModel(): Decision {
        return Decision(
            id = id,
            title = title,
            rawProblemStatement = rawProblemStatement,
            status = try { DecisionStatus.valueOf(status) } catch (e: Exception) { DecisionStatus.RAW },
            tier = try { DecisionTier.valueOf(tier) } catch (e: Exception) { DecisionTier.T2 },
            currentRevisionNumber = currentRevisionNumber,
            revisionHash = revisionHash,
            createdAt = createdAt,
            updatedAt = updatedAt,
            claims = parseClaimsJson(claimsJson),
            questions = parseQuestionsJson(questionsJson),
            options = parseOptionsJson(optionsJson),
            criteria = parseCriteriaJson(criteriaJson),
            perspectives = parsePerspectivesJson(perspectivesJson),
            critiques = parseCritiquesJson(critiquesJson),
            qualityVector = parseQualityVectorJson(qualityVectorJson),
            recommendedOptionId = recommendedOptionId,
            decisiveRationale = decisiveRationale,
            whatCouldChangeThis = whatCouldChangeThis,
            adrId = adrId,
            dependencies = parseStringList(dependenciesJson)
        )
    }

    private fun Decision.toEntity(): DecisionEntity {
        return DecisionEntity(
            id = id,
            title = title,
            rawProblemStatement = rawProblemStatement,
            status = status.name,
            tier = tier.name,
            currentRevisionNumber = currentRevisionNumber,
            revisionHash = revisionHash,
            createdAt = createdAt,
            updatedAt = updatedAt,
            claimsJson = claimsToJson(claims),
            questionsJson = questionsToJson(questions),
            optionsJson = optionsToJson(options),
            criteriaJson = criteriaToJson(criteria),
            perspectivesJson = perspectivesToJson(perspectives),
            critiquesJson = critiquesToJson(critiques),
            qualityVectorJson = qualityVectorToJson(qualityVector),
            recommendedOptionId = recommendedOptionId,
            decisiveRationale = decisiveRationale,
            whatCouldChangeThis = whatCouldChangeThis,
            adrId = adrId,
            dependenciesJson = stringListToJson(dependencies)
        )
    }

    private fun AdrEntity.toModel(): AdrRecord {
        return AdrRecord(
            id = id,
            decisionId = decisionId,
            adrNumber = adrNumber,
            title = title,
            status = status,
            context = context,
            decisionOutcome = decisionOutcome,
            chosenOption = chosenOption,
            consequences = consequences,
            revisionHash = revisionHash,
            packetHash = packetHash,
            approvedBy = approvedBy,
            approvalTimestamp = approvalTimestamp,
            renderedMarkdown = renderedMarkdown
        )
    }

    private fun AdrRecord.toEntity(): AdrEntity {
        return AdrEntity(
            id = id,
            decisionId = decisionId,
            adrNumber = adrNumber,
            title = title,
            status = status,
            context = context,
            decisionOutcome = decisionOutcome,
            chosenOption = chosenOption,
            consequences = consequences,
            revisionHash = revisionHash,
            packetHash = packetHash,
            approvedBy = approvedBy,
            approvalTimestamp = approvalTimestamp,
            renderedMarkdown = renderedMarkdown
        )
    }

    private fun BlueprintEdgeEntity.toModel(): BlueprintEdge {
        return BlueprintEdge(
            id = id,
            sourceId = sourceId,
            targetId = targetId,
            relationType = try { EdgeRelationType.valueOf(relationType) } catch (e: Exception) { EdgeRelationType.DEPENDS_ON }
        )
    }

    private fun BlueprintEdge.toEntity(): BlueprintEdgeEntity {
        return BlueprintEdgeEntity(
            id = id,
            sourceId = sourceId,
            targetId = targetId,
            relationType = relationType.name
        )
    }

    private fun OutcomeObservationEntity.toModel(): OutcomeObservation {
        return OutcomeObservation(
            id = id,
            decisionId = decisionId,
            adrNumber = adrNumber,
            expectedMetric = expectedMetric,
            observedMetric = observedMetric,
            divergenceClass = try { DivergenceClass.valueOf(divergenceClass) } catch (e: Exception) { DivergenceClass.NONE },
            observationDate = observationDate,
            reviewNotes = reviewNotes,
            warrantsSupersedingRevision = warrantsSupersedingRevision
        )
    }

    private fun OutcomeObservation.toEntity(): OutcomeObservationEntity {
        return OutcomeObservationEntity(
            id = id,
            decisionId = decisionId,
            adrNumber = adrNumber,
            expectedMetric = expectedMetric,
            observedMetric = observedMetric,
            divergenceClass = divergenceClass.name,
            observationDate = observationDate,
            reviewNotes = reviewNotes,
            warrantsSupersedingRevision = warrantsSupersedingRevision
        )
    }

    private fun ChatMessageEntity.toModel(): ChatMessage {
        return ChatMessage(
            id = id,
            role = role,
            content = content,
            timestamp = timestamp,
            modelName = modelName,
            thinkingProcess = thinkingProcess,
            sources = parseStringList(sourcesJson)
        )
    }

    private fun ChatMessage.toEntity(): ChatMessageEntity {
        return ChatMessageEntity(
            id = id,
            role = role,
            content = content,
            timestamp = timestamp,
            modelName = modelName,
            thinkingProcess = thinkingProcess,
            sourcesJson = stringListToJson(sources)
        )
    }

    // --- JSON Serialization Helpers ---

    private fun parseClaimsJson(jsonStr: String): List<EpistemicClaim> {
        val list = mutableListOf<EpistemicClaim>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    EpistemicClaim(
                        id = obj.getString("id"),
                        text = obj.getString("text"),
                        type = EpistemicType.valueOf(obj.getString("type")),
                        confidence = obj.optDouble("confidence", 0.8).toFloat(),
                        evidenceSource = obj.optString("evidenceSource", null)
                    )
                )
            }
        } catch (e: Exception) { /* Empty */ }
        return list
    }

    private fun claimsToJson(claims: List<EpistemicClaim>): String {
        val arr = JSONArray()
        for (c in claims) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("text", c.text)
                put("type", c.type.name)
                put("confidence", c.confidence)
                put("evidenceSource", c.evidenceSource ?: "")
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseQuestionsJson(jsonStr: String): List<ContextQuestion> {
        val list = mutableListOf<ContextQuestion>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ContextQuestion(
                        id = obj.getString("id"),
                        questionEn = obj.getString("questionEn"),
                        questionBn = obj.getString("questionBn"),
                        consequenceEn = obj.getString("consequenceEn"),
                        consequenceBn = obj.getString("consequenceBn"),
                        category = obj.getString("category"),
                        status = QuestionStatus.valueOf(obj.optString("status", QuestionStatus.PENDING.name)),
                        answer = obj.optString("answer", "")
                    )
                )
            }
        } catch (e: Exception) { /* Empty */ }
        return list
    }

    private fun questionsToJson(questions: List<ContextQuestion>): String {
        val arr = JSONArray()
        for (q in questions) {
            val obj = JSONObject().apply {
                put("id", q.id)
                put("questionEn", q.questionEn)
                put("questionBn", q.questionBn)
                put("consequenceEn", q.consequenceEn)
                put("consequenceBn", q.consequenceBn)
                put("category", q.category)
                put("status", q.status.name)
                put("answer", q.answer)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseOptionsJson(jsonStr: String): List<DecisionOption> {
        val list = mutableListOf<DecisionOption>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    DecisionOption(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.getString("description"),
                        pros = parseStringList(obj.optString("pros", "[]")),
                        cons = parseStringList(obj.optString("cons", "[]")),
                        estimatedMonthlyCost = obj.optString("estimatedMonthlyCost", "$0"),
                        reversibilityScore = obj.optDouble("reversibilityScore", 0.7).toFloat(),
                        isRecommended = obj.optBoolean("isRecommended", false)
                    )
                )
            }
        } catch (e: Exception) { /* Empty */ }
        return list
    }

    private fun optionsToJson(options: List<DecisionOption>): String {
        val arr = JSONArray()
        for (o in options) {
            val obj = JSONObject().apply {
                put("id", o.id)
                put("title", o.title)
                put("description", o.description)
                put("pros", stringListToJson(o.pros))
                put("cons", stringListToJson(o.cons))
                put("estimatedMonthlyCost", o.estimatedMonthlyCost)
                put("reversibilityScore", o.reversibilityScore)
                put("isRecommended", o.isRecommended)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseCriteriaJson(jsonStr: String): List<EvaluationCriterion> {
        val list = mutableListOf<EvaluationCriterion>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    EvaluationCriterion(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        weight = obj.getDouble("weight").toFloat(),
                        description = obj.getString("description")
                    )
                )
            }
        } catch (e: Exception) { /* Empty */ }
        return list
    }

    private fun criteriaToJson(criteria: List<EvaluationCriterion>): String {
        val arr = JSONArray()
        for (c in criteria) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("weight", c.weight)
                put("description", c.description)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parsePerspectivesJson(jsonStr: String): List<ExpertPerspective> {
        val list = mutableListOf<ExpertPerspective>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ExpertPerspective(
                        id = obj.getString("id"),
                        role = ExpertRole.valueOf(obj.getString("role")),
                        position = obj.getString("position"),
                        keyArgument = obj.getString("keyArgument"),
                        identifiedRisks = parseStringList(obj.optString("identifiedRisks", "[]")),
                        dissentNote = obj.optString("dissentNote", null),
                        confidenceScore = obj.optDouble("confidenceScore", 0.85).toFloat(),
                        provenanceMode = try { ProvenanceMode.valueOf(obj.getString("provenanceMode")) } catch (e: Exception) { ProvenanceMode.PROVIDER }
                    )
                )
            }
        } catch (e: Exception) { /* Empty */ }
        return list
    }

    private fun perspectivesToJson(perspectives: List<ExpertPerspective>): String {
        val arr = JSONArray()
        for (p in perspectives) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("role", p.role.name)
                put("position", p.position)
                put("keyArgument", p.keyArgument)
                put("identifiedRisks", stringListToJson(p.identifiedRisks))
                put("dissentNote", p.dissentNote ?: "")
                put("confidenceScore", p.confidenceScore)
                put("provenanceMode", p.provenanceMode.name)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseCritiquesJson(jsonStr: String): List<CritiqueFinding> {
        val list = mutableListOf<CritiqueFinding>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    CritiqueFinding(
                        id = obj.getString("id"),
                        dimension = CritiqueDimension.valueOf(obj.getString("dimension")),
                        targetOptionId = obj.getString("targetOptionId"),
                        targetOptionTitle = obj.getString("targetOptionTitle"),
                        severity = obj.getString("severity"),
                        objection = obj.getString("objection"),
                        evidenceGap = obj.getString("evidenceGap"),
                        suggestedFalsificationTest = obj.getString("suggestedFalsificationTest")
                    )
                )
            }
        } catch (e: Exception) { /* Empty */ }
        return list
    }

    private fun critiquesToJson(critiques: List<CritiqueFinding>): String {
        val arr = JSONArray()
        for (c in critiques) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("dimension", c.dimension.name)
                put("targetOptionId", c.targetOptionId)
                put("targetOptionTitle", c.targetOptionTitle)
                put("severity", c.severity)
                put("objection", c.objection)
                put("evidenceGap", c.evidenceGap)
                put("suggestedFalsificationTest", c.suggestedFalsificationTest)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseQualityVectorJson(jsonStr: String): QualityVector {
        return try {
            val obj = JSONObject(jsonStr)
            QualityVector(
                evidenceStrength = obj.optDouble("evidenceStrength", 0.75).toFloat(),
                frameCompleteness = obj.optDouble("frameCompleteness", 0.85).toFloat(),
                constraintFit = obj.optDouble("constraintFit", 0.80).toFloat(),
                optionCoverage = obj.optDouble("optionCoverage", 0.70).toFloat(),
                reversibility = obj.optDouble("reversibility", 0.65).toFloat(),
                riskExposure = obj.optDouble("riskExposure", 0.25).toFloat(),
                disagreementScore = obj.optDouble("disagreementScore", 0.30).toFloat(),
                validationReadiness = obj.optDouble("validationReadiness", 0.80).toFloat(),
                complexityPenalty = obj.optDouble("complexityPenalty", 0.15).toFloat()
            )
        } catch (e: Exception) {
            QualityVector()
        }
    }

    private fun qualityVectorToJson(qv: QualityVector): String {
        return JSONObject().apply {
            put("evidenceStrength", qv.evidenceStrength)
            put("frameCompleteness", qv.frameCompleteness)
            put("constraintFit", qv.constraintFit)
            put("optionCoverage", qv.optionCoverage)
            put("reversibility", qv.reversibility)
            put("riskExposure", qv.riskExposure)
            put("disagreementScore", qv.disagreementScore)
            put("validationReadiness", qv.validationReadiness)
            put("complexityPenalty", qv.complexityPenalty)
        }.toString()
    }

    private fun parseStringList(jsonStr: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
        } catch (e: Exception) { /* Empty */ }
        return list
    }

    private fun stringListToJson(list: List<String>): String {
        val arr = JSONArray()
        for (item in list) {
            arr.put(item)
        }
        return arr.toString()
    }

    /**
     * Seeds initial production-grade technical architecture decisions
     * if the database is newly created.
     */
    suspend fun seedInitialDecisionsIfEmpty() = withContext(Dispatchers.IO) {
        val existing = decisionDao.getDecisionById("DEC-ARCH-001")
        if (existing == null) {
            val d1 = createModularMonolithDecision()
            val d2 = createDatabaseStrategyDecision()
            val d3 = createCachingStrategyDecision()

            saveDecision(d1)
            saveDecision(d2)
            saveDecision(d3)

            // Add blueprint edges between decisions
            addBlueprintEdge(
                BlueprintEdge(
                    id = "EDGE-001",
                    sourceId = d1.id,
                    targetId = d2.id,
                    relationType = EdgeRelationType.DEPENDS_ON
                )
            )
            addBlueprintEdge(
                BlueprintEdge(
                    id = "EDGE-002",
                    sourceId = d2.id,
                    targetId = d3.id,
                    relationType = EdgeRelationType.DEPENDS_ON
                )
            )

            // Create initial approved ADR for d1
            val adr1 = DeterministicAdrRenderer.renderMarkdown(
                decision = d1,
                adrNumber = 1,
                approvedBy = "Engr. Nirzor (Owner)",
                approvalTimestamp = System.currentTimeMillis() - 86400000L,
                packetHash = "7f8b91a24d88e0c3b991a030ef1d23456789abcdef0123456789abcdef012345"
            )
            saveAdr(adr1)

            // Seed outcome observation
            addOutcomeObservation(
                OutcomeObservation(
                    id = "OUT-001",
                    decisionId = d1.id,
                    adrNumber = 1,
                    expectedMetric = "API Latency p95 < 120ms at 5,000 req/sec; Zero cross-service serialization overhead.",
                    observedMetric = "API Latency p95 measured at 78ms; In-process domain calls reduced failure rate by 99.4%.",
                    divergenceClass = DivergenceClass.NONE,
                    reviewNotes = "Excellent alignment with expected architectural benefits. No invalidation detected."
                )
            )
        }
    }

    private fun createModularMonolithDecision(): Decision {
        val id = "DEC-ARCH-001"
        val title = "Primary Backend Architecture: Modular Monolith vs Microservices"
        val problem = "KingMaker v7 requires rock-solid transactional governance, strict RFC 8785 revision hashing, and low operational friction for a personal and small-workspace footprint. Should the system be deployed as a Modular Monolith with transactional event writes, or split immediately into independent Microservices?"

        val claims = listOf(
            EpistemicClaim("C1", "KingMaker requires atomic transactions between domain events and current state updates.", EpistemicType.CONSTRAINT),
            EpistemicClaim("C2", "Target audience is initially single-owner personal edition expanding to small workspaces.", EpistemicType.FACT),
            EpistemicClaim("C3", "Microservices introduce distributed transactions (2PC / Sagas) with potential partial failure windows.", EpistemicType.RISK),
            EpistemicClaim("C4", "A modular monolith with strict domain boundaries can later be extracted into microservices if scaling demands it.", EpistemicType.INFERENCE)
        )

        val questions = listOf(
            ContextQuestion(
                id = "Q1",
                questionEn = "Will the team have dedicated platform engineers to manage distributed tracing, service meshes, and saga orchestrations?",
                questionBn = "ডিস্ট্রিবিউটেড ট্রেসিং, সার্ভিস মেশ ও সাগা অর্কেস্ট্রেশন পরিচালনার জন্য কি ডেডিকেটেড প্ল্যাটফর্ম টিম থাকবে?",
                consequenceEn = "If No, microservices will induce severe operational drag.",
                consequenceBn = "না হলে মাইক্রোসার্ভিস সিস্টেমের মেইনটেইনেবিলিটি কমিয়ে দেবে।",
                category = "Operations",
                status = QuestionStatus.ANSWERED,
                answer = "No, single owner / small engineering footprint."
            ),
            ContextQuestion(
                id = "Q2",
                questionEn = "Is independent auto-scaling required for individual domain modules in year one?",
                questionBn = "প্রথম বছরে কি প্রতিটি মডিউলের জন্য আলাদা অটো-স্কেলিং প্রয়োজন?",
                consequenceEn = "Determines whether process boundaries are strictly needed.",
                consequenceBn = "আলাদা প্রসেস বাউন্ডারি অপরিহার্য কিনা তা নির্ধারণ করে।",
                category = "Scalability",
                status = QuestionStatus.ANSWERED,
                answer = "Worker process can scale independently; API remains unified."
            )
        )

        val options = listOf(
            DecisionOption(
                id = "OPT-001",
                title = "Modular Monolith + Independent Worker Process (Recommended)",
                description = "Unified codebase with clean domain boundaries in Kotlin, sharing PostgreSQL transactional boundary while running compute workers in a separate container.",
                pros = listOf("ACID transaction guarantees", "Single PostgreSQL data store", "Zero distributed saga bugs", "Fast local development"),
                cons = listOf("Shared process memory (isolated by modules)", "Single deployment artifact for core API"),
                estimatedMonthlyCost = "$15/month",
                reversibilityScore = 0.88f,
                isRecommended = true
            ),
            DecisionOption(
                id = "OPT-002",
                title = "Choreographed Microservices (5 independent services)",
                description = "Separate services for Identity, Decision Core, Execution Worker, Graph Service, and Notifications using Kafka or RabbitMQ event broker.",
                pros = listOf("Independent language stacks", "Fault isolation per service"),
                cons = listOf("High network latency", "Complex distributed sagas", "High cloud infrastructure cost ($200+/mo)", "Eventual consistency risks"),
                estimatedMonthlyCost = "$240/month",
                reversibilityScore = 0.35f,
                isRecommended = false
            )
        )

        val criteria = listOf(
            EvaluationCriterion("CRIT-1", "Transactional Atomicity", 0.35f, "Guaranteed all-or-nothing writes for governance records."),
            EvaluationCriterion("CRIT-2", "Operational Simplicity", 0.30f, "Can be managed and debugged by a single engineer."),
            EvaluationCriterion("CRIT-3", "Cost Sustainability", 0.20f, "Low idle infrastructure overhead."),
            EvaluationCriterion("CRIT-4", "Migration Path", 0.15f, "Ease of future architectural evolution.")
        )

        val perspectives = listOf(
            ExpertPerspective(
                id = "P1",
                role = ExpertRole.SOFTWARE_ARCHITECT,
                position = "Supports Modular Monolith",
                keyArgument = "Modular Monolith preserves clean Hexagonal boundaries without the distributed complexity penalty.",
                identifiedRisks = listOf("Accidental code coupling if internal interfaces are breached."),
                dissentNote = null,
                confidenceScore = 0.92f,
                provenanceMode = ProvenanceMode.PROVIDER
            ),
            ExpertPerspective(
                id = "P2",
                role = ExpertRole.DEVILS_ADVOCATE,
                position = "Adversarial Critique",
                keyArgument = "If worker execution crashes due to heavy AI payloads, it could threaten API responsiveness if co-located.",
                identifiedRisks = listOf("Need strict process separation between API and Worker."),
                dissentNote = "Ensure one image, two runtime roles per ADR-V7-009.",
                confidenceScore = 0.88f,
                provenanceMode = ProvenanceMode.PROVIDER
            )
        )

        val critiques = listOf(
            CritiqueFinding(
                id = "CF-1",
                dimension = CritiqueDimension.CDR_07,
                targetOptionId = "OPT-001",
                targetOptionTitle = "Modular Monolith",
                severity = "MEDIUM",
                objection = "CPU-intensive task serialization could block API threads if threads are shared.",
                evidenceGap = "Benchmark thread pool isolation under 100 concurrent mock debate runs.",
                suggestedFalsificationTest = "Deploy separate container worker process with SKIP LOCKED PostgreSQL queue leasing."
            )
        )

        val qv = QualityVector(
            evidenceStrength = 0.88f,
            frameCompleteness = 0.92f,
            constraintFit = 0.95f,
            optionCoverage = 0.85f,
            reversibility = 0.88f,
            riskExposure = 0.12f,
            disagreementScore = 0.20f,
            validationReadiness = 0.90f,
            complexityPenalty = 0.10f
        )

        val revHash = JcsHasher.computeRevisionHash(
            id, 1, title, problem,
            claimsToJson(claims),
            optionsToJson(options)
        )

        return Decision(
            id = id,
            title = title,
            rawProblemStatement = problem,
            status = DecisionStatus.APPROVED,
            tier = DecisionTier.T2,
            currentRevisionNumber = 1,
            revisionHash = revHash,
            createdAt = System.currentTimeMillis() - 172800000L,
            updatedAt = System.currentTimeMillis() - 86400000L,
            claims = claims,
            questions = questions,
            options = options,
            criteria = criteria,
            perspectives = perspectives,
            critiques = critiques,
            qualityVector = qv,
            recommendedOptionId = "OPT-001",
            decisiveRationale = "Modular Monolith provides 100% ACID consistency for governance events while drastically reducing operational costs to $15/month.",
            whatCouldChangeThis = "If multi-region active-active deployment or a 50+ person engineering team becomes mandatory.",
            adrId = "ADR-$id-1",
            dependencies = emptyList()
        )
    }

    private fun createDatabaseStrategyDecision(): Decision {
        val id = "DEC-DATA-002"
        val title = "Persistence Authority: PostgreSQL vs DynamoDB NoSQL"
        val problem = "KingMaker mandates append-only immutable event histories, strict ACID transactions, and deterministic revision ordering. Should the primary authority store be PostgreSQL or a managed NoSQL store like DynamoDB/Firestore?"

        val claims = listOf(
            EpistemicClaim("C1", "PostgreSQL provides transactional triggers, FOR UPDATE SKIP LOCKED queues, and JSONB canonical indexing.", EpistemicType.FACT),
            EpistemicClaim("C2", "Firestore/DynamoDB cannot enforce cross-document transactional append constraints with immutable DB triggers.", EpistemicType.CONSTRAINT)
        )

        val options = listOf(
            DecisionOption(
                id = "OPT-201",
                title = "PostgreSQL (Authority) + Neon/Cloud Run (Recommended)",
                description = "PostgreSQL as system of record for decisions, revisions, approvals, and immutable events with database-level immutability triggers.",
                pros = listOf("Strict ACID", "JSONB indexing", "Skip Locked queue leasing", "Replay verification"),
                cons = listOf("Requires connection pooling for serverless (PgBouncer/Neon)"),
                estimatedMonthlyCost = "$0-$15/month",
                reversibilityScore = 0.85f,
                isRecommended = true
            ),
            DecisionOption(
                id = "OPT-202",
                title = "Serverless NoSQL (Firestore as Authority)",
                description = "Use Firestore as the primary domain authority.",
                pros = listOf("Built-in mobile real-time listeners"),
                cons = listOf("Cannot guarantee I-04 authority invariants", "Weak revision concurrency controls"),
                estimatedMonthlyCost = "$10/month",
                reversibilityScore = 0.50f,
                isRecommended = false
            )
        )

        val revHash = JcsHasher.computeRevisionHash(
            id, 1, title, problem,
            claimsToJson(claims),
            optionsToJson(options)
        )

        return Decision(
            id = id,
            title = title,
            rawProblemStatement = problem,
            status = DecisionStatus.APPROVED,
            tier = DecisionTier.T2,
            currentRevisionNumber = 1,
            revisionHash = revHash,
            createdAt = System.currentTimeMillis() - 86400000L,
            updatedAt = System.currentTimeMillis() - 43200000L,
            claims = claims,
            options = options,
            recommendedOptionId = "OPT-201",
            decisiveRationale = "PostgreSQL is selected as canonical authority per KingMaker ADR-V7-006.",
            whatCouldChangeThis = "Nothing in v7 scope; PostgreSQL authority is a constitutional invariant.",
            adrId = "ADR-$id-2",
            dependencies = listOf("DEC-ARCH-001")
        )
    }

    private fun createCachingStrategyDecision(): Decision {
        val id = "DEC-CACHE-003"
        val title = "Distributed Caching & Rate Limiting: Redis vs In-Memory Caffeine"
        val problem = "Should KingMaker deploy a shared Redis instance for API rate limiting and transient task leases, or rely on PostgreSQL row-level locks and in-memory caches?"

        val claims = listOf(
            EpistemicClaim("C1", "Personal deployment targets single owner with minimal idle costs.", EpistemicType.PREFERENCE),
            EpistemicClaim("C2", "PostgreSQL SKIP LOCKED eliminates the strict need for an external Redis queue in v1.", EpistemicType.FACT)
        )

        val options = listOf(
            DecisionOption(
                id = "OPT-301",
                title = "PostgreSQL SKIP LOCKED + In-Memory Leases (Recommended)",
                description = "Rely on PostgreSQL ACID job leasing for tasks, avoiding extra Redis hosting charges.",
                pros = listOf("Zero additional infrastructure", "Transactional job rollback", "Auditable job state"),
                cons = listOf("Slightly higher DB IOPS under extreme polling"),
                estimatedMonthlyCost = "$0/month",
                reversibilityScore = 0.90f,
                isRecommended = true
            ),
            DecisionOption(
                id = "OPT-302",
                title = "Dedicated Redis Cloud Cluster",
                description = "Deploy managed Redis for distributed token buckets and Pub/Sub.",
                pros = listOf("Sub-millisecond rate limiting"),
                cons = listOf("Extra $15-$30/month cost", "Another failure point in personal stack"),
                estimatedMonthlyCost = "$25/month",
                reversibilityScore = 0.60f,
                isRecommended = false
            )
        )

        val revHash = JcsHasher.computeRevisionHash(
            id, 1, title, problem,
            claimsToJson(claims),
            optionsToJson(options)
        )

        return Decision(
            id = id,
            title = title,
            rawProblemStatement = problem,
            status = DecisionStatus.READY_FOR_DEBATE,
            tier = DecisionTier.T1,
            currentRevisionNumber = 1,
            revisionHash = revHash,
            createdAt = System.currentTimeMillis() - 36000000L,
            updatedAt = System.currentTimeMillis(),
            claims = claims,
            options = options,
            recommendedOptionId = "OPT-301",
            decisiveRationale = "Aligns with KingMaker ADR-V7-008 for personal deployment efficiency.",
            whatCouldChangeThis = "Scaling beyond 100 concurrent workers.",
            dependencies = listOf("DEC-DATA-002")
        )
    }
}
