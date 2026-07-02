plugins {
    id("enter.android.library")
}
val libraryVersion = (rootProject.ext.get("libraryVersion") as? String) ?: "1.0.0"
val localSdkGroupId = "cn.entertech.android"
val baseArtifactId = "affective-offline-sdk"
val projectFlavors = listOf("breath", "obsession", "flowtime", "sdk")
val authenticationFlavors = mapOf("auth" to true, "noAuth" to false)

PublishInfo {
    groupId = localSdkGroupId // 库的组织，使用域名表示
    artifactId = baseArtifactId // 库名称，发布插件按 product 前缀和 authentication 后缀生成变体 artifactId
    version = libraryVersion  // 库版本
}

android {
    namespace = "cn.entertech.affectivesdk"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        externalNativeBuild {
            cmake {
                cppFlags("-std=c++11 -fexceptions")
//                cppFlags "-std=c++11 -frtti -fexceptions -Who-format"
            }
        }
    }
    flavorDimensions += listOf("project", "authentication")
    productFlavors {
        projectFlavors.forEach { projectFlavor ->
            create(projectFlavor) {
                dimension = "project"
                buildConfigField("String", "AFFECTIVE_PROJECT", "\"$projectFlavor\"")
            }
        }
        authenticationFlavors.forEach { (authenticationFlavor, requireAuthentication) ->
            create(authenticationFlavor) {
                dimension = "authentication"
                buildConfigField("boolean", "REQUIRE_AUTHENTICATION", requireAuthentication.toString())
            }
        }
    }
    externalNativeBuild {
        cmake {
            path("CMakeLists.txt")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx.v190)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.base.v101demo)
    api(project(mapOf("path" to ":affective_sdk_api")))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.powermock.api.mockito)
    testImplementation(libs.powermock.module.junit4)
    implementation(libs.gson.v289)
    implementation(libs.conscrypt.android)
    implementation(libs.bcprov.jdk18on)
    implementation(libs.bcpkix.jdk18on)
    implementation(libs.device)
}
