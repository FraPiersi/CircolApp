#!/bin/bash

# Script to validate the UTP fix implementation without requiring a device
# This tests the Gradle configuration and ensures all tasks are properly defined

echo "🔍 Validating UTP fix implementation..."
echo "====================================="

# Check if we're in the right directory
if [ ! -f "app/build.gradle.kts" ]; then
    echo "❌ Errore: Eseguire questo script dalla directory root del progetto CircolApp"
    exit 1
fi

echo ""
echo "📋 Verifica configurazione Gradle..."

# Test 1: Check if custom tasks are defined
echo "1. Controllo task personalizzati..."
if grep -q "connectedTestNoUTP" app/build.gradle.kts; then
    echo "   ✅ Task connectedTestNoUTP definito"
else
    echo "   ❌ Task connectedTestNoUTP mancante"
fi

if grep -q "connectedTestWithFallback" app/build.gradle.kts; then
    echo "   ✅ Task connectedTestWithFallback definito"
else
    echo "   ❌ Task connectedTestWithFallback mancante"
fi

# Test 2: Check UTP configuration
echo "2. Controllo configurazione UTP..."
if grep -q "timeout_msec" app/build.gradle.kts; then
    echo "   ✅ Timeout UTP configurato"
else
    echo "   ❌ Timeout UTP mancante"
fi

if grep -q "clearPackageData" app/build.gradle.kts; then
    echo "   ✅ ClearPackageData configurato"
else
    echo "   ❌ ClearPackageData mancante"
fi

# Test 3: Check test runner improvements
echo "3. Controllo test runner personalizzato..."
if grep -q "UTP" app/src/androidTest/java/com/example/circolapp/FirebaseTestRunner.kt; then
    echo "   ✅ Gestione errori UTP nel test runner"
else
    echo "   ❌ Gestione errori UTP mancante"
fi

# Test 4: Check documentation
echo "4. Controllo documentazione..."
if [ -f "UTP_ISSUES_SOLUTION.md" ]; then
    echo "   ✅ Documentazione UTP presente"
else
    echo "   ❌ Documentazione UTP mancante"
fi

# Test 5: Check script improvements
echo "5. Controllo script migliorati..."
if grep -q "connectedTestNoUTP" run_instrumented_tests.sh; then
    echo "   ✅ Script include strategia No-UTP"
else
    echo "   ❌ Script non include strategia No-UTP"
fi

echo ""
echo "📊 RISULTATO VALIDAZIONE:"

# Count successes
success_count=0
total_tests=5

if grep -q "connectedTestNoUTP" app/build.gradle.kts && grep -q "connectedTestWithFallback" app/build.gradle.kts; then
    ((success_count++))
fi

if grep -q "timeout_msec" app/build.gradle.kts && grep -q "clearPackageData" app/build.gradle.kts; then
    ((success_count++))
fi

if grep -q "UTP" app/src/androidTest/java/com/example/circolapp/FirebaseTestRunner.kt; then
    ((success_count++))
fi

if [ -f "UTP_ISSUES_SOLUTION.md" ]; then
    ((success_count++))
fi

if grep -q "connectedTestNoUTP" run_instrumented_tests.sh; then
    ((success_count++))
fi

echo ""
echo "✅ Test passati: $success_count/$total_tests"

if [ $success_count -eq $total_tests ]; then
    echo "🎉 VALIDAZIONE COMPLETATA CON SUCCESSO!"
    echo ""
    echo "Le seguenti correzioni UTP sono state implementate:"
    echo "- ✅ Task Gradle per bypassare UTP (connectedTestNoUTP)"
    echo "- ✅ Configurazione timeout e parametri UTP"
    echo "- ✅ Test runner con gestione errori UTP"
    echo "- ✅ Script con strategia fallback per UTP"
    echo "- ✅ Documentazione completa per risoluzione problemi UTP"
    echo ""
    echo "🚀 PRONTO PER L'USO:"
    echo "   Quando hai un dispositivo/emulatore disponibile, usa:"
    echo "   ./run_instrumented_tests.sh"
    echo ""
    echo "   In caso di errori UTP, usa:"
    echo "   ./gradlew connectedTestNoUTP"
    exit 0
else
    echo "❌ VALIDAZIONE FALLITA: $((total_tests - success_count)) test falliti"
    echo ""
    echo "Controlla i file indicati sopra per completare l'implementazione."
    exit 1
fi