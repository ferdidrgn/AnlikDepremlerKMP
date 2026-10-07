package com.ferdidrgn.anlikdepremler.data.repository

import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.data.mapper.toDomain
import com.ferdidrgn.anlikdepremler.data.remote.EarthquakeSource
import com.ferdidrgn.anlikdepremler.data.remote.dto.EmscEarthquakeDto
import com.ferdidrgn.anlikdepremler.data.remote.dto.TurkeyAllEarthquake
import com.ferdidrgn.anlikdepremler.data.remote.dto.TurkeyAllEarthquakeDto
import com.ferdidrgn.anlikdepremler.data.remote.dto.WorldIGPEarthquakeDto
import com.ferdidrgn.anlikdepremler.data.remote.dto.WorldUSGSEarthquakeDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EarthquakeRepository(
    private val httpClient: HttpClient
) {

    fun getEarthquakes(source: EarthquakeSource = EarthquakeSource.KANDILLI): Flow<List<Earthquake>> =
        flow {
            try {
                val list = when (source) {
                    // Kandilli's own site (and the mertsenturk.net mirror this used to call) sits
                    // behind a Cloudflare bot challenge and sends no Access-Control-Allow-Origin,
                    // so browsers block it outright - confirmed via a direct header check, not a
                    // guess. AFAD's official API 302-redirects every request (including the CORS
                    // preflight) to servisnet.afad.gov.tr without ever answering the preflight
                    // itself, which browsers also treat as a hard CORS failure. Both worked on
                    // Android/iOS only because native HTTP clients don't enforce CORS at all -
                    // the underlying endpoints were never actually reliable.
                    //
                    // api.orhanaydogdu.com.tr aggregates both (confirmed: its "provider" field is
                    // "kandilli" or "afad") and sends Access-Control-Allow-Origin: *, so this
                    // fetches from there instead and filters by provider - same data, works on
                    // every platform.
                    EarthquakeSource.KANDILLI -> {
                        fetchOrhanAydogdu().filter { it.provider == "kandilli" }.map { it.toDomain() }
                    }

                    EarthquakeSource.AFAD -> {
                        fetchOrhanAydogdu().filter { it.provider == "afad" }.map { it.toDomain() }
                    }

                    EarthquakeSource.TURKEY_ALL -> {
                        fetchOrhanAydogdu().map { it.toDomain() }
                    }

                    EarthquakeSource.USGS -> {
                        httpClient.get("https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson")
                            .body<WorldUSGSEarthquakeDto>()
                            .features.orEmpty().map { it.toDomain() }
                    }

                    EarthquakeSource.WORLD_IGP -> {
                        httpClient.get("https://cache.earthquakenetwork.it/distquake_download_automatic21.php?pro")
                            .body<List<WorldIGPEarthquakeDto>>()
                            .map { it.toDomain() }
                    }

                    EarthquakeSource.EMSC -> {
                        httpClient.get("https://www.seismicportal.eu/fdsnws/event/1/query") {
                            parameter("limit", 100)
                            parameter("format", "json")
                        }.body<EmscEarthquakeDto>().features.orEmpty().map { it.toDomain() }
                    }
                }
                emit(list)
            } catch (e: Exception) {
                // Surfaced to the UI via MainViewModel.loadEarthquakes()'s .catch{} - swallowing
                // this into an empty list used to hide real network/parsing failures entirely.
                throw e
            }
        }

    private suspend fun fetchOrhanAydogdu(): List<TurkeyAllEarthquake> =
        httpClient.get("https://api.orhanaydogdu.com.tr/deprem/")
            .body<TurkeyAllEarthquakeDto>()
            .result.orEmpty()
}
