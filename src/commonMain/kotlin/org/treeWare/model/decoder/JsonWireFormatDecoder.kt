package org.treeWare.model.decoder

import okio.BufferedSource
import org.treeWare.json.JsonToken
import org.treeWare.json.JsonTokenType
import org.treeWare.json.tokenizeJson
import org.treeWare.model.decoder.stateMachine.DelegatingStateMachine
import org.treeWare.util.TokenException

class JsonWireFormatDecoder : WireFormatDecoder {
    /**
     * @return an error message if there is an error.
     */
    override fun decode(bufferedSource: BufferedSource, delegatingStateMachine: DelegatingStateMachine): String? = try {
        tokenizeJson(bufferedSource).forEach { jsonToken: JsonToken ->
            val success = when (jsonToken.jsonTokenType) {
                JsonTokenType.OBJECT_START -> delegatingStateMachine.decodeObjectStart()
                JsonTokenType.OBJECT_END -> delegatingStateMachine.decodeObjectEnd()
                JsonTokenType.ARRAY_START -> delegatingStateMachine.decodeListStart()
                JsonTokenType.ARRAY_END -> delegatingStateMachine.decodeListEnd()
                JsonTokenType.KEY_NAME -> delegatingStateMachine.decodeKey(jsonToken.value)
                JsonTokenType.VALUE_STRING -> delegatingStateMachine.decodeStringValue(jsonToken.value)
                JsonTokenType.VALUE_NUMBER -> delegatingStateMachine.decodeNumericValue(jsonToken.value)
                JsonTokenType.VALUE_TRUE -> delegatingStateMachine.decodeBooleanValue(true)
                JsonTokenType.VALUE_FALSE -> delegatingStateMachine.decodeBooleanValue(false)
                JsonTokenType.VALUE_NULL -> delegatingStateMachine.decodeNullValue()
            }
            if (!success) throw TokenException("JSON decoding failed", jsonToken.position)
        }
        null
    } catch (e: TokenException) {
        e.toString()
    }
}