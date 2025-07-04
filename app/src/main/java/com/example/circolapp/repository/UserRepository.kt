package com.example.circolapp.repository // Crea questo package se non esiste

import android.util.Log
import com.example.circolapp.model.User // Assicurati che l'import sia corretto
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("utenti")

    suspend fun addOrUpdateUserInFirestore(firebaseUser: FirebaseUser) {
        if (firebaseUser.uid.isBlank()) {
            Log.w("UserRepository", "FirebaseUser UID è vuoto, impossibile aggiungere/aggiornare l'utente.")
            // Potresti voler lanciare un'eccezione qui per gestirla nel ViewModel
            throw IllegalArgumentException("FirebaseUser UID is blank")
        }

        val userDocumentRef = usersCollection.document(firebaseUser.uid)

        // Non è strettamente necessario leggere prima se usi SetOptions.merge(),
        // ma può essere utile se vuoi logiche condizionali più complesse
        // basate sui dati esistenti, o se vuoi evitare scritture inutili.
        // val existingUserDoc = userDocumentRef.get().await()

        val userMap = mutableMapOf<String, Any?>()
        userMap["uid"] = firebaseUser.uid
        userMap["email"] = firebaseUser.email

        // Per displayName e photoUrl, potresti volerli sincronizzare sempre da Firebase Auth
        // o avere una logica più complessa se l'utente può modificarli nell'app.
        // Semplifichiamo per ora: li prendiamo sempre da FirebaseUser.
        if (firebaseUser.displayName != null) {
            userMap["displayName"] = firebaseUser.displayName
        }
        if (firebaseUser.photoUrl != null) {
            userMap["photoUrl"] = firebaseUser.photoUrl.toString()
        }

        userMap["lastSeen"] = FieldValue.serverTimestamp()
        // userMap["fcmToken"] = ... // Gestisci l'FCM token se necessario

        try {
            userDocumentRef.set(userMap, SetOptions.merge()).await()
            Log.d("UserRepository", "Utente ${firebaseUser.uid} aggiunto/aggiornato in Firestore.")
        } catch (e: Exception) {
            Log.e("UserRepository", "Errore nell'aggiungere/aggiornare l'utente ${firebaseUser.uid}", e)
            throw e // Rilancia l'eccezione per essere gestita dal chiamante (ViewModel)
        }
    }

    // Altre funzioni del repository per gli utenti (es. getUser, updateUserFcmToken)
    suspend fun getUser(userId: String): User? {
        if (userId.isBlank()) return null
        return try {
            usersCollection.document(userId).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "Errore nel recuperare l'utente $userId", e)
            null
        }
    }
}