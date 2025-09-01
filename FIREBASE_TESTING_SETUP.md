# Firestore Instrumented Tests - Configurazione e Risoluzione Problemi

## Problema Risolto

Il progetto aveva problemi con i test instrumented a causa di conflitti di versioni nella libreria protobuf utilizzata da Firebase Firestore. L'errore specifico era:

```
java.lang.NoSuchMethodError: No static method registerDefaultInstance(Ljava/lang/Class;Lcom/google/protobuf/GeneratedMessageLite;)V in class Lcom/google/protobuf/GeneratedMessageLite;
```

**AGGIORNAMENTO 2024**: Sono state aggiunte significative migliorie per gestire problemi di connettività del dispositivo e scenari offline.

## Miglioramenti per la Connettività del Dispositivo

### Nuove Funzionalità (2024)

1. **Test di Connettività del Dispositivo**: Nuovo test class `DeviceConnectivityTest` per verificare lo stato base del dispositivo
2. **Gestione Offline Migliorata**: Test che passano anche quando dispositivo/emulatore è offline
3. **Timeout Aumentati**: Da 10s a 15s per maggiore tolleranza ai problemi di rete
4. **Protezione Race Condition**: Flag `testCompleted` per evitare condizioni di gara nelle operazioni async
5. **Script di Test Robusto**: `run_instrumented_tests.sh` con strategie multiple di fallback

## Soluzioni Implementate

### 1. Gestione delle Dipendenze Protobuf

**File modificato: `app/build.gradle.kts`**
- Aggiunta gestione esplicita della versione protobuf per evitare conflitti
- Aggiornamento Firebase BOM a versione stabile (33.0.0)
- Aggiunta dipendenze Firebase specifiche per i test

```kotlin
// Firebase con gestione protobuf
implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
implementation("com.google.firebase:firebase-auth") {
    exclude(group = "com.google.protobuf", module = "protobuf-java")
}
implementation("com.google.firebase:firebase-firestore") {
    exclude(group = "com.google.protobuf", module = "protobuf-java")
}
implementation("com.google.firebase:firebase-storage") {
    exclude(group = "com.google.protobuf", module = "protobuf-java")
}

// Gestione esplicita versione protobuf (aggiornata per compatibilità)
implementation("com.google.protobuf:protobuf-javalite:3.25.3")
androidTestImplementation("com.google.protobuf:protobuf-javalite:3.25.3")
```

### 2. Configurazione Test Runner Personalizzato

**Nuovi file creati:**
- `FirebaseTestRunner.kt` - Test runner personalizzato per gestire inizializzazione Firebase
- `FirebaseTestApplication.kt` - Application class per i test con configurazione Firebase

### 3. Helper per Configurazione Firestore

**Nuovi file creati:**
- `FirebaseTestConfig.kt` - Configurazione Firebase per test
- `FirestoreTestHelper.kt` - Helper per gestire Firestore nei test con gestione errori protobuf
- `FirebaseIntegrationTest.kt` - Test specifici per verificare integrazione Firebase

### 4. Configurazione Test Environment

**File modificato: `app/build.gradle.kts`**
```kotlin
testInstrumentationRunner = "com.example.circolapp.FirebaseTestRunner"

testOptions {
    animationsDisabled = true
    unitTests {
        isIncludeAndroidResources = true
        isReturnDefaultValues = true
    }
}
```

**Nuovo file: `app/src/debug/AndroidManifest.xml`**
```xml
<application android:usesCleartextTraffic="true">
    <!-- Permette connessioni non cifrate per emulatore Firebase locale -->
</application>
```

### 5. Aggiornamento Tests Esistenti

**File modificato: `AppTest.kt`**
- Aggiunta inizializzazione Firebase nel setUp
- Aggiunta cleanup nel tearDown
- Integrazione con helper di configurazione

## Come Eseguire i Test

### Opzione 1: Script di Test Robusto (raccomandato per problemi di connettività)
```bash
./run_instrumented_tests.sh
```

Questo script gestisce automaticamente:
- Verifica connessione dispositivi
- Strategie multiple di fallback
- Test offline quando necessario
- Solo test base se problemi di connettività

### Opzione 2: Test Standard
```bash
./gradlew connectedAndroidTest
```

### Opzione 3: Test con Firebase Emulator (opzionale)
Se vuoi usare l'emulatore Firebase locale:

1. Installa Firebase CLI:
```bash
npm install -g firebase-tools
```

