package com.dsquares.library.ui

import android.app.Activity.RESULT_FIRST_USER
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.dsquares.library.DSquareSDK
import com.dsquares.library.ui.navigation.DsquareNavGraph
import com.dsquares.library.ui.theme.DsquareTaskTheme

class DSquareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!DSquareSDK.checkInitialized()) {
            setResult(RESULT_FIRST_USER)
            finish()
            return
        }
        enableEdgeToEdge()
        setContent {
            DsquareTaskTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    DsquareNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
