package com.example.circolapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.circolapp.adapter.MessageListAdapter
import com.example.circolapp.databinding.FragmentChatMessageBinding
import com.example.circolapp.viewmodel.ChatMessageViewModel
import com.example.circolapp.viewmodel.ChatMessageViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class ChatMessageFragment : Fragment() {

    private            throw IllegalStateException("Utente non autenticato, impossibile creare ViewModel")
        }
        ChatMessageViewModelFactory(args.chatId, currentUserId!!, args.otherUserId)
    }
    private            return
        }

        setupToolbar()
        setupRecyclerView()
        setupObservers()

        binding.editTextMessageInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.recyclerViewMessages.postDelayed({
                    scrollToBottom()
                }, 200)
            }
        }

        binding.buttonSendMoney.setOnClickListener {
            showMoneyTransferDialog()
        }
    }

    private fun setupToolbar() {
        binding.toolbarChatMessage.title = args.contactName ?: "Chat"
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarChatMessage)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarChatMessage.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        messageListAdapter = MessageListAdapter(currentUserId!!)        binding.recyclerViewMessages.apply {
            this.layoutManager = layoutManager
            adapter = messageListAdapter
        }
    }

    private fun setupObservers() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            messageListAdapter.submitList(messages) {
                    if (lastVisiblePosition == -1 || (itemCount - 1) - lastVisiblePosition < 5) {
                        scrollToBottom()
                    }
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                Log.e("ChatMessageFragment", "Errore: $it")
            }
        }
    }

    private fun scrollToBottom() {
        binding.recyclerViewMessages.post {        viewModel.getCurrentUserBalance { balance ->
            textBalance.text = "Saldo disponibile: €${String.format("%.2f", balance)}"
        }

        builder.setView(customView)
        builder.setPositiveButton("Invia") { _, _ ->
            val amountText = editAmount.text.toString().trim()
            if (amountText.isNotEmpty()) {
                val amount = amountText.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    viewModel.sendMoneyTransfer(amount, args.otherUserId)
                } else {
                    Toast.makeText(context, "Inserisci un importo valido", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Inserisci un importo", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Annulla", null)
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}