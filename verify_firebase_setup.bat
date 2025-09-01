@echo off
setlocal enabledelayedexpansion

echo 🔥 Verifica Configurazione Firebase Testing
echo ==========================================

REM Verifica file di configurazione Firebase
if exist "app\google-services.json" (
    echo ✅ google-services.json presente
) else (
    echo ⚠️  google-services.json mancante - necessario per Firebase
)

REM Verifica file di test
echo.
echo 📱 File di test Firebase:
if exist "app\src\androidTest\java\com\example\circolapp\FirebaseTestConfig.kt" (
    echo ✅ FirebaseTestConfig.kt
) else (
    echo ❌ FirebaseTestConfig.kt mancante
)

if exist "app\src\androidTest\java\com\example\circolapp\FirebaseTestRunner.kt" (
    echo ✅ FirebaseTestRunner.kt
) else (
    echo ❌ FirebaseTestRunner.kt mancante
)

if exist "app\src\androidTest\java\com\example\circolapp\FirestoreTestHelper.kt" (
    echo ✅ FirestoreTestHelper.kt
) else (
    echo ❌ FirestoreTestHelper.kt mancante
)

if exist "app\src\androidTest\java\com\example\circolapp\FirebaseIntegrationTest.kt" (
    echo ✅ FirebaseIntegrationTest.kt
) else (
    echo ❌ FirebaseIntegrationTest.kt mancante
)

REM Verifica configurazione build
echo.
echo 🔧 Configurazione Build:
findstr /c:"FirebaseTestRunner" "app\build.gradle.kts" >nul 2>&1
if !errorlevel! equ 0 (
    echo ✅ Test runner personalizzato configurato
) else (
    echo ❌ Test runner personalizzato non configurato
)

findstr /c:"protobuf-javalite" "app\build.gradle.kts" >nul 2>&1
if !errorlevel! equ 0 (
    echo ✅ Gestione protobuf configurata
) else (
    echo ❌ Gestione protobuf non configurata
)

REM Verifica manifest debug
if exist "app\src\debug\AndroidManifest.xml" (
    echo ✅ Debug manifest presente
) else (
    echo ❌ Debug manifest mancante
)

echo.
echo 🚀 Per eseguire i test:
echo    gradlew connectedAndroidTest
echo.
echo 📖 Per maggiori dettagli, vedi: FIREBASE_TESTING_SETUP.md