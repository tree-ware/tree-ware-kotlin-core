package org.treeWare.json

import okio.BufferedSource
import okio.EOFException
import org.treeWare.util.TokenBuilder

private enum class NestingState {
    OBJECT_START, ARRAY_START, STRING_START, KEY_NAME, COLON, OBJECT_VALUE, ARRAY_VALUE, COMMA
}

private class TokenizeState(bufferedSource: BufferedSource) {
    val tokenBuilder = TokenBuilder(bufferedSource)
    val nestingStack = ArrayDeque<NestingState>()
}

// TODO(performance): would it be faster to use a buffered channel compared to the current approach of yielding after
//                    each token? The channel buffer size can be varied to tradeoff performance and memory consumption.

fun tokenizeJson(bufferedSource: BufferedSource): Sequence<JsonToken> = sequence {
    val tokenizeState = TokenizeState(bufferedSource)
    try {
        this.parseElement(tokenizeState)
        // Throw an exception if EOF has not occurred
        if (tokenizeState.tokenBuilder.hasPeekedUtf8CodePoint()) tokenizeState.tokenBuilder.discardPeekedUtf8CodePoint()
        tokenizeState.tokenBuilder.throwException("Unexpected character")
    } catch (_: EOFException) {
        if (tokenizeState.nestingStack.isNotEmpty()) tokenizeState.tokenBuilder.throwException("Incomplete JSON")
    }
}

// region Grammar functions (named and implemented as per grammar on the right at https://www.json.org/json-en.html)

// TODO(performance): use an approach that is faster than reading 1 character at a time.

private suspend fun SequenceScope<JsonToken>.parseValue(tokenizeState: TokenizeState) {
    when (tokenizeState.tokenBuilder.peekUtf8CodePoint()) {
        '{'.code -> parseObject(tokenizeState)
        '['.code -> parseArray(tokenizeState)
        '"'.code -> parseString(tokenizeState) { JsonToken.ValueString(it) }
        '-'.code -> parseNumber(tokenizeState)
        in '0'.code..'9'.code -> parseNumber(tokenizeState)
        't'.code -> parseTrue(tokenizeState)
        'f'.code -> parseFalse(tokenizeState)
        'n'.code -> parseNull(tokenizeState)
        '}'.code -> handleClose(tokenizeState, NestingState.OBJECT_START, "Unexpected '}'")
        ']'.code -> handleClose(tokenizeState, NestingState.ARRAY_START, "Unexpected ']'")
        else -> {
            tokenizeState.tokenBuilder.discardPeekedUtf8CodePoint()
            tokenizeState.tokenBuilder.throwException("Unknown value type")
        }
    }
}

private fun handleClose(tokenizeState: TokenizeState, expectedNestingState: NestingState, error: String) =
    when (tokenizeState.nestingStack.firstOrNull()) {
        expectedNestingState -> {}
        NestingState.COMMA -> tokenizeState.tokenBuilder.throwException("Unsupported trailing comma")
        else -> {
            tokenizeState.tokenBuilder.discardPeekedUtf8CodePoint()
            tokenizeState.tokenBuilder.throwException(error)
        }
    }

private suspend fun SequenceScope<JsonToken>.parseObject(tokenizeState: TokenizeState) {
    tokenizeState.tokenBuilder.discardPeekedUtf8CodePoint() // discard '{'
    tokenizeState.nestingStack.addFirst(NestingState.OBJECT_START)
    yield(JsonToken.ObjectStart)
    skipWs(tokenizeState)
    when (tokenizeState.tokenBuilder.peekUtf8CodePoint()) {
        '}'.code -> {}
        ']'.code -> {
            tokenizeState.tokenBuilder.discardPeekedUtf8CodePoint()
            tokenizeState.tokenBuilder.throwException("Expected '}'")
        }
        else -> parseMembers(tokenizeState)
    }
    tokenizeState.tokenBuilder.expectCharacters("}")
    tokenizeState.nestingStack.removeFirst()
    yield(JsonToken.ObjectEnd)
}

private suspend fun SequenceScope<JsonToken>.parseMembers(tokenizeState: TokenizeState) {
    while (true) {
        parseMember(tokenizeState)
        if (tokenizeState.tokenBuilder.peekUtf8CodePoint() != ','.code) break
        tokenizeState.tokenBuilder.discardPeekedUtf8CodePoint() // discard ','
    }
}

