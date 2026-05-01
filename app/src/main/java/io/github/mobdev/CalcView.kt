package io.github.mobdev

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CalculatorViewModel : ViewModel() {
    private val _displayText = MutableLiveData("0")
    val displayText: LiveData<String> = _displayText
    private val _error = MutableLiveData<String?>(null)

    private var current = ""
    private var first = 0.0
    private var operator = ""

    fun onDigit(digit: String) {
        current += digit
        _displayText.value = current
    }

    fun onDot() {
        if (!current.contains(".")) {
            if (current.isEmpty()) current = "0"
            current += "."
            _displayText.value = current
        }
    }

    fun onOperator(op: String) {
        if (current.isEmpty()) return
        first = current.toDouble()
        operator = op
        current = ""
    }

    fun onEqual() {
        if (current.isEmpty()) return

        val second = current.toDouble()

        val result = when (operator) {
            "+" -> first + second
            "-" -> first - second
            "×" -> first * second
            "÷" -> {
                if (second == 0.0) {
                    _error.value = "Division by zero"
                    return
                } else first / second
            }
            else -> 0.0
        }

        _error.value = null
        current = result.toString()
        _displayText.value = current
    }

    fun onClear() {
        current = ""
        first = 0.0
        operator = ""
        _displayText.value = "0"
        _error.value = null
    }

    fun onDelete() {
        if (current.isNotEmpty()) {
            current = current.dropLast(1)
            _displayText.value = current.ifEmpty { "0" }
        }
    }
}