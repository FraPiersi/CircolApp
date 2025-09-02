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
    private        datePickerDialog.datePicker.minDate = today.timeInMillis

        datePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}