plugins {
    alias(libs.plugins.android.application)
    //Plug-in uri pentru conectarea la Firebase
    id("com.google.gms.google-services") version "4.4.2"
}

android {
    namespace = "com.example.appauthtutorialpaper"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.appauthtutorialpaper"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

   // //Aducem dependențe pentru a conecta proiectul la Firebase
   // // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
   //  TODO: Add the dependencies for Firebase products you want to use
   // // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
   // // Add the dependencies for any other desired Firebase products
   // // https://firebase.google.com/docs/android/setup#available-libraries
//
   // // // The dependencies for Firebase Authentication and Cloud Firestore
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-firestore")
}

