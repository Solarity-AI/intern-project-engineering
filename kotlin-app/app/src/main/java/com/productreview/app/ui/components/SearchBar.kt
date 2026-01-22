package com.productreview.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.productreview.app.ui.theme.AppTheme

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    searchHistory: List<String>,
    onHistoryItemClick: (String) -> Unit,
    onRemoveHistoryItem: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search products..."
) {
    val colors = AppTheme.colors
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    val showHistory = isFocused && searchHistory.isNotEmpty()

    Box(modifier = modifier) {
        Column {
            // Search Input
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colors.secondary
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            onSearch(value)
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = colors.mutedForeground
                        )
                    }

                    TextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { isFocused = it.isFocused },
                        placeholder = {
                            Text(
                                text = placeholder,
                                color = colors.mutedForeground
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            cursorColor = colors.primary
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                onSearch(value)
                                focusManager.clearFocus()
                            }
                        )
                    )

                    if (value.isNotEmpty()) {
                        IconButton(
                            onClick = { onValueChange("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = colors.mutedForeground
                            )
                        }
                    }
                }
            }

            // Search History Dropdown
            AnimatedVisibility(
                visible = showHistory,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.card
                ) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(searchHistory) { term ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onHistoryItemClick(term)
                                        focusManager.clearFocus()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = colors.mutedForeground,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = term,
                                    modifier = Modifier.weight(1f),
                                    color = colors.foreground
                                )
                                IconButton(
                                    onClick = { onRemoveHistoryItem(term) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = colors.mutedForeground,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            HorizontalDivider(color = colors.border.copy(alpha = 0.3f))
                        }

                        if (searchHistory.isNotEmpty()) {
                            item {
                                TextButton(
                                    onClick = onClearHistory,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Clear Search History",
                                        color = colors.destructive,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
