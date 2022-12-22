package org.treeWare.json

import okio.BufferedSource
import okio.EOFException
import org.treeWare.util.TokenBuilder

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

// TODO(performance): would it be faster to use a buffered channel compared to the current approach of yielding after
//                    each token? The channel buffer size can be varied to tradeoff performance and memory consumption.

fun tokenizeJson(bufferedSource: BufferedSource): Sequence<JsonToken> = sequence {
    val tokenBuilder = TokenBuilder(bufferedSource)
    try {
        this.parseElement(tokenBuilder)
    } catch (_: EOFException) {
    }
}

// region Grammar functions (named and implemented as per grammar on the right at https://www.json.org/json-en.html)

// TODO(performance): use an approach that is faster than reading 1 character at a time.

private suspend fun SequenceScope<JsonToken>.parseValue(tokenBuilder: TokenBuilder) {
    when (tokenBuilder.peekUtf8CodePoint()) {
        '{'.code -> parseObject(tokenBuilder)
        '['.code -> parseArray(tokenBuilder)
        '"'.code -> parseString(tokenBuilder) { JsonToken.ValueString(it) }
        '-'.code -> parseNumber(tokenBuilder)
        in '0'.code..'9'.code -> parseNumber(tokenBuilder)
        't'.code -> parseTrue(tokenBuilder)
        'f'.code -> parseFalse(tokenBuilder)
        'n'.code -> parseNull(tokenBuilder)
        else -> {}
    }
}

private suspend fun SequenceScope<JsonToken>.parseObject(tokenBuilder: TokenBuilder) {
    tokenBuilder.discardPeekedUtf8CodePoint() // discard '{'
    yield(JsonToken.ObjectStart)
    skipWs(tokenBuilder)
    if (tokenBuilder.peekUtf8CodePoint() != '}'.code) parseMembers(tokenBuilder)
    tokenBuilder.expectCharacters("}")
    yield(JsonToken.ObjectEnd)
}

private suspend fun SequenceScope<JsonToken>.parseMembers(tokenBuilder: TokenBuilder) {
    while (true) {
        parseMember(tokenBuilder)
        if (tokenBuilder.peekUtf8CodePoint() != ','.code) break
        tokenBuilder.discardPeekedUtf8CodePoint() // discard ','
    }
}

private suspend fun SequenceScope<JsonToken>.parseMember(tokenBuilder: TokenBuilder) {
    skipWs(tokenBuilder)
    this.parseString(tokenBuilder) { JsonToken.KeyName(it) }
    skipWs(tokenBuilder)
    tokenBuilder.expectCharacters(":")
    parseElement(tokenBuilder)
}

private suspend fun SequenceScope<JsonToken>.parseArray(tokenBuilder: TokenBuilder) {
    tokenBuilder.discardPeekedUtf8CodePoint() // discard '['
    yield(JsonToken.ArrayStart)
    parseElements(tokenBuilder)
    tokenBuilder.expectCharacters("]")
    yield(JsonToken.ArrayEnd)
}

private suspend fun SequenceScope<JsonToken>.parseElements(tokenBuilder: TokenBuilder) {
    while (true) {
        parseElement(tokenBuilder)
        if (tokenBuilder.peekUtf8CodePoint() != ','.code) break
        tokenBuilder.discardPeekedUtf8CodePoint() // discard ','
    }
}

private suspend fun SequenceScope<JsonToken>.parseElement(tokenBuilder: TokenBuilder) {
    skipWs(tokenBuilder)
    this.parseValue(tokenBuilder)
    skipWs(tokenBuilder)
}

private suspend fun SequenceScope<JsonToken>.parseString(
    tokenBuilder: TokenBuilder,
    tokenFactory: (String) -> JsonToken
) {
    tokenBuilder.expectCharacters("\"")
    tokenBuilder.clearToken()
    parseCharacters(tokenBuilder)
    tokenBuilder.expectCharacters("\"")
    yield(tokenFactory(tokenBuilder.getToken()))
    tokenBuilder.clearToken()
}

private fun parseCharacters(tokenBuilder: TokenBuilder) {
    while (true) if (!parseCharacter(tokenBuilder)) break
}

private fun parseCharacter(tokenBuilder: TokenBuilder): Boolean = when (tokenBuilder.peekUtf8CodePoint()) {
    '"'.code -> false
    '\\'.code -> parseEscape(tokenBuilder)
    in 0x0020..0x10FFFF -> {
        tokenBuilder.appendUtf8CodePointToToken(tokenBuilder.readUtf8CodePoint())
        true
    }
    else -> false
}