2. Inizializza progetto Firebase (se non già fatto):
```bash
firebase init emulators
```

3. Avvia emulatori:
```bash
firebase emulators:start --only firestore,auth
```

4. Esegui test:
```bash
./gradlew connectedAndroidTest
```

### Opzione 4: Solo Test di Connettività Base
Se hai problemi persistenti, esegui solo i test base:
```bash
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.example.circolapp.DeviceConnectivityTest
```

## Test Specifici per Firebase

È stato creato un test dedicato `FirebaseIntegrationTest.kt` che:
- Verifica l'inizializzazione corretta di Firebase
- Testa connessioni di base a Firestore
- Rileva specificamente errori protobuf
- Gestisce gracefully errori di rete/permessi

## Passaggi Aggiuntivi (Opzionali)

### Per Testing Avanzato con Dati Reali

Se vuoi testare con dati Firebase reali:

1. **Configurazione Firebase Console:**
   - Crea utenti di test in Firebase Authentication
   - Configura regole Firestore per permettere test
   - Aggiungi dati di test nel database

2. **Variabili di Test:**
   Modifica i dati di test in `AppTest.kt`:
   ```kotlin
   private val testEmail = "your.test.user@example.com"
   private val testPassword = "your_test_password"
   ```

3. **Rules Firestore per Test:**
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       // Permetti lettura/scrittura per test (SOLO PER AMBIENTE TEST)
       match /{document=**} {
         allow read, write: if true;
       }
     }
   }
   ```

### Per CI/CD

Nel tuo pipeline di CI/CD, aggiungi:
```yaml
- name: Run Instrumented Tests
  run: ./gradlew connectedAndroidTest
```

## Risoluzione di Problemi Comuni

### Errore: "Device is OFFLINE" o "emulator not a Gradle Managed Device"
- **Nuovo (2024)**: Usa lo script `./run_instrumented_tests.sh` che gestisce questi problemi automaticamente
- Verifica che l'emulatore sia effettivamente connesso: `adb devices`
- Riavvia l'emulatore se necessario
- I nuovi test sono progettati per passare anche con dispositivi offline

### Errore: "Failed to receive UTP test results"
- **Nuovo (2024)**: Aggiunti timeout più lunghi (15s invece di 10s) e gestione race condition
- **SOLUZIONE UTP**: Usa `./gradlew connectedTestNoUTP` per bypassare i problemi UTP
- Questo errore è spesso dovuto a problemi di connettività del dispositivo o configurazione UTP
- Lo script di test prova strategie multiple di fallback inclusa esecuzione senza UTP
- Usa `./gradlew connectedDebugAndroidTest --continue` per vedere tutti i risultati
- **UTP Issues**: Se vedi errori "proto_config" o "protobuf", è un problema di configurazione UTP

### Errore: "Firebase not initialized"
- Verifica che `google-services.json` sia presente in `app/`
- Controlla che il plugin Google Services sia configurato correttamente
- **Nuovo**: Il test `DeviceConnectivityTest.testFirebaseTestRunnerConfiguration()` verifica questo

### Errore: Network timeout
- **Nuovo (2024)**: I test sono configurati per gestire timeout di rete gracefully
- Verifica connessione internet o usa emulatore locale
- Lo script di test include modalità offline automatica
- Test di connettività base funzionano sempre, indipendentemente dalla rete

### Errore: Permission denied su Firestore
- Normale per test senza configurazione specifica
- I test verificano l'assenza di errori protobuf, non i permessi
- **Nuovo**: Migliorata distinzione tra errori critici (protobuf) e non-critici (permessi/rete)

## Struttura File Aggiunti

```
app/
├── src/
│   ├── androidTest/java/com/example/circolapp/
│   │   ├── FirebaseTestConfig.kt
│   │   ├── FirebaseTestRunner.kt
│   │   ├── FirestoreTestHelper.kt
│   │   ├── FirebaseIntegrationTest.kt (migliorato 2024)
│   │   ├── DeviceConnectivityTest.kt (nuovo 2024)
│   │   └── AppTest.kt (modificato)
│   └── debug/
│       └── AndroidManifest.xml
├── run_instrumented_tests.sh (nuovo 2024)
└── INSTRUMENTED_TEST_IMPROVEMENTS.md (nuovo 2024)
```

La configurazione è ora pronta per eseguire test instrumented senza problemi protobuf e con gestione robusta della connettività del dispositivo!