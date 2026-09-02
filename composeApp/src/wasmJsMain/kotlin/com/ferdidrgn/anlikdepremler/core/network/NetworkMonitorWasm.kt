package com.ferdidrgn.anlikdepremler.core.network

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private fun isBrowserOnline(): Boolean = js("navigator.onLine")

private fun addOnlineOfflineListeners(onOnline: () -> Unit, onOffline: () -> Unit): Unit = js(
    """{
        window.addEventListener('online', onOnline);
        window.addEventListener('offline', onOffline);
    }"""
)

private fun removeOnlineOfflineListeners(onOnline: () -> Unit, onOffline: () -> Unit): Unit = js(
    """{
        window.removeEventListener('online', onOnline);
        window.removeEventListener('offline', onOffline);
    }"""
)

/** Backed by the browser's `navigator.onLine` plus the `online`/`offline` window events. */
class WasmNetworkMonitor : NetworkMonitor {
    override val isConnected: Flow<Boolean> = callbackFlow {
        trySend(isBrowserOnline())

        val onOnline: () -> Unit = { trySend(true) }
        val onOffline: () -> Unit = { trySend(false) }
        addOnlineOfflineListeners(onOnline, onOffline)

        awaitClose { removeOnlineOfflineListeners(onOnline, onOffline) }
    }
}
