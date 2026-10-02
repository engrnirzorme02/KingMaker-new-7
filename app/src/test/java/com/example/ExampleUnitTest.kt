package com.example

import com.example.domain.DeterministicAdrRenderer
import com.example.domain.JcsHasher
import com.example.domain.TarjanGraphService
import com.example.model.BlueprintEdge
import com.example.model.BlueprintNode
import com.example.model.Decision
import com.example.model.DecisionOption
import com.example.model.DecisionStatus
import com.example.model.DecisionTier
import com.example.model.EdgeRelationType
import com.example.model.EpistemicClaim
import com.example.model.EpistemicType
import com.example.model.QualityVector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testJcsCanonicalizationAndSha256Determinism() {
        val jsonA = "{\"title\":\"Database Strategy\",\"decisionId\":\"DEC-01\",\"revisionNumber\":1}"
        val jsonB = "{\"revisionNumber\":1,\"decisionId\":\"DEC-01\",\"title\":\"Database Strategy\"}"

        val canonA = JcsHasher.canonicalize(jsonA)
        val canonB = JcsHasher.canonicalize(jsonB)

        assertEquals("JCS should produce identical canonical string regardless of key ordering", canonA, canonB)

        val hashA = JcsHasher.sha256Hex(canonA)
        val hashB = JcsHasher.sha256Hex(canonB)

        assertEquals("JCS SHA-256 hash must be strictly deterministic", hashA, hashB)
        assertEquals(64, hashA.length)
    }

    @Test
    fun testTarjanSccCycleDetectionAndResolution() {
        val nodeA = BlueprintNode("N1", "DEC-1", "Service A", DecisionTier.T2, DecisionStatus.APPROVED)
        val nodeB = BlueprintNode("N2", "DEC-2", "Service B", DecisionTier.T2, DecisionStatus.APPROVED)
        val nodeC = BlueprintNode("N3", "DEC-3", "Service C", DecisionTier.T2, DecisionStatus.APPROVED)

        // Create a circular dependency: A -> B -> C -> A
        val edge1 = BlueprintEdge("E1", "N1", "N2", EdgeRelationType.DEPENDS_ON)
        val edge2 = BlueprintEdge("E2", "N2", "N3", EdgeRelationType.DEPENDS_ON)
        val edge3 = BlueprintEdge("E3", "N3", "N1", EdgeRelationType.DEPENDS_ON)

        val resultWithCycle = TarjanGraphService.analyze(
            listOf(nodeA, nodeB, nodeC),
            listOf(edge1, edge2, edge3)
        )

        assertTrue("Tarjan SCC must detect circular dependency", resultWithCycle.hasCycles)
        assertEquals(1, resultWithCycle.cycles.size)
        assertEquals(3, resultWithCycle.cycles[0].size)
        assertTrue(resultWithCycle.proposals.isNotEmpty())

        // Break edge3 (N3 -> N1)
        val resultAcyclic = TarjanGraphService.analyze(
            listOf(nodeA, nodeB, nodeC),
            listOf(edge1, edge2)
        )

        assertFalse("Graph must be acyclic after breaking circular edge", resultAcyclic.hasCycles)
        assertTrue(resultAcyclic.cycles.isEmpty())
    }

    @Test
    fun testDeterministicAdrRendering() {
        val decision = Decision(
            id = "DEC-TEST-001",
            title = "Modular Monolith Strategy",
            rawProblemStatement = "Need high data consistency with zero network saga failures.",
            status = DecisionStatus.APPROVED,
            tier = DecisionTier.T2,
            currentRevisionNumber = 1,
            revisionHash = "a1b2c3d4e5f67890a1b2c3d4e5f67890a1b2c3d4e5f67890a1b2c3d4e5f67890",
            claims = listOf(
                EpistemicClaim("C1", "Transactional atomicity required", EpistemicType.CONSTRAINT)
            ),
            options = listOf(
                DecisionOption("OPT-1", "Modular Monolith", "Unified domain modules with PostgreSQL", isRecommended = true)
            ),
            recommendedOptionId = "OPT-1",
            decisiveRationale = "Lowest cognitive and operational cost."
        )

        val adr = DeterministicAdrRenderer.renderMarkdown(
            decision = decision,
            adrNumber = 1,
            approvedBy = "Engr. Nirzor",
            approvalTimestamp = 1700000000000L,
            packetHash = "abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789"
        )

        assertNotNull(adr)
        assertEquals(1, adr.adrNumber)
        assertTrue(adr.renderedMarkdown.contains("# ADR-0001: Modular Monolith Strategy"))
        assertTrue(adr.renderedMarkdown.contains("Chosen Option: **Modular Monolith**"))
        assertTrue(adr.renderedMarkdown.contains("a1b2c3d4e5f67890"))
    }

    @Test
    fun testQualityVectorComputation() {
        val qv = QualityVector(
            evidenceStrength = 0.9f,
            frameCompleteness = 0.9f,
            constraintFit = 0.9f,
            optionCoverage = 0.8f,
            reversibility = 0.8f,
            riskExposure = 0.1f,
            validationReadiness = 0.9f,
            complexityPenalty = 0.1f
        )
        val score = qv.compositeScore
        assertTrue("Composite score must be within [0, 1]", score in 0f..1f)
        assertTrue("High quality vector should yield score > 0.7", score > 0.7f)
    }
}
