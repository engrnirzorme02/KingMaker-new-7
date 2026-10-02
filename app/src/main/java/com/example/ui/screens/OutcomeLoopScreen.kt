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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Decision
import com.example.model.DivergenceClass
import com.example.model.OutcomeObservation
import com.example.ui.theme.AmberWarning
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OutcomeLoopScreen(
    outcomes: List<OutcomeObservation>,
    decisions: List<Decision>,
    isBangla: Boolean,
    onRecordOutcome: (decisionId: String, adrNumber: Int, expected: String, observed: String, divergence: DivergenceClass, notes: String) -> Unit,
    onSelectDecision: (Decision) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDecisionId by remember { mutableStateOf(decisions.firstOrNull()?.id ?: "") }
    var expectedMetric by remember { mutableStateOf("") }
    var observedMetric by remember { mutableStateOf("") }
    var divergenceClass by remember { mutableStateOf(DivergenceClass.NONE) }
    var notes by remember { mutableStateOf("") }

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
                    text = if (isBangla) "ফলাফল পর্যবেক্ষণ লুপ (Outcome Loop)" else "Outcome Tracking Loop",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Text(
                    text = if (isBangla) "বাস্তব ফলাফল তুলনা করে দেখুন। ইনভ্যালিডেশন ঘটলে নতুন সংশোধন প্রস্তাব তৈরি হবে।" else "Compare expected vs observed outcomes without altering historical t0 quality.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
            Button(
                onClick = { showAddDialog = !showAddDialog },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                modifier = Modifier.testTag("btn_toggle_add_outcome")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBangla) "পর্যবেক্ষণ" else "Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // New Outcome Input Form
        if (showAddDialog) {
            Card(
                colors = CardDefaults.cardColors(containerColor = KingDarkCardElevated),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isBangla) "নতুন ফলাফল পর্যবেক্ষণ রেকর্ড করুন" else "Record New Outcome Observation",
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = expectedMetric,
                        onValueChange = { expectedMetric = it },
                        label = { Text(if (isBangla) "প্রত্যাশিত মেট্রিক (Expected Metric)" else "Expected Metric") },
                        placeholder = { Text("e.g. Latency p95 < 100ms") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = KingDarkCard,
                            unfocusedContainerColor = KingDarkCard
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = observedMetric,
                        onValueChange = { observedMetric = it },
                        label = { Text(if (isBangla) "বাস্তবে পর্যবেক্ষণকৃত মেট্রিক (Observed Metric)" else "Observed Metric") },
                        placeholder = { Text("e.g. Measured 75ms in production") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = KingDarkCard,
                            unfocusedContainerColor = KingDarkCard
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(if (isBangla) "পর্যবেক্ষণ মন্তব্য (Review Notes)" else "Review Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = KingDarkCard,
                            unfocusedContainerColor = KingDarkCard
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Divergence Class Selector
                    Text(text = "Divergence Classification:", fontSize = 11.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { divergenceClass = DivergenceClass.NONE },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (divergenceClass == DivergenceClass.NONE) EmeraldSuccess.copy(alpha = 0.2f) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("NONE", fontSize = 10.sp, color = if (divergenceClass == DivergenceClass.NONE) EmeraldSuccess else TextSecondary)
                        }
                        OutlinedButton(
                            onClick = { divergenceClass = DivergenceClass.EXPECTED_VARIANCE },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (divergenceClass == DivergenceClass.EXPECTED_VARIANCE) GoldPrimary.copy(alpha = 0.2f) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("VARIANCE", fontSize = 10.sp, color = if (divergenceClass == DivergenceClass.EXPECTED_VARIANCE) GoldPrimary else TextSecondary)
                        }
                        OutlinedButton(
                            onClick = { divergenceClass = DivergenceClass.DECISION_INVALIDATION },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (divergenceClass == DivergenceClass.DECISION_INVALIDATION) CrimsonDanger.copy(alpha = 0.2f) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("INVALIDATION", fontSize = 10.sp, color = if (divergenceClass == DivergenceClass.DECISION_INVALIDATION) CrimsonDanger else TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (expectedMetric.isNotBlank() && observedMetric.isNotBlank()) {
                                onRecordOutcome(
                                    selectedDecisionId.ifBlank { decisions.firstOrNull()?.id ?: "DEC-001" },
                                    1,
                                    expectedMetric,
                                    observedMetric,
                                    divergenceClass,
                                    notes
                                )
                                showAddDialog = false
                                expectedMetric = ""
                                observedMetric = ""
                                notes = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                    ) {
                        Text(if (isBangla) "পর্যবেক্ষণ সংরক্ষণ করুন" else "Save Outcome", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // List of Historical Observations
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(outcomes, key = { it.id }) { outcome ->
                OutcomeObservationCard(
                    outcome = outcome,
                    decisions = decisions,
                    isBangla = isBangla,
                    onOpenDecision = { d -> onSelectDecision(d) }
                )
            }
        }
    }
}

@Composable
fun OutcomeObservationCard(
    outcome: OutcomeObservation,
    decisions: List<Decision>,
    isBangla: Boolean,
    onOpenDecision: (Decision) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val formattedDate = dateFormat.format(Date(outcome.observationDate))
    val parentDecision = decisions.find { it.id == outcome.decisionId }

    val (badgeColor, badgeText) = when (outcome.divergenceClass) {
        DivergenceClass.NONE -> Pair(EmeraldSuccess, "ALIGNED (NONE)")
        DivergenceClass.EXPECTED_VARIANCE -> Pair(CyanAccent, "EXPECTED VARIANCE")
        DivergenceClass.EVIDENCE_GAP -> Pair(AmberWarning, "EVIDENCE GAP")
        DivergenceClass.OPERATIONAL_ISSUE -> Pair(PurpleAdvisory, "OPERATIONAL ISSUE")
        DivergenceClass.DECISION_INVALIDATION -> Pair(CrimsonDanger, "DECISION INVALIDATION")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = KingDarkCard),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = parentDecision?.title ?: "Decision: ${outcome.decisionId}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = badgeText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Expected: ${outcome.expectedMetric}", fontSize = 11.sp, color = TextSecondary)
            Text(text = "Observed: ${outcome.observedMetric}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CyanAccent)

            if (outcome.reviewNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Notes: ${outcome.reviewNotes}", fontSize = 11.sp, color = TextSecondary)
            }

            if (outcome.warrantsSupersedingRevision) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CrimsonDanger.copy(alpha = 0.15f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (isBangla)
                            "সতর্কতা: সিদ্ধান্তটি অবৈধ হয়েছে (Invalidated)। একটি নতুন সংশোধিত সিদ্ধান্ত ড্রাফট তৈরি করা আবশ্যক।"
                        else
                            "Warning: Decision invalidated. Initiating superseding revision recommended.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonDanger
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Observed on $formattedDate", fontSize = 10.sp, color = TextSecondary)
        }
    }
}
