// InfoEventoFragment.kt
package com.example.circolapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Importa viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.circolapp.databinding.FragmentInfoEventoBinding
import com.example.circolapp.model.Evento // Assicurati che il percorso sia corretto
import com.example.circolapp.model.UserRole
import com.example.circolapp.viewmodel.InfoEventoViewModel // Importa il ViewModel
import java.text.SimpleDateFormat
import java.util.*

class InfoEventoFragment : Fragment() {

    private var _binding: FragmentInfoEventoBinding? = null
    private val binding get() = _binding!!

    // Recupera gli argomenti di navigazione
    private val navArguments: InfoEventoFragmentArgs by navArgs()
    private lateinit var userRole: UserRole

    // Inizializza il ViewModel usando la KTX library
    private val viewModel: InfoEventoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_info_evento, container, false)
        binding.lifecycleOwner = viewLifecycleOwner // Imposta il lifecycle owner per osservare LiveData nel layout
        binding.viewModel = viewModel // Collega il viewModel al binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userRole = navArguments.userRole
        // Carica l'evento nel ViewModel
        try {
            val eventoArg: Evento? = navArguments.evento
            viewModel.caricaEvento(eventoArg)
        } catch (e: Exception) {
            Log.e("InfoEventoFragment", "Errore nel recuperare l'argomento evento dagli args: ${e.message}", e)
            Toast.makeText(context, "Errore fatale nel caricare l'evento.", Toast.LENGTH_LONG).show()
            if (isAdded) findNavController().popBackStack()
            return
        }

        setupObservers()
        // Mostra/nascondi partecipanti e bottone in base al ruolo
        val btnPartecipa = view.findViewById<View>(R.id.btnPartecipa)
        val partecipantiLayout = view.findViewById<LinearLayout?>(R.id.layoutPartecipanti)
        if (userRole == UserRole.ADMIN) {
            btnPartecipa?.visibility = View.GONE
            partecipantiLayout?.visibility = View.VISIBLE
        } else {
            partecipantiLayout?.visibility = View.GONE
        }
    }

    private fun setupObservers() {
        // Osserva l'evento nel ViewModel
        viewModel.evento.observe(viewLifecycleOwner) { evento ->
            if (evento == null && viewModel.messaggioToast.value != null) {
                if(isAdded) findNavController().popBackStack()
            }

            // Aggiorna la visualizzazione della data formattata
            aggiornaDataEvento(evento)

            // Controlla se l'utente ha già partecipato e aggiorna la UI
            controllaPartecipazioneUtente(evento)
        }

        // Osserva i partecipanti dalla sottocollezione (per admin)
        viewModel.partecipanti.observe(viewLifecycleOwner) { partecipanti ->
            if (userRole == UserRole.ADMIN) {
                val listaPartecipanti = view?.findViewById<TextView>(R.id.listaPartecipanti)
                val partecipantiText = if (partecipanti.isNotEmpty()) {
                    partecipanti.joinToString(separator = "\n")
                } else {
                    "Nessun partecipante"
                }
                listaPartecipanti?.text = partecipantiText
                android.util.Log.d("InfoEventoFragment", "Partecipanti aggiornati: $partecipanti")
            }
        }

        // Osserva i messaggi Toast dal ViewModel
        viewModel.messaggioToast.observe(viewLifecycleOwner) { messaggio ->
            messaggio?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.onMessaggioToastMostrato() // Resetta il messaggio dopo averlo mostrato
            }
        }

        // Osserva l'esito dell'azione di partecipazione
        viewModel.azionePartecipazioneCompletata.observe(viewLifecycleOwner) { completata ->
            if (completata) {
                Log.d("InfoEventoFragment", "Azione partecipazione gestita, eventuale navigazione o UI update qui.")
                // Dopo aver completato la partecipazione, ricontrolla lo stato
                controllaPartecipazioneUtente(viewModel.evento.value)
                viewModel.onAzionePartecipazioneGestita() // Resetta lo stato
            }
        }
    }

    private fun controllaPartecipazioneUtente(evento: Evento?) {
        if (evento == null || userRole == UserRole.ADMIN) return

        val btnPartecipa = view?.findViewById<View>(R.id.btnPartecipa)
        val layoutGiaPartecipante = view?.findViewById<View>(R.id.layoutGiaPartecipante)
        val btnAnnullaPartecipazione = view?.findViewById<View>(R.id.btnAnnullaPartecipazione)

        // Utilizza il nuovo metodo del ViewModel per verificare la partecipazione tramite sottocollezione
        viewModel.verificaPartecipazioneUtente(evento.id) { staPartecipando ->
            if (staPartecipando) {
                // L'utente ha già partecipato: mostra il messaggio con bottone annulla e nascondi il bottone partecipa
                btnPartecipa?.visibility = View.GONE
                layoutGiaPartecipante?.visibility = View.VISIBLE

                // Collega il click listener al bottone annulla
                btnAnnullaPartecipazione?.setOnClickListener {
                    viewModel.onAnnullaPartecipazioneClicked()
                }
            } else {
                // L'utente non ha ancora partecipato: mostra il bottone partecipa e nascondi il layout partecipante
                btnPartecipa?.visibility = View.VISIBLE
                layoutGiaPartecipante?.visibility = View.GONE
            }
        }
    }

    private fun aggiornaDataEvento(evento: Evento?) {
        val dataTextView = view?.findViewById<TextView>(R.id.dataEventoTextView)
        if (evento?.data != null) {
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
            dataTextView?.text = dateFormatter.format(evento.data)
        } else {
            dataTextView?.text = "Data non specificata"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Pulisci il binding per evitare memory leak
    }
}