package com.example.circolapp
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModelsimport androidx.appcompat.app.AppCompatActivity
import com.example.circolapp.databinding.ActivityLoginBinding
import com.example.circolapp.model.UserRole
import com.example.circolapp.viewmodel.AuthResult
import com.example.circolapp.viewmodel.AuthViewModel


class LoginActivity : AppCompatActivity() {

    private lateinit    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !isLoading
        binding.editTextEmail.isEnabled = !isLoading
        binding.editTextPassword.isEnabled = !isLoading
    }
}
