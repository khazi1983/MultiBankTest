# MultiBankTest

Assessment from multibank

## Overview
`MultiBankTest` is a sample Android app that displays real-time style stock quotes using a WebSocket feed.
The UI lets you start/stop the feed and shows a short visual flash (green/red) on price change.

## Features
- Stock list screen with:
  - `Start/Stop` button in the top app bar.
  - WebSocket feed that is explicitly closed on `Stop`, and reconnected on `Start`.
  - Price-change flash for 1 second:
    - `quote.change > 0` -> flashes green
    - `quote.change <= 0` -> flashes red
    - then returns to normal text color (black)
- Stock detail screen shows the latest quote and 24h change direction.

## Tech Stack
- Kotlin
- Jetpack Compose (Material3)
- OkHttp WebSocket
- Kotlin Coroutines + Flow
- MVI-ish state management (ViewModel + `StockContract`)

## Run the app
1. Open the project in Android Studio.
2. Ensure you have:
   - Android SDK installed
   - A device/emulator running (API level >= 24)
3. Run the app from Android Studio.

## Tests (Compose UI)
There are instrumented Compose UI tests for the `StockListScreen`:
- `feedToggleButton_callsToggleFeed_whenDisconnected`
- `feedToggleButton_callsWebsocketClose_whenConnected`
- `priceChange_flashesGreen_thenNormal`
- `priceChange_flashesRed_thenNormal`

