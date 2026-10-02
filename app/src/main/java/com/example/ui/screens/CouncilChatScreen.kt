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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.model.ChatMessage
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.KingDarkCard
import com.example.ui.theme.KingDarkCardElevated
import com.example.ui.theme.PurpleAdvisory
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CouncilChatScreen(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    isBangla: Boolean,
    onSendMessage: (text: String, useSearch: Boolean, useMaps: Boolean) -> Unit,
    onClearChat: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var useSearchGrounding by remember { mutableStateOf(false) }
    var useMapsGrounding by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBangla) "কাউন্সিল চ্যাটবট (Council Copilot)" else "Decision Council Copilot",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Text(
                    text = if (isBangla) "মাল্টি-টার্ন আলোচনা, Google Search এবং Google Maps গ্রাউন্ডিং সহ।" else "Multi-turn Gemini chatbot with Search & Maps Grounding.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
            IconButton(
                onClick = onClearChat,
                modifier = Modifier.testTag("btn_clear_chat")
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear", tint = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grounding Tool Toggle Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = useSearchGrounding,
                onClick = { useSearchGrounding = !useSearchGrounding },
                label = { Text("Google Search Grounding", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Language, contentDescription = "Search", modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                    selectedLabelColor = CyanAccent,
                    selectedLeadingIconColor = CyanAccent
                )
            )

            FilterChip(
                selected = useMapsGrounding,
                onClick = { useMapsGrounding = !useMapsGrounding },
                label = { Text("Google Maps Grounding", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Map, contentDescription = "Maps", modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldPrimary.copy(alpha = 0.2f),
                    selectedLabelColor = GoldPrimary,
                    selectedLeadingIconColor = GoldPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scrollable Chat Thread
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.SmartToy, contentDescription = "Copilot", tint = GoldPrimary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isBangla) "কাউন্সিলের কাছে কোনো আর্কিটেকচার প্রশ্ন জিজ্ঞাসা করুন।" else "Ask the Council any architectural trade-off question.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(message = msg, isBangla = isBangla)
                }
            }
        }

        // Input Field and Send Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_chat_message"),
                placeholder = { Text(if (isBangla) "প্রশ্ন লিখুন..." else "Ask Council...", fontSize = 13.sp) },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = KingDarkCard,
                    unfocusedContainerColor = KingDarkCard
                ),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputText.isNotBlank() && !isLoading) {
                        val text = inputText
                        inputText = ""
                        onSendMessage(text, useSearchGrounding, useMapsGrounding)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldPrimary)
                    .testTag("btn_send_chat")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                } else {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isBangla: Boolean) {
    val isUser = message.role == "user"
    val align = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) KingDarkCardElevated else KingDarkCard
    val borderColor = if (isUser) CyanAccent.copy(alpha = 0.4f) else BorderSubtle

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = bgColor),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 12.dp
            ),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isUser) "YOU (Owner)" else "COUNCIL (${message.modelName})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) CyanAccent else GoldPrimary
                    )
                }

                if (!message.thinkingProcess.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(PurpleAdvisory.copy(alpha = 0.15f))
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "Thinking Process:\n${message.thinkingProcess}",
                            fontSize = 10.sp,
                            color = PurpleAdvisory
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.content,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )

                if (message.sources.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Grounding Sources:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                    message.sources.forEach { src ->
                        Text(text = "• $src", fontSize = 9.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}
