package com.ferdidrgn.anlikdepremler.core.navigation

import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink

object DeepLinkHelper {

    const val BASE_URI = "https://anlikdepremler.com"
    const val DEEPLINK_SCHEME = "anlikdepremler://"

    val earthquakeDetailDeepLink: List<NavDeepLink> = listOf(
        navDeepLink { uriPattern = "$BASE_URI/earthquake/{earthquakeId}" },
        navDeepLink { uriPattern = "$DEEPLINK_SCHEME/earthquake/{earthquakeId}" }
    )
}