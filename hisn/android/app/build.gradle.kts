plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.hisn.receiver"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.hisn.receiver"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "0.3.0"
    }
}
