package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Locale

class NotificheFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificheAdapter
    private                notifiche.addAll(notificheList.sortedByDescending { it.timestamp?.toDate() })
                adapter.notifyDataSetChanged()

                android.util.Log.d("NotificheFragment", "Caricate ${notifiche.size} notifiche per utente ${currentUser.uid}")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("NotificheFragment", "Errore nel recupero delle notifiche: ${e.message}")
                Toast.makeText(context, "Errore nel recupero delle notifiche: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostraNotificaDialog(notifica: Notifica) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(notifica.titolo)
            .setMessage(notifica.messaggio)
            .setPositiveButton("OK") { dialog, _ ->
                // Segna la notifica come letta
                if (!notifica.letta) {
                    segnaComeLetta(notifica)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun segnaComeLetta(notifica: Notifica) {
        if (notifica.id.isNotBlank()) {
            FirebaseFirestore.getInstance().collection("notifiche")
                .document(notifica.id)
                .update("letta", true)
                .addOnSuccessListener {
                    val index = notifiche.indexOfFirst { it.id == notifica.id }
                    if (index != -1) {
                        notifiche[index] = notifica.copy(letta = true)
                        adapter.notifyItemChanged(index)
                    }
                }
        }
    }
}

data class Notifica(
    val id: String = "",
    val uidUtente: String = "",
    val titolo: String = "",
    val messaggio: String = "",
    val tipo: String = "",
    val letta: Boolean = false,
    val timestamp: com.google.firebase.Timestamp? = null
)

class NotificheAdapter(
    private val notifiche: List<Notifica>,
    private val onItemClick: (Notifica) -> Unit
) : RecyclerView.Adapter<NotificheViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificheViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notifica, parent, false)
        return NotificheViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: NotificheViewHolder, position: Int) {
        holder.bind(notifiche[position])
    }

    override fun getItemCount() = notifiche.size
}

class NotificheViewHolder(
    itemView: View,
    private val onItemClick: (Notifica) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    fun bind(notifica: Notifica) {
        itemView.findViewById<android.widget.TextView>(R.id.textViewTitolo).text = notifica.titolo
        itemView.findViewById<android.widget.TextView>(R.id.textViewMessaggio).text = notifica.messaggio

        val dataString = notifica.timestamp?.toDate()?.let {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
        } ?: "-"
        itemView.findViewById<android.widget.TextView>(R.id.textViewData).text = dataString

        // Cambia il background se non letta
        if (!notifica.letta) {
            itemView.setBackgroundColor(android.graphics.Color.parseColor("#E3F2FD"))
        } else {
            itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        itemView.setOnClickListener { onItemClick(notifica) }
    }
}
