package com.example.circolapp.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.circolapp.R

class CassaFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cassa, container, false)
        val btnRicarica = view.findViewById<Button>(R.id.btnRicarica)
        val btnRiscuoti = view.findViewById<Button>(R.id.btnRiscuoti)
        // Qui puoi aggiungere eventuali listener ai bottoni
        return view
    }
}