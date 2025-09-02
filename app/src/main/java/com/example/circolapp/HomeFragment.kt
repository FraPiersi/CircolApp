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
import com.example.circolapp.adapter.MovimentiAdapterimport com.example.circolapp.databinding.FragmentHomeBinding
import com.example.circolapp.viewmodel.HomeViewModelimport com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {
    private    private        binding.iconNotifiche.setOnClickListener {
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

        binding.progressBarHome.visibility = View.VISIBLE        binding.textViewNoDataMessage.visibility = View.GONE
        binding.recyclerViewMovimenti.visibility = View.GONE    }

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
            // La ProgressBar viene già gestita dall'observer dei movimenti per ora.
            Log.d("HomeFragment", "Saldo aggiornato: $saldo")
        }

        homeViewModel.movimenti.observe(viewLifecycleOwner) { movimenti ->
            binding.progressBarHome.visibility = View.GONE
            if (movimenti.isNullOrEmpty()) {
                binding.textViewNoDataMessage.text = getString(R.string.no_movimenti_found)
                binding.textViewNoDataMessage.visibility = View.VISIBLE
                binding.recyclerViewMovimenti.visibility = View.GONE
            } else {
                binding.textViewNoDataMessage.visibility = View.GONE
                binding.recyclerViewMovimenti.visibility = View.VISIBLE
            }
            movimentiAdapter.submitList(movimenti ?: emptyList())
            Log.d("HomeFragment", "Movimenti aggiornati: ${movimenti?.size ?: 0} elementi")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Non è strettamente necessario impostare l'adapter a null per RecyclerView
        // se il binding viene annullato (_binding = null),
        // binding.recyclerViewMovimenti.adapter = null
        _binding = null
    }
}