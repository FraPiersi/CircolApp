package com.example.circolapp.admin

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import com.example.circolapp.R
import com.example.circolapp.model.Ordine
import com.google.firebase.firestore.FieldValue
import java.text.SimpleDateFormat
import java.util.Locale

class ListaOrdinazioniFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrdinazioniAdapter
    private val ordinazioni = mutableListOf<Ordine>()
    private val mappaUidOriginali = mutableMapOf<String, String>() // Map: ID documento -> UID originale

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ordinazioni, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewOrdinazioni)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = OrdinazioniAdapter(ordinazioni) { ordine ->
            mostraDettaglioOrdinazione(ordine)
        }
        recyclerView.adapter = adapter
        caricaOrdinazioniDaFirestore()
        return view
    }

    private fun caricaOrdinazioniDaFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("ordinazioni")
            .get()
            .addOnSuccessListener { result ->
                ordinazioni.clear()
                mappaUidOriginali.clear() // Pulisci la mappa
                val utentiMap = mutableMapOf<String, String>()
                val ordiniTmp = mutableListOf<Ordine>()
                for (document in result) {
                    val ordine = document.toObject(Ordine::class.java)
                    val ordineConId = ordine.copy(prodottoId = document.id)
                    ordiniTmp.add(ordineConId)
                    // Salva l'UID originale nella mappa
                    mappaUidOriginali[document.id] = ordine.uidUtente
                }
                // Recupera i nomi utenti in batch
                val uids = ordiniTmp.map { it.uidUtente }.distinct().filter { it.isNotBlank() }
                if (uids.isEmpty()) {
                    ordinazioni.addAll(ordiniTmp)
                    adapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }
                db.collection("utenti").whereIn("uid", uids).get().addOnSuccessListener { utentiSnap ->
                    for (utenteDoc in utentiSnap) {
                        val uid = utenteDoc.getString("uid") ?: ""
                        val nome = utenteDoc.getString("displayName") ?: uid
                        utentiMap[uid] = nome
                    }
                    for (ordine in ordiniTmp) {
                        val nomeUtente = utentiMap[ordine.uidUtente]
                        if (nomeUtente != null) {
                            ordinazioni.add(ordine.copy(uidUtente = nomeUtente))
                        } else {
                            ordinazioni.add(ordine)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }.addOnFailureListener {
                    ordinazioni.addAll(ordiniTmp)
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Errore nel recupero delle ordinazioni", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostraDettaglioOrdinazione(ordine: Ordine) {
        val dataString = ordine.timestamp?.let {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
        } ?: "-"

        val msg = """
            Utente: ${ordine.uidUtente}
            Prodotto: ${ordine.nomeProdotto}
            ID Prodotto: ${ordine.prodottoId}
            Richieste aggiuntive: ${ordine.richiesteAggiuntive ?: "Nessuna"}
            Stato: ${ordine.stato}
            Data: $dataString
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Dettaglio Ordinazione")
            .setMessage(msg)
            .setPositiveButton("Completa") { _, _ ->
                completaOrdinazione(ordine)
            }
            .setNegativeButton("Chiudi") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun completaOrdinazione(ordine: Ordine) {
        val db = FirebaseFirestore.getInstance()
        // Usa direttamente l'ID del documento che ora è salvato in prodottoId
        if (ordine.prodottoId.isNotBlank()) {
            db.collection("ordinazioni").document(ordine.prodottoId)
                .delete() // Elimina completamente il documento invece di aggiornare lo stato
                .addOnSuccessListener {
                    Toast.makeText(context, "Ordinazione completata ed eliminata!", Toast.LENGTH_SHORT).show()
                    // Usa l'UID originale dalla mappa
                    val uidOriginale = mappaUidOriginali[ordine.prodottoId] ?: ordine.uidUtente
                    val ordineConUidOriginale = ordine.copy(uidUtente = uidOriginale)
                    inviaNotificaUtente(ordineConUidOriginale)
                    caricaOrdinazioniDaFirestore() // Ricarica la lista
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Errore nell'eliminazione: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("ListaOrdinazioniFragment", "Errore eliminazione ordine: ${e.message}")
                }
        } else {
            Toast.makeText(context, "ID ordinazione non valido", Toast.LENGTH_SHORT).show()
            Log.e("ListaOrdinazioniFragment", "prodottoId vuoto per ordine: $ordine")
        }
    }

    private fun inviaNotificaUtente(ordine: Ordine) {
        // Utilizza solo notifiche in-app (senza Firebase Functions)
        salvaNotificaInApp(ordine)
        Log.d("ListaOrdinazioniFragment", "Notifica in-app salvata per utente: ${ordine.uidUtente}")
    }

    private fun salvaNotificaInApp(ordine: Ordine) {
        val notifica = mapOf(
            "uidUtente" to ordine.uidUtente,
            "titolo" to "Ordinazione Completata",
            "messaggio" to "La tua ordinazione di ${ordine.nomeProdotto} è stata completata!",
            "tipo" to "ordinazione_completata",
            "letta" to false,
            "timestamp" to FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance().collection("notifiche")
            .add(notifica)
            .addOnSuccessListener {
                Log.d("ListaOrdinazioniFragment", "Notifica in-app salvata")
                Toast.makeText(context, "Notifica inviata all'utente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("ListaOrdinazioniFragment", "Errore nel salvare notifica in-app: ${e.message}")
            }
    }
}

// Adattatore e modello base
class OrdinazioniAdapter(private val ordinazioni: List<Ordine>, private val onItemClick: (Ordine) -> Unit) : RecyclerView.Adapter<OrdinazioniViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdinazioniViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ordinazione, parent, false)
        return OrdinazioniViewHolder(view, onItemClick)
    }
    override fun onBindViewHolder(holder: OrdinazioniViewHolder, position: Int) {
        holder.bind(ordinazioni[position])
    }
    override fun getItemCount() = ordinazioni.size
}

class OrdinazioniViewHolder(itemView: View, private val onItemClick: (Ordine) -> Unit) : RecyclerView.ViewHolder(itemView) {
    fun bind(ordine: Ordine) {
        itemView.findViewById<TextView>(R.id.textViewUtente).text = "Utente: ${ordine.uidUtente}"
        itemView.findViewById<TextView>(R.id.textViewProdotti).text = "Prodotto: ${ordine.nomeProdotto}"
        itemView.findViewById<TextView>(R.id.textViewTotale).text = "Stato: ${ordine.stato}"
        val data = ordine.timestamp
        val dataString = if (data != null) SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(data) else "-"
        itemView.findViewById<TextView>(R.id.textViewData).text = "Data: $dataString"
        itemView.setOnClickListener { onItemClick(ordine) }
    }
}
