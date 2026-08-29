# ===================================================================
# Deprem & Veri Modellerini Koruma
# ===================================================================
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Veri modellerinin paket isimlerini koru
-keep class com.ferdidrgn.anlikdepremler.data.** { *; }

# ===================================================================
# Network (Retrofit, OkHttp, Gson)
# ===================================================================
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# ===================================================================
# Jetpack Compose & Navigation
# ===================================================================
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ===================================================================
# Dagger Hilt
# ===================================================================
-keep class * extends androidx.lifecycle.ViewModel
-keep class *_Factory { *; }
-keep class *_MembersInjector { *; }

# ===================================================================
# Dagger Hilt
# ===================================================================
-keep class * extends androidx.lifecycle.ViewModel
-keep class *_Factory { *; }
-keep class *_MembersInjector { *; }

-keep class dagger.hilt.** { *; }
-keep class com.google.dagger.** { *; }
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}
-dontwarn dagger.hilt.**

# ===================================================================
# Firebase, Google Play & AdMob
# ===================================================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ===================================================================
# Google Maps Compose & Clustering
# ===================================================================
-keep class com.google.maps.android.** { *; }
-keep class com.google.android.gms.maps.** { *; }
-dontwarn com.google.maps.android.**

# ===================================================================
# Coroutines & DataStore & Startup
# ===================================================================
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
-keep class androidx.startup.** { *; }
-dontwarn androidx.**