package com.example.circolapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.circolapp.R
import com.example.circolapp.model.Feedback
import java.text.SimpleDateFormat
import java.util.*

class FeedbackListAdapter(
    private val feedbackList: List<Feedback>,
    private val onFeedbackClick: (Feedback) -> Unit
) : RecyclerView.Adapter<FeedbackListAdapter.FeedbackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        holder.bind(feedbackList[position])
    }

    override fun getItemCount(): Int = feedbackList.size

    inner class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textCategoria = itemView.findViewById<TextView>(R.id.textViewCategoria)
        private val textTitolo = itemView.findViewById<TextView>(R.id.textViewTitolo)
        private val textUtente = itemView.findViewById<TextView>(R.id.textViewUtente)
        private val textData = itemView.findViewById<TextView>(R.id.textViewData)
        private val textMessaggio = itemView.findViewById<TextView>(R.id.textViewMessaggio)
        private val indicatoreLetto = itemView.findViewById<View>(R.id.indicatorLetto)

        fun bind(feedback: Feedback) {
            textCategoria.text = feedback.categoria
            textTitolo.text = feedback.titolo
            textUtente.text = "Da: ${feedback.nomeUtente}"
            textMessaggio.text = feedback.messaggio

            // Formatta la data
            feedback.timestamp?.let { timestamp ->
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                textData.text = formatter.format(timestamp.toDate())
            } ?: run {
                textData.text = "Data non disponibile"
            }

            // Mostra/nascondi indicatore di lettura
            indicatoreLetto.visibility = if (feedback.letto) View.GONE else View.VISIBLE

            // Cambia lo sfondo se non letto
            itemView.alpha = if (feedback.letto) 0.7f else 1.0f

            itemView.setOnClickListener {
                onFeedbackClick(feedback)
            }
        }
    }
}
