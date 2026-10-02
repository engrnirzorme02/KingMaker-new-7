package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.model.DecisionTier
import com.example.model.PresentationLens
import com.example.ui.components.QualityVectorCard
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
fun ReviewGovernanceScreen(
    decision: Decision,
    isBangla: Boolean,
    currentLens: PresentationLens,
    onSelectLens: (PresentationLens) -> Unit,
    isLoading: Boolean,
    onPerformGovernanceAction: (action: String, rationale: String) -> Unit
) {
    var humanRationale by remember { mutableStateOf(decision.decisiveRationale) }
    var stepUpVerified by remember { mutableStateOf(false) }

    val recommendedOption = decision.options.find { it.id == decision.recommendedOptionId }
        ?: decision.options.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBangla) "মানবীয় পর্যালোচনা ও গভর্ন্যান্স (D7 Review)" else "Human Governance Review (D7)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Text(
                    text = if (isBangla) "AI শুধু প্রস্তাব করে। চূড়ান্ত অনুমোদন ও সিদ্ধান্ত সম্পূর্ণ মানুষের এখতিয়ার।" else "AI proposes; authorized human decides. Exact revision binding applied.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
            TierBadge(tier = decision.tier)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Five Presentation Lenses Tab Row
        Text(
            text = if (isBangla) "উপস্থাপন ভিউ (5 Lenses):" else "Presentation Lens:",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = CyanAccent
        )
        Spacer(modifier = Modifier.height(4.dp))
        TabRow(
            selectedTabIndex = currentLens.ordinal,
            containerColor = KingDarkCard,
            contentColor = GoldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[currentLens.ordinal]),
                    color = GoldPrimary
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            PresentationLens.values().forEach { lens ->
                Tab(
                    selected = currentLens == lens,
                    onClick = { onSelectLens(lens) },
                    text = {
                        Text(
                            text = lens.name.take(4),
                            fontSize = 11.sp,
                            fontWeight = if (currentLens == lens) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Lens-Specific Focused View
        when (currentLens) {
            PresentationLens.EXECUTIVE -> {
                ExecutiveLensCard(decision = decision, chosen = recommendedOption?.title ?: "N/A", isBangla = isBangla)
            }
            PresentationLens.ARCHITECTURE -> {
                ArchitectureLensCard(decision = decision, isBangla = isBangla)
            }
            PresentationLens.UX -> {
                UxLensCard(decision = decision, isBangla = isBangla)
            }
            PresentationLens.DEVELOPER -> {
                DeveloperLensCard(decision = decision, isBangla = isBangla)
            }
            PresentationLens.GOVERNANCE -> {
                GovernanceLensCard(decision = decision, isBangla = isBangla)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quality Vector Visual Summary
        QualityVectorCard(qv = decision.qualityVector, isBangla = isBangla)

        Spacer(modifier = Modifier.height(14.dp))

        // Decisive Rationale Input
        Text(
            text = if (isBangla) "মানবীয় অনুমোদনের যৌক্তিক ব্যাখ্যা (Human Rationale):" else "Human Approval Rationale:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = humanRationale,
            onValueChange = { humanRationale = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .testTag("input_approval_rationale"),
            placeholder = { Text(if (isBangla) "কেন এই বিকল্পটি নির্বাচন করলেন তার স্পষ্ট কারণ লিখুন..." else "State explicit rationale for chosen architecture...") },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = BorderSubtle,
                focusedContainerColor = KingDarkCard,
                unfocusedContainerColor = KingDarkCard
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Step-up Biometric / PIN Verification Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (stepUpVerified) EmeraldSuccess.copy(alpha = 0.15f) else KingDarkCardElevated
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Step-up Auth",
                        tint = if (stepUpVerified) EmeraldSuccess else GoldPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (stepUpVerified) (if (isBangla) "অনুমোদক যাচাইকৃত (Biometric Verified)" else "Biometric Step-Up Verified")
                            else (if (isBangla) "স্টেপ-আপ বায়োমেট্রিক অথেন্টিকেশন" else "Step-Up Biometric Verification"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (stepUpVerified) EmeraldSuccess else TextPrimary
                        )
                        Text(
                            text = "Actor: Engr. Nirzor (Personal Owner Allowlist)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
                if (!stepUpVerified) {
                    OutlinedButton(
                        onClick = { stepUpVerified = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_verify_step_up")
                    ) {
                        Text(text = if (isBangla) "যাচাই করুন" else "Verify", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // The 4 Constitutional Governance Action Buttons
        Text(
            text = if (isBangla) "গভর্ন্যান্স সিদ্ধান্ত গ্রহণ (Governance Actions):" else "Governance Decision:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // APPROVE Button
        Button(
            onClick = { onPerformGovernanceAction("APPROVE", humanRationale) },
            enabled = !isLoading && (stepUpVerified || decision.tier == DecisionTier.T1),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_governance_approve"),
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldSuccess,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
            } else {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Approve")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBangla) "অনুমোদন করুন ও ADR প্রকাশ করুন (APPROVE)" else "APPROVE & Publish Immutable ADR",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // REQUEST REVISION
            OutlinedButton(
                onClick = { onPerformGovernanceAction("REQUEST_REVISION", humanRationale) },
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("btn_governance_revision"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Revision", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBangla) "সংশোধন" else "Revision", fontSize = 11.sp)
            }

            // DEFER
            OutlinedButton(
                onClick = { onPerformGovernanceAction("DEFER", humanRationale) },
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("btn_governance_defer"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Pause, contentDescription = "Defer", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBangla) "স্থগিত" else "Defer", fontSize = 11.sp)
            }

            // REJECT
            OutlinedButton(
                onClick = { onPerformGovernanceAction("REJECT", humanRationale) },
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("btn_governance_reject"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonDanger),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Reject", tint = CrimsonDanger, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (isBangla) "বাতিল" else "Reject", fontSize = 11.sp, color = CrimsonDanger)
            }
        }
    }
}

@Composable
fun ExecutiveLensCard(decision: Decision, chosen: String, isBangla: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = KingDarkCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "EXECUTIVE LENS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Recommended Option: $chosen", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyanAccent)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Strategic Trade-off: Minimizes operational complexity and infrastructure expenses without premature scaling overhead.", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun ArchitectureLensCard(decision: Decision, isBangla: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = KingDarkCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "ARCHITECTURE LENS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "System Boundaries & Hexagonal Decoupling", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "All domain logic is encapsulated. Ports & Adapters isolate UI and PostgreSQL. Any future microservices decomposition can happen cleanly along bounded contexts.", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun UxLensCard(decision: Decision, isBangla: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = KingDarkCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "UX & HUMAN FACTORS LENS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PurpleAdvisory)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "User Decision Ergonomics", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Low latency p95 ensures instantaneous screen transitions and interactive graph exploration. No asynchronous spinner stall for core decision governance.", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun DeveloperLensCard(decision: Decision, isBangla: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = KingDarkCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "DEVELOPER IMPLEMENTATION LENS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Developer Velocity & Local Testability", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Zero requirement for Docker-compose microservice setups on developer laptops. Rapid JVM Robolectric testing runs in <5 seconds.", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun GovernanceLensCard(decision: Decision, isBangla: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = KingDarkCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "GOVERNANCE & AUDIT LENS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Cryptographic Binding & Compliance", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Revision Hash: ${decision.revisionHash.take(16)}... (JCS SHA-256)\nImmutable audit trail locked in PostgreSQL domain event sequence.", fontSize = 11.sp, color = TextSecondary)
        }
    }
}
