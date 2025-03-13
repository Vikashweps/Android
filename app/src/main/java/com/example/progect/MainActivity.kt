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
import java.lang.Exception




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
        var tvHelloWorld: TextView = findViewById(R.id.textv1)
        tvHelloWorld.setText("")

        val result: TextView = findViewById(R.id.textv1) as TextView

        // Задаем обработчик нажатия кнопки.
        var b1: Button = findViewById<Button>(R.id.button1)
        b1.setOnClickListener({ result.append("1") });

        var bc: Button = findViewById<Button>(R.id.buttonC)
        bc.setOnClickListener({tvHelloWorld.setText("")});

        var bButtonTest2: Button = findViewById<Button>(R.id.button2)
        bButtonTest2.setOnClickListener({ result.append("2") });

        var bButtonTest3: Button = findViewById<Button>(R.id.button3)
        bButtonTest3.setOnClickListener({ result.append("3") });

        var bButtonTest4: Button = findViewById<Button>(R.id.button4)
        bButtonTest4.setOnClickListener({ result.append("4") });

        var bButtonTest5: Button = findViewById<Button>(R.id.button5)
        bButtonTest5.setOnClickListener({ result.append("5") });

        var bButtonTest6: Button = findViewById<Button>(R.id.button6)
        bButtonTest6.setOnClickListener({ result.append("6") });

        var bButtonTest7: Button = findViewById<Button>(R.id.button7)
        bButtonTest7.setOnClickListener({ result.append("7") });

        var bButtonTest8: Button = findViewById<Button>(R.id.button8)
        bButtonTest8.setOnClickListener({ result.append("8") });

        var bButtonTest9: Button = findViewById<Button>(R.id.button9)
        bButtonTest9.setOnClickListener({ result.append("9") });

        var bButtonTest10: Button = findViewById<Button>(R.id.button0)
        bButtonTest10.setOnClickListener({ result.append("0") });

        var bButtonTest11: Button = findViewById<Button>(R.id.buttonPlus)
        bButtonTest11.setOnClickListener({ result.append("+") });

        var bButtonTest12: Button = findViewById<Button>(R.id.buttonMinus)
        bButtonTest12.setOnClickListener({ result.append("-") });

        var bButtonTest13: Button = findViewById<Button>(R.id.buttonUmn)
        bButtonTest13.setOnClickListener({ result.append("x") });

        var bButtonTest14: Button = findViewById<Button>(R.id.buttonDel)
        bButtonTest14.setOnClickListener({ result.append("/") });

        var bButtonTest15: Button = findViewById<Button>(R.id.buttonItog)


        fun evaluateExpression(expression: String): Double {
            var itogo = 0.0

            try {

                val trimmedExpression = expression.replace(" ", "")
                val operator = when {
                    trimmedExpression.contains("+") -> "+"
                    trimmedExpression.contains("-") -> "-"
                    trimmedExpression.contains("x") -> "x"
                    trimmedExpression.contains("/") -> "/"
                    else -> throw IllegalArgumentException("Операция не поддерживается")
                }
                val numbers = trimmedExpression.split(operator)

                if (numbers.size != 2) {
                    throw IllegalArgumentException("некорректный формат")
                }
                val num1 = numbers[0].toDouble()
                val num2 = numbers[1].toDouble()


                itogo = when (operator) {
                    "+" -> num1 + num2
                    "-" -> num1 - num2
                    "x" -> num1 * num2
                    "/" -> {
                        if (num2 == 0.0) throw ArithmeticException("нулевое значение")
                        num1 / num2
                    }

                    else -> throw IllegalArgumentException(" ")
                }

                return itogo

            } catch (e: NumberFormatException) {

                throw IllegalArgumentException("Недопустимый формат чисел: " + e.message)

            }
        }

        bButtonTest15.setOnClickListener {

            val expression = result.text.toString()

            try {
                val result1 = evaluateExpression(expression)

                result.text =String.format("%.2f",result1)

            }
            catch (e: Exception)
            {
                result.text = "ошибка : $e"
            }

        }
    }
}