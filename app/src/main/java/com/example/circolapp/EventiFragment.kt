// app/src/main/java/com/example/circolapp/EventiFragment.kt
package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.circolapp.databinding.FragmentEventiBinding
import com.example.circolapp.databinding.ItemEventoBinding
import com.example.circolapp.viewmodel.EventiViewModel

class EventiFragment : Fragment() {

    private val viewModel: EventiViewModel by viewModels()
    private var binding: FragmentEventiBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_eventi, container, false)

        viewModel.eventi.observe(viewLifecycleOwner) { eventiList ->
            val eventiContainer = binding?.eventiContainer
            eventiContainer?.removeAllViews()
            eventiList.forEach { evento ->
                val itemBinding = ItemEventoBinding.inflate(inflater, eventiContainer, false)
                itemBinding.evento = evento
                eventiContainer?.addView(itemBinding.root)
            }
        }

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}