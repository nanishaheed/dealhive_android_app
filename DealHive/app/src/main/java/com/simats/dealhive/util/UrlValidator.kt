package com.simats.dealhive.util

object UrlValidator {
    
    private val supportedDomains = listOf(
        "amazon.in",
        "amazon.com",
        "amzn.in",
        "amzn.to",
        "flipkart.com"
    )
    
    fun isValidCompareUrl(url: String): Boolean {
        if (url.isBlank()) return false
        return supportedDomains.any { domain -> 
            url.contains(domain, ignoreCase = true) 
        }
    }
    
    fun getSource(url: String): String {
        return when {
            url.contains("amazon", ignoreCase = true) || 
            url.contains("amzn", ignoreCase = true) -> "amazon"
            url.contains("flipkart", ignoreCase = true) -> "flipkart"
            else -> "unknown"
        }
    }
}
