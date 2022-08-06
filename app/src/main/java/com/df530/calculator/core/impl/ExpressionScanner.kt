package com.df530.calculator.core.impl

import com.df530.calculator.core.*

internal object ExpressionScanner {
    private val fromOperatorToBinaryOperator = mapOf(
        Operator.PLUS to BinaryOperatorToken.SUM,
        Operator.MINUS to BinaryOperatorToken.SUBTRACT,
        Operator.MULTIPLY_CHAR to BinaryOperatorToken.MULTIPLY,
        Operator.DIVISION_CHAR to BinaryOperatorToken.DIVISION,
        Operator.POW_CHAR to BinaryOperatorToken.POWER,
    )

    /** Returns false if couldn't translate from [InputToken]s to [ExpressionToken]s */
    fun tryScan(
        inputTokens: List<InputToken>,
        expressionTokens: MutableList<ExpressionToken>
    ): Boolean {
        var curTokenIndex = 0
        while (curTokenIndex < inputTokens.size) {
            var token = inputTokens[curTokenIndex]
            when (token) {
                is Digit -> {
                    val numberBuilder = StringBuilder()
                    var isTokenDigitOrDot = { t: InputToken -> t is Digit || t is Dot }
                    while (isTokenDigitOrDot(inputTokens[curTokenIndex])) {
                        token = inputTokens[curTokenIndex]
                        if (token is Digit)
                            numberBuilder.append(token.value.toString())
                        if (token is Dot)
                            numberBuilder.append(".")
                        if (curTokenIndex + 1 < inputTokens.size && isTokenDigitOrDot(inputTokens[curTokenIndex + 1]))
                            curTokenIndex++
                        else
                            break
                    }

                    val number: FloatType =
                        numberBuilder.toString().toDoubleOrNull() ?: return false
                    expressionTokens.add(FloatNumber(number))
                }
                is Dot -> return false

                Brace.LEFT -> expressionTokens.add(ExpressionBrace.LEFT)
                Brace.RIGHT -> expressionTokens.add(ExpressionBrace.RIGHT)

                is Operator -> {
                    val prevToken = expressionTokens.lastOrNull()
                    val operatorToken: ExpressionToken?
                    if (token == Operator.PLUS || token == Operator.MINUS) {
                        if (prevToken is FloatNumber || prevToken == ExpressionBrace.RIGHT) {
                            operatorToken = fromOperatorToBinaryOperator[token]
                        } else if (token == Operator.PLUS) {
                            operatorToken = UnaryOperatorToken.PLUS
                        } else {
                            operatorToken = UnaryOperatorToken.MINUS
                        }
                    } else {
                        operatorToken = fromOperatorToBinaryOperator[token]
                    }
                    requireNotNull(operatorToken)
                    expressionTokens.add(operatorToken)
                }
            }
            curTokenIndex++
        }
        return true
    }
}