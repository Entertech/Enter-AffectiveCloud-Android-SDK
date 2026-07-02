plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("custom.android.plugin")
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
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++11 -fexceptions")
//                cppFlags "-std=c++11 -frtti -fexceptions -Who-format"
            }
        }
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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
