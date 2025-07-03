// DettaglioEventoFragment.kt
package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.circolapp.databinding.FragmentInfoEventoBinding // Assicurati di avere questo layout
import com.example.circolapp.model.Evento

class InfoEventoFragment : Fragment() {

    private var _binding: FragmentInfoEventoBinding? = null
    private val binding get() = _binding!!

    // Recupera gli argomenti passati in modo typesafe
    private val args: InfoEventoFragmentArgs by navArgs()
    private lateinit var evento: Evento

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        evento = args.evento!! // Ottieni l'oggetto Evento
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_info_evento, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Usa l'oggetto 'evento' per popolare le viste
        binding.evento = evento // Se usi Data Binding nel layout del dettaglio
        // Altrimenti, imposta manualmente i testi, immagini, ecc.
        // binding.nomeEventoTextView.text = evento.nome
        // binding.descrizioneEventoTextView.text = evento.descrizione
        // ... e così via
        binding.executePendingBindings() // Se usi Data Binding
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}