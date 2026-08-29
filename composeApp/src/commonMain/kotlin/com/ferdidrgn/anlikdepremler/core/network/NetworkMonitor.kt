package com.ferdidrgn.anlikdepremler.core.network

import kotlinx.coroutines.flow.Flow

/** Platform-specific connectivity observer (Android: ConnectivityManager, iOS: NWPathMonitor). */
interface NetworkMonitor {
    val isConnected: Flow<Boolean>
}
