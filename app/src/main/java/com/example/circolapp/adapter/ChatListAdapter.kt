package com.example.circolapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.circolapp.R
import com.example.circolapp.databinding.ItemChatBinding
import com.example.circolapp.model.ChatConversation
import java.text.SimpleDateFormat
import java.util.*

class ChatListAdapter(
    private val onItemClicked: (ChatConversation) -> Unit,
    // funzione da chiamare quando si verifica un clic prolungato
    private val onItemLongClicked: (ChatConversation) -> Boolean,
    private val getSelectedConversation: () -> ChatConversation? ) :
    ListAdapter<ChatConversation, ChatListAdapter.ChatViewHolder>(ChatConversationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val conversation = getItem(position)
        holder.bind(conversation)

        // Controlla se l'elemento corrente è quello selezionato
        val isSelected = conversation == getSelectedConversation()
        holder.itemView.isSelected = isSelected
        holder.itemView.setBackgroundResource(if (isSelected) R.color.selected_chat_background else 0)

        //clic normali
        holder.itemView.setOnClickListener {
            if (getSelectedConversation() != null) {
                // Se c'è una selezione, disabilita il clic
            } else {
                onItemClicked(conversation)
            }
        }

        //listener per clic prolungato
        holder.itemView.setOnLongClickListener {
            onItemLongClicked(conversation)
        }
    }

    class ChatViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: ChatConversation) {
            binding.textViewContactName.text = conversation.otherUserName
            binding.textViewLastMessage.text = conversation.lastMessageText

            conversation.lastMessageTimestamp?.toDate()?.let { date ->
                // Formatta il timestamp in una stringa leggibile (es. "10:30" o "Ieri" o "23/03")
                binding.textViewTimestamp.text = formatTimestamp(date)
            } ?: run {
                binding.textViewTimestamp.text = ""
            }

            // Carica l'immagine del profilo usando Glide
            Glide.with(binding.imageViewProfile.context)
                .load(conversation.otherUserPhotoUrl)
                .placeholder(R.drawable.account) // Immagine placeholder di default
                .error(R.drawable.account)       // Immagine di errore
                .circleCrop() // Rende l'immagine circolare
                .into(binding.imageViewProfile)
        }

        private fun formatTimestamp(date: Date): String {
            val calendar = Calendar.getInstance()
            calendar.time = date
            val today = Calendar.getInstance()

            return if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            ) {
                // Oggi: mostra solo l'ora
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            } else if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1
            ) {
                // Ieri
                "Ieri"
            } else {
                // Altro: mostra la data
                SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
            }
        }
    }
}

class ChatConversationDiffCallback : DiffUtil.ItemCallback<ChatConversation>() {
    override fun areItemsTheSame(oldItem: ChatConversation, newItem: ChatConversation): Boolean {
        return oldItem.chatId == newItem.chatId
    }

    override fun areContentsTheSame(oldItem: ChatConversation, newItem: ChatConversation): Boolean {
        return oldItem == newItem // Data class compara i contenuti
    }
}