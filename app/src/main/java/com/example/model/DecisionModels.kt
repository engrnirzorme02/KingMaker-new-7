package com.example.model

/**
 * Epistemic types mandated by KingMaker v7 Specification.
 * UNKNOWN is meaningful decision information.
 * ASSUMPTION is not FACT. INFERENCE is not verified evidence.
 */
enum class EpistemicType {
    FACT,
    CONSTRAINT,
    PREFERENCE,
    INFERENCE,
    RECOMMENDATION,
    RISK,
    ASSUMPTION,
    UNKNOWN,
    UNVERIFIED
}

enum class ProvenanceMode {
    SIMULATED,
    REPLAY,
    PROVIDER
}

enum class TrustState {
    UNREVIEWED,
    USER_ATTESTED,
    VERIFIED_SOURCE,
    DISPUTED,
    STALE
}

enum class DecisionStatus {
    RAW,
    INTERPRETATION,
    CONTEXT,
    FRAMING,
    READY_FOR_DEBATE,
    RUNNING,
    HUMAN_REVIEW,
    APPROVED,
    REJECTED,
    DEFERRED,
    REVISION_REQUESTED
}

enum class DecisionTier {
    T1, // Low consequence / highly reversible
    T2, // Material technical consequence
    T3  // High impact / low reversibility / sensitive (requires High Thinking & pre-mortem)
}

enum class QuestionStatus {
    PENDING,
    ANSWERED,
    UNKNOWN,
    NOT_APPLICABLE,
    DEFERRED
}

enum class ExpertRole(val displayName: String, val mandate: String) {
    SOFTWARE_ARCHITECT("Software Architect", "System boundaries, architectural patterns & modularity"),
    SECURITY_ARCHITECT("Security Architect", "Threat surfaces, credential handling & trust boundaries"),
    RELIABILITY_ENGINEER("Reliability / SRE", "Failure modes, degradation, recovery & SLA adherence"),
    DATA_ARCHITECT("Data Architect", "Storage integrity, sync correctness & schema evolution"),
    FINOPS_ANALYST("FinOps Analyst", "Cost sustainability, infrastructure & token budgets"),
    DEVILS_ADVOCATE("Devil's Advocate", "Adversarial critique targeting hidden assumptions & fragility"),
    COVERAGE_AUDITOR("Coverage Auditor", "Missing domains, unverified constraints & blindspots"),
    SYNTHESIS_LEAD("Synthesis Lead", "Neutral, criteria-based trade-off resolution & recommendation")
}

/**
 * CDR-P1: KingMaker Personal 11-Dimension Critique Registry
 */
enum class CritiqueDimension(val code: String, val title: String, val question: String) {
    CDR_01("CDR-01", "Problem & Goal Integrity", "Are we solving the right decision and outcome?"),
    CDR_02("CDR-02", "Context & Constraint Integrity", "Are material facts, constraints and unknowns explicit?"),
    CDR_03("CDR-03", "Option & Trade-off Coverage", "Are meaningful alternatives and trade-offs represented?"),
    CDR_04("CDR-04", "Technical Feasibility & Architecture Fit", "Can the proposed option actually work within the architecture?"),
    CDR_05("CDR-05", "Data & Integration Integrity", "Are data flows, interfaces and dependencies coherent?"),
    CDR_06("CDR-06", "Security, Privacy & Trust Boundary", "Are security/privacy risks and trust boundary violations addressed?"),
    CDR_07("CDR-07", "Reliability, Ops & Failure Modes", "What happens under failure, degradation and recovery?"),
    CDR_08("CDR-08", "Scalability, Performance & Complexity", "Does the option remain sustainable as scale grows?"),
    CDR_09("CDR-09", "Cost & Resource Sustainability", "Are recurring, one-time and operational costs proportionate?"),
    CDR_10("CDR-10", "UX, Human Factors & Adoption", "Can the affected humans understand, operate and recover?"),
    CDR_11("CDR-11", "Evidence, Governance & System Integrity", "Is the decision evidence-aware, policy-compliant and boundary-safe?")
}

enum class PresentationLens {
    EXECUTIVE,
    ARCHITECTURE,
    UX,
    DEVELOPER,
    GOVERNANCE
}

enum class EdgeRelationType {
    DEPENDS_ON,
    CONSTRAINS,
    SUPPORTS,
    SUPERSEDES,
    CONTRADICTS
}

enum class DivergenceClass {
    NONE,
    EXPECTED_VARIANCE,
    EVIDENCE_GAP,
    OPERATIONAL_ISSUE,
    DECISION_INVALIDATION
}

