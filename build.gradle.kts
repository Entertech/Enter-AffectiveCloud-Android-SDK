// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.18.1"
    id("enter.module-dependency-analysis")
}

apiValidation {
    ignoredProjects.addAll(
        listOf(
            "demo",
            "affective_cloud_sdk",
            "affective_cloud_protocol",
            "affective_data_provider",
            "affective_local_sdk",
            "affective_sdk_manager",
            "legacy-consumer",
        ),
    )
}
apply( "version.gradle")
buildscript {
    dependencies {
        classpath("cn.entertech.android:publish:1.2.1")
    }
}
