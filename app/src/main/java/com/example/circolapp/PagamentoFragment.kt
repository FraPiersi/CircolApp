
package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.circolapp.model.UserRole



class PagamentoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view.findViewById<Button>(R.id.btn_opzione1).setOnClickListener {
            // Safe Args genererà una classe PagamentoFragmentDirections
        view.findViewById<Button>(R.id.btn_opzione2).setOnClickListener {
            // Assumendo che tu abbia un'action definita anche per questo nel nav_graph
            // e che tu voglia usare il Navigation Component per coerenza.
            val action = PagamentoFragmentDirections.actionPagamentoFragmentToProductCatalogFragment()
            findNavController().navigate(action)

        }

        return view
    }
}