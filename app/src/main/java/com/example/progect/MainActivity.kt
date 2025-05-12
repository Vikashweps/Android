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
    private var log_tag : String = "MY_LOG_TAG"
    private lateinit var bGoToCalc: Button
    private lateinit var bGoToPlayer: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bGoToCalc = findViewById<Button>(R.id.calc)
        bGoToPlayer = findViewById<Button>(R.id.player)
    }

    override fun onStart() {
        super.onStart()
        Log.d (log_tag, "onStart method")
    }
    override fun onResume() {
        super.onResume()
        Log.d (log_tag, "onResume method")
        bGoToCalc.setOnClickListener({

            val randomIntent = Intent(this, Calc::class.java)

            startActivity(randomIntent)
        });
        bGoToPlayer.setOnClickListener({
            val randomIntent = Intent(this, Player::class.java)
            startActivity(randomIntent)
        });
    }

    override fun onPause() {
        super.onPause()
        Log.d (log_tag, "onPause method")
    }

    override fun onStop() {
        super.onStop()
        Log.d (log_tag, "onStop method")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d (log_tag, "onDestroy method")
    }


}