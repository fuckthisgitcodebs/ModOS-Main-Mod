plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("androidx.room") version "2.6.1"
    kotlin("kapt")
}

android {
    namespace = "com.mod.os.recents"
    compileSdk = 35

    defaultConfig {
        minSdk = 34
        targetSdk = 35
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Paging 3 for archive view
    implementation("androidx.paging:paging-runtime:3.3.0")
    implementation("androidx.paging:paging-compose:3.3.0")

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Coil for image loading (card previews, future)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Accompanist for blur fallback
    implementation("com.google.accompanist:accompanist-blur:0.34.0")
}
