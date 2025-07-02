package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Catalogo : Fragment() {
    private val catalogo = listOf("Elemento 1", "Elemento 2", "Elemento 3", "Elemento 4")
    private var filteredCatalogo = catalogo.toList()
    private lateinit var adapter: ListaCatalogo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_catalogo, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_catalogo)
        adapter = ListaCatalogo(filteredCatalogo)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val editSearch = view.findViewById<EditText>(R.id.edit_search)
        view.findViewById<Button>(R.id.btn_search).setOnClickListener {
            val query = editSearch.text.toString().trim()
            filteredCatalogo = if (query.isEmpty()) {
                catalogo
            } else {
                catalogo.filter { it.contains(query, ignoreCase = true) }
            }
            adapter.updateList(filteredCatalogo)
        }

        return view
    }
}