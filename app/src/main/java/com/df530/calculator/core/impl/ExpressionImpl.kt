package com.df530.calculator.core.impl

import com.df530.calculator.core.Expression
import com.df530.calculator.core.FloatType
import com.df530.calculator.core.InputToken
import com.df530.calculator.core.inputTokenFromChar

class ExpressionImpl() : Expression {
    private val inputTokens = mutableListOf<InputToken>()
    private var cursorPosition = 0

    constructor(expr: String) : this() {
        for (char in expr)
            addToken(
                inputTokenFromChar(char) ?: throw RuntimeException("Unsupported token '$char'")
            )
    }

    override fun shiftCursorLeft(): Boolean {
        if (cursorPosition > 0) {
            cursorPosition--
            return true
        }
        return false
    }

    override fun shiftCursorRight(): Boolean {
        if (cursorPosition < inputTokens.size) {
            cursorPosition++
            return true
        }
        return false
    }

    override fun getCursorPosition(): Int {
        return cursorPosition
    }

    override fun setCursorPosition(newPosition: Int): Boolean {
        val newPos: Int
        if (newPosition < 0)
            newPos = 0
        else if (newPosition > inputTokens.size)
            newPos = inputTokens.size
        else
            newPos = newPosition
        if (cursorPosition == newPos)
            return false

        cursorPosition = newPos
        return true
    }

    override fun addToken(inputToken: InputToken) {
        inputTokens.add(cursorPosition, inputToken)
        require(cursorPosition < inputTokens.size)
        shiftCursorRight()
    }

    override fun deleteToken(): Boolean {
        require(cursorPosition <= inputTokens.size)
        if (cursorPosition > 0) {
            inputTokens.removeAt(cursorPosition - 1)
            shiftCursorLeft()
            return true
        }
        return false
    }

    override fun clear(): Boolean {
        if (inputTokens.isNotEmpty()) {
            inputTokens.clear()
            cursorPosition = 0
            return true
        }
        return false
    }

    override fun isEmpty() = inputTokens.isEmpty()

    override fun toString(): String {
        return inputTokens.joinToString(separator = "") { it.toString() }
    }

    override fun calculate(): FloatType? {
        if (inputTokens.isEmpty())
            return 0.0

        val expressionTokens = mutableListOf<ExpressionToken>()
        if (!ExpressionScanner.tryScan(inputTokens, expressionTokens))
            return null
        val numbersStack = ArrayDeque<FloatType>()
        val operatorsStack = ArrayDeque<ExpressionToken>()

        for (token in expressionTokens) {
            when (token) {
                is FloatNumber -> numbersStack.addLast(token.value)
                ExpressionBrace.LEFT -> operatorsStack.addLast(token)
                ExpressionBrace.RIGHT -> {
                    var lastOperator: ExpressionToken? = operatorsStack.lastOrNull() ?: return null
                    while (lastOperator is OperatorToken) {
                        if (!applyOperatorToStack(lastOperator, numbersStack))
                            return null
                        operatorsStack.removeLast()
                        lastOperator = operatorsStack.lastOrNull()
                    }
                    if (lastOperator != ExpressionBrace.LEFT)
                        return null
                    operatorsStack.removeLast()
                }
                is UnaryOperatorToken -> {
                    var lastOperator: ExpressionToken? = operatorsStack.lastOrNull()
                    while (lastOperator is BinaryOperatorToken && lastOperator.associativity == Associativity.RIGHT) {
                        if (!applyOperatorToStack(lastOperator, numbersStack))
                            return null
                        operatorsStack.removeLast()
                        lastOperator = operatorsStack.lastOrNull()
                    }
                    operatorsStack.addLast(token)
                }
                is BinaryOperatorToken -> {
                    var lastOperator: ExpressionToken? = operatorsStack.lastOrNull()
                    while (lastOperator is OperatorToken && (
                                lastOperator is UnaryOperatorToken && token.associativity == Associativity.LEFT ||
                                        lastOperator is BinaryOperatorToken &&
                                        (token.associativity == Associativity.LEFT && lastOperator.priority >= token.priority ||
                                                token.associativity == Associativity.RIGHT && lastOperator.priority > token.priority))
                    ) {
                        if (!applyOperatorToStack(lastOperator, numbersStack))
                            return null
                        operatorsStack.removeLast()
                        lastOperator = operatorsStack.lastOrNull()
                    }
                    operatorsStack.addLast(token)
                }
            }
        }

        while (operatorsStack.isNotEmpty()) {
            val lastOperator = operatorsStack.removeLast()
            if (lastOperator !is OperatorToken || !applyOperatorToStack(lastOperator, numbersStack))
                return null
        }
        return numbersStack.lastOrNull()
    }

    companion object {
        private fun applyOperatorToStack(
            operator: OperatorToken,
            numbersStack: ArrayDeque<FloatType>
        ): Boolean {
            when (operator) {
                is UnaryOperatorToken -> {
                    val operand = numbersStack.removeLastOrNull() ?: return false
                    numbersStack.addLast(operator.apply(operand))
                }
                is BinaryOperatorToken -> {
                    val operand2 = numbersStack.removeLastOrNull() ?: return false
                    val operand1 = numbersStack.removeLastOrNull() ?: return false
                    numbersStack.addLast(operator.apply(operand1, operand2))
                }
            }
            return true
        }
    }
}