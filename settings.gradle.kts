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


        maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlinx/maven") }
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-coroutines/maven") }


    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // <-- Add this line
        maven { url = uri("https://artifactory.appodeal.com/appodeal-public/") }

        maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlinx/maven") }
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-coroutines/maven") }
    }
}


rootProject.name = "LetsLink"
include(":app")
