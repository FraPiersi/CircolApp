package com.example.circolapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.circolapp.databinding.ActivityMainBinding
import com.example.circolapp.model.UserRole
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.io.path.name

interface NavControllerProvider {
    fun getAppNavController(): NavController?
}

class MainActivity : AppCompatActivity(), NavControllerProvider {

    private lateinit            return try {
            }
        }
    }
    private fun setupAdminBottomNavigation() {
        binding.bottomNavView.menu.clear()        binding.bottomNavView.inflateMenu(R.menu.admin_bottom_nav_menu)        binding.bottomNavView.setupWithNavController(navController)
        Log.d("MainActivity", "Setup Admin Bottom Navigation completato.")
    }

    private fun setupUserBottomNavigation() {
        binding.bottomNavView.menu.clear()
        binding.bottomNavView.inflateMenu(R.menu.bottom_nav_menu)        binding.bottomNavView.setupWithNavController(navController)
        Log.d("MainActivity", "Setup User Bottom Navigation completato.")
    }

    // Opzionale: per gestire il pulsante "Indietro" del sistema con il NavController
    override fun onSupportNavigateUp(): Boolean {
        val result = navController.navigateUp() || super.onSupportNavigateUp()
        Log.d("MainActivity_Nav", "onSupportNavigateUp - result: $result, new currentDest: ${navController.currentDestination?.label}")
        return result
    }

    override fun onResume() {
        super.onResume()
        if (::navController.isInitialized) {
            Log.d("MainActivity_Nav", "onResume - NavController currentDest: ${navController.currentDestination?.label}")
        }
    }
}
