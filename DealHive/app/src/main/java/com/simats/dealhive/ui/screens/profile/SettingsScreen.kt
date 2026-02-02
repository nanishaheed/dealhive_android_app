package com.simats.dealhive.ui.screens.profile

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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import com.simats.dealhive.ui.theme.ThemeState

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isDarkMode = LocalIsDarkMode.current
    
    // Use MaterialTheme colors for dynamic theming
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMutedColor = MaterialTheme.colorScheme.outline
    val errorColor = MaterialTheme.colorScheme.error
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        DealHeader(
            title = "Settings",
            showBack = true,
            onBackClick = onNavigateBack
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.xl)
        ) {
            // Preferences Section
            SectionTitle("Preferences", textMutedColor)
            
            ToggleItem(
                icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                title = "Dark Mode",
                subtitle = if (isDarkMode) "Using dark theme" else "Using light theme",
                checked = isDarkMode,
                onCheckedChange = { newValue ->
                    ThemeState.updateTheme(newValue, context)
                },
                surfaceColor = surfaceColor,
                surfaceVariantColor = surfaceVariantColor,
                textColor = textColor,
                textSecondaryColor = textSecondaryColor
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xl))
            
            // Legal Section
            SectionTitle("Legal", textMutedColor)
            
            NavigationItem(
                icon = Icons.Default.Security,
                title = "Terms of Service",
                onClick = onNavigateToTerms,
                surfaceColor = surfaceColor,
                surfaceVariantColor = surfaceVariantColor,
                textColor = textColor,
                textMutedColor = textMutedColor
            )
            
            NavigationItem(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy Policy",
                onClick = onNavigateToPrivacy,
                surfaceColor = surfaceColor,
                surfaceVariantColor = surfaceVariantColor,
                textColor = textColor,
                textMutedColor = textMutedColor
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xl))
            
            // About Section
            SectionTitle("About", textMutedColor)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(surfaceColor)
                    .padding(Spacing.lg)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(surfaceVariantColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "App Version",
                            tint = Primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.Companion.width(Spacing.md))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "App Version",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                        Text(
                            text = "1.0.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryColor
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xxl))
            
            // Logout Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(errorColor.copy(alpha = 0.1f))
                    .clickable { onLogout() }
                    .padding(Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = errorColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.Companion.width(Spacing.sm))
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = errorColor
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String, textColor: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        color = textColor,
        modifier = Modifier.padding(bottom = Spacing.md)
    )
}

@Composable
private fun ToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    surfaceColor: Color,
    surfaceVariantColor: Color,
    textColor: Color,
    textSecondaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(surfaceVariantColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Primary,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.Companion.width(Spacing.md))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondaryColor
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = surfaceVariantColor
            )
        )
    }
    
    Spacer(modifier = Modifier.Companion.height(Spacing.md))
}

@Composable
private fun NavigationItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    surfaceColor: Color,
    surfaceVariantColor: Color,
    textColor: Color,
    textMutedColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .clickable { onClick() }
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(surfaceVariantColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Primary,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.Companion.width(Spacing.md))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = textMutedColor,
            modifier = Modifier.size(24.dp)
        )
    }
    
    Spacer(modifier = Modifier.Companion.height(Spacing.md))
}
