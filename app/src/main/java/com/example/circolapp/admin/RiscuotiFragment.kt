package com.example.circolapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.circolapp.R
import com.example.circolapp.model.Product
import com.example.circolapp.ui.dialog.BarcodeScannerDialogFragment
import com.example.circolapp.viewmodel.ProductCatalogViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class RiscuotiFragment : Fragment() {
    private var username: String? = null
    private var prodotti: String? = null
    private val productCatalogViewModel: ProductCatalogViewModel by viewModels()
    private val prodottiImporti = mutableListOf<Double>()
    private val prodottiAcquistati = mutableListOf<Product>() // Lista dei prodotti acquistati

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            username = it.getString("username")
            prodotti = it.getString("prodotti")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_riscuoti, container, false)
        val labelUtente = view.findViewById<TextView>(R.id.labelUtente)
        val btnScanUtente = view.findViewById<Button>(R.id.btnScanUtente)
        val labelProdotti = view.findViewById<TextView>(R.id.labelProdotti)
        val btnScanProdotti = view.findViewById<Button>(R.id.btnScanProdotti)
        val labelTotale = view.findViewById<TextView>(R.id.labelTotale)
        val btnFine = view.findViewById<Button>(R.id.btnFine)

        labelUtente.text = "Utente: ${username ?: "Non selezionato"}"
        labelProdotti.text = "Prodotti: ${prodotti ?: "Nessun prodotto"}"

        btnScanUtente.setOnClickListener {
            val dialog = BarcodeScannerDialogFragment { qrCode ->
                labelUtente.text = "Utente: $qrCode"
                username = qrCode
            }
            dialog.show(parentFragmentManager, "BarcodeScannerDialogUtente")
        }

        btnScanProdotti.setOnClickListener {
            val dialog = BarcodeScannerDialogFragment { barcode ->
                val prodotto = cercaProdottoPerCodice(barcode)
                if (prodotto != null) {
                    // Verifica se il prodotto è disponibile
                    if (prodotto.numeroPezzi <= 0) {
                        Toast.makeText(
                            requireContext(),
                            "Prodotto '${prodotto.nome}' non disponibile (quantità esaurita)!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@BarcodeScannerDialogFragment
                    }

                    val nomeProdotto = prodotto.nome
                    val importoProdotto = prodotto.importo
                    val prodottoInfo = "$nomeProdotto (€$importoProdotto)"
                    prodotti =
                        if (prodotti.isNullOrEmpty()) prodottoInfo else prodotti + "\n" + prodottoInfo
                    prodottiImporti.add(importoProdotto)
                    prodottiAcquistati.add(prodotto) 
                    labelProdotti.text = "Prodotti:\n$prodotti"
                    val totale = prodottiImporti.sum()
                    labelTotale.text = "Totale: €$totale"
                } else {
                    Toast.makeText(requireContext(), "Prodotto non trovato!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            dialog.show(parentFragmentManager, "BarcodeScannerDialogProdotti")
        }

        btnFine.setOnClickListener {
            val uid = username
            val totale = prodottiImporti.sum()
            if (uid.isNullOrBlank() || totale == 0.0) {
                Toast.makeText(requireContext(), "Seleziona utente e prodotti", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("utenti").document(uid)

            // Esegui una transazione per gestire saldo, movimenti e quantità prodotti atomicamente
            db.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef)
                val saldoAttuale = userSnapshot.getDouble("saldo") ?: 0.0

                // Verifica saldo sufficiente
                if (saldoAttuale < totale) {
                    throw Exception("Saldo insufficiente. Saldo attuale: €$saldoAttuale, Totale richiesto: €$totale")
                }

                // Verifica disponibilità di tutti i prodotti e aggiorna le quantità
                for (prodotto in prodottiAcquistati) {
                    val productRef = db.collection("prodotti").document(prodotto.id)
                    val productSnapshot = transaction.get(productRef)
                    val quantitaAttuale = productSnapshot.getLong("numeroPezzi")?.toInt() ?: 0

                    if (quantitaAttuale <= 0) {
                        throw Exception("Prodotto '${prodotto.nome}' non più disponibile")
                    }

                    // Decrementa la quantità del prodotto
                    val nuovaQuantita = quantitaAttuale - 1
                    transaction.update(productRef, "numeroPezzi", nuovaQuantita)
                }

                // Aggiorna il saldo dell'utente
                val nuovoSaldo = saldoAttuale - totale
                transaction.update(userRef, "saldo", nuovoSaldo)

                // Registra il movimento nella sottocollezione
                val movimentoData = mapOf(
                    "importo" to -totale,
                    "descrizione" to "Pagamento in cassa",
                    "data" to Timestamp(Date())
                )
                val movimentoRef = userRef.collection("movimenti").document()
                transaction.set(movimentoRef, movimentoData)

            }.addOnSuccessListener {
                Toast.makeText(requireContext(), "Pagamento registrato e quantità aggiornate!", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Errore: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    // Funzione di ricerca prodotto (mock, da collegare a repository reale)
    private fun cercaProdottoPerCodice(codice: String): Product? {
        val prodottiList = productCatalogViewModel.screenState.value?.products ?: emptyList()
        return prodottiList.find { it.id == codice }
    }
}