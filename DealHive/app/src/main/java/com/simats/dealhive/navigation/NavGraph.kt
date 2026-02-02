package com.simats.dealhive.navigation

import android.util.Base64
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.simats.dealhive.data.api.RetrofitClient
import com.simats.dealhive.data.repository.AuthRepository
import com.simats.dealhive.ui.screens.admin.AddCategoryScreen
import com.simats.dealhive.ui.screens.admin.AddProductScreen
import com.simats.dealhive.ui.screens.admin.AdminCategoriesScreen
import com.simats.dealhive.ui.screens.admin.AdminDashboardScreen
import com.simats.dealhive.ui.screens.admin.AdminProductsScreen
import com.simats.dealhive.ui.screens.admin.AdminUsersScreen
import com.simats.dealhive.ui.screens.admin.EditCategoryScreen
import com.simats.dealhive.ui.screens.admin.EditProductScreen
import com.simats.dealhive.ui.screens.auth.ForgotPasswordScreen
import com.simats.dealhive.ui.screens.auth.LoginScreen
import com.simats.dealhive.ui.screens.auth.PrivacyPolicyScreen
import com.simats.dealhive.ui.screens.auth.RegisterScreen
import com.simats.dealhive.ui.screens.auth.RoleSelectionScreen
import com.simats.dealhive.ui.screens.auth.SplashScreen
import com.simats.dealhive.ui.screens.auth.SubscriptionScreen
import com.simats.dealhive.ui.screens.auth.TermsOfServiceScreen
import com.simats.dealhive.ui.screens.compare.BestAlternativeScreen
import com.simats.dealhive.ui.screens.compare.CompareDbProductsScreen
import com.simats.dealhive.ui.screens.compare.ComparisonLoadingScreen
import com.simats.dealhive.ui.screens.compare.CustomizeWeightsScreen
import com.simats.dealhive.ui.screens.compare.PasteLinksScreen
import com.simats.dealhive.ui.screens.compare.PriceHistoryScreen
import com.simats.dealhive.ui.screens.compare.SideBySideCompareScreen
import com.simats.dealhive.ui.screens.home.CategoriesScreen
import com.simats.dealhive.ui.screens.home.HomeFeedScreen
import com.simats.dealhive.ui.screens.home.ProductDetailScreen
import com.simats.dealhive.ui.screens.home.ProductListScreen
import com.simats.dealhive.ui.screens.home.SearchResultsScreen
import com.simats.dealhive.ui.screens.profile.EditProfileScreen
import com.simats.dealhive.ui.screens.profile.HistoryScreen
import com.simats.dealhive.ui.screens.profile.ProfileScreen
import com.simats.dealhive.ui.screens.profile.SavedItemsScreen
import com.simats.dealhive.ui.screens.profile.SettingsScreen
import com.simats.dealhive.ui.viewmodel.ProductViewModel

