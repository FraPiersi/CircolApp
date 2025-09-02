package com.example.circolapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.circolapp.R
import com.example.circolapp.model.User
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class GestisciTessereAdapter(
    private val utenti: List<User>,
    private val onActionClick: (User, String) -> Unit
) : RecyclerView.Adapter<GestisciTessereAdapter.ViewHolder>() {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textNomeUtente: TextView = view.findViewById(R.id.text_nome_utente)
        val textEmailUtente: TextView = view.findViewById(R.id.text_email_utente)
        val textStatoTessera: TextView = view.findViewById(R.id.text_tipo_richiesta)
        val textDettagli: TextView = view.findViewById(R.id.text_data_richiesta)
        val buttonGestisci: Button = view.findViewById(R.id.button_approva)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_richiesta_tessera, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val utente = utenti[position]

        holder.textNomeUtente.text = utente.nome.ifEmpty { "Nome non disponibile" }
        holder.textEmailUtente.text = "UID: ${utente.uid}"

        // Determina lo stato della tessera
        val statoTessera = when {
            utente.richiestaRinnovoInCorso -> "🟡 Richiesta in attesa"
            utente.hasTessera -> {
                if (utente.dataScadenzaTessera != null && utente.dataScadenzaTessera.before(Date())) {
                    "🔴 Tessera scaduta"
                } else {
                    "🟢 Tessera attiva"
                }
            }
            else -> "⚪ Nessuna tessera"
        }
        holder.textStatoTessera.text = statoTessera

        // Mostra dettagli aggiuntivi
        val dettagli = buildString {
            append("Saldo: ${currencyFormatter.format(utente.saldo)}")
            if (utente.hasTessera && utente.numeroTessera != null) {
                append("\nTessera: ${utente.numeroTessera}")
                if (utente.dataScadenzaTessera != null) {
                    append("\nScadenza: ${dateFormatter.format(utente.dataScadenzaTessera)}")
                }
            }
        }
        holder.textDettagli.text = dettagli

        // Configura il pulsante
        holder.buttonGestisci.text = "Gestisci"
        holder.buttonGestisci.setOnClickListener {
            onActionClick(utente, "gestisci")
        }

        // Nascondi il secondo pulsante (rifiuta) dato che non serve più
        holder.itemView.findViewById<Button>(R.id.button_rifiuta)?.visibility = View.GONE
    }

    override fun getItemCount() = utenti.size
}