private suspend fun SequenceScope<JsonToken>.parseMember(tokenizeState: TokenizeState) {
    skipWs(tokenizeState)
    this.parseString(tokenizeState) { JsonToken.KeyName(it) }
    skipWs(tokenizeState)
    tokenizeState.tokenBuilder.expectCharacters(":")
    parseElement(tokenizeState)
}

private suspend fun SequenceScope<JsonToken>.parseArray(tokenizeState: TokenizeState) {
    tokenizeState.tokenBuilder.discardPeekedUtf8CodePoint() // discard '['
    tokenizeState.nestingStack.addFirst(NestingState.ARRAY_START)
    yield(JsonToken.ArrayStart)
    parseElements(tokenizeState)
    tokenizeState.tokenBuilder.expectCharacters("]")
    tokenizeState.nestingStack.removeFirst()
    yield(JsonToken.ArrayEnd)
}

private suspend fun SequenceScope<JsonToken>.parseElements(tokenizeState: TokenizeState) {
    while (true) {
        parseElement(tokenizeState)
        // Remove comma from previous iteration of the loop
        if (tokenizeState.nestingStack.firstOrNull() == NestingState.COMMA) tokenizeState.nestingStack.removeFirst()
        if (tokenizeState.tokenBuilder.peekUtf8CodePoint() != ','.code) break
        tokenizeState.tokenBuilder.discardPeekedUtf8CodePoint() // discard ','
        tokenizeState.nestingStack.addFirst(NestingState.COMMA)
    }
}

private suspend fun SequenceScope<JsonToken>.parseElement(tokenizeState: TokenizeState) {
    skipWs(tokenizeState)
    this.parseValue(tokenizeState)
    skipWs(tokenizeState)
}

private suspend fun SequenceScope<JsonToken>.parseString(
    tokenizeState: TokenizeState,
    tokenFactory: (String) -> JsonToken
) {
    tokenizeState.tokenBuilder.expectCharacters("\"")
    tokenizeState.nestingStack.addFirst(NestingState.STRING_START)
    tokenizeState.tokenBuilder.clearToken()
    parseCharacters(tokenizeState)
    tokenizeState.tokenBuilder.expectCharacters("\"")
    tokenizeState.nestingStack.removeFirst()
    yield(tokenFactory(tokenizeState.tokenBuilder.getToken()))
    tokenizeState.tokenBuilder.clearToken()
}

private fun parseCharacters(tokenizeState: TokenizeState) {
    while (true) if (!parseCharacter(tokenizeState)) break
}

private fun parseCharacter(tokenizeState: TokenizeState): Boolean =
    when (tokenizeState.tokenBuilder.peekUtf8CodePoint()) {
        '"'.code -> false
        '\\'.code -> parseEscape(tokenizeState)
        in 0x0020..0x10FFFF -> {
            tokenizeState.tokenBuilder.appendUtf8CodePointToToken(tokenizeState.tokenBuilder.readUtf8CodePoint())
            true
        }
        else -> false
    }

private fun parseEscape(tokenizeState: TokenizeState): Boolean {
    tokenizeState.tokenBuilder.discardPeekedUtf8CodePoint() // discard '\'
    when (val code = tokenizeState.tokenBuilder.readUtf8CodePoint()) {
        '"'.code, '\\'.code, '/'.code -> tokenizeState.tokenBuilder.appendUtf8CodePointToToken(code)
        'b'.code -> tokenizeState.tokenBuilder.appendUtf8ToToken("\b")
        // "\f" is supported in Java but not in Kotlin: https://youtrack.jetbrains.com/issue/KT-8507
        'f'.code -> tokenizeState.tokenBuilder.appendUtf8ToToken("\u000c")
        'n'.code -> tokenizeState.tokenBuilder.appendUtf8ToToken("\n")
        'r'.code -> tokenizeState.tokenBuilder.appendUtf8ToToken("\r")
        't'.code -> tokenizeState.tokenBuilder.appendUtf8ToToken("\t")
        'u'.code -> {
            var codePoint = 0
            repeat(4) {
                val hex: Int = parseHex(tokenizeState)
                codePoint = codePoint * 16 + hex
            }
            tokenizeState.tokenBuilder.appendUtf8CodePointToToken(codePoint)
        }
    }
    return true
}

