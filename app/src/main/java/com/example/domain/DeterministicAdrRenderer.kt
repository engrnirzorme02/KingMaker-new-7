package com.example.domain

import com.example.model.AdrRecord
import com.example.model.Decision
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DeterministicAdrRenderer {

    fun renderMarkdown(
        decision: Decision,
        adrNumber: Int,
        approvedBy: String,
        approvalTimestamp: Long,
        packetHash: String
    ): AdrRecord {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US)
        val formattedDate = dateFormat.format(Date(approvalTimestamp))

        val chosenOption = decision.options.find { it.id == decision.recommendedOptionId }
            ?: decision.options.firstOrNull()

        val md = buildString {
            appendLine("# ADR-${String.format("%04d", adrNumber)}: ${decision.title}")
            appendLine()
            appendLine("**Status:** APPROVED")
            appendLine("**Date:** $formattedDate")
            appendLine("**Author / Decision Owner:** $approvedBy")
            appendLine("**Tier:** ${decision.tier.name}")
            appendLine("**Revision Hash (JCS SHA-256):** `${decision.revisionHash}`")
            appendLine("**Review Packet Hash:** `$packetHash`")
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## 1. Context and Problem Statement")
            appendLine(decision.rawProblemStatement)
            appendLine()
            if (decision.claims.isNotEmpty()) {
                appendLine("### Epistemic Grounding (Claims & Constraints)")
                decision.claims.forEach { claim ->
                    appendLine("- **[${claim.type.name}]** ${claim.text}")
                }
                appendLine()
            }

            appendLine("## 2. Considered Alternatives")
            decision.options.forEach { opt ->
                val marker = if (opt.id == chosenOption?.id) " (SELECTED)" else ""
                appendLine("### Option: ${opt.title}$marker")
                appendLine(opt.description)
                if (opt.pros.isNotEmpty()) {
                    appendLine("- *Pros:* ${opt.pros.joinToString("; ")}")
                }
                if (opt.cons.isNotEmpty()) {
                    appendLine("- *Cons:* ${opt.cons.joinToString("; ")}")
                }
                appendLine("- *Est. Monthly Cost:* ${opt.estimatedMonthlyCost} | *Reversibility:* ${(opt.reversibilityScore * 100).toInt()}%")
                appendLine()
            }

            appendLine("## 3. Decision Outcome")
            appendLine("Chosen Option: **${chosenOption?.title ?: "N/A"}**")
            appendLine()
            appendLine("### Decision Rationale")
            appendLine(decision.decisiveRationale.ifBlank {
                "Selected on the basis of superior architectural alignment, lower reversibility friction, and strong quality vector confirmation."
            })
            appendLine()

            if (decision.whatCouldChangeThis.isNotBlank()) {
                appendLine("### Revisit Triggers (\"What could change this?\")")
                appendLine(decision.whatCouldChangeThis)
                appendLine()
            }

            appendLine("## 4. Consequences and Validation Plan")
            appendLine("- Positive: Provides deterministic, auditable governance with clear responsibility boundaries.")
            appendLine("- Negative / Trade-offs: Requires conscious verification discipline and step-up authorization.")
            appendLine()

            appendLine("## 5. Quality Vector Confirmation")
            appendLine("- Evidence Strength: ${(decision.qualityVector.evidenceStrength * 100).toInt()}%")
            appendLine("- Frame Completeness: ${(decision.qualityVector.frameCompleteness * 100).toInt()}%")
            appendLine("- Constraint Fit: ${(decision.qualityVector.constraintFit * 100).toInt()}%")
            appendLine("- Option Coverage: ${(decision.qualityVector.optionCoverage * 100).toInt()}%")
            appendLine("- Reversibility: ${(decision.qualityVector.reversibility * 100).toInt()}%")
            appendLine("- Composite Quality Index: ${(decision.qualityVector.compositeScore * 100).toInt()}%")
            appendLine()
            appendLine("---")
            appendLine("*Rendered deterministically by KingMaker v7.0 Canonical Governance Engine.*")
        }

        return AdrRecord(
            id = "ADR-${decision.id}-$adrNumber",
            decisionId = decision.id,
            adrNumber = adrNumber,
            title = decision.title,
            status = "APPROVED",
            context = decision.rawProblemStatement,
            decisionOutcome = chosenOption?.title ?: "Selected Architecture",
            chosenOption = chosenOption?.title ?: "Selected Architecture",
            consequences = "Deterministic governance record locked into immutable history.",
            revisionHash = decision.revisionHash,
            packetHash = packetHash,
            approvedBy = approvedBy,
            approvalTimestamp = approvalTimestamp,
            renderedMarkdown = md
        )
    }
}
