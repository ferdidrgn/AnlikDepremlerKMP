package com.ferdidrgn.anlikdepremler.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow

/**
 * TODO: wire up a real `platform.Network.nw_path_monitor_t` (NWPathMonitor) implementation.
 * For now this always reports "connected" so the shared UI logic isn't blocked; it does not
 * reflect the device's actual connectivity state.
 */
class IosNetworkMonitor : NetworkMonitor {
    override val isConnected: Flow<Boolean> = MutableStateFlow(true)
}
