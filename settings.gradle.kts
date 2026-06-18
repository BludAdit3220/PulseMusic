@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("androidx")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://central.sonatype.com/repository/maven-snapshots/") }
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://maven.aliyun.com/repository/public") }
    }
}

rootProject.name = "PulseMusic"
include(":app")
include(":innertube")
include(":kugou")
include(":lrclib")
include(":lastfm")
include(":betterlyrics")
include(":paxsenix")
include(":canvas")
include(":shazamkit")
include(":spotifycore")
include(":unison")
include(":simpmusic")
