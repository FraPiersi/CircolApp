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
    private val usersCollection = db.collection("utenti")

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

            val existingUserDoc = userDocumentRef.get().await()
            if (!existingUserDoc.exists()) {
                userMap["saldo"] = 0.0
                Log.d("UserRepository", "Nuovo utente ${firebaseUser.uid}. Impostazione saldo a 0.")
            } else {
                Log.d("UserRepository", "Utente esistente ${firebaseUser.uid}. Saldo non modificato durante il login.")
            }

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
            throw e
        }
    }
}