package com.example.circolapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.*

class ProfiloFragment : Fragment() {

    private lateinit var textNome: TextView
    private lateinit var textEmail: TextView
    private lateinit var textTelefono: TextView
    private lateinit var textSaldo: TextView
    private lateinit var textRuolo: TextView
    private lateinit var progressBar: ProgressBar

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profilo, container, false)

        // Inizializza le views
        textNome = view.findViewById(R.id.text_nome)
        textEmail = view.findViewById(R.id.text_email)
        textTelefono = view.findViewById(R.id.text_telefono)
        textSaldo = view.findViewById(R.id.text_saldo)
        textRuolo = view.findViewById(R.id.text_ruolo)

        // Aggiungi ProgressBar programmaticamente se non presente nel layout
        progressBar = ProgressBar(requireContext()).apply {
            visibility = View.VISIBLE
        }

        val logoutButton = view.findViewById<Button>(R.id.buttonLogout)
        logoutButton.setOnClickListener {
            AuthUI.getInstance().signOut(requireContext()).addOnCompleteListener {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        // Carica i dati dell'utente
        caricaDatiUtente()

        return view
    }

    private fun caricaDatiUtente() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Utente non autenticato", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostra dati di Firebase Auth
        textEmail.text = currentUser.email ?: "Email non disponibile"
        textNome.text = currentUser.displayName ?: "Nome non disponibile"

        // Carica dati aggiuntivi da Firestore
        FirebaseFirestore.getInstance().collection("utenti")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Nome/Display Name
                    val displayName = document.getString("displayName")
                        ?: document.getString("nome")
                        ?: currentUser.displayName
                        ?: "Nome non disponibile"
                    textNome.text = displayName

                    // Telefono
                    val telefono = document.getString("telefono") ?: "Non disponibile"
                    textTelefono.text = "Telefono: $telefono"

                    // Saldo
                    val saldo = document.getDouble("saldo") ?: 0.0
                    textSaldo.text = "Saldo: ${currencyFormatter.format(saldo)}"

                    // Ruolo
                    val ruolo = document.getString("ruolo") ?: "USER"
                    textRuolo.text = "Ruolo: ${if (ruolo == "ADMIN") "Amministratore" else "Utente"}"

                } else {
                    Toast.makeText(context, "Dati utente non trovati", Toast.LENGTH_SHORT).show()
                    // Usa solo i dati di Firebase Auth
                    textTelefono.text = "Telefono: Non disponibile"
                    textSaldo.text = "Saldo: €0,00"
                    textRuolo.text = "Ruolo: Utente"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Errore nel caricamento dati: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                // Fallback con dati di base
                textTelefono.text = "Telefono: Non disponibile"
                textSaldo.text = "Saldo: €0,00"
                textRuolo.text = "Ruolo: Utente"
            }
    }
}