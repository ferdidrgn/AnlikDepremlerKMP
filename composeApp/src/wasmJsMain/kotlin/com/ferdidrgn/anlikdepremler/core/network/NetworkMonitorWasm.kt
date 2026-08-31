package com.ferdidrgn.anlikdepremler.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow

/**
 * TODO: wire up the browser's online/offline events (window.addEventListener("online"/"offline"))
 * via kotlinx-browser. For now this always reports "connected".
 */
class WasmNetworkMonitor : NetworkMonitor {
    override val isConnected: Flow<Boolean> = MutableStateFlow(true)
}
