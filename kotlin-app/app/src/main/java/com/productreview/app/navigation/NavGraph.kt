package com.productreview.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.productreview.app.ui.screens.aiassistant.AIAssistantScreen
import com.productreview.app.ui.screens.notifications.NotificationDetailScreen
import com.productreview.app.ui.screens.notifications.NotificationsScreen
import com.productreview.app.ui.screens.productdetails.ProductDetailsScreen
import com.productreview.app.ui.screens.productlist.ProductListScreen
import com.productreview.app.ui.screens.wishlist.WishlistScreen

@Composable
fun ProductReviewNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.PRODUCT_LIST
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { 
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = { 
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = { 
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = { 
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        // Product List (Home)
        composable(Routes.PRODUCT_LIST) {
            ProductListScreen(
                onNavigateToProductDetails = { productId ->
                    navController.navigate(Routes.productDetails(productId))
                },
                onNavigateToNotifications = {
                    navController.navigate(Routes.NOTIFICATIONS)
                },
                onNavigateToWishlist = {
                    navController.navigate(Routes.WISHLIST)
                }
            )
        }

        // Product Details
        composable(
            route = Routes.PRODUCT_DETAILS,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailsScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAIAssistant = { id, name ->
                    navController.navigate(Routes.aiAssistant(id, name))
                }
            )
        }

        // Notifications
        composable(
            route = Routes.NOTIFICATIONS,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn()
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeOut()
            }
        ) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNotificationDetail = { notificationId ->
                    navController.navigate(Routes.notificationDetail(notificationId))
                },
                onNavigateToProductDetails = { productId ->
                    navController.navigate(Routes.productDetails(productId))
                }
            )
        }

        // Notification Detail
        composable(
            route = Routes.NOTIFICATION_DETAIL,
            arguments = listOf(
                navArgument("notificationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val notificationId = backStackEntry.arguments?.getString("notificationId") ?: ""
            NotificationDetailScreen(
                notificationId = notificationId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProductDetails = { productId ->
                    navController.navigate(Routes.productDetails(productId))
                }
            )
        }

        // Wishlist
        composable(Routes.WISHLIST) {
            WishlistScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProductDetails = { productId ->
                    navController.navigate(Routes.productDetails(productId))
                },
                onNavigateToProductList = {
                    navController.navigate(Routes.PRODUCT_LIST) {
                        popUpTo(Routes.PRODUCT_LIST) { inclusive = true }
                    }
                }
            )
        }

        // AI Assistant
        composable(
            route = Routes.AI_ASSISTANT,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType },
                navArgument("productName") { type = NavType.StringType }
            ),
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn()
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeOut()
            }
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val productName = backStackEntry.arguments?.getString("productName")?.replace("%2F", "/") ?: ""
            AIAssistantScreen(
                productId = productId,
                productName = productName,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
