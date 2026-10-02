package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DecisionStatus
import com.example.model.DecisionTier
import com.example.model.EpistemicType
import com.example.model.PresentationLens
import com.example.model.QualityVector
import com.example.ui.Screen
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KingMakerTopBar(
    title: String,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    isBangla: Boolean,
    onToggleLanguage: () -> Unit,
    onOpenLiveVoice: () -> Unit,
    currentLens: PresentationLens? = null,
    onSelectLens: ((PresentationLens) -> Unit)? = null
) {
    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GoldPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "v7.0 CANONICAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }
                }
                if (currentLens != null) {
                    Text(
                        text = "Lens: ${currentLens.name}",
                        fontSize = 11.sp,
                        color = CyanAccent
                    )
                }
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            } else {
                Box(modifier = Modifier.padding(start = 12.dp)) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "KingMaker Logo",
                        tint = GoldPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        actions = {
            // Live Voice Action
            IconButton(
                onClick = onOpenLiveVoice,
                modifier = Modifier.testTag("action_live_voice")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Live Voice Debrief",
                    tint = CyanAccent
                )
            }
            // Language Toggle Action (বাংলা / English)
            IconButton(
                onClick = onToggleLanguage,
                modifier = Modifier.testTag("action_toggle_language")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(KingDarkCardElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Toggle Language",
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBangla) "বাং" else "EN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun KingMakerBottomNav(
    currentScreen: Screen,
    onSelectScreen: (Screen) -> Unit,
    isBangla: Boolean
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(Screen.DECISIONS_LIST, Icons.Default.Description, if (isBangla) "সিদ্ধান্ত" else "Decisions"),
            Triple(Screen.LIVING_BLUEPRINT, Icons.Default.AccountTree, if (isBangla) "ব্লুপ্রিন্ট" else "Blueprint"),
            Triple(Screen.COUNCIL_CHAT, Icons.Default.Chat, if (isBangla) "কাউন্সিল" else "Council"),
            Triple(Screen.OUTCOME_LOOP, Icons.Default.Timeline, if (isBangla) "ফলাফল" else "Outcomes"),
            Triple(Screen.SETTINGS, Icons.Default.Settings, if (isBangla) "সেটিংস" else "Settings")
        )

        items.forEach { (screen, icon, label) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectScreen(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(text = label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GoldPrimary,
                    selectedTextColor = GoldPrimary,
                    indicatorColor = GoldPrimary.copy(alpha = 0.15f),
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                modifier = Modifier.testTag("nav_tab_${screen.name.lowercase()}")
            )
        }
    }
}

