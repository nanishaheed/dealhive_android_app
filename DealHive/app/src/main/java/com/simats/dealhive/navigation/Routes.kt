package com.simats.dealhive.navigation

sealed class Routes(val route: String) {
    // Auth
    data object Splash : Routes("splash")
    data object Login : Routes("login/{role}") {
        fun createRoute(role: String) = "login/$role"
    }
    data object Register : Routes("register/{role}") {
        fun createRoute(role: String) = "register/$role"
    }
    data object ForgotPassword : Routes("forgot_password")
    data object TermsOfService : Routes("terms")
    data object PrivacyPolicy : Routes("privacy")
    data object RoleSelection : Routes("role_selection")
    data object Subscription : Routes("subscription")
    
    // Main tabs
    data object Home : Routes("home")
    data object Categories : Routes("categories")
    data object Compare : Routes("compare")
    data object Profile : Routes("profile")
    
    // Product flows
    data object ProductList : Routes("product_list/{category}") {
        fun createRoute(category: String) = "product_list/$category"
    }
    data object ProductDetail : Routes("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    data object SearchResults : Routes("search/{query}") {
        fun createRoute(query: String) = "search/$query"
    }
    
    // Comparison flows
    data object ComparisonLoading : Routes("comparison_loading") {
        const val URL1_ARG = "url1"
        const val URL2_ARG = "url2"
    }
    data object SideBySideCompare : Routes("side_by_side")
    data object CustomizeWeights : Routes("customize_weights")
    data object CompareDbProducts : Routes("compare_db_products")
    data object PriceHistory : Routes("price_history")
    data object BestAlternative : Routes("best_alternative")
    
    // Profile flows
    data object EditProfile : Routes("edit_profile")
    data object Settings : Routes("settings")
    data object History : Routes("history")
    data object SavedItems : Routes("saved_items")
    
    // Admin flows
    data object AdminDashboard : Routes("admin_dashboard")
    data object AdminProducts : Routes("admin_products")
    data object AdminCategories : Routes("admin_categories")
    data object AdminUsers : Routes("admin_users")
    data object AddProduct : Routes("add_product")
    data object AddCategory : Routes("add_category")
    data object EditProduct : Routes("edit_product/{productId}") {
        fun createRoute(productId: Int) = "edit_product/$productId"
    }
    data object EditCategory : Routes("edit_category/{categoryId}") {
        fun createRoute(categoryId: Int) = "edit_category/$categoryId"
    }
}

