package com.simats.dealhive.ui.screens.auth

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.data.repository.AuthRepository
import com.simats.dealhive.ui.components.DealButton
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.components.DealInput
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository.Companion.getInstance(context) }
    
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val errorColor = MaterialTheme.colorScheme.error
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    fun performLogin() {
        // Validate inputs
        if (email.isBlank()) {
            errorMessage = "Please enter your email"
            return
        }
        if (password.isBlank()) {
            errorMessage = "Please enter your password"
            return
        }
        
        errorMessage = null
        isLoading = true
        
        coroutineScope.launch {
            val result = authRepository.login(email.trim(), password)
            isLoading = false
            
            result.fold(
                onSuccess = { response ->
                    Toast.makeText(context, "Welcome back, ${response.user.name}!", Toast.LENGTH_SHORT).show()
                    onNavigateToHome()
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Login failed"
                }
            )
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        DealHeader()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl)
        ) {
            Spacer(modifier = Modifier.Companion.height(Spacing.lg))
            
            // Header Area
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Text(
                text = "Sign in to continue your deal hunting",
                style = MaterialTheme.typography.bodyLarge,
                color = textSecondaryColor,
                modifier = Modifier.padding(top = Spacing.xs)
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xxl))
            
            // Error Message
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = errorColor,
                    modifier = Modifier.padding(bottom = Spacing.md)
                )
            }
            
            // Form
            DealInput(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = "Email Address",
                placeholder = "name@example.com",
                keyboardType = KeyboardType.Email
            )

            DealInput(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = "Password",
                placeholder = "••••••••",
                isPassword = true
            )
            
            // Forgot Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    modifier = Modifier
                        .clickable { onNavigateToForgotPassword() }
                        .padding(bottom = Spacing.xl)
                )
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.md))
            
            // Login Button with Loading State
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Primary,
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    DealButton(
                        text = "Sign In",
                        onClick = { performLogin() },
                        enabled = email.isNotBlank() && password.isNotBlank()
                    )
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xxl))
            
            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondaryColor
                )
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

