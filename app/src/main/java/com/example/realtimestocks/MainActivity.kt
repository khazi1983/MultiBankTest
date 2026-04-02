package com.example.realtimestocks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.realtimestocks.navigation.StockNavHost
import com.example.realtimestocks.ui.theme.RealtimeStocksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RealtimeStocksTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    StockNavHost()
                }
            }
        }
    }
}
