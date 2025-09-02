package com.example.circolapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.circolapp.adapter.MovimentiAdapter // Assicurati che il percorso sia corretto
import com.example.circolapp.databinding.FragmentHomeBinding
import com.example.circolapp.viewmodel.HomeViewModel // Assicurati che il percorso sia corretto
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var movimentiAdapter: MovimentiAdapter

    private val homeViewModel: HomeViewModel by viewModels()

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Gestione click icona notifiche
        binding.iconNotifiche.setOnClickListener {
            findNavController().navigate(R.id.notificheFragment)
        }

        if (FirebaseAuth.getInstance().currentUser == null) {
            Log.w("HomeFragment", "Utente non loggato. Impossibile caricare i dati.")
            binding.saldoText.text = getString(R.string.login_required_saldo) // Stringa dedicata
            binding.textViewNoDataMessage.text = getString(R.string.login_required_data) // Stringa dedicata
            binding.textViewNoDataMessage.visibility = View.VISIBLE
            binding.recyclerViewMovimenti.visibility = View.GONE
            binding.progressBarHome.visibility = View.GONE
            return
        }

        setupRecyclerView()
        observeViewModel()

        binding.progressBarHome.visibility = View.VISIBLE // Mostra ProgressBar all'inizio
        binding.textViewNoDataMessage.visibility = View.GONE
        binding.recyclerViewMovimenti.visibility = View.GONE // Nascondi RecyclerView finché i dati non arrivano
    }

    private fun setupRecyclerView() {
        movimentiAdapter = MovimentiAdapter() // Istanzia il ListAdapter
        binding.recyclerViewMovimenti.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = movimentiAdapter
        }
    }

    private fun observeViewModel() {
        homeViewModel.saldo.observe(viewLifecycleOwner) { saldo ->
            binding.saldoText.text = getString(R.string.saldo_format, currencyFormatter.format(saldo ?: 0.0))
            // Nascondi la ProgressBar se anche i movimenti sono già arrivati o se questo è l'ultimo dato atteso
            // La ProgressBar viene già gestita dall'observer dei movimenti per ora.
            Log.d("HomeFragment", "Saldo aggiornato: $saldo")
        }

        homeViewModel.movimenti.observe(viewLifecycleOwner) { movimenti ->
            binding.progressBarHome.visibility = View.GONE // Nascondi ProgressBar quando i movimenti arrivano

            if (movimenti.isNullOrEmpty()) {
                binding.textViewNoDataMessage.text = getString(R.string.no_movimenti_found)
                binding.textViewNoDataMessage.visibility = View.VISIBLE
                binding.recyclerViewMovimenti.visibility = View.GONE
            } else {
                binding.textViewNoDataMessage.visibility = View.GONE
                binding.recyclerViewMovimenti.visibility = View.VISIBLE
            }
            movimentiAdapter.submitList(movimenti ?: emptyList()) // Usa submitList con ListAdapter

            Log.d("HomeFragment", "Movimenti aggiornati: ${movimenti?.size ?: 0} elementi")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Non è strettamente necessario impostare l'adapter a null per RecyclerView
        // se il binding viene annullato (_binding = null),
        // ma non fa male e può aiutare in alcuni scenari con listener complessi.
        // binding.recyclerViewMovimenti.adapter = null
        _binding = null
    }
}