package com.df530.calculator.core

typealias FloatType = Double;

interface Expression {
    /** Returns true if cursor position was changed, false otherwise */
    fun shiftCursorLeft(): Boolean

    /** Returns true if cursor position was changed, false otherwise */
    fun shiftCursorRight(): Boolean

    /** Returns the position of the cursor in tokens list */
    fun getCursorPosition(): Int

    /** Set position of cursor. If [newPosition] more than length of expression, set
     *  cursor at the end of expression. If [newPosition] is negative -- set cursor at the
     *  start of expression
     *  Returns true if cursor position was changed, false otherwise
     */
    fun setCursorPosition(newPosition: Int): Boolean

    /** Adds [inputToken] on cursor position */
    fun addToken(inputToken: InputToken)

    /** Delete token from cursor position. Not change expression,
     *  if cursor is at the start of expression
     *  Returns true if expression was changed, false otherwise
     */
    fun deleteToken(): Boolean

    /** Returns true if expression was changed (when it hadn't been empty), false otherwise */
    fun clear(): Boolean

    /** Returns null, when expression is incorrect, 0 if it is empty and calculation result otherwise */
    fun calculate(): FloatType?

    /** Returns if expression is empty */
    fun isEmpty(): Boolean

    /** Returns the joining of expression tokens. Token's string are got from [InputToken.toString]. */
    override fun toString(): String
}