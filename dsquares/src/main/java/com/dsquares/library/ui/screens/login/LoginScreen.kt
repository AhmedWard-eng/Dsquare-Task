package com.dsquares.library.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dsquares.library.R
import com.dsquares.library.ui.components.common.DsquareButton
import com.dsquares.library.ui.components.common.LoadingButton
import com.dsquares.library.ui.components.login.PhoneInput
import com.dsquares.library.ui.theme.DsquareTaskTheme

@Composable
fun LoginScreen(
    loginState: LoginUiState,
    onLoginClick: (String) -> Unit,
    onCouponsClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onResetState: () -> Unit,
    modifier: Modifier = Modifier
) {
    val phoneState = rememberTextFieldState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading = loginState is LoginUiState.Loading
    val noInternetMessage = stringResource(R.string.no_internet)
    val invalidPhoneMessage = stringResource(R.string.invalid_phone_number)
    val loginFailedMessage = stringResource(R.string.login_failed)

    LaunchedEffect(loginState) {
        val message = when (loginState) {
            is LoginUiState.Success -> { onLoginSuccess(); null }
            is LoginUiState.NoInternetConnection -> noInternetMessage
            is LoginUiState.ServerError -> loginState.message ?: loginFailedMessage
            is LoginUiState.InvalidPhoneNumber -> invalidPhoneMessage
            else -> null
        }
        message?.let {
            snackbarHostState.showSnackbar(it)
            onResetState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 24.dp)
        ) {
            LoginHeader()

            Spacer(modifier = Modifier.height(24.dp))

            PhoneInput(state = phoneState)

            Spacer(modifier = Modifier.weight(1f))

            LoadingButton(
                text = stringResource(R.string.login),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = { onLoginClick(phoneState.text.toString()) },
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            DsquareButton(
                text = stringResource(R.string.coupons),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = onCouponsClick
            )
        }
    }
}

@Composable
private fun LoginHeader() {
    Text(
        text = stringResource(R.string.enter_phone_number),
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.enter_phone_number_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    DsquareTaskTheme {
        LoginScreen(
            loginState = LoginUiState.Idle,
            onLoginClick = {},
            onCouponsClick = {},
            onLoginSuccess = {},
            onResetState = {}
        )
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
fun LoginScreenArabicPreview() {
    DsquareTaskTheme {
        LoginScreen(
            loginState = LoginUiState.Idle,
            onLoginClick = {},
            onCouponsClick = {},
            onLoginSuccess = {},
            onResetState = {}
        )
    }
}