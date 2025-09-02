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

    private        textNomeProdotto.text = product.nome
        textDescrizioneProdotto.text = product.descrizione.ifBlank { "Nessuna descrizione disponibile" }
        textPrezzoProdotto.text = "€${product.importo}"

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
        db.runTransaction { transaction ->
            if (quantitaAttuale <= 0) {
                throw Exception("Prodotto non disponibile. Quantità esaurita.")
            }

            if (saldoAttuale < product.importo) {
                throw Exception("Saldo insufficiente. Saldo attuale: €${saldoAttuale}, Importo richiesto: €${product.importo}")
            }

            // Calcola il nuovo saldo e la nuova quantità
            transaction.update(userRef, "saldo", nuovoSaldo)

            transaction.update(productRef, "numeroPezzi", nuovaQuantita)

                "descrizione" to "Ordine: ${product.nome}",
                "data" to com.google.firebase.Timestamp(Date())
            )

            // Aggiungi il movimento nella sottocollezione movimenti
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Errore: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
