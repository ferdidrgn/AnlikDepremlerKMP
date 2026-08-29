package com.ferdidrgn.anlikdepremler.domain.usecase

import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.data.remote.EarthquakeSource
import com.ferdidrgn.anlikdepremler.data.repository.EarthquakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetSignificantEarthquakesUseCase (
    private val repository: EarthquakeRepository
) {
    operator fun invoke(source: EarthquakeSource = EarthquakeSource.KANDILLI): Flow<List<Earthquake>> {
        return repository.getEarthquakes(source).map { list ->
            list.filter { it.isSignificant || it.magnitude >= 4.5 }
        }
    }
}