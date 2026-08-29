package com.ferdidrgn.anlikdepremler.domain.usecase

import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.data.remote.EarthquakeSource
import com.ferdidrgn.anlikdepremler.data.repository.EarthquakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetEarthquakeByIdUseCase (
    private val repository: EarthquakeRepository
) {
    operator fun invoke(
        id: String,
        source: EarthquakeSource = EarthquakeSource.KANDILLI
    ): Flow<Earthquake?> {
        return repository.getEarthquakes(source).map { list ->
            list.find { it.id == id }
        }
    }
}