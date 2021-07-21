package org.treeWare.common.codec

import org.apache.logging.log4j.LogManager
import java.io.Reader
import javax.json.Json
import javax.json.stream.JsonParser

class JsonWireFormatDecoder : WireFormatDecoder {
    override fun decode(reader: Reader, decodingStateMachine: DecodingStateMachine): Boolean {
        val parser: JsonParser = Json.createParser(reader)
        var success = true
        while (parser.hasNext() && success) {
            val event = parser.next()
            success = when (event) {
                JsonParser.Event.START_ARRAY -> decodingStateMachine.decodeListStart()
                JsonParser.Event.START_OBJECT -> decodingStateMachine.decodeObjectStart()
                JsonParser.Event.KEY_NAME -> decodingStateMachine.decodeKey(parser.string)
                JsonParser.Event.VALUE_STRING -> decodingStateMachine.decodeStringValue(parser.string)
                JsonParser.Event.VALUE_NUMBER -> decodingStateMachine.decodeNumericValue(parser.bigDecimal)
                JsonParser.Event.VALUE_TRUE -> decodingStateMachine.decodeBooleanValue(true)
                JsonParser.Event.VALUE_FALSE -> decodingStateMachine.decodeBooleanValue(false)
                JsonParser.Event.VALUE_NULL -> decodingStateMachine.decodeNullValue()
                JsonParser.Event.END_OBJECT -> decodingStateMachine.decodeObjectEnd()
                JsonParser.Event.END_ARRAY -> decodingStateMachine.decodeListEnd()
            }
        }
        if (!success) logger.debug("JSON decoding failed at: ${parser.location}")
        return success
    }

    private val logger = LogManager.getLogger()
}
