package com.example.realtimestocks.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.realtimestocks.model.StockQuote

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    symbol: String,
    quote: StockQuote?,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("$symbol Details") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (quote == null) {
                Text(
                    text = "Waiting for latest quote...",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Text(
                    text = quote.symbol,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Latest Price: $${"%.2f".format(quote.price)}",
                    style = MaterialTheme.typography.titleLarge
                )
                val isUp = quote.change > 0
                val isDown = quote.change < 0
                val changeColor = when {
                    isUp -> Color(0xFF2E7D32)
                    isDown -> Color(0xFFC62828)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val indicator = when {
                    isUp -> "↑"
                    isDown -> "↓"
                    else -> "•"
                }
                Text(
                    text = "24h Change: $indicator ${if (quote.change >= 0) "+" else ""}${"%.2f".format(quote.change)}%",
                    color = changeColor,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Updates refresh every second using a simulated feed.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(onClick = onBack) {
                Text("Back to list")
            }
        }
    }
}
