package com.siroha.calculator.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.math.*

data class CalculatorState(
    val display: String = "0",
    val expression: String = "",
    val history: List<HistoryEntry> = emptyList(),
    val isError: Boolean = false,
    val isScientificMode: Boolean = false,
    val isDegreeMode: Boolean = true,
    val isResultShown: Boolean = false,
    val memoryValue: Double = 0.0,
    val hasMemory: Boolean = false
)

data class HistoryEntry(
    val expression: String,
    val result: String
)

sealed class CalculatorAction {
    data class Number(val number: String) : CalculatorAction()
    data class Operator(val operator: String) : CalculatorAction()
    object Decimal : CalculatorAction()
    object Clear : CalculatorAction()
    object ClearEntry : CalculatorAction()
    object Delete : CalculatorAction()
    object Calculate : CalculatorAction()
    object Negate : CalculatorAction()
    object Percent : CalculatorAction()
    object ToggleScientific : CalculatorAction()
    object ToggleDegreeRadian : CalculatorAction()
    object MemoryStore : CalculatorAction()
    object MemoryRecall : CalculatorAction()
    object MemoryClear : CalculatorAction()
    object MemoryAdd : CalculatorAction()
    object ClearHistory : CalculatorAction()
    data class ScientificFunction(val function: String) : CalculatorAction()
}

class CalculatorViewModel : ViewModel() {

    var state by mutableStateOf(CalculatorState())
        private set

