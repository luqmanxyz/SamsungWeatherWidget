plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.samsungweatherwidget"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.samsungweatherwidget"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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
}
