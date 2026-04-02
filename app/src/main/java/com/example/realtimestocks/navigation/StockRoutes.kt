package com.example.realtimestocks.navigation

object StockRoutes {
    const val LIST = "stock_list"
    const val DETAIL = "stock_detail/{symbol}"

    fun detail(symbol: String): String = "stock_detail/$symbol"
}
