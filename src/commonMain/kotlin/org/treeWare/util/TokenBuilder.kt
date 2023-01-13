package org.treeWare.util

import okio.Buffer
import okio.BufferedSource

interface TokenPosition {
    val line: Int
    val column: Int
    val charactersFromStart: Int

    fun isEqual(other: Any?): Boolean {
        val that = other as? TokenPosition ?: return false
        if (this.line != that.line) return false
        if (this.column != that.column) return false
        if (this.charactersFromStart != that.charactersFromStart) return false
        return true
    }

    fun asString(): String = "{line: $line, column: $column, charactersFromStart: $charactersFromStart}"
}

class ImmutableTokenPosition(
    override val line: Int, override val column: Int, override val charactersFromStart: Int
) : TokenPosition {
    override fun equals(other: Any?): Boolean = isEqual(other)
    override fun toString(): String = asString()
}

class MutableTokenPosition(
    override var line: Int, override var column: Int, override var charactersFromStart: Int
) : TokenPosition {
    override fun equals(other: Any?): Boolean = isEqual(other)
    override fun toString(): String = asString()
}

class TokenException(message: String, val position: TokenPosition) : Exception(message) {
    override fun toString() =
        "$message at line ${position.line} column ${position.column}, ${position.charactersFromStart} characters from the start"
}

class TokenBuilder(private val bufferedSource: BufferedSource) {
    private val tokenBuffer = Buffer()

    private var peekedUtf8CodePoint: Int? = null

    private val position = MutableTokenPosition(1, 0, 0)

    fun getToken(): String = tokenBuffer.readUtf8()
    fun getPosition(): TokenPosition = position

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
        throw TokenException(message, position)
    }

    private fun updatePosition(code: Int) {
        ++position.charactersFromStart
        if (code == '\n'.code) {
            ++position.line
            position.column = 0
        } else {
            position.column += if (code == '\t'.code) 4 else 1
        }
    }
}