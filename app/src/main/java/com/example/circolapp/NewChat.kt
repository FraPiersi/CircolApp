package com.example.circolapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.circolapp.adapter.UserSelectionAdapter
import com.example.circolapp.databinding.FragmentNewChatBinding
import com.example.circolapp.model.User
import com.example.circolapp.viewmodel.NewChatViewModel

class NewChatFragment : Fragment() {

    private                // che accetti chatId e otherUserId (o come hai definito la tua schermata messaggi)
                    contactName = viewModel.users.value?.firstOrNull { u -> u.uid == it.otherUserId }?.username ?: "Utente",                    contactPhotoUrl = viewModel.users.value?.firstOrNull { u -> u.uid == it.otherUserId }?.photoUrl

                )
                findNavController().navigate(action)
                viewModel.onChatNavigated() // Resetta l'evento per evitare navigazioni multiple
            }
        }

        // Gli observer per isLoading e errorMessage sono gestiti dal Data Binding
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                Log.e("NewChatFragment", "Errore: $it")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}