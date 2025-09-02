package com.example.circolapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.circolapp.R
import com.example.circolapp.databinding.ItemProductBindingimport com.example.circolapp.model.Product
import com.example.circolapp.model.UserRoleimport java.text.NumberFormat
import java.util.Locale

class ProductCatalogAdapter(
    internal    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
    ) : RecyclerView.ViewHolder(binding.root) {

        private            if (userRole == UserRole.ADMIN) {
                binding.textViewProductQuantityAdmin.visibility = View.VISIBLE
                binding.textViewProductQuantityAdmin.text = "Disponibili: ${product.numeroPezzi}"
            } else {
                binding.textViewProductQuantityAdmin.visibility = View.GONE

            }

            itemView.setOnClickListener {
                onProductClick(product)            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            Log.d("ProductDiffCallback", "ARE_ITEMS_THE_SAME CALLED: oldId=${oldItem.id}, newId=${newItem.id}")
            // FORZA IL DIFF PER VEDERE SE VIENE CHIAMATO areContentsTheSame
            return false // Considera sempre gli item diversi per forzare la chiamata a areContentsTheSame
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            Log.d("ProductDiffCallback", "ARE_CONTENTS_THE_SAME CALLED: oldName=${oldItem.nome}, newName=${newItem.nome}")
            // FORZA IL DIFF
            return false // Considera sempre i contenuti diversi
        }
    }
}
