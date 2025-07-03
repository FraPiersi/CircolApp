package com.example.circolapp

// com/example/circolapp/HomeFragment.kt
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.circolapp.databinding.FragmentHomeBinding
import com.example.circolapp.viewmodel.HomeViewModel
import com.example.circolapp.viewmodel.HomeViewModelFactory

class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private lateinit var adapter: MovimentiAdapter
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory("username")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        adapter = MovimentiAdapter(emptyList())
        binding?.recyclerViewMovimenti?.layoutManager = LinearLayoutManager(context)
        binding?.recyclerViewMovimenti?.adapter = adapter

        viewModel.saldo.observe(viewLifecycleOwner) { saldo ->
            binding?.saldoText?.text = "Saldo: € $saldo"
        }
        viewModel.movimenti.observe(viewLifecycleOwner) { movimenti ->
            adapter.movimenti = movimenti
            adapter.notifyDataSetChanged()
        }
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}