package com.ferdidrgn.anlikdepremler.di

import org.koin.core.context.startKoin

/** Called once from main.kt before ComposeViewport renders anything. */
fun doInitKoin() {
    startKoin {
        modules(commonAppModule, networkModule, platformModule(), wasmAppModule)
    }
}