    private var firstOperand: Double? = null
    private var pendingOperator: String? = null
    private var shouldResetDisplay = false

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> handleNumber(action.number)
            is CalculatorAction.Operator -> handleOperator(action.operator)
            is CalculatorAction.Decimal -> handleDecimal()
            is CalculatorAction.Clear -> handleClear()
            is CalculatorAction.ClearEntry -> handleClearEntry()
            is CalculatorAction.Delete -> handleDelete()
            is CalculatorAction.Calculate -> handleCalculate()
            is CalculatorAction.Negate -> handleNegate()
            is CalculatorAction.Percent -> handlePercent()
            is CalculatorAction.ToggleScientific -> toggleScientific()
            is CalculatorAction.ToggleDegreeRadian -> toggleDegreeRadian()
            is CalculatorAction.MemoryStore -> handleMemoryStore()
            is CalculatorAction.MemoryRecall -> handleMemoryRecall()
            is CalculatorAction.MemoryClear -> handleMemoryClear()
            is CalculatorAction.MemoryAdd -> handleMemoryAdd()
            is CalculatorAction.ClearHistory -> clearHistory()
            is CalculatorAction.ScientificFunction -> handleScientificFunction(action.function)
        }
    }

    private fun handleNumber(number: String) {
        if (state.isError) {
            state = state.copy(display = number, isError = false, expression = "")
            shouldResetDisplay = false
            return
        }
        if (shouldResetDisplay || state.isResultShown) {
            state = state.copy(display = number, isResultShown = false)
            shouldResetDisplay = false
        } else {
            val currentDisplay = state.display
            val newDisplay = if (currentDisplay == "0") number else currentDisplay + number
            if (newDisplay.length <= 15) {
                state = state.copy(display = newDisplay)
            }
        }
    }

    private fun handleOperator(operator: String) {
        if (state.isError) return

        val current = state.display.toDoubleOrNull() ?: return

        if (firstOperand != null && pendingOperator != null && !shouldResetDisplay && !state.isResultShown) {
            val result = calculate(firstOperand!!, current, pendingOperator!!)
            firstOperand = result
            state = state.copy(
                display = formatResult(result),
                expression = "${formatResult(result)} $operator",
                isResultShown = false
            )
        } else {
            firstOperand = current
            state = state.copy(
                expression = "${formatNumber(current)} $operator",
                isResultShown = false
            )
        }

        pendingOperator = operator
        shouldResetDisplay = true
    }

    private fun handleDecimal() {
        if (state.isError) return
        if (shouldResetDisplay || state.isResultShown) {
            state = state.copy(display = "0.", isResultShown = false)
            shouldResetDisplay = false
            return
        }
        if (!state.display.contains(".")) {
            state = state.copy(display = state.display + ".")
        }
    }

    private fun handleClear() {
        firstOperand = null
        pendingOperator = null
        shouldResetDisplay = false
        state = state.copy(display = "0", expression = "", isError = false, isResultShown = false)
    }

    private fun handleClearEntry() {
        if (state.isError) {
            handleClear()
            return
        }
        state = state.copy(display = "0", isError = false)
        shouldResetDisplay = false
    }

    private fun handleDelete() {
        if (state.isError || state.isResultShown) {
            handleClear()
            return
        }
        val display = state.display
        if (display.length > 1) {
            state = state.copy(display = display.dropLast(1))
        } else {
            state = state.copy(display = "0")
        }
    }

    private fun handleCalculate() {
        if (state.isError) return
        val current = state.display.toDoubleOrNull() ?: return
        val first = firstOperand ?: return
        val operator = pendingOperator ?: return

        val expression = "${formatNumber(first)} $operator ${formatNumber(current)}"
        val result = calculate(first, current, operator)

        val newHistory = (listOf(HistoryEntry(expression, formatResult(result))) + state.history).take(20)

        firstOperand = null
        pendingOperator = null
        shouldResetDisplay = true

        if (result.isNaN() || result.isInfinite()) {
            state = state.copy(
                display = "Error",
                expression = "$expression =",
                isError = true,
                isResultShown = true,
                history = newHistory
            )
        } else {
            state = state.copy(
                display = formatResult(result),
                expression = "$expression =",
                isResultShown = true,
                isError = false,
                history = newHistory
            )
        }
    }

    private fun handleNegate() {
        if (state.isError) return
        val value = state.display.toDoubleOrNull() ?: return
        state = state.copy(display = formatResult(-value))
    }

    private fun handlePercent() {
        if (state.isError) return
        val value = state.display.toDoubleOrNull() ?: return
        val result = if (firstOperand != null && (pendingOperator == "+" || pendingOperator == "-")) {
            firstOperand!! * (value / 100.0)
        } else {
            value / 100.0
        }
        state = state.copy(display = formatResult(result))
    }

    private fun handleScientificFunction(function: String) {
        if (state.isError) return
        val value = state.display.toDoubleOrNull() ?: return
        val angleValue = if (state.isDegreeMode) Math.toRadians(value) else value

        val result = when (function) {
            "sin" -> sin(angleValue)
            "cos" -> cos(angleValue)
            "tan" -> if (cos(angleValue) == 0.0) Double.NaN else tan(angleValue)
            "asin" -> if (value < -1 || value > 1) Double.NaN else {
                val r = asin(value)
                if (state.isDegreeMode) Math.toDegrees(r) else r
            }
            "acos" -> if (value < -1 || value > 1) Double.NaN else {
                val r = acos(value)
                if (state.isDegreeMode) Math.toDegrees(r) else r
            }
            "atan" -> {
                val r = atan(value)
                if (state.isDegreeMode) Math.toDegrees(r) else r
            }
            "log" -> if (value <= 0) Double.NaN else log10(value)
            "ln" -> if (value <= 0) Double.NaN else ln(value)
            "sqrt" -> if (value < 0) Double.NaN else sqrt(value)
            "cbrt" -> cbrt(value)
            "square" -> value * value
            "cube" -> value * value * value
            "inv" -> if (value == 0.0) Double.NaN else 1.0 / value
            "exp" -> exp(value)
            "10x" -> 10.0.pow(value)
            "2x" -> 2.0.pow(value)
            "pi" -> {
                if (shouldResetDisplay || state.isResultShown) {
                    state = state.copy(display = formatResult(PI), isResultShown = false)
                    shouldResetDisplay = false
                }
                return
            }
            "e" -> {
                if (shouldResetDisplay || state.isResultShown) {
                    state = state.copy(display = formatResult(E), isResultShown = false)
                    shouldResetDisplay = false
                }
                return
            }
            "abs" -> abs(value)
            "factorial" -> {
                if (value < 0 || value != floor(value) || value > 20) Double.NaN
                else factorial(value.toInt()).toDouble()
            }
            else -> return
        }

        val expression = "$function(${ formatNumber(value) }) ="
        val newHistory = (listOf(HistoryEntry(expression, formatResult(result))) + state.history).take(20)

        if (result.isNaN() || result.isInfinite()) {
            state = state.copy(display = "Error", expression = expression, isError = true, history = newHistory)
        } else {
            state = state.copy(
                display = formatResult(result),
                expression = expression,
                isResultShown = true,
                history = newHistory
            )
        }
        firstOperand = null
        pendingOperator = null
        shouldResetDisplay = true
    }

    private fun toggleScientific() {
        state = state.copy(isScientificMode = !state.isScientificMode)
    }

    private fun toggleDegreeRadian() {
        state = state.copy(isDegreeMode = !state.isDegreeMode)
    }

    private fun handleMemoryStore() {
        val value = state.display.toDoubleOrNull() ?: return
        state = state.copy(memoryValue = value, hasMemory = true)
    }

    private fun handleMemoryRecall() {
        if (!state.hasMemory) return
        state = state.copy(display = formatResult(state.memoryValue), isResultShown = false)
        shouldResetDisplay = true
    }

    private fun handleMemoryClear() {
        state = state.copy(memoryValue = 0.0, hasMemory = false)
    }

    private fun handleMemoryAdd() {
        val value = state.display.toDoubleOrNull() ?: return
        state = state.copy(memoryValue = state.memoryValue + value, hasMemory = true)
    }

    private fun clearHistory() {
        state = state.copy(history = emptyList())
    }

    private fun calculate(a: Double, b: Double, operator: String): Double {
        return when (operator) {
            "+" -> a + b
            "-" -> a - b
            "×" -> a * b
            "÷" -> if (b == 0.0) Double.NaN else a / b
            "^" -> a.pow(b)
            "%" -> a % b
            else -> b
        }
    }

    private fun factorial(n: Int): Long {
        if (n <= 1) return 1
        return n * factorial(n - 1)
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        return if (value == floor(value) && abs(value) < 1e15) {
            value.toLong().toString()
        } else {
            val formatted = "%.10f".format(value).trimEnd('0').trimEnd('.')
            formatted
        }
    }

    private fun formatNumber(value: Double): String = formatResult(value)
}
