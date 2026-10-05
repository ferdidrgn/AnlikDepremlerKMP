package com.ferdidrgn.anlikdepremler.di

import com.ferdidrgn.anlikdepremler.core.ads.AdManager
import com.ferdidrgn.anlikdepremler.core.ads.RewardedAdManager
import com.ferdidrgn.anlikdepremler.ui.screen.MainViewModel
import com.ferdidrgn.anlikdepremler.ui.screen.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val androidAppModule = module {
    single { AdManager() }
    single { RewardedAdManager() }

    viewModel { MainViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
}

/** All modules the Android app needs, wired together for startKoin(). */
val androidPlatformModules = listOf(
    commonAppModule,
    networkModule,
    platformModule(),
    androidAppModule
)
