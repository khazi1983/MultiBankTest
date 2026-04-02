package com.example.realtimestocks.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.realtimestocks.mvi.StockContract

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockListScreen(
    state: StockContract.State,
    onSymbolClick: (String) -> Unit,
    onToggleFeed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Realtime Stocks")
                        Text(if (state.isConnected) "🟢 connected" else "🔴 disconnected")
                    }
                },
                actions = {
                    TextButton(onClick = onToggleFeed) {
                        Text(if (state.isFeedRunning) "Stop" else "Start")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Connecting to market feed...",
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.quotes, key = { it.symbol }) { quote ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSymbolClick(quote.symbol) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = quote.symbol,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap for details",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$${"%.2f".format(quote.price)}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                val isUp = quote.change > 0
                                val isDown = quote.change < 0
                                val color = when {
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
                                    text = "$indicator ${if (quote.change >= 0) "+" else ""}${"%.2f".format(quote.change)}%",
                                    color = color,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
