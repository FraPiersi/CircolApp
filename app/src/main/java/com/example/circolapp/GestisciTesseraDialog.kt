package com.example.circolapp

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.circolapp.model.RichiestaTessera
import com.example.circolapp.model.TipoRichiesta
import com.example.circolapp.model.User
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class GestisciTesseraDialog(
    private val utente: User,
    private val onActionCompleted: () -> Unit
) : DialogFragment() {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_gestisci_tessera, null)

        setupViews(view)

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Gestisci Tessera - ${utente.nome}")
            .setNegativeButton("Chiudi", null)
            .create()
    }

    private fun setupViews(view: View) {
        // Informazioni utente
        view.findViewById<TextView>(R.id.text_nome_utente).text = utente.nome
        view.findViewById<TextView>(R.id.text_uid_utente).text = "UID: ${utente.uid}"
        view.findViewById<TextView>(R.id.text_saldo_utente).text = "Saldo: ${currencyFormatter.format(utente.saldo)}"

        // Stato tessera
        val textStatoTessera = view.findViewById<TextView>(R.id.text_stato_tessera)
        val textDettagliTessera = view.findViewById<TextView>(R.id.text_dettagli_tessera)

        when {
            utente.richiestaRinnovoInCorso -> {
                textStatoTessera.text = "🟡 Richiesta in attesa"
                textDettagliTessera.text = "L'utente ha una richiesta di tessera in attesa di approvazione"
                setupPendingRequestButtons(view)
            }
            utente.hasTessera -> {
                if (utente.dataScadenzaTessera != null && utente.dataScadenzaTessera.before(Date())) {
                    textStatoTessera.text = "🔴 Tessera scaduta"
                    textDettagliTessera.text = "Tessera: ${utente.numeroTessera}\nScaduta il: ${dateFormatter.format(utente.dataScadenzaTessera)}"
                    setupExpiredCardButtons(view)
                } else {
                    textStatoTessera.text = "🟢 Tessera attiva"
                    val scadenza = if (utente.dataScadenzaTessera != null)
                        dateFormatter.format(utente.dataScadenzaTessera) else "Non specificata"
                    textDettagliTessera.text = "Tessera: ${utente.numeroTessera}\nScadenza: $scadenza"
                    setupActiveCardButtons(view)
                }
            }
            else -> {
                textStatoTessera.text = "⚪ Nessuna tessera"
                textDettagliTessera.text = "L'utente non ha una tessera socio"
                setupNoCardButtons(view)
            }
        }
    }

    private fun setupPendingRequestButtons(view: View) {
        val buttonContainer = view.findViewById<ViewGroup>(R.id.button_container)
        buttonContainer.removeAllViews()

        val buttonApprova = Button(requireContext()).apply {
            text = "Approva Richiesta"
            setOnClickListener { approvaRichiesta() }
        }
        val buttonRifiuta = Button(requireContext()).apply {
            text = "Rifiuta Richiesta"
            setOnClickListener { rifiutaRichiesta() }
        }

        buttonContainer.addView(buttonApprova)
        buttonContainer.addView(buttonRifiuta)
    }

    private fun setupActiveCardButtons(view: View) {
        val buttonContainer = view.findViewById<ViewGroup>(R.id.button_container)
        buttonContainer.removeAllViews()

        val buttonRinnova = Button(requireContext()).apply {
            text = "Rinnova Tessera"
            setOnClickListener { rinnovaTessera() }
        }
        val buttonRevoca = Button(requireContext()).apply {
            text = "Revoca Tessera"
            setOnClickListener { revocaTessera() }
        }

        buttonContainer.addView(buttonRinnova)
        buttonContainer.addView(buttonRevoca)
    }

    private fun setupExpiredCardButtons(view: View) {
        val buttonContainer = view.findViewById<ViewGroup>(R.id.button_container)
        buttonContainer.removeAllViews()

        val buttonRinnova = Button(requireContext()).apply {
            text = "Rinnova Tessera"
            setOnClickListener { rinnovaTessera() }
        }

        buttonContainer.addView(buttonRinnova)
    }

    private fun setupNoCardButtons(view: View) {
        val buttonContainer = view.findViewById<ViewGroup>(R.id.button_container)
        buttonContainer.removeAllViews()

        val buttonAssegna = Button(requireContext()).apply {
            text = "Assegna Tessera"
            setOnClickListener { assegnaTessera() }
        }

        buttonContainer.addView(buttonAssegna)
    }

    private fun approvaRichiesta() {
        val db = FirebaseFirestore.getInstance()

        // Genera numero tessera e data scadenza
        val numeroTessera = "TS${System.currentTimeMillis().toString().takeLast(8)}"
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, 1)
        val dataScadenza = calendar.time

        // Aggiorna l'utente
        val userUpdates = mapOf(
            "hasTessera" to true,
            "numeroTessera" to numeroTessera,
            "dataScadenzaTessera" to dataScadenza,
            "richiestaRinnovoInCorso" to false
        )

        db.collection("utenti").document(utente.uid)
            .update(userUpdates)
            .addOnSuccessListener {
                // Rimuovi la richiesta dalla collezione richieste_tessera se esiste
                db.collection("richieste_tessera")
                    .whereEqualTo("uidUtente", utente.uid)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (doc in documents) {
                            doc.reference.delete()
                        }
                    }

                Toast.makeText(context, "Tessera assegnata con successo!", Toast.LENGTH_SHORT).show()
                onActionCompleted()
                dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Errore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rifiutaRichiesta() {
        val db = FirebaseFirestore.getInstance()

        db.collection("utenti").document(utente.uid)
            .update("richiestaRinnovoInCorso", false)
            .addOnSuccessListener {
                // Rimuovi la richiesta dalla collezione richieste_tessera se esiste
                db.collection("richieste_tessera")
                    .whereEqualTo("uidUtente", utente.uid)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (doc in documents) {
                            doc.reference.delete()
                        }
                    }

                Toast.makeText(context, "Richiesta rifiutata", Toast.LENGTH_SHORT).show()
                onActionCompleted()
                dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Errore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun assegnaTessera() {
        val numeroTessera = "TS${System.currentTimeMillis().toString().takeLast(8)}"
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, 1)
        val dataScadenza = calendar.time

        val userUpdates = mapOf(
            "hasTessera" to true,
            "numeroTessera" to numeroTessera,
            "dataScadenzaTessera" to dataScadenza,
            "richiestaRinnovoInCorso" to false
        )

        FirebaseFirestore.getInstance().collection("utenti").document(utente.uid)
            .update(userUpdates)
            .addOnSuccessListener {
                Toast.makeText(context, "Tessera assegnata!", Toast.LENGTH_SHORT).show()
                onActionCompleted()
                dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Errore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rinnovaTessera() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, 1)
        val nuovaDataScadenza = calendar.time

        FirebaseFirestore.getInstance().collection("utenti").document(utente.uid)
            .update("dataScadenzaTessera", nuovaDataScadenza)
            .addOnSuccessListener {
                Toast.makeText(context, "Tessera rinnovata fino al ${dateFormatter.format(nuovaDataScadenza)}", Toast.LENGTH_SHORT).show()
                onActionCompleted()
                dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Errore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun revocaTessera() {
        AlertDialog.Builder(requireContext())
            .setTitle("Conferma Revoca")
            .setMessage("Sei sicuro di voler revocare la tessera di ${utente.nome}?")
            .setPositiveButton("Revoca") { _, _ ->
                val userUpdates = mapOf(
                    "hasTessera" to false,
                    "numeroTessera" to null,
                    "dataScadenzaTessera" to null,
                    "richiestaRinnovoInCorso" to false
                )

                FirebaseFirestore.getInstance().collection("utenti").document(utente.uid)
                    .update(userUpdates)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Tessera revocata", Toast.LENGTH_SHORT).show()
                        onActionCompleted()
                        dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Errore: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }
}
