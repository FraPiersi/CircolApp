package com.example.circolapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.circolapp.adapter.ProductCatalogAdapter
import com.example.circolapp.databinding.FragmentProductCatalogBinding
import com.example.circolapp.model.Product
import com.example.circolapp.model.UserRole
import com.example.circolapp.viewmodel.ProductCatalogViewModel
import com.example.circolapp.viewmodel.ProductCatalogScreenState

class ProductCatalogFragment : Fragment() {
    private var _binding: FragmentProductCatalogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductCatalogViewModel by viewModels()
    private lateinit var productAdapter: ProductCatalogAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_product_catalog, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewProducts.layoutManager = GridLayoutManager(context, 2)
        observeScreenState()
    }

    override fun onResume() {
        super.onResume()
        Log.d("ProductCatalogFragment", "onResume chiamato")

        // Assicurati sempre che l'adapter sia configurato
        val currentState = viewModel.screenState.value
        if (currentState != null) {
            Log.d("ProductCatalogFragment", "onResume: riconfigurazione UI con stato esistente")

            // Forza la riconfigurazione dell'adapter
            forceReconfigureAdapter(currentState.currentUserRole, currentState.products)

            updateUI(currentState)
        }

        // Solo ricarica se non ci sono già prodotti caricati o se c'è stato un errore
        if (currentState?.products?.isEmpty() == true && !currentState.isLoading) {
            Log.d("ProductCatalogFragment", "onResume: ricaricando dati perché lista vuota")
            viewModel.refreshData()
        } else {
            Log.d("ProductCatalogFragment", "onResume: dati già presenti, non ricarico")
        }
    }

    private fun forceReconfigureAdapter(userRole: UserRole?, products: List<Product>) {
        Log.d("ProductCatalogFragment", "forceReconfigureAdapter: userRole=$userRole, products.size=${products.size}")

        if (userRole != null) {
            // Forza la ricreazione dell'adapter
            productAdapter = ProductCatalogAdapter(
                userRole = userRole,
                onProductClick = { product -> handleProductClick(product) },
                onAddToCartClick = { product ->
                    viewModel.addProductToCart(product)
                    Toast.makeText(context, "Prodotto aggiunto al carrello", Toast.LENGTH_SHORT).show()
                }
            )

            Log.d("ProductCatalogFragment", "Assegnando adapter al RecyclerView")
            binding.recyclerViewProducts.adapter = productAdapter

            // Subito dopo assegna la lista
            if (products.isNotEmpty()) {
                Log.d("ProductCatalogFragment", "Assegnando ${products.size} prodotti all'adapter")
                productAdapter.submitList(products)
            }
        } else {
            Log.w("ProductCatalogFragment", "userRole è null, non posso configurare l'adapter")
        }
    }

    private fun observeScreenState() {
        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            updateUI(state)
        }
    }

    private fun updateUI(state: ProductCatalogScreenState) {
        if (state.errorMessage != null) {
            Toast.makeText(context, state.errorMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }

        // Forza l'aggiornamento del binding
        binding.invalidateAll()

        // Gestisci la visibilità del progress bar manualmente se il data binding non funziona
        binding.progressBarCatalog.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        setupAdapter(state.currentUserRole)
        updateProductList(state.products)

        // Gestisci la visibilità del messaggio "nessun prodotto"
        binding.textViewNoProductsCatalog.visibility =
            if (!state.isLoading && state.products.isEmpty()) View.VISIBLE else View.GONE

        if (state.currentUserRole == UserRole.ADMIN) {
            binding.fabAddProduct.visibility = View.VISIBLE
            binding.fabAddProduct.setOnClickListener {
                try {
                    findNavController().navigate(
                        ProductCatalogFragmentDirections.actionProductCatalogFragmentToAddEditProductFragment()
                    )
                } catch (_: Exception) {
                    Toast.makeText(context, "Errore nella navigazione", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            binding.fabAddProduct.visibility = View.GONE
        }
    }

    private fun setupAdapter(userRole: UserRole?) {
        if (userRole != null && (!::productAdapter.isInitialized || productAdapter.userRole != userRole)) {
            productAdapter = ProductCatalogAdapter(
                userRole = userRole,
                onProductClick = { product -> handleProductClick(product) },
                onAddToCartClick = { product ->
                    viewModel.addProductToCart(product)
                    Toast.makeText(context, "Prodotto aggiunto al carrello", Toast.LENGTH_SHORT).show()
                }
            )
            binding.recyclerViewProducts.adapter = productAdapter
        }
    }

    private fun updateProductList(products: List<Product>) {
        if (::productAdapter.isInitialized) {
            productAdapter.submitList(products)
        }
    }

    private fun handleProductClick(product: Product) {
        view?.doOnPreDraw {
            try {
                // Per gli admin, naviga alla schermata di modifica
                // Per gli utenti normali, naviga alla schermata di dettaglio
                val currentUserRole = viewModel.screenState.value?.currentUserRole

                if (currentUserRole == UserRole.ADMIN) {
                    findNavController().navigate(
                        ProductCatalogFragmentDirections.actionProductCatalogFragmentToAddEditProductFragment(
                            product.id
                        )
                    )
                } else {
                    findNavController().navigate(
                        ProductCatalogFragmentDirections.actionProductCatalogFragmentToDettaglioProdottoFragment(
                            product
                        )
                    )
                }
            } catch (_: Exception) {
                Toast.makeText(context, "Errore nell'apertura del prodotto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
