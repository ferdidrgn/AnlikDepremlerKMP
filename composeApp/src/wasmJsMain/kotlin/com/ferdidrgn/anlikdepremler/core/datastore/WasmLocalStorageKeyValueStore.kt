package com.ferdidrgn.anlikdepremler.core.datastore

import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Browser localStorage-backed [KeyValueStore] for the wasmJs target - androidx.datastore doesn't
 * publish a working wasmJs variant (see KeyValueStore.kt for why), so this is a from-scratch
 * implementation rather than wrapping DataStore the way Android/iOS do. Persists permanently
 * (survives closing the tab), same as a real app install's storage would.
 */
class WasmLocalStorageKeyValueStore : KeyValueStore {
    private val rawFlows = mutableMapOf<String, MutableStateFlow<String?>>()

    private fun rawFlow(key: String): MutableStateFlow<String?> =
        rawFlows.getOrPut(key) { MutableStateFlow(localStorage.getItem(key)) }

    private fun writeRaw(key: String, value: String) {
        localStorage.setItem(key, value)
        rawFlow(key).value = value
    }

    override fun observeString(key: String, default: String): Flow<String> =
        rawFlow(key).map { it ?: default }

    override suspend fun setString(key: String, value: String) = writeRaw(key, value)

    override fun observeBoolean(key: String, default: Boolean): Flow<Boolean> =
        rawFlow(key).map { it?.toBooleanStrictOrNull() ?: default }

    override suspend fun setBoolean(key: String, value: Boolean) = writeRaw(key, value.toString())

    override fun observeInt(key: String, default: Int): Flow<Int> =
        rawFlow(key).map { it?.toIntOrNull() ?: default }

    override suspend fun setInt(key: String, value: Int) = writeRaw(key, value.toString())

    override fun observeFloat(key: String, default: Float): Flow<Float> =
        rawFlow(key).map { it?.toFloatOrNull() ?: default }

    override suspend fun setFloat(key: String, value: Float) = writeRaw(key, value.toString())

    override fun observeLong(key: String, default: Long): Flow<Long> =
        rawFlow(key).map { it?.toLongOrNull() ?: default }

    override suspend fun setLong(key: String, value: Long) = writeRaw(key, value.toString())

    override fun observeStringSet(key: String, default: Set<String>): Flow<Set<String>> =
        rawFlow(key).map { raw ->
            if (raw == null) {
                default
            } else {
                runCatching { Json.decodeFromString<List<String>>(raw).toSet() }.getOrDefault(default)
            }
        }

    override suspend fun setStringSet(key: String, value: Set<String>) =
        writeRaw(key, Json.encodeToString(value.toList()))
}
