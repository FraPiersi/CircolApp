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
    private var userRole = UserRole.UNKNOWN

    // Quota per la tessera socio
    private val QUOTA_TESSERA = 3.0 // 25 euro per la tessera
    private var currentUserSaldo = 0.0

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
        textTesseraStatus = view.findViewById(R.id.text_tessera_status)
        textNumeroTessera = view.findViewById(R.id.text_numero_tessera)
        textScadenzaTessera = view.findViewById(R.id.text_scadenza_tessera)
        buttonTessera = view.findViewById(R.id.buttonTessera)
        buttonGestisciTessere = view.findViewById(R.id.buttonGestisciTessere)
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

        buttonTessera.setOnClickListener {
            gestisciTessera()
        }

        buttonGestisciTessere.setOnClickListener {
            navigateToGestisciTessere()
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
                    currentUserSaldo = saldo // Salva il saldo corrente

                    // Ruolo
                    val ruolo = document.getString("ruolo") ?: "USER"
                    userRole = UserRole.fromString(ruolo)
                    textRuolo.text = "Ruolo: ${userRole.getDisplayName()}"

                    // Tessera socio
                    caricaDatiTessera(document)

                    // Aggiorna il testo del pulsante in base al ruolo
                    updateFeedbackButtonText()
                    updateTesseraUI()

                } else {
                    Toast.makeText(context, "Dati utente non trovati", Toast.LENGTH_SHORT).show()
                    // Usa solo i dati di Firebase Auth
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
        val hasTessera = document.getBoolean("hasTessera") ?: false
        val numeroTessera = document.getString("numeroTessera")
        val dataScadenza = document.getDate("dataScadenzaTessera")
        val richiestaInCorso = document.getBoolean("richiestaRinnovoInCorso") ?: false

        if (hasTessera && numeroTessera != null) {
            textTesseraStatus.text = "Tessera Attiva"
            textTesseraStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))

            textNumeroTessera.text = "Numero: $numeroTessera"
            textNumeroTessera.visibility = View.VISIBLE

            if (dataScadenza != null) {
                textScadenzaTessera.text = "Scadenza: ${dateFormatter.format(dataScadenza)}"
                textScadenzaTessera.visibility = View.VISIBLE

                // Controlla se la tessera è in scadenza (30 giorni)
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, 30)
                if (dataScadenza.before(calendar.time)) {
                    textScadenzaTessera.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
                }

                // Controlla se la tessera è scaduta
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
            // Nascondi anche tutte le informazioni sulla tessera per l'admin
            requireView().findViewById<TextView>(R.id.text_tessera_title)?.visibility = View.GONE
            textTesseraStatus.visibility = View.GONE
            textNumeroTessera.visibility = View.GONE
            textScadenzaTessera.visibility = View.GONE
        } else {
            buttonGestisciTessere.visibility = View.GONE
            buttonTessera.visibility = View.VISIBLE
            // Mostra le informazioni tessera per gli utenti normali
            requireView().findViewById<TextView>(R.id.text_tessera_title)?.visibility = View.VISIBLE
            textTesseraStatus.visibility = View.VISIBLE
            // textNumeroTessera e textScadenzaTessera vengono gestiti in caricaDatiTessera()
        }
    }

    private fun gestisciTessera() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        FirebaseFirestore.getInstance().collection("utenti")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val hasTessera = document.getBoolean("hasTessera") ?: false
                val richiestaInCorso = document.getBoolean("richiestaRinnovoInCorso") ?: false
                val dataScadenza = document.getDate("dataScadenzaTessera")

                when {
                    richiestaInCorso -> {
                        Toast.makeText(context, "Hai già una richiesta in corso", Toast.LENGTH_SHORT).show()
                    }
                    !hasTessera -> {
                        mostraDialogRichiestaTessera(TipoRichiesta.NUOVA)
                    }
                    dataScadenza != null && dataScadenza.before(Date()) -> {
                        mostraDialogRichiestaTessera(TipoRichiesta.RINNOVO)
                    }
                    else -> {
                        Toast.makeText(context, "La tua tessera è ancora valida", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun mostraDialogRichiestaTessera(tipo: TipoRichiesta) {
        // Controlla se l'utente ha saldo sufficiente
        if (currentUserSaldo < QUOTA_TESSERA) {
            AlertDialog.Builder(requireContext())
                .setTitle("Saldo Insufficiente")
                .setMessage("Per richiedere la tessera socio è necessario avere un saldo di almeno ${currencyFormatter.format(QUOTA_TESSERA)}.\n\nIl tuo saldo attuale è: ${currencyFormatter.format(currentUserSaldo)}")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val title = if (tipo == TipoRichiesta.NUOVA) "Richiedi Tessera Socio" else "Rinnova Tessera Socio"
        val message = if (tipo == TipoRichiesta.NUOVA)
            "Vuoi richiedere una nuova tessera socio?\n\nCosto: ${currencyFormatter.format(QUOTA_TESSERA)}\nSaldo attuale: ${currencyFormatter.format(currentUserSaldo)}\nSaldo dopo il pagamento: ${currencyFormatter.format(currentUserSaldo - QUOTA_TESSERA)}"
        else
            "Vuoi rinnovare la tua tessera socio?\n\nCosto: ${currencyFormatter.format(QUOTA_TESSERA)}\nSaldo attuale: ${currencyFormatter.format(currentUserSaldo)}\nSaldo dopo il pagamento: ${currencyFormatter.format(currentUserSaldo - QUOTA_TESSERA)}"

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Conferma Pagamento") { _, _ ->
                inviaRichiestaTessera(tipo)
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun inviaRichiestaTessera(tipo: TipoRichiesta) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        // Prima effettua il pagamento
        val nuovoSaldo = currentUserSaldo - QUOTA_TESSERA
        val userRef = db.collection("utenti").document(currentUser.uid)

        // Aggiorna saldo e aggiunge movimento
        val movimentoData = mapOf(
            "importo" to -QUOTA_TESSERA,
            "descrizione" to "Pagamento tessera socio",
            "data" to Date()
        )

        db.runTransaction { transaction ->
            // PRIMA: Esegui tutte le letture
            val userDoc = transaction.get(userRef)

            // DOPO: Esegui tutte le scritture
            // Aggiorna il saldo
            transaction.update(userRef, "saldo", nuovoSaldo)

            // Aggiungi il movimento nella sottocollezione movimenti
            val movimentoDataForSubcollection = mapOf(
                "importo" to -QUOTA_TESSERA,
                "descrizione" to "Pagamento tessera socio",
                "data" to com.google.firebase.Timestamp(Date())
            )
            val movimentoRef = userRef.collection("movimenti").document()
            transaction.set(movimentoRef, movimentoDataForSubcollection)

            // Aggiorna il flag di richiesta in corso
            transaction.update(userRef, "richiestaRinnovoInCorso", true)

            // Crea la richiesta tessera
            val richiestaRef = db.collection("richieste_tessera").document()
            val richiesta = RichiestaTessera(
                id = richiestaRef.id,
                uidUtente = currentUser.uid,
                nomeUtente = currentUser.displayName ?: "Utente",
                emailUtente = currentUser.email ?: "",
                tipo = tipo
            )
            transaction.set(richiestaRef, richiesta)
        }.addOnSuccessListener {
            Toast.makeText(context, "Pagamento effettuato e richiesta inviata con successo!", Toast.LENGTH_SHORT).show()
            caricaDatiUtente() // Ricarica i dati per aggiornare l'UI
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