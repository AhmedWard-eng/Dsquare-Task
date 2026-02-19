package com.dsquares.library.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dsquares.library.ui.screens.CouponsScreen
import com.dsquares.library.ui.screens.LoginScreen

@Composable
fun DsquareNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = "login", modifier = modifier) {
        composable("login") {
            LoginScreen(
                onCouponsClick = { navController.navigate("coupons") }
            )
        }
        composable("coupons") {
            CouponsScreen(
                coupons = emptyList(),
                categories = emptyList(),
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
