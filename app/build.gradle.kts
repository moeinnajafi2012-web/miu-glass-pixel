plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.mui.glasslauncher"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mui.glasslauncher"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}
