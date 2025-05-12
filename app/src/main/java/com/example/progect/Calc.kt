package com.example.progect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.lang.Exception

class Calc : AppCompatActivity() {
    private lateinit var bGoToMain1: Button
    private lateinit var result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.calc)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bGoToMain1 = findViewById(R.id.prevC)
        result = findViewById(R.id.textv1)

        bGoToMain1.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Настройка цифровых кнопок
        val numberButtons = listOf<Pair<Int, String>>(
            R.id.button0 to "0",
            R.id.button1 to "1",
            R.id.button2 to "2",
            R.id.button3 to "3",
            R.id.button4 to "4",
            R.id.button5 to "5",
            R.id.button6 to "6",
            R.id.button7 to "7",
            R.id.button8 to "8",
            R.id.button9 to "9"
        )

        numberButtons.forEach { (id, value) ->
            findViewById<Button>(id).setOnClickListener {
                result.append(value)
            }
        }

        val operatorButtons = listOf<Pair<Int, String>>(
            R.id.buttonPlus to "+",
            R.id.buttonMinus to "-",
            R.id.buttonUmn to "×",
            R.id.buttonDel to "/"
        )

        operatorButtons.forEach { (id, value) ->
            findViewById<Button>(id).setOnClickListener {
                result.append(value)
            }
        }

        findViewById<Button>(R.id.buttonC).setOnClickListener {
            result.text = ""
        }

        findViewById<Button>(R.id.buttonItog).setOnClickListener {
            try {
                val expression = result.text.toString().replace("×", "*")
                val resultValue = evaluateExpression(expression)
                result.text = if (resultValue % 1 == 0.0) {
                    String.format("%.0f", resultValue)
                } else {
                    String.format("%.2f", resultValue)
                }
            } catch (e: Exception) {
                result.text = "Ошибка: ${e.message}"
            }
        }
    }

    private fun evaluateExpression(expression: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Некорректный символ: ${expression[pos]}")
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.code) -> x += parseTerm()
                        eat('-'.code) -> x -= parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*'.code) -> x *= parseFactor()
                        eat('/'.code) -> x /= parseFactor()
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                val startPos = pos
                var x: Double
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Некорректный символ: ${ch.toChar()}")
                }
                return x
            }
        }.parse()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()

    }

    override fun onStop() {
        super.onStop()
    }
    override fun onDestroy() {
        super.onDestroy()
    }
}