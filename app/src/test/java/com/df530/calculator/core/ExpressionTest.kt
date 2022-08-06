package com.df530.calculator.core

import org.junit.Assert
import org.junit.Test

abstract class ExpressionTest {
    companion object {
        val multiplySymbol = Operator.MULTIPLY_CHAR.toString()
        val divisionSymbol = Operator.DIVISION_CHAR.toString()
        val powerSymbol = Operator.POW_CHAR.toString()
        val bigExpressionString: String =
            // -10^2 + (-(+6 / 2 - 3 * 6)^2)
            "-10${powerSymbol}2+(-(+6${divisionSymbol}2-3${multiplySymbol}6)${powerSymbol}2)"
    }

    abstract fun createExpression(): Expression

    fun createExpressionFromString(exprString: String): Expression {
        val expr = createExpression()
        for (char in exprString)
            expr.addToken(
                inputTokenFromChar(char) ?: throw RuntimeException("Unsupported token '$char'")
            )
        return expr
    }

    @Test
    fun testCreateExpressionFromString() {
        Assert.assertEquals(
            createExpressionFromString(bigExpressionString).toString(),
            bigExpressionString
        )
    }

    @Test
    fun testShiftCursor() {
        val expr: Expression = createExpressionFromString(bigExpressionString)
        Assert.assertEquals(bigExpressionString.length, expr.getCursorPosition())
        Assert.assertFalse(expr.shiftCursorRight())
        for (i in 1..bigExpressionString.length) {
            Assert.assertTrue(expr.shiftCursorLeft())
            Assert.assertEquals(bigExpressionString.length - i, expr.getCursorPosition())
        }
        Assert.assertFalse(expr.shiftCursorLeft())
        for (i in 1..bigExpressionString.length) {
            Assert.assertTrue(expr.shiftCursorRight())
            Assert.assertEquals(i, expr.getCursorPosition())
        }
    }

    @Test
    fun testSetCursorPosition() {
        val expr: Expression = createExpressionFromString(bigExpressionString)
        Assert.assertEquals(bigExpressionString.length, expr.getCursorPosition())


        Assert.assertTrue(expr.setCursorPosition(-1))
        Assert.assertEquals(0, expr.getCursorPosition())

        for (i in 1..bigExpressionString.length) {
            Assert.assertTrue(expr.setCursorPosition(i))
            Assert.assertEquals(i, expr.getCursorPosition())
        }

        Assert.assertFalse(expr.setCursorPosition(bigExpressionString.length + 1))
        Assert.assertEquals(bigExpressionString.length, expr.getCursorPosition())
    }

    @Test
    fun testAddElementInDifferentPlaces() {
        val expr: Expression = createExpressionFromString("20")
        expr.addToken(Digit.NINE)
        Assert.assertEquals("209", expr.toString())

        expr.shiftCursorLeft()
        expr.addToken(Dot)
        Assert.assertEquals("20.9", expr.toString())

        expr.apply {
            shiftCursorLeft()
            shiftCursorLeft()
            shiftCursorLeft()
            addToken(Digit.ONE)
        }
        Assert.assertEquals("120.9", expr.toString())
    }

    @Test
    fun testDeleteElementInDifferentPlaces() {
        val expr: Expression = createExpressionFromString("1209")
        Assert.assertTrue(expr.deleteToken())
        Assert.assertEquals("120", expr.toString())

        expr.shiftCursorLeft()
        Assert.assertTrue(expr.deleteToken())
        Assert.assertEquals("10", expr.toString())

        expr.shiftCursorLeft()
        Assert.assertFalse(expr.deleteToken())
        Assert.assertEquals("10", expr.toString())
    }

    @Test
    fun testClear() {
        val expr: Expression = createExpressionFromString("12")
        Assert.assertTrue(expr.clear())
        Assert.assertTrue(expr.toString().isEmpty())
        Assert.assertFalse(expr.clear())
    }

