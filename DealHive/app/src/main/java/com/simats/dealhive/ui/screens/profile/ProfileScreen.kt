package com.simats.dealhive.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.data.repository.AuthRepository
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSavedItems: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdmin: () -> Unit = {},
    onLogout: () -> Unit = {},
    onVerifyAccount: () -> Unit = {},
    onDeleteAccount: () -> Unit = {}
) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository.Companion.getInstance(context) }
    
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textMutedColor = MaterialTheme.colorScheme.outline
    val errorColor = MaterialTheme.colorScheme.error
    
    // Get user data from session
    val userName = authRepository.getCurrentUserName() ?: "Guest User"
    val userEmail = authRepository.getCurrentUserEmail() ?: "Not signed in"
    val userAvatar = authRepository.getCurrentUserAvatar()
    val isAdmin = authRepository.isAdmin()
    val coroutineScope = rememberCoroutineScope()
    
    // Dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Delete Account Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        coroutineScope.launch {
                            authRepository.deleteAccount()
                            Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                            onDeleteAccount()
                        }
                    }
                ) {
                    Text("Delete", color = errorColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        DealHeader(
            title = "Profile",
            showBack = true,
            onBackClick = onNavigateBack
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.xl)
        ) {
            // Profile Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!userAvatar.isNullOrBlank()) {
                        AsyncImage(
                            model = userAvatar,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            tint = Primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.Companion.width(Spacing.lg))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondaryColor
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(surfaceColor)
                        .clickable { onNavigateToEditProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xxl))
            
            // Menu Items
            Text(
                text = "Account",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = textMutedColor,
                modifier = Modifier.padding(bottom = Spacing.md)
            )
            
            MenuItem(
                icon = Icons.Default.History,
                title = "Comparison History",
                subtitle = "View your past comparisons",
                onClick = onNavigateToHistory
            )
            
            MenuItem(
                icon = Icons.Default.Bookmark,
                title = "Saved Items",
                subtitle = "Products you've bookmarked",
                onClick = onNavigateToSavedItems
            )
            
            MenuItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                subtitle = "App preferences and account",
                onClick = onNavigateToSettings
            )
            
            MenuItem(
                icon = Icons.Default.DeleteOutline,
                title = "Delete Account",
                subtitle = "Permanently delete your account",
                onClick = { showDeleteDialog = true },
                isDestructive = true
            )
            
            // Show Admin Dashboard for admin users
            if (isAdmin) {
                Spacer(modifier = Modifier.Companion.height(Spacing.lg))
                
                Text(
                    text = "Administration",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = textMutedColor,
                    modifier = Modifier.padding(bottom = Spacing.md)
                )
                
                MenuItem(
                    icon = Icons.Default.Shield,
                    title = "Admin Dashboard",
                    subtitle = "Manage products, categories & users",
                    onClick = onNavigateToAdmin
                )
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.lg))
            
            // Logout Section
            Text(
                text = "Session",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = textMutedColor,
                modifier = Modifier.padding(bottom = Spacing.md)
            )
            
            // Logout Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(errorColor.copy(alpha = 0.1f))
                    .clickable { 
                        authRepository.logout()
                        onLogout()
                    }
                    .padding(Spacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(errorColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = errorColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.Companion.width(Spacing.md))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = errorColor
                    )
                    Text(
                        text = "Sign out of your account",
                        style = MaterialTheme.typography.bodySmall,
                        color = errorColor.copy(alpha = 0.7f)
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = errorColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMutedColor = MaterialTheme.colorScheme.outline
    val errorColor = MaterialTheme.colorScheme.error
    
    val iconTint = if (isDestructive) errorColor else Primary
    val titleColor = if (isDestructive) errorColor else textColor
    val subtitleColor = if (isDestructive) errorColor.copy(alpha = 0.7f) else textSecondaryColor
    val bgColor = if (isDestructive) errorColor.copy(alpha = 0.1f) else surfaceColor
    val iconBgColor = if (isDestructive) errorColor.copy(alpha = 0.15f) else surfaceVariantColor
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.Companion.width(Spacing.md))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor
            )
        }
        
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = if (isDestructive) errorColor.copy(alpha = 0.5f) else textMutedColor,
            modifier = Modifier.size(24.dp)
        )
    }
    
    Spacer(modifier = Modifier.Companion.height(Spacing.md))
}

