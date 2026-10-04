package com.ferdidrgn.anlikdepremler.di

import com.ferdidrgn.anlikdepremler.ui.screen.MainViewModel
import org.koin.dsl.module

/**
 * There's no ViewModelStore on the web the way there is on Android, so MainViewModel is a
 * plain Koin `single` here instead of the Android-only androidx-compose `viewModel {}` DSL -
 * one instance for the lifetime of the browser tab is exactly what a single-page app needs.
 */
val wasmAppModule = module {
    single { MainViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
}
