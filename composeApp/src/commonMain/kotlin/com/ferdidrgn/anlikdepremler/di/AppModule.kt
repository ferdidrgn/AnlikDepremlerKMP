package com.ferdidrgn.anlikdepremler.di

import com.ferdidrgn.anlikdepremler.core.datastore.PreferencesManager
import com.ferdidrgn.anlikdepremler.data.repository.EarthquakeRepository
import com.ferdidrgn.anlikdepremler.domain.usecase.CalculateStatisticsUseCase
import com.ferdidrgn.anlikdepremler.domain.usecase.GetEarthquakesUseCase
import com.ferdidrgn.anlikdepremler.domain.usecase.GetUserPreferencesUseCase
import com.ferdidrgn.anlikdepremler.domain.usecase.SaveUserPreferencesUseCase
import org.koin.dsl.module

/** Shared (platform-independent) bindings. Combine with platformModule() from each target. */
val commonAppModule = module {
    single { PreferencesManager(get()) }
    single { EarthquakeRepository(get()) }

    factory { GetEarthquakesUseCase(get()) }
    factory { CalculateStatisticsUseCase() }
    factory { GetUserPreferencesUseCase(get()) }
    factory { SaveUserPreferencesUseCase(get()) }
}

/** Provided per-platform (androidMain / iosMain): binds LocationTracker, NetworkMonitor, etc. */
expect fun platformModule(): org.koin.core.module.Module
