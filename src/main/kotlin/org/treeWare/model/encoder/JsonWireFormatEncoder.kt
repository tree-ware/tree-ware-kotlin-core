package org.treeWare.model.encoder

import java.io.Writer
import java.util.*

private enum class NestingState {
    OBJECT_START, OBJECT_ELEMENT,
    LIST_START, LIST_ELEMENT,
}

class JsonWireFormatEncoder(
    private val writer: Writer,
    prettyPrint: Boolean = false,
    indentSizeInSpaces: Int = 2
) : WireFormatEncoder {
    private val nesting = ArrayDeque<NestingState>()
    private val prettyPrinter =
        PrettyPrintHelper(prettyPrint, indentSizeInSpaces)

    private fun encodeNameValue(name: String?, value: String) {
        when (nesting.peekFirst()) {
            NestingState.OBJECT_START -> {
                writer.write(prettyPrinter.endOfLine)
                prettyPrinter.indent()
                writer.write(prettyPrinter.currentIndent)
                assert(name != null)
                writer.write("\"${name ?: ""}\": $value")
            }
            NestingState.OBJECT_ELEMENT -> {
                writer.write(",")
                writer.write(prettyPrinter.endOfLine)
                writer.write(prettyPrinter.currentIndent)
                assert(name != null)
                writer.write("\"${name ?: ""}\": $value")
            }
            NestingState.LIST_START -> {
                writer.write(prettyPrinter.endOfLine)
                prettyPrinter.indent()
                writer.write(prettyPrinter.currentIndent)
                writer.write(value)
            }
            NestingState.LIST_ELEMENT -> {
                writer.write(",")
                writer.write(prettyPrinter.endOfLine)
                writer.write(prettyPrinter.currentIndent)
                writer.write(value)
            }
            else -> {
                assert(prettyPrinter.currentIndent == "")
                writer.write(value)
            }
        }
    }

    private fun elementEncoded() {
        when (nesting.peekFirst()) {
            NestingState.OBJECT_START -> nesting.addFirst(
                NestingState.OBJECT_ELEMENT
            )
            NestingState.LIST_START -> nesting.addFirst(NestingState.LIST_ELEMENT)
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
        when (nesting.peekFirst()) {
            NestingState.OBJECT_START -> {
                writer.write("}")
                nesting.pollFirst()
                elementEncoded()
            }
            NestingState.OBJECT_ELEMENT -> {
                writer.write(prettyPrinter.endOfLine)
                prettyPrinter.unindent()
                writer.write(prettyPrinter.currentIndent)
                writer.write("}")
                nesting.pollFirst() // remove OBJECT_ELEMENT
                assert(nesting.peekFirst() == NestingState.OBJECT_START)
                nesting.pollFirst() // remove OBJECT_START
                elementEncoded()
            }
            NestingState.LIST_START -> assert(false) { "End of JSON object instead of end of JSON list" }
            NestingState.LIST_ELEMENT -> assert(false) { "End of JSON object instead of end of JSON list" }
            else -> assert(false) { "End of JSON object without start of JSON object" }
        }
    }

    override fun encodeListStart(name: String?) {
        encodeNameValue(name, "[")
        nesting.addFirst(NestingState.LIST_START)
    }

    override fun encodeListEnd() {
        when (nesting.peekFirst()) {
            NestingState.OBJECT_START -> assert(false) { "End of JSON list instead of end of JSON object" }
            NestingState.OBJECT_ELEMENT -> assert(false) { "End of JSON list instead of end of JSON object" }
            NestingState.LIST_START -> {
                writer.write("]")
                nesting.pollFirst()
                elementEncoded()
            }
            NestingState.LIST_ELEMENT -> {
                writer.write(prettyPrinter.endOfLine)
                prettyPrinter.unindent()
                writer.write(prettyPrinter.currentIndent)
                writer.write("]")
                nesting.pollFirst() // remove LIST_ELEMENT
                assert(nesting.peekFirst() == NestingState.LIST_START)
                nesting.pollFirst() // remove LIST_START
                elementEncoded()
            }
            else -> assert(false) { "End of JSON list without start of JSON list" }
        }
    }

    override fun encodeNullField(name: String) {
        encodeNameValue(name, "null")
        elementEncoded()
    }

    override fun encodeStringField(name: String, value: String) {
        encodeNameValue(name, "\"$value\"")
        elementEncoded()
    }

    override fun <T : Number> encodeNumericField(name: String, value: T) {
        encodeNameValue(name, value.toString())
        elementEncoded()
    }

    override fun encodeBooleanField(name: String, value: Boolean) {
        encodeNameValue(name, if (value) "true" else "false")
        elementEncoded()
    }
}
