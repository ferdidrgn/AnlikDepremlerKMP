# Deprem Takip — Premium Kart Tasarımı + Harita + Reklam + Gerçek Veri

## Proje yapısı (Kotlin Multiplatform)

Proje artık tek modüllü bir Android app değil, gerçek bir **Kotlin Multiplatform**
projesi:

```
composeApp/
  src/
    commonMain/   ← Android + iOS ortak: modeller, DTO'lar, mapper, repository
                    (Ktor), use case'ler, DataStore, Koin DI, AppThemeMode
    androidMain/   ← Android'e özel: Compose UI ekranları, navigation, Maps,
                    AdMob, Firebase, Hilt yerine Koin ile bağlanan ViewModel'ler
    iosMain/       ← iOS'a özel: CoreLocation konum, Koin platform modülü
iosApp/            ← Xcode projesine eklenecek Swift kaynakları (bkz. iosApp/README.md)
```

Paylaşılan katman: veri modelleri, ağ istekleri (Retrofit+Gson yerine Ktor+
kotlinx.serialization), repository/use case katmanı, DataStore tercihleri, DI
(Hilt yerine Koin — KMP'de Hilt çalışmıyor). Android UI hâlâ Jetpack Compose
(`androidMain`); iOS tarafı şimdilik paylaşılan katmanı kullanan minimal bir
SwiftUI ekranı — Compose UI'ı da paylaşmak ve Maps/AdMob/Firebase'i iOS'a
taşımak için `iosApp/README.md` içindeki "Sırada ne var" bölümüne bak.

Bu yapı bir Linux sandbox'ta hazırlandı; Android SDK, Xcode ve Google'ın Maven
deposu (`dl.google.com`) o ortamdan erişilebilir değildi, yani hiçbir hedef
(`assembleDebug` dahil) orada derlenip doğrulanamadı. İlk gerçek Gradle sync'i
Android Studio'da (veya normal internet erişimi olan herhangi bir makinede)
çalıştırıp çıkan hataları düzeltmen gerekebilir — mekanik bir taşıma olduğu için
beklenen sorunlar küçük olmalı (paket/versiyon uyuşmazlıkları gibi).

---

Bu paket, mevcut (veya yeni oluşturacağın) bir Android Studio projesinin üzerine
**overlay** edilmek üzere hazırlandı. Paket adı `com.ferdi.deprem`.

## 🎨 Yeni Tasarım Dili — "Obsidian Premium"

Gönderdiğin referans görseldeki (Sentry IoT app) kart tabanlı, koyu ve "premium"
hissi buraya taşındı:

- **Soğuk, gerçek siyaha yakın zemin** (`#0A0A0D`) — önceki sürümdeki sıcak
  kahverengi nötr tonlar tamamen kaldırıldı.
- **Katmanlı kart yüzeyleri**: `ObsidianSurface` → `ObsidianSurfaceHigh` →
  `ObsidianSurfaceHighest`, her biri hafif farklı koyulukta, ince (%12 beyaz)
  konturlu kartlar (`ui/theme/Theme.kt` içindeki `LocalElevationColors`).
- **Parlayan (glow) büyüklük rozeti**: düz renk yerine radial-gradient ışıltı +
  5.5+ depremlerde nabız animasyonu (`MagnitudeBadge.kt`).
- **"CANLI" hap rozeti**: referanstaki kırmızı "● Live" etiketinin karşılığı,
  son 1 saatteki depremlerde yanıp sönen kırmızı nokta (`LiveBadge.kt`).
- **Dairesel "günün risk göstergesi"**: referanstaki lamba dial'inin karşılığı —
  bugünkü en yüksek büyüklüğü, büyüklüğe göre renklenen parlak bir yay ile
  gösterir (`DailyRiskDial.kt`, Canvas ile çizildi).
- **Hap filtre çipleri**: "Tümü / Bugün / 4.0+ / Yakınımda" — referanstaki
  "Doorbell / Living room / Kitchen" çip satırının karşılığı (`FilterChipsRow.kt`).
- **Gruplu ayar kartları**: her ayar bölümü artık tek bir kart, renkli yumuşak
  ikon avatarlarıyla (`SettingsSection.kt` — turuncu=bildirim, teal=konum,
  mor=görünüm, yeşil=uygulama).
- **Canlı yeşil switch'ler**: M3'ün varsayılan switch rengi yerine referanstaki
  parlak yeşil (`premiumSwitchColors()`).
- **Koyu harita**: Google Haritalar artık koyu temada otomatik karanlık stille
  açılıyor (`res/raw/map_style_dark.json`), beyaz harita artık göze batmıyor.
- Varsayılan tema artık **DARK** (premium tasarım en iyi koyu temada görünür;
  Ayarlar'dan istediğin an Açık/Sistem'e geçebilirsin).

## 1) Kurulum

1. Android Studio'da **Empty Activity (Compose)** projesi aç (paket adı
   `com.ferdi.deprem`).
