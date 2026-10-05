plugins {
    alias(libs.plugins.android.application)
}

android {
    // Paquete de la app. Reemplaza al atributo package="" del AndroidManifest.xml
    namespace = "com.santo_tomas.cuidapp"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.santo_tomas.cuidapp"
        minSdk = 31      // Android 12 o superior
        targetSdk = 36   // Android 16
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
    implementation(libs.appcompat)          // AppCompatActivity
    implementation(libs.material)           // MaterialButton, MaterialCardView, BottomNavigationView, Material3
    implementation(libs.activity)           // Activity Result API (registerForActivityResult)
    implementation(libs.constraintlayout)   // ConstraintLayout y Flow
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}