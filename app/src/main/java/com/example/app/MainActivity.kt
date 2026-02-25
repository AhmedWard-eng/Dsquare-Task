package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dsquares.library.CouponsResult
import com.dsquares.library.DSquareSDK
import com.dsquares.library.DSquareSDK.Companion.registerCouponsLauncher
import com.dsquares.library.LoginResult
import com.example.app.ui.XMLActivity
import com.example.app.ui.theme.DsquareTaskTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var couponsLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        couponsLauncher =
            registerCouponsLauncher { result ->
                when (result) {
                    is CouponsResult.Success ->
                        Toast.makeText(
                            this@MainActivity, "Coupons success: ${result.data}",
                            Toast.LENGTH_SHORT
                        ).show()

                    is CouponsResult.Canceled -> Toast.makeText(
                        this@MainActivity, "Coupons canceled",
                        Toast.LENGTH_SHORT
                    ).show()

                    is CouponsResult.Failure -> Toast.makeText(
                        this@MainActivity, "Coupons failure: ${result.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


        setContent {
            DsquareTaskTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DSquareScreen(
                        modifier = Modifier.padding(innerPadding),
                        onLoginClick = { phone ->
                            val result = DSquareSDK.logIn(phone)
                            when (result) {
                                is LoginResult.Error -> Toast.makeText(
                                    this@MainActivity, "login failed: ${result.message}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                LoginResult.Success -> Toast.makeText(
                                    this@MainActivity, "login succeed: $result",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onShowCouponsClick = {
                            val launcher = couponsLauncher
                            if (launcher != null) {
                                DSquareSDK.launchCoupons(this, launcher)
                            }
                        },
                        onGoToTheXML = {
                            startActivity(Intent(this, XMLActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DSquareScreen(
    modifier: Modifier = Modifier,
    onLoginClick: suspend (String) -> Unit = {},
    onShowCouponsClick: () -> Unit = {},
    onGoToTheXML: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier
            .padding(all = 20.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            modifier = Modifier.size(200.dp, 50.dp), onClick = {
                coroutineScope.launch {
                    onLoginClick("01020935102")
                }
            }) {
            Text("Login")
        }
        Spacer(Modifier.height(20.dp))
        Button(
            modifier = Modifier.size(200.dp, 50.dp), onClick = onShowCouponsClick
        ) {
            Text(stringResource(R.string.show_coupons))
        }

        Spacer(Modifier.height(20.dp))
        Button(
            modifier = Modifier.size(200.dp, 50.dp), onClick = onGoToTheXML
        ) {
            Text("go to the xml screen")
        }

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DsquareTaskTheme {
        DSquareScreen(Modifier)
    }
}