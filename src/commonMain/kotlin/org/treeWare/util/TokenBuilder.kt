package org.treeWare.util

import okio.Buffer
import okio.BufferedSource

class TokenException(message: String, val line: Int, val column: Int, val offset: Int) : Exception(message) {
    override fun toString(): String = "$message at line $line column $column offset $offset"
}

class TokenBuilder(private val bufferedSource: BufferedSource) {
    private val tokenBuffer = Buffer()

    private var peekedUtf8CodePoint: Int? = null
    // TODO(deepak-nulu) update following position variables
    private var line = 0
    private var column = 0
    private var offset = 0

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

    fun discardPeekedUtf8CodePoint() {
        if (peekedUtf8CodePoint == null) throw IllegalStateException("No peeked character to discard")
        peekedUtf8CodePoint = null
    }

    fun readUtf8CodePoint(): Int {
        val existing = peekedUtf8CodePoint
        if (existing != null) {
            peekedUtf8CodePoint = null
            return existing
        }
        return bufferedSource.readUtf8CodePoint()
    }

    fun expectCharacters(characters: String) {
        val asExpected = characters.all { it.code == readUtf8CodePoint() }
        if (!asExpected) throwException("Expected '$characters'")
    }

    fun throwException(message: String): Nothing {
        throw TokenException(message, line, column, offset)
    }
}