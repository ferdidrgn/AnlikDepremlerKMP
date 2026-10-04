package com.ferdidrgn.anlikdepremler.data.repository

import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.data.mapper.toDomain
import com.ferdidrgn.anlikdepremler.data.remote.EarthquakeSource
import com.ferdidrgn.anlikdepremler.data.remote.dto.EmscEarthquakeDto
import com.ferdidrgn.anlikdepremler.data.remote.dto.TurkeyAfadEarthquakeDto
import com.ferdidrgn.anlikdepremler.data.remote.dto.TurkeyAllEarthquakeDto
import com.ferdidrgn.anlikdepremler.data.remote.dto.TurkeyKandilliEarthquakeDto
import com.ferdidrgn.anlikdepremler.data.remote.dto.WorldIGPEarthquakeDto
import com.ferdidrgn.anlikdepremler.data.remote.dto.WorldUSGSEarthquakeDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

class EarthquakeRepository(
    private val httpClient: HttpClient
) {

    fun getEarthquakes(source: EarthquakeSource = EarthquakeSource.KANDILLI): Flow<List<Earthquake>> =
        flow {
            try {
                val list = when (source) {
                    EarthquakeSource.KANDILLI -> {
                        httpClient.get("https://www.mertsenturk.net/deprem/api/limit/800")
                            .body<List<TurkeyKandilliEarthquakeDto>>()
                            .map { it.toDomain() }
                    }

                    EarthquakeSource.AFAD -> {
                        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                        val fourDaysAgo = today.minus(4, DateTimeUnit.DAY)

                        httpClient.get("https://deprem.afad.gov.tr/apiv2/event/filter") {
                            parameter("start", fourDaysAgo.toString())
                            parameter("end", today.toString())
                            parameter("orderby", "timedesc")
                            parameter("minmag", 2)
                            parameter("limit", 100)
                        }.body<List<TurkeyAfadEarthquakeDto>>().map { it.toDomain() }
                    }

                    EarthquakeSource.TURKEY_ALL -> {
                        httpClient.get("https://api.orhanaydogdu.com.tr/deprem/")
                            .body<TurkeyAllEarthquakeDto>()
                            .result.orEmpty().map { it.toDomain() }
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
}
