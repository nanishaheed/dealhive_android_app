package com.simats.dealhive.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.theme.Spacing

/**
 * Screen that displays the privacy policy.
 */
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
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
            title = "Privacy Policy",
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
            
            Text(
                text = "Last Updated: January 1, 2026",
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondaryColor
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xl))
            
            PrivacySectionTitle("1. Information We Collect", textColor)
            PrivacySectionContent(
                "We collect information you provide directly to us, such as when you create an " +
                "account, use our services, or contact us for support. This may include:\n\n" +
                "• Name and email address\n" +
                "• Product comparison history\n" +
                "• Saved items and preferences\n" +
                "• Device information and usage data",
                textSecondaryColor
            )
            
            PrivacySectionTitle("2. How We Use Your Information", textColor)
            PrivacySectionContent(
                "We use the information we collect to:\n\n" +
                "• Provide, maintain, and improve our services\n" +
                "• Personalize your experience\n" +
                "• Send you technical notices and support messages\n" +
                "• Respond to your comments and questions\n" +
                "• Analyze usage patterns to improve our app",
                textSecondaryColor
            )
            
            PrivacySectionTitle("3. Information Sharing", textColor)
            PrivacySectionContent(
                "We do not sell, trade, or otherwise transfer your personal information to outside " +
                "parties. This does not include trusted third parties who assist us in operating our " +
                "app, conducting our business, or servicing you, so long as those parties agree to " +
                "keep this information confidential.",
                textSecondaryColor
            )
            
            PrivacySectionTitle("4. Data Security", textColor)
            PrivacySectionContent(
                "We implement a variety of security measures to maintain the safety of your personal " +
                "information. Your personal information is contained behind secured networks and is " +
                "only accessible by a limited number of persons who have special access rights.",
                textSecondaryColor
            )
            
            PrivacySectionTitle("5. Your Rights", textColor)
            PrivacySectionContent(
                "You have the right to:\n\n" +
                "• Access your personal data\n" +
                "• Correct inaccurate data\n" +
                "• Request deletion of your data\n" +
                "• Object to data processing\n" +
                "• Data portability",
                textSecondaryColor
            )
            
            PrivacySectionTitle("6. Contact Us", textColor)
            PrivacySectionContent(
                "If you have any questions about this Privacy Policy, please contact us at " +
                "privacy@dealhive.app",
                textSecondaryColor
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PrivacySectionTitle(title: String, textColor: Color) {
    Spacer(modifier = Modifier.Companion.height(Spacing.lg))
    Text(
        text = title,
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        color = textColor
    )
    Spacer(modifier = Modifier.Companion.height(Spacing.sm))
}

@Composable
private fun PrivacySectionContent(content: String, textColor: Color) {
    Text(
        text = content,
        style = MaterialTheme.typography.bodyLarge,
        color = textColor,
        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5f
    )
}
