package org.treeWare.util

import okio.Buffer
import okio.BufferedSource

class TokenException(
    message: String, val line: Int, val column: Int, val charactersFromStart: Int
) : Exception(message) {
    override fun toString() = "$message at line $line column $column, $charactersFromStart characters from the start"
}

class TokenBuilder(private val bufferedSource: BufferedSource) {
    private val tokenBuffer = Buffer()

    private var peekedUtf8CodePoint: Int? = null

    // Position
    private var line = 1
    private var column = 0
    private var charactersFromStart = 0

    fun getToken(): String = tokenBuffer.readUtf8()

    fun appendUtf8CodePointToToken(code: Int) {
        tokenBuffer.writeUtf8CodePoint(code)
    }

    fun appendUtf8ToToken(string: String) {
        tokenBuffer.writeUtf8(string)
    }

    fun clearToken() {
        tokenBuffer.clear()
    }

    fun peekUtf8CodePoint(): Int {
        val existing = peekedUtf8CodePoint
        if (existing != null) return existing
        val next = bufferedSource.readUtf8CodePoint()
        peekedUtf8CodePoint = next
        return next
    }

    fun hasPeekedUtf8CodePoint(): Boolean = peekedUtf8CodePoint != null

    fun discardPeekedUtf8CodePoint() {
        val code = peekedUtf8CodePoint ?: throw IllegalStateException("No peeked character to discard")
        updatePosition(code)
        peekedUtf8CodePoint = null
    }

    fun readUtf8CodePoint(): Int {
        val existing = peekedUtf8CodePoint
        val code = if (existing != null) {
            peekedUtf8CodePoint = null
            existing
        } else bufferedSource.readUtf8CodePoint()
        updatePosition(code)
        return code
    }

    fun expectCharacters(characters: String) {
        val asExpected = characters.all { it.code == readUtf8CodePoint() }
        if (!asExpected) throwException("Expected '$characters'")
    }

    fun throwException(message: String): Nothing {
        throw TokenException(message, line, column, charactersFromStart)
    }

    private fun updatePosition(code: Int) {
        ++charactersFromStart
        if (code == '\n'.code) {
            ++line
            column = 0
        } else {
            column += if (code == '\t'.code) 4 else 1
        }
    }
}