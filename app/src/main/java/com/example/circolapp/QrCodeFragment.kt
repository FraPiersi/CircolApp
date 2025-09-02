package com.example.circolapp

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

        viewModel.generateUserQrCode()
    }

    private fun setupObservers() {
        viewModel.qrCodeBitmap.observe(viewLifecycleOwner) { bitmap ->
            binding.qrCodeImageView.setImageBitmap(bitmap)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}