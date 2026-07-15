plugins {
    id("enter.android.application")
}

android {
    signingConfigs {
        create("config") {
            keyAlias = "entertech"
            keyPassword = "123456"
            storeFile = file("../entertech.jks")
            storePassword = "123456"
        }
    }
    namespace = "cn.entertech.biomoduledemo"
    defaultConfig {
        applicationId = "cn.entertech.biomoduledemo"
        versionCode = 9
        versionName = "1.8.1"
        missingDimensionStrategy("project", "flowtime")
        missingDimensionStrategy("authentication", "noAuth")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            if (file("../entertech.jks").exists()) {
                signingConfig = signingConfigs.getByName("config")
            }
        }
    }
    packaging {
        resources.excludes += "META-INF/ble_release.kotlin_module"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout.v113)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.material.v100)
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.biomoduleble)
    implementation(libs.gson.v2101)
    implementation(libs.java.websocket)
    implementation(project(":affective_cloud_sdk"))
    implementation(project(":affective_sdk_api"))
    implementation(libs.crashreport)
    implementation(project(":affective_local_sdk"))
    implementation(project(":affective_sdk_manager"))
    implementation(project(":affective_cloud_protocol"))
    implementation(project(":affective_data_provider"))
}
