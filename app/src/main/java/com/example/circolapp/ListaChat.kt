package com.example.circolapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.circolapp.model.ChatItem

class ListaChat(private val chatList: List<ChatItem>) :
    RecyclerView.Adapter<ListaChat.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nome: TextView = view.findViewById(R.id.text_nome)
        val immagine: ImageView = view.findViewById(R.id.image_profilo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        holder.nome.text = chat.nome
        holder.immagine.setImageResource(chat.immagineResId)
    }

    override fun getItemCount() = chatList.size
}