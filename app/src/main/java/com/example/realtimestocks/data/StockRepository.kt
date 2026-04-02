package com.example.realtimestocks.data

import com.example.realtimestocks.model.StockQuote
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import kotlin.random.Random

class StockRepository {
    private val client = OkHttpClient()
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    private val _isFeedRunning = MutableStateFlow(true)
    @Volatile
    var currentWebSocket: WebSocket? = null

    fun setFeedRunning(isRunning: Boolean) {
        _isFeedRunning.value = isRunning
    }
    fun closeWebSocket(reason: String = "WebSocket closed") {
        _isConnected.value = false
        val ws = currentWebSocket
        currentWebSocket = null
        ws?.close(1000, reason)
    }
    fun streamQuotes(symbols: List<String>): Flow<List<StockQuote>> = callbackFlow {
        val random = Random(System.currentTimeMillis())
        val latestQuotes = symbols.associateWith {
            StockQuote(
                symbol = it,
                price = 100 + random.nextDouble() * 400,
                change = 0.0
            )
        }.toMutableMap()

        fun emitLatest() {
            trySend(symbols.map { symbol -> latestQuotes.getValue(symbol) })
        }

        val request = Request.Builder()
            .url("wss://ws.postman-echo.com/raw")
            .build()

        fun connectIfNeeded() {
            if (currentWebSocket != null) return
            currentWebSocket = client.newWebSocket(request, listener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    _isConnected.value = true
                    emitLatest()
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    val parsed = parsePayload(text)
                    if (parsed.isNotEmpty()) {
                        parsed.forEach { quote ->
                            latestQuotes[quote.symbol] = quote
                        }
                        emitLatest()
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    _isConnected.value = false
                    if (currentWebSocket == webSocket) currentWebSocket = null
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    _isConnected.value = false
                    if (currentWebSocket == webSocket) currentWebSocket = null
                }
            })
        }

        fun disconnect(reason: String) {
            // Ensure the send loop and reconnect logic see the socket as "gone".
            val ws = currentWebSocket
            currentWebSocket = null
            _isConnected.value = false
            ws?.close(1000, reason)
        }

        val connectionJob = launch {
            // Start/stop WebSocket based on the latest feedRunning value.
            _isFeedRunning.collect { running ->
                if (running) connectIfNeeded() else disconnect("WebSocketFeedStopped")
            }
        }

        launch {
            while (isActive) {
                if (_isFeedRunning.value) {
                    // Only send while the socket is alive.
                    val ws = currentWebSocket
                    if (ws != null) {
                        symbols.forEach { symbol ->
                            val oldQuote = latestQuotes.getValue(symbol)
                            val delta = random.nextDouble(from = -5.5, until = 5.5)
                            val nextPrice = (oldQuote.price + delta).coerceAtLeast(1.0)
                            val change = (nextPrice - oldQuote.price)
                            val payload = encodePayload(
                                listOf(StockQuote(symbol, nextPrice, change))
                            )
                            withContext(Dispatchers.IO) {
                                ws.send(payload)
                            }
                        }
                    }
                }
                delay(2000L)
            }
        }

        awaitClose {
            connectionJob.cancel()
            disconnect("Closing stream")
        }
    }

    private fun encodePayload(quotes: List<StockQuote>): String {
        return quotes.joinToString(separator = ";") { quote ->
            "${quote.symbol}|${"%.2f".format(quote.price)}|${"%.2f".format(quote.change)}"
        }
    }

    private fun parsePayload(payload: String): List<StockQuote> {
        return payload
            .split(";")
            .mapNotNull { part ->
                val chunks = part.split("|")
                if (chunks.size != 3) return@mapNotNull null
                val symbol = chunks[0]
                val price = chunks[1].toDoubleOrNull() ?: return@mapNotNull null
                val change = chunks[2].toDoubleOrNull() ?: return@mapNotNull null
                StockQuote(symbol = symbol, price = price, change = change)
            }
    }


}
