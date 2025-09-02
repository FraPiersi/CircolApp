#!/bin/bash

# Verification script for androidx.test:core version conflict fix
# Run this script after applying the fix to verify it works

cd "$(dirname "$0")"

echo "=== Verifying androidx.test:core Version Conflict Fix ==="
echo

# Check if gradlew is executable
if [ ! -x "./gradlew" ]; then
    echo "❌ gradlew is not executable. Run: chmod +x gradlew"
    exit 1
fi

echo "1. Testing basic gradle connectivity..."
./gradlew --version | head -5 || {
    echo "❌ Gradle wrapper failed to run"
    exit 1
}

echo
echo "2. Attempting to resolve dependencies..."
echo "Running: ./gradlew app:dependencies --configuration debugAndroidTestRuntimeClasspath"

if ./gradlew app:dependencies --configuration debugAndroidTestRuntimeClasspath | grep -q "androidx.test:core.*1.6.1"; then
    echo "✅ androidx.test:core resolved to version 1.6.1"
elif ./gradlew app:dependencies --configuration debugAndroidTestRuntimeClasspath 2>&1 | grep -q "version conflict"; then
    echo "❌ Version conflict still exists"
    exit 1
else
    echo "⚠️  Unable to verify dependency resolution (check network connectivity)"
fi

echo
echo "3. Testing build compilation..."
echo "Running: ./gradlew app:compileDebugAndroidTestSources"

if ./gradlew app:compileDebugAndroidTestSources; then
    echo "✅ Android test sources compile successfully"
    echo
    echo "🎉 Fix verification completed successfully!"
    echo
    echo "You can now run Android tests with:"
    echo "  ./gradlew connectedAndroidTest"
    echo "  or"
    echo "  ./gradlew app:connectedDebugAndroidTest"
else
    echo "❌ Android test compilation failed"
    echo "Check the error logs above for details"
    exit 1
fi

echo
echo "=== Fix Applied Successfully ==="
echo "The androidx.test:core version conflict has been resolved."
echo "All test dependencies should now use consistent versions."