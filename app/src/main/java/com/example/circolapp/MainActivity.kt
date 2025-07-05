package com.example.circolapp // Assicurati che il package sia corretto

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.circolapp.databinding.ActivityMainBinding // Assumi che usi ViewBinding
import com.example.circolapp.model.UserRole
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.io.path.name

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userRoleString = intent.getStringExtra("USER_ROLE") ?: UserRole.USER.name // Default a USER
        val userRole = try {
            UserRole.valueOf(userRoleString) // Converte la stringa (es. "ADMIN") in enum
        } catch (e: IllegalArgumentException) {
            Log.e("MainActivity", "Ruolo non valido ricevuto: $userRoleString. Default a USER.")
            UserRole.USER
        }

        Log.d("MainActivity", "USER_ROLE ricevuto: $userRole")

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment
        navController = navHostFragment.navController

        // Imposta il NavGraph e la start destination in base al ruolo
        if (userRole == UserRole.ADMIN) {
            // Se hai NavGraph completamente separati e vuoi caricarli:
            // val adminGraph = navController.navInflater.inflate(R.navigation.admin_nav_graph)
            // navController.graph = adminGraph
            // In questo caso, admin_nav_graph.xml avrebbe come startDestination adminHomeFragment.

            // Se usi un unico NavGraph principale (nav_graph.xml) ma con start destination diverse:
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
            navGraph.setStartDestination(R.id.adminHomeFragment)
            navController.graph = navGraph // Applica il graph con la nuova start destination

            setupAdminBottomNavigation()
        } else {
            // Configurazione utente standard
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
            navGraph.setStartDestination(R.id.homeFragment) // ID del fragment di partenza per utente
            navController.graph = navGraph // Applica il graph con la nuova start destination

            setupUserBottomNavigation()
        }
    }

    private fun setupAdminBottomNavigation() {
        binding.bottomNavView.menu.clear() // Rimuovi il menu precedente (se impostato nel XML)
        binding.bottomNavView.inflateMenu(R.menu.admin_bottom_nav_menu) // Carica il menu admin
        binding.bottomNavView.setupWithNavController(navController)
        Log.d("MainActivity", "Setup Admin Bottom Navigation completato.")
    }

    private fun setupUserBottomNavigation() {
        binding.bottomNavView.menu.clear()
        binding.bottomNavView.inflateMenu(R.menu.bottom_nav_menu) // Carica il menu utente standard
        binding.bottomNavView.setupWithNavController(navController)
        Log.d("MainActivity", "Setup User Bottom Navigation completato.")
    }

    // Opzionale: per gestire il pulsante "Indietro" del sistema con il NavController
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
