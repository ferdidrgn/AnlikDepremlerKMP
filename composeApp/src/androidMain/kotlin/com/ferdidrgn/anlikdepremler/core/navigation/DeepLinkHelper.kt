package com.ferdidrgn.anlikdepremler.core.navigation

import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink

object DeepLinkHelper {

    const val BASE_URI = "https://anlikdeprem.web.app"
    const val APP_URI = "anlikdepremler://app"

    val earthquakeDetailDeepLink: List<NavDeepLink> = listOf(
        navDeepLink { uriPattern = "$BASE_URI/earthquake/{earthquakeId}" },
        navDeepLink { uriPattern = "$APP_URI/earthquake/{earthquakeId}" }
    )

    val mapDeepLink: List<NavDeepLink> = listOf(
        navDeepLink { uriPattern = "$BASE_URI/map" },
        navDeepLink { uriPattern = "$APP_URI/map" }
    )

    fun earthquakeUri(earthquakeId: String): String = "$APP_URI/earthquake/$earthquakeId"
}
