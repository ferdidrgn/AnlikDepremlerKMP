package com.ferdidrgn.anlikdepremler.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                val json = Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                }
                // A few of the earthquake sources (e.g. Kandilli) send real JSON but
                // mislabel it as text/html or text/plain - accept the JSON converter
                // for those content types too, not just application/json.
                json(json, contentType = ContentType.Application.Json)
                json(json, contentType = ContentType.Text.Plain)
                json(json, contentType = ContentType.Text.Html)
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 15_000
            }
            // The very first request right after a cold app start can fail on Android
            // (UnknownHostException/connect timeout) before the OS has finished bringing the
            // network up - before this, that one bad request surfaced as a dead-end "no
            // internet" screen with no automatic recovery. Retries connection-level failures
            // and 5xx responses up to twice with a short exponential backoff.
            install(HttpRequestRetry) {
                retryOnExceptionOrServerErrors(maxRetries = 2)
                exponentialDelay()
            }
        }
    }
}
