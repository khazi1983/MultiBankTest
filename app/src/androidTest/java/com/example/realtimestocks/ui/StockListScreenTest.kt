package com.example.realtimestocks.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.realtimestocks.model.StockQuote
import com.example.realtimestocks.mvi.StockContract
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class StockListScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun feedToggleButton_callsToggleFeed_whenDisconnected() {
        val toggleCount = AtomicInteger(0)
        val closeCount = AtomicInteger(0)

        composeRule.setContent {
            StockListScreen(
                state = StockContract.State(
                    isLoading = false,
                    isConnected = false,
                    isFeedRunning = false,
                    quotes = emptyList()
                ),
                onSymbolClick = {},
                onToggleFeed = { toggleCount.incrementAndGet() },
                onWebsocketClose = { closeCount.incrementAndGet() }
            )
        }

        composeRule.onNodeWithTag("feedToggleButton").performClick()

        assertEquals(1, toggleCount.get())
        assertEquals(0, closeCount.get())
    }

    @Test
    fun feedToggleButton_callsWebsocketClose_whenConnected() {
        val toggleCount = AtomicInteger(0)
        val closeCount = AtomicInteger(0)

        composeRule.setContent {
            StockListScreen(
                state = StockContract.State(
                    isLoading = false,
                    isConnected = true,
                    isFeedRunning = true,
                    quotes = emptyList()
                ),
                onSymbolClick = {},
                onToggleFeed = { toggleCount.incrementAndGet() },
                onWebsocketClose = { closeCount.incrementAndGet() }
            )
        }

        composeRule.onNodeWithTag("feedToggleButton").performClick()

        assertEquals(0, toggleCount.get())
        assertEquals(1, closeCount.get())
    }

    @Test
    fun priceChange_flashesGreen_thenNormal() {
        val symbol = "AAPL"
        val quote = StockQuote(symbol = symbol, price = 120.0, change = 1.0)
        val flashDurationMillis = 400L

        composeRule.mainClock.autoAdvance = false

        composeRule.setContent {
            StockListScreen(
                state = StockContract.State(
                    isLoading = false,
                    isConnected = true,
                    isFeedRunning = true,
                    quotes = listOf(quote)
                ),
                onSymbolClick = {},
                onToggleFeed = {},
                onWebsocketClose = {},
                flashDurationMillis = flashDurationMillis
            )
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("flash-$symbol-green").assertExists()

        composeRule.mainClock.advanceTimeBy(flashDurationMillis)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("flash-$symbol-green").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("flash-$symbol-normal").assertExists()
    }

    @Test
    fun priceChange_flashesRed_thenNormal() {
        val symbol = "NVDA"
        val quote = StockQuote(symbol = symbol, price = 120.0, change = -2.0)
        val flashDurationMillis = 400L

        composeRule.mainClock.autoAdvance = false

        composeRule.setContent {
            StockListScreen(
                state = StockContract.State(
                    isLoading = false,
                    isConnected = true,
                    isFeedRunning = true,
                    quotes = listOf(quote)
                ),
                onSymbolClick = {},
                onToggleFeed = {},
                onWebsocketClose = {},
                flashDurationMillis = flashDurationMillis
            )
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("flash-$symbol-red").assertExists()

        composeRule.mainClock.advanceTimeBy(flashDurationMillis)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("flash-$symbol-red").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("flash-$symbol-normal").assertExists()
    }
}

