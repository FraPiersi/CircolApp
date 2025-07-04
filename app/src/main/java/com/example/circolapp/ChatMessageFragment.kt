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

    private val args: ChatMessageFragmentArgs by navArgs() // Generato da Safe Args

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val viewModelFactory: ChatMessageViewModelFactory by lazy {
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Utente non autenticato!", Toast.LENGTH_LONG).show()
            findNavController().popBackStack() // Torna indietro se l'utente non è loggato
            // Potrebbe essere necessario un placeholder factory o gestire meglio questo caso
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

        if (currentUserId == null) { // Controllo aggiuntivo per sicurezza
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupObservers()

        binding.editTextMessageInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Scrolla alla fine quando l'EditText prende il focus,
                // utile se la tastiera copre l'ultimo messaggio.
                binding.recyclerViewMessages.postDelayed({
                    scrollToBottom()
                }, 200)
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbarChatMessage.title = args.contactName ?: "Chat" // Usa il nome del contatto passato
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarChatMessage)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarChatMessage.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        messageListAdapter = MessageListAdapter(currentUserId!!) // Passa l'UID dell'utente corrente
        val layoutManager = LinearLayoutManager(context)
        // layoutManager.stackFromEnd = true // I nuovi messaggi appaiono in fondo e spingono i vecchi in alto
        binding.recyclerViewMessages.apply {
            this.layoutManager = layoutManager
            adapter = messageListAdapter
        }
    }

    private fun setupObservers() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            messageListAdapter.submitList(messages) {
                // Scrolla alla fine solo se l'utente non sta scrollando attivamente all'indietro
                // o se è il primo caricamento / un nuovo messaggio aggiunto alla fine.
                val itemCount = messageListAdapter.itemCount
                if (itemCount > 0) {
                    val lastVisiblePosition = (binding.recyclerViewMessages.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                    // Se l'ultimo elemento visibile è vicino alla fine della lista, o se è il primo caricamento
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
        binding.recyclerViewMessages.post { // Usa post per assicurarti che il layout sia completo
            val itemCount = messageListAdapter.itemCount
            if (itemCount > 0) {
                binding.recyclerViewMessages.smoothScrollToPosition(itemCount - 1)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}