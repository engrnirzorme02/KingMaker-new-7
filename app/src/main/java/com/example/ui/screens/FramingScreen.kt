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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.model.Decision
import com.example.ui.components.EpistemicBadge
import com.example.ui.components.TierBadge
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.KingDarkCard
import com.example.ui.theme.KingDarkCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FramingScreen(
    decision: Decision,
    isBangla: Boolean,
    onConfirmFraming: () -> Unit
) {
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
                    text = if (isBangla) "সিদ্ধান্তের ফ্রেমিং ও সিলিং (D3 Framing)" else "Decision Framing & Sealing (D3)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Text(
                    text = if (isBangla) "ফ্রেমিং নিশ্চিত করলে রিভিশন হ্যাশ তৈরি হবে এবং ডিবেটের জন্য লক হবে।" else "Confirming the frame computes canonical JCS hash and locks draft.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
            TierBadge(tier = decision.tier)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Readiness Gate Card
        OutlinedCard(
            colors = CardDefaults.outlinedCardColors(containerColor = KingDarkCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Ready", tint = EmeraldSuccess)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBangla) "রেডিনেস গেট চেকলিস্ট (Readiness Gate Passed)" else "Readiness Gate Passed",
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "✓ Decision question normalized & confirmed", fontSize = 11.sp, color = TextSecondary)
                Text(text = "✓ Constraints and hard limits explicit", fontSize = 11.sp, color = TextSecondary)
                Text(text = "✓ Viable comparison alternatives present (>= 2 options)", fontSize = 11.sp, color = TextSecondary)
                Text(text = "✓ Readiness verified for Tier ${decision.tier.name} analysis", fontSize = 11.sp, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Epistemic Claims Section
        Text(
            text = if (isBangla) "প্রামাণ্য উপাদান ও সীমাবদ্ধতা (Epistemic Claims):" else "Epistemic Claims & Constraints:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
        )
        Spacer(modifier = Modifier.height(6.dp))

        decision.claims.forEach { claim ->
            Card(
                colors = CardDefaults.cardColors(containerColor = KingDarkCard),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EpistemicBadge(type = claim.type)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = claim.text, fontSize = 12.sp, color = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Comparison Options Section
        Text(
            text = if (isBangla) "বিবেচ্য বিকল্পসমূহ (Candidate Options):" else "Candidate Options:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
        )
        Spacer(modifier = Modifier.height(6.dp))

        decision.options.forEach { opt ->
            Card(
                colors = CardDefaults.cardColors(containerColor = KingDarkCard),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = opt.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldPrimary)
                        Text(text = opt.estimatedMonthlyCost, fontSize = 11.sp, color = CyanAccent)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = opt.description, fontSize = 12.sp, color = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Confirm & Seal Button
        Button(
            onClick = onConfirmFraming,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_confirm_framing"),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock & Seal")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isBangla) "ফ্রেমিং নিশ্চিত ও লক করুন (Confirm & Seal)" else "Confirm Frame & Start Council (D4)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}
