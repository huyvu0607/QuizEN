plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinKapt) apply false
    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.5.0" apply false
}