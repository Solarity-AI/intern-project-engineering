package com.productreview.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.productreview.app.ui.theme.AppTheme
import com.productreview.app.ui.theme.GradientColors

// Categories list
val categories = listOf(
    "All",
    "Electronics",
    "Audio",
    "Wearables",
    "Accessories",
    "Gaming"
)

@Composable
fun CategoryFilter(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = selectedCategory == category

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .then(
                        if (isSelected) {
                            Modifier
                                .shadow(4.dp, RoundedCornerShape(50))
                                .background(
                                    Brush.linearGradient(GradientColors.Primary),
                                    RoundedCornerShape(50)
                                )
                        } else {
                            Modifier.background(colors.secondary, RoundedCornerShape(50))
                        }
                    )
                    .clickable { onCategoryChange(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) colors.primaryForeground else colors.secondaryForeground
                )
            }
        }
    }
}

// Sort Options
data class SortOption(
    val value: String,
    val label: String
)

val sortOptions = listOf(
    SortOption("name,asc", "Aa Name (A-Z)"),
    SortOption("name,desc", "Aa Name (Z-A)"),
    SortOption("averageRating,desc", "★ Top Rated"),
    SortOption("price,asc", "Low → High"),
    SortOption("price,desc", "High → Low"),
    SortOption("reviewCount,desc", "Most Reviews")
)

@Composable
fun SortFilter(
    selectedSort: String,
    onSortChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sortOptions.forEach { option ->
            val isSelected = selectedSort == option.value

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isSelected) colors.primary else colors.secondary,
                        RoundedCornerShape(50)
                    )
                    .border(
                        1.dp,
                        if (isSelected) colors.primary else colors.border,
                        RoundedCornerShape(50)
                    )
                    .clickable { onSortChange(option.value) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = option.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) colors.primaryForeground else colors.secondaryForeground
                )
            }
        }
    }
}
