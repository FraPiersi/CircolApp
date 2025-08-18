package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.circolapp.databinding.FragmentAddEventoBinding

class AddEventoFragment : Fragment() {
    private var _binding: FragmentAddEventoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_evento, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = requireActivity().let {
            androidx.lifecycle.ViewModelProvider(it).get(com.example.circolapp.viewmodel.EventiViewModel::class.java)
        }
        binding.btnSalvaEvento.setOnClickListener {
            val nome = binding.editNomeEvento.text.toString().trim()
            val descrizione = binding.editDescrizioneEvento.text.toString().trim()
            if (nome.isEmpty()) {
                Toast.makeText(requireContext(), "Inserisci il nome dell'evento", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val nuovoEvento = com.example.circolapp.model.Evento(
                nome = nome,
                descrizione = descrizione
            )
            viewModel.addEvento(nuovoEvento,
                onComplete = {
                    Toast.makeText(requireContext(), "Evento aggiunto!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                },
                onError = {
                    Toast.makeText(requireContext(), "Errore: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
        binding.btnAnnullaEvento.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
