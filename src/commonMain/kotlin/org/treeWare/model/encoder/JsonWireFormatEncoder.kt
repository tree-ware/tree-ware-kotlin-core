package org.treeWare.model.encoder

import okio.BufferedSink
import org.treeWare.util.assertInDevMode

private enum class NestingState {
    OBJECT_START, OBJECT_ELEMENT,
    LIST_START, LIST_ELEMENT,
}

class JsonWireFormatEncoder(
    private val bufferedSink: BufferedSink,
    private val prettyPrint: Boolean = false,
    indentSizeInSpaces: Int = 2
) : WireFormatEncoder {
    private val nesting = ArrayDeque<NestingState>()
    private val prettyPrinter = PrettyPrintHelper(prettyPrint, indentSizeInSpaces)

    private fun encodeNameValue(name: String?, value: String) {
        when (nesting.firstOrNull()) {
            NestingState.OBJECT_START -> {
                bufferedSink.writeUtf8(prettyPrinter.endOfLine)
                prettyPrinter.indent()
                bufferedSink.writeUtf8(prettyPrinter.currentIndent)
                assertInDevMode(name != null)
                bufferedSink.writeUtf8("\"${name ?: ""}\":")
                if (prettyPrint) bufferedSink.writeUtf8(" ")
                bufferedSink.writeUtf8(value)
            }
            NestingState.OBJECT_ELEMENT -> {
                bufferedSink.writeUtf8(",")
                bufferedSink.writeUtf8(prettyPrinter.endOfLine)
                bufferedSink.writeUtf8(prettyPrinter.currentIndent)
                assertInDevMode(name != null)
                bufferedSink.writeUtf8("\"${name ?: ""}\":")
                if (prettyPrint) bufferedSink.writeUtf8(" ")
                bufferedSink.writeUtf8(value)
            }
            NestingState.LIST_START -> {
                bufferedSink.writeUtf8(prettyPrinter.endOfLine)
                prettyPrinter.indent()
                bufferedSink.writeUtf8(prettyPrinter.currentIndent)
                bufferedSink.writeUtf8(value)
            }
            NestingState.LIST_ELEMENT -> {
                bufferedSink.writeUtf8(",")
                bufferedSink.writeUtf8(prettyPrinter.endOfLine)
                bufferedSink.writeUtf8(prettyPrinter.currentIndent)
                bufferedSink.writeUtf8(value)
            }
            else -> {
                assertInDevMode(prettyPrinter.currentIndent == "")
                bufferedSink.writeUtf8(value)
            }
        }
    }

    private fun elementEncoded() {
        when (nesting.firstOrNull()) {
            NestingState.OBJECT_START -> nesting.addFirst(
                NestingState.OBJECT_ELEMENT
            )
            NestingState.LIST_START -> nesting.addFirst(NestingState.LIST_ELEMENT)
            else -> {}
        }
    }

    // WireFormatEncoder methods

    override fun getAuxFieldName(fieldName: String?, auxName: String): String =
        if (fieldName != null) "${fieldName}__${auxName}_" else "${auxName}_"

    override fun encodeObjectStart(name: String?) {
        encodeNameValue(name, "{")
        nesting.addFirst(NestingState.OBJECT_START)
    }

    override fun encodeObjectEnd() {
        when (nesting.firstOrNull()) {
            NestingState.OBJECT_START -> {
                bufferedSink.writeUtf8("}")
                nesting.removeFirst()
                elementEncoded()
            }
            NestingState.OBJECT_ELEMENT -> {
                bufferedSink.writeUtf8(prettyPrinter.endOfLine)
                prettyPrinter.unindent()
                bufferedSink.writeUtf8(prettyPrinter.currentIndent)
                bufferedSink.writeUtf8("}")
                nesting.removeFirst() // remove OBJECT_ELEMENT
                assertInDevMode(nesting.firstOrNull() == NestingState.OBJECT_START)
                nesting.removeFirst() // remove OBJECT_START
                elementEncoded()
            }
            NestingState.LIST_START -> assertInDevMode(false) { "End of JSON object instead of end of JSON list" }
            NestingState.LIST_ELEMENT -> assertInDevMode(false) { "End of JSON object instead of end of JSON list" }
            else -> assertInDevMode(false) { "End of JSON object without start of JSON object" }
        }
    }

    override fun encodeListStart(name: String?) {
        encodeNameValue(name, "[")
        nesting.addFirst(NestingState.LIST_START)
    }

    override fun encodeListEnd() {
        when (nesting.firstOrNull()) {
            NestingState.OBJECT_START -> assertInDevMode(false) { "End of JSON list instead of end of JSON object" }
            NestingState.OBJECT_ELEMENT -> assertInDevMode(false) { "End of JSON list instead of end of JSON object" }
            NestingState.LIST_START -> {
                bufferedSink.writeUtf8("]")
                nesting.removeFirst()
                elementEncoded()
            }
            NestingState.LIST_ELEMENT -> {
                bufferedSink.writeUtf8(prettyPrinter.endOfLine)
                prettyPrinter.unindent()
                bufferedSink.writeUtf8(prettyPrinter.currentIndent)
                bufferedSink.writeUtf8("]")
                nesting.removeFirst() // remove LIST_ELEMENT
                assertInDevMode(nesting.first() == NestingState.LIST_START)
                nesting.removeFirst() // remove LIST_START
                elementEncoded()
            }
            else -> assertInDevMode(false) { "End of JSON list without start of JSON list" }
        }
    }

    override fun encodeNullField(name: String) {
        encodeNameValue(name, "null")
        elementEncoded()
    }

    override fun encodeStringField(name: String, value: String) {
        encodeNameValue(name, "\"${escape(value)}\"")
        elementEncoded()
    }

    override fun <T> encodeNumericField(name: String, value: T) {
        encodeNameValue(name, value.toString())
        elementEncoded()
    }

    override fun encodeBooleanField(name: String, value: Boolean) {
        encodeNameValue(name, if (value) "true" else "false")
        elementEncoded()
    }
}

private fun escape(value: String): String {
    val builder = StringBuilder()
    value.forEach { character ->
        when (character) {
            '"', '\\' -> builder.append('\\').append(character)
            '\b' -> builder.append("\\b")
            '\u000C' -> builder.append("\\f")
            '\n' -> builder.append("\\n")
            '\r' -> builder.append("\\r")
            '\t' -> builder.append("\\t")
            else -> builder.append(character)
        }
    }
    return builder.toString()
}