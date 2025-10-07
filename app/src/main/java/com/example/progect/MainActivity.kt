package com.example.progect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private var log_tag: String = "MY_LOG_TAG"
    private lateinit var bGoToCalc: Button
    private lateinit var bGoToPlayer: Button
    private lateinit var bGoToLocation: Button
    private lateinit var bGoToSoket: Button
    private lateinit var bGoToSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Инициализация кнопок
        bGoToCalc = findViewById(R.id.calc)
        bGoToPlayer = findViewById(R.id.player)
        bGoToLocation = findViewById(R.id.location)
        bGoToSoket = findViewById(R.id.soket)
        bGoToSave = findViewById(R.id.saveLocation)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        bGoToCalc.setOnClickListener {
            val intent = Intent(this, Calc::class.java)
            startActivity(intent)
        }

        bGoToPlayer.setOnClickListener {
            val intent = Intent(this, Player::class.java)
            startActivity(intent)
        }

        /*bGoToLocation.setOnClickListener {
            val intent = Intent(this, Locations::class.java)
            startActivity(intent)
        }*/

        /*bGoToSoket.setOnClickListener {
            val intent = Intent(this, Soket::class.java)
            startActivity(intent)
        }*/
        bGoToSave.setOnClickListener {
            val intent = Intent(this, Save_locations::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(log_tag, "onStart method")
    }

    override fun onResume() {
        super.onResume()
        Log.d(log_tag, "onResume method")
    }

    override fun onPause() {
        super.onPause()
        Log.d(log_tag, "onPause method")
    }

    override fun onStop() {
        super.onStop()
        Log.d(log_tag, "onStop method")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(log_tag, "onDestroy method")
    }
}