<div align="center">

<img src="composeApp/src/androidMain/ic_launcher-playstore.png" width="120" alt="Anlık Depremler ikonu" />

# 🌍 Anlık Depremler

### Türkiye ve dünya genelinde gerçek zamanlı deprem takibi — Android, iOS ve Web'de tek kod tabanından.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.7.1-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Platforms](https://img.shields.io/badge/Platforms-Android%20%7C%20iOS%20%7C%20Web-success?style=for-the-badge)](#-hakkında)

[🌐 Canlı Web Uygulaması](https://anlikdeprem.web.app) · [🐛 Hata Bildir](https://github.com/ferdidrgn/AnlikDepremlerKMP/issues) · [✨ Özellik İste](https://github.com/ferdidrgn/AnlikDepremlerKMP/issues)

</div>

---

## 📖 Hakkında

**Anlık Depremler**, Kandilli Rasathanesi, AFAD, USGS, EMSC ve dünya genelindeki (IGP) sismik
veri kaynaklarını tek bir uygulamada birleştiren, gerçek zamanlı bir deprem takip
uygulamasıdır. Kotlin Multiplatform üzerine inşa edilmiştir: veri katmanı, iş mantığı ve
durum yönetimi (ViewModel) **tek bir ortak Kotlin kod tabanında** yazılır; Android, iOS ve
Web (Kotlin/Wasm) hedeflerinin her biri kendi arayüzünü — ama aynı canlı veriyi — sunar.

<div align="center">

|  | Android | iOS | Web |
|---|:---:|:---:|:---:|
| **Durum** | ✅ Yayında | 🚧 Geliştiriliyor | ✅ Yayında |
| **Arayüz** | Jetpack Compose (tam) | SwiftUI (iskelet) | Compose Multiplatform (dashboard) |
| **Canlı veri** | ✅ | ✅ | ✅ |

</div>

---

## ✨ Özellikler

### 🔴 Canlı Deprem Verisi
- 🇹🇷 **Kandilli**, **AFAD**, **Türkiye Karışık** ve 🌍 **USGS**, **EMSC**, **Dünya Genel (IGP)**
  kaynakları arasında anlık geçiş
- 📊 Günlük / haftalık / aylık deprem istatistikleri, ortalama & maksimum büyüklük, en aktif bölge
- 📈 Büyüklük dağılım grafiği (canlı animasyonlu çubuklar)
- ⏱️ Zaman filtreleri: son 1 saat, 6 saat, 24 saat, 7 gün, 30 gün

### 📍 Konum Farkındalığı
- GPS ile otomatik şehir tespiti ve o bölgedeki depremleri listeleme
- **Yakın deprem uyarısı**: 100 km yakınında 4.0+ büyüklüğünde bir deprem olduğunda anında kart
  uyarısı + kaydedilen acil durum telefon numarasına hızlı erişim
- 🗺️ İnteraktif Google Haritalar entegrasyonu, büyüklüğe göre renklendirilmiş işaretçiler

### 🔔 Bildirim & Paylaşım
- Firebase Cloud Messaging ile anlık deprem bildirimleri
- **Derin bağlantılar (deep links)**: paylaşılan bir deprem/harita linki — uygulama telefonda
  yüklüyse doğrudan ilgili ekranı açar, yüklü değilse otomatik olarak web sitesine yönlendirir
  (Android App Links, `assetlinks.json` ile doğrulanmış)

### 🎨 Kişiselleştirme
- 🌗 3 tema modu: **Krem (Açık)**, **Sistem**, **Koyu Gece**
- 🌐 **14 dil** desteği, uygulama içinden anında dil değişimi (yeniden başlatma gerekmez)
- Android 12+ için özel tasarlanmış, markalı sistem açılış (splash) ekranı

### 📚 Ek İçerik
- 🏛️ Geçmiş büyük depremler arşivi (Kahramanmaraş 2023, İzmir 2020, Van 2011, Gölcük 1999...)
- ✅ Acil durum çantası kontrol listesi ve hazırlık ipuçları
- 📄 Gizlilik politikası ve kullanım şartları uygulama içinde

### 💰 Gelir Modeli
- AdMob banner & native reklamlar (mobil)
- Google AdSense (web)
- Google Play Billing ile isteğe bağlı "kahve ısmarla" bağışı

---

## 🖼️ Ekran Görüntüleri

> Gerçek cihaz/simülatör ekran görüntüleri henüz eklenmedi — katkıda bulunmak isterseniz
> `docs/screenshots/` altına Android, iOS ve Web ekran görüntülerinizi ekleyip bir PR açabilirsiniz. 🙌

<div align="center">
<img src="composeApp/src/wasmJsMain/resources/icons/icon-512.png" width="96" alt="Uygulama rozeti" />
</div>

---

## 🏗️ Mimari

Proje, [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) ile **tek
sorumluluk** ilkesine göre katmanlara ayrılmıştır: veri/iş mantığı `commonMain`'de paylaşılır,
her platform yalnızca kendi arayüz katmanını yazar.

```mermaid
flowchart TB
    subgraph common["📦 commonMain — paylaşılan katman"]
        direction LR
        Model["Domain Modelleri<br/>Earthquake, EarthquakeStatistics"]
        DTO["Uzak DTO'lar<br/>Kandilli · AFAD · USGS · EMSC · IGP"]
        Repo["EarthquakeRepository<br/>(Ktor Client)"]
        UseCase["Use Case'ler<br/>GetEarthquakes · CalculateStatistics"]
        VM["MainViewModel<br/>(StateFlow)"]
        DI["Koin DI"]
        Store["DataStore<br/>(tercihler)"]

        DTO --> Repo --> UseCase --> VM
        Store --> VM
        DI -.-> Repo & UseCase & VM & Store
    end

    subgraph android["🤖 androidMain"]
        AndroidUI["Jetpack Compose Ekranları<br/>Home · Map · Settings · Detail"]
        Maps["Google Maps"]
        Ads["AdMob"]
        FCM["Firebase Messaging"]
    end

    subgraph ios["🍎 iosMain"]
        SwiftUI["SwiftUI (iskelet)"]
        CoreLoc["CoreLocation"]
    end

    subgraph web["🌐 wasmJsMain"]
        Dashboard["Web Dashboard<br/>(responsive, hover destekli)"]
        AdSense["Google AdSense"]
    end

    VM --> AndroidUI
    VM --> SwiftUI
    VM --> Dashboard

    style common fill:#EBE3D5,stroke:#113D62,color:#111827
    style android fill:#DDEBFF,stroke:#2196F3,color:#111827
    style ios fill:#FFE8DD,stroke:#FF6B4A,color:#111827
    style web fill:#DFF5E1,stroke:#22C55E,color:#111827
```

### Klasör yapısı

```
composeApp/
└── src/
    ├── commonMain/     # Modeller, DTO'lar, mapper, repository (Ktor),
    │                   # use case'ler, DataStore, Koin DI, MainViewModel
    ├── androidMain/     # Jetpack Compose UI, navigation, Maps, AdMob, Firebase
    ├── iosMain/         # CoreLocation, Koin platform modülü
    └── wasmJsMain/      # Web dashboard, tarayıcı API'leri (localStorage vb.)
iosApp/                  # Xcode projesine eklenecek Swift kaynakları
```

---

## 🛠️ Teknoloji Yığını

| Katman | Teknoloji |
|---|---|
| **Dil** | [Kotlin](https://kotlinlang.org) (Multiplatform) |
| **UI** | [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) · Material 3 |
| **Ağ** | [Ktor Client](https://ktor.io/) + kotlinx.serialization |
| **DI** | [Koin](https://insert-koin.io/) |
| **Yerel Depolama** | AndroidX DataStore Preferences (Multiplatform) |
| **Harita** | Google Maps Compose |
| **Bildirim & Analitik** | Firebase (Cloud Messaging, Firestore, Analytics, Crashlytics) |
| **Reklam** | Google AdMob (mobil) · Google AdSense (web) |
| **Ödeme** | Google Play Billing |
| **Hosting (Web)** | Firebase Hosting |
| **Web Hedefi** | Kotlin/Wasm + `ComposeViewport` |

---

## 🌐 Desteklenen Diller

🇹🇷 Türkçe · 🇬🇧 English · 🇩🇪 Deutsch · 🇪🇸 Español · 🇮🇹 Italiano · 🇷🇺 Русский · 🇺🇦 Українська ·
🇬🇷 Ελληνικά · 🇰🇬 Кыргызча · 🇺🇿 Oʻzbekcha · 🇸🇦 العربية · 🇰🇷 한국어 · 🇯🇵 日本語 · 🇨🇳 中文

---

## 🚀 Başlarken

### Gereksinimler
- **Android**: Android Studio (Koala+), JDK 17
- **iOS**: Xcode 15+, Mac (bkz. [`iosApp/README.md`](iosApp/README.md))
- **Web**: JDK 17, Node.js (Kotlin/Wasm toolchain otomatik iner)

### Kurulum

```bash
git clone https://github.com/ferdidrgn/AnlikDepremlerKMP.git
cd AnlikDepremlerKMP
```

`local.properties` dosyasına kendi API anahtarlarınızı ekleyin (Google Maps, AdMob) —
`google-services.json` dosyasını da `composeApp/` altına yerleştirmeniz gerekir (Firebase Console).

### Çalıştırma

```bash
# Android
./gradlew :composeApp:assembleDebug

# Web (tarayıcıda geliştirme sunucusu)
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Web (production derleme + Firebase Hosting'e deploy)
./gradlew :composeApp:wasmJsBrowserDistribution
firebase deploy --only hosting
```

iOS için Xcode kurulumu adım adım [`iosApp/README.md`](iosApp/README.md) içinde.

---

## 📄 Lisans

Bu proje şu an özel (kapalı kaynak) bir uygulamadır — tüm hakları saklıdır. Açık kaynak
yapmak isterseniz bir `LICENSE` dosyası ekleyip bu bölümü güncelleyebilirsiniz.

---

<div align="center">

Türkiye'de yaşayan herkes için ❤️ ile geliştirildi.

**[⬆ Başa dön](#-anlık-depremler)**

</div>
