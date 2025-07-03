package com.example.circolapp

// com/example/circolapp/MovimentiAdapter.kt
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.circolapp.databinding.ItemMovimentoBinding
import com.example.circolapp.model.Movimento

class MovimentiAdapter(var movimenti: List<Movimento>) : RecyclerView.Adapter<MovimentiAdapter.MovimentoViewHolder>() {
    class MovimentoViewHolder(val binding: ItemMovimentoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovimentoViewHolder {
        val binding = ItemMovimentoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovimentoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovimentoViewHolder, position: Int) {
        val movimento = movimenti[position]
        holder.binding.cifraText.text = movimento.importo.toString()
        holder.binding.descrizioneText.text = movimento.descrizione
        holder.binding.dataText.text = movimento.data.toString()
    }

    override fun getItemCount() = movimenti.size
}