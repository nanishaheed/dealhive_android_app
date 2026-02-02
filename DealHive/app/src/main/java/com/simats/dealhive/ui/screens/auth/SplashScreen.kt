package com.simats.dealhive.ui.screens.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.data.repository.AuthRepository
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.White
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToAdminDashboard: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {}
) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository.Companion.getInstance(context) }
    
    var startAnimation by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }
    var showLoader by remember { mutableStateOf(false) }
    
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    // Main fade-in animation
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "splash_alpha"
    )
    
    // Scale animation for logo
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "splash_scale"
    )
    
    // Tagline fade-in
    val taglineAlpha by animateFloatAsState(
        targetValue = if (showTagline) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "tagline_alpha"
    )
    
    // Loader fade-in
    val taglineTextColor = if (isDarkMode) White.copy(alpha = 0.7f) else textColor.copy(alpha = 0.7f)
    val versionTextColor = if (isDarkMode) White.copy(alpha = 0.5f) else textColor.copy(alpha = 0.5f)
    val loaderBackground = if (isDarkMode) White.copy(alpha = 0.1f) else textColor.copy(alpha = 0.1f)

    val loaderAlpha by animateFloatAsState(
        targetValue = if (showLoader) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "loader_alpha"
    )
    
    // Infinite pulsing animation for the glow
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Shimmer animation for loader
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(400)
        showTagline = true
        delay(400)
        showLoader = true
        delay(1500)
        
        // Check if user is already logged in
        if (authRepository.isLoggedIn()) {
            // Navigate based on user role
            if (authRepository.isAdmin()) {
                onNavigateToAdminDashboard()
            } else {
                // Check subscription for regular users
                if (authRepository.isSubscribed()) {
                    onNavigateToHome()
                } else {
                    onNavigateToSubscription()
                }
            }
        } else {
            onNavigateToLogin()
        }
    }
    
    // Modern gradient background colors
    val gradientColors = if (isDarkMode) {
        listOf(
            Color(0xFF0A0E21),      // Deep dark blue
            Color(0xFF1A1F38),      // Dark purple-blue
            Color(0xFF0F1629)       // Dark blue
        )
    } else {
        listOf(
            Color(0xFFF8FAFC),      // Light gray blue
            Color(0xFFEFF6FF),      // Lightest blue
            Color(0xFFF1F5F9)       // Light slate
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = gradientColors)
            )
    ) {
        // Decorative gradient orbs in background
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (-100).dp)
                .blur(100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Primary.copy(alpha = if (isDarkMode) 0.3f else 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 120.dp)
                .blur(100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6366F1).copy(alpha = if (isDarkMode) 0.25f else 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo container with glow effect
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .alpha(alphaAnim)
                    .scale(scaleAnim)
            ) {
                // Pulsing glow behind logo
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(pulseScale)
                        .blur(40.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Primary.copy(alpha = 0.6f),
                                    Primary.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                
                // Logo background
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Primary,
                                    Color(0xFF6366F1)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = "Deal Hive Logo",
                        tint = White,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App name with gradient text effect simulation
            Text(
                text = "Deal Hive",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 42.sp,
                    letterSpacing = (-1).sp
                ),
                fontWeight = FontWeight.Black,
                color = textColor,
                modifier = Modifier
                    .alpha(alphaAnim)
                    .scale(scaleAnim)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Tagline with staggered animation
            Text(
                text = "Compare. Decide. Save.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    letterSpacing = 2.sp
                ),
                fontWeight = FontWeight.Medium,
                color = taglineTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha)
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Modern shimmer loading indicator
            Box(
                modifier = Modifier
                    .alpha(loaderAlpha)
                    .width(120.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(loaderBackground)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(4.dp)
                        .offset(x = (shimmerOffset * 100).dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Primary,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
        
        // Bottom version info
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(loaderAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Smart Shopping Assistant",
                    style = MaterialTheme.typography.bodySmall,
                    color = versionTextColor,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
