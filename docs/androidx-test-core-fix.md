# Fix for androidx.test:core Version Conflict

## Problem
The build was failing with a version conflict error:
```
Cannot find a version of 'androidx.test:core' that satisfies the version constraints:
   Dependency path 'CircolApp:app:unspecified' --> 'androidx.test:core:1.6.1'
   Constraint path 'CircolApp:app:unspecified' --> 'androidx.test:core:{strictly 1.5.0}' because of the following reason: version resolved in configuration ':app:debugRuntimeClasspath' by consistent resolution
```

## Root Cause
1. Multiple test dependencies (espresso-core, espresso-intents, junit:1.2.1) required androidx.test:core:1.6.1
2. Some configuration was forcing it to version 1.5.0 via "consistent resolution"
3. Configuration cache was causing issues with databinding serialization
4. Plugin versions were inconsistent between build files and version catalog

## Solution Applied
The fix uses a multi-layered approach to ensure version consistency:

### 1. Gradle Configuration Changes (gradle.properties)
- Disabled `org.gradle.configuration-cache=true` to avoid databinding serialization issues
- Disabled `android.dependency.useCompileClasspathVersions=true` to prevent forced version resolution

### 2. Plugin Version Alignment (build.gradle.kts)
- Changed AGP from 8.12.2 to 8.5.0 (matching libs.versions.toml)
- Changed Kotlin from 2.1.0 to 1.9.20 (matching libs.versions.toml)

### 3. Dependency Resolution Strategy (app/build.gradle.kts)
Added resolution strategy to force androidx.test:core to version 1.6.1:
```kotlin
configurations.all {
    resolutionStrategy {
        force("androidx.test:core:1.6.1")
    }
}
```

### 4. Dependency Constraints (app/build.gradle.kts)
Added explicit constraints for all test dependencies:
```kotlin
dependencies {
    constraints {
        implementation("androidx.test:core:1.6.1")
        androidTestImplementation("androidx.test:core:1.6.1")
        androidTestImplementation("androidx.test.ext:junit:1.2.1")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
        androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1")
        androidTestImplementation("androidx.test.espresso:espresso-contrib:3.6.1")
    }
    // ... rest of dependencies
}
```

## Testing the Fix
Run the following command to verify the fix:
```bash
./gradlew app:dependencies --configuration debugAndroidTestRuntimeClasspath
```

The androidx.test:core dependency should now resolve to version 1.6.1 consistently.

## Future Considerations
1. Re-enable configuration cache once verified that the issue is resolved
2. Consider using a test BOM (Bill of Materials) for better dependency management
3. Monitor for any new version conflicts when updating test dependencies

## Files Modified
- `gradle.properties` - Disabled problematic configuration flags
- `build.gradle.kts` - Aligned plugin versions
- `app/build.gradle.kts` - Added resolution strategy and constraints