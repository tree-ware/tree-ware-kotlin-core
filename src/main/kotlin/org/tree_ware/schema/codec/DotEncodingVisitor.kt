package org.tree_ware.schema.codec

import org.tree_ware.common.codec.PrettyPrintHelper
import org.tree_ware.common.traversal.TraversalAction
import org.tree_ware.schema.core.*
import org.tree_ware.schema.visitor.AbstractSchemaVisitor
import java.io.Writer

class DotEncodingVisitor(
    private val graphWriter: Writer,
    private val nodesWriter: Writer,
    private val linksWriter: Writer
) : AbstractSchemaVisitor<TraversalAction>(TraversalAction.CONTINUE) {
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

        val multiplicity = when {
            field.multiplicity.isRequired() -> "required"
            field.multiplicity.isOptional() -> "optional"
            else -> {
                val max = if (field.multiplicity.max == 0L) "*" else field.multiplicity.max.toString()
                "[${field.multiplicity.min}..$max]"
            }
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

    private fun writeLegend() {
        graphWriter.write(
            """
            |  subgraph "cluster_legend" {
            |    label="legend"
            |    color=lightgray
            |    "source" [label=<
            |      <TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0" CELLPADDING="5">
            |        <TR><TD ALIGN="RIGHT" PORT="0">parent</TD></TR>
            |        <TR><TD ALIGN="RIGHT" PORT="1">source</TD></TR>
            |      </TABLE>
            |    >]
            |    "target" [label=<
            |      <TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0" CELLPADDING="5">
            |        <TR>
            |          <TD ALIGN="LEFT" PORT="0">child</TD>
            |          <TD>:</TD>
            |          <TD ALIGN="LEFT">composition (deleting the parent will delete the child)</TD>
            |        </TR>
            |        <TR>
            |          <TD ALIGN="LEFT" PORT="1">target</TD>
            |          <TD>:</TD>
            |          <TD ALIGN="LEFT">association (deleting the source will NOT delete the target)</TD>
            |        </TR>
            |      </TABLE>
            |    >]
            |    source:0 -> target:0 [dir=both arrowtail=diamond color=orangered]
            |    source:1 -> target:1 [style="dashed" color=sienna]
            |  }
            """.trimMargin()
        )
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

    // SchemaVisitor methods

    override fun visit(schema: Schema): TraversalAction {
        writeEmptyLine(graphWriter)
        prettyPrinter.indent()
        writeLine(graphWriter, "rankdir=LR")
        writeLine(graphWriter, "node[shape=plaintext]")
        writeEmptyLine(graphWriter)
        writeLegend()
        writeEmptyLine(graphWriter)
        writeEmptyLine(graphWriter)

        return TraversalAction.CONTINUE
    }

    override fun leave(schema: Schema) {
        prettyPrinter.unindent()
    }

    override fun visit(pkg: PackageSchema): TraversalAction {
        writeLine(nodesWriter, """subgraph "cluster_${pkg.fullName}" {""")
        prettyPrinter.indent()
        writeLine(nodesWriter, """label="${pkg.name}"""")
        writeLine(nodesWriter, "color=lightgray")

        return TraversalAction.CONTINUE
    }

    override fun leave(pkg: PackageSchema) {
        prettyPrinter.unindent()
        writeLine(nodesWriter, "}")
        writeEmptyLine(nodesWriter)
    }

    override fun visit(enumeration: EnumerationSchema): TraversalAction {
        writeLine(nodesWriter, """"${enumeration.fullName}" [label=<""")
        prettyPrinter.indent()
        writeLine(nodesWriter, """<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0">""")
        prettyPrinter.indent()
        writeLine(
            nodesWriter,
            """<TR><TD ALIGN="LEFT" PORT="0" BGCOLOR="khaki1"><B>${enumeration.name} (enum)  </B></TD></TR>"""
        )

        return TraversalAction.CONTINUE
    }

    override fun leave(enumeration: EnumerationSchema) {
        prettyPrinter.unindent()
        writeLine(nodesWriter, "</TABLE>")
        prettyPrinter.unindent()
        writeLine(nodesWriter, ">]")
    }

    override fun visit(enumerationValue: EnumerationValueSchema): TraversalAction {
        writeLine(nodesWriter, """<TR><TD ALIGN="LEFT">${enumerationValue.name}</TD></TR>""")
        return TraversalAction.CONTINUE
    }

    override fun visit(entity: EntitySchema): TraversalAction {
        writeLine(nodesWriter, """"${entity.fullName}" [label=<""")
        prettyPrinter.indent()
        writeLine(nodesWriter, """<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0">""")
        prettyPrinter.indent()
        writeLine(
            nodesWriter,
            """<TR><TD ALIGN="LEFT" PORT="0" COLSPAN="4" BGCOLOR="cadetblue1"><B>${entity.name} (entity)</B></TD></TR>"""
        )

        return TraversalAction.CONTINUE
    }

    override fun leave(entity: EntitySchema) {
        prettyPrinter.unindent()
        writeLine(nodesWriter, "</TABLE>")
        prettyPrinter.unindent()
        writeLine(nodesWriter, ">]")
    }

    override fun visit(primitiveField: PrimitiveFieldSchema): TraversalAction {
        writeNodeField(primitiveField, getPrimitiveType(primitiveField.primitive))
        return TraversalAction.CONTINUE
    }

    override fun visit(aliasField: AliasFieldSchema): TraversalAction {
        writeNodeField(aliasField, "${aliasField.aliasName} (${getPrimitiveType(aliasField.resolvedAlias.primitive)})")
        return TraversalAction.CONTINUE
    }

    override fun visit(enumerationField: EnumerationFieldSchema): TraversalAction {
        writeNodeField(enumerationField, enumerationField.enumerationName)
        return TraversalAction.CONTINUE
    }

    override fun visit(associationField: AssociationFieldSchema): TraversalAction {
        writeNodeField(associationField, associationField.resolvedEntity.name)

        linksWriter.write("""  "${associationField.parent.fullName}":"${associationField.name}:e" -> "${associationField.resolvedEntity.fullName}":0 [style="dashed" color=sienna]""")
        linksWriter.write(prettyPrinter.endOfLine)

        return TraversalAction.CONTINUE
    }

    override fun visit(compositionField: CompositionFieldSchema): TraversalAction {
        writeNodeField(compositionField, compositionField.resolvedEntity.name)

        linksWriter.write("""  "${compositionField.parent.fullName}":"${compositionField.name}:e" -> "${compositionField.resolvedEntity.fullName}":0 [dir=both arrowtail=diamond color=orangered]""")
        linksWriter.write(prettyPrinter.endOfLine)

        return TraversalAction.CONTINUE
    }
}
