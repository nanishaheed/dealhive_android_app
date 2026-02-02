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

@Composable
fun TermsOfServiceScreen(
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
            title = "Terms of Service",
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
            
            SectionTitle("1. Acceptance of Terms", textColor)
            SectionContent(
                "By accessing and using Deal Hive, you agree to be bound by these Terms of Service " +
                "and all applicable laws and regulations. If you do not agree with any of these terms, " +
                "you are prohibited from using or accessing this application.",
                textSecondaryColor
            )
            
            SectionTitle("2. Use License", textColor)
            SectionContent(
                "Permission is granted to temporarily download one copy of Deal Hive for personal, " +
                "non-commercial transitory viewing only. This is the grant of a license, not a transfer " +
                "of title. Under this license you may not:\n\n" +
                "• Modify or copy the materials\n" +
                "• Use the materials for any commercial purpose\n" +
                "• Attempt to decompile or reverse engineer any software\n" +
                "• Remove any copyright or other proprietary notations\n" +
                "• Transfer the materials to another person",
                textSecondaryColor
            )
            
            SectionTitle("3. Product Information", textColor)
            SectionContent(
                "Deal Hive provides product comparison services by aggregating publicly available " +
                "information from various e-commerce platforms. We do not guarantee the accuracy, " +
                "completeness, or timeliness of product information, prices, or availability. " +
                "All product data is provided 'as is' and you should verify information before making purchase decisions.",
                textSecondaryColor
            )
            
            SectionTitle("4. Disclaimer", textColor)
            SectionContent(
                "The materials on Deal Hive are provided on an 'as is' basis. Deal Hive makes no " +
                "warranties, expressed or implied, and hereby disclaims and negates all other warranties " +
                "including, without limitation, implied warranties or conditions of merchantability, " +
                "fitness for a particular purpose, or non-infringement of intellectual property or other " +
                "violation of rights.",
                textSecondaryColor
            )
            
            SectionTitle("5. Limitations", textColor)
            SectionContent(
                "In no event shall Deal Hive or its suppliers be liable for any damages (including, " +
                "without limitation, damages for loss of data or profit, or due to business interruption) " +
                "arising out of the use or inability to use Deal Hive.",
                textSecondaryColor
            )
            
            SectionTitle("6. Contact Us", textColor)
            SectionContent(
                "If you have any questions about these Terms of Service, please contact us at " +
                "support@dealhive.app",
                textSecondaryColor
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String, textColor: Color) {
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
private fun SectionContent(content: String, textColor: Color) {
    Text(
        text = content,
        style = MaterialTheme.typography.bodyLarge,
        color = textColor,
        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5f
    )
}
