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

    // onStart() – вызывается перед тем, как Activity будет видно пользователю
    override fun onStart() {
        super.onStart()
        Log.d (log_tag, "onStart method")
    }

    // onResume() – вызывается перед тем как будет доступно для активности пользователя (взаимодействие)
    override fun onResume() {
        super.onResume()
        Log.d (log_tag, "onResume method")
        bGoToCalc.setOnClickListener({
            // Create an Intent to start the second activity
            val randomIntent = Intent(this, Calc::class.java)
            // Start the new activity.
            startActivity(randomIntent)
        });
        bGoToPlayer.setOnClickListener({
            // Create an Intent to start the second activity
            val randomIntent = Intent(this, Player::class.java)
            // Start the new activity.
            startActivity(randomIntent)
        });
    }

    // onPause() – вызывается перед тем, как будет показано другое Activity
    override fun onPause() {
        super.onPause()
        Log.d (log_tag, "onPause method")
    }

    // onStop() – вызывается когда Activity становится не видно пользователю
    override fun onStop() {
        super.onStop()
        Log.d (log_tag, "onStop method")
    }

    // onDestroy() – вызывается перед тем, как Activity будет уничтожено
    override fun onDestroy() {
        super.onDestroy()
        Log.d (log_tag, "onDestroy method")
    }


}