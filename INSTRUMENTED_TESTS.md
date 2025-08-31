# CircolApp - Test di Instrumentazione

## AppTest.kt

Questo file contiene test di instrumentazione per le principali funzionalità dell'app CircolApp.

### Funzionalità Testate

1. **Registrazione utente** (`testUserRegistration`)
   - Verifica la registrazione di un nuovo utente
   - Testa il form di registrazione e la validazione

2. **Login utente** (`testUserLogin`)
   - Verifica il login di un utente esistente
   - Testa la validazione del form di login

3. **Logout** (`testUserLogout`)
   - Verifica la funzionalità di logout
   - Testa la navigazione dopo il logout

4. **Aggiunta prodotto** (`testAddNewProduct`)
   - Verifica l'aggiunta di un nuovo prodotto (solo admin)
   - Testa il form di creazione prodotto

5. **Ordinazione prodotto** (`testProductOrdering`)
   - Verifica il flusso di ordinazione di un prodotto
   - Testa l'accesso al catalogo prodotti

6. **Partecipazione eventi** (`testEventParticipation`)
   - Verifica l'accesso alla lista eventi
   - Testa la navigazione verso gli eventi

7. **Creazione eventi** (`testEventCreation`)
   - Verifica la creazione di eventi (solo admin)
   - Testa il form di creazione evento

8. **Richiesta tessera** (`testTesseraRequest`)
   - Verifica la richiesta di tessera socio
   - Testa l'accesso alla funzionalità tessera

9. **Assegnazione tessera** (`testTesseraAssignment`)
   - Verifica la gestione tessere (solo admin)
   - Testa i permessi admin per le tessere

10. **Ricarica utente** (`testUserRecharge`)
    - Verifica il flusso di ricarica del saldo
    - Testa l'accesso al QR code per ricariche

11. **Pagamento in cassa** (`testCashPayment`)
    - Verifica l'accesso alla cassa (solo admin)
    - Testa la funzionalità di pagamento

12. **Funzionalità base** (`testBasicAppFunctionality`)
    - Verifica che l'app si avvii correttamente
    - Testa la navigazione base tra login e registrazione

13. **QR Code** (`testQrCodeGeneration`)
    - Verifica la generazione del QR code utente
    - Testa la visualizzazione del QR code

14. **Gestione saldo** (`testUserBalanceManagement`)
    - Verifica la visualizzazione del saldo utente
    - Testa l'accesso ai movimenti

15. **Accesso funzionalità** (`testUserAccessToFeatures`)
    - Verifica l'accesso a tutte le funzionalità utente
    - Testa la navigazione tra i fragment principali

### Permessi e Ruoli

I test verificano anche le differenze di permessi tra:
- **Utenti normali**: Accesso a home, eventi, pagamenti, profilo
- **Admin**: Accesso aggiuntivo a gestione prodotti, cassa, gestione tessere

### Come Eseguire i Test

```bash
# Esegui tutti i test di instrumentazione
./gradlew connectedAndroidTest

# Esegui solo la classe AppTest
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.circolapp.AppTest
```

### Prerequisiti per Test Completi

Per test completamente funzionali con Firebase:

1. **Firebase Test Environment**
   - Configurare Firebase Auth Emulator
   - Configurare Firestore Emulator
   - Creare utenti di test

2. **Dati di Test**
   - Prodotti di esempio nel database
   - Eventi di esempio
   - Utenti con diversi ruoli

3. **Configurazione Test**
   - Mock Firebase dependencies dove necessario
   - Setup/teardown per ogni test
   - Gestione stato di autenticazione

### Limitazioni Attuali

- I test verificano principalmente la presenza e l'accessibilità degli elementi UI
- Non testano la logica di business completa (richiederebbe setup Firebase)
- Alcuni test potrebbero fallire senza dati appropriati nel database
- L'autenticazione Firebase non è mockata (richiede configurazione aggiuntiva)

### Note Tecniche

- Utilizzano AndroidJUnit4 e Espresso per l'automazione UI
- Supportano sia utenti normali che admin
- Includono verifiche di validazione form
- Testano la navigazione tra fragment
- Verificano la presenza di elementi specifici per ogni ruolo

### Struttura App Testata

L'app CircolApp è strutturata con:

- **Firebase Authentication** per la gestione utenti
- **Firebase Firestore** per il database
- **Navigation Component** per la navigazione
- **ViewModels + LiveData** per la gestione stato
- **Ruoli utente** (USER/ADMIN) per i permessi

### Modelli Principali

- **User**: gestione utenti, saldo, tessere
- **Product**: catalogo prodotti e inventario
- **Evento**: gestione eventi e partecipazione
- **Ordine**: gestione ordinazioni
- **RichiestaTessera**: gestione richieste tessere
- **Movimento**: storico transazioni

Questi test forniscono una copertura base delle funzionalità principali e possono essere estesi per includere test più specifici della logica di business.