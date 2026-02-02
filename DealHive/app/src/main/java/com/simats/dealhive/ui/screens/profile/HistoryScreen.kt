package com.simats.dealhive.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.data.api.RetrofitClient
import com.simats.dealhive.data.model.ComparisonDto
import com.simats.dealhive.data.repository.AuthRepository
import com.simats.dealhive.ui.components.DealButton
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToComparison: (String) -> Unit
) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository.Companion.getInstance(context) }
    val currentUserId = authRepository.getCurrentUserId()
    
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMutedColor = MaterialTheme.colorScheme.outline
    
    var historyItems by remember { mutableStateOf<List<ComparisonDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Fetch comparison history from backend
    LaunchedEffect(currentUserId) {
        if (currentUserId == -1) {
            error = "Please log in to view history"
            isLoading = false
            return@LaunchedEffect
        }
        
        isLoading = true
        error = null
        try {
            val response = RetrofitClient.phpApiService.getComparisonHistory(currentUserId)
            historyItems = response.comparisons
            Log.d("HistoryScreen", "Loaded ${historyItems.size} history items")
        } catch (e: Exception) {
            error = "Failed to load history: ${e.message}"
            Log.e("HistoryScreen", "Error loading history", e)
        } finally {
            isLoading = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        DealHeader(
            title = "History",
            showBack = true,
            onBackClick = onNavigateBack
        )
        
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(modifier = Modifier.Companion.height(Spacing.md))
                        Text(
                            text = "Loading history...",
                            color = textSecondaryColor
                        )
                    }
                }
            }
            
            error != null && historyItems.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.Companion.padding(Spacing.xxl)
                    ) {
                        Text(
                            text = "Could not load history",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.Companion.height(Spacing.xs))
                        Text(
                            text = error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondaryColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.Companion.height(Spacing.xl))
                        DealButton(
                            text = "Go Back",
                            onClick = onNavigateBack
                        )
                    }
                }
            }
            
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Spacing.xl)
                ) {
                    Spacer(modifier = Modifier.Companion.height(Spacing.lg))
                    
                    Text(
                        text = "Comparison History",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    Text(
                        text = "${historyItems.size} comparisons",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondaryColor,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                    
                    Spacer(modifier = Modifier.Companion.height(Spacing.xl))
                    
                    if (historyItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CompareArrows,
                                    contentDescription = null,
                                    tint = textMutedColor,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.Companion.height(Spacing.md))
                                Text(
                                    text = "No comparisons yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textMutedColor
                                )
                                Spacer(modifier = Modifier.Companion.height(Spacing.xs))
                                Text(
                                    text = "Start comparing products to build your history!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textSecondaryColor
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(Spacing.md),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(historyItems) { item ->
                                HistoryCard(
                                    item = item,
                                    onClick = { onNavigateToComparison(item.id.toString()) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    item: ComparisonDto,
    onClick: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMutedColor = MaterialTheme.colorScheme.outline
    
    // Format date
    val formattedDate = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(item.createdAt ?: "")
        date?.let { outputFormat.format(it) } ?: item.createdAt ?: "Unknown date"
    } catch (e: Exception) {
        item.createdAt ?: "Unknown date"
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .clickable { onClick() }
            .padding(Spacing.lg)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product images with slight overlap effect
                Box(modifier = Modifier.width(80.dp).height(50.dp)) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = item.product1Image,
                            contentDescription = item.product1Title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.CenterEnd)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(4.dp)
                            // Add border to distinguish between overlapping images
                            .background(surfaceColor, RoundedCornerShape(10.dp))
                            .padding(2.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = item.product2Image,
                            contentDescription = item.product2Title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                
                Spacer(modifier = Modifier.Companion.width(Spacing.md))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${item.product1Title} vs ${item.product2Title}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (!item.aiVerdict.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Column {
                            Text(
                                text = "Verdict:",
                                style = MaterialTheme.typography.labelSmall,
                                color = textSecondaryColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.aiVerdict,
                                style = MaterialTheme.typography.bodySmall,
                                color = Primary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.sm))
            
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = textMutedColor
            )
        }
    }
}

