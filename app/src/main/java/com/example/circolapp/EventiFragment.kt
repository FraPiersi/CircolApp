// app/src/main/java/com/example/circolapp/EventiFragment.kt
package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.circolapp.databinding.FragmentEventiBinding
import com.example.circolapp.viewmodel.EventiViewModel
import com.example.circolapp.model.Evento // Importa la tua classe Evento
import com.example.circolapp.model.UserRole

class EventiFragment : Fragment() {

    private val viewModel: EventiViewModel by viewModels()
    private var _binding: FragmentEventiBinding? = null // Convenzione per nullable backing property
    private val binding get() = _binding!! // Getter non-nullo

    private lateinit var eventoAdapter: ListaEventi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View { // Rimuovi il tipo nullable se il binding è gestito correttamente
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_eventi, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewModel.eventi.observe(viewLifecycleOwner) { eventiList ->
            eventoAdapter.submitList(eventiList)
        }
        // Mostra il bottone solo se l'utente è ADMIN
        val userRole = (activity?.intent?.getStringExtra("USER_ROLE") ?: "USER")
        if (userRole == "ADMIN") {
            binding.fabAddEvento.visibility = View.VISIBLE
            binding.fabAddEvento.setOnClickListener {
                findNavController().navigate(R.id.action_eventiFragment_to_addEventoFragment)
            }
        } else {
            binding.fabAddEvento.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        val userRole = activity?.intent?.getStringExtra("USER_ROLE")?.let { UserRole.valueOf(it) } ?: UserRole.USER
        eventoAdapter = ListaEventi { evento ->
            val action = EventiFragmentDirections.actionEventiFragmentToInfoEventoFragment(evento, userRole)
            findNavController().navigate(action)
        }

        binding.eventiRecyclerView.apply { // Assicurati di avere un RecyclerView nel tuo layout
            adapter = eventoAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Pulisci il binding per evitare memory leak
    }
}