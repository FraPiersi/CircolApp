package com.example.circolapp // Assicurati che il package sia corretto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels // Per usare by viewModels()
import androidx.appcompat.app.AppCompatActivity
import com.example.circolapp.databinding.ActivityLoginBinding
import com.example.circolapp.model.UserRole
import com.example.circolapp.viewmodel.AuthResult
import com.example.circolapp.viewmodel.AuthViewModel
// Non hai più bisogno di importare FirebaseAuth e FirebaseFirestore direttamente qui

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels() // Inizializza il ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeAuthResult()

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            authViewModel.loginUser(email, password)
        }


        binding.textViewRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeAuthResult() {
        authViewModel.authResult.observe(this) { result ->
            when (result) {
                is AuthResult.Loading -> {
                    setLoading(true)
                }
                is AuthResult.Success -> {
                    setLoading(false)
                    Log.d("LoginActivity", "Autenticazione riuscita. Ruolo: ${result.userRole.name}")
                    navigateToMainActivity(result.userRole)
                    authViewModel.resetAuthResult() // Resetta per evitare trigger multipli se l'activity viene ricreata
                }
                is AuthResult.Error -> {
                    setLoading(false)
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    Log.w("LoginActivity", "Errore di autenticazione: ${result.message}")
                    authViewModel.resetAuthResult()
                }
                is AuthResult.Idle -> {
                    setLoading(false)
                    // Stato iniziale o dopo un logout, non fare nulla o prepara la UI
                }
            }
        }
    }

    private fun navigateToMainActivity(userRole: UserRole) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_ROLE", userRole.name) // Passa il nome dell'enum (es. "ADMIN" o "USER")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish() // Termina LoginActivity
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !isLoading
        binding.editTextEmail.isEnabled = !isLoading
        binding.editTextPassword.isEnabled = !isLoading
    }
}
