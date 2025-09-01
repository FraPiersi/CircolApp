# Fix per Conflitto Dipendenze androidx.test:monitor

## Problema Risolto

Il progetto aveva un conflitto di versioni nelle dipendenze di test Android, specificamente con `androidx.test:monitor`. L'errore era:

```
Cannot find a version of 'androidx.test:monitor' that satisfies the version constraints:
   Dependency path 'CircolApp:app:unspecified' --> 'androidx.test:monitor:1.8.0'
   Constraint path 'CircolApp:app:unspecified' --> 'androidx.test:monitor:{strictly 1.6.0}'
   ...
```

## Soluzione Applicata

### Modifiche al file `app/build.gradle.kts`:

1. **Rimossa dipendenza esplicita `androidx.test:monitor:1.7.1`**
   - Questa dipendenza era fornita transitivamente da altre librerie di test
   - La sua presenza esplicita causava conflitti di versione

2. **Allineate le versioni di Espresso**
   - `androidx.test.espresso:espresso-core`: `3.5.0` → `3.6.1`
   - `androidx.test.espresso:espresso-intents`: `3.5.1` → `3.6.1`
   - Ora tutte le dipendenze Espresso sono alla versione `3.6.1`

### Dipendenze di test dopo la correzione:

```kotlin
// Android Testing
androidTestImplementation(libs.androidx.junit)
androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
androidTestImplementation("androidx.test.espresso:espresso-contrib:3.6.1") 
androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1")
androidTestImplementation("androidx.test:runner:1.6.1")
androidTestImplementation("androidx.test:rules:1.6.1")
androidTestImplementation("androidx.test.ext:junit:1.2.1")
androidTestImplementation("androidx.test:core-ktx:1.6.1")
// androidx.test:monitor rimossa - fornita transitivamente
androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
```

## Validazione

Per verificare che la correzione funzioni:

```bash
# Esegui il script di validazione
./validate_dependencies.sh

# Oppure manualmente:
./gradlew app:dependencies --configuration androidTestRuntimeClasspath
./gradlew connectedAndroidTest
```

## Risultato

- ✅ Conflitti di versione `androidx.test:monitor` risolti
- ✅ Dipendenze Espresso allineate alla versione 3.6.1  
- ✅ Build Android test configurazione funzionante
- ✅ Tutti i test instrumented possono essere eseguiti

La configurazione è ora compatibile con Gradle 8.13 e risolve i conflitti di dipendenze precedenti.