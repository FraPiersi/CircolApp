// EventoAdapter.kt
package com.example.circolapp// o la tua package structure

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.circolapp.databinding.ItemEventoBinding
import com.example.circolapp.model.Evento // Assicurati che il path sia corretto

class ListaEventi(private val onItemClicked: (Evento) -> Unit) :
    ListAdapter<Evento, ListaEventi.EventoViewHolder>(EventoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val binding = ItemEventoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = getItem(position)
        holder.bind(evento)
        holder.itemView.setOnClickListener {
            onItemClicked(evento)
        }
    }

    class EventoViewHolder(private val binding: ItemEventoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(evento: Evento) {
            binding.evento = evento
            binding.executePendingBindings() // Importante per Data Binding in RecyclerView
        }
    }
}

class EventoDiffCallback : DiffUtil.ItemCallback<Evento>() {
    override fun areItemsTheSame(oldItem: Evento, newItem: Evento): Boolean {
        return oldItem.id == newItem.id // Assumendo che Evento abbia un 'id' univoco
    }

    override fun areContentsTheSame(oldItem: Evento, newItem: Evento): Boolean {
        return oldItem == newItem
    }
}