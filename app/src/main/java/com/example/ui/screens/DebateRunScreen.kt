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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.model.CritiqueFinding
import com.example.model.Decision
import com.example.model.DecisionStatus
import com.example.model.DecisionTier
import com.example.model.ExpertPerspective
import com.example.model.ProvenanceMode
import com.example.ui.components.QualityVectorCard
import com.example.ui.components.TierBadge
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

@Composable
fun DebateRunScreen(
    decision: Decision,
    isBangla: Boolean,
    isLoading: Boolean,
    onRunDebate: (enableHighThinking: Boolean) -> Unit,
    onProceedToReview: () -> Unit
) {
    var highThinkingEnabled by remember {
        mutableStateOf(decision.tier == DecisionTier.T3)
    }

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
                    text = if (isBangla) "বিশেষজ্ঞ কাউন্সিল ও রেড-টিম ডিবেট (D4-D6)" else "Council Debate & Red Team (D4-D6)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Text(
                    text = if (isBangla) "স্বাধীন বিশেষজ্ঞ মতামত এবং ডেভিলস অ্যাডভোকেট দিয়ে প্রতিকূল চ্যালেঞ্জ।" else "Independent specialist perspectives challenged by Devil's Advocate.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
            TierBadge(tier = decision.tier)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // High Thinking Mode Toggle Card (using gemini-3.1-pro-preview with thinkingLevel HIGH)
        Card(
            colors = CardDefaults.cardColors(containerColor = KingDarkCard),
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Thinking Mode",
                        tint = PurpleAdvisory,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBangla) "হাই থিংকিং মোড (High Thinking)" else "Enable High Thinking Mode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isBangla) "gemini-3.1-pro-preview (thinkingLevel = HIGH)" else "gemini-3.1-pro-preview deep architectural pre-mortem",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
                Switch(
                    checked = highThinkingEnabled,
                    onCheckedChange = { highThinkingEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GoldPrimary,
                        checkedTrackColor = PurpleAdvisory
                    ),
                    modifier = Modifier.testTag("switch_high_thinking")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Trigger Run Button (if not already run or if rerunning)
        if (decision.perspectives.isEmpty() || decision.status == DecisionStatus.READY_FOR_DEBATE) {
            Button(
                onClick = { onRunDebate(highThinkingEnabled) },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_run_debate"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBangla) "কাউন্সিল বিশ্লেষণ চলছে..." else "Running Council Debate...")
                } else {
                    Icon(imageVector = Icons.Default.Bolt, contentDescription = "Run")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBangla) "কাউন্সিল ডিবেট পরিচালনা করুন" else "Execute Council & Red-Team Debate",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Perspectives & Critiques List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (decision.perspectives.isNotEmpty()) {
                item {
                    Text(
                        text = if (isBangla) "বিশেষজ্ঞদের অবস্থান (Expert Perspectives):" else "Expert Perspectives:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                }
                items(decision.perspectives, key = { it.id }) { perspective ->
                    PerspectiveCard(perspective = perspective, isBangla = isBangla)
                }
            }

            if (decision.critiques.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBangla) "ডেভিলস অ্যাডভোকেট প্রতিকূল ফাইন্ডিংস (CDR-P1):" else "Devil's Advocate Falsifications (CDR-P1):",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CrimsonDanger)
                    )
                }
                items(decision.critiques, key = { it.id }) { critique ->
                    CritiqueCard(critique = critique, isBangla = isBangla)
                }
            }
        }

        // Proceed to Governance Review Button
        if (decision.perspectives.isNotEmpty()) {
            Button(
                onClick = onProceedToReview,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_proceed_to_review"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isBangla) "মানবীয় গভর্ন্যান্স পর্যালোচনায় যান (D7 Review)" else "Proceed to Human Governance (D7)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

@Composable
fun PerspectiveCard(perspective: ExpertPerspective, isBangla: Boolean) {
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
                    text = perspective.role.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = GoldPrimary
                )
                ProvenanceBadge(mode = perspective.provenanceMode)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = perspective.position,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = CyanAccent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = perspective.keyArgument,
                fontSize = 12.sp,
                color = TextSecondary
            )
            if (perspective.dissentNote != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CrimsonDanger.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Dissent: ${perspective.dissentNote}",
                        fontSize = 11.sp,
                        color = CrimsonDanger,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun CritiqueCard(critique: CritiqueFinding, isBangla: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = KingDarkCardElevated),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.BugReport, contentDescription = "Critique", tint = CrimsonDanger, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${critique.dimension.code}: ${critique.dimension.title}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CrimsonDanger
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CrimsonDanger.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = critique.severity, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CrimsonDanger)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Target: ${critique.targetOptionTitle}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = critique.objection, fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Falsification Test: ${critique.suggestedFalsificationTest}", fontSize = 11.sp, color = AmberWarning)
        }
    }
}

@Composable
fun ProvenanceBadge(mode: ProvenanceMode) {
    val (color, text) = when (mode) {
        ProvenanceMode.PROVIDER -> Pair(EmeraldSuccess, "PROVIDER (LIVE)")
        ProvenanceMode.SIMULATED -> Pair(AmberWarning, "SIMULATED (MOCK)")
        ProvenanceMode.REPLAY -> Pair(CyanAccent, "REPLAY")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = text, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
