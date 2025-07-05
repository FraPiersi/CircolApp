package com.example.circolapp.adapters // o il tuo package adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.circolapp.R
import com.example.circolapp.databinding.ItemProductBinding // Usa il tuo binding corretto
import com.example.circolapp.model.Product
import com.example.circolapp.model.UserRole // Importa UserRole
import java.text.NumberFormat
import java.util.Locale

class ProductCatalogAdapter(
    internal val userRole: UserRole, // Passa il ruolo dell'utente
    private val onProductClick: (Product) -> Unit,
    private val onAddToCartClick: (Product) -> Unit
) : ListAdapter<Product, ProductCatalogAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding, userRole) // Passa il ruolo al ViewHolder
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product, onProductClick, onAddToCartClick)
    }

    class ProductViewHolder(
        private val binding: ItemProductBinding,
        private val userRole: UserRole // Ricevi il ruolo
    ) : RecyclerView.ViewHolder(binding.root) {

        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)

        fun bind(
            product: Product,
            onProductClick: (Product) -> Unit,
            onAddToCartClick: (Product) -> Unit
        ) {
            binding.textViewProductName.text = product.nome
            binding.textViewProductPrice.text = currencyFormatter.format(product.importo)

            if (!product.imageUrl.isNullOrEmpty()) {
                Glide.with(binding.imageViewProduct.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.prodotto)
                    .error(R.drawable.errore)
                    .into(binding.imageViewProduct)
            } else {
                binding.imageViewProduct.setImageResource(R.drawable.prodotto)
            }

            // Gestione visibilità basata sul ruolo
            if (userRole == UserRole.ADMIN) {
                binding.textViewProductQuantityAdmin.visibility = View.VISIBLE
                binding.textViewProductQuantityAdmin.text = "Disponibili: ${product.numeroPezzi}"
            } else { // USER o UNKNOWN
                binding.textViewProductQuantityAdmin.visibility = View.GONE

            }


            itemView.setOnClickListener {
                onProductClick(product) // Per l'admin potrebbe navigare a modifica, per utente a dettaglio
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
