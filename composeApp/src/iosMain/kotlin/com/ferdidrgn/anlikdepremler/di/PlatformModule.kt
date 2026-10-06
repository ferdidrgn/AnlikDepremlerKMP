package com.ferdidrgn.anlikdepremler.di

import com.ferdidrgn.anlikdepremler.core.datastore.DataStoreKeyValueStore
import com.ferdidrgn.anlikdepremler.core.datastore.KeyValueStore
import com.ferdidrgn.anlikdepremler.core.datastore.PREFERENCES_DATASTORE_FILE_NAME
import com.ferdidrgn.anlikdepremler.core.datastore.createDataStore
import com.ferdidrgn.anlikdepremler.core.network.IosNetworkMonitor
import com.ferdidrgn.anlikdepremler.core.network.NetworkMonitor
import com.ferdidrgn.anlikdepremler.core.notification.NearbyEarthquakeNotifier
import com.ferdidrgn.anlikdepremler.core.notification.NoOpNearbyEarthquakeNotifier
import com.ferdidrgn.anlikdepremler.core.util.IosLocationTracker
import com.ferdidrgn.anlikdepremler.core.util.LocationTracker
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun platformModule(): Module = module {
    single<KeyValueStore> {
        DataStoreKeyValueStore(
            createDataStore {
                val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = true,
                    error = null
                )
                requireNotNull(documentDirectory?.path) { "Could not resolve iOS documents directory" } + "/$PREFERENCES_DATASTORE_FILE_NAME"
            }
        )
    }
    single<LocationTracker> { IosLocationTracker() }
    single<NetworkMonitor> { IosNetworkMonitor() }
    single<NearbyEarthquakeNotifier> { NoOpNearbyEarthquakeNotifier() }
}
