# iosApp

Bu klasör bir Xcode projesi **değil** — `.xcodeproj` dosyası elle/güvenilir şekilde
üretilemediği için (bu depoyu hazırlayan ortamda Xcode yoktu) burada sadece Xcode
projesine ekleyeceğin Swift kaynak dosyaları var:

- `iosApp/iOSApp.swift` — uygulama girişi, `KoinIosKt.doInitKoin()` ile paylaşılan
  Kotlin/Koin katmanını başlatır.
- `iosApp/ContentView.swift` — paylaşılan `EarthquakeRepository`/`GetEarthquakesUseCase`
  üzerinden veri çeken minimal bir SwiftUI ekranı (Android'deki Compose ekranlarının
  birebir portu değil — bkz. "Sırada ne var" bölümü).
- `iosApp/Info.plist`

## Kurulum (Mac + Xcode gerekir)

1. `composeApp` modülünü bir framework olarak inşa edecek Xcode projesini oluştur:
   Xcode → File → New → Project → **iOS → App** (SwiftUI, Bundle ID:
   `com.ferdidrgn.anlikdepremler`, proje adı `iosApp`, bu reponun köküne
   `iosApp/` altına kaydet — bu README'nin yanına).
2. Xcode'un oluşturduğu varsayılan `ContentView.swift` / `iOSApp.swift` dosyalarını bu
   klasördekilerle değiştir, `Info.plist`'i birleştir (konum izni satırını unutma).
3. Target → Build Phases → **+ New Run Script Phase**, en üste taşı, şunu ekle:
   ```sh
   cd "$SRCROOT/.."
   ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
   ```
   Bu, `composeApp`'i (`commonMain` + `iosMain`) bir `ComposeApp.framework`'e derleyip
   Xcode'un `$FRAMEWORKS_FOLDER_PATH`'ine gömer.
4. Target → General → Frameworks and Libraries kısmına gerek yok (Run Script ile
   otomatik gömülüyor), ama **Build Settings → Framework Search Paths**'e
   `$(SRCROOT)/../composeApp/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`
   eklemen gerekebilir (KMP wizard'ın ürettiği projede bu otomatik ayarlanır).
5. Build & Run — simülatörde `KANDILLI` kaynağından çekilen deprem listesini
   görmelisin.

Resmi JetBrains "Kotlin Multiplatform" Xcode entegrasyon adımları (daha ayrıntılı,
resimli): https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-integrate-in-existing-app.html

## Sırada ne var (Phase 2 — bu oturumda yapılmadı)

Bu ilk geçişte **iş mantığı** (model/DTO/mapper/repository/use case/ağ/DataStore/DI)
`commonMain`'e taşındı ve gerçekten paylaşılıyor. Compose UI ekranları hâlâ sadece
Android'de (`androidMain`) — iOS tarafı şimdilik yukarıdaki basit SwiftUI listesi.
Android'deki Compose ekranlarını da paylaşmak (Compose Multiplatform ile birebir aynı
arayüz) için:

1. `ui/screen`, `ui/components`, `ui/theme`, `navigation` paketlerini `commonMain`'e taşı.
2. String kaynaklarını (`res/values*/strings.xml`, 14 dil) Compose Multiplatform'un
   `composeResources` formatına taşı (`stringResource(R.string.x)` →
   `stringResource(Res.string.x)`) — mekanik ama hacimli bir iş.
3. Google Maps Compose'un **iOS karşılığı resmi olarak yok** — `MapScreen` için ya
   iOS'ta native MapKit'e (expect/actual ile ayrı ekran) düş, ya da bir üçüncü parti
   KMP harita kütüphanesi değerlendir.
4. AdMob'un da resmi KMP desteği yok — Android tarafı `com.google.android.gms:play-services-ads`
   ile kalır, iOS tarafı Google Mobile Ads iOS SDK'sını (CocoaPods/SPM) native Swift
   koduyla, `expect`/`actual` bir `AdManager` arkasında entegre etmek gerekir.
5. Firebase: `firebase-firestore`/`messaging`/`analytics`/`crashlytics` şu an Android'e
   özel kaldı (`FeltRepository`, `FcmTokenManager`, `CrashlyticsLogger`). Paylaşmak
   istersen GitLive'ın `dev.gitlive:firebase-*` KMP sarmalayıcılarına geçmek gerekir;
   push bildirimleri iOS'ta ayrıca APNs sertifikası gerektirir.

Bu maddelerin hiçbiri mimariyi değiştirmiyor — hepsi mevcut `expect`/`actual` +
Koin modül deseninin üzerine ekleniyor.