private fun parseEscape(tokenBuilder: TokenBuilder): Boolean {
    tokenBuilder.discardPeekedUtf8CodePoint() // discard '\'
    when (val code = tokenBuilder.readUtf8CodePoint()) {
        '"'.code, '\\'.code, '/'.code -> tokenBuilder.appendUtf8CodePointToToken(code)
        'b'.code -> tokenBuilder.appendUtf8ToToken("\b")
        // "\f" is supported in Java but not in Kotlin: https://youtrack.jetbrains.com/issue/KT-8507
        'f'.code -> tokenBuilder.appendUtf8ToToken("\u000c")
        'n'.code -> tokenBuilder.appendUtf8ToToken("\n")
        'r'.code -> tokenBuilder.appendUtf8ToToken("\r")
        't'.code -> tokenBuilder.appendUtf8ToToken("\t")
        'u'.code -> {
            var codePoint = 0
            repeat(4) {
                val hex: Int = parseHex(tokenBuilder)
                codePoint = codePoint * 16 + hex
            }
            tokenBuilder.appendUtf8CodePointToToken(codePoint)
        }
    }
    return true
}

private fun parseHex(tokenBuilder: TokenBuilder): Int = when (val code = tokenBuilder.readUtf8CodePoint()) {
    in '0'.code..'9'.code -> code - '0'.code
    in 'A'.code..'F'.code -> code - 'A'.code + 10
    in 'a'.code..'f'.code -> code - 'a'.code + 10
    else -> tokenBuilder.throwException("Expected hex digit but found '${Char(code)}'")
}

private suspend fun SequenceScope<JsonToken>.parseNumber(tokenBuilder: TokenBuilder) {
    tokenBuilder.clearToken()
    parseInteger(tokenBuilder)
    parseFraction(tokenBuilder)
    parseExponent(tokenBuilder)
    yield(JsonToken.ValueNumber(tokenBuilder.getToken()))
    tokenBuilder.clearToken()
}

private fun parseInteger(tokenBuilder: TokenBuilder) {
    when (tokenBuilder.peekUtf8CodePoint()) {
        '-'.code -> tokenBuilder.appendUtf8CodePointToToken(tokenBuilder.readUtf8CodePoint())
        else -> {}
    }
    parseDigits(tokenBuilder)
}

private fun parseDigits(tokenBuilder: TokenBuilder) {
    val isDigit = parseDigit(tokenBuilder)
    if (!isDigit) tokenBuilder.throwException("Expected digit")
    while (true) if (!parseDigit(tokenBuilder)) break
}

private fun parseDigit(tokenBuilder: TokenBuilder): Boolean = when (tokenBuilder.peekUtf8CodePoint()) {
    in '0'.code..'9'.code -> {
        tokenBuilder.appendUtf8CodePointToToken(tokenBuilder.readUtf8CodePoint())
        true
    }
    else -> false
}

private fun parseFraction(tokenBuilder: TokenBuilder) {
    when (tokenBuilder.peekUtf8CodePoint()) {
        '.'.code -> {
            tokenBuilder.appendUtf8CodePointToToken(tokenBuilder.readUtf8CodePoint())
            parseDigits(tokenBuilder)
        }
        else -> {}
    }
}

private fun parseExponent(tokenBuilder: TokenBuilder) {
    when (tokenBuilder.peekUtf8CodePoint()) {
        'E'.code, 'e'.code -> {
            tokenBuilder.appendUtf8CodePointToToken(tokenBuilder.readUtf8CodePoint())
            parseSign(tokenBuilder)
            parseDigits(tokenBuilder)
        }
        else -> {}
    }
}

private fun parseSign(tokenBuilder: TokenBuilder) {
    when (tokenBuilder.peekUtf8CodePoint()) {
        '+'.code, '-'.code -> tokenBuilder.appendUtf8CodePointToToken(tokenBuilder.readUtf8CodePoint())
        else -> {}
    }
}

private suspend fun SequenceScope<JsonToken>.parseTrue(tokenBuilder: TokenBuilder) {
    tokenBuilder.expectCharacters("true")
    yield(JsonToken.ValueTrue)
}

private suspend fun SequenceScope<JsonToken>.parseFalse(tokenBuilder: TokenBuilder) {
    tokenBuilder.expectCharacters("false")
    yield(JsonToken.ValueFalse)
}

private suspend fun SequenceScope<JsonToken>.parseNull(tokenBuilder: TokenBuilder) {
    tokenBuilder.expectCharacters("null")
    yield(JsonToken.ValueNull)
}

private fun skipWs(tokenBuilder: TokenBuilder) {
    while (true) {
        when (tokenBuilder.peekUtf8CodePoint()) {
            0x0020, 0x000A, 0x000D, 0x0009 -> tokenBuilder.discardPeekedUtf8CodePoint()
            else -> break
        }
    }
}

// endregion