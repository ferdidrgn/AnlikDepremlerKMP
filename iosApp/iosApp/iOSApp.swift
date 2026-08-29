import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    init() {
        // Starts the shared Koin DI graph (network client, repositories, use cases)
        // before any shared Kotlin class is touched.
        KoinIosKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
