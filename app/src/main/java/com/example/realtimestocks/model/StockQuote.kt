package com.example.realtimestocks.model

data class StockQuote(
    val symbol: String,
    val price: Double,
    val change: Double
)
