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

interface NavControllerProvider {
    fun getAppNavController(): NavController?
}

class MainActivity : AppCompatActivity(), NavControllerProvider {


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
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
            navGraph.setStartDestination(R.id.ProductCatalogFragment)
            navController.graph = navGraph
            // Forza la navigazione verso ProductCatalogFragment dopo aver impostato il graph
            navController.navigate(R.id.ProductCatalogFragment)
            setupAdminBottomNavigation()
        } else {
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
            navGraph.setStartDestination(R.id.homeFragment) // ID del fragment di partenza per utente
            navController.graph = navGraph // Applica il graph con la nuova start destination

            setupUserBottomNavigation()
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("MainActivity_Nav", "Destination changed to: ${destination.label} (ID: ${destination.id})")
        }
        Log.d("MainActivity_Nav", "onCreate - Initial NavController currentDest: ${navController.currentDestination?.label}")

    }
    // In MainActivity
    override fun getAppNavController(): NavController? { // Cambia il tipo di ritorno in NavController?
        Log.d("MainActivity_Life", "getAppNavController called.")
        if (::navController.isInitialized) {
            Log.d("MainActivity_Life", "getAppNavController returning INITIALIZED instance. Current Dest: ${navController.currentDestination?.label}")
            return navController
        } else {
            // Questo blocco non dovrebbe essere raggiunto se l'Activity è stata creata correttamente.
            Log.e("MainActivity_Life", "getAppNavController: appNavControllerInstance IS NOT INITIALIZED! This is a critical error if activity is created.")
            // Prova a reinizializzare come ultima spiaggia, ma questo indica un problema di ciclo di vita.
            return try {
                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment
                navController = navHostFragment.navController
                Log.w("MainActivity_Life", "getAppNavController: Re-initialized appNavControllerInstance successfully.")
                navController
            } catch (e: Exception) {
                Log.e("MainActivity_Life", "getAppNavController: FAILED to re-initialize appNavControllerInstance.", e)
                null // Restituisci null se fallisce
            }
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
