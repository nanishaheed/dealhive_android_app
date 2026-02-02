package com.simats.dealhive.ui.screens.compare

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.ui.components.DealButton
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.components.DealInput
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import com.simats.dealhive.util.UrlValidator

@Composable
fun PasteLinksScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLoading: (String, String) -> Unit
) {
    var url1 by remember { mutableStateOf("") }
    var url2 by remember { mutableStateOf("") }
    val context = LocalContext.current
    
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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        DealHeader(
            title = "Compare Deals",
            showBack = true,
            onBackClick = onNavigateBack
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.xl)
        ) {
            // Intro Area
            Text(
                text = "Paste & Compare",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Text(
                text = "Enter two product URLs to see a side-by-side comparison with AI insights.",
                style = MaterialTheme.typography.bodyLarge,
                color = textSecondaryColor,
                modifier = Modifier.padding(top = Spacing.xs)
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xxl))
            
            // Input Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(surfaceColor)
                    .padding(Spacing.xl)
            ) {
                Column {
                    // Product 1 URL
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = Spacing.sm)
                    ) {
                        Text(
                            text = "Product 1 URL",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondaryColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DealInput(
                        value = url1,
                        onValueChange = { url1 = it },
                        placeholder = "e.g. amazon.in/product-link",
                        keyboardType = KeyboardType.Uri
                    )
                    
                    // VS Circle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(surfaceLightColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.Companion.height(Spacing.md))
                    
                    // Product 2 URL
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = Spacing.sm)
                    ) {
                        Text(
                            text = "Product 2 URL",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondaryColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DealInput(
                        value = url2,
                        onValueChange = { url2 = it },
                        placeholder = "e.g. flipkart.com/product-link",
                        keyboardType = KeyboardType.Uri
                    )
                    
                    Spacer(modifier = Modifier.Companion.height(Spacing.md))

                    DealButton(
                        text = "Fetch & Compare",
                        onClick = {
                            when {
                                url1.isBlank() || url2.isBlank() -> {
                                    Toast.makeText(
                                        context,
                                        "Please paste two valid product links to compare.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                !UrlValidator.isValidCompareUrl(url1) || !UrlValidator.isValidCompareUrl(
                                    url2
                                ) -> {
                                    Toast.makeText(
                                        context,
                                        "Currently we only support Amazon.in and Flipkart.com links.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                else -> {
                                    onNavigateToLoading(url1, url2)
                                }
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xl))
            
            // Info Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(textColor.copy(alpha = 0.03f))
                    .padding(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = textMutedColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.Companion.width(Spacing.sm))
                Text(
                    text = "Pro Tip: You can also share links directly from Amazon/Flipkart app to Deal Hive!",
                    style = MaterialTheme.typography.bodySmall,
                    color = textMutedColor
                )
            }
        }
    }
}
