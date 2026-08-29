package com.ferdidrgn.anlikdepremler.di

import org.koin.core.context.startKoin

/** Called once from Swift (AnlikDepremlerApp.init) before any shared class is used. */
fun doInitKoin() {
    startKoin {
        modules(commonAppModule, networkModule, platformModule())
    }
}
