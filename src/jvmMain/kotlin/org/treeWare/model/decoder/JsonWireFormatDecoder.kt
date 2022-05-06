package org.treeWare.model.decoder

import org.treeWare.model.decoder.stateMachine.DecodingStateMachine
import java.io.Reader
import javax.json.Json
import javax.json.stream.JsonParser

// TODO(deepak-nulu): move this into commonMain with an expect-ed enum and interface for the JSON parser
class JsonWireFormatDecoder : WireFormatDecoder {
    /**
     * @return an error message if there is an error.
     */
    override fun decode(reader: Reader, decodingStateMachine: DecodingStateMachine): String? {
        val parser: JsonParser = Json.createParser(reader)
        var success = true
        while (parser.hasNext() && success) {
            val event = parser.next()
            success = when (event) {
                JsonParser.Event.START_ARRAY -> decodingStateMachine.decodeListStart()
                JsonParser.Event.START_OBJECT -> decodingStateMachine.decodeObjectStart()
                JsonParser.Event.KEY_NAME -> decodingStateMachine.decodeKey(parser.string)
                JsonParser.Event.VALUE_STRING -> decodingStateMachine.decodeStringValue(parser.string)
                JsonParser.Event.VALUE_NUMBER -> decodingStateMachine.decodeNumericValue(parser.string)
                JsonParser.Event.VALUE_TRUE -> decodingStateMachine.decodeBooleanValue(true)
                JsonParser.Event.VALUE_FALSE -> decodingStateMachine.decodeBooleanValue(false)
                JsonParser.Event.VALUE_NULL -> decodingStateMachine.decodeNullValue()
                JsonParser.Event.END_OBJECT -> decodingStateMachine.decodeObjectEnd()
                JsonParser.Event.END_ARRAY -> decodingStateMachine.decodeListEnd()
            }
        }
        return if (success) null else "JSON decoding failed at: ${parser.location}"
    }
}