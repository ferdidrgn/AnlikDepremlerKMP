package com.ferdidrgn.anlikdepremler.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ferdidrgn.anlikdepremler.core.datastore.PREFERENCES_DATASTORE_FILE_NAME
import com.ferdidrgn.anlikdepremler.core.datastore.createDataStore
import com.ferdidrgn.anlikdepremler.core.network.NetworkMonitor
import com.ferdidrgn.anlikdepremler.core.network.WasmNetworkMonitor
import com.ferdidrgn.anlikdepremler.core.util.LocationTracker
import com.ferdidrgn.anlikdepremler.core.util.WasmLocationTracker
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * TODO: androidx.datastore's browser (wasmJs, non-Node) filesystem story is unverified - this
 * compiles against the same PreferenceDataStoreFactory API as Android/iOS, but a real browser
 * has no filesystem, so this almost certainly needs a localStorage/IndexedDB-backed DataStore
 * (or a different persistence layer entirely) before settings actually survive a page reload.
 */
actual fun platformModule(): Module = module {
    single<DataStore<Preferences>> {
        createDataStore { "/$PREFERENCES_DATASTORE_FILE_NAME" }
    }
    single<LocationTracker> { WasmLocationTracker() }
    single<NetworkMonitor> { WasmNetworkMonitor() }
}
