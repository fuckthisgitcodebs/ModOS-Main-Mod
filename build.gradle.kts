plugins {
    id("com.android.application") version "8.5.0" apply false
    id("com.android.library") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("com.google.dagger.hilt.android") version "2.51" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false
    kotlin("kapt") version "2.0.20" apply false
    id("androidx.room") version "2.6.1" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
