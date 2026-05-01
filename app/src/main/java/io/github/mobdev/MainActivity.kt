package io.github.mobdev

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val display = findViewById<TextView>(R.id.display)

        viewModel.displayText.observe(this) {
            display.text = it
        }

        val digits = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2,
            R.id.btn3, R.id.btn4, R.id.btn5,
            R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )

        for (id in digits) {
            findViewById<Button>(id).setOnClickListener {
                viewModel.onDigit((it as Button).text.toString())
            }
        }

        findViewById<Button>(R.id.btnAdd).setOnClickListener { viewModel.onOperator("+") }
        findViewById<Button>(R.id.btnSub).setOnClickListener { viewModel.onOperator("-") }
        findViewById<Button>(R.id.btnMult).setOnClickListener { viewModel.onOperator("×") }
        findViewById<Button>(R.id.btnDiv).setOnClickListener { viewModel.onOperator("÷") }

        findViewById<Button>(R.id.btnDot).setOnClickListener { viewModel.onDot() }
        findViewById<Button>(R.id.btnEq).setOnClickListener { viewModel.onEqual() }
        findViewById<Button>(R.id.btnClear).setOnClickListener { viewModel.onClear() }
        findViewById<Button>(R.id.btnDelete).setOnClickListener { viewModel.onDelete() }
    }
}