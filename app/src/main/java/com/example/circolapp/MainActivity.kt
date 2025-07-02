package com.example.circolapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
                    true }
                R.id.nav_search -> { supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, InfoLocaleFragment())
                    .commit()
                    true }
                R.id.nav_add -> { supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MenuOpzioniFragment())
                    .commit()
                    true }
                R.id.nav_notifications -> { supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ChatFragment())
                    .commit()
                    true }
                R.id.nav_profile -> { supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfiloFragment())
                    .commit()
                    true }
                else -> false
            }
        }
    }

}

