package com.ferdidrgn.anlikdepremler.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ferdidrgn.anlikdepremler.core.datastore.PREFERENCES_DATASTORE_FILE_NAME
import com.ferdidrgn.anlikdepremler.core.datastore.createDataStore
import com.ferdidrgn.anlikdepremler.core.network.NetworkMonitor
import com.ferdidrgn.anlikdepremler.core.network.WasmNetworkMonitor
import com.ferdidrgn.anlikdepremler.core.notification.NearbyEarthquakeNotifier
import com.ferdidrgn.anlikdepremler.core.notification.NoOpNearbyEarthquakeNotifier
import com.ferdidrgn.anlikdepremler.core.util.LocationTracker
import com.ferdidrgn.anlikdepremler.core.util.WasmLocationTracker
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * PreferenceDataStoreFactory.createWithPath has a webMain `actual` (since datastore
 * 1.3.0-alpha01) that swaps in WebSessionStorage automatically instead of touching a
 * filesystem - the exact same createDataStore() call used on Android/iOS works here
 * unchanged. Caveat: it's *session* storage, so settings persist for the browser tab's
 * lifetime but not across a full close-and-reopen; androidx.datastore's newer WebLocalStorage
 * (1.3.0-alpha08+) would persist permanently instead, at the cost of calling
 * PreferenceDataStoreFactory.create(storage = ...) directly instead of this shared helper.
 */
actual fun platformModule(): Module = module {
    single<DataStore<Preferences>> {
        createDataStore { "/$PREFERENCES_DATASTORE_FILE_NAME" }
    }
    single<LocationTracker> { WasmLocationTracker() }
    single<NetworkMonitor> { WasmNetworkMonitor() }
    single<NearbyEarthquakeNotifier> { NoOpNearbyEarthquakeNotifier() }
}
