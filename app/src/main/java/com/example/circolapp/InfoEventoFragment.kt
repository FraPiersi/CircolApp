// InfoEventoFragment.kt
package com.example.circolapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Importa viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.circolapp.databinding.FragmentInfoEventoBinding
import com.example.circolapp.model.Evento // Assicurati che il percorso sia corretto
import com.example.circolapp.viewmodel.InfoEventoViewModel // Importa il ViewModel

class InfoEventoFragment : Fragment() {

    private var _binding: FragmentInfoEventoBinding? = null
    private val binding get() = _binding!!

    // Recupera gli argomenti di navigazione
    private val navArguments: InfoEventoFragmentArgs by navArgs()

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

        // Carica l'evento nel ViewModel
        try {
            val eventoArg: Evento? = navArguments.evento // Può essere nullo se definito come nullable
            viewModel.caricaEvento(eventoArg)
        } catch (e: Exception) {
            Log.e("InfoEventoFragment", "Errore nel recuperare l'argomento evento dagli args: ${e.message}", e)
            Toast.makeText(context, "Errore fatale nel caricare l'evento.", Toast.LENGTH_LONG).show()
            if (isAdded) findNavController().popBackStack()
            return
        }

        setupObservers()
    }

    private fun setupObservers() {
        // Osserva l'evento nel ViewModel (anche se il binding diretto nel layout dovrebbe bastare per la visualizzazione)
        // Questo è utile se devi reagire a cambiamenti dell'evento nel Fragment
        viewModel.evento.observe(viewLifecycleOwner) { evento ->
            if (evento == null && viewModel.messaggioToast.value != null) {
                // Se l'evento è nullo a causa di un errore già gestito in caricaEvento,
                // il Toast e la navigazione potrebbero già essere in corso.
                // Potrebbe essere necessario un controllo più robusto dello stato.
                if(isAdded) findNavController().popBackStack()
            }
            // Il binding nel layout XML dovrebbe aggiornare automaticamente la UI
            // binding.executePendingBindings() // Di solito non necessario con LiveData e lifecycleOwner impostato
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
                // Esempio: Naviga a una schermata di "mie partecipazioni" o aggiorna la UI
                // findNavController().navigate(R.id.action_infoEventoFragment_to_miePartecipazioniFragment)
                viewModel.onAzionePartecipazioneGestita() // Resetta lo stato
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Pulisci il binding per evitare memory leak
    }
}