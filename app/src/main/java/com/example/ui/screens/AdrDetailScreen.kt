package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdrRecord
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.KingDarkCard
import com.example.ui.theme.KingDarkCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdrDetailScreen(
    adr: AdrRecord,
    isBangla: Boolean,
    isLoading: Boolean,
    isPlayingAudio: Boolean,
    onSpeakSummary: (String) -> Unit,
    onStopAudio: () -> Unit,
    onNotify: (String, String) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(adr.approvalTimestamp))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ADR-${String.format("%04d", adr.adrNumber)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Text(
                    text = adr.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(EmeraldSuccess.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "IMMUTABLE ADR",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldSuccess
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Audio Recitation via gemini-3.8-flash-tts
        Card(
            colors = CardDefaults.cardColors(containerColor = KingDarkCardElevated),
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
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "TTS Voice",
                        tint = CyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isBangla) "অডিও পাঠ (Gemini TTS)" else "Listen (gemini-3.8-flash-tts)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isPlayingAudio) "Playing with voice 'Kore'..." else "Synthesize decision audio summary",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (isPlayingAudio) {
                    IconButton(
                        onClick = onStopAudio,
                        modifier = Modifier.testTag("btn_stop_audio")
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop", tint = GoldPrimary)
                    }
                } else {
                    Button(
                        onClick = {
                            val summary = "Decision: ${adr.title}. Outcome: ${adr.chosenOption}. Rationale: ${adr.context}"
                            onSpeakSummary(summary)
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                        modifier = Modifier.testTag("btn_speak_adr")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black)
                        } else {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isBangla) "শুনুন" else "Play", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cryptographic Governance Stamp
        OutlinedCard(
            colors = CardDefaults.outlinedCardColors(containerColor = KingDarkCard),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "CRYPTOGRAPHIC GOVERNANCE STAMP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Approved by: ${adr.approvedBy} on $formattedDate", fontSize = 11.sp, color = TextPrimary)
                Text(text = "Revision Hash (JCS): ${adr.revisionHash}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                Text(text = "Review Packet Hash: ${adr.packetHash}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Markdown Document Body
        Card(
            colors = CardDefaults.cardColors(containerColor = KingDarkCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = adr.renderedMarkdown,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Copy Markdown to Clipboard Button
        OutlinedButton(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("KingMaker ADR", adr.renderedMarkdown)
                clipboard.setPrimaryClip(clip)
                onNotify("ADR Markdown copied to clipboard.", "ADR মার্কডাউন ক্লিপবোর্ডে কপি করা হয়েছে।")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_copy_adr_markdown"),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = if (isBangla) "মার্কডাউন কপি করুন (Copy Markdown)" else "Copy ADR Markdown", fontSize = 13.sp)
        }
    }
}
