package org.treeWare.json

import okio.Buffer
import okio.BufferedSource
import okio.EOFException

sealed interface JsonToken {
    object ObjectStart : JsonToken
    object ObjectEnd : JsonToken
    object ArrayStart : JsonToken
    object ArrayEnd : JsonToken
    data class KeyName(val name: String) : JsonToken
    data class ValueString(val value: String) : JsonToken
    data class ValueNumber(val value: String) : JsonToken
    object ValueTrue : JsonToken
    object ValueFalse : JsonToken
    object ValueNull : JsonToken
}

// TODO(deepak-nulu) include position of lexer
class InvalidJsonException(message: String) : Exception(message)

// TODO(performance): would it be faster to use a buffered channel compared to the current approach of yielding after
//                    each token? The channel buffer size can be varied to tradeoff performance and memory consumption.

fun tokenizeJson(bufferedSource: BufferedSource): Sequence<JsonToken> = sequence {
    val state = JsonState(bufferedSource)
    try {
        this.parseElement(state)
    } catch (_: EOFException) {
    }
}

private class JsonState(val bufferedSource: BufferedSource) {
    var peekedUtf8CodePoint: Int? = null
    val tokenBuffer = Buffer()
}

// region Grammar functions (named and implemented as per grammar on the right at https://www.json.org/json-en.html)

// TODO(performance): use an approach that is faster than reading 1 character at a time.

private suspend fun SequenceScope<JsonToken>.parseValue(state: JsonState) {
    when (peekUtf8CodePoint(state)) {
        '{'.code -> parseObject(state)
        '['.code -> parseArray(state)
        '"'.code -> parseString(state) { JsonToken.ValueString(it) }
        '-'.code -> parseNumber(state)
        in '0'.code..'9'.code -> parseNumber(state)
        't'.code -> parseTrue(state)
        'f'.code -> parseFalse(state)
        'n'.code -> parseNull(state)
        else -> {}
    }
}

private suspend fun SequenceScope<JsonToken>.parseObject(state: JsonState) {
    discardPeekedUtf8CodePoint(state) // discard '{'
    yield(JsonToken.ObjectStart)
    skipWs(state)
    if (peekUtf8CodePoint(state) != '}'.code) parseMembers(state)
    expectCharacters(state, "}")
    yield(JsonToken.ObjectEnd)
}

private suspend fun SequenceScope<JsonToken>.parseMembers(state: JsonState) {
    while (true) {
        parseMember(state)
        if (peekUtf8CodePoint(state) != ','.code) break
        discardPeekedUtf8CodePoint(state) // discard ','
    }
}

private suspend fun SequenceScope<JsonToken>.parseMember(state: JsonState) {
    skipWs(state)
    this.parseString(state) { JsonToken.KeyName(it) }
    skipWs(state)
    expectCharacters(state, ":")
    parseElement(state)
}

private suspend fun SequenceScope<JsonToken>.parseArray(state: JsonState) {
    discardPeekedUtf8CodePoint(state) // discard '['
    yield(JsonToken.ArrayStart)
    parseElements(state)
    expectCharacters(state, "]")
    yield(JsonToken.ArrayEnd)
}

private suspend fun SequenceScope<JsonToken>.parseElements(state: JsonState) {
    while (true) {
        parseElement(state)
        if (peekUtf8CodePoint(state) != ','.code) break
        discardPeekedUtf8CodePoint(state) // discard ','
    }
}

private suspend fun SequenceScope<JsonToken>.parseElement(state: JsonState) {
    skipWs(state)
    this.parseValue(state)
    skipWs(state)
}

private suspend fun SequenceScope<JsonToken>.parseString(state: JsonState, tokenFactory: (String) -> JsonToken) {
    expectCharacters(state, "\"")
    state.tokenBuffer.clear()
    parseCharacters(state)
    expectCharacters(state, "\"")
    yield(tokenFactory(state.tokenBuffer.readUtf8()))
    state.tokenBuffer.clear()
}

private fun parseCharacters(state: JsonState) {
    while (true) if (!parseCharacter(state)) break
}

private fun parseCharacter(state: JsonState): Boolean = when (peekUtf8CodePoint(state)) {
    '"'.code -> false
    '\\'.code -> parseEscape(state)
    in 0x0020..0x10FFFF -> {
        state.tokenBuffer.writeUtf8CodePoint(readUtf8CodePoint(state))
        true
    }
    else -> false
}

