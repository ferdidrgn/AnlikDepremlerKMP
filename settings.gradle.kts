pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    // PREFER_SETTINGS (removed) made Gradle ignore repositories any plugin adds at the project
    // level entirely - that's exactly how Kotlin's Binaryen plugin registers its real download
    // source (GitHub Releases), so it was being silently skipped during dependency resolution,
    // even though these two repositories cover every other dependency this project has.
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AnlikDepremler"
include(":composeApp")