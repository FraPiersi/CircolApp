package com.example.circolapp.ui.admin // o il tuo package

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.circolapp.R
import com.example.circolapp.databinding.FragmentAddEditProductBinding
import com.example.circolapp.viewmodel.AddEditProductEvent
import com.example.circolapp.viewmodel.AddEditProductViewModel

class AddEditProductFragment : Fragment() {

    private var _binding: FragmentAddEditProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditProductViewModel by viewModels()
    private val args: AddEditProductFragmentArgs by navArgs() // productId sarà null per nuovi prodotti

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_edit_product, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.start(args.productId) // Passa l'ID del prodotto (può essere null)

        setupObservers()

        if (args.productId != null) {
            // Modalità Modifica
            (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = "Modifica Prodotto"
            binding.btnDeleteProduct.visibility = View.VISIBLE
        } else {
            // Modalità Aggiungi
            (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = "Aggiungi Prodotto"
            binding.btnDeleteProduct.visibility = View.GONE
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBarAddEdit.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveProduct.isEnabled = !isLoading
            binding.btnDeleteProduct.isEnabled = !isLoading // Anche il pulsante elimina
        })

        viewModel.event.observe(viewLifecycleOwner, Observer { event ->
            event?.let {
                when (it) {
                    is AddEditProductEvent.ProductSaved -> {
                        Toast.makeText(context, "Prodotto salvato con successo!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is AddEditProductEvent.ProductDeleted -> {
                        Toast.makeText(context, "Prodotto eliminato!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is AddEditProductEvent.Error -> {
                        Toast.makeText(context, "Errore: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                }
                viewModel.onEventHandled() // Segnala che l'evento è stato gestito
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

