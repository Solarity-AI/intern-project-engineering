package com.productreview.app.ui.screens.aiassistant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.productreview.app.ui.theme.AppTheme
import com.productreview.app.ui.theme.GradientColors

@Composable
fun AIAssistantScreen(
    productId: String,
    productName: String,
    onNavigateBack: () -> Unit,
    viewModel: AIAssistantViewModel = hiltViewModel()
) {
    val colors = AppTheme.colors
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Initialize
    LaunchedEffect(productId, productName) {
        viewModel.initialize(productId, productName)
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp,
            color = colors.background
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.foreground
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Brush.linearGradient(GradientColors.AI),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "AI Assistant",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.foreground
                        )
                        Text(
                            text = "Analyzing reviews for $productName",
                            fontSize = 12.sp,
                            color = colors.mutedForeground,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(40.dp))
            }
        }

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.messages) { message ->
                MessageBubble(
                    message = message,
                    isActive = message.id == uiState.lastActiveMessageId,
                    waitingForMore = uiState.waitingForMore,
                    isLoading = uiState.isLoading,
                    isExiting = uiState.isExiting,
                    onOptionClick = { option ->
                        if (uiState.waitingForMore) {
                            viewModel.handleMoreQuestions(option, message.id, onNavigateBack)
                        } else {
                            viewModel.selectQuestion(option, message.id)
                        }
                    }
                )
            }

            // Loading indicator
            if (uiState.isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .background(colors.card, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = colors.primary,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Analyzing reviews...",
                            fontSize = 14.sp,
                            color = colors.mutedForeground
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isActive: Boolean,
    waitingForMore: Boolean,
    isLoading: Boolean,
    isExiting: Boolean,
    onOptionClick: (String) -> Unit
) {
    val colors = AppTheme.colors
    val isUser = message.role == MessageRole.USER
    val showOptions = !isUser && isActive && !message.options.isNullOrEmpty()
    val isDisabled = isLoading || isExiting

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // AI Icon (for assistant messages)
        if (!isUser && !message.hideIcon) {
            Box(
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .size(32.dp)
                    .shadow(4.dp, CircleShape)
                    .background(
                        Brush.linearGradient(GradientColors.AI),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Message Content
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (isUser) colors.primary else colors.card,
            border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, colors.border) else null,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = if (isUser) colors.primaryForeground else colors.foreground
                )

                // Options
                if (showOptions) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (waitingForMore) "Do you have more questions?" else "Choose a question:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.mutedForeground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    message.options?.forEach { option ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = !isDisabled) { onOptionClick(option) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDisabled) colors.muted else colors.secondary,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                        ) {
                            Text(
                                text = option,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDisabled) colors.mutedForeground else colors.foreground,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Timestamp
        Text(
            text = message.timestamp,
            fontSize = 12.sp,
            color = colors.mutedForeground,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
