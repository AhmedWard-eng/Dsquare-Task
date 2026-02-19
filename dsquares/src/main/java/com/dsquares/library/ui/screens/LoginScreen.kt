package com.dsquares.library.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dsquares.library.R
import com.dsquares.library.ui.components.DsquareButton
import com.dsquares.library.ui.components.PhoneInput
import com.dsquares.library.ui.theme.DsquareTaskTheme

@Composable
fun LoginScreen(
    onCouponsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val phoneState = rememberTextFieldState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 24.dp)
    ) {
        LoginHeader()

        Spacer(modifier = Modifier.height(24.dp))

        PhoneInput(state = phoneState)

        Spacer(modifier = Modifier.weight(1f))

        DsquareButton(
            text = stringResource(R.string.login),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick =  {

            }
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
        LoginScreen()
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
fun LoginScreenArabicPreview() {
    DsquareTaskTheme {
        LoginScreen()
    }
}