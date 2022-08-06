package com.df530.calculator.core.impl

import com.df530.calculator.core.FloatType
import java.util.function.BinaryOperator
import java.util.function.UnaryOperator
import kotlin.math.pow


internal sealed interface ExpressionToken
internal data class FloatNumber(val value: FloatType) : ExpressionToken
internal enum class ExpressionBrace : ExpressionToken {
    LEFT,
    RIGHT
}

internal sealed interface OperatorToken : ExpressionToken

internal enum class UnaryOperatorToken : OperatorToken, UnaryOperator<FloatType> {
    PLUS {
        override fun apply(p0: FloatType) = p0
    },
    MINUS {
        override fun apply(p0: FloatType) = -p0
    };
}

internal enum class Associativity {
    LEFT, RIGHT
}

internal enum class BinaryOperatorToken(
    val priority: Int,
    val associativity: Associativity = Associativity.LEFT
) : OperatorToken,
    BinaryOperator<FloatType> {
    SUM(1) {
        override fun apply(p0: FloatType, p1: FloatType) = p0 + p1
    },
    SUBTRACT(1) {
        override fun apply(p0: FloatType, p1: FloatType) = p0 - p1
    },
    MULTIPLY(2) {
        override fun apply(p0: FloatType, p1: FloatType) = p0 * p1
    },
    DIVISION(2) {
        override fun apply(p0: FloatType, p1: FloatType) = p0 / p1
    },
    POWER(3, Associativity.RIGHT) {
        override fun apply(p0: FloatType, p1: FloatType) = p0.pow(p1)
    }
}
