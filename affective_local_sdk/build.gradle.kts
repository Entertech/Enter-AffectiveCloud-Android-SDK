plugins {
    id("enter.android.library")
}
val libraryVersion = (rootProject.ext.get("libraryVersion") as? String) ?: "1.0.0"
val localSdkGroupId = "cn.entertech.android"
val baseArtifactId = "affective-offline-sdk"
val projectFlavors = listOf("breath", "obsession", "flowtime", "sdk")
val authenticationFlavors = mapOf("auth" to true, "noAuth" to false)

fun buildLocalSdkArtifactId(
    project: String,
    requireAuthentication: Boolean,
): String {
    val projectPrefix = project.takeIf { it.isNotBlank() }?.let { "$it-" } ?: ""
    val authenticationSuffix = if (requireAuthentication) "-authentication" else ""
    return "$projectPrefix$baseArtifactId$authenticationSuffix"
}

PublishInfo {
    groupId = localSdkGroupId // 库的组织，使用域名表示
    artifactId = baseArtifactId // 库名称，作为单 publication 或动态规则缺省值
    version = libraryVersion  // 库版本

    artifactIdForVariant { variant ->
        buildLocalSdkArtifactId(
            project = variant.flavor("project"),
            requireAuthentication = variant.flavor("authentication") == "auth",
        )
    }
}

android {
    namespace = "cn.entertech.affectivesdk"
    ndkVersion = "28.2.13676358"

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
    testImplementation(kotlin("test"))
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
