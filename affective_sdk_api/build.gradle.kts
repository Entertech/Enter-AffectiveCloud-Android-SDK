plugins {
    id("enter.android.library")
}
val libraryVersion = (rootProject.ext.get("libraryVersion") as? String) ?: "1.0.0"
val apiGroupId = "cn.entertech.android"
PublishInfo {
    groupId = apiGroupId // 库的组织，使用域名表示
    artifactId = "affective_sdk_api" // 库名称
    version = libraryVersion // 库版本
}


android {
    namespace = "cn.entertech.affective.sdk.api"
}

dependencies {
    api (libs.log.api)
    implementation(libs.androidx.core.ktx.v160)
    testImplementation(libs.junit)
    implementation(libs.gson.v2101)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    api(libs.device)
    api(libs.kotlinx.coroutines.core)
}
