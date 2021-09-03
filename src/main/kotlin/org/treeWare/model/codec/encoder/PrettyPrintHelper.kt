package org.treeWare.model.codec.encoder

class PrettyPrintHelper(private val prettyPrint: Boolean, private val indentSizeInSpaces: Int = 2) {
    var currentIndent: String = ""
        private set
    var endOfLine: String = if (prettyPrint) "\n" else ""
        private set

    fun indent() {
        if (prettyPrint) {
            val newIndent = currentIndent.length + indentSizeInSpaces
            currentIndent = " ".repeat(newIndent)
        }
    }

    fun unindent() {
        if (prettyPrint) {
            val newIndent = currentIndent.length - indentSizeInSpaces
            currentIndent = " ".repeat(newIndent)
        }
    }
}
