package com.simats.dealhive.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.data.api.RetrofitClient
import com.simats.dealhive.data.model.ForgotPasswordRequest
import com.simats.dealhive.data.model.ResetPasswordRequest
import com.simats.dealhive.data.model.VerifyOtpRequest
import com.simats.dealhive.ui.components.DealButton
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.components.DealInput
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import com.simats.dealhive.ui.theme.Success
import kotlinx.coroutines.launch
import retrofit2.HttpException

enum class ForgotPasswordStep {
    ENTER_EMAIL,
    ENTER_OTP,
    NEW_PASSWORD,
    SUCCESS
}

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var currentStep by remember { mutableStateOf(ForgotPasswordStep.ENTER_EMAIL) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val errorColor = MaterialTheme.colorScheme.error

    fun sendOtp() {
        if (email.isBlank()) {
            errorMessage = "Please enter your email"
            return
        }
        
        isLoading = true
        errorMessage = null
        
        coroutineScope.launch {
            try {
                val response = RetrofitClient.phpApiService.sendPasswordResetOtp(
                    ForgotPasswordRequest(email = email.trim())
                )
                
                if (response.success) {
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                    currentStep = ForgotPasswordStep.ENTER_OTP
                } else {
                    errorMessage = response.message
                }
            } catch (e: HttpException) {
                errorMessage = "No account found with this email"
            } catch (e: Exception) {
                errorMessage = "Connection failed. Please check your network."
            } finally {
                isLoading = false
            }
        }
    }
    
    fun verifyOtp() {
        if (otp.length != 6) {
            errorMessage = "Please enter the 6-digit OTP"
            return
        }
        
        isLoading = true
        errorMessage = null
        
        coroutineScope.launch {
            try {
                val response = RetrofitClient.phpApiService.verifyOtp(
                    VerifyOtpRequest(email = email.trim(), otp = otp)
                )
                
                if (response.success) {
                    Toast.makeText(context, "OTP verified!", Toast.LENGTH_SHORT).show()
                    currentStep = ForgotPasswordStep.NEW_PASSWORD
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "Invalid OTP code"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun resetPassword() {
        if (newPassword.length < 6) {
            errorMessage = "Password must be at least 6 characters"
            return
        }
        if (newPassword != confirmPassword) {
            errorMessage = "Passwords do not match"
            return
        }
        
        isLoading = true
        errorMessage = null
        
        coroutineScope.launch {
            try {
                val response = RetrofitClient.phpApiService.resetPassword(
                    ResetPasswordRequest(email = email.trim(), newPassword = newPassword)
                )
                
                Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                currentStep = ForgotPasswordStep.SUCCESS
            } catch (e: Exception) {
                errorMessage = "Failed to reset password"
            } finally {
                isLoading = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        DealHeader(
            title = "",
            showBack = currentStep != ForgotPasswordStep.SUCCESS,
            onBackClick = {
                when (currentStep) {
                    ForgotPasswordStep.ENTER_OTP -> currentStep = ForgotPasswordStep.ENTER_EMAIL
                    ForgotPasswordStep.NEW_PASSWORD -> currentStep = ForgotPasswordStep.ENTER_OTP
                    else -> onNavigateBack()
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl)
        ) {
            Spacer(modifier = Modifier.Companion.height(Spacing.lg))
            
            when (currentStep) {
                ForgotPasswordStep.ENTER_EMAIL -> {
                    // Step 1: Enter Email
                    Text(
                        text = "Forgot Password",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Enter your email to receive a verification code",
                        style = MaterialTheme.typography.bodyLarge,
                        color = textSecondaryColor,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                    
                    Spacer(modifier = Modifier.Companion.height(Spacing.xxl))
                    
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = errorColor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = Spacing.md)
                        )
                    }

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
                    
                    Spacer(modifier = Modifier.Companion.height(Spacing.lg))
                    
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Primary)
                        }
                    } else {
                        DealButton(
                            text = "Send OTP",
                            onClick = { sendOtp() },
                            enabled = email.isNotBlank()
                        )
                    }
                }
                
                ForgotPasswordStep.ENTER_OTP -> {
                    // Step 2: Enter OTP
                    Text(
                        text = "Verify Code",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Enter the 6-digit code sent to\n$email",
                        style = MaterialTheme.typography.bodyLarge,
                        color = textSecondaryColor,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                    
                    Spacer(modifier = Modifier.Companion.height(Spacing.xxl))
                    
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = errorColor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = Spacing.md)
                        )
                    }
                    
                    // OTP Input Boxes
                    OtpInputField(
                        otp = otp,
                        onOtpChange = { 
                            otp = it
                            errorMessage = null
                        }
                    )
                    
                    Spacer(modifier = Modifier.Companion.height(Spacing.xl))
                    
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Primary)
                        }
                    } else {
                        DealButton(
                            text = "Verify OTP",
                            onClick = { verifyOtp() },
                            enabled = otp.length == 6
                        )
                    }
                    
                    Spacer(modifier = Modifier.Companion.height(Spacing.lg))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Didn't receive code? ",
                            color = textSecondaryColor
                        )
                        Text(
                            text = "Resend",
                            color = Primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                
                ForgotPasswordStep.NEW_PASSWORD -> {
                    // Step 3: New Password
                    Text(
                        text = "Reset Password",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Create a new password for your account",
                        style = MaterialTheme.typography.bodyLarge,
                        color = textSecondaryColor,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                    
                    Spacer(modifier = Modifier.Companion.height(Spacing.xxl))
                    
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = errorColor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = Spacing.md)
                        )
                    }

                    DealInput(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            errorMessage = null
                        },
                        label = "New Password",
                        placeholder = "Enter new password",
                        isPassword = true
                    )

                    DealInput(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = "Confirm Password",
                        placeholder = "Confirm new password",
                        isPassword = true
                    )
                    
                    Spacer(modifier = Modifier.Companion.height(Spacing.lg))
                    
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Primary)
                        }
                    } else {
                        DealButton(
                            text = "Reset Password",
                            onClick = { resetPassword() },
                            enabled = newPassword.length >= 6 && confirmPassword.isNotBlank()
                        )
                    }
                }
                
                ForgotPasswordStep.SUCCESS -> {
                    // Step 4: Success
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.xxl),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Success,
                                modifier = Modifier.size(80.dp)
                            )
                            
                            Spacer(modifier = Modifier.Companion.height(Spacing.xl))
                            
                            Text(
                                text = "Password Reset!",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            
                            Spacer(modifier = Modifier.Companion.height(Spacing.md))
                            
                            Text(
                                text = "Your password has been reset successfully.\nYou can now login with your new password.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textSecondaryColor,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.Companion.height(Spacing.xxl))

                            DealButton(
                                text = "Back to Login",
                                onClick = onNavigateBack
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun OtpInputField(
    otp: String,
    onOtpChange: (String) -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    BasicTextField(
        value = otp,
        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) onOtpChange(it) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(6) { index ->
                    val char = otp.getOrNull(index)?.toString() ?: ""
                    val isFocused = index == otp.length
                    
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isFocused) Primary.copy(alpha = 0.2f) else surfaceColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (char.isNotEmpty()) textColor else textSecondaryColor,
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }
    )
}
