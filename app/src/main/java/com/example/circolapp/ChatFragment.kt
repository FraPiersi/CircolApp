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

    private

    // E modifica la navigazione dalla lista delle chat esistenti:
    private fun navigateToChatScreen(conversation: ChatConversation) {
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
                // Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                Log.e("ChatFragment", "Errore osservato: $it")
            }
        }
    }

    // Nuova funzione: il callback dal nostro adapter
    private fun onChatLongClicked(conversation: ChatConversation): Boolean {
        if (actionMode == null) {
            actionMode = activity?.startActionMode(actionModeCallback)
        }

        //salva la conversazione selezionata
        selectedConversation = conversation
        //aggiorniamo la UI
        listaChat.notifyItemChanged(listaChat.currentList.indexOf(conversation))
        return true // Indica che l'evento è stato consumato
    }
    //la callback che gestirà il ciclo di vita dell'Action Mode
    private            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
           
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
           
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
                    listaChat.notifyItemChanged(position)
                }
            }
            selectedConversation = null // Resetta la variabile
            actionMode = null        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}