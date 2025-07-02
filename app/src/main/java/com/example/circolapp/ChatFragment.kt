package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.circolapp.model.ChatItem

class ChatFragment : Fragment() {
    private val chatList = listOf(
        ChatItem("Mario Rossi", R.drawable.account),
        ChatItem("Luca Bianchi", R.drawable.account),
        ChatItem("Giulia Verdi", R.drawable.account)
    )
    private lateinit var adapter: ListaChat

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_chat)
        adapter = ListaChat(chatList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        return view
    }
}