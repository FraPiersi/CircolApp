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
    private        val isSelected = conversation == getSelectedConversation()
        holder.itemView.isSelected = isSelected
        holder.itemView.setBackgroundResource(if (isSelected) R.color.selected_chat_background else 0)

        //clic normali
        holder.itemView.setOnClickListener {
            if (getSelectedConversation() != null) {
            } else {
                onItemClicked(conversation)
            }
        }

        //listener per clic prolungato
        holder.itemView.setOnLongClickListener {
            onItemLongClicked(conversation)
        }
    }

    class ChatViewHolder(private                binding.textViewTimestamp.text = formatTimestamp(date)
            } ?: run {
                binding.textViewTimestamp.text = ""
            }

           
            Glide.with(binding.imageViewProfile.context)
                .load(conversation.otherUserPhotoUrl)
                .placeholder(R.drawable.account) // Immagine placeholder di default
                .error(R.drawable.account)       // Immagine di errore
                .circleCrop() // Rende l'immagine circolare
                .into(binding.imageViewProfile)
        }

        private fun formatTimestamp(date: Date): String {
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
        return oldItem == newItem
    }
}