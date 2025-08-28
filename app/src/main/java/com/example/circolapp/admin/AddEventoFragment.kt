package com.example.circolapp.admin

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.circolapp.R
import com.example.circolapp.databinding.FragmentAddEventoBinding
import com.example.circolapp.model.Evento
import com.example.circolapp.viewmodel.EventiViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEventoFragment : Fragment() {
    private var _binding: FragmentAddEventoBinding? = null
    private val binding get() = _binding!!
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
    private var dataEventoSelezionata: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_evento, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = requireActivity().let {
            ViewModelProvider(it).get(EventiViewModel::class.java)
        }

        // Gestione del selettore di data
        binding.editDataEvento.setOnClickListener {
            mostraDatePicker()
        }

        binding.btnSalvaEvento.setOnClickListener {
            val nome = binding.editNomeEvento.text.toString().trim()
            val descrizione = binding.editDescrizioneEvento.text.toString().trim()
            val luogo = binding.editLuogoEvento.text.toString().trim()

            // Validazione campi obbligatori
            when {
                nome.isEmpty() -> {
                    Toast.makeText(requireContext(), "Inserisci il nome dell'evento", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                luogo.isEmpty() -> {
                    Toast.makeText(requireContext(), "Inserisci il luogo dell'evento", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                dataEventoSelezionata == null -> {
                    Toast.makeText(requireContext(), "Seleziona la data dell'evento", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val nuovoEvento = Evento(
                nome = nome,
                descrizione = descrizione,
                luogo = luogo,
                data = dataEventoSelezionata
            )

            viewModel.addEvento(nuovoEvento,
                onComplete = {
                    Toast.makeText(requireContext(), "Evento aggiunto!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                },
                onError = {
                    Toast.makeText(requireContext(), "Errore: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.btnAnnullaEvento.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun mostraDatePicker() {
        val calendar = Calendar.getInstance()

        // Imposta la data minima a oggi
        val today = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                dataEventoSelezionata = selectedCalendar.time

                // Aggiorna il campo di testo con la data formattata
                binding.editDataEvento.setText(dateFormatter.format(dataEventoSelezionata!!))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Imposta la data minima a oggi (non si possono creare eventi nel passato)
        datePickerDialog.datePicker.minDate = today.timeInMillis

        datePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}