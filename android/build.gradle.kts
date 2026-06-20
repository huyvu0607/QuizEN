plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinKapt) apply false
    id("com.google.gms.google-services") version "4.5.0" apply false
}