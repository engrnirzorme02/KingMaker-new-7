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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.remote.UserSession
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.KingDarkCard
import com.example.ui.theme.KingDarkCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    userSession: UserSession,
    isBangla: Boolean,
    onToggleLanguage: () -> Unit,
    onSignOut: () -> Unit,
    onSimulateLogin: (String, String) -> Unit,
    onResetInitialData: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = if (isBangla) "অ্যাপ্লিকেশন সেটিংস ও প্রোফাইল" else "Settings & Profile",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
        )
        Text(
            text = "KingMaker v7.0 Canonical Personal Edition",
            style = MaterialTheme.typography.bodySmall.copy(color = GoldPrimary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Identity & Firebase Auth Card
        Card(
            colors = CardDefaults.cardColors(containerColor = KingDarkCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "User", tint = GoldPrimary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = userSession.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Text(text = userSession.email, fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(EmeraldSuccess.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (userSession.isAllowlistedOwner) "OWNER ALLOWLIST" else "GUEST",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CloudSync, contentDescription = "Sync", tint = CyanAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (userSession.isCloudSynced) "Firebase Auth & Firestore Synced" else "Local Owner Mode (Offline Auth)",
                        fontSize = 11.sp,
                        color = CyanAccent
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                if (userSession.isAuthenticated) {
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Sign Out", fontSize = 12.sp, color = CrimsonDanger)
                    }
                } else {
                    Button(
                        onClick = { onSimulateLogin("engr.nirzor.me.02@gmail.com", "Engr. Nirzor") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                    ) {
                        Text("Sign In with Google", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Language Configuration
        Card(
            colors = CardDefaults.cardColors(containerColor = KingDarkCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = "Language", tint = CyanAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isBangla) "অ্যাপ্লিকেশনের ভাষা (UI Language)" else "App Language",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isBangla) "বর্তমান: বাংলা (Bangla)" else "Current: English",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = isBangla,
                        onCheckedChange = { onToggleLanguage() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldPrimary,
                            checkedTrackColor = CyanAccent
                        ),
                        modifier = Modifier.testTag("switch_language_toggle")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // API Key & Model Configuration
        Card(
            colors = CardDefaults.cardColors(containerColor = KingDarkCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = "API", tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gemini Model Gateway Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "• gemini-3.1-pro-preview: High Thinking & Red Team", fontSize = 11.sp, color = TextSecondary)
                Text(text = "• gemini-3.5-flash: General Reasoning, Search & Maps", fontSize = 11.sp, color = TextSecondary)
                Text(text = "• gemini-3.1-flash-lite: Low-Latency Intake Parsing", fontSize = 11.sp, color = TextSecondary)
                Text(text = "• gemini-3.8-flash-tts: Decision Recitation Audio", fontSize = 11.sp, color = TextSecondary)
                Text(text = "• gemini-3.8-live: Live Voice Debrief Session", fontSize = 11.sp, color = TextSecondary)
                Text(text = "• gemini-3.5-transcribe: Microphone Speech-to-Text", fontSize = 11.sp, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cost Budget Ledger
        Card(
            colors = CardDefaults.cardColors(containerColor = KingDarkCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AttachMoney, contentDescription = "Cost", tint = EmeraldSuccess)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cost Budget Ledger (ADR-V7-020)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Monthly AI Cap: $20.00 | Consumed: $2.40 | Remaining: $17.60", fontSize = 11.sp, color = TextSecondary)
                Text(text = "Infrastructure Target: $15.00/month (Neon + Cloud Run)", fontSize = 11.sp, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reset Sample Data
        OutlinedButton(
            onClick = onResetInitialData,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
        ) {
            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = "Seed")
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isBangla) "নমুনা আর্কিটেকচার সিদ্ধান্ত রিসেট করুন" else "Reset Sample Architecture Decisions", fontSize = 12.sp)
        }
    }
}
