package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.circolapp.databinding.FragmentOrdinazioneBinding
import com.example.circolapp.viewmodel.OrdinazioneViewModel
import com.google.android.material.snackbar.Snackbar

class OrdinazioneFragment : Fragment() {

    private var _binding: FragmentOrdinazioneBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrdinazioneViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ordinazione, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupProductSpinner()
        setupListeners()
        observeViewModel()
    }

    private fun setupProductSpinner() {
        viewModel.productNamesAdapter.observe(viewLifecycleOwner) { adapter ->
            binding.autoCompleteTextViewProduct.setAdapter(adapter)
            // Se vuoi pre-selezionare o gestire il ripristino dello stato
            viewModel.selectedProduct.value?.let { product ->
                val position = viewModel.products.value?.indexOf(product) ?: -1
                if (position != -1) {
                    binding.autoCompleteTextViewProduct.setText(product.nome, false)
                }
            }
        }

        binding.autoCompleteTextViewProduct.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                viewModel.onProductSelected(position)
                // Puoi anche aggiornare un LiveData nel ViewModel con il nome del prodotto selezionato
                // binding.autoCompleteTextViewProduct.text.toString()
            }
    }


    private fun setupListeners() {
        binding.buttonInviaOrdine.setOnClickListener {
            // La validazione di base (prodotto selezionato) è gestita dall'enabled del bottone
            // e dalla logica del ViewModel
            viewModel.richiesteAggiuntive.value = binding.editTextRichiesteAggiuntive.text.toString()
            viewModel.inviaOrdine()
        }
    }

    private fun observeViewModel() {
        viewModel.ordineInviato.observe(viewLifecycleOwner) { inviato ->
            if (inviato) {
                Snackbar.make(binding.root, "Ordine inviato con successo!", Snackbar.LENGTH_LONG).show()
                // Pulisci i campi manualmente o lascia che il ViewModel lo faccia
                binding.autoCompleteTextViewProduct.setText("", false) // Pulisce lo spinner
                binding.editTextRichiesteAggiuntive.text?.clear()
                viewModel.onOrdineInviatoHandled() // Resetta lo stato nel ViewModel
                // findNavController().popBackStack() // Opzionale: torna indietro dopo l'ordine
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.onErrorMessageHandled() // Resetta lo stato nel ViewModel
            }
        }

        // Se vuoi resettare il testo dello spinner quando selectedProduct nel ViewModel diventa null
        viewModel.selectedProduct.observe(viewLifecycleOwner) { product ->
            if (product == null && binding.autoCompleteTextViewProduct.text.isNotEmpty()) {
                binding.autoCompleteTextViewProduct.setText("", false)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}