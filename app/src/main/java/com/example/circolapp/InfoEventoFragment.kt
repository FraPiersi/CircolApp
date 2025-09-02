
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
import androidx.fragment.app.viewModelsimport androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.circolapp.databinding.FragmentInfoEventoBinding
import com.example.circolapp.model.Eventoimport com.example.circolapp.model.UserRole
import com.example.circolapp.viewmodel.InfoEventoViewModelimport java.text.SimpleDateFormat
import java.util.*

class InfoEventoFragment : Fragment() {

    private        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userRole = navArguments.userRole
        try {
        viewModel.evento.observe(viewLifecycleOwner) { evento ->
            if (evento == null && viewModel.messaggioToast.value != null) {
                if(isAdded) findNavController().popBackStack()
            }

            aggiornaDataEvento(evento)

            controllaPartecipazioneUtente(evento)
        }

        // Osserva i partecipanti dalla sottocollezione (per admin)
        viewModel.partecipanti.observe(viewLifecycleOwner) { partecipanti ->
            if (userRole == UserRole.ADMIN) {
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

        viewModel.verificaPartecipazioneUtente(evento.id) { staPartecipando ->
            if (staPartecipando) {
               
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
        _binding = null 
    }
}