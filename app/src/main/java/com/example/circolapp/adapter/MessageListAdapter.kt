package com.example.circolapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.circolapp.databinding.ItemMessageReceivedBinding
import com.example.circolapp.databinding.ItemMessageSentBinding
import com.example.circolapp.model.Message
import java.text.SimpleDateFormat
import java.util.*

private const                binding.textViewMessageTextSent.text = "💰 Hai inviato €${String.format("%.2f", message.transferAmount ?: 0.0)}"
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

    class ReceivedMessageViewHolder(private                binding.textViewMessageTextReceived.setTextColor(
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