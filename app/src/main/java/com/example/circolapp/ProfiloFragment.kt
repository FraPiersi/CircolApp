package com.example.circolapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import com.firebase.ui.auth.AuthUI

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
        val logoutButton = view.findViewById<Button>(R.id.buttonLogout)
        logoutButton.setOnClickListener {
            AuthUI.getInstance().signOut(requireContext()).addOnCompleteListener {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        return view
    }
}