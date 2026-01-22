package com.productreview.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.productreview.app.ui.theme.AppTheme
import com.productreview.app.ui.theme.GradientColors

private const val MIN_REVIEW_LEN = 10
private const val MAX_REVIEW_LEN = 500
private const val MIN_NAME_LEN = 2
private const val MAX_NAME_LEN = 50

@Composable
fun AddReviewModal(
    productName: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (userName: String?, rating: Int, comment: String) -> Unit
) {
    val colors = AppTheme.colors
    
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val trimmedComment = comment.trim()
    val trimmedName = userName.trim()

    fun validateAndSubmit() {
        // Rating validation
        if (rating == 0) {
            errorMessage = "Please select at least one star before submitting your review."
            return
        }

        // Comment validation
        if (trimmedComment.length < MIN_REVIEW_LEN) {
            errorMessage = "Your review must be at least $MIN_REVIEW_LEN characters long."
            return
        }

        if (trimmedComment.length > MAX_REVIEW_LEN) {
            errorMessage = "Your review cannot exceed $MAX_REVIEW_LEN characters."
            return
        }

        // Name validation (optional, only validate if provided)
        if (trimmedName.isNotEmpty() && trimmedName.length < MIN_NAME_LEN) {
            errorMessage = "Your name must be at least $MIN_NAME_LEN characters long (or leave it empty)."
            return
        }

        if (trimmedName.length > MAX_NAME_LEN) {
            errorMessage = "Your name cannot exceed $MAX_NAME_LEN characters."
            return
        }

        errorMessage = null
        onSubmit(
            trimmedName.takeIf { it.isNotEmpty() },
            rating,
            trimmedComment
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isSubmitting,
            dismissOnClickOutside = !isSubmitting
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.card)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Write a Review",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.foreground
                        )
                        Text(
                            text = productName,
                            fontSize = 16.sp,
                            color = colors.mutedForeground,
                            maxLines = 1
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .size(36.dp)
                            .background(colors.secondary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.mutedForeground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Error Message
                    errorMessage?.let { error ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = colors.destructive.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = error,
                                color = colors.destructive,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Rating Field
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Your Rating",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.foreground
                        )
                        StarRating(
                            rating = rating.toDouble(),
                            size = StarSize.LARGE,
                            interactive = true,
                            onRatingChange = { rating = it }
                        )
                    }

                    // Name Field
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Your Name (optional)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.foreground
                        )
                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your name", color = colors.mutedForeground) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedContainerColor = colors.secondary,
                                unfocusedContainerColor = colors.secondary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        Text(
                            text = "${trimmedName.length}/$MAX_NAME_LEN",
                            fontSize = 12.sp,
                            color = colors.mutedForeground
                        )
                    }

                    // Comment Field
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Your Review",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.foreground
                        )
                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { 
                                Text(
                                    "Share your experience with this product...",
                                    color = colors.mutedForeground
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedContainerColor = colors.secondary,
                                unfocusedContainerColor = colors.secondary
                            )
                        )
                        Text(
                            text = "Minimum $MIN_REVIEW_LEN characters (${trimmedComment.length}/$MAX_REVIEW_LEN)",
                            fontSize = 12.sp,
                            color = colors.mutedForeground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.foreground
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { validateAndSubmit() },
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(GradientColors.Primary),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = colors.primaryForeground,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Submit Review",
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.primaryForeground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
