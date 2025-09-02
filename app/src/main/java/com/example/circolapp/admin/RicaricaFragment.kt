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
    private            db.runTransaction { transaction ->
                transaction.update(userDocRef, "saldo", nuovoSaldo)

                // Aggiungi il movimento nella sottocollezione
                requireParentFragment().requireParentFragment().requireView().findNavController().navigate(action)
            }
            dialog.show(parentFragmentManager, "BarcodeScannerDialog")
        }
    }
}