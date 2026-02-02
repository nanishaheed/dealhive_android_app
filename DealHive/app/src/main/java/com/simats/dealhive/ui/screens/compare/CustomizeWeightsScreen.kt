package com.simats.dealhive.ui.screens.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.ui.components.DealButton
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import com.simats.dealhive.ui.viewmodel.ProductViewModel

@Composable
fun CustomizeWeightsScreen(
    productViewModel: ProductViewModel,
    onNavigateBack: () -> Unit
) {
    var priceWeight by remember { mutableFloatStateOf(productViewModel.priceWeight) }
    var ratingWeight by remember { mutableFloatStateOf(productViewModel.ratingWeight) }
    var featuresWeight by remember { mutableFloatStateOf(productViewModel.featuresWeight) }
    var brandWeight by remember { mutableFloatStateOf(productViewModel.brandWeight) }
    
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
            title = "Customize Weights",
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
                text = "Adjust Priority Weights",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Text(
                text = "Customize how much each factor influences the AI comparison results.",
                style = MaterialTheme.typography.bodyLarge,
                color = textSecondaryColor,
                modifier = Modifier.padding(top = Spacing.xs)
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xxl))
            
            WeightSlider(
                label = "Price",
                description = "How important is getting the lowest price?",
                value = priceWeight,
                onValueChange = { priceWeight = it }
            )
            
            WeightSlider(
                label = "Rating & Reviews",
                description = "How much do customer ratings matter?",
                value = ratingWeight,
                onValueChange = { ratingWeight = it }
            )
            
            WeightSlider(
                label = "Features",
                description = "How important are technical specifications?",
                value = featuresWeight,
                onValueChange = { featuresWeight = it }
            )
            
            WeightSlider(
                label = "Brand Reputation",
                description = "How much does brand name influence your decision?",
                value = brandWeight,
                onValueChange = { brandWeight = it }
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xl))

            DealButton(
                text = "Apply Weights",
                onClick = {
                    productViewModel.updateWeights(
                        priceWeight,
                        ratingWeight,
                        featuresWeight,
                        brandWeight
                    )
                    onNavigateBack()
                }
            )
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun WeightSlider(
    label: String,
    description: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .padding(Spacing.lg)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = onSurfaceColor
        )
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = textSecondaryColor,
            modifier = Modifier.padding(top = 2.dp)
        )
        
        Spacer(modifier = Modifier.Companion.height(Spacing.md))
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Primary,
                activeTrackColor = Primary,
                inactiveTrackColor = Primary.copy(alpha = 0.3f)
            )
        )
        
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
    }
    
    Spacer(modifier = Modifier.Companion.height(Spacing.md))
}
