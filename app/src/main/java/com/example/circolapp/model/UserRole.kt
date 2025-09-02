package com.example.circolapp.model
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
    UNKNOWN;

    companion object {
        /**
         * Converte una stringa nel corrispondente UserRole.
         * @param roleString La stringa del ruolo (es. "ADMIN", "USER")
         * @return Il UserRole corrispondente, o UNKNOWN se non riconosciuto
         */
        fun fromString(roleString: String?): UserRole {
            return when (roleString?.uppercase()) {
                "ADMIN" -> ADMIN
                "USER" -> USER
                else -> UNKNOWN
            }
        }
    }

    /**
     * Restituisce una rappresentazione leggibile del ruolo.
     */
    fun getDisplayName(): String {
        return when (this) {
            ADMIN -> "Amministratore"
            USER -> "Utente"
            UNKNOWN -> "Ruolo sconosciuto"
        }
    }
}