    @Test
    fun testCalculateEmptyExpression() {
        Assert.assertEquals(0.0, createExpressionFromString("").calculate())
    }

    @Test
    fun testCalculateOneNumber() {
        Assert.assertEquals(0.0, createExpressionFromString("0").calculate())
        Assert.assertEquals(120.0, createExpressionFromString("120").calculate())
        Assert.assertEquals(120.9, createExpressionFromString("120.9").calculate())
    }

    @Test
    fun testCalculateOneUnaryOperation() {
        Assert.assertEquals(120.9, createExpressionFromString("+120.9").calculate())
        Assert.assertEquals(-120.9, createExpressionFromString("-120.9").calculate())
    }

    @Test
    fun testCalculateOneBinaryOperation() {
        Assert.assertEquals(8.0, createExpressionFromString("6+2").calculate())
        Assert.assertEquals(4.0, createExpressionFromString("6-2").calculate())
        Assert.assertEquals(12.0, createExpressionFromString("6${multiplySymbol}2").calculate())
        Assert.assertEquals(3.0, createExpressionFromString("6${divisionSymbol}2").calculate())
        Assert.assertEquals(36.0, createExpressionFromString("6${powerSymbol}2").calculate())
    }

    @Test
    fun testCalculateDivisionByZero() {
        Assert.assertEquals(
            FloatType.POSITIVE_INFINITY,
            createExpressionFromString("6${divisionSymbol}0.0").calculate()
        )
        Assert.assertEquals(
            FloatType.NEGATIVE_INFINITY,
            createExpressionFromString("-6${divisionSymbol}0.0").calculate()
        )
    }

    @Test
    fun testRightAssociationOfRightAssociatedBinaryOperation() {
        Assert.assertEquals(
            512.0,
            createExpressionFromString("2${powerSymbol}3${powerSymbol}2").calculate()
        )
    }

    @Test
    fun testPriorityUnaryAndRightAssociative() {
        Assert.assertEquals(-4.0, createExpressionFromString("-2${powerSymbol}2").calculate())
    }

    @Test
    fun testPriorityUnaryAndLeftAssociative() {
        Assert.assertEquals(0.0, createExpressionFromString("-2+2").calculate())
    }

    @Test
    fun testPriorityBetweenBinaryOperations() {
        Assert.assertEquals(
            65.0,
            createExpressionFromString("1+16${multiplySymbol}2${powerSymbol}2").calculate()
        )
        Assert.assertEquals(
            -3.0,
            createExpressionFromString("1-16${divisionSymbol}2${powerSymbol}2").calculate()
        )
    }

    @Test
    fun testBraces() {
        Assert.assertEquals(17.0, createExpressionFromString("(1+16)").calculate())
        Assert.assertEquals(-17.0, createExpressionFromString("-(1+16)").calculate())
        Assert.assertEquals(17.0, createExpressionFromString("(((1)+(16)))").calculate())
        Assert.assertEquals(
            34.0,
            createExpressionFromString("(1+16)${multiplySymbol}2").calculate()
        )
    }

    @Test
    fun testCalculateExpressionsWithError() {
        Assert.assertNull(createExpressionFromString("(").calculate())
        Assert.assertNull(createExpressionFromString(")").calculate())
        Assert.assertNull(createExpressionFromString("()").calculate())
        Assert.assertNull(createExpressionFromString("(-)").calculate())
        Assert.assertNull(createExpressionFromString("1(").calculate())
        Assert.assertNull(createExpressionFromString("1-").calculate())
        Assert.assertNull(createExpressionFromString("-1-").calculate())
        Assert.assertNull(createExpressionFromString("(-1)-").calculate())
        Assert.assertNull(createExpressionFromString("1++").calculate())
    }

    @Test
    fun testCalculateBigExpression() {
        val expr: Expression = createExpressionFromString(bigExpressionString)
        val res = expr.calculate()
        Assert.assertNotNull(res)
        Assert.assertEquals(-325.0, res)
    }
}
