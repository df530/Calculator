package com.df530.calculator.core

sealed interface InputToken {
    override fun toString(): String
}

enum class Digit(val value: Int) : InputToken {
    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5),
    SIX(6), SEVEN(7), EIGHT(8), NINE(9), ZERO(0);

    override fun toString(): String {
        return value.toString()
    }
}


object Dot : InputToken {
    override fun toString() = "."
}

enum class Brace(private val stringValue: String) : InputToken {
    LEFT("("), RIGHT(")");

    override fun toString() = stringValue
}

enum class Operator(private val stringValue: String) : InputToken {
    PLUS("+"), MINUS("-"),
    MULTIPLY_CHAR("×"), DIVISION_CHAR("/"),
    POW_CHAR("^");

    override fun toString() = stringValue
}

val allPossibleTokens: Array<InputToken> = arrayOf<InputToken>()
    .plus(Digit.values())
    .plus(Dot)
    .plus(Operator.values())
    .plus(Brace.values())

fun inputTokenFromChar(char: Char): InputToken? {
    for (token in allPossibleTokens) {
        if (char.toString() == token.toString()) {
            return token
        }
    }
    return null
}