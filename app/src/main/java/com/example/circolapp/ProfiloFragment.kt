package com.example.circolapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.circolapp.model.RichiestaTessera
import com.example.circolapp.model.TipoRichiesta
import com.example.circolapp.model.StatoRichiesta
import com.example.circolapp.model.UserRole
import com.firebase.ui.auth.AuthUI
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ProfiloFragment : Fragment() {

    private lateinit var textNome: TextView
    private lateinit var textEmail: TextView
    private lateinit var textTelefono: TextView
    private lateinit var textSaldo: TextView
    private lateinit var textRuolo: TextView
    private lateinit var textTesseraStatus: TextView
    private lateinit var textNumeroTessera: TextView
    private lateinit var textScadenzaTessera: TextView
    private lateinit var buttonTessera: Button
    private lateinit var buttonGestisciTessere: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonFeedback: Button

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
    private progressBar = ProgressBar(requireContext()).apply {
            visibility = View.VISIBLE
        }

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

        buttonTessera.setOnClickListener {
            gestisciTessera()
        }

        buttonGestisciTessere.setOnClickListener {
            navigateToGestisciTessere()
        }

        return view
    }

    private fun caricaDatiUtente() {
        textEmail.text = currentUser.email ?: "Email non disponibile"
        textNome.text = currentUser.displayName ?: "Nome non disponibile"

       
        FirebaseFirestore.getInstance().collection("utenti")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Nome/Display Name

                    // Ruolo
                    caricaDatiTessera(document)

                    updateFeedbackButtonText()
                    updateTesseraUI()

                } else {
                    Toast.makeText(context, "Dati utente non trovati", Toast.LENGTH_SHORT).show()
                    textTelefono.text = "Telefono: Non disponibile"
                    textSaldo.text = "Saldo: €0,00"
                    textRuolo.text = "Ruolo: ${UserRole.USER.getDisplayName()}"
                    userRole = UserRole.USER
                    updateFeedbackButtonText()
                    updateTesseraUI()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Errore nel caricamento dati: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                // Fallback con dati di base
                textTelefono.text = "Telefono: Non disponibile"
                textSaldo.text = "Saldo: €0,00"
                textRuolo.text = "Ruolo: Utente"
                updateTesseraUI()
            }
    }

    private fun caricaDatiTessera(document: com.google.firebase.firestore.DocumentSnapshot) {
                if (dataScadenza.before(Date())) {
                    textTesseraStatus.text = "Tessera Scaduta"
                    textTesseraStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                }
            }
        } else if (richiestaInCorso) {
            textTesseraStatus.text = "Richiesta in attesa"
            textTesseraStatus.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
        } else {
            textTesseraStatus.text = "Nessuna tessera"
            textTesseraStatus.setTextColor(resources.getColor(android.R.color.darker_gray))
        }
    }

    private fun updateTesseraUI() {
        if (userRole == UserRole.ADMIN) {
            buttonGestisciTessere.visibility = View.VISIBLE
            // Gli admin non vedono nulla relativo alla propria tessera
            buttonTessera.visibility = View.GONE
            requireView().findViewById<TextView>(R.id.text_tessera_title)?.visibility = View.GONE
            textTesseraStatus.visibility = View.GONE
            textNumeroTessera.visibility = View.GONE
            textScadenzaTessera.visibility = View.GONE
        } else {
            buttonGestisciTessere.visibility = View.GONE
            buttonTessera.visibility = View.VISIBLE
            requireView().findViewById<TextView>(R.id.text_tessera_title)?.visibility = View.VISIBLE
            textTesseraStatus.visibility = View.VISIBLE
            // textNumeroTessera e textScadenzaTessera vengono gestiti in caricaDatiTessera()
        }
    }

    private fun gestisciTessera() {
        if (currentUserSaldo < QUOTA_TESSERA) {
            AlertDialog.Builder(requireContext())
                .setTitle("Saldo Insufficiente")
                .setMessage("Per richiedere la tessera socio è necessario avere un saldo di almeno ${currencyFormatter.format(QUOTA_TESSERA)}.\n\nIl tuo saldo attuale è: ${currencyFormatter.format(currentUserSaldo)}")
                .setPositiveButton("OK", null)
                .show()
            return
        }

            transaction.update(userRef, "saldo", nuovoSaldo)

            // Aggiungi il movimento nella sottocollezione movimenti
            transaction.update(userRef, "richiestaRinnovoInCorso", true)

        }.addOnFailureListener { e ->
            Toast.makeText(context, "Errore nel pagamento: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToGestisciTessere() {
        try {
            findNavController().navigate(R.id.gestisciTessereFragment)
        } catch (e: Exception) {
            Toast.makeText(context, "Errore nella navigazione: ${e.message}", Toast.LENGTH_SHORT).show()
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
        try {
            findNavController().navigate(R.id.feedbackListFragment)
        } catch (e: Exception) {
            Toast.makeText(context, "Errore nella navigazione: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSendFeedbackDialog() {

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