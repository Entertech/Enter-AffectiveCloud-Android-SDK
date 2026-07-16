plugins {
    id("enter.android.library")
}
val affectiveDataProviderVersion = (rootProject.ext.get("affectiveDataProviderVersion") as? String) ?: "1.0.0"
val dataProviderGroupId = "cn.entertech.android"
PublishInfo {
    groupId = dataProviderGroupId // 库的组织，使用域名表示
    artifactId = "affective-data-provider" // 库名称
    version = affectiveDataProviderVersion // 库版本
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
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.junit.v111)
    androidTestImplementation(libs.androidx.espresso.core.v310)
}
