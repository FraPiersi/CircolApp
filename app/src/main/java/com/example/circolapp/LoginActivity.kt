package com.example.circolapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.circolapp.databinding.ActivityLoginBinding // ASSICURATI DI CREARE QUESTO LAYOUT
import com.example.circolapp.viewmodel.AuthEvent
import com.example.circolapp.viewmodel.AuthViewModel
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding // Usa ViewBinding
    private val authViewModel: AuthViewModel by viewModels()

    private val signInLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
            authViewModel.handleSignInResult(result)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root) // Usa il binding per il layout

        // Nascondi la UI di login standard, ci penserà FirebaseUI o il ViewModel
        // Potresti avere un ProgressBar qui gestito dal ViewModel.isLoading
        binding.progressBarLogin.visibility = View.GONE // Assumendo che tu abbia un ProgressBar con id progressBarLogin

        observeViewModel()
    }

    private fun observeViewModel() {
        authViewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBarLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
            // Disabilita/Abilita altri controlli UI se necessario durante il caricamento
        })

        authViewModel.authEvent.observe(this, Observer { event ->
            event?.let {
                when (it) {
                    is AuthEvent.NavigateToMain -> {
                        Toast.makeText(this, "Accesso riuscito! Utente: ${it.user.displayName}", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Chiudi LoginActivity
                    }
                    is AuthEvent.ShowErrorToast -> {
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    }
                    AuthEvent.StartFirebaseUIFlow -> {
                        // Lancia il flusso di FirebaseUI
                        val signInIntent = authViewModel.createSignInIntent()
                        signInLauncher.launch(signInIntent)
                    }
                }
                authViewModel.onAuthEventHandled() // Segnala che l'evento è stato gestito
            }
        })

        // Opzionale: Osserva l'utente corrente se hai bisogno di reagire a cambiamenti di stato di login
        // al di fuori del flusso di login esplicito.
        authViewModel.currentUser.observe(this, Observer { user ->
            if (user != null && supportFragmentManager.findFragmentById(android.R.id.content) == null) {
                // Se l'utente è già loggato all'avvio dell'activity e non c'è già un flusso in corso,
                // potresti reindirizzarlo. Ma l'init del ViewModel già gestisce il lancio di FirebaseUI
                // se non c'è utente.
            }
        })
    }

    // Se hai bisogno di un pulsante di logout da qualche parte (es. in MainActivity, non qui)
    // fun handleSignOut() {
    //     authViewModel.signOut()
    //     // Torna a LoginActivity o gestisci la UI di conseguenza
    // }
}