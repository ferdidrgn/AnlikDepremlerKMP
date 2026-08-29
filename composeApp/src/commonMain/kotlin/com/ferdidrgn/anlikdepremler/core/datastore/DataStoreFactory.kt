package com.ferdidrgn.anlikdepremler.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

const val PREFERENCES_DATASTORE_FILE_NAME = "user_preferences.preferences_pb"

/** Each platform supplies where the file should live (Android: filesDir, iOS: NSDocumentDirectory). */
fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })
