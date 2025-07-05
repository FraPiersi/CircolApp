package com.example.circolapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.circolapp.adapters.ProductCatalogAdapter
import com.example.circolapp.databinding.FragmentCatalogoBinding
import com.example.circolapp.model.UserRole // Importa UserRole
import com.example.circolapp.viewmodel.ProductCatalogViewModel
import androidx.navigation.fragment.findNavController

class CatalogoFragment : Fragment() {

    private var _binding: FragmentCatalogoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductCatalogViewModel by viewModels()
    private var productAdapter: ProductCatalogAdapter? = null // Inizializza a null o lateinit

    // private val args: ProductCatalogFragmentArgs by navArgs() // RIMOSSO
    // private lateinit var currentUserRole: UserRole // RIMOSSO

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_catalogo, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // viewModel.initialize(...) // RIMOSSO - il ViewModel si inizializza da solo
        // setupRecyclerView() // Chiamato da observeScreenState quando il ruolo è noto
        setupCategorySpinner() // Può essere chiamato, ma l'adapter si popola con lo stato
        observeScreenState()
    }


    private fun setupCategorySpinner() {
        binding.spinnerCategoryFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = parent?.getItemAtPosition(position) as? String
                selectedCategory?.let {
                    viewModel.selectCategory(it)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeScreenState() {
        viewModel.screenState.observe(viewLifecycleOwner, Observer { state ->
            Log.d("CatalogFragment", "Nuovo Stato UI: $state")

            binding.progressBarCatalog.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            state.currentUserRole?.let { role ->
                if (role == UserRole.ADMIN) {
                    binding.fabAddProductCatalog.visibility = View.VISIBLE
                    binding.fabAddProductCatalog.setOnClickListener {
                        // Naviga per AGGIUNGERE un nuovo prodotto (productId è null)
                        val action = CatalogoFragmentDirections.actionCatalogoFragmentToAddEditProductFragment(null)
                        findNavController().navigate(action)
                    }
                } else {
                    binding.fabAddProductCatalog.visibility = View.GONE
                }
                setupRecyclerViewWithRole(role)
            } ?: run {
                // Il ruolo non è ancora disponibile o è nullo (es. utente non loggato)
                binding.fabAddProductCatalog.visibility = View.GONE
                // Potresti voler nascondere la RecyclerView o mostrare un messaggio specifico
                // se il ruolo è null e l'utente non dovrebbe vedere il catalogo.
                // L'adapter potrebbe essere lasciato a null o mostrare uno stato vuoto.
                productAdapter?.submitList(emptyList()) // Svuota l'adapter se il ruolo non c'è
            }

            // --- Aggiornamento RecyclerView Products (lista effettiva) ---
            // L'adapter viene creato/aggiornato in setupRecyclerViewWithRole, qui passiamo solo i dati
            if (productAdapter != null) {
                Log.d("CatalogFragment_Adapter", "Prima di submitList. Adapter NON null. Products count: ${state.products.size}")
                productAdapter?.submitList(state.products)
                Log.d("CatalogFragment_Adapter", "Dopo submitList. Adapter item count: ${productAdapter?.itemCount}")
            } else {
                Log.e("CatalogFragment_Adapter", "Adapter è NULL prima di submitList!")
            }


            if (!state.isLoading && state.products.isNotEmpty() && state.errorMessage == null) {
                binding.recyclerViewProducts.visibility = View.VISIBLE
            } else if (!state.isLoading && state.currentUserRole != null) { // Non mostrare se il ruolo non c'è e non si sta caricando
                binding.recyclerViewProducts.visibility = View.GONE
            }


            // --- Gestione TextView No Products / Error Message ---
            if (!state.isLoading && state.errorMessage != null) {
                binding.textViewNoProductsCatalog.visibility = View.VISIBLE
                binding.textViewNoProductsCatalog.text = state.errorMessage
            } else if (!state.isLoading && state.products.isEmpty() && state.currentUserRole != null && state.errorMessage == null) {
                // Mostra "Nessun prodotto" solo se il ruolo è noto e non ci sono errori
                binding.textViewNoProductsCatalog.visibility = View.VISIBLE
                binding.textViewNoProductsCatalog.text = "Nessun prodotto disponibile in questa categoria."
            } else {
                binding.textViewNoProductsCatalog.visibility = View.GONE
            }

            // --- Setup Spinner Categories ---
            if (state.categories.isNotEmpty() && isAdded) {
                if (binding.spinnerCategoryFilter.adapter == null ||
                    (binding.spinnerCategoryFilter.adapter as? ArrayAdapter<String>)?.count != state.categories.size ||
                    (binding.spinnerCategoryFilter.adapter as? ArrayAdapter<String>)?.getItem(0) != state.categories.getOrNull(0) // Controllo più robusto
                ) {
                    val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, state.categories)
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerCategoryFilter.adapter = spinnerAdapter
                }
                binding.spinnerCategoryFilter.visibility = View.VISIBLE

                state.selectedCategory?.let { currentCategoryInState ->
                    val position = state.categories.indexOf(currentCategoryInState)
                    if (position >= 0 && binding.spinnerCategoryFilter.selectedItemPosition != position) {
                        binding.spinnerCategoryFilter.setSelection(position, false)
                    }
                }
            } else if (isAdded) {
                binding.spinnerCategoryFilter.visibility = View.GONE
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewProducts.adapter = null
        _binding = null
    }

    private fun setupRecyclerViewWithRole(userRole: UserRole) {
        if (productAdapter == null || productAdapter?.userRole != userRole) {
            productAdapter = ProductCatalogAdapter(
                userRole = userRole,
                onProductClick = { product ->
                    handleProductClick(product, userRole) // Chiama una funzione del Fragment
                },
                onAddToCartClick = { product ->
                    viewModel.addProductToCart(product)
                    Toast.makeText(context, "${product.nome} aggiunto al carrello", Toast.LENGTH_SHORT).show()
                }
            )
            binding.recyclerViewProducts.layoutManager = GridLayoutManager(context, 2)
            binding.recyclerViewProducts.adapter = productAdapter
        }
    }

    private fun handleProductClick(product: com.example.circolapp.model.Product, role: UserRole) {
        if (role == UserRole.ADMIN) {
            product.id?.let { productId ->
                val action = CatalogoFragmentDirections.actionCatalogoFragmentToAddEditProductFragment(productId)
                findNavController().navigate(action)
            } ?: Toast.makeText(context, "ID prodotto mancante per la modifica.", Toast.LENGTH_SHORT).show()
        } else {
            // Naviga al dettaglio prodotto per utenti normali
            Toast.makeText(context, "Utente clicca per dettaglio: ${product.nome}", Toast.LENGTH_SHORT).show()
        }
    }
}
