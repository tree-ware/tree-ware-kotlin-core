package org.tree_ware.core.codec.dot

import org.tree_ware.core.codec.common.PrettyPrintHelper
import org.tree_ware.core.schema.*
import org.tree_ware.core.schema.visitors.AbstractSchemaVisitor
import java.io.Writer
import java.util.*

typealias EndHandler = () -> Unit

class DotEncodingVisitor(
    private val graphWriter: Writer,
    private val nodesWriter: Writer,
    private val linksWriter: Writer
) : BracketedVisitor, AbstractSchemaVisitor() {
    private var endHandlers = ArrayDeque<EndHandler>()
    private val prettyPrinter = PrettyPrintHelper(true)

    private fun writeLine(writer: Writer, string: String) {
        writer.write(prettyPrinter.currentIndent)
        writer.write(string)
        writer.write(prettyPrinter.endOfLine)
    }

    private fun writeEmptyLine(writer: Writer) {
        writer.write(prettyPrinter.endOfLine)
    }

    private fun writeNodeField(field: FieldSchema, type: String) {
        val keyIcon = if (field.isKey) "key" else ""

        val multiplicity = if (field.multiplicity.min == 1L && field.multiplicity.max == 1L) {
            "required"
        } else if (field.multiplicity.min == 0L && field.multiplicity.max == 1L) {
            "optional"
        } else {
            val max = if (field.multiplicity.max == 0L) "*" else field.multiplicity.max.toString()
            "[${field.multiplicity.min}..$max]"
        }

        writeLine(nodesWriter, "<TR>")
        prettyPrinter.indent()
        writeLine(nodesWriter, """<TD ALIGN="LEFT">$keyIcon</TD>""")
        writeLine(nodesWriter, """<TD ALIGN="LEFT">${field.name}</TD>""")
        writeLine(nodesWriter, """<TD ALIGN="LEFT">$type</TD>""")
        writeLine(nodesWriter, """<TD ALIGN="LEFT" PORT="${field.name}">$multiplicity</TD>""")
        prettyPrinter.unindent()
        writeLine(nodesWriter, "</TR>")
    }

    private fun getPrimitiveType(primitive: PrimitiveSchema): String = when (primitive) {
        is BooleanSchema -> "boolean"
        is ByteSchema -> "byte"
        is ShortSchema -> "short"
        is IntSchema -> "int"
        is LongSchema -> "long"
        is FloatSchema -> "float"
        is DoubleSchema -> "double"
        is Password1WaySchema -> "password_1_way"
        is Password2WaySchema -> "password_2_way"
        is StringSchema -> "string"
        is UuidSchema -> "uuid"
        is BlobSchema -> "blob"
        is TimestampSchema -> "timestamp"
        else -> "unknown"
    }

    // BracketedVisitor methods

    override fun objectStart(name: String) {
        // Register a default end-handler that does nothing.
        endHandlers.addFirst {}
    }

    override fun objectEnd() {
        if (endHandlers.isNotEmpty()) {
            val endHandler = endHandlers.pollFirst()
            endHandler()
        }
    }

    override fun listStart(name: String) {}

    override fun listEnd() {}

    // SchemaVisitor methods

    override fun visit(schema: Schema): Boolean {
        writeEmptyLine(graphWriter)
        prettyPrinter.indent()
        writeLine(graphWriter, "rankdir=LR")
        writeLine(graphWriter, "node[shape=plaintext]")
        writeEmptyLine(graphWriter)

        // Replace default end-handler with custom end-handler
        endHandlers.pollFirst()
        endHandlers.addFirst {
            prettyPrinter.unindent()
        }

        return true
    }

    override fun visit(pkg: PackageSchema): Boolean {
        writeLine(nodesWriter, """subgraph "cluster_${pkg.fullName}" {""")
        prettyPrinter.indent()
        writeLine(nodesWriter, """label="${pkg.name}"""")
        writeLine(nodesWriter, "color=lightgray")

        // Replace default end-handler with custom end-handler
        endHandlers.pollFirst()
        endHandlers.addFirst {
            prettyPrinter.unindent()
            writeLine(nodesWriter, "}")
            writeEmptyLine(nodesWriter)
        }

        return true
    }

    override fun visit(enumeration: EnumerationSchema): Boolean {
        writeLine(nodesWriter, """"${enumeration.fullName}" [label=<""")
        prettyPrinter.indent()
        writeLine(nodesWriter, """<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0">""")
        prettyPrinter.indent()
        writeLine(
            nodesWriter,
            """<TR><TD ALIGN="LEFT" PORT="0" BGCOLOR="khaki1"><B>${enumeration.name} (enum)  </B></TD></TR>"""
        )

        // Replace default end-handler with custom end-handler
        endHandlers.pollFirst()
        endHandlers.addFirst {
            prettyPrinter.unindent()
            writeLine(nodesWriter, "</TABLE>")
            prettyPrinter.unindent()
            writeLine(nodesWriter, ">]")
        }
        return true
    }

    override fun visit(enumerationValue: EnumerationValueSchema): Boolean {
        writeLine(nodesWriter, """<TR><TD ALIGN="LEFT">${enumerationValue.name}</TD></TR>""")
        return true
    }

    override fun visit(entity: EntitySchema): Boolean {
        writeLine(nodesWriter, """"${entity.fullName}" [label=<""")
        prettyPrinter.indent()
        writeLine(nodesWriter, """<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0">""")
        prettyPrinter.indent()
        writeLine(
            nodesWriter,
            """<TR><TD ALIGN="LEFT" PORT="0" COLSPAN="4" BGCOLOR="cadetblue1"><B>${entity.name} (entity)</B></TD></TR>"""
        )

        // Replace default end-handler with custom end-handler
        endHandlers.pollFirst()
        endHandlers.addFirst {
            prettyPrinter.unindent()
            writeLine(nodesWriter, "</TABLE>")
            prettyPrinter.unindent()
            writeLine(nodesWriter, ">]")
        }
        return true
    }

    override fun visit(primitiveField: PrimitiveFieldSchema): Boolean {
        writeNodeField(primitiveField, getPrimitiveType(primitiveField.primitive))
        return true
    }

    override fun visit(aliasField: AliasFieldSchema): Boolean {
        writeNodeField(aliasField, "${aliasField.aliasName} (${getPrimitiveType(aliasField.resolvedAlias.primitive)})")
        return true
    }

    override fun visit(enumerationField: EnumerationFieldSchema): Boolean {
        writeNodeField(enumerationField, enumerationField.enumerationName)
        return true
    }

    override fun visit(associationField: AssociationFieldSchema): Boolean {
        writeNodeField(associationField, associationField.resolvedEntity.name)

        linksWriter.write("""  "${associationField.parent.fullName}":"${associationField.name}" -> "${associationField.resolvedEntity.fullName}":0 [style="dashed" color=sienna]""")
        linksWriter.write(prettyPrinter.endOfLine)

        return true
    }

    override fun visit(compositionField: CompositionFieldSchema): Boolean {
        writeNodeField(compositionField, compositionField.resolvedEntity.name)

        linksWriter.write("""  "${compositionField.parent.fullName}":"${compositionField.name}" -> "${compositionField.resolvedEntity.fullName}":0 [dir=both arrowtail=diamond color=orangered]""")
        linksWriter.write(prettyPrinter.endOfLine)

        return true
    }
}
