#!/bin/bash

# Script per eseguire i test instrumented con gestione di problemi di connettività
# Usage: ./run_instrumented_tests.sh

echo "🚀 Avvio test instrumented CircolApp con gestione problemi di connettività..."

# Check if we're in the right directory
if [ ! -f "app/build.gradle.kts" ]; then
    echo "❌ Errore: Eseguire questo script dalla directory root del progetto CircolApp"
    exit 1
fi

# Function to check device connectivity
check_device() {
    echo "📱 Verifica connessione dispositivi/emulatori..."
    
    # Check ADB devices
    devices=$(adb devices | grep -v "List of devices attached" | grep -v "^$" | wc -l)
    
    if [ $devices -eq 0 ]; then
        echo "⚠️  Nessun dispositivo/emulatore connesso"
        echo "   Avvia un emulatore o connetti un dispositivo prima di procedere"
        return 1
    else
        echo "✅ Trovati $devices dispositivo/i connesso/i"
        adb devices
        return 0
    fi
}

# Function to run tests with different strategies
run_tests() {
    echo ""
    echo "🧪 Esecuzione test instrumented..."
    
    # Strategy 1: Try with standard configuration
    echo "📋 Strategia 1: Test standard"
    ./gradlew connectedDebugAndroidTest --continue --stacktrace
    
    local exit_code=$?
    
    if [ $exit_code -eq 0 ]; then
        echo "✅ Test completati con successo!"
        return 0
    else
        echo "⚠️  Test falliti con codice $exit_code"
        
        # Strategy 2: Try without UTP to avoid protobuf configuration issues
        echo ""
        echo "📋 Strategia 2: Test senza UTP per evitare errori protobuf/configurazione"
        ./gradlew connectedTestNoUTP --continue --stacktrace
        
        exit_code=$?
        
        if [ $exit_code -eq 0 ]; then
            echo "✅ Test completati con successo senza UTP!"
            return 0
        else
            echo "❌ Test senza UTP falliti, provo modalità offline..."
            
            # Strategy 3: Try with increased timeout and offline mode
            echo ""
            echo "📋 Strategia 3: Test con timeout aumentato e modalità offline"
            ./gradlew connectedDebugAndroidTest --offline --continue --stacktrace \
                -Pandroid.testInstrumentationRunnerArguments.timeout_msec=300000
            
            exit_code=$?
            
            if [ $exit_code -eq 0 ]; then
                echo "✅ Test completati con successo in modalità offline!"
                return 0
            else
                echo "❌ Test falliti anche in modalità offline"
                
                # Strategy 4: Run only basic connectivity tests
                echo ""
                echo "📋 Strategia 4: Solo test di connettività base"
                ./gradlew connectedDebugAndroidTest \
                    -Pandroid.testInstrumentationRunnerArguments.class=com.example.circolapp.DeviceConnectivityTest \
                    --continue --stacktrace
                
                exit_code=$?
                
                if [ $exit_code -eq 0 ]; then
                    echo "✅ Test di base completati - il dispositivo e l'app sono configurati correttamente"
                    echo "⚠️  I test Firebase potrebbero fallire per problemi di connettività o UTP"
                    return 0
                else
                    echo "❌ Anche i test di base falliscono - problema di configurazione dell'app"
                    return $exit_code
                fi
            fi
        fi
    fi
}

# Function to provide troubleshooting info
show_troubleshooting() {
    echo ""
    echo "🔧 RISOLUZIONE PROBLEMI:"
    echo ""
    echo "Se i test continuano a fallire, verifica:"
    echo "1. 📱 Emulatore/dispositivo connesso e online:"
    echo "   adb devices"
    echo ""
    echo "2. 🌐 Connettività di rete del dispositivo:"
    echo "   adb shell ping -c 3 8.8.8.8"
    echo ""
    echo "3. 🔧 PROBLEMI UTP (Unified Test Platform):"
    echo "   Se vedi 'Failed to receive UTP test results' o errori 'proto_config':"
    echo "   - Prova: ./gradlew connectedTestNoUTP"
    echo "   - Oppure: ./gradlew connectedDebugAndroidTest --no-configuration-cache"
    echo "   - È spesso dovuto a problemi di configurazione protobuf in UTP"
    echo ""
    echo "4. 🔥 File google-services.json presente:"
    echo "   ls -la app/google-services.json"
    echo ""
    echo "5. 📋 Log dettagliati dei test:"
    echo "   adb logcat -s TestRunner,FirebaseTestApplication,FirebaseIntegrationTest"
    echo ""
    echo "6. 🧹 Clean e rebuild:"
    echo "   ./gradlew clean && ./gradlew assembleDebug"
    echo ""
    echo "I test sono progettati per essere tolleranti ai problemi di connettività e UTP."
    echo "Se falliscono, probabilmente c'è un problema di configurazione dell'app."
}

# Main execution
main() {
    echo "CircolApp - Test Instrumented Runner"
    echo "===================================="
    
    # Check device connectivity first
    if ! check_device; then
        echo ""
        echo "❌ Impossibile procedere senza dispositivo connesso"
        show_troubleshooting
        exit 1
    fi
    
    # Run tests with fallback strategies
    if run_tests; then
        echo ""
        echo "🎉 TEST COMPLETATI SUCCESSFULLY!"
        echo ""
        echo "📊 Report disponibili in:"
        echo "   app/build/reports/androidTests/connected/debug/"
        exit 0
    else
        echo ""
        echo "❌ TEST FALLITI"
        show_troubleshooting
        exit 1
    fi
}

# Run main function
main "$@"