private fun parseEscape(state: JsonState): Boolean {
    discardPeekedUtf8CodePoint(state) // discard '\'
    when (val code = readUtf8CodePoint(state)) {
        '"'.code, '\\'.code, '/'.code -> state.tokenBuffer.writeUtf8CodePoint(code)
        'b'.code -> state.tokenBuffer.writeUtf8("\b")
        // "\f" is supported in Java but not in Kotlin: https://youtrack.jetbrains.com/issue/KT-8507
        'f'.code -> state.tokenBuffer.writeUtf8("\u000c")
        'n'.code -> state.tokenBuffer.writeUtf8("\n")
        'r'.code -> state.tokenBuffer.writeUtf8("\r")
        't'.code -> state.tokenBuffer.writeUtf8("\t")
        'u'.code -> {
            var codePoint = 0
            repeat(4) {
                val hex: Int = parseHex(state)
                codePoint = codePoint * 16 + hex
            }
            state.tokenBuffer.writeUtf8CodePoint(codePoint)
        }
    }
    return true
}

private fun parseHex(state: JsonState): Int = when (val code = readUtf8CodePoint(state)) {
    in '0'.code..'9'.code -> code - '0'.code
    in 'A'.code..'F'.code -> code - 'A'.code + 10
    in 'a'.code..'f'.code -> code - 'a'.code + 10
    else -> throw InvalidJsonException("Expected hex digit but found '${Char(code)}'")
}

private suspend fun SequenceScope<JsonToken>.parseNumber(state: JsonState) {
    state.tokenBuffer.clear()
    parseInteger(state)
    parseFraction(state)
    parseExponent(state)
    yield(JsonToken.ValueNumber(state.tokenBuffer.readUtf8()))
    state.tokenBuffer.clear()
}

private fun parseInteger(state: JsonState) {
    when (peekUtf8CodePoint(state)) {
        '-'.code -> state.tokenBuffer.writeUtf8CodePoint(readUtf8CodePoint(state))
        else -> {}
    }
    parseDigits(state)
}

private fun parseDigits(state: JsonState) {
    val isDigit = parseDigit(state)
    if (!isDigit) throw InvalidJsonException("Expected digit")
    while (true) if (!parseDigit(state)) break
}

private fun parseDigit(state: JsonState): Boolean = when (peekUtf8CodePoint(state)) {
    in '0'.code..'9'.code -> {
        state.tokenBuffer.writeUtf8CodePoint(readUtf8CodePoint(state))
        true
    }
    else -> false
}

private fun parseFraction(state: JsonState) {
    when (peekUtf8CodePoint(state)) {
        '.'.code -> {
            state.tokenBuffer.writeUtf8CodePoint(readUtf8CodePoint(state))
            parseDigits(state)
        }
        else -> {}
    }
}

private fun parseExponent(state: JsonState) {
    when (peekUtf8CodePoint(state)) {
        'E'.code, 'e'.code -> {
            state.tokenBuffer.writeUtf8CodePoint(readUtf8CodePoint(state))
            parseSign(state)
            parseDigits(state)
        }
        else -> {}
    }
}

private fun parseSign(state: JsonState) {
    when (peekUtf8CodePoint(state)) {
        '+'.code, '-'.code -> state.tokenBuffer.writeUtf8CodePoint(readUtf8CodePoint(state))
        else -> {}
    }
}

private suspend fun SequenceScope<JsonToken>.parseTrue(state: JsonState) {
    expectCharacters(state, "true")
    yield(JsonToken.ValueTrue)
}

private suspend fun SequenceScope<JsonToken>.parseFalse(state: JsonState) {
    expectCharacters(state, "false")
    yield(JsonToken.ValueFalse)
}

private suspend fun SequenceScope<JsonToken>.parseNull(state: JsonState) {
    expectCharacters(state, "null")
    yield(JsonToken.ValueNull)
}

private fun skipWs(state: JsonState) {
    while (true) {
        when (peekUtf8CodePoint(state)) {
            0x0020, 0x000A, 0x000D, 0x0009 -> discardPeekedUtf8CodePoint(state)
            else -> break
        }
    }
}

// endregion

// region Helper functions

private fun peekUtf8CodePoint(state: JsonState): Int {
    val existing = state.peekedUtf8CodePoint
    if (existing != null) return existing
    val next = state.bufferedSource.readUtf8CodePoint()
    state.peekedUtf8CodePoint = next
    return next
}

private fun discardPeekedUtf8CodePoint(state: JsonState) {
    if (state.peekedUtf8CodePoint == null) throw IllegalStateException("No peeked character to discard")
    state.peekedUtf8CodePoint = null
}

private fun readUtf8CodePoint(state: JsonState): Int {
    val existing = state.peekedUtf8CodePoint
    if (existing != null) {
        state.peekedUtf8CodePoint = null
        return existing
    }
    return state.bufferedSource.readUtf8CodePoint()
}

private fun expectCharacters(state: JsonState, characters: String) {
    val asExpected = characters.all { it.code == readUtf8CodePoint(state) }
    if (!asExpected) throw InvalidJsonException("Expected '$characters'")
}

// endregion