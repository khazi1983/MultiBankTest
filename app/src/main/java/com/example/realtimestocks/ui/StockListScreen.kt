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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.realtimestocks.mvi.StockContract
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockListScreen(
    state: StockContract.State,
    onSymbolClick: (String) -> Unit,
    onToggleFeed: () -> Unit,
    onWebsocketClose:()-> Unit,
    flashDurationMillis: Long = 1000L
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(if (state.isConnected) "🟢 connected" else "🔴 disconnected")
                    }
                },
                actions = {
                    TextButton(
                        modifier = Modifier.testTag("feedToggleButton"),
                        onClick = {
                            if (state.isConnected) onWebsocketClose() else onToggleFeed()
                        }
                    ) {
                        Text(if (state.isConnected) "Stop" else "Start")
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.quotes, key = { it.symbol }) { quote ->
                    val isPositive = quote.change > 0
                    var isFlashing by remember(quote.symbol) { mutableStateOf(false) }
                    LaunchedEffect(quote.symbol, quote.change) {
                        isFlashing = true
                        delay(flashDurationMillis)
                        isFlashing = false
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSymbolClick(quote.symbol) }
                            .testTag("stockCard_${quote.symbol}"),
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
                                val green = Color(0xFF2E7D32)
                                val red = Color(0xFFC62828)
                                val normal = Color.Black

                                val color = if (isFlashing) {
                                    if (quote.change > 0) green else red
                                } else {
                                    normal
                                }

                                val color_change =
                                    if (quote.change > 0) green else red

                                val indicator = when {
                                    quote.change > 0 -> "↑"
                                    quote.change < 0 -> "↓"
                                    else -> "•"
                                }

                                Text(
                                    text = "$${"%.2f".format(quote.price)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color=color,
                                    modifier = Modifier.semantics {
                                        val flashColor = when {
                                            quote.change > 0 -> "green"
                                            quote.change <= 0 -> "red"
                                            else -> "normal"
                                        }
                                        contentDescription = "flash-${quote.symbol}-$flashColor"
                                    }
                                )
                                Text(
                                    text = "$indicator ${if (quote.change >= 0) "+" else ""}${"%.2f".format(quote.change)}%",
                                    color = color_change,
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
