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
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class StockRepository {
    private val client = OkHttpClient()
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    private val _isFeedRunning = MutableStateFlow(true)

    fun setFeedRunning(isRunning: Boolean) {
        _isFeedRunning.value = isRunning
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
        val maxReconnectAttempts = 2
        val reconnectAttempts = AtomicInteger(0)
        val isShuttingDown = AtomicBoolean(false)
        val reconnectScheduled = AtomicBoolean(false)
        var currentWebSocket: WebSocket? = null

        fun connect() {
            if (isShuttingDown.get()) return
            currentWebSocket = client.newWebSocket(request, listener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    _isConnected.value = true
                    reconnectAttempts.set(0)
                    reconnectScheduled.set(false)
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
                    scheduleReconnect()
                }

                private fun scheduleReconnect() {
                    if (isShuttingDown.get()) return
                    if (!reconnectScheduled.compareAndSet(false, true)) return
                    val nextAttempt = reconnectAttempts.incrementAndGet()
                    if (nextAttempt > maxReconnectAttempts) {
                        reconnectScheduled.set(false)
                        return
                    }
                    launch {
                        delay(1000L * nextAttempt)
                        reconnectScheduled.set(false)
                        connect()
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    _isConnected.value = false
                    scheduleReconnect()
                }
            })
        }

        connect()

        launch {
            while (isActive) {
                if (!_isFeedRunning.value) {
                    delay(200L)
                    continue
                }
                symbols.forEach { symbol ->
                    val oldQuote = latestQuotes.getValue(symbol)
                    val delta = random.nextDouble(from = -3.5, until = 3.5)
                    val nextPrice = (oldQuote.price + delta).coerceAtLeast(1.0)
                    val change = random.nextDouble(from = -2.0, until = 2.0)
                    val payload = encodePayload(
                        listOf(StockQuote(symbol, nextPrice, change))
                    )
                    withContext(Dispatchers.IO) {
                        currentWebSocket?.send(payload)
                    }
                }
                delay(2000L)
            }
        }

        awaitClose {
            isShuttingDown.set(true)
            _isConnected.value = false
            currentWebSocket?.close(1000, "Closing stream")
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
