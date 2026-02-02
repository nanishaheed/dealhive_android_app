package com.simats.dealhive.ui.screens.auth

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.billingclient.api.*
import com.simats.dealhive.data.repository.AuthRepository
import com.simats.dealhive.ui.theme.Primary
import kotlinx.coroutines.launch

@Composable
fun SubscriptionScreen(
    onSubscriptionSuccess: () -> Unit,
    onSkip: () -> Unit = {}
) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository.Companion.getInstance(context) }
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var productDetails by remember { mutableStateOf<ProductDetails?>(null) }

    // We use a state wrapper to avoid "Unresolved reference" errors inside the listener
    val billingClientState = remember { mutableStateOf<BillingClient?>(null) }

    val billingClient = remember {
        BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                            scope.launch {
                                val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken)
                                    .build()

                                // Safely access the client from the state wrapper
                                val client = billingClientState.value
                                val result = client?.acknowledgePurchase(acknowledgeParams)

                                if (result?.responseCode == BillingClient.BillingResponseCode.OK) {
                                    authRepository.setSubscribed(true)
                                    onSubscriptionSuccess()
                                }
                            }
                        }
                    }
                }
            }
            .enablePendingPurchases()
            .build()
    }

    // Initialize the state wrapper immediately
    billingClientState.value = billingClient

    // Connect and Query SKU
    LaunchedEffect(Unit) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                        .setProductList(listOf(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId("dealhive_premium_subscription")
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build()
                        )).build()

                    billingClient.queryProductDetailsAsync(queryProductDetailsParams) { result, list ->
                        if (result.responseCode == BillingClient.BillingResponseCode.OK && list.isNotEmpty()) {
                            productDetails = list[0]
                        }
                        isLoading = false
                    }
                } else {
                    isLoading = false
                    Toast.makeText(context, "Billing Setup Failed", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onBillingServiceDisconnected() {
                isLoading = false
            }
        })
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground

    fun startPayment() {
        val activity = context as? Activity
        val details = productDetails
        val offerToken = details?.subscriptionOfferDetails?.getOrNull(0)?.offerToken

        if (activity != null && details != null && offerToken != null) {
            val params = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(offerToken)
                        .build()
                )).build()
            billingClient.launchBillingFlow(activity, params)
        } else {
            Toast.makeText(context, "Subscription plan not ready", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Premium Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Primary, Color(0xFFFF8F5C))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PREMIUM",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = "Unlock Full Access",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Get unlimited access to all features and find the best deals!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Subscription Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Monthly Plan",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Try to show dynamic price, fallback to ₹99 if loading or failed
                    val priceText = productDetails?.subscriptionOfferDetails?.getOrNull(0)
                        ?.pricingPhases?.pricingPhaseList?.getOrNull(0)?.formattedPrice ?: "₹99"

                    Text(
                        text = priceText,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Text(
                        text = "/month",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Features list
                val features = listOf(
                    "Unlimited product comparisons",
                    "Price history & alerts",
                    "Best deal recommendations",
                    "Ad-free experience",
                    "Priority support"
                )

                features.forEach { feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Subscribe Button
        Button(
            onClick = { startPayment() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Subscribe Now",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Skip button (for testing)
        TextButton(onClick = {
            // For student project - allow skip for testing
            authRepository.setSubscribed(true)
            onSubscriptionSuccess()
        }) {
            Text(
                text = "Skip for now",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Cancel anytime. Secure payment via Google Play.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}