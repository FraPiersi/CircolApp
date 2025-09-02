## Fix for AndroidX Test NoClassDefFoundError

### Problem Description
Tests were failing with the following error:
```
java.lang.NoClassDefFoundError: Failed resolution of: Landroidx/test/platform/concurrent/DirectExecutor;
at androidx.test.espresso.InteractionResultsHandler.gatherAnyResult(InteractionResultsHandler.java:52)
at androidx.test.espresso.ViewInteraction.waitForAndHandleInteractionResults(ViewInteraction.java:383)
at androidx.test.espresso.ViewInteraction.desugaredPerform(ViewInteraction.java:212)
at androidx.test.espresso.ViewInteraction.perform(ViewInteraction.java:140)
at com.example.circolapp.AppTest.testNavigationFromLoginToRegister(AppTest.kt:102)
```

### Root Cause
The `androidx.test:core` library was missing from the androidTest dependencies. This library contains the `DirectExecutor` class that Espresso's `InteractionResultsHandler` requires for managing UI test interactions.

### Solution
Added the missing `androidx.test:core:1.6.1` dependency:

#### Changes to `gradle/libs.versions.toml`
```toml
# Added to [versions] section
testCore = "1.6.1"

# Added to [libraries] section  
androidx-test-core = { group = "androidx.test", name = "core", version.ref = "testCore" }
```

#### Changes to `app/build.gradle.kts`
```kotlin
// Added to androidTest dependencies
androidTestImplementation(libs.androidx.test.core)
```

### Why This Fixes The Issue
- `androidx.test:core` provides essential AndroidX testing utilities including `DirectExecutor`
- Espresso relies on this class for handling asynchronous UI interactions
- Without this dependency, the class cannot be found at runtime, causing the `NoClassDefFoundError`
- This is a standard dependency that should be included in any AndroidX testing setup

### Impact
This fix resolves the test failure while maintaining compatibility with all existing test code. No changes were needed to the test files themselves - only the missing dependency was added.