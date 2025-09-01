package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.circolapp.model.Ordine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class DettaglioProdottoFragment : Fragment() {

    private val args: DettaglioProdottoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dettaglio_prodotto, container, false)

        val product = args.product

        // Trova le views
        val textNomeProdotto = view.findViewById<TextView>(R.id.textViewNomeProdotto)
        val textDescrizioneProdotto = view.findViewById<TextView>(R.id.textViewDescrizioneProdotto)
        val textPrezzoProdotto = view.findViewById<TextView>(R.id.textViewPrezzoProdotto)
        val editRichiesteAggiuntive = view.findViewById<EditText>(R.id.editTextRichiesteAggiuntive)
        val btnOrdina = view.findViewById<Button>(R.id.btnOrdina)

        // Popola i dati del prodotto
        textNomeProdotto.text = product.nome
        textDescrizioneProdotto.text = product.descrizione.ifBlank { "Nessuna descrizione disponibile" }
        textPrezzoProdotto.text = "€${product.importo}"

        // Mostra/nascondi i controlli di ordinazione in base alla proprietà ordinabile
        val labelRichieste = view.findViewById<TextView>(R.id.labelRichiesteAggiuntive)
        if (product.ordinabile) {
            // Prodotto ordinabile: mostra casella di testo e bottone
            labelRichieste.visibility = View.VISIBLE
            editRichiesteAggiuntive.visibility = View.VISIBLE
            btnOrdina.visibility = View.VISIBLE

            
            btnOrdina.setOnClickListener {
                ordinaProdotto(product, editRichiesteAggiuntive.text.toString())
            }
        } else {
            // Prodotto non ordinabile: nascondi casella di testo e bottone
            labelRichieste.visibility = View.GONE
            editRichiesteAggiuntive.visibility = View.GONE
            btnOrdina.visibility = View.GONE
        }

        return view
    }

    private fun ordinaProdotto(product: com.example.circolapp.model.Product, richiesteAggiuntive: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Utente non autenticato", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("utenti").document(currentUser.uid)
        val productRef = db.collection("prodotti").document(product.id)

        // Esegui una transazione per gestire saldo, movimenti e quantità prodotto atomicamente
        db.runTransaction { transaction ->
            val userSnapshot = transaction.get(userRef)
            val productSnapshot = transaction.get(productRef)

            val saldoAttuale = userSnapshot.getDouble("saldo") ?: 0.0
            val quantitaAttuale = productSnapshot.getLong("numeroPezzi")?.toInt() ?: 0

            // Verifica se il prodotto è disponibile
            if (quantitaAttuale <= 0) {
                throw Exception("Prodotto non disponibile. Quantità esaurita.")
            }

            // Verifica se l'utente ha saldo sufficiente
            if (saldoAttuale < product.importo) {
                throw Exception("Saldo insufficiente. Saldo attuale: €${saldoAttuale}, Importo richiesto: €${product.importo}")
            }

            // Calcola il nuovo saldo e la nuova quantità
            val nuovoSaldo = saldoAttuale - product.importo
            val nuovaQuantita = quantitaAttuale - 1

            // Aggiorna il saldo dell'utente
            transaction.update(userRef, "saldo", nuovoSaldo)

            // Aggiorna la quantità del prodotto
            transaction.update(productRef, "numeroPezzi", nuovaQuantita)

            // Crea il movimento per la transazione nella sottocollezione
            val movimentoData = mapOf(
                "importo" to -product.importo, // Negativo perché è un pagamento
                "descrizione" to "Ordine: ${product.nome}",
                "data" to com.google.firebase.Timestamp(Date())
            )

            // Aggiungi il movimento nella sottocollezione movimenti
            val movimentoRef = userRef.collection("movimenti").document()
            transaction.set(movimentoRef, movimentoData)

            // Crea l'ordine
            val ordine = Ordine(
                uidUtente = currentUser.uid,
                nomeProdotto = product.nome,
                prodottoId = product.id ?: "",
                richiesteAggiuntive = richiesteAggiuntive.ifBlank { null },
                timestamp = Date(),
                stato = "INVIATO"
            )

            // Salva l'ordine nella collection ordinazioni
            val ordineRef = db.collection("ordinazioni").document()
            transaction.set(ordineRef, ordine)

        }.addOnSuccessListener {
            Toast.makeText(context, "Ordine completato! Importo scalato dal saldo e quantità aggiornata.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack() // Torna indietro
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Errore: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
