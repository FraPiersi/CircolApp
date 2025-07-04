package com.example.circolapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.circolapp.adapter.ProductListAdapter
import com.example.circolapp.databinding.FragmentCatalogoBinding
import com.example.circolapp.model.Product
import com.example.circolapp.viewmodel.ProductCatalogViewModel

class CatalogoFragment : Fragment() {

    private var _binding: FragmentCatalogoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductCatalogViewModel by viewModels()
    private lateinit var productListAdapter: ProductListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_catalogo, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupObservers()
    }

    private fun setupToolbar() {
        // Se vuoi usare la toolbar del fragment e non quella dell'activity
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarProductCatalog)
        // Se usi la toolbar dell'activity, potresti volerla nascondere o configurarla diversamente
        // binding.toolbarProductCatalog.setNavigationOnClickListener { findNavController().popBackStack() }
    }

    private fun setupRecyclerView() {
        productListAdapter = ProductListAdapter { product ->
            // Azione al click su un prodotto, es. naviga a dettagli prodotto
            Toast.makeText(context, "Prodotto cliccato: ${product.nome}", Toast.LENGTH_SHORT).show()
            // Esempio di navigazione (dovrai definire questa azione nel nav_graph)
            // val action = ProductCatalogFragmentDirections.actionProductCatalogFragmentToProductDetailFragment(product.id)
            // findNavController().navigate(action)
        }

        // Usa LinearLayoutManager per una lista semplice o GridLayoutManager per una griglia
        binding.recyclerViewProducts.apply {
            // layoutManager = GridLayoutManager(context, 2) // Esempio per griglia a 2 colonne
            adapter = productListAdapter
        }
    }

    private fun setupSearch() {
        // L'databinding con text="@={viewModel.searchQuery}" gestisce l'aggiornamento.
        // Se vuoi gestione più fine, usa doAfterTextChanged. Il ViewModel già osserva searchQuery.
        // binding.editTextSearchProduct.doAfterTextChanged { text ->
        //     viewModel.searchQuery.value = text.toString()
        // }
    }

    private fun setupObservers() {
        viewModel.filteredProducts.observe(viewLifecycleOwner) { products ->
            productListAdapter.submitList(products)
            binding.textViewNoResults.visibility = if (products.isEmpty() && !viewModel.isLoading.value!! && viewModel.errorMessage.value == null) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // La visibilità del ProgressBar è già gestita dal databinding
            // Puoi aggiungere qui logica per disabilitare/abilitare la UI
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            // La visibilità del TextView di errore è già gestita dal databinding
            error?.let {
                Log.e("ProductCatalogFragment", "Errore: $it")
                // Potresti mostrare un Toast o Snackbar aggiuntivo se necessario
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewProducts.adapter = null // Pulisci l'adapter per evitare leak
        _binding = null
    }
}