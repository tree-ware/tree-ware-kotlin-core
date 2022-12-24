package org.treeWare.json

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