package com.example.circolapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.circolapp.R
import com.example.circolapp.ui.dialog.BarcodeScannerDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class RicaricaFragment : Fragment() {
    private val args: RicaricaFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ricarica, container, false)
        val tvUsername = view.findViewById<TextView>(R.id.tvUsername)
        val etImporto = view.findViewById<EditText>(R.id.etImporto)
        val btnConferma = view.findViewById<Button>(R.id.btnConfermaRicarica)

        tvUsername.text = args.username
        btnConferma.setOnClickListener {
            val importoText = etImporto.text.toString()
            val importo = importoText.toDoubleOrNull()
            if (importo == null || importo <= 0.0) {
                etImporto.error = "Importo non valido"
                return@setOnClickListener
            }
            val uidQr = args.username
            val db = FirebaseFirestore.getInstance()
            val utentiRef = db.collection("utenti")

            db.runTransaction { transaction ->
                val userDocRef = utentiRef.document(uidQr)
                val userSnapshot = transaction.get(userDocRef)

                if (!userSnapshot.exists()) {
                    throw Exception("Utente non trovato")
                }

                val saldoAttuale = userSnapshot.getDouble("saldo") ?: 0.0
                val nuovoSaldo = saldoAttuale + importo

                transaction.update(userDocRef, "saldo", nuovoSaldo)

                val movimento = hashMapOf(
                    "importo" to importo,
                    "descrizione" to "Ricarica in cassa",
                    "data" to Timestamp.now()
                )
                val movimentoRef = userDocRef.collection("movimenti").document()
                transaction.set(movimentoRef, movimento)

            }.addOnSuccessListener {
                Toast.makeText(requireContext(), "Ricarica effettuata!", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }.addOnFailureListener { e ->
                etImporto.error = "Errore durante la ricarica: ${e.message}"
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.username.isBlank()) {
            val dialog = BarcodeScannerDialogFragment { qrCode ->
                val action = RicaricaFragmentDirections.actionRicaricaFragmentSelf(qrCode)
                requireParentFragment().requireParentFragment().requireView().findNavController().navigate(action)
            }
            dialog.show(parentFragmentManager, "BarcodeScannerDialog")
        }
    }
}