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
    private                for (prodotto in prodottiAcquistati) {
    private fun cercaProdottoPerCodice(codice: String): Product? {
        val prodottiList = productCatalogViewModel.screenState.value?.products ?: emptyList()
        return prodottiList.find { it.id == codice }
    }
}