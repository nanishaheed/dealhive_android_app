package com.simats.dealhive.ui.screens.auth

import android.util.Patterns
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSignIn: () -> Unit = onNavigateBack, // For Sign In link - goes to actual login page
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToHome: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository.Companion.getInstance(context) }
    
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val errorColor = MaterialTheme.colorScheme.error
    
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Validation helper functions
    fun isValidName(name: String): Boolean {
        return name.all { it.isLetter() || it.isWhitespace() }
    }
    
    fun isValidPhone(phone: String): Boolean {
        return phone.length == 10 && phone.all { it.isDigit() }
    }
    
    fun performRegistration() {
        // Validate inputs
        if (fullName.isBlank()) {
            errorMessage = "Please enter your full name"
            return
        }
        if (!isValidName(fullName)) {
            errorMessage = "Name must contain only letters (no numbers or special characters)"
            return
        }
        if (email.isBlank()) {
            errorMessage = "Please enter your email"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage = "Please enter a valid email address"
            return
        }
        if (phone.isNotBlank() && !isValidPhone(phone)) {
            errorMessage = "Mobile number must be exactly 10 digits"
            return
        }
        // Strong password validation
        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters"
            return
        }
        if (!password.any { it.isUpperCase() }) {
            errorMessage = "Password must contain at least one uppercase letter"
            return
        }
        if (!password.any { it.isLowerCase() }) {
            errorMessage = "Password must contain at least one lowercase letter"
            return
        }
        if (!password.any { it.isDigit() }) {
            errorMessage = "Password must contain at least one number"
            return
        }
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match"
            return
        }
        if (!agreedToTerms) {
            errorMessage = "Please agree to the Terms and Privacy Policy"
            return
        }
        
        errorMessage = null
        isLoading = true
        
        coroutineScope.launch {
            val result = authRepository.register(
                name = fullName.trim(),
                email = email.trim(),
                password = password,
                phone = phone.trim().ifBlank { null }
            )
            isLoading = false
            
            result.fold(
                onSuccess = { response ->
                    Toast.makeText(context, "Account created! Welcome, ${response.user.name}!", Toast.LENGTH_SHORT).show()
                    // Navigate to subscription screen after successful registration
                    onNavigateToLogin()
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Registration failed"
                }
            )
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        DealHeader(
            title = "",
            showBack = true,
            onBackClick = onNavigateBack
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl)
        ) {
            Spacer(modifier = Modifier.Companion.height(Spacing.lg))
            
            // Header Area
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Text(
                text = "Start comparing and saving on deals",
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
                value = fullName,
                onValueChange = { newValue ->
                    // Only allow letters and spaces
                    if (newValue.all { it.isLetter() || it.isWhitespace() }) {
                        fullName = newValue
                        errorMessage = null
                    }
                },
                label = "Full Name",
                placeholder = "John Doe"
            )

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
                value = phone,
                onValueChange = { newValue ->
                    // Only allow digits and max 10 characters
                    if (newValue.all { it.isDigit() } && newValue.length <= 10) {
                        phone = newValue
                        errorMessage = null
                    }
                },
                label = "Mobile Number",
                placeholder = "9876543210",
                keyboardType = KeyboardType.Phone
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

            DealInput(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = "Confirm Password",
                placeholder = "••••••••",
                isPassword = true
            )
            
            // Terms Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = Spacing.md)
            ) {
                Checkbox(
                    checked = agreedToTerms,
                    onCheckedChange = { 
                        agreedToTerms = it
                        errorMessage = null
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Primary,
                        uncheckedColor = textSecondaryColor
                    )
                )
                Text(
                    text = "I agree to the ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondaryColor
                )
                Text(
                    text = "Terms",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    modifier = Modifier.clickable { onNavigateToTerms() }
                )
                Text(
                    text = " and ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondaryColor
                )
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    modifier = Modifier.clickable { onNavigateToPrivacy() }
                )
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.md))
            
            // Register Button with Loading State
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
                        text = "Create Account",
                        onClick = { performRegistration() },
                        enabled = agreedToTerms && fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
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
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondaryColor
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    modifier = Modifier.clickable { onNavigateToSignIn() }
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

