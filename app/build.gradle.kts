plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.0" // Update this if needed
    id("com.google.gms.google-services") // Add the Google Services plugin
}

android {
    compileSdk = 34 // or latest version

    defaultConfig {
        applicationId = "com.example.mapwithmarker"
        minSdk = 27 // Your current minimum SDK
        targetSdk = 34 // or latest version
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    namespace = "com.example.mapwithmarker"
}

dependencies {
    implementation("com.google.maps.android:maps-compose:2.11.4")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.material:material:1.9.0") // Replace with latest version
    implementation("androidx.core:core:1.6.0") // Check for the latest version
    implementation(libs.coreKtx)
    implementation(libs.kotlinStdlib)
    implementation(libs.appcompat)
    implementation(libs.firebaseDatabase)
    implementation("com.google.firebase:firebase-analytics-ktx:21.2.0") // Example Firebase dependency

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidxTestExtJunit)
    androidTestImplementation(libs.espressoCore)
}

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}
