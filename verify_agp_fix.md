# Android Gradle Plugin Version Fix

## Problem
The project was experiencing AAR metadata errors:
1. `Dependency 'androidx.core:core:1.16.0' requires Android Gradle plugin 8.6.0 or higher`
2. `Dependency 'androidx.core:core-ktx:1.16.0' requires Android Gradle plugin 8.6.0 or higher`

The project was using Android Gradle plugin 8.3.2, which is below the required minimum.

## Root Cause
- `androidx.core:core:1.16.0` and `androidx.core:core-ktx:1.16.0` require AGP 8.6.0+
- Project was using AGP 8.3.2 in `build.gradle.kts`
- Version catalog `libs.versions.toml` had AGP 8.5.0 but was inconsistent with root build file

## Solution Applied
Updated Android Gradle Plugin to version 8.6.1 in both locations:
1. `build.gradle.kts`: `classpath("com.android.tools.build:gradle:8.6.1")`
2. `gradle/libs.versions.toml`: `agp = "8.6.1"`

## Verification
- AGP 8.6.1 meets the minimum requirement of 8.6.0+ for androidx.core:1.16.0
- AGP 8.6.1 is a stable release compatible with Kotlin 1.9.20 and Gradle 8.13
- Change is minimal and surgical - only version numbers updated

## Dependencies Using Version Catalog
The project uses `libs.androidx.core.ktx` which resolves to androidx.core:core-ktx:1.16.0.
This will now work with AGP 8.6.1 without AAR metadata errors.