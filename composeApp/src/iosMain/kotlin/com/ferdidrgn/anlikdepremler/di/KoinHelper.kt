package com.ferdidrgn.anlikdepremler.di

import com.ferdi.deprem.model.Earthquake
import com.ferdidrgn.anlikdepremler.data.remote.EarthquakeSource
import com.ferdidrgn.anlikdepremler.data.repository.EarthquakeRepository
import com.ferdidrgn.anlikdepremler.domain.usecase.GetEarthquakesUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Swift/Objective-C can't call Koin's generic `get<T>()` or consume a Kotlin `Flow` directly,
 * so expose the handful of shared classes iosApp needs as plain suspend functions. Kotlin/Native
 * exports `suspend fun` as Swift `async throws` automatically — call with `try await` from Swift.
 */
object KoinHelper : KoinComponent {
    private val earthquakeRepository: EarthquakeRepository by inject()
    private val getEarthquakesUseCase: GetEarthquakesUseCase by inject()

    fun earthquakeRepository(): EarthquakeRepository = earthquakeRepository
    fun getEarthquakesUseCase(): GetEarthquakesUseCase = getEarthquakesUseCase

    /** One-shot fetch for the iOS demo screen (no Flow crossing the Swift boundary). */
    suspend fun fetchEarthquakes(source: EarthquakeSource = EarthquakeSource.KANDILLI): List<Earthquake> =
        getEarthquakesUseCase.invoke(source = source).first()
}
