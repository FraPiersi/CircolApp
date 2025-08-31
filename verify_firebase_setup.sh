#!/bin/bash

echo "🔥 Verifica Configurazione Firebase Testing"
echo "=========================================="

# Verifica file di configurazione Firebase
if [ -f "app/google-services.json" ]; then
    echo "✅ google-services.json presente"
else
    echo "⚠️  google-services.json mancante - necessario per Firebase"
fi

# Verifica file di test
echo ""
echo "📱 File di test Firebase:"
if [ -f "app/src/androidTest/java/com/example/circolapp/FirebaseTestConfig.kt" ]; then
    echo "✅ FirebaseTestConfig.kt"
else
    echo "❌ FirebaseTestConfig.kt mancante"
fi

if [ -f "app/src/androidTest/java/com/example/circolapp/FirebaseTestRunner.kt" ]; then
    echo "✅ FirebaseTestRunner.kt"
else
    echo "❌ FirebaseTestRunner.kt mancante"
fi

if [ -f "app/src/androidTest/java/com/example/circolapp/FirestoreTestHelper.kt" ]; then
    echo "✅ FirestoreTestHelper.kt"
else
    echo "❌ FirestoreTestHelper.kt mancante"
fi

if [ -f "app/src/androidTest/java/com/example/circolapp/FirebaseIntegrationTest.kt" ]; then
    echo "✅ FirebaseIntegrationTest.kt"
else
    echo "❌ FirebaseIntegrationTest.kt mancante"
fi

# Verifica configurazione build
echo ""
echo "🔧 Configurazione Build:"
if grep -q "FirebaseTestRunner" app/build.gradle.kts; then
    echo "✅ Test runner personalizzato configurato"
else
    echo "❌ Test runner personalizzato non configurato"
fi

if grep -q "protobuf-javalite" app/build.gradle.kts; then
    echo "✅ Gestione protobuf configurata"
else
    echo "❌ Gestione protobuf non configurata"
fi

# Verifica manifest debug
if [ -f "app/src/debug/AndroidManifest.xml" ]; then
    echo "✅ Debug manifest presente"
else
    echo "❌ Debug manifest mancante"
fi

echo ""
echo "🚀 Per eseguire i test:"
echo "   ./gradlew connectedAndroidTest"
echo ""
echo "📖 Per maggiori dettagli, vedi: FIREBASE_TESTING_SETUP.md"