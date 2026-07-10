plugins {
    id("enter.android.library")
}
val libraryVersion = (rootProject.ext.get("libraryVersion") as? String) ?: "1.0.0"
val managerGroupId = "cn.entertech.android"
PublishInfo {
    groupId = managerGroupId // 库的组织，使用域名表示
    artifactId = "affective-sdk-manager" // 库名称
    version = libraryVersion // 库版本
}

android {
    namespace = "cn.entertech.affective.sdk.manager"

    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation (libs.base)
    implementation(libs.androidx.core.ktx.v1131)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.kotlinx.coroutines.core)
    api(project(":affective_sdk_api"))
//    api(libs.affective.sdk.api)
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
