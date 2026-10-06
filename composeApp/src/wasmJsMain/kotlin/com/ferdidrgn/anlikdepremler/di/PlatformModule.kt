package com.ferdidrgn.anlikdepremler.di

import com.ferdidrgn.anlikdepremler.core.datastore.KeyValueStore
import com.ferdidrgn.anlikdepremler.core.datastore.WasmLocalStorageKeyValueStore
import com.ferdidrgn.anlikdepremler.core.network.NetworkMonitor
import com.ferdidrgn.anlikdepremler.core.network.WasmNetworkMonitor
import com.ferdidrgn.anlikdepremler.core.notification.NearbyEarthquakeNotifier
import com.ferdidrgn.anlikdepremler.core.notification.NoOpNearbyEarthquakeNotifier
import com.ferdidrgn.anlikdepremler.core.util.LocationTracker
import com.ferdidrgn.anlikdepremler.core.util.WasmLocationTracker
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<KeyValueStore> { WasmLocalStorageKeyValueStore() }
    single<LocationTracker> { WasmLocationTracker() }
    single<NetworkMonitor> { WasmNetworkMonitor() }
    single<NearbyEarthquakeNotifier> { NoOpNearbyEarthquakeNotifier() }
}
