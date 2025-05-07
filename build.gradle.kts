// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}
apply( "version.gradle")
buildscript {
    dependencies {
//        classpath("cn.entertech.android:publish:1.0.5")
        classpath("cn.entertech.android:publish:1.1.0-local")
    }
}
