package com.example.circolapp

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
import com.example.circolapp.ui.dialog.BarcodeScannerDialogFragment
import com.example.circolapp.viewmodel.ProductCatalogViewModel

class RiscuotiFragment : Fragment() {
    private var username: String? = null
    private var prodotti: String? = null
    private val productCatalogViewModel: ProductCatalogViewModel by viewModels()
    private val prodottiImporti = mutableListOf<Double>()

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
                    val nomeProdotto = prodotto.nome
                    val importoProdotto = prodotto.importo
                    val prodottoInfo = "$nomeProdotto (€$importoProdotto)"
                    prodotti = if (prodotti.isNullOrEmpty()) prodottoInfo else prodotti + "\n" + prodottoInfo
                    prodottiImporti.add(importoProdotto)
                    labelProdotti.text = "Prodotti:\n$prodotti"
                    val totale = prodottiImporti.sum()
                    labelTotale.text = "Totale: €$totale"
                } else {
                    Toast.makeText(requireContext(), "Prodotto non trovato!", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.show(parentFragmentManager, "BarcodeScannerDialogProdotti")
        }
        return view
    }

    // Funzione di ricerca prodotto (mock, da collegare a repository reale)
    private fun cercaProdottoPerCodice(codice: String): com.example.circolapp.model.Product? {
        val prodottiList = productCatalogViewModel.screenState.value?.products ?: emptyList()
        return prodottiList.find { it.id == codice }
    }
}
