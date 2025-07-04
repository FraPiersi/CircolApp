package com.example.circolapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Per caricare immagini da URL
import com.example.circolapp.R
import com.example.circolapp.databinding.ItemProductBinding // Verrà creato
import com.example.circolapp.model.Product
import java.text.NumberFormat
import java.util.Locale

class ProductListAdapter(private val onProductClick: (Product) -> Unit) :
    ListAdapter<Product, ProductListAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
        holder.itemView.setOnClickListener {
            onProductClick(product)
        }
    }

    class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)

        fun bind(product: Product) {
            binding.textViewProductName.text = product.nome
            binding.textViewProductDescription.text = product.descrizione
            binding.textViewProductPrice.text = currencyFormatter.format(product.importo)

            if (!product.imageUrl.isNullOrEmpty()) {
                Glide.with(binding.imageViewProduct.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.prodotto) // Crea un placeholder
                    .error(R.drawable.errore) // Crea un'immagine di errore
                    .into(binding.imageViewProduct)
            } else {
                binding.imageViewProduct.setImageResource(R.drawable.prodotto)
            }
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