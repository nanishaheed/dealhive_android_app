package com.simats.dealhive

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.simats.dealhive.data.repository.AuthRepository
import com.simats.dealhive.navigation.NavGraph
import com.simats.dealhive.navigation.Routes
import com.simats.dealhive.ui.theme.DealHiveTheme
import com.simats.dealhive.ui.theme.ThemeState
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener

// CompositionLocal for accessing isDarkMode throughout the app
val LocalIsDarkMode = compositionLocalOf { true }

class MainActivity : ComponentActivity(), PaymentResultListener {
    
    private var navController: NavHostController? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Razorpay
        Checkout.preload(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            val isDarkMode by ThemeState.rememberThemeState()
            
            DealHiveTheme(isDarkMode = isDarkMode) {
                CompositionLocalProvider(LocalIsDarkMode provides isDarkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val controller = rememberNavController()
                        navController = controller
                        NavGraph(navController = controller)
                    }
                }
            }
        }
    }
    
    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        // Payment successful - update subscription status
        val authRepository = AuthRepository.Companion.getInstance(this)
        authRepository.setSubscribed(true)
        
        Toast.makeText(this, "Payment Successful! Welcome to Premium!", Toast.LENGTH_LONG).show()
        
        // Navigate to Home
        navController?.navigate(Routes.Home.route) {
            popUpTo(Routes.Subscription.route) { inclusive = true }
        }
    }
    
    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment failed: $response", Toast.LENGTH_LONG).show()
    }
}
