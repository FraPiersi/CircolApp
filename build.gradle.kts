// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
        classpath("com.google.gms:google-services:4.4.3")
        val nav_version = "2.7.7" // Usa l'ultima versione di Navigation
        classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
    }
}