private fun parseHex(tokenizeState: TokenizeState): Int =
    when (val code = tokenizeState.tokenBuilder.readUtf8CodePoint()) {
        in '0'.code..'9'.code -> code - '0'.code
        in 'A'.code..'F'.code -> code - 'A'.code + 10
        in 'a'.code..'f'.code -> code - 'a'.code + 10
        else -> tokenizeState.tokenBuilder.throwException("Expected hex digit but found '${Char(code)}'")
    }

private suspend fun SequenceScope<JsonToken>.parseNumber(tokenizeState: TokenizeState) {
    tokenizeState.tokenBuilder.clearToken()
    parseInteger(tokenizeState)
    parseFraction(tokenizeState)
    parseExponent(tokenizeState)
    yield(JsonToken.ValueNumber(tokenizeState.tokenBuilder.getToken()))
    tokenizeState.tokenBuilder.clearToken()
}

private fun parseInteger(tokenizeState: TokenizeState) {
    when (tokenizeState.tokenBuilder.peekUtf8CodePoint()) {
        '-'.code -> tokenizeState.tokenBuilder.appendUtf8CodePointToToken(tokenizeState.tokenBuilder.readUtf8CodePoint())
        else -> {}
    }
    parseDigits(tokenizeState)
}

private fun parseDigits(tokenizeState: TokenizeState) {
    val isDigit = parseDigit(tokenizeState)
    if (!isDigit) {
        tokenizeState.tokenBuilder.discardPeekedUtf8CodePoint()
        tokenizeState.tokenBuilder.throwException("Expected digit")
    }
    while (true) if (!parseDigit(tokenizeState)) break
}

private fun parseDigit(tokenizeState: TokenizeState): Boolean = when (tokenizeState.tokenBuilder.peekUtf8CodePoint()) {
    in '0'.code..'9'.code -> {
        tokenizeState.tokenBuilder.appendUtf8CodePointToToken(tokenizeState.tokenBuilder.readUtf8CodePoint())
        true
    }
    else -> false
}

private fun parseFraction(tokenizeState: TokenizeState) {
    when (tokenizeState.tokenBuilder.peekUtf8CodePoint()) {
        '.'.code -> {
            tokenizeState.tokenBuilder.appendUtf8CodePointToToken(tokenizeState.tokenBuilder.readUtf8CodePoint())
            parseDigits(tokenizeState)
        }
        else -> {}
    }
}

private fun parseExponent(tokenizeState: TokenizeState) {
    when (tokenizeState.tokenBuilder.peekUtf8CodePoint()) {
        'E'.code, 'e'.code -> {
            tokenizeState.tokenBuilder.appendUtf8CodePointToToken(tokenizeState.tokenBuilder.readUtf8CodePoint())
            parseSign(tokenizeState)
            parseDigits(tokenizeState)
        }
        else -> {}
    }
}

private fun parseSign(tokenizeState: TokenizeState) {
    when (tokenizeState.tokenBuilder.peekUtf8CodePoint()) {
        '+'.code, '-'.code -> tokenizeState.tokenBuilder.appendUtf8CodePointToToken(tokenizeState.tokenBuilder.readUtf8CodePoint())
        else -> {}
    }
}

private suspend fun SequenceScope<JsonToken>.parseTrue(tokenizeState: TokenizeState) {
    tokenizeState.tokenBuilder.expectCharacters("true")
    yield(JsonToken.ValueTrue)
}

private suspend fun SequenceScope<JsonToken>.parseFalse(tokenizeState: TokenizeState) {
    tokenizeState.tokenBuilder.expectCharacters("false")
    yield(JsonToken.ValueFalse)
}

private suspend fun SequenceScope<JsonToken>.parseNull(tokenizeState: TokenizeState) {
    tokenizeState.tokenBuilder.expectCharacters("null")
    yield(JsonToken.ValueNull)
}

private fun skipWs(tokenizeState: TokenizeState) {
    while (true) {
        when (tokenizeState.tokenBuilder.peekUtf8CodePoint()) {
            0x0020, 0x000A, 0x000D, 0x0009 -> tokenizeState.tokenBuilder.discardPeekedUtf8CodePoint()
            else -> break
        }
    }
}

// endregion