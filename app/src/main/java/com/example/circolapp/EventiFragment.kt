
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
import com.example.circolapp.adapter.EventiAdapter
import com.example.circolapp.databinding.FragmentEventiBinding
import com.example.circolapp.viewmodel.EventiViewModel
import com.example.circolapp.model.Eventoimport com.example.circolapp.model.UserRole

class EventiFragment : Fragment() {

    private    private
    private lateinit            adapter = eventoAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null 
    }
}