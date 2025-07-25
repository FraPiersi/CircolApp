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
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.example.circolapp.model.Feedback
import com.example.circolapp.model.UserRole
import com.firebase.ui.auth.AuthUI
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
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
    private lateinit var buttonFeedback: Button

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)
    private var userRole = UserRole.UNKNOWN

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
        buttonFeedback = view.findViewById(R.id.buttonFeedback)

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

        buttonFeedback.setOnClickListener {
            if (userRole == UserRole.ADMIN) {
                // Admin: mostra tutti i feedback ricevuti
                showFeedbackList()
            } else {
                // Utente normale: mostra dialog per inviare feedback
                showSendFeedbackDialog()
            }
        }

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
                    userRole = UserRole.fromString(ruolo)
                    textRuolo.text = "Ruolo: ${userRole.getDisplayName()}"

                    // Aggiorna il testo del pulsante in base al ruolo
                    updateFeedbackButtonText()

                } else {
                    Toast.makeText(context, "Dati utente non trovati", Toast.LENGTH_SHORT).show()
                    // Usa solo i dati di Firebase Auth
                    textTelefono.text = "Telefono: Non disponibile"
                    textSaldo.text = "Saldo: €0,00"
                    textRuolo.text = "Ruolo: ${UserRole.USER.getDisplayName()}"
                    userRole = UserRole.USER
                    updateFeedbackButtonText()
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

    private fun updateFeedbackButtonText() {
        buttonFeedback.text = if (userRole == UserRole.ADMIN) {
            "Visualizza Feedback Ricevuti"
        } else {
            "Invia Feedback"
        }
    }

    private fun showFeedbackList() {
        // Usa il NavController per navigare al FeedbackListFragment
        try {
            findNavController().navigate(R.id.feedbackListFragment)
        } catch (e: Exception) {
            Toast.makeText(context, "Errore nella navigazione: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSendFeedbackDialog() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Utente non autenticato", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(
            R.layout.dialog_send_feedback, null
        )

        val spinnerCategoria = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.spinnerCategoria)
        val editTitolo = dialogView.findViewById<TextInputEditText>(R.id.editTextTitolo)
        val editMessaggio = dialogView.findViewById<TextInputEditText>(R.id.editTextMessaggio)

        // Configura le categorie
        val categorie = arrayOf("GENERALE", "BUG", "SUGGERIMENTO", "ALTRO")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categorie)
        spinnerCategoria.setAdapter(adapter)
        spinnerCategoria.setText(categorie[0], false) // Imposta il primo elemento come default

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        builder.setPositiveButton("Invia") { _, _ ->
            val categoria = spinnerCategoria.text.toString()
            val titolo = editTitolo.text.toString().trim()
            val messaggio = editMessaggio.text.toString().trim()

            if (titolo.isEmpty()) {
                Toast.makeText(context, "Inserisci un titolo", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            if (messaggio.isEmpty()) {
                Toast.makeText(context, "Inserisci un messaggio", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            // Crea e salva il feedback
            sendFeedback(currentUser.uid, currentUser.displayName ?: "Utente",
                        currentUser.email ?: "", categoria, titolo, messaggio)
        }
        builder.setNegativeButton("Annulla", null)
        builder.show()
    }

    private fun sendFeedback(uidUtente: String, nomeUtente: String, emailUtente: String,
                           categoria: String, titolo: String, messaggio: String) {
        val db = FirebaseFirestore.getInstance()
        val feedbackRef = db.collection("feedback").document()

        val feedback = Feedback(
            id = feedbackRef.id,
            uidUtente = uidUtente,
            nomeUtente = nomeUtente,
            emailUtente = emailUtente,
            titolo = titolo,
            messaggio = messaggio,
            categoria = categoria,
            letto = false
        )

        feedbackRef.set(feedback)
            .addOnSuccessListener {
                Toast.makeText(context, "Feedback inviato con successo!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Errore nell'invio del feedback: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}