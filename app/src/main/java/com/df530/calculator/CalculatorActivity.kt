package com.df530.calculator

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.df530.calculator.core.*
import com.df530.calculator.core.impl.ExpressionImpl

class CalculatorActivity : AppCompatActivity() {
    private var curExpression = ExpressionImpl()

    private val buttonsDigitsClickReactions = mapOf(
        R.id.button0 to { curExpression.addToken(Digit.ZERO) },
        R.id.button1 to { curExpression.addToken(Digit.ONE) },
        R.id.button2 to { curExpression.addToken(Digit.TWO) },
        R.id.button3 to { curExpression.addToken(Digit.THREE) },
        R.id.button4 to { curExpression.addToken(Digit.FOUR) },
        R.id.button5 to { curExpression.addToken(Digit.FIVE) },
        R.id.button6 to { curExpression.addToken(Digit.SIX) },
        R.id.button7 to { curExpression.addToken(Digit.SEVEN) },
        R.id.button8 to { curExpression.addToken(Digit.EIGHT) },
        R.id.button9 to { curExpression.addToken(Digit.NINE) },
    )

    private val buttonsOperationsAndDotClickReactions = mapOf(
        R.id.buttonDot to { curExpression.addToken(Dot) },
        R.id.buttonMinus to { curExpression.addToken(Operator.MINUS) },
        R.id.buttonPlus to { curExpression.addToken(Operator.PLUS) },
        R.id.buttonDivision to { curExpression.addToken(Operator.DIVISION_CHAR) },
        R.id.buttonMultiply to { curExpression.addToken(Operator.MULTIPLY_CHAR) },
        R.id.buttonPow to { curExpression.addToken(Operator.POW_CHAR) },
    )

    private val buttonsBracesClickReactions = mapOf(
        R.id.buttonLeftBrace to { curExpression.addToken(Brace.LEFT) },
        R.id.buttonRightBrace to { curExpression.addToken(Brace.RIGHT) },
    )

    private val buttonsMoveCursorClickReactions = mapOf(
        R.id.buttonLeftArrow to { curExpression.shiftCursorLeft() },
        R.id.buttonRightArrow to { curExpression.shiftCursorRight() },
    )

    private val buttonsToken = mapOf(
        R.id.buttonDot to Dot,

        R.id.buttonMinus to Operator.MINUS,
        R.id.buttonPlus to Operator.PLUS,
        R.id.buttonDivision to Operator.DIVISION_CHAR,
        R.id.buttonMultiply to Operator.MULTIPLY_CHAR,
        R.id.buttonPow to Operator.POW_CHAR,
    )

    private lateinit var resultsScrollView: ScrollView
    private lateinit var resultView: TextView
    private lateinit var currentExpressionEditor: EditText
    private lateinit var expressionsHistoryView: TextView

    private fun calculateAndDisplayResult() {
        currentExpressionEditor.setText(curExpression.toString())
        val res = curExpression.calculate()
        if (res == null)
            resultView.text = ""
        else
            resultView.text = res.toString()
        currentExpressionEditor.setSelection(curExpression.getCursorPosition())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        resultView = findViewById(R.id.ResultView)
        currentExpressionEditor = findViewById(R.id.currentExpressionEditor)
        resultsScrollView = findViewById(R.id.resultsScrollView)
        expressionsHistoryView = findViewById(R.id.expressionsHistory)

        // disable keyword
        currentExpressionEditor.showSoftInputOnFocus = false

        // Setting scroll view down when expression or it's result is changing
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                resultsScrollView.post { resultsScrollView.fullScroll(ScrollView.FOCUS_DOWN); }
            }

            override fun afterTextChanged(editable: Editable?) {
            }
        }
        currentExpressionEditor.addTextChangedListener(textWatcher)
        resultView.addTextChangedListener(textWatcher)


        // Setting text on some buttons
        buttonsToken.forEach {
            findViewById<Button>(it.key).text = it.value.toString()
        }

        // Setting click listeners for digit buttons
        buttonsDigitsClickReactions.forEach {
            findViewById<Button>(it.key).setOnClickListener { v ->
                it.value()
                calculateAndDisplayResult()
            }
        }

        // Setting click listeners for operation buttons
        buttonsOperationsAndDotClickReactions.forEach {
            findViewById<Button>(it.key).setOnClickListener { v ->
                if (curExpression.isEmpty())
                    curExpression.addToken(Digit.ZERO);
                it.value()
                calculateAndDisplayResult()
            }
        }

        // Setting click listeners for braces buttons
        buttonsBracesClickReactions.forEach {
            findViewById<Button>(it.key).setOnClickListener { v ->
                it.value()
                calculateAndDisplayResult()
            }
        }

        // Setting click listeners for moving cursor buttons
        buttonsMoveCursorClickReactions.forEach {
            findViewById<Button>(it.key).setOnClickListener { v ->
                if (it.value()) {
                    calculateAndDisplayResult()
                }
            }
        }

        // setting click listener for clear expression button
        findViewById<Button>(R.id.buttonClear).setOnClickListener { v ->
            if (curExpression.clear()) {
                currentExpressionEditor.setText("0")
                resultView.text = "0.0"
                currentExpressionEditor.setSelection(1)
            }
        }

        // Setting click listener for delete token button
        findViewById<Button>(R.id.buttonDelete).setOnClickListener { v ->
            if (curExpression.deleteToken()) {
                calculateAndDisplayResult()
                if (curExpression.isEmpty())
                    currentExpressionEditor.setText("0")
            }
        }

        // Setting click listener for equality button
        findViewById<Button>(R.id.buttonEquality).setOnClickListener { v ->
            val res = curExpression.calculate()
            if (res == null)
                resultView.text = resources.getString(R.string.ErrorMsg)
            else {
                resultView.text = res.toString()
                expressionsHistoryView.append("${curExpression.toString()}=${res.toString()}\n")
            }
        }

        // Setting default expression view
        currentExpressionEditor.setText("0")
        resultView.text = "0.0"
        currentExpressionEditor.setSelection(1)
    }
}