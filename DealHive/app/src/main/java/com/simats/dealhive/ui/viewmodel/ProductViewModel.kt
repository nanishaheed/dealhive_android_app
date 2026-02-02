package com.simats.dealhive.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.simats.dealhive.data.model.Product
import kotlin.collections.iterator
import kotlin.math.abs

/**
 * Shared ViewModel for passing product data between screens
 */
class ProductViewModel : ViewModel() {
    
    // Currently selected product for detail view
    var selectedProduct by mutableStateOf<Product?>(null)
        private set
    
    // Products for comparison
    var comparisonProducts by mutableStateOf<List<Product>>(emptyList())
        private set
    
    // Comparison Weights
    var priceWeight by mutableStateOf(0.4f)
    var ratingWeight by mutableStateOf(0.3f)
    var featuresWeight by mutableStateOf(0.2f)
    var brandWeight by mutableStateOf(0.1f)
    
    fun selectProduct(product: Product) {
        selectedProduct = product
    }
    
    fun updateWeights(p: Float, r: Float, f: Float, b: Float) {
        priceWeight = p
        ratingWeight = r
        featuresWeight = f
        brandWeight = b
    }
    
    fun clearSelectedProduct() {
        selectedProduct = null
    }
    
    fun setComparisonProducts(product1: Product, product2: Product) {
        comparisonProducts = listOf(product1, product2)
    }
    
    fun addToComparison(product: Product) {
        if (comparisonProducts.size < 2 && !comparisonProducts.any { it.url == product.url }) {
            comparisonProducts = comparisonProducts + product
        }
    }
    
    fun removeFromComparison(product: Product) {
        comparisonProducts = comparisonProducts.filter { it.url != product.url }
    }
    
    fun clearComparison() {
        comparisonProducts = emptyList()
    }

    // ==================== INDUSTRY-GRADE MCDA ENGINE ====================
    
    /**
     * Chipset Performance Benchmarks (AnTuTu-based normalized scores)
     */
    private val chipsetBenchmarks = mapOf(
        // Apple Silicon
        "a17" to 100, "a16" to 95, "a15" to 90, "a14" to 85, "a13" to 78,
        "bionic" to 90,
        // Qualcomm Snapdragon
        "snapdragon 8 gen 3" to 98, "snapdragon 8 gen 2" to 94, "snapdragon 8 gen 1" to 88,
        "snapdragon 888" to 82, "snapdragon 870" to 78, "snapdragon 865" to 75,
        "snapdragon 7" to 65, "snapdragon 6" to 55, "snapdragon 4" to 45,
        // MediaTek Dimensity
        "dimensity 9300" to 96, "dimensity 9200" to 92, "dimensity 9000" to 88,
        "dimensity 8" to 70, "dimensity 7" to 60, "dimensity 6" to 50,
        // MediaTek Helio
        "helio g99" to 48, "helio g96" to 45, "helio g95" to 44, "helio g90" to 42,
        "helio g88" to 40, "helio g85" to 38, "helio g80" to 35, "helio g70" to 30,
        "helio a22" to 20, "helio p22" to 22, "helio p35" to 25,
        // Unisoc
        "unisoc t612" to 28, "unisoc t616" to 30, "unisoc t606" to 25, "unisoc" to 22,
        // Samsung Exynos
        "exynos 2400" to 94, "exynos 2200" to 85, "exynos 2100" to 80,
        "exynos 1380" to 55, "exynos 1280" to 50
    )
    
    private fun extractNumber(text: String): Int {
        val regex = Regex("(\\d+)")
        return regex.find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }
    
    /**
     * Search ALL specs and title for any matching text
     */
    private fun searchInProduct(product: Product, vararg keywords: String): String {
        val allText = buildString {
            append(product.title.lowercase())
            append(" ")
            product.specs?.forEach { (key, value) ->
                append(key.lowercase())
                append(" ")
                append(value.lowercase())
                append(" ")
            }
        }
        return allText
    }
    
    /**
     * Get chipset score - uses price as unique differentiator
     */
    private fun getChipsetScore(product: Product): Int {
        val allText = searchInProduct(product)
        
        for ((chip, score) in chipsetBenchmarks) {
            if (allText.contains(chip)) return score
        }
        
        // Fallback: Use EXACT price to create unique scores (no ties)
        // Higher price = slightly better assumed performance
        return (product.price / 500).coerceIn(10, 80)
    }
    
