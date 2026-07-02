plugins {
    `kotlin-dsl`
}

group = "cn.entertech.buildlogic"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "enter.android.library"
            implementationClass = "EnterAndroidLibraryConventionPlugin"
        }
    }
}
