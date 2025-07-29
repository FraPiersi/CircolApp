package com.example.circolapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.circolapp.adapter.ChatListAdapter
import com.example.circolapp.databinding.FragmentChatBinding
import com.example.circolapp.model.ChatConversation
import com.example.circolapp.viewmodel.ChatListViewModel

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatListViewModel by viewModels()
    private lateinit var listaChat: ChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        binding.fabNewChat.setOnClickListener {
            findNavController().navigate(R.id.action_chatFragment_to_newChatFragment)
        }
    }
    // In ChatFragment.kt, nel listener del FAB:


    // E modifica la navigazione dalla lista delle chat esistenti:
    private fun navigateToChatScreen(conversation: ChatConversation) {
        val action = ChatFragmentDirections.actionChatFragmentToChatMessageFragment(
            chatId = conversation.chatId,
            otherUserId = conversation.otherUserId,
            contactName = conversation.otherUserName,
            contactPhotoUrl = conversation.otherUserPhotoUrl
        )
        findNavController().navigate(action)
    }

    private fun setupRecyclerView() {
        listaChat = ChatListAdapter { conversation ->
            // Naviga alla schermata dei messaggi per questa conversazione
            navigateToChatScreen(conversation)
        }
        binding.recyclerViewChats.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = listaChat
            // Aggiungi ItemDecoration per i divisori se lo desideri
            // addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun setupObservers() {
        viewModel.conversations.observe(viewLifecycleOwner) { conversations ->
            listaChat.submitList(conversations)
            // La visibilità di textViewNoChats è gestita dal Data Binding
        }

        // isLoading e errorMessage sono gestiti dal Data Binding per ProgressBar e TextViewError
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                // Potresti mostrare un Toast aggiuntivo se lo preferisci, ma il layout lo mostra già
                // Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                Log.e("ChatFragment", "Errore osservato: $it")
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}