package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CycleAnalysisResult
import com.example.model.BlueprintEdge
import com.example.model.Decision
import com.example.ui.components.TierBadge
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.KingDarkCard
import com.example.ui.theme.KingDarkCardElevated
import com.example.ui.theme.PurpleAdvisory
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LivingBlueprintScreen(
    decisions: List<Decision>,
    edges: List<BlueprintEdge>,
    cycleAnalysis: CycleAnalysisResult?,
    isBangla: Boolean,
    onRunTarjanAnalysis: () -> Unit,
    onResolveCycle: (edgeId: String) -> Unit,
    onSelectDecision: (Decision) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBangla) "লিভিং ব্লুপ্রিন্ট (Living Blueprint)" else "Living Blueprint Projection",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Text(
                    text = if (isBangla) "আর্কিটেকচারাল নির্ভরতার নেটওয়ার্ক ও টারজান SCC সাইকেল সনাক্তকরণ।" else "Rebuildable dependency projection & Tarjan SCC cycle validation.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyanAccent.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "PROJECTION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tarjan SCC Verification Trigger Button
        OutlinedButton(
            onClick = onRunTarjanAnalysis,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("btn_run_tarjan_analysis"),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(imageVector = Icons.Default.AccountTree, contentDescription = "Tarjan SCC", tint = GoldPrimary)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isBangla) "টারজান SCC ডিপেন্ডেন্সি সাইকেল পরীক্ষা করুন" else "Run Tarjan SCC Cycle Validation",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = GoldPrimary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tarjan Result Banner
        if (cycleAnalysis != null) {
            if (cycleAnalysis.hasCycles) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CrimsonDanger.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "Warning", tint = CrimsonDanger)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBangla) "টারজান সাইকেল শনাক্ত হয়েছে (${cycleAnalysis.cycles.size}টি সাইকেল)!" else "Tarjan SCC Detected Dependency Cycle!",
                                fontWeight = FontWeight.Bold,
                                color = CrimsonDanger,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBangla)
                                "নির্ভরশীলতার বৃত্তাকার চক্র তৈরি হয়েছে যা আর্কিটেকচার ডেডলক ঘটাতে পারে। নিচের গাইড সমাধান ব্যবহার করুন:"
                            else
                                "Circular dependency detected. Use Guided Resolution below to break cycle:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        cycleAnalysis.proposals.forEach { proposal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBangla) proposal.descriptionBn else proposal.descriptionEn,
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onResolveCycle(proposal.edgeToBreak.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(text = if (isBangla) "সমাধান" else "Resolve", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmeraldSuccess.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "OK", tint = EmeraldSuccess)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBangla) "গ্রাফ যাচাইকৃত: কোনো ক্ষতিকর সাইকেল নেই (Acyclic DAG)।" else "Graph Verified: Strictly Acyclic DAG. No cycles found.",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Graph Nodes & Edges View
        Text(
            text = if (isBangla) "আর্কিটেকচার নোড এবং নির্ভরশীলতা তালিকা:" else "Architecture Nodes & Edge Relations:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(decisions, key = { it.id }) { decision ->
                val outgoingEdges = edges.filter { it.sourceId == decision.id }
                BlueprintNodeCard(
                    decision = decision,
                    outgoingEdges = outgoingEdges,
                    allDecisions = decisions,
                    isBangla = isBangla,
                    onClick = { onSelectDecision(decision) }
                )
            }
        }
    }
}

@Composable
fun BlueprintNodeCard(
    decision: Decision,
    outgoingEdges: List<BlueprintEdge>,
    allDecisions: List<Decision>,
    isBangla: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = KingDarkCard),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = decision.id,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
                TierBadge(tier = decision.tier)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = decision.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextPrimary
            )

            if (outgoingEdges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                outgoingEdges.forEach { edge ->
                    val targetTitle = allDecisions.find { it.id == edge.targetId }?.title ?: edge.targetId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "└─ [${edge.relationType.name}] ──>",
                            fontSize = 10.sp,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = targetTitle,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
