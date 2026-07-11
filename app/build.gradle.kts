import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.ryosoftware.caffeine_tile"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.ryosoftware.caffeine_tile"
        minSdk = 30

        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String",
            "TAG",
            "\"caffeine_tile\""
        )
    }

    buildFeatures {
        buildConfig = true
        resValues = true
        viewBinding = true
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
}
