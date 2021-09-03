package org.treeWare.metaModel.codec

import org.treeWare.model.codec.encoder.PrettyPrintHelper
import java.io.StringWriter
import java.io.Writer

class DotWriter {
    private val nodesWriter = StringWriter()
    private val linksWriter = StringWriter()

    fun nodesIndent() = prettyPrinter.indent()
    fun nodesUnindent() = prettyPrinter.unindent()

    fun nodesWriteLine(string: String) {
        nodesWriter.write(prettyPrinter.currentIndent)
        nodesWriter.write(string)
        nodesWriter.write(prettyPrinter.endOfLine)
    }

    fun linksWriteLine(string: String) {
        linksWriter.write(string)
        linksWriter.write(prettyPrinter.endOfLine)
    }

    fun writeAll(writer: Writer) {
        writeGraphStart(writer)
        writer.write(nodesWriter.toString())
        writer.write(linksWriter.toString())
        writeGraphClose(writer)
    }

    private val prettyPrinter = PrettyPrintHelper(true)

    private fun writeGraphStart(writer: Writer) {
        writer.write(
            """
            |digraph meta_model {
            |  rankdir=LR
            |  node[shape=plaintext]
            |
            |
            """.trimMargin()
        )
        writeLegend(writer)
    }

    private fun writeGraphClose(writer: Writer) {
        writer.write("}")
    }

    private fun writeLegend(writer: Writer) {
        writer.write(
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
            |
            |
            """.trimMargin()
        )
    }
}
