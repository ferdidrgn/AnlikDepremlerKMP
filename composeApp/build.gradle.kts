import java.io.FileInputStream
import java.util.Properties
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(FileInputStream(file))
}

fun secret(key: String, default: String = ""): String =
    localProperties.getProperty(key, default)

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentnegotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)

            implementation(libs.koin.core)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.androidx.datastore.core)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.okio)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.splashscreen)
            implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
            implementation("androidx.activity:activity-compose:1.9.3")

            implementation(libs.material)

            implementation("androidx.navigation:navigation-compose:2.8.4")
            implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
            implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

            // Firebase BoM (Doğru ve tam paket adlarıyla)
            implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
            implementation("com.google.firebase:firebase-firestore-ktx")
            implementation("com.google.firebase:firebase-messaging-ktx")
            implementation("com.google.firebase:firebase-analytics-ktx")
            implementation("com.google.firebase:firebase-crashlytics-ktx")

            implementation("com.android.billingclient:billing-ktx:8.0.0")
            implementation("com.google.android.gms:play-services-ads:23.6.0")

            implementation("com.google.android.play:review:2.0.2")
            implementation("com.google.android.play:review-ktx:2.0.2")

            implementation(libs.ktor.client.okhttp)

            implementation("io.coil-kt:coil-compose:2.7.0")

            implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")

            implementation("com.google.maps.android:maps-compose:6.2.1")
            implementation("com.google.maps.android:maps-compose-utils:6.2.1")
            implementation("com.google.android.gms:play-services-maps:19.0.0")
            implementation("com.google.android.gms:play-services-location:21.3.0")

            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            implementation("androidx.glance:glance-appwidget:1.1.1")
            implementation("androidx.work:work-runtime-ktx:2.10.0")
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }

        androidUnitTest.dependencies {
            implementation(libs.junit)
        }

        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.junit)
            implementation(libs.androidx.espresso.core)
        }
    }
}

android {
    namespace = "com.ferdidrgn.anlikdepremler"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ferdidrgn.anlikdepremler"
        minSdk = 24
        targetSdk = 36
        versionCode = 38
        versionName = "1.38"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
        resourceConfigurations.addAll(
            listOf("tr", "en", "de", "es", "it", "ru", "uk", "el", "ky", "uz", "ar", "ko", "ja", "zh")
        )

        manifestPlaceholders["API_KEY_ADMOB"] = secret("API_KEY_ADMOB")
        manifestPlaceholders["API_KEY_LOCATION"] = secret("API_KEY_LOCATION")
    }

    bundle {
        language.enableSplit = false
        density.enableSplit = true
        abi.enableSplit = true
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            buildConfigField("String", "ADMOB_APP_OPEN_ID", "\"ca-app-pub-3940256099942544/9257395921\"")
            buildConfigField("String", "ADMOB_BANNER_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "ADMOB_NATIVE_ID", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "ADMOB_REWARDED_ID", "\"ca-app-pub-3940256099942544/5224354917\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val appOpenId = secret("ADMOB_APP_OPEN_ID")
            val bannerId = secret("ADMOB_BANNER_ID")
            val interstitialId = secret("ADMOB_INTERSTITIAL_ID")
            val nativeId = secret("ADMOB_NATIVE_ID")
            val rewardedId = secret("ADMOB_REWARDED_ID")

            buildConfigField("String", "ADMOB_APP_OPEN_ID", "\"$appOpenId\"")
            buildConfigField("String", "ADMOB_BANNER_ID", "\"$bannerId\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$interstitialId\"")
            buildConfigField("String", "ADMOB_NATIVE_ID", "\"$nativeId\"")
            buildConfigField("String", "ADMOB_REWARDED_ID", "\"$rewardedId\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

compose {
    resources {
        packageOfResClass = "com.ferdidrgn.anlikdepremler.resources"
    }
}
