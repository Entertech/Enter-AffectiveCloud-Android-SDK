plugins {
    id("enter.android.library")
}

val affectiveCloudSdkVersion = (rootProject.ext.get("affectiveCloudSdkVersion") as? String) ?: "1.0.0"
val groupIdString = (rootProject.ext.get("groupId") as? String) ?: ""

PublishInfo {
    groupId = groupIdString // 库的组织，使用域名表示
    artifactId = "affective_cloud_sdk" // 库名称
    version = affectiveCloudSdkVersion // 库版本
}

android {
    namespace = "cn.entertech.android.affective_cloud_sdk"
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    if (name.endsWith("UnitTestKotlin")) {
        // These historical fixtures require live credentials/network and an obsolete PowerMock setup.
        exclude(
            "cn/entertech/affectivecloudsdk/EnterAffectiveCloudApiTest.kt",
            "cn/entertech/affectivecloudsdk/EnterAffectiveCloudManagerTest.kt",
            "cn/entertech/affectivecloudsdk/EnterAffectiveCloudManagerTestMultichannel.kt",
        )
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.androidx.appcompat)
    implementation(project(":affective_sdk_api"))
    implementation(project(":affective_cloud_protocol"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.gson.v285)
    implementation(libs.java.websocket)
    implementation(libs.base)
}