data class EpistemicClaim(
    val id: String,
    val text: String,
    val type: EpistemicType,
    val confidence: Float = 0.8f,
    val evidenceSource: String? = null
)

data class ContextQuestion(
    val id: String,
    val questionEn: String,
    val questionBn: String,
    val consequenceEn: String,
    val consequenceBn: String,
    val category: String,
    var status: QuestionStatus = QuestionStatus.PENDING,
    var answer: String = ""
)

data class DecisionOption(
    val id: String,
    val title: String,
    val description: String,
    val pros: List<String> = emptyList(),
    val cons: List<String> = emptyList(),
    val estimatedMonthlyCost: String = "$0",
    val reversibilityScore: Float = 0.7f,
    val isRecommended: Boolean = false
)

data class EvaluationCriterion(
    val id: String,
    val name: String,
    val weight: Float,
    val description: String
)

data class ExpertPerspective(
    val id: String,
    val role: ExpertRole,
    val position: String,
    val keyArgument: String,
    val criteriaAssessments: Map<String, String> = emptyMap(),
    val identifiedRisks: List<String> = emptyList(),
    val dissentNote: String? = null,
    val confidenceScore: Float = 0.85f,
    val provenanceMode: ProvenanceMode = ProvenanceMode.PROVIDER
)

data class CritiqueFinding(
    val id: String,
    val dimension: CritiqueDimension,
    val targetOptionId: String,
    val targetOptionTitle: String,
    val severity: String, // CRITICAL, HIGH, MEDIUM, LOW
    val objection: String,
    val evidenceGap: String,
    val suggestedFalsificationTest: String
)

data class QualityVector(
    val evidenceStrength: Float = 0.75f,
    val frameCompleteness: Float = 0.85f,
    val constraintFit: Float = 0.80f,
    val optionCoverage: Float = 0.70f,
    val reversibility: Float = 0.65f,
    val riskExposure: Float = 0.25f, // lower is better
    val disagreementScore: Float = 0.30f,
    val validationReadiness: Float = 0.80f,
    val complexityPenalty: Float = 0.15f
) {
    val compositeScore: Float
        get() = ((evidenceStrength + frameCompleteness + constraintFit + optionCoverage + reversibility + validationReadiness - riskExposure - complexityPenalty) / 6f).coerceIn(0f, 1f)
}

data class Decision(
    val id: String,
    val title: String,
    val rawProblemStatement: String,
    val status: DecisionStatus = DecisionStatus.RAW,
    val tier: DecisionTier = DecisionTier.T2,
    val currentRevisionNumber: Int = 1,
    val revisionHash: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val claims: List<EpistemicClaim> = emptyList(),
    val questions: List<ContextQuestion> = emptyList(),
    val options: List<DecisionOption> = emptyList(),
    val criteria: List<EvaluationCriterion> = emptyList(),
    val perspectives: List<ExpertPerspective> = emptyList(),
    val critiques: List<CritiqueFinding> = emptyList(),
    val qualityVector: QualityVector = QualityVector(),
    val recommendedOptionId: String? = null,
    val decisiveRationale: String = "",
    val whatCouldChangeThis: String = "",
    val adrId: String? = null,
    val dependencies: List<String> = emptyList() // Target decision IDs
)

data class AdrRecord(
    val id: String,
    val decisionId: String,
    val adrNumber: Int,
    val title: String,
    val status: String = "APPROVED",
    val context: String,
    val decisionOutcome: String,
    val chosenOption: String,
    val consequences: String,
    val revisionHash: String,
    val packetHash: String,
    val approvedBy: String,
    val approvalTimestamp: Long,
    val renderedMarkdown: String
)

data class BlueprintNode(
    val id: String,
    val decisionId: String,
    val title: String,
    val tier: DecisionTier,
    val status: DecisionStatus,
    val adrNumber: Int? = null
)

data class BlueprintEdge(
    val id: String,
    val sourceId: String,
    val targetId: String,
    val relationType: EdgeRelationType
)

data class OutcomeObservation(
    val id: String,
    val decisionId: String,
    val adrNumber: Int,
    val expectedMetric: String,
    val observedMetric: String,
    val divergenceClass: DivergenceClass,
    val observationDate: Long = System.currentTimeMillis(),
    val reviewNotes: String,
    val warrantsSupersedingRevision: Boolean = false
)

data class ChatMessage(
    val id: String,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelName: String = "gemini-3.5-flash",
    val thinkingProcess: String? = null,
    val sources: List<String> = emptyList()
)
