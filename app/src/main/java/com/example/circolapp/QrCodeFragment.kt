package com.example.circolapp // o il tuo package

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.circolapp.databinding.FragmentQrCodeBinding
import com.example.circolapp.viewmodel.QrCodeViewModel

class QrCodeFragment : Fragment() {

    private var _binding: FragmentQrCodeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QrCodeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_qr_code, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()

        // Richiedi la generazione del QR code quando il fragment è pronto
        viewModel.generateUserQrCode()
    }

    private fun setupObservers() {
        viewModel.qrCodeBitmap.observe(viewLifecycleOwner) { bitmap ->
            // Il Data Binding può essere usato anche per l'immagine,
            // ma impostarlo qui è anche un'opzione comune.
            binding.qrCodeImageView.setImageBitmap(bitmap)
        }

        // Gli observer per isLoading e errorMessage sono già gestiti dal Data Binding nel layout
        // per la visibilità di ProgressBar e TextView dell'errore.
        // Puoi aggiungere qui logica aggiuntiva se necessario.
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            // Ad esempio, potresti voler loggare l'errore o fare altro
            error?.let {
                // Toast.makeText(context, it, Toast.LENGTH_LONG).show() // Già mostrato nel layout
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}