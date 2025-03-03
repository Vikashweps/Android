package com.example.progect

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat

import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var tvHelloWorld : TextView = findViewById(R.id.textv1)
        tvHelloWorld.setText("Введите значения")

        // Задаем обработчик нажатия кнопки.
        var bButton1 : Button = findViewById<Button>(R.id.button1)
        bButton1.setOnClickListener({
            tvHelloWorld.setText("1")
        });
        var bButtonTest2 : Button = findViewById<Button>(R.id.button2)
        bButtonTest2.setOnClickListener({
            tvHelloWorld.setText("2")
        });
        var bButtonTest3 : Button = findViewById<Button>(R.id.button3)
        bButtonTest3.setOnClickListener({
            tvHelloWorld.setText("3")
        });
        var bButtonTest4 : Button = findViewById<Button>(R.id.button4)
        bButtonTest4.setOnClickListener({
            tvHelloWorld.setText("4")
        });
        var bButtonTest5 : Button = findViewById<Button>(R.id.button5)
        bButtonTest5.setOnClickListener({
            tvHelloWorld.setText("5")
        });
        var bButtonTest6 : Button = findViewById<Button>(R.id.button6)
        bButtonTest6.setOnClickListener({
            tvHelloWorld.setText("6")
        });
        var bButtonTest7 : Button = findViewById<Button>(R.id.button7)
        bButtonTest7.setOnClickListener({
            tvHelloWorld.setText("7")
        });
        var bButtonTest8 : Button = findViewById<Button>(R.id.button8)
        bButtonTest8.setOnClickListener({
            tvHelloWorld.setText("8")
        });
        var bButtonTest9 : Button = findViewById<Button>(R.id.button9)
        bButtonTest9.setOnClickListener({
            tvHelloWorld.setText("9")
        });
        var bButtonTest10 : Button = findViewById<Button>(R.id.button0)
        bButtonTest10.setOnClickListener({
            tvHelloWorld.setText("0")
        });

        var bButtonTest11 : Button = findViewById<Button>(R.id.buttonPlus)
        bButtonTest11.setOnClickListener({
            tvHelloWorld.setText("+")
        });
        var bButtonTest12 : Button = findViewById<Button>(R.id.buttonMinus)
        bButtonTest12.setOnClickListener({
            tvHelloWorld.setText("-")
        });
        var bButtonTest13 : Button = findViewById<Button>(R.id.buttonUmn)
        bButtonTest13.setOnClickListener({
            tvHelloWorld.setText("x")
        });
        var bButtonTest14 : Button = findViewById<Button>(R.id.buttonDel)
        bButtonTest14.setOnClickListener({
            tvHelloWorld.setText("/")
        });
        var bButtonTest15 : Button = findViewById<Button>(R.id.buttonItog)
        bButtonTest15.setOnClickListener({
            tvHelloWorld.setText("1")

        });
        var counter : Int = 0
        var itog : Int
        fun countButton(view: View) {
            counter++
            var tvH : TextView = findViewById(R.id.textv1)
            tvH.setText(counter.toString())
            var value = tvH.text.toString().toInt()

            Log.d ("BUTTON", value.toString())
        }
    }

}