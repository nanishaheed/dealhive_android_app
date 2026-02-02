package com.simats.dealhive.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.data.model.KeyFeature
import com.simats.dealhive.data.model.Product
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.components.ProductCard
import com.simats.dealhive.ui.theme.Spacing

// Sample search results
private val searchResults = listOf(
    Product(
        id = "1",
        source = "amazon",
        title = "Samsung Galaxy S24 Ultra",
        price = 124999,
        originalPrice = 139999,
        rating = 4.5f,
        reviews = "2,456",
        image = "https://m.media-amazon.com/images/I/71lD7eGdW-L._SX679_.jpg",
        specs = mapOf("Display" to "6.8-inch AMOLED"),
        keyFeatures = listOf(
            KeyFeature("Monitor", "6.8-inch AMOLED"),
            KeyFeature("Cpu", "Snapdragon 8 Gen 3")
        ),
        url = "https://amazon.in/product"
    ),
    Product(
        id = "2",
        source = "flipkart",
        title = "Samsung Galaxy S24+",
        price = 89999,
        originalPrice = 99999,
        rating = 4.3f,
        reviews = "1,234",
        image = "https://m.media-amazon.com/images/I/7 Sa3dqTqzL._SX679_.jpg",
        specs = mapOf("Display" to "6.7-inch AMOLED"),
        keyFeatures = listOf(
            KeyFeature("Monitor", "6.7-inch AMOLED"),
            KeyFeature("Cpu", "Exynos 2400")
        ),
        url = "https://flipkart.com/product"
    )
)

@Composable
fun SearchResultsScreen(
    query: String,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        DealHeader(
            title = "Search Results",
            showBack = true,
            onBackClick = onNavigateBack
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.xl)
        ) {
            Spacer(modifier = Modifier.Companion.height(Spacing.lg))
            
            Text(
                text = "Results for \"$query\"",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Text(
                text = "${searchResults.size} products found",
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondaryColor,
                modifier = Modifier.padding(top = Spacing.xs)
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xl))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(searchResults) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onNavigateToDetail(product.id) }
                    )
                }
            }
        }
    }
}
