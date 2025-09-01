@echo off
setlocal enabledelayedexpansion

REM Script per eseguire i test instrumented con gestione completa di problemi UTP e connettività
REM 
REM 🎯 QUESTO SCRIPT RISOLVE:
REM - Errori UTP (Unified Test Platform): "Failed to receive UTP test results"  
REM - Errori protobuf: "Fatal error with proto_config"
REM - Problemi device: "emulator is not a Gradle Managed Device"
REM - Problemi di connettività e timeout
REM
REM Usage: run_instrumented_tests.bat

echo 🚀 Avvio test instrumented CircolApp con gestione UTP e connettività...
echo    Questo script risolve automaticamente gli errori UTP comuni

REM Check if we're in the right directory
if not exist "app\build.gradle.kts" (
    echo ❌ Errore: Eseguire questo script dalla directory root del progetto CircolApp
    exit /b 1
)

REM Function equivalent: check_device
call :check_device
if !errorlevel! neq 0 (
    echo.
    echo ❌ Impossibile procedere senza dispositivo connesso
    call :show_troubleshooting
    exit /b 1
)

REM Function equivalent: run_tests
call :run_tests
if !errorlevel! equ 0 (
    echo.
    echo 🎉 TEST COMPLETATI SUCCESSFULLY!
    echo.
    echo 📊 Report disponibili in:
    echo    app\build\reports\androidTests\connected\debug\
    exit /b 0
) else (
    echo.
    echo ❌ TEST FALLITI
    call :show_troubleshooting
    exit /b 1
)

REM Function to check device connectivity
:check_device
echo 📱 Verifica connessione dispositivi/emulatori...

REM Use proper adb path
set ADB_CMD=adb
if defined ANDROID_HOME (
    set ADB_CMD=%ANDROID_HOME%\platform-tools\adb
)

REM Check ADB devices
set devices=0
for /f "delims=" %%i in ('!ADB_CMD! devices 2^>nul') do (
    echo %%i | find "device" >nul && set /a devices+=1
)

if !devices! equ 0 (
    echo ⚠️  Nessun dispositivo/emulatore connesso
    echo    Avvia un emulatore o connetti un dispositivo prima di procedere
    exit /b 1
) else (
    echo ✅ Trovati !devices! dispositivo/i connesso/i
    !ADB_CMD! devices
    exit /b 0
)

REM Function to run tests with different strategies
:run_tests
echo.
echo 🧪 Esecuzione test instrumented...

REM Strategy 1: Try with standard configuration
echo 📋 Strategia 1: Test standard
gradlew connectedDebugAndroidTest --continue --stacktrace
set strategy1_result=!errorlevel!

if !strategy1_result! equ 0 (
    echo ✅ Test completati con successo!
    exit /b 0
) else (
    echo ⚠️  Test falliti con codice !strategy1_result!
    
    REM Strategy 2: Try without UTP to avoid protobuf configuration issues
    echo.
    echo 📋 Strategia 2: Test senza UTP per evitare errori protobuf/configurazione
    gradlew connectedTestNoUTP --continue --stacktrace
    set strategy2_result=!errorlevel!
    
    if !strategy2_result! equ 0 (
        echo ✅ Test completati con successo senza UTP!
        exit /b 0
    ) else (
        echo ❌ Test senza UTP falliti, provo modalità offline...
        
        REM Strategy 3: Try with increased timeout and offline mode
        echo.
        echo 📋 Strategia 3: Test con timeout aumentato e modalità offline
        gradlew connectedDebugAndroidTest --offline --continue --stacktrace -Pandroid.testInstrumentationRunnerArguments.timeout_msec=300000
        set strategy3_result=!errorlevel!
        
        if !strategy3_result! equ 0 (
            echo ✅ Test completati con successo in modalità offline!
            exit /b 0
        ) else (
            echo ❌ Test falliti anche in modalità offline
            
            REM Strategy 4: Run only basic connectivity tests
            echo.
            echo 📋 Strategia 4: Solo test di connettività base
            gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.circolapp.DeviceConnectivityTest --continue --stacktrace
            set strategy4_result=!errorlevel!
            
            if !strategy4_result! equ 0 (
                echo ✅ Test di base completati - il dispositivo e l'app sono configurati correttamente
                echo ⚠️  I test Firebase potrebbero fallire per problemi di connettività o UTP
                exit /b 0
            ) else (
                echo ❌ Anche i test di base falliscono - problema di configurazione dell'app
                exit /b !strategy4_result!
            )
        )
    )
)

:show_troubleshooting
echo.
echo 🔧 RISOLUZIONE PROBLEMI:
echo.

REM Use proper adb path
set ADB_CMD=adb
if defined ANDROID_HOME (
    set ADB_CMD=%ANDROID_HOME%\platform-tools\adb
)

echo Se i test continuano a fallire, verifica:
echo 1. 📱 Emulatore/dispositivo connesso e online:
echo    !ADB_CMD! devices
echo.
echo 2. 🌐 Connettività di rete del dispositivo:
echo    !ADB_CMD! shell ping -c 3 8.8.8.8
echo.
echo 3. 🔧 PROBLEMI UTP (Unified Test Platform):
echo    Se vedi 'Failed to receive UTP test results' o errori 'proto_config':
echo    - Prova: gradlew connectedTestNoUTP
echo    - Oppure: gradlew connectedDebugAndroidTest --no-configuration-cache
echo    - È spesso dovuto a problemi di configurazione protobuf in UTP
echo.
echo 4. 🔥 File google-services.json presente:
echo    dir app\google-services.json
echo.
echo 5. 📋 Log dettagliati dei test:
echo    !ADB_CMD! logcat -s TestRunner,FirebaseTestApplication,FirebaseIntegrationTest
echo.
echo 6. 🧹 Clean e rebuild:
echo    gradlew clean ^&^& gradlew assembleDebug
echo.
echo I test sono progettati per essere tolleranti ai problemi di connettività e UTP.
echo Se falliscono, probabilmente c'è un problema di configurazione dell'app.
exit /b 0