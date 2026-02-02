package com.simats.dealhive.ui.screens.compare

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.data.api.RetrofitClient
import com.simats.dealhive.data.model.CreateProductRequest
import com.simats.dealhive.data.model.SaveComparisonRequest
import com.simats.dealhive.data.repository.AuthRepository
import com.simats.dealhive.ui.theme.Error
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import com.simats.dealhive.ui.viewmodel.ProductViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

private val loadingSteps = listOf(
    "Fetching product data...",
    "Analyzing specifications...",
    "Comparing prices...",
    "Generating AI insights...",
    "Preparing comparison..."
)

@Composable
fun ComparisonLoadingScreen(
    url1: String,
    url2: String,
    productViewModel: ProductViewModel,
    onNavigateToComparison: () -> Unit,
    onError: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Pulse animations for loading dots
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse1"
    )
    
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse2"
    )
    
    val pulse3 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse3"
    )
    
    LaunchedEffect(url1, url2) {
        try {
            currentStep = 0
            Log.d("ComparisonLoading", "Scraping URL1: $url1")
            Log.d("ComparisonLoading", "Scraping URL2: $url2")
            
            // Scrape both products in parallel
            val (product1, product2) = coroutineScope {
                val deferred1 = async { 
                    currentStep = 0
                    RetrofitClient.apiService.scrapeProduct(url1)
                }
                val deferred2 = async { 
                    RetrofitClient.apiService.scrapeProduct(url2)
                }
                
                currentStep = 1 // Analyzing specifications
                Pair(deferred1.await(), deferred2.await())
            }
            
            currentStep = 2 // Comparing prices
            Log.d("ComparisonLoading", "Product 1: ${product1.title}")
            Log.d("ComparisonLoading", "Product 2: ${product2.title}")
            
            currentStep = 3 // Generating AI insights
            
            // Save products to database and get their IDs
            val userId = AuthRepository.Companion.getInstance(context).getCurrentUserId()
            
            try {
                // Save product 1 to database
                val product1Request = CreateProductRequest(
                    source = product1.source,
                    title = product1.title,
                    price = product1.price,
                    originalPrice = product1.originalPrice,
                    rating = product1.rating,
                    reviews = product1.reviews,
                    image = product1.image,
                    url = product1.url,
                    specs = product1.specs
                )
                val product1Response = RetrofitClient.phpApiService.createProduct(product1Request)
                val product1Id = product1Response.id ?: 0
                
                // Save product 2 to database
                val product2Request = CreateProductRequest(
                    source = product2.source,
                    title = product2.title,
                    price = product2.price,
                    originalPrice = product2.originalPrice,
                    rating = product2.rating,
                    reviews = product2.reviews,
                    image = product2.image,
                    url = product2.url,
                    specs = product2.specs
                )
                val product2Response = RetrofitClient.phpApiService.createProduct(product2Request)
                val product2Id = product2Response.id ?: 0
                
                // Save comparison to database
                if (userId > 0 && product1Id > 0 && product2Id > 0) {
                    val overallWinner = productViewModel.getOverallWinner(product1, product2)
                    val winnerId = if (overallWinner == product2) product2Id else product1Id
                    val comparisonRequest = SaveComparisonRequest(
                        userId = userId,
                        product1Id = product1Id,
                        product2Id = product2Id,
                        winnerId = winnerId,
                        aiVerdict = productViewModel.getComparisonVerdict(product1, product2)
                    )
                    RetrofitClient.phpApiService.saveComparison(comparisonRequest)
                    Log.d("ComparisonLoading", "Comparison saved with MCDA logic!")
                }
            } catch (e: Exception) {
                Log.e("ComparisonLoading", "Failed to save to database: ${e.message}")
                // Continue anyway - comparison can still be shown
            }
            
            // Store products in ViewModel
            productViewModel.setComparisonProducts(product1, product2)
            
            currentStep = 4 // Preparing comparison
            
            Log.d("ComparisonLoading", "Products stored in ViewModel, navigating to comparison")
            onNavigateToComparison()
            
        } catch (e: Exception) {
            Log.e("ComparisonLoading", "Error scraping products: ${e.message}", e)
            errorMessage = "Failed to load products: ${e.message}"
            // Removed onError() to let user see the error message
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(backgroundColor, surfaceColor)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.Companion.padding(Spacing.xl)
        ) {
            // Animated Icon or Error Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        if (errorMessage != null) Error.copy(alpha = 0.15f)
                        else Primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (errorMessage != null) Icons.Default.Close 
                                 else Icons.Default.CompareArrows,
                    contentDescription = null,
                    tint = if (errorMessage != null) Error else Primary,
                    modifier = Modifier
                        .size(50.dp)
                        .then(if (errorMessage == null) Modifier.rotate(rotation) else Modifier)
                )
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xxl))
            
            Text(
                text = if (errorMessage != null) "Error" else "Comparing Products",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.md))
            
            Text(
                text = errorMessage ?: loadingSteps.getOrElse(currentStep) { loadingSteps.last() },
                style = MaterialTheme.typography.bodyLarge,
                color = if (errorMessage != null) Error else textSecondaryColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xxl))
            
            if (errorMessage == null) {
                // Custom pulsing dots loading indicator
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .scale(pulse1)
                            .clip(CircleShape)
                            .background(Primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .scale(pulse2)
                            .clip(CircleShape)
                            .background(Primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .scale(pulse3)
                            .clip(CircleShape)
                            .background(Primary)
                    )
                }
                
                Spacer(modifier = Modifier.Companion.height(Spacing.xl))
                
                // Step indicators
                Text(
                    text = "Step ${currentStep + 1} of ${loadingSteps.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondaryColor
                )
            } else {
                // Error State - Go Back Button
                Button(
                    onClick = onError,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    )
                ) {
                    Text("Go Back")
                }
            }
        }
    }
}


