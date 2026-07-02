pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "Enter-AffectiveCloud-Android-SDK"
include(":demo")
//include(":affective_cloud_sdk")
include(":affective_sdk_api")
include(":affective_local_sdk")
include(":affective_sdk_manager")
include(":affective_cloud_protocol")
include(":affective_data_provider")