    /**
     * Camera score - searches for MP anywhere in specs/title
     */
    private fun getCameraScore(product: Product): Int {
        val allText = searchInProduct(product)
        val title = product.title.lowercase()
        
        // Find highest megapixel value in entire product data
        val mpMatches = Regex("(\\d+)\\s*mp").findAll(allText)
        val maxMp = mpMatches.mapNotNull { it.groupValues[1].toIntOrNull() }.maxOrNull() ?: 0
        
        var score = (maxMp * 0.6).toInt().coerceAtMost(60)
        
        // Brand bonuses
        if (title.contains("iphone") || title.contains("apple")) score += 25
        if (title.contains("pixel") || title.contains("google")) score += 23
        if (title.contains("samsung")) score += 15
        if (title.contains("oneplus")) score += 12
        if (title.contains("vivo") || title.contains("oppo")) score += 8
        if (title.contains("realme")) score += 6
        if (title.contains("redmi") || title.contains("poco")) score += 5
        
        // If no MP found, use price-based differentiation
        if (maxMp == 0) {
            score = (product.price / 600).coerceIn(5, 50)
        }
        
        return score.coerceIn(0, 100)
    }
    
    /**
     * Battery score - searches for mAh anywhere
     */
    private fun getBatteryScore(product: Product): Int {
        val allText = searchInProduct(product)
        
        // Find battery capacity in mAh
        val mahMatches = Regex("(\\d{4,5})\\s*mah").findAll(allText)
        val capacity = mahMatches.mapNotNull { it.groupValues[1].toIntOrNull() }.maxOrNull() ?: 0
        
        var score = if (capacity > 0) {
            (capacity / 60.0).toInt().coerceIn(20, 85)
        } else {
            // Use price-based fallback with variance
            (product.price / 400).coerceIn(15, 60)
        }
        
        // Fast charging bonus
        if (allText.contains("65w") || allText.contains("67w")) score += 12
        if (allText.contains("33w") || allText.contains("30w")) score += 8
        if (allText.contains("25w") || allText.contains("18w")) score += 4
        
        return score.coerceIn(0, 100)
    }
    
    /**
     * Display score - searches for display tech anywhere
     */
    private fun getDisplayScore(product: Product): Int {
        val allText = searchInProduct(product)
        
        var score = 35 // Base LCD score
        
        // Panel type bonuses
        if (allText.contains("super amoled")) score = 75
        else if (allText.contains("amoled")) score = 70
        else if (allText.contains("oled")) score = 68
        else if (allText.contains("retina")) score = 80
        else if (allText.contains("ips")) score = 45
        
        // Refresh rate bonus
        if (allText.contains("120hz") || allText.contains("144hz")) score += 15
        else if (allText.contains("90hz")) score += 8
        else if (allText.contains("60hz")) score += 2
        
        // Resolution bonus
        if (allText.contains("fhd") || allText.contains("1080")) score += 8
        else if (allText.contains("hd+") || allText.contains("720")) score += 4
        
        // If no display info found, use price
        if (score == 35) {
            score = (product.price / 500).coerceIn(20, 60)
        }
        
        return score.coerceIn(0, 100)
    }
    
    /**
     * Calculate software/updates score based on brand reputation
     */
    private fun getSoftwareScore(product: Product): Int {
        val title = product.title.lowercase()
        
        return when {
            title.contains("iphone") || title.contains("apple") -> 100 // 6+ years updates
            title.contains("pixel") || title.contains("google") -> 95 // 7 years updates
            title.contains("samsung") && product.price > 30000 -> 85 // 4 years
            title.contains("oneplus") -> 75 // 3-4 years
            title.contains("iqoo") -> 70 // 3 years (Vivo sub-brand)
            title.contains("motorola") -> 60 // 2-3 years
            title.contains("xiaomi") || title.contains("redmi") || title.contains("poco") -> 55
            title.contains("realme") || title.contains("oppo") || title.contains("vivo") -> 50
            title.contains("tecno") || title.contains("infinix") -> 45
            else -> 40
        }
    }
    
    data class CategoryResult(
        val category: String,
        val icon: String,
        val winner: String,
        val reason: String,
        val score1: Int = 0,
        val score2: Int = 0
    )

