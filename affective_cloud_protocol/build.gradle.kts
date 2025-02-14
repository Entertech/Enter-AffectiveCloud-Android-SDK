plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("custom.android.plugin")
}
val libraryVersion = (rootProject.ext.get("libraryVersion") as? String) ?: "1.0.0"
val groupIdString = (rootProject.ext.get("groupId") as? String) ?: "1.0.0"
PublishInfo {
    groupId = groupIdString // 库的组织，使用域名表示
    artifactId = "affective-cloud-protocol" // 库名称
    version = "0.0.1-vr-control" // 库版本
}

android {
    namespace = "cn.entertech.affective.clound.protocol"
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
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation ("com.google.code.gson:gson:2.8.5")
    implementation("androidx.appcompat:appcompat:1.0.0")
    implementation(project(":affective_sdk_api"))
    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0")
}