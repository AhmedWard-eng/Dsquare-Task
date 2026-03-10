package com.dsquares.library.ui.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.paging.compose.collectAsLazyPagingItems
import com.dsquares.library.DSquareSDK
import com.dsquares.library.di.CouponsContainer
import com.dsquares.library.di.LoginContainer
import com.dsquares.library.ui.screens.coupons.CouponsScreen
import com.dsquares.library.ui.screens.login.LoginScreen
import com.dsquares.library.ui.screens.coupons.CouponsViewModel
import com.dsquares.library.ui.screens.login.LoginViewModel

@Composable
fun DsquareNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    val activity = LocalActivity.current

    NavHost(navController = navController, startDestination = "login", modifier = modifier) {
        composable("login") {
            val container = remember { LoginContainer(DSquareSDK.appContainer) }
            val viewModel: LoginViewModel = viewModel(factory = container.loginViewModelFactory)
            val loginState by viewModel.loginState.collectAsStateWithLifecycle()
            val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()

            LoginScreen(
                loginState = loginState,
                isUserLoggedIn = isUserLoggedIn,
                onLoginClick = { phone -> viewModel.login(phone) },
                onCouponsClick = {
                    navController.navigate("coupons")
                },
                onLoginSuccess = {
                    navController.navigate("coupons") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onResetState = { viewModel.resetState() }
            )
        }
        composable("coupons") {
            val container = remember { CouponsContainer(DSquareSDK.appContainer) }
            val viewModel: CouponsViewModel = viewModel(factory = container.couponsViewModelFactory)
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
