package com.example.circolapp.repository


import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlin.io.path.exists

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("utenti") // Assicurati che sia "utenti"

    suspend fun addOrUpdateUserInFirestore(firebaseUser: FirebaseUser) {
        if (firebaseUser.uid.isBlank()) {
            Log.w("UserRepository",
                "FirebaseUser UID è vuoto, impossibile aggiungere/aggiornare l'utente."
            )
            throw IllegalArgumentException("FirebaseUser UID is blank")
        }

        val userDocumentRef = usersCollection.document(firebaseUser.uid)

        try {
            val userMap = mutableMapOf<String, Any?>()
            userMap["uid"] = firebaseUser.uid
            userMap["email"] = firebaseUser.email

            if (firebaseUser.displayName != null) {
                userMap["displayName"] = firebaseUser.displayName
            }
            if (firebaseUser.photoUrl != null) {
                userMap["photoUrl"] = firebaseUser.photoUrl.toString()
            }

            userMap["lastSeen"] = FieldValue.serverTimestamp()
            // userMap["fcmToken"] = ... // Gestisci l'FCM token se necessario

            // Controlla se il documento esiste già.
            // Se non esiste, è un nuovo utente, quindi inizializza il saldo.
            val existingUserDoc = userDocumentRef.get().await()
            if (!existingUserDoc.exists()) {
                userMap["saldo"] = 0.0 // Inizializza il saldo a 0 per i nuovi utenti
                Log.d("UserRepository", "Nuovo utente ${firebaseUser.uid}. Impostazione saldo a 0.")
            } else {
                // Utente esistente. Non tocchiamo il saldo qui a meno che non ci sia una logica specifica
                // per aggiornarlo durante il login (generalmente non necessario per il saldo).
                // Il saldo verrà modificato da altre funzioni (es. trasferimenti).
                Log.d("UserRepository", "Utente esistente ${firebaseUser.uid}. Saldo non modificato durante il login.")
            }

            // Usa SetOptions.merge() per aggiornare solo i campi specificati
            // e non sovrascrivere l'intero documento se esiste già.
            // Se l'utente è nuovo, questo creerà il documento con tutti i campi, incluso il saldo.
            // Se l'utente esiste e 'saldo' non è in userMap, il merge non lo toccherà.
            userDocumentRef.set(userMap, SetOptions.merge()).await()
            Log.d(
                "UserRepository",
                "Utente ${firebaseUser.uid} aggiunto/aggiornato in Firestore."
            )

        } catch (e: Exception) {
            Log.e(
                "UserRepository",
                "Errore nell'aggiungere/aggiornare l'utente ${firebaseUser.uid}",
                e
            )
            throw e // Rilancia l'eccezione
        }
    }

    // ... altre funzioni del repository ...
}