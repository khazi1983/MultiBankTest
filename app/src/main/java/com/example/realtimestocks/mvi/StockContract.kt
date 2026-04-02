package com.example.realtimestocks.mvi

import com.example.realtimestocks.model.StockQuote

object StockContract {
    data class State(
        val isLoading: Boolean = true,
        val isConnected: Boolean = false,
        val isFeedRunning: Boolean = true,
        val symbols: List<String> = listOf("NVDA", "AAPL", "GOOG", "MSFT", "AMZN"),
        val quotes: List<StockQuote> = emptyList(),
        val selectedSymbol: String? = null
    ) {
        val selectedQuote: StockQuote?
            get() = quotes.firstOrNull { it.symbol == selectedSymbol }
    }

    sealed interface Intent {
        data class SelectSymbol(val symbol: String) : Intent
        data object ClearSelection : Intent
        data object ToggleFeed : Intent
    }
}
