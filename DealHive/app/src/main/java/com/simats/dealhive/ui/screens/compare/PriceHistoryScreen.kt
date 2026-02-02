package com.simats.dealhive.ui.screens.compare

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import com.simats.dealhive.ui.theme.Success

// Sample price history data points
private val priceHistoryData = listOf(
    "Dec 1" to 139999,
    "Dec 8" to 134999,
    "Dec 15" to 129999,
    "Dec 22" to 124999,
    "Dec 29" to 119999,
    "Jan 1" to 124999
)

@Composable
fun PriceHistoryScreen(
    onNavigateBack: () -> Unit
) {
    val maxPrice = priceHistoryData.maxOf { it.second }
    val minPrice = priceHistoryData.minOf { it.second }
    val priceRange = maxPrice - minPrice
    
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        DealHeader(
            title = "Price History",
            showBack = true,
            onBackClick = onNavigateBack
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.xl)
        ) {
            Text(
                text = "Price Trends",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Text(
                text = "Track price changes over the last 30 days",
                style = MaterialTheme.typography.bodyLarge,
                color = textSecondaryColor,
                modifier = Modifier.padding(top = Spacing.xs)
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xxl))
            
            // Price stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                StatCard(
                    title = "Current",
                    value = "₹${priceHistoryData.last().second.formatPrice()}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Lowest",
                    value = "₹${minPrice.formatPrice()}",
                    isHighlight = true,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Highest",
                    value = "₹${maxPrice.formatPrice()}",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xxl))
            
            // Simple bar chart visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(surfaceColor)
                    .padding(Spacing.lg)
            ) {
                Column {
                    Text(
                        text = "Price Chart",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.Companion.height(Spacing.lg))
                    
                    // Bar chart
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        priceHistoryData.forEach { (date, price) ->
                            val heightPercent = if (priceRange > 0) {
                                ((price - minPrice).toFloat() / priceRange) * 0.7f + 0.3f
                            } else {
                                1f
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                  contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.6f)
                                            .fillMaxSize(heightPercent)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(Primary, Primary.copy(alpha = 0.5f))
                                                )
                                            )
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = date.split(" ").last(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondaryColor
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xl))
            
            // Price alerts info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(surfaceColor)
                    .padding(Spacing.lg)
            ) {
                Column {
                    Text(
                        text = "💡 Price Alert",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.Companion.height(Spacing.sm))
                    
                    Text(
                        text = "This product is currently 11% cheaper than its highest price. Based on the trend, prices may rise after the holiday season.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondaryColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isHighlight) Success.copy(alpha = 0.15f) else surfaceColor)
            .padding(Spacing.md)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondaryColor
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isHighlight) Success else onSurfaceColor
            )
        }
    }
}

private fun Int.formatPrice(): String {
    return String.format("%,d", this)
}
