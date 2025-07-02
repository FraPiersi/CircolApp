package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast

class InfoLocaleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_eventi, container, false)
        val eventi = listOf("Concerto Jazz", "Serata Karaoke", "Torneo di Ping Pong")
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_eventi)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ListaEventi(eventi) { evento ->
            Toast.makeText(context, "Hai selezionato: $evento", Toast.LENGTH_SHORT).show()
            // Qui puoi aprire un nuovo fragment/activity con i dettagli
        }
        return view
    }
}