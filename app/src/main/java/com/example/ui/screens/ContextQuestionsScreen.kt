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
import androidx.compose.material.icons.filled.HelpOutline
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
import com.example.model.ContextQuestion
import com.example.model.Decision
import com.example.model.QuestionStatus
import com.example.ui.components.EpistemicBadge
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.KingDarkCard
import com.example.ui.theme.KingDarkCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ContextQuestionsScreen(
    decision: Decision,
    isBangla: Boolean,
    onAnswerQuestion: (questionId: String, answer: String, status: QuestionStatus) -> Unit,
    onProceedToFraming: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (isBangla) "প্রাসঙ্গিক সিদ্ধান্তমূলক প্রশ্নাবলী (D2 Context)" else "Consequential Questions (D2 Context)",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isBangla)
                "শুধুমাত্র সেই প্রশ্নগুলির উত্তর দিন যা সিদ্ধান্তের ফলাফল পরিবর্তন করতে পারে। 'জানা নেই' উত্তরও গুরুত্বপূর্ণ সিদ্ধান্ত-তথ্য।"
            else
                "Answer only high-consequence questions. 'I don't know' is meaningful decision information.",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Questions List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(decision.questions, key = { it.id }) { question ->
                QuestionItemCard(
                    question = question,
                    isBangla = isBangla,
                    onSaveAnswer = { ans, status ->
                        onAnswerQuestion(question.id, ans, status)
                    }
                )
            }
        }

        // Proceed to Framing Button
        Button(
            onClick = onProceedToFraming,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_proceed_to_framing"),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = if (isBangla) "ফ্রেমিং যাচাইকরণে অগ্রসর হন (D3 Framing)" else "Proceed to Framing (D3)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next")
        }
    }
}

@Composable
fun QuestionItemCard(
    question: ContextQuestion,
    isBangla: Boolean,
    onSaveAnswer: (String, QuestionStatus) -> Unit
) {
    var textAnswer by remember { mutableStateOf(question.answer) }

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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = question.category,
                        color = GoldPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = question.status.name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (question.status == QuestionStatus.ANSWERED) CyanAccent else TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isBangla) question.questionBn else question.questionEn,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Consequence: " + if (isBangla) question.consequenceBn else question.consequenceEn,
                fontSize = 11.sp,
                color = CyanAccent
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = textAnswer,
                onValueChange = {
                    textAnswer = it
                    onSaveAnswer(it, QuestionStatus.ANSWERED)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(if (isBangla) "আপনার উত্তর লিখুন..." else "Type your answer...", fontSize = 12.sp) },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = KingDarkCardElevated,
                    unfocusedContainerColor = KingDarkCardElevated
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Pills (I don't know, Not Applicable, Defer)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        textAnswer = "UNKNOWN: বর্তমানে দলের কাছে পর্যাপ্ত তথ্য নেই。"
                        onSaveAnswer(textAnswer, QuestionStatus.UNKNOWN)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(text = if (isBangla) "জানা নেই (Unknown)" else "I Don't Know", fontSize = 10.sp)
                }

                OutlinedButton(
                    onClick = {
                        textAnswer = "N/A: প্রযোজ্য নয়।"
                        onSaveAnswer(textAnswer, QuestionStatus.NOT_APPLICABLE)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(text = if (isBangla) "প্রযোজ্য নয়" else "N/A", fontSize = 10.sp)
                }

                OutlinedButton(
                    onClick = {
                        textAnswer = "DEFERRED: পরবর্তী রিলিজে বিবেচনা করা হবে।"
                        onSaveAnswer(textAnswer, QuestionStatus.DEFERRED)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(text = if (isBangla) "স্থগিত (Defer)" else "Defer", fontSize = 10.sp)
                }
            }
        }
    }
}
