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
    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "enter.android.application"
            implementationClass = "EnterAndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "enter.android.library"
            implementationClass = "EnterAndroidLibraryConventionPlugin"
        }
        register("moduleDependencyAnalysis") {
            id = "enter.module-dependency-analysis"
            implementationClass = "ModuleDependencyAnalysisPlugin"
        }
    }
}
