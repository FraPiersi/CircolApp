// PagamentoFragment.kt
package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController // Importa findNavController
// Non c'è bisogno di FirebaseFirestore qui se non la usi direttamente in questo listener

class PagamentoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pagamento, container, false)

        // Bottone per navigare a QrCodeFragment
        view.findViewById<Button>(R.id.btn_opzione1).setOnClickListener {
            // Usa l'azione definita nel nav_graph per navigare
            // Safe Args genererà una classe PagamentoFragmentDirections
            val action = PagamentoFragmentDirections.actionPagamentoFragmentToQrCodeFragment()
            findNavController().navigate(action)

        }

        // Bottone per navigare a CatalogoFragment
        view.findViewById<Button>(R.id.btn_opzione2).setOnClickListener {
            // Assumendo che tu abbia un'action definita anche per questo nel nav_graph
            // e che tu voglia usare il Navigation Component per coerenza.
            val action = PagamentoFragmentDirections.actionPagamentoFragmentToCatalogoFragment()
            findNavController().navigate(action)

        }

        // Bottone per navigare a OrdinazioneFragment
        view.findViewById<Button>(R.id.btn_opzione3).setOnClickListener {
            // Assumendo che tu abbia un'action definita anche per questo nel nav_graph
            val action = PagamentoFragmentDirections.actionPagamentoFragmentToOrdinazioneFragment()
            findNavController().navigate(action)

        }

        return view
    }
}