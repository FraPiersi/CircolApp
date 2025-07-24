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

        // Gestisci il click del bottone Ordina
        btnOrdina.setOnClickListener {
            ordinaProdotto(product, editRichiesteAggiuntive.text.toString())
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

        // Esegui una transazione per gestire saldo e movimenti atomicamente
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val saldoAttuale = snapshot.getDouble("saldo") ?: 0.0

            // Verifica se l'utente ha saldo sufficiente
            if (saldoAttuale < product.importo) {
                throw Exception("Saldo insufficiente. Saldo attuale: €${saldoAttuale}, Importo richiesto: €${product.importo}")
            }

            // Calcola il nuovo saldo
            val nuovoSaldo = saldoAttuale - product.importo

            // Aggiorna il saldo dell'utente
            transaction.update(userRef, "saldo", nuovoSaldo)

            // Crea il movimento per la transazione
            val movimento = mapOf(
                "importo" to -product.importo, // Negativo perché è un pagamento
                "descrizione" to "Ordine: ${product.nome}",
                "data" to Date()
            )

            // Aggiungi il movimento alla lista movimenti dell'utente
            val movimenti = (snapshot.get("movimenti") as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()
            movimenti.add(movimento)
            transaction.update(userRef, "movimenti", movimenti)

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
            Toast.makeText(context, "Ordine completato! Importo scalato dal saldo.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack() // Torna indietro
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Errore: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
