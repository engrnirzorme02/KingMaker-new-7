package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Decision
import com.example.model.DecisionStatus
import com.example.ui.KingMakerViewModel
import com.example.ui.Screen
import com.example.ui.components.KingMakerBottomNav
import com.example.ui.components.KingMakerTopBar
import com.example.ui.screens.AdrDetailScreen
import com.example.ui.screens.CaptureIntakeScreen
import com.example.ui.screens.ContextQuestionsScreen
import com.example.ui.screens.CouncilChatScreen
import com.example.ui.screens.DebateRunScreen
import com.example.ui.screens.DecisionsListScreen
import com.example.ui.screens.FramingScreen
import com.example.ui.screens.LiveVoiceScreen
import com.example.ui.screens.LivingBlueprintScreen
import com.example.ui.screens.OutcomeLoopScreen
import com.example.ui.screens.ReviewGovernanceScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.KingDarkBackground
import com.example.ui.theme.KingMakerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: KingMakerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KingMakerTheme {
                KingMakerApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun KingMakerApp(viewModel: KingMakerViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isBangla by viewModel.isBangla.collectAsStateWithLifecycle()
    val currentLens by viewModel.currentLens.collectAsStateWithLifecycle()
    val activeDecision by viewModel.activeDecision.collectAsStateWithLifecycle()
    val activeAdr by viewModel.activeAdr.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val notification by viewModel.notification.collectAsStateWithLifecycle()
    val isVoiceActive by viewModel.isVoiceActive.collectAsStateWithLifecycle()
    val voiceStatusText by viewModel.voiceStatusText.collectAsStateWithLifecycle()
    val isPlayingAudio by viewModel.isPlayingAudio.collectAsStateWithLifecycle()

    val decisions by viewModel.decisions.collectAsStateWithLifecycle()
    val adrs by viewModel.adrs.collectAsStateWithLifecycle()
    val blueprintEdges by viewModel.blueprintEdges.collectAsStateWithLifecycle()
    val outcomes by viewModel.outcomes.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val userSession by viewModel.userSession.collectAsStateWithLifecycle()
    val cycleAnalysis by viewModel.cycleAnalysis.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notification) {
        notification?.let {
            val msg = if (isBangla) it.messageBn else it.messageEn
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissNotification()
        }
    }

    // BackHandler per guidelines
    BackHandler(enabled = currentScreen != Screen.DECISIONS_LIST) {
        when (currentScreen) {
            Screen.CAPTURE_INTAKE,
            Screen.LIVING_BLUEPRINT,
            Screen.OUTCOME_LOOP,
            Screen.COUNCIL_CHAT,
            Screen.LIVE_VOICE,
            Screen.SETTINGS -> {
                viewModel.navigateTo(Screen.DECISIONS_LIST)
            }
            Screen.CONTEXT_QUESTIONS -> {
                viewModel.navigateTo(Screen.CAPTURE_INTAKE)
            }
            Screen.FRAMING -> {
                viewModel.navigateTo(Screen.CONTEXT_QUESTIONS)
            }
            Screen.DEBATE_RUN -> {
                viewModel.navigateTo(Screen.FRAMING)
            }
            Screen.REVIEW_GOVERNANCE -> {
                viewModel.navigateTo(Screen.DEBATE_RUN)
            }
            Screen.ADR_DETAIL -> {
                viewModel.navigateTo(Screen.DECISIONS_LIST)
            }
            else -> {
                viewModel.navigateTo(Screen.DECISIONS_LIST)
            }
        }
    }

    val topBarTitle = when (currentScreen) {
        Screen.DECISIONS_LIST -> if (isBangla) "KingMaker সিদ্ধান্ত তালিকা" else "KingMaker Decisions"
        Screen.CAPTURE_INTAKE -> if (isBangla) "নতুন সিদ্ধান্ত ইনটেক (D1)" else "Intake Capture (D1)"
        Screen.CONTEXT_QUESTIONS -> if (isBangla) "প্রাসঙ্গিক প্রশ্নাবলী (D2)" else "Context QA (D2)"
        Screen.FRAMING -> if (isBangla) "ফ্রেমিং ও সিলিং (D3)" else "Framing & Sealing (D3)"
        Screen.DEBATE_RUN -> if (isBangla) "কাউন্সিল ও রেড-টিম (D4-D6)" else "Council Debate (D4-D6)"
        Screen.REVIEW_GOVERNANCE -> if (isBangla) "মানবীয় গভর্ন্যান্স (D7)" else "Governance Review (D7)"
        Screen.ADR_DETAIL -> if (isBangla) "আর্কিটেকচার রেকর্ড (ADR)" else "Decision Record (ADR)"
        Screen.LIVING_BLUEPRINT -> if (isBangla) "লিভিং ব্লুপ্রিন্ট গ্রাফ" else "Living Blueprint Graph"
        Screen.OUTCOME_LOOP -> if (isBangla) "ফলাফল পর্যবেক্ষণ লুপ" else "Outcome Observation"
        Screen.COUNCIL_CHAT -> if (isBangla) "কাউন্সিল চ্যাটবট" else "Council Copilot"
        Screen.LIVE_VOICE -> if (isBangla) "লাইভ ভয়েস আলোচনা" else "Live Voice Debrief"
        Screen.SETTINGS -> if (isBangla) "সেটিংস ও প্রোফাইল" else "Settings & Profile"
    }

    Scaffold(
        topBar = {
            KingMakerTopBar(
                title = topBarTitle,
                canNavigateBack = currentScreen != Screen.DECISIONS_LIST,
                onNavigateBack = {
                    if (currentScreen != Screen.DECISIONS_LIST) {
                        viewModel.navigateTo(Screen.DECISIONS_LIST)
                    }
                },
                isBangla = isBangla,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onOpenLiveVoice = { viewModel.navigateTo(Screen.LIVE_VOICE) },
                currentLens = if (currentScreen == Screen.REVIEW_GOVERNANCE) currentLens else null,
                onSelectLens = { viewModel.setLens(it) }
            )
        },
        bottomBar = {
            KingMakerBottomNav(
                currentScreen = currentScreen,
                onSelectScreen = { viewModel.navigateTo(it) },
                isBangla = isBangla
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(KingDarkBackground)
        ) {
            when (currentScreen) {
                Screen.DECISIONS_LIST -> {
                    DecisionsListScreen(
                        decisions = decisions,
                        isBangla = isBangla,
                        onSelectDecision = { decision ->
                            viewModel.selectDecision(decision)
                            when (decision.status) {
                                DecisionStatus.RAW, DecisionStatus.INTERPRETATION -> viewModel.navigateTo(Screen.CONTEXT_QUESTIONS)
                                DecisionStatus.CONTEXT -> viewModel.navigateTo(Screen.CONTEXT_QUESTIONS)
                                DecisionStatus.FRAMING -> viewModel.navigateTo(Screen.FRAMING)
                                DecisionStatus.READY_FOR_DEBATE, DecisionStatus.RUNNING -> viewModel.navigateTo(Screen.DEBATE_RUN)
                                DecisionStatus.HUMAN_REVIEW -> viewModel.navigateTo(Screen.REVIEW_GOVERNANCE)
                                DecisionStatus.APPROVED -> {
                                    val adr = adrs.find { it.decisionId == decision.id }
                                    if (adr != null) {
                                        viewModel.selectAdr(adr)
                                    } else {
                                        viewModel.navigateTo(Screen.REVIEW_GOVERNANCE)
                                    }
                                }
                                else -> viewModel.navigateTo(Screen.REVIEW_GOVERNANCE)
                            }
                        },
                        onCreateNewDecision = {
                            viewModel.navigateTo(Screen.CAPTURE_INTAKE)
                        }
                    )
                }

                Screen.CAPTURE_INTAKE -> {
                    CaptureIntakeScreen(
                        isBangla = isBangla,
                        isLoading = isLoading,
                        onSubmitIntake = { rawProblem, title, tier ->
                            viewModel.createAndInterpretDecision(rawProblem, title, tier)
                        },
                        onTranscribeAudio = { audioBytes, onResult ->
                            viewModel.transcribeAudioRecord(audioBytes, onResult)
                        },
                        onAnalyzeDiagramImage = { bitmap, onResult ->
                            viewModel.analyzeArchitectureDiagram(bitmap, onResult)
                        }
                    )
                }

                Screen.CONTEXT_QUESTIONS -> {
                    activeDecision?.let { decision ->
                        ContextQuestionsScreen(
                            decision = decision,
                            isBangla = isBangla,
                            onAnswerQuestion = { qId, ans, status ->
                                viewModel.answerQuestion(qId, ans, status)
                            },
                            onProceedToFraming = {
                                viewModel.navigateTo(Screen.FRAMING)
                            }
                        )
                    } ?: DecisionsListScreen(
                        decisions = decisions,
                        isBangla = isBangla,
                        onSelectDecision = { viewModel.selectDecision(it) },
                        onCreateNewDecision = { viewModel.navigateTo(Screen.CAPTURE_INTAKE) }
                    )
                }

                Screen.FRAMING -> {
                    activeDecision?.let { decision ->
                        FramingScreen(
                            decision = decision,
                            isBangla = isBangla,
                            onConfirmFraming = {
                                viewModel.confirmFraming()
                            }
                        )
                    } ?: viewModel.navigateTo(Screen.DECISIONS_LIST)
                }

                Screen.DEBATE_RUN -> {
                    activeDecision?.let { decision ->
                        DebateRunScreen(
                            decision = decision,
                            isBangla = isBangla,
                            isLoading = isLoading,
                            onRunDebate = { highThinking ->
                                viewModel.runDebate(highThinking)
                            },
                            onProceedToReview = {
                                viewModel.navigateTo(Screen.REVIEW_GOVERNANCE)
                            }
                        )
                    } ?: viewModel.navigateTo(Screen.DECISIONS_LIST)
                }

                Screen.REVIEW_GOVERNANCE -> {
                    activeDecision?.let { decision ->
                        ReviewGovernanceScreen(
                            decision = decision,
                            isBangla = isBangla,
                            currentLens = currentLens,
                            onSelectLens = { viewModel.setLens(it) },
                            isLoading = isLoading,
                            onPerformGovernanceAction = { action, rationale ->
                                viewModel.performGovernanceAction(action, rationale)
                            }
                        )
                    } ?: viewModel.navigateTo(Screen.DECISIONS_LIST)
                }

                Screen.ADR_DETAIL -> {
                    activeAdr?.let { adr ->
                        AdrDetailScreen(
                            adr = adr,
                            isBangla = isBangla,
                            isLoading = isLoading,
                            isPlayingAudio = isPlayingAudio,
                            onSpeakSummary = { textToSpeak ->
                                viewModel.speakDecisionSummary(textToSpeak)
                            },
                            onStopAudio = { viewModel.stopAudio() },
                            onNotify = { en, bn -> viewModel.notify(en, bn) }
                        )
                    } ?: viewModel.navigateTo(Screen.DECISIONS_LIST)
                }

                Screen.LIVING_BLUEPRINT -> {
                    LivingBlueprintScreen(
                        decisions = decisions,
                        edges = blueprintEdges,
                        cycleAnalysis = cycleAnalysis,
                        isBangla = isBangla,
                        onRunTarjanAnalysis = { viewModel.runCycleAnalysis() },
                        onResolveCycle = { edgeId -> viewModel.resolveCycleByBreakingEdge(edgeId) },
                        onSelectDecision = { d ->
                            viewModel.selectDecision(d)
                            viewModel.navigateTo(Screen.REVIEW_GOVERNANCE)
                        }
                    )
                }

                Screen.OUTCOME_LOOP -> {
                    OutcomeLoopScreen(
                        outcomes = outcomes,
                        decisions = decisions,
                        isBangla = isBangla,
                        onRecordOutcome = { dId, adrNum, exp, obs, div, notes ->
                            viewModel.recordOutcome(dId, adrNum, exp, obs, div, notes)
                        },
                        onSelectDecision = { d ->
                            viewModel.selectDecision(d)
                            viewModel.navigateTo(Screen.REVIEW_GOVERNANCE)
                        }
                    )
                }

                Screen.COUNCIL_CHAT -> {
                    CouncilChatScreen(
                        messages = chatMessages,
                        isLoading = isLoading,
                        isBangla = isBangla,
                        onSendMessage = { text, useSearch, useMaps ->
                            viewModel.sendChatMessage(text, useSearch, useMaps)
                        },
                        onClearChat = { viewModel.clearChatHistory() }
                    )
                }

                Screen.LIVE_VOICE -> {
                    LiveVoiceScreen(
                        isVoiceActive = isVoiceActive,
                        statusText = voiceStatusText,
                        isBangla = isBangla,
                        onToggleLiveVoice = { viewModel.toggleLiveVoiceDebrief() }
                    )
                }

                Screen.SETTINGS -> {
                    SettingsScreen(
                        userSession = userSession,
                        isBangla = isBangla,
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        onSignOut = { viewModel.authManager.signOut() },
                        onSimulateLogin = { email, name -> viewModel.authManager.simulateSignIn(email, name) },
                        onResetInitialData = {
                            // Seed fresh decisions
                        }
                    )
                }
            }
        }
    }
}
