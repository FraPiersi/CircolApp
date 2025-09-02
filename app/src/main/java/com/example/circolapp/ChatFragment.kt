package com.example.circolapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.*
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
import com.example.circolapp.R

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatListViewModel by viewModels()
    private lateinit var listaChat: ChatListAdapter
    private var actionMode: ActionMode? = null
    // varaibile per salvare la chat selezionata
    private var selectedConversation: ChatConversation? = null

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
        listaChat = ChatListAdapter(
            onItemClicked = { conversation -> navigateToChatScreen(conversation) },
            // Naviga alla schermata dei messaggi per questa conversazione
            onItemLongClicked = { conversation -> onChatLongClicked(conversation)},
            getSelectedConversation = { selectedConversation }
        )
        binding.recyclerViewChats.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = listaChat
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

    // Nuova funzione: il callback dal nostro adapter
    private fun onChatLongClicked(conversation: ChatConversation): Boolean {
        if (actionMode == null) {
            // Avvia la modalità d'azione solo se non è già attiva
            actionMode = activity?.startActionMode(actionModeCallback)
        }

        //salva la conversazione selezionata
        selectedConversation = conversation
        //aggiorniamo la UI
        listaChat.notifyItemChanged(listaChat.currentList.indexOf(conversation))
        return true // Indica che l'evento è stato consumato
    }
    //la callback che gestirà il ciclo di vita dell'Action Mode
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            //  menu per la barra d'azione
            val inflater = mode?.menuInflater
            inflater?.inflate(R.menu.contextual_action_mode_menu, menu)
            mode?.title = "Seleziona chat" // Titolo della barra
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            // Metodo chiamato per aggiornare la UI della barra d'azione
            // non utilizzata
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            // Metodo chiamato quando un pulsante viene cliccato sulla barra d'azione
            return when (item?.itemId) {
                R.id.action_delete -> {
                    // Logica per l'eliminazione della chat
                    selectedConversation?.let { conversation ->
                        viewModel.deleteChat(conversation.chatId)
                    }
                    mode?.finish() // Chiude la barra d'azione
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            if (selectedConversation != null) {
                val position = listaChat.currentList.indexOf(selectedConversation)
                if (position != -1) {
                    // Notifica all'adapter di aggiornare l'elemento per rimuovere la selezione
                    listaChat.notifyItemChanged(position)
                }
            }
            selectedConversation = null // Resetta la variabile
            actionMode = null // Rimuovi il riferimento
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}