2. Bu zip'teki dosyaları üzerine kopyala:
   - `app/src/main/java/com/ferdi/deprem/**`
   - `app/src/main/res/**`
   - `app/build.gradle.kts`
   - **`build.gradle.kts`** (proje kökü) — gönderdiğin dosyanın AYNISI, AGP 8.5.2 /
     Kotlin 2.0.0 / Compose plugin sürümleriyle uyumlu hale getirildi.
   - **`local.properties`** — gönderdiğin dosyanın AYNISI zaten zip'in içinde
     (gerçek AdMob App ID + Maps key + test reklam birim ID'lerinle). **Bu
     dosyayı asla git'e ekleme** (`.gitignore` zaten eklendi).
3. `AndroidManifest_EKLE.xml` içindeki izin/meta-data'ları kendi manifest'ine ekle.
4. Gradle Sync yap ve çalıştır.

> Not: `compileSdk`/`targetSdk` değerini AGP 8.5.2 ile uyumlu olacak şekilde
> **35**'e çektim (senin gönderdiğin root dosyada AGP 8.5.2 sabitlenmişti;
> compileSdk 36 için daha yeni bir AGP gerekir).

## 2) Reklam Birim ID'lerin Şu An TEST Modunda

`local.properties`'inde gönderdiğin `ADMOB_BANNER_ID` ve `ADMOB_NATIVE_ID`
değerleri Google'ın resmi TEST ID'leri — bu bilinçli ve doğru bir tercih
(geliştirme sürecinde hesabını riske atmaz). Yayına almadan önce AdMob
panelinden gerçek **Banner** ve **Native Advanced** birim ID'lerini alıp
`local.properties`'te bu iki satırı güncellemen yeterli; kod tarafında hiçbir
değişiklik gerekmez.

## 3) Neler Var? (Güncel Özet)

- **Ana Sayfa**: Karşılama başlığı + bildirim zili (rozet sayacı), dairesel
  günlük risk göstergesi, filtre hapları, premium deprem kartları (glow rozet,
  CANLI etiketi, genişleyen detay + "Haritada gör" aksiyonu), her 10 kayıtta
  native reklam, altta sabit banner.
- **Harita**: Koyu temalı Google Haritalar, büyüklüğe göre renkli marker'lar,
  tıklayınca alttan kayan premium detay kartı, sağ üstte cam kart lejant.
- **Ayarlar**: Gruplu premium kartlar — Bildirimler / Konum / Görünüm (Tema +
  Dil) / Uygulama (paylaş, değerlendir, hakkında).

## 4) Sonraki Adımlar (İsteğe Bağlı)

- "Yakınımda" filtresi şu an Ataşehir/İçerenköy koordinatlarını (40.98, 29.13)
  referans alıyor; konum izni verildiğinde gerçek GPS konumuyla değiştirmek
  `HomeViewModel.kt`'deki `DEFAULT_LAT`/`DEFAULT_LON` sabitlerini
  `FusedLocationProviderClient`'tan gelen değerle değiştirmek kadar basit.
- Native reklam kartına kapatma (X) butonu eklenebilir.
- Haritada çok fazla marker olursa `maps-compose-utils` ile clustering eklenebilir.