// Helper functions for URL-safe Base64 encoding
private fun encodeUrl(url: String): String {
    return Base64.encodeToString(url.toByteArray(Charsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP)
}

private fun decodeUrl(encoded: String): String {
    return try {
        String(Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_WRAP), Charsets.UTF_8)
    } catch (e: Exception) {
        ""
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Routes.Splash.route
) {
    // Shared ViewModel for passing product data
    val productViewModel: ProductViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screens
        composable(Routes.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    // Go to role selection first
                    navController.navigate(Routes.RoleSelection.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    // User is already logged in and subscribed, go directly to Home
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToAdminDashboard = {
                    // Admin is already logged in, go directly to Admin Dashboard
                    navController.navigate(Routes.AdminDashboard.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToSubscription = {
                    // User is logged in but not subscribed
                    navController.navigate(Routes.Subscription.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Routes.Login.route,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "user"
            val context = LocalContext.current
            val authRepository = remember { AuthRepository.Companion.getInstance(context) }

            LoginScreen(
                onNavigateToHome = {
                    // Navigate based on role
                    if (role == "admin") {
                        navController.navigate(Routes.AdminDashboard.route) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                    } else {
                        // Check subscription for regular users
                        if (authRepository.isSubscribed()) {
                            navController.navigate(Routes.Home.route) {
                                popUpTo(Routes.Login.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Routes.Subscription.route) {
                                popUpTo(Routes.Login.route) { inclusive = true }
                            }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.Register.createRoute(role))
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.ForgotPassword.route)
                }
            )
        }
        
        composable(Routes.RoleSelection.route) {
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    // Navigate to login, passing the role
                    navController.navigate("login/$role") {
                        popUpTo(Routes.RoleSelection.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Routes.Register.route,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "user"
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    // After registration, go to subscription for regular users
                    if (role == "admin") {
                        navController.navigate(Routes.AdminDashboard.route) {
                            popUpTo(Routes.Register.route) { inclusive = true }
                        }
                    } else {
                        // New users go to subscription screen
                        navController.navigate(Routes.Subscription.route) {
                            popUpTo(Routes.Register.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToSignIn = {
                    // Navigate to actual login screen
                    navController.navigate("login/$role") {
                        popUpTo(Routes.Register.route) { inclusive = true }
                    }
                },
                onNavigateToTerms = { navController.navigate(Routes.TermsOfService.route) },
                onNavigateToPrivacy = { navController.navigate(Routes.PrivacyPolicy.route) }
            )
        }
        
        composable(Routes.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Routes.TermsOfService.route) {
            TermsOfServiceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Routes.PrivacyPolicy.route) {
            PrivacyPolicyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Subscription Screen
        composable(Routes.Subscription.route) {
            val context = LocalContext.current
            val authRepository = remember { AuthRepository.Companion.getInstance(context) }

            SubscriptionScreen(
                onSubscriptionSuccess = {
                    authRepository.setSubscribed(true)
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Subscription.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Main Screens
        composable(Routes.Home.route) {
            HomeFeedScreen(
                onNavigateToProfile = { navController.navigate(Routes.Profile.route) },
                onNavigateToCompare = { navController.navigate(Routes.Compare.route) },
                onNavigateToCategories = { navController.navigate(Routes.Categories.route) },
                onNavigateToHistory = { navController.navigate(Routes.History.route) },
                onNavigateToSavedItems = { navController.navigate(Routes.SavedItems.route) },
                onNavigateToProduct = { productId ->
                    productViewModel.clearSelectedProduct() // Clear previous product
                    navController.navigate(Routes.ProductDetail.createRoute(productId))
                },
                onNavigateToCompareDb = { navController.navigate(Routes.CompareDbProducts.route) }
            )
        }
        
        composable(Routes.Categories.route) {
            CategoriesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProductList = { category ->
                    navController.navigate(Routes.ProductList.createRoute(category))
                }
            )
        }
        
        composable(Routes.Compare.route) {
            PasteLinksScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLoading = { url1, url2 ->
                    val encodedUrl1 = encodeUrl(url1)
                    val encodedUrl2 = encodeUrl(url2)
                    navController.navigate("comparison_loading/$encodedUrl1/$encodedUrl2")
                }
            )
        }
        
        composable(Routes.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditProfile = { navController.navigate(Routes.EditProfile.route) },
                onNavigateToHistory = { navController.navigate(Routes.History.route) },
                onNavigateToSavedItems = { navController.navigate(Routes.SavedItems.route) },
                onNavigateToSettings = { navController.navigate(Routes.Settings.route) },
                onNavigateToAdmin = { navController.navigate(Routes.AdminDashboard.route) },
                onLogout = {
                    navController.navigate(Routes.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onDeleteAccount = {
                    navController.navigate(Routes.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Product Screens
        composable(
            route = Routes.ProductList.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            ProductListScreen(
                category = category,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { product ->
                    // Store product in ViewModel and navigate
                    productViewModel.selectProduct(product)
                    navController.navigate(Routes.ProductDetail.createRoute(product.id))
                }
            )
        }
        
        composable(
            route = Routes.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            
            // Get product from ViewModel or fetch from database
            val product = productViewModel.selectedProduct
            
            // If product is null, we need to fetch it from the database
            val productState = remember { mutableStateOf(product) }
            
            if (product == null && productId.isNotEmpty()) {
                LaunchedEffect(productId) {
                    try {
                        val fetchedProduct = RetrofitClient.phpApiService.getProductById(productId.toIntOrNull() ?: 0)
                        productState.value = fetchedProduct
                    } catch (e: Exception) {
                        Log.e("ProductDetail", "Failed to load product", e)
                    }
                }
            }

            ProductDetailScreen(
                product = productState.value,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Routes.SearchResults.route,
            arguments = listOf(navArgument("query") { type = NavType.StringType })
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            SearchResultsScreen(
                query = query,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { productId ->
                    navController.navigate(Routes.ProductDetail.createRoute(productId))
                }
            )
        }
        
        // Compare Screens - Using path parameters with Base64 encoding
        composable(
            route = "comparison_loading/{url1}/{url2}",
            arguments = listOf(
                navArgument("url1") { type = NavType.StringType },
                navArgument("url2") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val url1 = decodeUrl(backStackEntry.arguments?.getString("url1") ?: "")
            val url2 = decodeUrl(backStackEntry.arguments?.getString("url2") ?: "")
            ComparisonLoadingScreen(
                url1 = url1,
                url2 = url2,
                productViewModel = productViewModel,
                onNavigateToComparison = {
                    navController.navigate(Routes.SideBySideCompare.route) {
                        popUpTo("comparison_loading/{url1}/{url2}") { inclusive = true }
                    }
                },
                onError = { navController.popBackStack() }
            )
        }
        
        composable(Routes.SideBySideCompare.route) {
            SideBySideCompareScreen(
                productViewModel = productViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Routes.CustomizeWeights.route) {
            CustomizeWeightsScreen(
                productViewModel = productViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Routes.CompareDbProducts.route) {
            CompareDbProductsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCompare = { product1, product2 ->
                    // Use the product URLs to scrape fresh data
                    val encodedUrl1 = encodeUrl(product1.url)
                    val encodedUrl2 = encodeUrl(product2.url)
                    navController.navigate("comparison_loading/$encodedUrl1/$encodedUrl2")
                }
            )
        }
        
        composable(Routes.PriceHistory.route) {
            PriceHistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Routes.BestAlternative.route) {
            BestAlternativeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Profile Screens
        composable(Routes.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Routes.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTerms = { navController.navigate(Routes.TermsOfService.route) },
                onNavigateToPrivacy = { navController.navigate(Routes.PrivacyPolicy.route) },
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToComparison = { /* Navigate to comparison detail */ }
            )
        }
        
        composable(Routes.SavedItems.route) {
            SavedItemsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { productId ->
                    navController.navigate(Routes.ProductDetail.createRoute(productId))
                }
            )
        }
        
        // Admin Screens
        composable(Routes.AdminDashboard.route) {
            val context = LocalContext.current
            val authRepository = remember { AuthRepository.Companion.getInstance(context) }

            AdminDashboardScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProducts = { navController.navigate(Routes.AdminProducts.route) },
                onNavigateToCategories = { navController.navigate(Routes.AdminCategories.route) },
                onNavigateToUsers = { navController.navigate(Routes.AdminUsers.route) },
                onLogout = {
                    authRepository.logout()
                    navController.navigate(Routes.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.AdminProducts.route) {
            AdminProductsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddProduct = { navController.navigate(Routes.AddProduct.route) },
                onNavigateToEditProduct = { productId ->
                    navController.navigate(
                        Routes.EditProduct.createRoute(
                            productId
                        )
                    )
                }
            )
        }
        
        composable(Routes.AdminCategories.route) {
            AdminCategoriesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCategory = { navController.navigate(Routes.AddCategory.route) },
                onNavigateToEditCategory = { categoryId ->
                    navController.navigate(
                        Routes.EditCategory.createRoute(
                            categoryId
                        )
                    )
                }
            )
        }
        
        composable(Routes.AdminUsers.route) {
            AdminUsersScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Routes.AddProduct.route) {
            AddProductScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Routes.AddCategory.route) {
            AddCategoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Routes.EditProduct.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            EditProductScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Routes.EditCategory.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 0
            EditCategoryScreen(
                categoryId = categoryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}


