import SwiftUI
import ComposeApp

// Minimal proof-of-life screen: fetches earthquakes through the SAME shared Kotlin
// repository/use case/network layer the Android app uses (composeApp/src/commonMain).
// This is intentionally plain SwiftUI, not a port of the Android Compose UI yet —
// see iosApp/README.md for what's still needed for feature parity.
struct ContentView: View {
    @State private var earthquakes: [Earthquake] = []
    @State private var isLoading = true
    @State private var errorMessage: String?

    var body: some View {
        NavigationView {
            Group {
                if isLoading {
                    ProgressView("Depremler yükleniyor…")
                } else if let errorMessage {
                    Text(errorMessage).foregroundColor(.red)
                } else {
                    List(earthquakes, id: \.id) { eq in
                        VStack(alignment: .leading) {
                            Text(eq.location).font(.headline)
                            Text("M\(String(format: "%.1f", eq.magnitude)) • \(eq.date) \(eq.time)")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                    }
                }
            }
            .navigationTitle("Anlık Depremler")
            .task {
                await loadEarthquakes()
            }
        }
    }

    private func loadEarthquakes() async {
        do {
            earthquakes = try await KoinHelper.shared.fetchEarthquakes(source: EarthquakeSource.kandilli)
            isLoading = false
        } catch {
            errorMessage = error.localizedDescription
            isLoading = false
        }
    }
}
