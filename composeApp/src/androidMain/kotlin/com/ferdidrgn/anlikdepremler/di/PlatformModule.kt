package com.ferdidrgn.anlikdepremler.di

import com.ferdidrgn.anlikdepremler.core.datastore.DataStoreKeyValueStore
import com.ferdidrgn.anlikdepremler.core.datastore.KeyValueStore
import com.ferdidrgn.anlikdepremler.core.datastore.PREFERENCES_DATASTORE_FILE_NAME
import com.ferdidrgn.anlikdepremler.core.datastore.createDataStore
import com.ferdidrgn.anlikdepremler.core.data.EarthquakeCommentRepository
import com.ferdidrgn.anlikdepremler.core.data.EventPresenceRepository
import com.ferdidrgn.anlikdepremler.core.data.FeltReportRepository
import com.ferdidrgn.anlikdepremler.core.network.AndroidNetworkMonitor
import com.ferdidrgn.anlikdepremler.core.network.NetworkMonitor
import com.ferdidrgn.anlikdepremler.core.notification.AndroidNearbyEarthquakeNotifier
import com.ferdidrgn.anlikdepremler.core.notification.EarthquakeVoiceAnnouncer
import com.ferdidrgn.anlikdepremler.core.notification.NearbyEarthquakeNotifier
import com.ferdidrgn.anlikdepremler.core.util.AndroidLocationTracker
import com.ferdidrgn.anlikdepremler.core.util.LocationTracker
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<KeyValueStore> {
        DataStoreKeyValueStore(
            createDataStore {
                androidContext().filesDir.resolve(PREFERENCES_DATASTORE_FILE_NAME).absolutePath
            }
        )
    }
    single<LocationTracker> { AndroidLocationTracker(androidContext()) }
    single<NetworkMonitor> { AndroidNetworkMonitor(androidContext()) }
    single { EarthquakeVoiceAnnouncer(androidContext()) }
    single { FeltReportRepository() }
    single { EarthquakeCommentRepository() }
    single { EventPresenceRepository(androidContext()) }
    single<NearbyEarthquakeNotifier> { AndroidNearbyEarthquakeNotifier(androidContext(), get(), get()) }
}
