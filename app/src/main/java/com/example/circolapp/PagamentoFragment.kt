package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore



class PagamentoFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pagamento, container, false)

        view.findViewById<Button>(R.id.btn_opzione1).setOnClickListener {

            /*var db: FirebaseFirestore = FirebaseFirestore.getInstance()
            val user = hashMapOf(
                "username" to "Ada",
                "password" to "Lovelace",
                "livello" to "Lovelace"
            )

            db.collection("utenti")
                .add(user)*/
           parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, QrCodeFragment())
                .addToBackStack(null)
                .commit()
        }
        view.findViewById<Button>(R.id.btn_opzione2).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CatalogoFragment())
                .addToBackStack(null)
                .commit()
        }
        view.findViewById<Button>(R.id.btn_opzione3).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OrdinazioneFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }


}
