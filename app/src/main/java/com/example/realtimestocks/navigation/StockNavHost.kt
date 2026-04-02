package com.example.realtimestocks.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.realtimestocks.mvi.StockContract
import com.example.realtimestocks.mvi.StockViewModel
import com.example.realtimestocks.ui.StockDetailScreen
import com.example.realtimestocks.ui.StockListScreen

@Composable
fun StockNavHost(
    stockViewModel: StockViewModel = viewModel()
) {
    val navController = rememberNavController()
    val state by stockViewModel.state.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = StockRoutes.LIST
    ) {
        composable(StockRoutes.LIST) {
            StockListScreen(
                state = state,
                onSymbolClick = { symbol ->
                    stockViewModel.onIntent(StockContract.Intent.SelectSymbol(symbol))
                    navController.navigate(StockRoutes.detail(symbol))
                },
                onToggleFeed = {
                    stockViewModel.onIntent(StockContract.Intent.ToggleFeed)
                },
                onWebsocketClose={
                    stockViewModel.onIntent(StockContract.Intent.WebsocketClose)
                }
            )
        }

        composable(
            route = StockRoutes.DETAIL,
            arguments = listOf(navArgument("symbol") { type = NavType.StringType })
        ) { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol").orEmpty()
            StockDetailScreen(
                symbol = symbol,
                quote = state.quotes.firstOrNull { it.symbol == symbol },
                onBack = {
                    stockViewModel.onIntent(StockContract.Intent.ClearSelection)
                    navController.popBackStack()
                }
            )
        }
    }
}
