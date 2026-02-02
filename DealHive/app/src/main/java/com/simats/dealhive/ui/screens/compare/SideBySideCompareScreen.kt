package com.simats.dealhive.ui.screens.compare

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.data.model.Offer
import com.simats.dealhive.data.model.Product
import com.simats.dealhive.ui.components.ButtonVariant
import com.simats.dealhive.ui.components.DealButton
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.theme.Error
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import com.simats.dealhive.ui.theme.Success
import com.simats.dealhive.ui.theme.White
import com.simats.dealhive.ui.viewmodel.ProductViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.collections.orEmpty

@Composable
fun SideBySideCompareScreen(
    productViewModel: ProductViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val priceFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))
    
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceLightColor = if (isDarkMode) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    }
    val textMutedColor = MaterialTheme.colorScheme.outline
    
    // Get products from ViewModel
    val products = productViewModel.comparisonProducts
    
    // Handle case where products aren't loaded
    if (products.size < 2) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Primary)
                Spacer(modifier = Modifier.Companion.height(Spacing.md))
                Text(
                    text = "Loading products...",
                    color = textSecondaryColor
                )
            }
        }
        return
    }
    
    val product1 = products[0]
    val product2 = products[1]
    val allSpecs = (product1.specs?.keys.orEmpty() + product2.specs?.keys.orEmpty()).distinct()
    
    // Calculate Category-Based Winner
    val categorizedResults = productViewModel.getCategorizedResults(product1, product2)
    val p1Wins = categorizedResults.count { it.winner == product1.title }
    val p2Wins = categorizedResults.count { it.winner == product2.title }
    val verdict = productViewModel.getComparisonVerdict(product1, product2)
    val winningProduct = productViewModel.getOverallWinner(product1, product2)
    val losingProduct = if (winningProduct == product1) product2 else product1
    val winningCatCount = if (winningProduct == product1) p1Wins else p2Wins

    
    // Function to open URL in browser
    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        DealHeader(
            title = "Comparison",
            showBack = true,
            onBackClick = onNavigateBack,
            rightContent = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(surfaceColor)
                        .clickable { /* Navigate to price history */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Price History",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.xl)
        ) {
            // Dual Product Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProductMiniCard(
                    product = product1,
                    isWinner = winningProduct == product1,
                    modifier = Modifier.weight(1f)
                )
                
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceLightColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 10.sp
                    )
                }
                
                ProductMiniCard(
                    product = product2,
                    isWinner = winningProduct == product2,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xl))
            
            // AI Verdict Banner (Advanced)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Primary.copy(alpha = 0.2f),
                                Primary.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .padding(Spacing.lg)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(textColor.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ADVANCED AI ANALYSIS",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Primary,
                            fontSize = 10.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = verdict,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Detailed Breakdown
                    Text(
                        text = "Analysis detail:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    Text(
                        text = "${winningProduct.title.split("|")[0].trim()} wins $winningCatCount out of ${categorizedResults.size} categories, making it the better overall choice.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondaryColor,
                        lineHeight = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xl))
            
            // Category Winners Section
            Text(
                text = "Category Winners",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            
            Spacer(modifier = Modifier.Companion.height(Spacing.md))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(surfaceColor)
            ) {
                Column {
                    categorizedResults.forEach { result ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = result.icon,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = Spacing.md)
                            )
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = result.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = textMutedColor
                                )
                                Text(
                                    text = result.winner.split("|")[0].trim(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (result.winner == product1.title || result.winner == product2.title) Primary else textColor
                                )
                                Text(
                                    text = result.reason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondaryColor,
                                    fontSize = 10.sp
                                )
                            }
                            
                            if (result.winner == product1.title || result.winner == product2.title) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Success,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        if (result != categorizedResults.last()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(textColor.copy(alpha = 0.05f))
                                    .padding(horizontal = Spacing.md)
                             )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xl))
            
            // Specs Section Header
            Text(
                text = "Technical Specs",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.md))
            
            // Specs Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(surfaceColor)
            ) {
                Column {
                    allSpecs.forEach { spec ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.md)
                        ) {
                            Text(
                                text = spec.uppercase(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = textMutedColor,
                                textAlign = TextAlign.Center,
                                letterSpacing = 1.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = product1.specs?.get(spec) ?: "-",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(15.dp)
                                        .background(textColor.copy(alpha = 0.1f))
                                )
                                
                                Text(
                                    text = product2.specs?.get(spec) ?: "-",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Primary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        if (spec != allSpecs.last()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(textColor.copy(alpha = 0.05f))
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xl))
            
            // Offers Section
            val hasProduct1Offers = !product1.offers.isNullOrEmpty()
            val hasProduct2Offers = !product2.offers.isNullOrEmpty()
            
            if (hasProduct1Offers || hasProduct2Offers) {
                Text(
                    text = "🏷️ AVAILABLE OFFERS",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = textMutedColor,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.Companion.height(Spacing.md))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    OffersColumn(
                        offers = product1.offers ?: emptyList(),
                        productName = product1.source.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.weight(1f)
                    )
                    
                    OffersColumn(
                        offers = product2.offers ?: emptyList(),
                        productName = product2.source.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.Companion.height(Spacing.xl))
            }
            
            // Dynamically Generated Pros & Cons based on category analysis
            val product1Pros = mutableListOf<String>()
            val product1Cons = mutableListOf<String>()
            val product2Pros = mutableListOf<String>()
            val product2Cons = mutableListOf<String>()
            
            // Analyze category results to build pros/cons
            categorizedResults.forEach { result ->
                val shortName1 = product1.title.split("|")[0].split("(")[0].trim().take(15)
                val shortName2 = product2.title.split("|")[0].split("(")[0].trim().take(15)
                
                when (result.category) {
                    "Performance" -> {
                        if (result.winner == product1.title) {
                            product1Pros.add("Better Performance")
                            product2Cons.add("Slower Processor")
                        } else {
                            product2Pros.add("Better Performance")
                            product1Cons.add("Slower Processor")
                        }
                    }
                    "Camera" -> {
                        if (result.winner == product1.title) {
                            product1Pros.add("Superior Camera")
                            product2Cons.add("Basic Camera")
                        } else {
                            product2Pros.add("Superior Camera")
                            product1Cons.add("Basic Camera")
                        }
                    }
                    "Battery Life" -> {
                        if (result.winner == product1.title) {
                            product1Pros.add("Longer Battery")
                            product2Cons.add("Smaller Battery")
                        } else {
                            product2Pros.add("Longer Battery")
                            product1Cons.add("Smaller Battery")
                        }
                    }
                    "Display" -> {
                        if (result.winner == product1.title) {
                            product1Pros.add("Better Display")
                            product2Cons.add("Basic Display")
                        } else {
                            product2Pros.add("Better Display")
                            product1Cons.add("Basic Display")
                        }
                    }
                    "Software" -> {
                        if (result.winner == product1.title) {
                            product1Pros.add("Better Updates")
                            product2Cons.add("Fewer Updates")
                        } else {
                            product2Pros.add("Better Updates")
                            product1Cons.add("Fewer Updates")
                        }
                    }
                    "Value" -> {
                        if (result.winner == product1.title) {
                            product1Pros.add("Better Value")
                            product2Cons.add("Expensive")
                        } else {
                            product2Pros.add("Better Value")
                            product1Cons.add("Expensive")
                        }
                    }
                }
            }
            
            ProsConsCard(
                title = product1.title.split("|")[0].split("(")[0].trim(),
                pros = product1Pros.take(3),
                cons = product1Cons.take(2)
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.md))
            
            ProsConsCard(
                title = product2.title.split("|")[0].split("(")[0].trim(),
                pros = product2Pros.take(3),
                cons = product2Cons.take(2)
            )
            
            Spacer(modifier = Modifier.height(120.dp))
        }
        
        // Buy Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(surfaceColor)
                .padding(horizontal = Spacing.xl, vertical = Spacing.xl)
                .padding(bottom = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "₹${priceFormat.format(product1.price)}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                DealButton(
                    text = "Buy",
                    onClick = { openUrl(product1.url) },
                    variant = ButtonVariant.SECONDARY
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "₹${priceFormat.format(product2.price)}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                DealButton(
                    text = "Buy",
                    onClick = { openUrl(product2.url) }
                )
            }
        }
    }
}

