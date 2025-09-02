package com.example.circolapp // Assicurati che il package sia corretto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.circolapp.databinding.ActivityRegisterBinding
import com.example.circolapp.model.UserRole
import com.example.circolapp.viewmodel.RegisterViewModel
import com.example.circolapp.viewmodel.RegistrationResult

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val registerViewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeRegistrationStatus()

        binding.buttonRegister.setOnClickListener {
            val displayName = binding.editTextDisplayName.text.toString().trim()
            val email = binding.editTextEmailRegister.text.toString().trim()
            val password = binding.editTextPasswordRegister.text.toString().trim()

            registerViewModel.registerUser(email, password, displayName)
        }

        binding.textViewLoginLink.setOnClickListener {
            // Torna alla LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun observeRegistrationStatus() {
        registerViewModel.registrationStatus.observe(this) { result ->
            when (result) {
                is RegistrationResult.Loading -> {
                    setLoading(true)
                }
                is RegistrationResult.Success -> {
                    setLoading(false)
                    Toast.makeText(this, "Registrazione completata con successo!", Toast.LENGTH_LONG).show()
                    Log.d("RegisterActivity", "Registrazione riuscita per: ${result.user.email}")
                    // Dopo la registrazione, l'utente è un "USER" standard.
                    // Puoi navigare alla MainActivity passandogli il ruolo USER.
                    navigateToMainActivity()
                }
                is RegistrationResult.Error -> {
                    setLoading(false)
                    Toast.makeText(this, "Errore di registrazione: ${result.message}", Toast.LENGTH_LONG).show()
                    Log.w("RegisterActivity", "Errore di registrazione: ${result.message}")
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            // L'utente appena registrato è sempre un USER standard
            putExtra("USER_ROLE", UserRole.USER.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Pulisce lo stack
        }
        startActivity(intent)
        finishAffinity() // Chiude RegisterActivity e LoginActivity (se era nello stack)
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarRegister.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonRegister.isEnabled = !isLoading
        binding.editTextDisplayName.isEnabled = !isLoading
        binding.editTextEmailRegister.isEnabled = !isLoading
        binding.editTextPasswordRegister.isEnabled = !isLoading
        binding.textViewLoginLink.isEnabled = !isLoading
    }
}
