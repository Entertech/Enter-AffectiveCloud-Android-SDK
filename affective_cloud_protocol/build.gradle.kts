plugins {
    id("enter.android.library")
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
