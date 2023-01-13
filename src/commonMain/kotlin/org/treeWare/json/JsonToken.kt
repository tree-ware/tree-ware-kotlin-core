package org.treeWare.json

import org.treeWare.util.ImmutableTokenPosition
import org.treeWare.util.MutableTokenPosition
import org.treeWare.util.TokenPosition

enum class JsonTokenType {
    OBJECT_START,
    OBJECT_END,
    ARRAY_START,
    ARRAY_END,
    KEY_NAME,
    VALUE_STRING,
    VALUE_NUMBER,
    VALUE_TRUE,
    VALUE_FALSE,
    VALUE_NULL
}

interface JsonToken {
    val jsonTokenType: JsonTokenType
    val value: String
    val position: TokenPosition

    fun isEqual(other: Any?): Boolean {
        val that = other as? JsonToken ?: return false
        if (this.jsonTokenType != that.jsonTokenType) return false
        if (this.value != that.value) return false
        if (this.position != that.position) return false
        return true
    }

    fun asString(): String = "{jsonTokenType: $jsonTokenType, value: $value, position: $position}"
}

class ImmutableJsonToken(
    override val jsonTokenType: JsonTokenType,
    override val value: String,
    line: Int,
    column: Int,
    charactersFromStart: Int
) : JsonToken {
    override val position: TokenPosition = ImmutableTokenPosition(line, column, charactersFromStart)
    override fun equals(other: Any?): Boolean = isEqual(other)
    override fun toString(): String = asString()
}

class MutableJsonToken(
    override var jsonTokenType: JsonTokenType,
    override var value: String,
    override var position: TokenPosition
) : JsonToken {
    override fun equals(other: Any?): Boolean = isEqual(other)
    override fun toString(): String = asString()
}