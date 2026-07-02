plugins {
    id("enter.android.library")
}
val libraryVersion = (rootProject.ext.get("libraryVersion") as? String) ?: "1.0.0"
val dataProviderGroupId = "cn.entertech.android"
PublishInfo {
    groupId = dataProviderGroupId // 库的组织，使用域名表示
    artifactId = "affective-data-provider" // 库名称
    version = libraryVersion // 库版本
}
android {
    namespace = "cn.entertech.affective.data.provider"

    defaultConfig {
        minSdk = 24
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
