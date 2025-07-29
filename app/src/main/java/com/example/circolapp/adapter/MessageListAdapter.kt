package com.example.circolapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.circolapp.databinding.ItemMessageReceivedBinding // Verrà creato
import com.example.circolapp.databinding.ItemMessageSentBinding // Verrà creato
import com.example.circolapp.model.Message
import java.text.SimpleDateFormat
import java.util.*

private const val VIEW_TYPE_MESSAGE_SENT = 1
private const val VIEW_TYPE_MESSAGE_RECEIVED = 2

class MessageListAdapter(private val currentUserId: String) :
    ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_MESSAGE_SENT
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SentMessageViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceivedMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    class SentMessageViewHolder(private val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            if (message.isMoneyTransfer) {
                // Gestisci i messaggi di trasferimento denaro
                binding.textViewMessageTextSent.text = "💰 Hai inviato €${String.format("%.2f", message.transferAmount ?: 0.0)}"
                // Cambia il colore o lo stile per i trasferimenti di denaro
                binding.textViewMessageTextSent.setTextColor(
                    binding.root.context.getColor(android.R.color.holo_green_dark)
                )
            } else {
                // Messaggio normale
                binding.textViewMessageTextSent.text = message.text
                // Ripristina il colore normale
                binding.textViewMessageTextSent.setTextColor(
                    binding.root.context.getColor(android.R.color.white)
                )
            }

            binding.textViewMessageTimestampSent.text = message.timestamp?.toDate()?.let {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
            } ?: ""
        }
    }

    class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            if (message.isMoneyTransfer) {
                // Gestisci i messaggi di trasferimento denaro ricevuti
                binding.textViewMessageTextReceived.text = "💰 Hai ricevuto €${String.format("%.2f", message.transferAmount ?: 0.0)}"
                // Cambia il colore o lo stile per i trasferimenti di denaro
                binding.textViewMessageTextReceived.setTextColor(
                    binding.root.context.getColor(android.R.color.holo_green_dark)
                )
            } else {
                // Messaggio normale
                binding.textViewMessageTextReceived.text = message.text
                // Ripristina il colore normale
                binding.textViewMessageTextReceived.setTextColor(
                    binding.root.context.getColor(android.R.color.black)
                )
            }

            binding.textViewMessageTimestampReceived.text = message.timestamp?.toDate()?.let {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
            } ?: ""
            // Potresti anche voler mostrare il nome del mittente o l'avatar qui se non è una chat 1-a-1
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.messageId == newItem.messageId && oldItem.messageId.isNotEmpty()
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }
}