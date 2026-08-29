package com.ferdidrgn.anlikdepremler.domain.usecase

import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.data.remote.EarthquakeSource
import com.ferdidrgn.anlikdepremler.data.repository.EarthquakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetEarthquakesUseCase (
    private val repository: EarthquakeRepository
) {
    operator fun invoke(
        source: EarthquakeSource = EarthquakeSource.KANDILLI,
        query: String = "",
        minMagnitude: Double = 0.0
    ): Flow<List<Earthquake>> {
        return repository.getEarthquakes(source).map { list ->
            list.filter { eq ->
                val matchesQuery = eq.location.contains(query, ignoreCase = true) ||
                        eq.region.contains(query, ignoreCase = true)
                val matchesMag = eq.magnitude >= minMagnitude
                matchesQuery && matchesMag
            }
        }
    }
}