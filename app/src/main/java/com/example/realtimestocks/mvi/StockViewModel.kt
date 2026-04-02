package com.example.realtimestocks.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimestocks.data.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StockViewModel(
    private val repository: StockRepository = StockRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(StockContract.State())
    val state: StateFlow<StockContract.State> = _state.asStateFlow()

    init {
        observeStockUpdates()
        observeConnectionStatus()
    }

    fun onIntent(intent: StockContract.Intent) {
        when (intent) {
            is StockContract.Intent.SelectSymbol -> {
                _state.update { it.copy(selectedSymbol = intent.symbol) }
            }

            StockContract.Intent.ClearSelection -> {
                _state.update { it.copy(selectedSymbol = null) }
            }

            StockContract.Intent.ToggleFeed -> {
                // UI calls this only from the "Start" button (when we are disconnected).
                repository.setFeedRunning(true)
                _state.update { it.copy(isFeedRunning = true) }
            }

            StockContract.Intent.WebsocketClose -> {
                // Internal event: WebSocket disconnected.
                repository.setFeedRunning(false)
                repository.closeWebSocket("WebSocketFeedClosed")
                _state.update { it.copy(isConnected = false, isFeedRunning = false) }
            }
        }
    }

    private fun observeStockUpdates() {
        viewModelScope.launch {
            repository.streamQuotes(_state.value.symbols).collect { incomingQuotes ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        quotes = incomingQuotes
                    )
                }
            }
        }
    }

    private fun observeConnectionStatus() {
        viewModelScope.launch {
            repository.isConnected.collect { connected ->
                _state.update { it.copy(isConnected = connected) }
            }
        }
    }
}
