plugins {

    id("com.google.gms.google-services")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.circolapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.circolapp"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        dataBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}



dependencies{
    implementation ("androidx.camera:camera-core:1.4.2")
    implementation ("androidx.camera:camera-camera2:1.4.2")
    implementation ("androidx.camera:camera-lifecycle:1.4.2")
    implementation ("androidx.camera:camera-view:1.4.2") // Per PreviewView di CameraX
    implementation ("com.google.mlkit:barcode-scanning:17.3.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation ("androidx.recyclerview:recyclerview:1.4.0")
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.2")
    implementation ("androidx.fragment:fragment-ktx:1.8.8")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.9.2")
    implementation ("com.firebaseui:firebase-ui-auth:8.0.2")
    implementation ("com.google.firebase:firebase-auth-ktx")
    implementation(platform("com.google.firebase:firebase-bom:33.16.0")) // Controlla la versione più recente!
    implementation("com.google.firebase:firebase-firestore")
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation (libs.material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}