package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.circolapp.adapter.GestisciTessereAdapter
import com.example.circolapp.model.User
import com.google.firebase.firestore.FirebaseFirestore

class GestisciTessereFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GestisciTessereAdapter
    private val utenti = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gestisci_tessere, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewRichieste)
        setupRecyclerView()
        caricaUtenti()

        return view
    }

    private fun setupRecyclerView() {
        adapter = GestisciTessereAdapter(utenti) { utente, azione ->
            when (azione) {
                "gestisci" -> mostraDettagliUtente(utente)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun caricaUtenti() {
        FirebaseFirestore.getInstance()
            .collection("utenti")
            .get()
            .addOnSuccessListener { documents ->
                utenti.clear()
                for (document in documents) {
                    try {
                        val utente = User(
                            uid = document.id,
                            username = document.getString("username") ?: "",
                            nome = document.getString("displayName") ?: document.getString("nome") ?: "",
                            saldo = document.getDouble("saldo") ?: 0.0,
                            hasTessera = document.getBoolean("hasTessera") ?: false,
                            numeroTessera = document.getString("numeroTessera"),
                            dataScadenzaTessera = document.getDate("dataScadenzaTessera"),
                            richiestaRinnovoInCorso = document.getBoolean("richiestaRinnovoInCorso") ?: false
                        )
                        utenti.add(utente)
                    } catch (e: Exception) {
                        // Ignora utenti con dati malformati
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Errore nel caricamento: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostraDettagliUtente(utente: User) {
        // Mostra dialog con dettagli e azioni per l'utente
        val dialog = GestisciTesseraDialog(utente) {
            // Callback per aggiornare la lista quando viene fatta un'azione
            caricaUtenti()
        }
        dialog.show(parentFragmentManager, "GestisciTesseraDialog")
    }
}