@Composable
fun EpistemicBadge(type: EpistemicType) {
    val (bgColor, textColor) = when (type) {
        EpistemicType.FACT -> Pair(EmeraldSuccess.copy(alpha = 0.2f), EmeraldSuccess)
        EpistemicType.CONSTRAINT -> Pair(CrimsonDanger.copy(alpha = 0.2f), CrimsonDanger)
        EpistemicType.PREFERENCE -> Pair(PurpleAdvisory.copy(alpha = 0.2f), PurpleAdvisory)
        EpistemicType.UNKNOWN -> Pair(AmberWarning.copy(alpha = 0.2f), AmberWarning)
        EpistemicType.RISK -> Pair(CrimsonDanger.copy(alpha = 0.25f), CrimsonDanger)
        EpistemicType.INFERENCE -> Pair(CyanAccent.copy(alpha = 0.2f), CyanAccent)
        EpistemicType.RECOMMENDATION -> Pair(GoldPrimary.copy(alpha = 0.2f), GoldPrimary)
        EpistemicType.ASSUMPTION -> Pair(Color.Gray.copy(alpha = 0.3f), Color.LightGray)
        EpistemicType.UNVERIFIED -> Pair(Color.Red.copy(alpha = 0.2f), Color(0xFFFF8A80))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = type.name,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusBadge(status: DecisionStatus, isBangla: Boolean) {
    val (bgColor, textColor, labelBn, labelEn) = when (status) {
        DecisionStatus.RAW -> Tuple4(Color.Gray.copy(alpha = 0.2f), Color.LightGray, "র' ইনটেক", "RAW INTAKE")
        DecisionStatus.INTERPRETATION -> Tuple4(CyanAccent.copy(alpha = 0.2f), CyanAccent, "ইন্টারপ্রিটেশন", "INTERPRETATION")
        DecisionStatus.CONTEXT -> Tuple4(AmberWarning.copy(alpha = 0.2f), AmberWarning, "প্রাসঙ্গিক প্রশ্ন", "CONTEXT QA")
        DecisionStatus.FRAMING -> Tuple4(PurpleAdvisory.copy(alpha = 0.2f), PurpleAdvisory, "ফ্রেমিং ড্রাফট", "FRAMING DRAFT")
        DecisionStatus.READY_FOR_DEBATE -> Tuple4(CyanAccent.copy(alpha = 0.2f), CyanAccent, "ডিবেটের জন্য প্রস্তুত", "READY FOR DEBATE")
        DecisionStatus.RUNNING -> Tuple4(GoldPrimary.copy(alpha = 0.25f), GoldPrimary, "বিশ্লেষণ চলমান", "ANALYZING")
        DecisionStatus.HUMAN_REVIEW -> Tuple4(AmberWarning.copy(alpha = 0.3f), AmberWarning, "মানবীয় পর্যালোচনা", "HUMAN REVIEW")
        DecisionStatus.APPROVED -> Tuple4(EmeraldSuccess.copy(alpha = 0.2f), EmeraldSuccess, "অনুমোদিত (ADR)", "APPROVED (ADR)")
        DecisionStatus.REJECTED -> Tuple4(CrimsonDanger.copy(alpha = 0.2f), CrimsonDanger, "বাতিল", "REJECTED")
        DecisionStatus.DEFERRED -> Tuple4(Color.Gray.copy(alpha = 0.3f), Color.Gray, "স্থগিত", "DEFERRED")
        DecisionStatus.REVISION_REQUESTED -> Tuple4(PurpleAdvisory.copy(alpha = 0.25f), PurpleAdvisory, "সংশোধনী কাম্য", "REVISION REQUESTED")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = if (isBangla) labelBn else labelEn,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

@Composable
fun TierBadge(tier: DecisionTier) {
    val (color, desc) = when (tier) {
        DecisionTier.T1 -> Pair(EmeraldSuccess, "T1 (Low/Reversible)")
        DecisionTier.T2 -> Pair(GoldPrimary, "T2 (Material Impact)")
        DecisionTier.T3 -> Pair(CrimsonDanger, "T3 (High/Irreversible)")
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = desc,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun QualityVectorCard(qv: QualityVector, isBangla: Boolean) {
    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(
            containerColor = KingDarkCard
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "কোয়ালিটি ভেক্টর (Quality Vector)" else "Quality Vector",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Text(
                    text = "${(qv.compositeScore * 100).toInt()}% Composite",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            QualityBarItem(label = if (isBangla) "প্রমাণের গভীরতা (Evidence Strength)" else "Evidence Strength", score = qv.evidenceStrength)
            QualityBarItem(label = if (isBangla) "ফ্রেমিং সম্পূর্ণতা (Frame Completeness)" else "Frame Completeness", score = qv.frameCompleteness)
            QualityBarItem(label = if (isBangla) "কনস্ট্রেইন্ট সামঞ্জস্য (Constraint Fit)" else "Constraint Fit", score = qv.constraintFit)
            QualityBarItem(label = if (isBangla) "বিকল্পের কভারেজ (Option Coverage)" else "Option Coverage", score = qv.optionCoverage)
            QualityBarItem(label = if (isBangla) "রিভার্সিবিলিটি (Reversibility)" else "Reversibility", score = qv.reversibility)
            QualityBarItem(label = if (isBangla) "যাচাইকরণ প্রস্তুতি (Validation Readiness)" else "Validation Readiness", score = qv.validationReadiness)
        }
    }
}

@Composable
fun QualityBarItem(label: String, score: Float) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 11.sp, color = TextSecondary)
            Text(text = "${(score * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { score.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = if (score > 0.75f) EmeraldSuccess else if (score > 0.5f) GoldPrimary else AmberWarning,
            trackColor = BorderSubtle
        )
    }
}
