#!/bin/bash

# Script to validate that the androidx.test:monitor dependency conflict is resolved

echo "=== Validating AndroidTest Dependencies ==="
echo ""

echo "1. Checking for dependency conflicts..."
./gradlew app:dependencies --configuration androidTestRuntimeClasspath | grep -E "(androidx\.test:monitor|FAILED|conflict)"

echo ""
echo "2. Attempting to resolve androidTestRuntimeClasspath..."
./gradlew app:dependencies --configuration androidTestRuntimeClasspath > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "✅ AndroidTest dependencies resolved successfully!"
    echo ""
    echo "3. Checking androidx.test:monitor resolution..."
    ./gradlew app:dependencies --configuration androidTestRuntimeClasspath | grep "androidx.test:monitor"
else
    echo "❌ AndroidTest dependencies failed to resolve"
    echo "Running with --info for more details..."
    ./gradlew app:dependencies --configuration androidTestRuntimeClasspath --info
fi

echo ""
echo "4. Attempting to run androidTest compilation check..."
./gradlew app:compileDebugAndroidTestSources

if [ $? -eq 0 ]; then
    echo "✅ AndroidTest sources compiled successfully!"
else
    echo "❌ AndroidTest compilation failed"
fi

echo ""
echo "=== Validation Complete ==="