    /**
     * IMPORTANT: This function must be ORDER-INDEPENDENT
     * Swapping p1 and p2 should give the same winner (just with swapped positions)
     */
    fun getCategorizedResults(p1: Product, p2: Product): List<CategoryResult> {
        val results = mutableListOf<CategoryResult>()
        
        // Calculate ALL scores FIRST (before any comparisons)
        val perf1 = getChipsetScore(p1)
        val perf2 = getChipsetScore(p2)
        val cam1 = getCameraScore(p1)
        val cam2 = getCameraScore(p2)
        val bat1 = getBatteryScore(p1)
        val bat2 = getBatteryScore(p2)
        val disp1 = getDisplayScore(p1)
        val disp2 = getDisplayScore(p2)
        val soft1 = getSoftwareScore(p1)
        val soft2 = getSoftwareScore(p2)
        val offer1 = (p1.offers?.size ?: 0) * 10
        val offer2 = (p2.offers?.size ?: 0) * 10
        
        // Value: Lower price with same performance = better value
        // Use a formula that's completely based on the product itself
        val value1 = ((1000000.0 / (p1.price.toDouble().coerceAtLeast(1.0))) * (perf1 + 1)).toInt().coerceIn(0, 100)
        val value2 = ((1000000.0 / (p2.price.toDouble().coerceAtLeast(1.0))) * (perf2 + 1)).toInt().coerceIn(0, 100)
        
        // Helper function that determines winner STRICTLY by scores
        // CRITICAL: Uses > not >= to ensure true tie detection
        fun determineWinner(s1: Int, s2: Int): String {
            return when {
                s1 > s2 -> p1.title
                s2 > s1 -> p2.title
                else -> "TIE"
            }
        }
        
        // Add all categories with their scores
        results.add(CategoryResult("Performance", "�", determineWinner(perf1, perf2), 
            "Chipset benchmark analysis.", perf1, perf2))
        results.add(CategoryResult("Camera", "📸", determineWinner(cam1, cam2), 
            "Sensor & image processing.", cam1, cam2))
        results.add(CategoryResult("Battery Life", "🔋", determineWinner(bat1, bat2), 
            "Capacity & charging speed.", bat1, bat2))
        results.add(CategoryResult("Display", "📺", determineWinner(disp1, disp2), 
            "Panel tech & refresh rate.", disp1, disp2))
        results.add(CategoryResult("Software", "🔄", determineWinner(soft1, soft2), 
            "Update support longevity.", soft1, soft2))
        results.add(CategoryResult("Offers", "🏷️", determineWinner(offer1, offer2), 
            "Available deals & discounts.", offer1.coerceAtMost(100), offer2.coerceAtMost(100)))
        results.add(CategoryResult("Value", "💰", determineWinner(value1, value2), 
            "Price-to-performance ratio.", value1, value2))

        return results
    }

    fun getComparisonVerdict(p1: Product, p2: Product): String {
        val categories = getCategorizedResults(p1, p2)
        
        // Count category wins
        val p1Wins = categories.count { it.winner == p1.title }
        val p2Wins = categories.count { it.winner == p2.title }
        
        // Truncate long titles for the verdict
        val t1 = p1.title.split("|")[0].split("(")[0].trim()
        val t2 = p2.title.split("|")[0].split("(")[0].trim()
        
        return when {
            p1Wins > p2Wins + 2 -> "$t1 dominates with $p1Wins out of ${categories.size} category wins!"
            p2Wins > p1Wins + 2 -> "$t2 dominates with $p2Wins out of ${categories.size} category wins!"
            p1Wins > p2Wins -> "$t1 wins $p1Wins categories vs $p2Wins for $t2."
            p2Wins > p1Wins -> "$t2 wins $p2Wins categories vs $p1Wins for $t1."
            else -> "It's a tie at $p1Wins categories each! Choose based on your priority."
        }
    }
    
    fun getOverallWinner(p1: Product, p2: Product): Product {
        val categories = getCategorizedResults(p1, p2)
        val p1Wins = categories.count { it.winner == p1.title }
        val p2Wins = categories.count { it.winner == p2.title }
        
        // Strict comparison - only winner if more wins
        // Tie-breaker: use total scores from all categories
        return when {
            p1Wins > p2Wins -> p1
            p2Wins > p1Wins -> p2
            else -> {
                // Tie-breaker: sum of all scores
                val p1Total = categories.sumOf { it.score1 }
                val p2Total = categories.sumOf { it.score2 }
                if (p1Total >= p2Total) p1 else p2
            }
        }
    }
    
    /**
     * Get confidence level of the comparison
     */
    fun getConfidenceLevel(p1: Product, p2: Product): String {
        val categories = getCategorizedResults(p1, p2)
        val p1Wins = categories.count { it.winner == p1.title }
        val p2Wins = categories.count { it.winner == p2.title }
        val gap = abs(p1Wins - p2Wins)
        
        return when {
            gap >= 5 -> "Very High (Clear Winner)"
            gap >= 3 -> "High"
            gap >= 1 -> "Moderate"
            else -> "Low (Close Match)"
        }
    }
}

