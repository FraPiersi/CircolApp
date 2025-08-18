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

    private var _binding: FragmentNewChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewChatViewModel by viewModels()
    private lateinit var userSelectionAdapter: UserSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_chat, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        userSelectionAdapter = UserSelectionAdapter { selectedUser ->
            viewModel.startChatWithUser(selectedUser)
        }
        binding.recyclerViewUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userSelectionAdapter
        }
    }

    private fun setupObservers() {
        viewModel.users.observe(viewLifecycleOwner) { users ->
            userSelectionAdapter.submitList(users)
        }

        viewModel.navigateToChat.observe(viewLifecycleOwner) { navigationEvent ->
            navigationEvent?.let {
                // Naviga alla schermata dei messaggi
                // Assicurati di avere un'azione nel nav_graph da NewChatFragment a ChatMessageFragment
                // che accetti chatId e otherUserId (o come hai definito la tua schermata messaggi)
                val action = NewChatFragmentDirections.actionNewChatFragmentToChatMessageFragment(
                    chatId = it.chatId,
                    otherUserId = it.otherUserId, // Questo potrebbe essere il nome dell'altro utente o il suo UID
                    contactName = viewModel.users.value?.firstOrNull { u -> u.uid == it.otherUserId }?.username ?: "Utente", // Passa il nome per la UI della chat
                    contactPhotoUrl = viewModel.users.value?.firstOrNull { u -> u.uid == it.otherUserId }?.photoUrl

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