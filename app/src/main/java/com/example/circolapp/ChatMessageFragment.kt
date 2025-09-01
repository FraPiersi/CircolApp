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

    private var _binding: FragmentChatMessageBinding? = null
    private val binding get() = _binding!!

    private val args: ChatMessageFragmentArgs by navArgs()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val viewModelFactory: ChatMessageViewModelFactory by lazy {
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Utente non autenticato!", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            throw IllegalStateException("Utente non autenticato, impossibile creare ViewModel")
        }
        ChatMessageViewModelFactory(args.chatId, currentUserId!!, args.otherUserId)
    }
    private val viewModel: ChatMessageViewModel by viewModels { viewModelFactory }


    private lateinit var messageListAdapter: MessageListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_message, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (currentUserId == null) { 
            return
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
        messageListAdapter = MessageListAdapter(currentUserId!!) 
        val layoutManager = LinearLayoutManager(context)
        // layoutManager.stackFromEnd = true 
        binding.recyclerViewMessages.apply {
            this.layoutManager = layoutManager
            adapter = messageListAdapter
        }
    }

    private fun setupObservers() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            messageListAdapter.submitList(messages) {
                
                
                val itemCount = messageListAdapter.itemCount
                if (itemCount > 0) {
                    val lastVisiblePosition = (binding.recyclerViewMessages.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                    
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
        binding.recyclerViewMessages.post { 
            val itemCount = messageListAdapter.itemCount
            if (itemCount > 0) {
                binding.recyclerViewMessages.smoothScrollToPosition(itemCount - 1)
            }
        }
    }

    private fun showMoneyTransferDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(
            android.R.layout.select_dialog_item, null
        )

        
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Invia denaro a ${args.contactName}")

        
        val customView = LayoutInflater.from(requireContext()).inflate(
            R.layout.dialog_money_transfer, null
        )

        val editAmount = customView.findViewById<android.widget.EditText>(R.id.editTextAmount)
        val textBalance = customView.findViewById<android.widget.TextView>(R.id.textViewBalance)

        
        viewModel.getCurrentUserBalance { balance ->
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