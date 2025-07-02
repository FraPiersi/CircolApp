package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class InfoEventoFragment : Fragment() {

    companion object {
        private const val ARG_NOME_EVENTO = "nome_evento"
        fun newInstance(nomeEvento: String): InfoEventoFragment {
            val fragment = InfoEventoFragment()
            val args = Bundle()
            args.putString(ARG_NOME_EVENTO, nomeEvento)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_info_evento, container, false)
        val nomeEvento = arguments?.getString(ARG_NOME_EVENTO) ?: "Evento"
        view.findViewById<TextView>(R.id.text_nome_evento).text = nomeEvento
        view.findViewById<TextView>(R.id.text_descrizione_evento).text = "Descrizione di $nomeEvento"

        view.findViewById<Button>(R.id.button_partecipa).setOnClickListener {
            Toast.makeText(context, "Richiesta di partecipazione inviata!", Toast.LENGTH_SHORT).show()
        }
        return view
    }
}