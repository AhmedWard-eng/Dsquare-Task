package com.dsquares.library.ui.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.paging.compose.collectAsLazyPagingItems
import com.dsquares.library.domain.usecase.IsUserLoggedInUseCase
import com.dsquares.library.ui.screens.coupons.CouponsScreen
import com.dsquares.library.ui.screens.login.LoginScreen
import com.dsquares.library.ui.screens.coupons.CouponsViewModel
import com.dsquares.library.ui.screens.login.LoginViewModel

@Composable
fun DsquareNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    val activity = LocalActivity.current

    NavHost(navController = navController, startDestination = "login", modifier = modifier) {
        composable("login") {
            val viewModel: LoginViewModel = viewModel()
            val loginState by viewModel.loginState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                val isUserLoggedIn = IsUserLoggedInUseCase()
                if (isUserLoggedIn()) {
                    navController.navigate("coupons") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }

            LoginScreen(
                loginState = loginState,
                onLoginClick = { phone -> viewModel.login(phone) },
                onCouponsClick = { navController.navigate("coupons"){
                    popUpTo("login") { inclusive = true }
                } },
                onLoginSuccess = {
                    navController.navigate("coupons") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onResetState = { viewModel.resetState() }
            )
        }
        composable("coupons") {
            val viewModel: CouponsViewModel = viewModel()
            val coupons = viewModel.coupons.collectAsLazyPagingItems()
            val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

            CouponsScreen(
                coupons = coupons,
                categories = emptyList(),
                searchQuery = searchQuery,
                onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                onBackClick = {
                    if (navController.previousBackStackEntry != null) {
                        navController.navigateUp()
                    } else {
                        activity?.finish()
                    }
                }
            )
        }
    }
}
