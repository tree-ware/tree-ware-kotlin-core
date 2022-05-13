package org.treeWare.model.decoder

import org.treeWare.model.decoder.stateMachine.DelegatingStateMachine
import java.io.Reader
import javax.json.Json
import javax.json.stream.JsonParser

// TODO(deepak-nulu): move this into commonMain with an expect-ed enum and interface for the JSON parser
class JsonWireFormatDecoder : WireFormatDecoder {
    /**
     * @return an error message if there is an error.
     */
    override fun decode(reader: Reader, delegatingStateMachine: DelegatingStateMachine): String? {
        val parser: JsonParser = Json.createParser(reader)
        var success = true
        while (parser.hasNext() && success) {
            val event = parser.next()
            success = when (event) {
                JsonParser.Event.START_ARRAY -> delegatingStateMachine.decodeListStart()
                JsonParser.Event.START_OBJECT -> delegatingStateMachine.decodeObjectStart()
                JsonParser.Event.KEY_NAME -> delegatingStateMachine.decodeKey(parser.string)
                JsonParser.Event.VALUE_STRING -> delegatingStateMachine.decodeStringValue(parser.string)
                JsonParser.Event.VALUE_NUMBER -> delegatingStateMachine.decodeNumericValue(parser.string)
                JsonParser.Event.VALUE_TRUE -> delegatingStateMachine.decodeBooleanValue(true)
                JsonParser.Event.VALUE_FALSE -> delegatingStateMachine.decodeBooleanValue(false)
                JsonParser.Event.VALUE_NULL -> delegatingStateMachine.decodeNullValue()
                JsonParser.Event.END_OBJECT -> delegatingStateMachine.decodeObjectEnd()
                JsonParser.Event.END_ARRAY -> delegatingStateMachine.decodeListEnd()
            }
        }
        return if (success) null else "JSON decoding failed at: ${parser.location}"
    }
}