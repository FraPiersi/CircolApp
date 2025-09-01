# 🚨 SOLUZIONE IMMEDIATA per Errori UTP

## ⚡ SOLUZIONE VELOCE:
Se vedi errori UTP, usa questo comando invece:
```bash
./gradlew connectedTestNoUTP
```

## Il tuo errore:
```
GRAVE: Fatal error while executing main with args: --proto_config=...runnerConfig*.pb --proto_server_config=...serverConfig*.pb
Failed to receive the UTP test results
emulator-5554 with id Medium_Phone is not a Gradle Managed Device
```

## 💡 SOLUZIONI IMMEDIATE (ordinate per efficacia):

### ✅ OPZIONE 1 - RACCOMANDATA: Bypassa UTP completamente
```bash
./gradlew connectedTestNoUTP
```
*Questo disabilita UTP e funziona con emulatori standard*

### ✅ OPZIONE 2: Script automatico con tutte le strategie
```bash
./run_instrumented_tests.sh
```
*Prova automaticamente 4 strategie diverse in sequenza*

### ✅ OPZIONE 3: Disabilita cache UTP manualmente
```bash
./gradlew connectedDebugAndroidTest --no-configuration-cache --no-build-cache
```

### ✅ OPZIONE 4: Solo test di base (sempre funziona)
```bash
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.circolapp.DeviceConnectivityTest
```

### ✅ OPZIONE 5: Strategia completa con fallback
```bash
./gradlew connectedTestWithFallback
```
*Prova diverse strategie automaticamente*

## 🔧 Spiegazione degli errori:

- **"Failed to receive UTP test results"** → UTP non riesce a comunicare con l'emulatore
- **"Fatal error with proto_config"** → Errore di configurazione protobuf di UTP  
- **"not a Gradle Managed Device"** → UTP preferisce device gestiti ma il tuo emulatore è normale
- **"GRAVE: Fatal error"** → UTP ha problemi con i file di configurazione
- **"cannot serialize Gradle script object references"** → Problemi con configuration cache (RISOLTO ✅)
- **"invocation of 'Task.project' at execution time"** → Reference 'project' durante esecuzione (RISOLTO ✅)

## 🎯 RACCOMANDAZIONE FINALE:

**Usa sempre l'OPZIONE 1**: `./gradlew connectedTestNoUTP`

Questo comando:
- ✅ Bypassa completamente UTP
- ✅ Funziona con emulatori standard  
- ✅ Evita errori protobuf
- ✅ È più veloce e affidabile
- ✅ Compatible con configuration cache (AGGIORNATO)

## 📚 Documentazione completa:
- Vedi `UTP_ISSUES_SOLUTION.md` per dettagli tecnici
- Vedi `FIREBASE_TESTING_SETUP.md` per setup Firebase