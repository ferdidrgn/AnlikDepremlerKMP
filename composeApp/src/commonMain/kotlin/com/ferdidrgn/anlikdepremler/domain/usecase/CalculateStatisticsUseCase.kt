package com.ferdidrgn.anlikdepremler.domain.usecase

import com.ferdi.deprem.model.Earthquake
import com.ferdi.deprem.model.EarthquakeStatistics
import com.ferdidrgn.anlikdepremler.domain.util.calculateStatistics

class CalculateStatisticsUseCase () {

    operator fun invoke(earthquakes: List<Earthquake>): EarthquakeStatistics {
        return earthquakes.calculateStatistics()
    }
}