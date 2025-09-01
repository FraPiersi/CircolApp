# Firestore Instrumented Tests - Configurazione e Risoluzione Problemi

## Problema Risolto

Il progetto aveva problemi con i test instrumented a causa di conflitti di versioni nella libreria protobuf utilizzata da Firebase Firestore. L'errore specifico era:

```
java.lang.NoSuchMethodError: No static method registerDefaultInstance(Ljava/lang/Class;Lcom/google/protobuf/GeneratedMessageLite;)V in class Lcom/google/protobuf/GeneratedMessageLite;
```

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

### Opzione 1: Test Standard (raccomandato)
```bash
./gradlew connectedAndroidTest
```

### Opzione 2: Test con Firebase Emulator (opzionale)
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

### Errore: "Firebase not initialized"
- Verifica che `google-services.json` sia presente in `app/`
- Controlla che il plugin Google Services sia configurato correttamente

### Errore: Network timeout
- I test sono configurati per gestire timeout di rete gracefully
- Verifica connessione internet o usa emulatore locale

### Errore: Permission denied su Firestore
- Normale per test senza configurazione specifica
- I test verificano l'assenza di errori protobuf, non i permessi

## Struttura File Aggiunti

```
app/
├── src/
│   ├── androidTest/java/com/example/circolapp/
│   │   ├── FirebaseTestConfig.kt
│   │   ├── FirebaseTestRunner.kt
│   │   ├── FirestoreTestHelper.kt
│   │   ├── FirebaseIntegrationTest.kt
│   │   └── AppTest.kt (modificato)
│   └── debug/
│       └── AndroidManifest.xml
```

La configurazione è ora pronta per eseguire test instrumented senza problemi protobuf!