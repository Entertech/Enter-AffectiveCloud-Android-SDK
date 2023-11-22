plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("custom.android.plugin")
}

PublishInfo {
    groupId = "cn.entertech.android" // 库的组织，使用域名表示
    artifactId = "affective_sdk_api" // 库名称
    version = "1.1.4" // 库版本
}


android {
    namespace = "cn.entertech.affective.sdk.api"
    compileSdk = 32

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
        jvmTarget = "11"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.6.0")
    testImplementation("junit:junit:4.13.2")
    implementation("com.google.code.gson:gson:2.8.5")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}