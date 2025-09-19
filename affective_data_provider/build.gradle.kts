plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("custom.android.plugin")
}
val libraryVersion = (rootProject.ext.get("libraryVersion") as? String) ?: "1.0.0"
val groupIdString = (rootProject.ext.get("groupId") as? String) ?: "1.0.0"
PublishInfo {
    groupId = groupIdString // 库的组织，使用域名表示
    artifactId = "affective-data-provider" // 库名称
    version = libraryVersion // 库版本
}
android {
    namespace = "cn.entertech.affective.data.provider"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

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
//    api(libs.affective.sdk.api)
    api(project(":affective_sdk_api"))
    implementation(libs.androidx.core.ktx.v1131)
    implementation(libs.androidx.appcompat.v100)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v111)
    androidTestImplementation(libs.androidx.espresso.core.v310)
}