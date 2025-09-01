#!/bin/bash

echo "🔍 CircolApp Test Diagnostics"
echo "============================="
echo

echo "📱 Project Structure:"
echo "- Main source: $(find app/src/main -name "*.kt" | wc -l) Kotlin files"
echo "- Test source: $(find app/src/androidTest -name "*.kt" | wc -l) Android test files"
echo "- Unit tests: $(find app/src/test -name "*.kt" 2>/dev/null | wc -l) unit test files"
echo

echo "🧪 Android Test Files:"
for test_file in app/src/androidTest/java/com/example/circolapp/*.kt; do
    if [ -f "$test_file" ]; then
        test_count=$(grep -c "@Test" "$test_file" 2>/dev/null || echo "0")
        echo "- $(basename "$test_file"): $test_count tests"
    fi
done
echo

echo "🔧 Build Configuration:"
if grep -q "FirebaseTestRunner" app/build.gradle.kts; then
    echo "✅ Custom test runner configured"
else
    echo "❌ Custom test runner not found"
fi

if grep -q "protobuf-javalite" app/build.gradle.kts; then
    echo "✅ Protobuf dependency managed"
else
    echo "❌ Protobuf dependency not found"
fi
echo

echo "🔥 Firebase Configuration:"
if [ -f "app/google-services.json" ]; then
    echo "✅ google-services.json present"
else
    echo "❌ google-services.json missing"
fi

if [ -f "app/src/debug/AndroidManifest.xml" ]; then
    echo "✅ Debug manifest present"
else
    echo "❌ Debug manifest missing"
fi
echo

echo "📋 Common Test Issues and Solutions:"
echo "1. Network connectivity issues:"
echo "   - Ensure internet connection is available"
echo "   - Check if corporate firewall blocks Google repositories"
echo
echo "2. Firebase initialization errors:"
echo "   - Verify google-services.json is in app/ folder"
echo "   - Check Firebase project configuration"
echo
echo "3. Protobuf conflicts:"
echo "   - Build configuration includes protobuf version management"
echo "   - Custom test runner should handle Firebase initialization"
echo
echo "4. UI test failures:"
echo "   - Tests now include error handling for missing UI elements"
echo "   - Firebase operations are wrapped in try-catch blocks"
echo
echo "🚀 To run tests manually (when network is available):"
echo "   ./gradlew connectedDebugAndroidTest"
echo
echo "📖 For detailed setup info, see: FIREBASE_TESTING_SETUP.md"