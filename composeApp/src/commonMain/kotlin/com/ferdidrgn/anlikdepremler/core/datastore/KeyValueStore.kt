package com.ferdidrgn.anlikdepremler.core.datastore

import kotlinx.coroutines.flow.Flow

/**
 * Minimal cross-platform key-value store that [PreferencesManager] is built on, so this
 * (commonMain) code never references androidx.datastore types directly - that library doesn't
 * publish a working wasmJs target in the version this project depends on (the type itself is
 * "Unresolved reference" when compileKotlinWasmJs tries to compile commonMain against it), so any
 * commonMain file importing it fails the web build. Android/iOS bind a real DataStore-backed
 * implementation; wasmJs binds one backed by browser localStorage instead.
 */
interface KeyValueStore {
    fun observeString(key: String, default: String): Flow<String>
    suspend fun setString(key: String, value: String)

    fun observeBoolean(key: String, default: Boolean): Flow<Boolean>
    suspend fun setBoolean(key: String, value: Boolean)

    fun observeInt(key: String, default: Int): Flow<Int>
    suspend fun setInt(key: String, value: Int)

    fun observeFloat(key: String, default: Float): Flow<Float>
    suspend fun setFloat(key: String, value: Float)

    fun observeLong(key: String, default: Long): Flow<Long>
    suspend fun setLong(key: String, value: Long)

    fun observeStringSet(key: String, default: Set<String>): Flow<Set<String>>
    suspend fun setStringSet(key: String, value: Set<String>)
}
