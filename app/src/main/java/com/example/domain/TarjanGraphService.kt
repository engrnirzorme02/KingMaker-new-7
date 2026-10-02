package com.example.domain

import com.example.model.BlueprintEdge
import com.example.model.BlueprintNode
import com.example.model.EdgeRelationType
import kotlin.math.min

data class CycleResolutionProposal(
    val id: String,
    val descriptionEn: String,
    val descriptionBn: String,
    val edgeToBreak: BlueprintEdge,
    val affectedNodeIds: List<String>,
    val projectedCycleFree: Boolean
)

data class CycleAnalysisResult(
    val hasCycles: Boolean,
    val cycles: List<List<String>>, // Node IDs involved in each cycle
    val cycleEdges: List<BlueprintEdge>,
    val proposals: List<CycleResolutionProposal>
)

object TarjanGraphService {

    /**
     * Executes Tarjan's Strongly Connected Components (SCC) algorithm.
     * Any SCC with more than 1 node (or a self-loop) indicates a dependency cycle.
     */
    fun analyze(
        nodes: List<BlueprintNode>,
        edges: List<BlueprintEdge>
    ): CycleAnalysisResult {
        // Build adjacency list for structural edges (DEPENDS_ON, CONSTRAINS, SUPERSEDES)
        val structuralEdges = edges.filter {
            it.relationType == EdgeRelationType.DEPENDS_ON ||
            it.relationType == EdgeRelationType.CONSTRAINS ||
            it.relationType == EdgeRelationType.SUPERSEDES
        }

        val nodeIds = nodes.map { it.id }.toSet()
        val adj = mutableMapOf<String, MutableList<String>>()
        val edgeMap = mutableMapOf<Pair<String, String>, BlueprintEdge>()

        for (id in nodeIds) {
            adj[id] = mutableListOf()
        }

        for (e in structuralEdges) {
            if (nodeIds.contains(e.sourceId) && nodeIds.contains(e.targetId)) {
                adj[e.sourceId]?.add(e.targetId)
                edgeMap[Pair(e.sourceId, e.targetId)] = e
            }
        }

        var index = 0
        val indices = mutableMapOf<String, Int>()
        val lowLink = mutableMapOf<String, Int>()
        val onStack = mutableMapOf<String, Boolean>()
        val stack = ArrayDeque<String>()
        val sccs = mutableListOf<List<String>>()

        fun strongConnect(u: String) {
            indices[u] = index
            lowLink[u] = index
            index++
            stack.addLast(u)
            onStack[u] = true

            val neighbors = adj[u] ?: emptyList()
            for (v in neighbors) {
                if (!indices.containsKey(v)) {
                    strongConnect(v)
                    lowLink[u] = min(lowLink[u] ?: 0, lowLink[v] ?: 0)
                } else if (onStack[v] == true) {
                    lowLink[u] = min(lowLink[u] ?: 0, indices[v] ?: 0)
                }
            }

            if (lowLink[u] == indices[u]) {
                val scc = mutableListOf<String>()
                while (true) {
                    val w = stack.removeLast()
                    onStack[w] = false
                    scc.add(w)
                    if (w == u) break
                }
                if (scc.size > 1) {
                    sccs.add(scc)
                }
            }
        }

        for (id in nodeIds) {
            if (!indices.containsKey(id)) {
                strongConnect(id)
            }
        }

        val cycleEdges = mutableListOf<BlueprintEdge>()
        val proposals = mutableListOf<CycleResolutionProposal>()

        for (scc in sccs) {
            val sccSet = scc.toSet()
            val edgesInScc = structuralEdges.filter { sccSet.contains(it.sourceId) && sccSet.contains(it.targetId) }
            cycleEdges.addAll(edgesInScc)

            // Generate proposals to break each edge in the cycle
            for ((idx, edge) in edgesInScc.withIndex()) {
                val sourceNode = nodes.find { it.id == edge.sourceId }?.title ?: edge.sourceId
                val targetNode = nodes.find { it.id == edge.targetId }?.title ?: edge.targetId
                proposals.add(
                    CycleResolutionProposal(
                        id = "PROP-$idx-${edge.id}",
                        descriptionEn = "Break dependency: decouple '$sourceNode' from '$targetNode' by introducing an asynchronous event interface or mediation facade.",
                        descriptionBn = "নির্ভরশীলতা বিচ্ছিন্ন করুন: '$sourceNode' ও '$targetNode'-এর মধ্যে অ্যাসিঙ্ক্রোনাস ইভেন্ট ইন্টারফেস প্রবর্তন করে সাইকেল দূর করুন।",
                        edgeToBreak = edge,
                        affectedNodeIds = listOf(edge.sourceId, edge.targetId),
                        projectedCycleFree = true
                    )
                )
            }
        }

        return CycleAnalysisResult(
            hasCycles = sccs.isNotEmpty(),
            cycles = sccs,
            cycleEdges = cycleEdges,
            proposals = proposals
        )
    }
}
