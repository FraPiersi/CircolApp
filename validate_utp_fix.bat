@echo off
setlocal enabledelayedexpansion

REM Script to validate the UTP fix implementation without requiring a device
REM This tests the Gradle configuration and ensures all tasks are properly defined

echo 🔍 Validating UTP fix implementation...
echo =====================================

REM Check if we're in the right directory
if not exist "app\build.gradle.kts" (
    echo ❌ Errore: Eseguire questo script dalla directory root del progetto CircolApp
    exit /b 1
)

echo.
echo 📋 Verifica configurazione Gradle...

REM Test 1: Check if custom tasks are defined
echo 1. Controllo task personalizzati...
findstr /c:"connectedTestNoUTP" "app\build.gradle.kts" >nul 2>&1
if !errorlevel! equ 0 (
    echo    ✅ Task connectedTestNoUTP definito
    set /a task1_ok=1
) else (
    echo    ❌ Task connectedTestNoUTP mancante
    set /a task1_ok=0
)

findstr /c:"connectedTestWithFallback" "app\build.gradle.kts" >nul 2>&1
if !errorlevel! equ 0 (
    echo    ✅ Task connectedTestWithFallback definito
    set /a task2_ok=1
) else (
    echo    ❌ Task connectedTestWithFallback mancante
    set /a task2_ok=0
)

REM Test 2: Check UTP configuration
echo 2. Controllo configurazione UTP...
findstr /c:"timeout_msec" "app\build.gradle.kts" >nul 2>&1
if !errorlevel! equ 0 (
    echo    ✅ Timeout UTP configurato
    set /a timeout_ok=1
) else (
    echo    ❌ Timeout UTP mancante
    set /a timeout_ok=0
)

findstr /c:"clearPackageData" "app\build.gradle.kts" >nul 2>&1
if !errorlevel! equ 0 (
    echo    ✅ ClearPackageData configurato
    set /a clear_ok=1
) else (
    echo    ❌ ClearPackageData mancante
    set /a clear_ok=0
)

REM Test 3: Check test runner improvements
echo 3. Controllo test runner personalizzato...
if exist "app\src\androidTest\java\com\example\circolapp\FirebaseTestRunner.kt" (
    findstr /c:"UTP" "app\src\androidTest\java\com\example\circolapp\FirebaseTestRunner.kt" >nul 2>&1
    if !errorlevel! equ 0 (
        echo    ✅ Gestione errori UTP nel test runner
        set /a runner_ok=1
    ) else (
        echo    ❌ Gestione errori UTP mancante
        set /a runner_ok=0
    )
) else (
    echo    ❌ Gestione errori UTP mancante
    set /a runner_ok=0
)

REM Test 4: Check documentation
echo 4. Controllo documentazione...
if exist "UTP_ISSUES_SOLUTION.md" (
    echo    ✅ Documentazione UTP presente
    set /a doc_ok=1
) else (
    echo    ❌ Documentazione UTP mancante
    set /a doc_ok=0
)

REM Test 5: Check script improvements
echo 5. Controllo script migliorati...
if exist "run_instrumented_tests.sh" (
    findstr /c:"connectedTestNoUTP" "run_instrumented_tests.sh" >nul 2>&1
    if !errorlevel! equ 0 (
        echo    ✅ Script include strategia No-UTP
        set /a script_ok=1
    ) else (
        echo    ❌ Script non include strategia No-UTP
        set /a script_ok=0
    )
) else (
    echo    ❌ Script non include strategia No-UTP
    set /a script_ok=0
)

echo.
echo 📊 RISULTATO VALIDAZIONE:

REM Count successes
set /a success_count=0
set /a total_tests=5

REM Test 1 check
if !task1_ok! equ 1 if !task2_ok! equ 1 (
    set /a success_count+=1
)

REM Test 2 check  
if !timeout_ok! equ 1 if !clear_ok! equ 1 (
    set /a success_count+=1
)

REM Test 3 check
if !runner_ok! equ 1 (
    set /a success_count+=1
)

REM Test 4 check
if !doc_ok! equ 1 (
    set /a success_count+=1
)

REM Test 5 check
if !script_ok! equ 1 (
    set /a success_count+=1
)

echo.
echo ✅ Test passati: !success_count!/!total_tests!

if !success_count! equ !total_tests! (
    echo 🎉 VALIDAZIONE COMPLETATA CON SUCCESSO!
    echo.
    echo Le seguenti correzioni UTP sono state implementate:
    echo - ✅ Task Gradle per bypassare UTP (connectedTestNoUTP)
    echo - ✅ Configurazione timeout e parametri UTP
    echo - ✅ Test runner con gestione errori UTP
    echo - ✅ Script con strategia fallback per UTP
    echo - ✅ Documentazione completa per risoluzione problemi UTP
    echo.
    echo 🚀 PRONTO PER L'USO:
    echo    Quando hai un dispositivo/emulatore disponibile, usa:
    echo    run_instrumented_tests.bat
    echo.
    echo    In caso di errori UTP, usa:
    echo    gradlew connectedTestNoUTP
    exit /b 0
) else (
    set /a failed_tests=!total_tests!-!success_count!
    echo ❌ VALIDAZIONE FALLITA: !failed_tests! test falliti
    echo.
    echo Controlla i file indicati sopra per completare l'implementazione.
    exit /b 1
)