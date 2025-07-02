package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView

class ProfiloFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profilo, container, false)
        // Puoi impostare dinamicamente le info utente qui se necessario
        view.findViewById<TextView>(R.id.text_nome).text = "Mario Rossi"
        view.findViewById<TextView>(R.id.text_email).text = "mario.rossi@email.com"
        view.findViewById<TextView>(R.id.text_telefono).text = "Telefono: 3331234567"
        view.findViewById<ImageView>(R.id.image_profilo).setImageResource(R.drawable.account)
        return view
    }
}