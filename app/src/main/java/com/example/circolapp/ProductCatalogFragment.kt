package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.circolapp.adapter.ProductCatalogAdapter
import com.example.circolapp.databinding.FragmentProductCatalogBinding
import com.example.circolapp.model.Product
import com.example.circolapp.model.UserRole
import com.example.circolapp.viewmodel.ProductCatalogViewModel
import com.example.circolapp.viewmodel.ProductCatalogScreenState
import com.example.circolapp.ProductCatalogFragmentDirections
import java.util.UUID

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
        setupCategorySpinner()
        observeScreenState()
    }

    private fun setupCategorySpinner() {
        binding.spinnerCategoryFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = parent?.getItemAtPosition(position) as? String
                selectedCategory?.let { viewModel.selectCategory(it) }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeScreenState() {
        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            val currentBinding = _binding ?: return@observe
            currentBinding.progressBarCatalog.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            state.currentUserRole?.let { role ->
                if (!::productAdapter.isInitialized || productAdapter.userRole != role || currentBinding.recyclerViewProducts.adapter != productAdapter) {
                    productAdapter = ProductCatalogAdapter(
                        userRole = role,
                        onProductClick = { product -> handleProductClick(product, role) },
                        onAddToCartClick = { product ->
                            viewModel.addProductToCart(product)
                            Toast.makeText(context, "${product.nome} aggiunto al carrello", Toast.LENGTH_SHORT).show()
                        }
                    )
                    currentBinding.recyclerViewProducts.adapter = productAdapter
                }
                if (role == UserRole.ADMIN) {
                    currentBinding.fabAddProductCatalog.visibility = View.VISIBLE
                    currentBinding.fabAddProductCatalog.setOnClickListener {
                        if (!isAdded) return@setOnClickListener
                        val action = ProductCatalogFragmentDirections.actionProductCatalogFragmentToAddEditProductFragment(null)
                        findNavController().navigate(action)
                    }
                } else {
                    currentBinding.fabAddProductCatalog.visibility = View.GONE
                }
                if (currentBinding.recyclerViewProducts.adapter == productAdapter) {
                    val listToSubmit = state.products.toList()
                    productAdapter.submitList(listToSubmit) {
                        updateRecyclerViewVisibility(state, currentBinding)
                        updateNoProductsMessageVisibility(state, currentBinding)
                    }
                } else {
                    currentBinding.recyclerViewProducts.visibility = View.GONE
                    currentBinding.textViewNoProductsCatalog.visibility = View.VISIBLE
                    currentBinding.textViewNoProductsCatalog.text = "Errore durante l'aggiornamento dei prodotti."
                }
            } ?: run {
                currentBinding.fabAddProductCatalog.visibility = View.GONE
                if (::productAdapter.isInitialized) {
                    productAdapter.submitList(emptyList()) {
                        updateRecyclerViewVisibility(state, currentBinding)
                        updateNoProductsMessageVisibility(state, currentBinding)
                    }
                } else {
                    updateRecyclerViewVisibility(state, currentBinding)
                    updateNoProductsMessageVisibility(state, currentBinding)
                }
            }
            if (state.categories.isNotEmpty() && isAdded) {
                val currentSpinnerAdapter = currentBinding.spinnerCategoryFilter.adapter as? ArrayAdapter<String>
                if (currentSpinnerAdapter == null || currentSpinnerAdapter.count != state.categories.size + 1 ||
                    !currentSpinnerAdapter.areListsSame(state.categories)) {
                    val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, state.categories)
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    currentBinding.spinnerCategoryFilter.adapter = spinnerAdapter
                }
                currentBinding.spinnerCategoryFilter.visibility = View.VISIBLE
                state.selectedCategory?.let { currentCategoryInState ->
                    val position = state.categories.indexOf(currentCategoryInState)
                    if (position >= 0 && currentBinding.spinnerCategoryFilter.selectedItemPosition != position) {
                        currentBinding.spinnerCategoryFilter.setSelection(position, false)
                    }
                }
            } else if (isAdded) {
                currentBinding.spinnerCategoryFilter.visibility = View.GONE
            }
        }
    }

    private fun ArrayAdapter<String>?.areListsSame(newCategories: List<String>): Boolean {
        if (this == null) return false
        if (this.count != newCategories.size) return false
        for (i in 0 until this.count) {
            if (this.getItem(i) != newCategories[i]) return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.recyclerViewProducts?.adapter = null
        _binding = null
    }

    private fun handleProductClick(product: Product, role: UserRole) {
        if (role == UserRole.ADMIN) {
            product.id?.let { productId ->
                if (!isAdded || view == null) return
                val action = ProductCatalogFragmentDirections.actionProductCatalogFragmentToAddEditProductFragment(productId)
                requireView().doOnPreDraw {
                    val activityProvidedNavController = getNavControllerFromActivity()
                    if (activityProvidedNavController == null) {
                        Toast.makeText(context, "Errore NavController (Activity Provider).", Toast.LENGTH_SHORT).show()
                        return@doOnPreDraw
                    }
                    if (activityProvidedNavController.currentDestination?.id == R.id.ProductCatalogFragment) {
                        activityProvidedNavController.navigate(action)
                    } else if (activityProvidedNavController.currentDestination == null) {
                        requireView().postDelayed({
                            val navControllerRetry = getNavControllerFromActivity()
                            if (navControllerRetry?.currentDestination?.id == R.id.ProductCatalogFragment) {
                                navControllerRetry.navigate(action)
                            } else {
                                Toast.makeText(context, "Navigazione non pronta.", Toast.LENGTH_SHORT).show()
                            }
                        }, 100)
                    } else {
                        Toast.makeText(context, "Navigazione (activity) non pronta.", Toast.LENGTH_SHORT).show()
                    }
                }
            } ?: Toast.makeText(context, "ID prodotto mancante per la modifica.", Toast.LENGTH_SHORT).show()
        } else {
            // Utente normale: naviga ai dettagli del prodotto per ordinare
            if (!isAdded || view == null) return
            val action = ProductCatalogFragmentDirections.actionProductCatalogFragmentToDettaglioProdottoFragment(product)
            requireView().doOnPreDraw {
                val activityProvidedNavController = getNavControllerFromActivity()
                if (activityProvidedNavController == null) {
                    Toast.makeText(context, "Errore NavController.", Toast.LENGTH_SHORT).show()
                    return@doOnPreDraw
                }
                if (activityProvidedNavController.currentDestination?.id == R.id.ProductCatalogFragment) {
                    activityProvidedNavController.navigate(action)
                } else if (activityProvidedNavController.currentDestination == null) {
                    requireView().postDelayed({
                        val navControllerRetry = getNavControllerFromActivity()
                        if (navControllerRetry?.currentDestination?.id == R.id.ProductCatalogFragment) {
                            navControllerRetry.navigate(action)
                        } else {
                            Toast.makeText(context, "Navigazione non pronta.", Toast.LENGTH_SHORT).show()
                        }
                    }, 100)
                } else {
                    Toast.makeText(context, "Navigazione non pronta.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getNavControllerFromActivity(): NavController? {
        val currentActivity = activity
        return if (currentActivity is NavControllerProvider) {
            currentActivity.getAppNavController()
        } else {
            null
        }
    }

    override fun onResume() {
        super.onResume()
        val activityNavController = (requireActivity() as? NavControllerProvider)?.getAppNavController()
        if (activityNavController?.currentDestination == null) {
            activityNavController?.navigate(R.id.ProductCatalogFragment)
        }
    }

    private fun updateRecyclerViewVisibility(state: ProductCatalogScreenState, binding: FragmentProductCatalogBinding) {
        if (!state.isLoading && state.currentUserRole != null && state.errorMessage == null) {
            if (::productAdapter.isInitialized && productAdapter.itemCount > 0) {
                binding.recyclerViewProducts.visibility = View.VISIBLE
            } else {
                binding.recyclerViewProducts.visibility = View.GONE
            }
        } else {
            binding.recyclerViewProducts.visibility = View.GONE
        }
    }

    private fun updateNoProductsMessageVisibility(state: ProductCatalogScreenState, binding: FragmentProductCatalogBinding) {
        if (!state.isLoading && state.errorMessage != null) {
            binding.textViewNoProductsCatalog.visibility = View.VISIBLE
            binding.textViewNoProductsCatalog.text = state.errorMessage
            binding.recyclerViewProducts.visibility = View.GONE
        } else if (!state.isLoading && state.currentUserRole != null &&
            ::productAdapter.isInitialized && productAdapter.itemCount == 0) {
            binding.textViewNoProductsCatalog.visibility = View.VISIBLE
            binding.textViewNoProductsCatalog.text = "No prodotti"
        } else {
            binding.textViewNoProductsCatalog.visibility = View.GONE
        }
    }
}
