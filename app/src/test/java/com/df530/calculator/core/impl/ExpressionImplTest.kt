package com.df530.calculator.core.impl

import com.df530.calculator.core.Expression
import com.df530.calculator.core.ExpressionTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExpressionImplTest : ExpressionTest() {
    override fun createExpression(): Expression {
        return ExpressionImpl()
    }

    @Test
    fun testCalculateNumberWithStartingZeros() {
        assertEquals(1.0, createExpressionFromString("0001").calculate())
    }

    @Test
    fun testFewDotsBetweenDigit() {
        assertNull(createExpressionFromString("1.0.0").calculate())
    }
}