plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("custom.android.plugin")
}
val libraryVersion = (rootProject.ext.get("libraryVersion") as? String) ?: "1.0.0"
val protocolGroupId = "cn.entertech.android"
PublishInfo {
    groupId = protocolGroupId // 库的组织，使用域名表示
    artifactId = "affective-cloud-protocol" // 库名称
    version = libraryVersion // 库版本
}

android {
    namespace = "cn.entertech.affective.clound.protocol"
    compileSdk = 34

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation (libs.gson.v285)
    implementation(libs.androidx.appcompat)
    api(project(":affective_sdk_api"))
//    api(libs.affective.sdk.api)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
