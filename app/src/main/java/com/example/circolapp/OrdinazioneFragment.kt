package com.example.circolapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class OrdinazioneFragment : Fragment() {

    private val menu = listOf(
        "Panino - €5.00",
        "Pizza - €8.00",
        "Insalata - €4.50"
    )
    private val prezzi = listOf(5.00, 8.00, 4.50)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ordinazione, container, false)

        val spinner = view.findViewById<Spinner>(R.id.spinner_menu)
        val totaleText = view.findViewById<TextView>(R.id.text_totale)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, menu)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Aggiorna il totale quando cambia la selezione
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                totaleText.text = "Totale dovuto: €%.2f".format(prezzi[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        view.findViewById<Button>(R.id.btn_ordina).setOnClickListener {
            Toast.makeText(context, "Ordine inviato!", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}