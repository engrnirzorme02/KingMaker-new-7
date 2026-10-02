package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DecisionTier
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
fun CaptureIntakeScreen(
    isBangla: Boolean,
    isLoading: Boolean,
    onSubmitIntake: (rawProblem: String, title: String, tier: DecisionTier) -> Unit,
    onTranscribeAudio: (ByteArray, (String) -> Unit) -> Unit,
    onAnalyzeDiagramImage: (Bitmap, (String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var rawProblem by remember { mutableStateOf("") }
    var selectedTier by remember { mutableStateOf(DecisionTier.T2) }

    // Photo picker for architecture diagram understanding
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                onAnalyzeDiagramImage(bitmap) { extractedText ->
                    if (title.isBlank()) {
                        title = "Architecture Diagram: Cloud Infrastructure Decision"
                    }
                    rawProblem = if (rawProblem.isBlank()) extractedText else "$rawProblem\n\n[Extracted Diagram Components]:\n$extractedText"
                }
            } catch (e: Exception) {
                // Handled in VM
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = if (isBangla) "নতুন কারিগরি সিদ্ধান্ত গ্রহণ (D1 Intake)" else "Capture Technical Decision (D1 Intake)",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isBangla)
                "আপনার অসম্পূর্ণ সমস্যা বা কারিগরি দ্বিধা লিখুন। KingMaker এটিকে প্রমাণ-ভিত্তিক ফ্রেমিং এবং গভর্ন্যান্সে রূপান্তর করবে।"
            else
                "Describe your technical problem, architecture doubt or design choice. KingMaker will frame and govern it.",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Preset Templates
        Text(
            text = if (isBangla) "দ্রুত নমুনা টেমপ্লেট:" else "Quick Preset Templates:",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = GoldPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetChip(
                label = "Monolith vs Microservices",
                onClick = {
                    title = "Backend Architecture: Modular Monolith vs Microservices"
                    rawProblem = "সিস্টেমের বর্তমান একক সার্ভারে পিক লোড বৃদ্ধি পাচ্ছে। আমাদের কি সম্পূর্ণ মাইক্রোসার্ভিসে বিভক্ত করা উচিত নাকি মডুলার মনোলিথ হিসেবে রেখে ট্রানজ্যাকশনাল কনসিস্টেন্সি রক্ষা করা শ্রেয়?"
                    selectedTier = DecisionTier.T2
                }
            )
            PresetChip(
                label = "Postgres vs NoSQL",
                onClick = {
                    title = "Database Authority Strategy: PostgreSQL vs DynamoDB"
                    rawProblem = "গ্রাহক লেনদেন ও অডিট ট্রেইল সংরক্ষণের জন্য কোন ডেটাবেস সিস্টেম চূড়ান্ত সত্য (Single Source of Truth) হিসেবে কাজ করবে?"
                    selectedTier = DecisionTier.T2
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title Input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_decision_title"),
            label = { Text(if (isBangla) "সিদ্ধান্তের শিরোনাম (Title)" else "Decision Title") },
            placeholder = { Text(if (isBangla) "উদা: ক্যাশিং আর্কিটেকচার নির্বাচন" else "e.g., Caching Layer Strategy") },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = BorderSubtle,
                focusedContainerColor = KingDarkCard,
                unfocusedContainerColor = KingDarkCard
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Problem Statement Text Field with Voice & Image Upload Buttons
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = rawProblem,
                onValueChange = { rawProblem = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .testTag("input_raw_problem"),
                label = { Text(if (isBangla) "মূল সমস্যা ও প্রেক্ষাপট (Raw Problem Context)" else "Raw Problem Statement") },
                placeholder = {
                    Text(
                        if (isBangla)
                            "বাংলা, ইংরেজি বা বাংলিশে আপনার কারিগরি চিন্তা লিখুন...\n(মাইক্রোফোন দিয়ে বলতে পারেন বা ডায়াগ্রাম আপলোড করতে পারেন)"
                        else
                            "Enter constraints, goals, trade-offs in English or Bangla...\n(Or use mic to speak, or upload architecture diagram)"
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = KingDarkCard,
                    unfocusedContainerColor = KingDarkCard
                )
            )

            // Multi-modal actions inside the box (Microphone + Diagram Photo Picker)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                // Voice Transcription (gemini-3.5-transcribe)
                IconButton(
                    onClick = {
                        // Simulates microphone capture and calls gemini-3.5-transcribe
                        val mockAudio = ByteArray(10)
                        onTranscribeAudio(mockAudio) { transcribedText ->
                            rawProblem = if (rawProblem.isBlank()) transcribedText else "$rawProblem $transcribedText"
                            if (title.isBlank()) title = "ভয়েস ইনপুট থেকে কারিগরি সিদ্ধান্ত"
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(KingDarkCardElevated)
                        .testTag("btn_voice_input")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input (gemini-3.5-transcribe)",
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Diagram Image Understanding (gemini-3.1-pro-preview)
                IconButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(KingDarkCardElevated)
                        .testTag("btn_upload_diagram")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Upload Diagram (gemini-3.1-pro-preview)",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tier Selection (T1, T2, T3)
        Text(
            text = if (isBangla) "সিদ্ধান্তের ঝুঁকি ও গুরুত্ব স্তর (Governance Tier):" else "Decision Governance Tier:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
        )
        Spacer(modifier = Modifier.height(6.dp))

        TierSelectionRow(
            tier = DecisionTier.T1,
            selected = selectedTier == DecisionTier.T1,
            onSelect = { selectedTier = DecisionTier.T1 },
            title = "T1: Low Impact / Reversible",
            subtitle = if (isBangla) "সহজে পরিবর্তনযোগ্য, সীমিত বাজেট ও স্বল্প রেড-টিম রিভিউ" else "Quick turnaround, reversible, low budget overhead",
            color = EmeraldSuccess
        )
        Spacer(modifier = Modifier.height(6.dp))
        TierSelectionRow(
            tier = DecisionTier.T2,
            selected = selectedTier == DecisionTier.T2,
            onSelect = { selectedTier = DecisionTier.T2 },
            title = "T2: Material Technical Consequence (Recommended)",
            subtitle = if (isBangla) "যথেষ্ট কারিগরি প্রভাব, পূর্ণাঙ্গ বিশেষজ্ঞ কাউন্সিল ও রেড-টিম ডিবেট" else "Material impact, full expert perspectives & Devil's Advocate",
            color = GoldPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        TierSelectionRow(
            tier = DecisionTier.T3,
            selected = selectedTier == DecisionTier.T3,
            onSelect = { selectedTier = DecisionTier.T3 },
            title = "T3: High Impact / Low Reversibility (High Thinking)",
            subtitle = if (isBangla) "অপরিবর্তনীয় বা সংবেদনশীল, ডিপ প্রি-মর্টেম ও কঠোর অনুমোদন শর্ত" else "Irreversible, sensitive, deep pre-mortem & High Thinking mode",
            color = CrimsonDanger
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button (calls gemini-3.1-flash-lite for low-latency interpretation)
        Button(
            onClick = {
                val finalTitle = title.ifBlank { "Untitled Technical Decision" }
                val finalProblem = rawProblem.ifBlank { "Unspecified architectural dilemma." }
                onSubmitIntake(finalProblem, finalTitle, selectedTier)
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_submit_intake"),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isBangla) "ইন্টারপ্রিটেশন চলমান..." else "Interpreting via gemini-3.1-flash-lite...")
            } else {
                Icon(imageVector = Icons.Default.Bolt, contentDescription = "Fast AI")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBangla) "শুরু করুন (দ্রুত ইন্টারপ্রিটেশন)" else "Start Intake (Fast Interpretation)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun PresetChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(KingDarkCard)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = label, fontSize = 11.sp, color = CyanAccent)
    }
}

@Composable
fun TierSelectionRow(
    tier: DecisionTier,
    selected: Boolean,
    onSelect: () -> Unit,
    title: String,
    subtitle: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected) KingDarkCardElevated else KingDarkCard
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = color,
                    unselectedColor = BorderSubtle
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
                Text(text = subtitle, fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}
