import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("custom.android.plugin")
}
val libraryVersion = (rootProject.ext.get("libraryVersion") as? String) ?: "1.0.0"
val groupIdString = (rootProject.ext.get("groupId") as? String) ?: "1.0.0"
val baseArtifactId = "affective-offline-sdk"
val projectFlavors = listOf("breath", "obsession", "flowtime", "sdk")
val authenticationFlavors = listOf("auth", "noAuth")

data class ReleaseFlavorPublication(
    val variantName: String,
    val publicationName: String,
    val artifactId: String
)

fun String.uppercaseFirstChar(): String = replaceFirstChar { char ->
    if (char.isLowerCase()) char.titlecase() else char.toString()
}

val releaseFlavorPublications = projectFlavors.flatMap { projectFlavor ->
    authenticationFlavors.map { authenticationFlavor ->
        val variantName = "${projectFlavor}${authenticationFlavor.uppercaseFirstChar()}Release"
        val projectArtifactPart = if (projectFlavor == "sdk") "" else "-$projectFlavor"
        val authenticationArtifactPart = if (authenticationFlavor == "auth") "-authentication" else ""

        ReleaseFlavorPublication(
            variantName = variantName,
            publicationName = variantName,
            artifactId = "$baseArtifactId$projectArtifactPart$authenticationArtifactPart"
        )
    }
}

PublishInfo {
    groupId = groupIdString // 库的组织，使用域名表示
    artifactId = baseArtifactId // 库名称
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
        create("auth") {
            dimension = "authentication"
            buildConfigField("boolean", "REQUIRE_AUTHENTICATION", "true")
        }
        create("noAuth") {
            dimension = "authentication"
            buildConfigField("boolean", "REQUIRE_AUTHENTICATION", "false")
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

afterEvaluate {
    extensions.configure(PublishingExtension::class.java) {
        publications {
            releaseFlavorPublications.forEach { publication ->
                create(publication.publicationName, MavenPublication::class.java) {
                    from(project.components[publication.variantName])
                    groupId = groupIdString
                    artifactId = publication.artifactId
                    version = libraryVersion
                }
            }
        }
    }
}
