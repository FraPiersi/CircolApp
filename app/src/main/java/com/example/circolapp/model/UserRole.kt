package com.example.circolapp.model // Assicurati che il package sia corretto

/**
 * Rappresenta i possibili ruoli utente nell'applicazione.
 */
enum class UserRole {
    /**
     * Utente standard con permessi limitati.
     */
    USER,

    /**
     * Utente amministratore con permessi elevati.
     */
    ADMIN,

    /**
     * Ruolo sconosciuto o non ancora determinato.
     * Utile come stato iniziale o per gestire casi imprevisti.
     */
    UNKNOWN
}
