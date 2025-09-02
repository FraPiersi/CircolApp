package com.example.circolapp
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

    private lateinit        }
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
