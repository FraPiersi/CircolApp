package com.example.circolapp.repository

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlin.io.path.exists

class UserRepository {

    private
    suspend fun addOrUpdateUserInFirestore(firebaseUser: FirebaseUser) {
        if (firebaseUser.uid.isBlank()) {
            Log.w("UserRepository",
                "FirebaseUser UID è vuoto, impossibile aggiungere/aggiornare l'utente."
            )
            throw IllegalArgumentException("FirebaseUser UID is blank")
        }

                Log.d("UserRepository", "Nuovo utente ${firebaseUser.uid}. Impostazione saldo a 0.")
            } else {
                // Utente esistente. Non tocchiamo il saldo qui a meno che non ci sia una logica specifica
                // per aggiornarlo durante il login (generalmente non necessario per il saldo).
                // Il saldo verrà modificato da altre funzioni (es. trasferimenti).
                Log.d("UserRepository", "Utente esistente ${firebaseUser.uid}. Saldo non modificato durante il login.")
            }

            // e non sovrascrivere l'intero documento se esiste già.
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