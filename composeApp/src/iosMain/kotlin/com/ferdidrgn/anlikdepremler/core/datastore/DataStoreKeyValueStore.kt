package com.ferdidrgn.anlikdepremler.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

const val PREFERENCES_DATASTORE_FILE_NAME = "user_preferences.preferences_pb"

/** Each platform supplies where the file should live (Android: filesDir, iOS: NSDocumentDirectory). */
fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })

/**
 * Wraps a real androidx.datastore DataStore<Preferences> behind the [KeyValueStore] abstraction
 * [PreferencesManager] depends on. This (and [createDataStore]) can't live in commonMain -
 * androidx.datastore doesn't publish a working wasmJs target in this project's dependency
 * version, so a commonMain file importing it fails compileKotlinWasmJs with "Unresolved
 * reference". Duplicated identically in iosMain since there's no existing non-wasm shared
 * source set to hang a single copy off of.
 */
class DataStoreKeyValueStore(private val dataStore: DataStore<Preferences>) : KeyValueStore {
    override fun observeString(key: String, default: String): Flow<String> {
        val prefKey = stringPreferencesKey(key)
        return dataStore.data.map { it[prefKey] ?: default }
    }

    override suspend fun setString(key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        dataStore.edit { it[prefKey] = value }
    }

    override fun observeBoolean(key: String, default: Boolean): Flow<Boolean> {
        val prefKey = booleanPreferencesKey(key)
        return dataStore.data.map { it[prefKey] ?: default }
    }

    override suspend fun setBoolean(key: String, value: Boolean) {
        val prefKey = booleanPreferencesKey(key)
        dataStore.edit { it[prefKey] = value }
    }

    override fun observeInt(key: String, default: Int): Flow<Int> {
        val prefKey = intPreferencesKey(key)
        return dataStore.data.map { it[prefKey] ?: default }
    }

    override suspend fun setInt(key: String, value: Int) {
        val prefKey = intPreferencesKey(key)
        dataStore.edit { it[prefKey] = value }
    }

    override fun observeFloat(key: String, default: Float): Flow<Float> {
        val prefKey = floatPreferencesKey(key)
        return dataStore.data.map { it[prefKey] ?: default }
    }

    override suspend fun setFloat(key: String, value: Float) {
        val prefKey = floatPreferencesKey(key)
        dataStore.edit { it[prefKey] = value }
    }

    override fun observeLong(key: String, default: Long): Flow<Long> {
        val prefKey = longPreferencesKey(key)
        return dataStore.data.map { it[prefKey] ?: default }
    }

    override suspend fun setLong(key: String, value: Long) {
        val prefKey = longPreferencesKey(key)
        dataStore.edit { it[prefKey] = value }
    }

    override fun observeStringSet(key: String, default: Set<String>): Flow<Set<String>> {
        val prefKey = stringSetPreferencesKey(key)
        return dataStore.data.map { it[prefKey] ?: default }
    }

    override suspend fun setStringSet(key: String, value: Set<String>) {
        val prefKey = stringSetPreferencesKey(key)
        dataStore.edit { it[prefKey] = value }
    }
}