@Composable
private fun ProductMiniCard(
    product: Product,
    isWinner: Boolean,
    modifier: Modifier = Modifier
) {
    val priceFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .padding(Spacing.md)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(White)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (product.image.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        tint = Primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    AsyncImage(
                        model = product.image,
                        contentDescription = product.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.sm))
            
            Text(
                text = product.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = "₹${priceFormat.format(product.price)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Primary
            )
        }
    }
}

@Composable
private fun ProsConsCard(
    title: String,
    pros: List<String>,
    cons: List<String>
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .padding(Spacing.md)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.sm))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    pros.forEach { pro ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Success,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = pro,
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondaryColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    cons.forEach { con ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Error,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = con,
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondaryColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OffersColumn(
    offers: List<Offer>,
    productName: String,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textMutedColor = MaterialTheme.colorScheme.outline
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .padding(Spacing.md)
    ) {
        Text(
            text = productName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Primary,
            modifier = Modifier.padding(bottom = Spacing.sm)
        )
        
        if (offers.isEmpty()) {
            Text(
                text = "No offers found",
                style = MaterialTheme.typography.bodySmall,
                color = textMutedColor
            )
        } else {
            offers.take(5).forEachIndexed { index, offer ->
                OfferItem(offer = offer)
                if (index < offers.size - 1 && index < 4) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
            
            if (offers.size > 5) {
                Text(
                    text = "+${offers.size - 5} more offers",
                    style = MaterialTheme.typography.bodySmall,
                    color = Primary,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }
    }
}

@Composable
private fun OfferItem(offer: Offer) {
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iconColor = when (offer.type) {
        "bank" -> Color(0xFF3B82F6)
        "exchange" -> Color(0xFFF59E0B)
        "coupon" -> Color(0xFFA855F7)
        "emi" -> Color(0xFF22C55E)
        "delivery" -> Color(0xFF10B981)
        "combo" -> Color(0xFFEC4899)
        else -> Primary
    }
    
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = offer.icon ?: "✨",
            fontSize = 12.sp,
            modifier = Modifier.padding(end = 4.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = offer.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = iconColor,
                fontSize = 10.sp
            )
            Text(
                text = offer.description,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondaryColor,
                fontSize = 9.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 12.sp
            )
        }
    }
}
