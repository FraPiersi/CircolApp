package com.example.circolapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.circolapp.R
import com.example.circolapp.databinding.ItemMovimentoBinding
import com.example.circolapp.model.Movimento // Assicurati che il percorso sia corretto
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class MovimentiAdapter : ListAdapter<Movimento, MovimentiAdapter.MovimentoViewHolder>(
    // Definisci DiffUtil.ItemCallback come oggetto anonimo qui
    object : DiffUtil.ItemCallback<Movimento>() {
        override fun areItemsTheSame(oldItem: Movimento, newItem: Movimento): Boolean {
            // Se Movimento ha un ID univoco, confronta quello.
            // Altrimenti, se i campi chiave sono sufficienti per l'unicità e non cambiano
            // frequentemente per lo STESSO item logico, puoi usarli.
            // Dato che Movimento potrebbe non avere un ID stabile, e la descrizione+data+importo
            // potrebbero ripetersi per item DIVERSI, questo è un compromesso.
            // L'ideale per areItemsTheSame è un ID stabile.
            // Se non hai un ID, e due movimenti con gli stessi dati sono considerati
            // lo "stesso" item per scopi di aggiornamento della UI, allora questo va bene.
            // Se hai molti movimenti con dati identici ma sono logicamente distinti,
            // dovresti davvero considerare di aggiungere un ID a Movimento.
            return oldItem.descrizione == newItem.descrizione &&
                    oldItem.importo == newItem.importo &&
                    oldItem.data == newItem.data
            // Per esempio, se Movimento avesse un campo `id: String`:
            // return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movimento, newItem: Movimento): Boolean {
            // Dato che Movimento è (si spera) una data class,
            // l'implementazione di equals() confronta tutti i campi.
            return oldItem == newItem
        }
    }
) {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)
    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.ITALIAN)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovimentoViewHolder {
        val binding = ItemMovimentoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovimentoViewHolder(binding, currencyFormatter, dateFormatter)
    }

    override fun onBindViewHolder(holder: MovimentoViewHolder, position: Int) {
        val movimento = getItem(position)
        holder.bind(movimento)
    }

    class MovimentoViewHolder(
        private val binding: ItemMovimentoBinding,
        private val currencyFormatter: NumberFormat,
        private val dateFormatter: SimpleDateFormat
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movimento: Movimento) {
            binding.textViewMovimentoDescrizione.text = movimento.descrizione
            binding.textViewMovimentoData.text = dateFormatter.format(movimento.data)

            val formattedImporto = currencyFormatter.format(movimento.importo)
            binding.textViewMovimentoImporto.text = formattedImporto

            if (movimento.importo >= 0) {
                binding.textViewMovimentoImporto.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.positive_amount_color)
                )
            } else {
                binding.textViewMovimentoImporto.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.negative_amount_color)
                )
            }
        }
    }
}