package org.treeWare.metaModel.encoder

import okio.BufferedSink
import okio.Sink
import org.treeWare.model.encoder.PrettyPrintHelper
import org.treeWare.util.buffered

class DotWriter {
    private val nodesWriter = StringBuilder()
    private val linksWriter = StringBuilder()

    fun nodesIndent() = prettyPrinter.indent()
    fun nodesUnindent() = prettyPrinter.unindent()

    fun nodesWriteLine(string: String) {
        nodesWriter.append(prettyPrinter.currentIndent)
        nodesWriter.append(string)
        nodesWriter.append(prettyPrinter.endOfLine)
    }

    fun linksWriteLine(string: String) {
        linksWriter.append(string)
        linksWriter.append(prettyPrinter.endOfLine)
    }

    fun writeAll(sink: Sink) {
        sink.buffered().use { bufferedSink ->
            writeGraphStart(bufferedSink)
            bufferedSink.writeUtf8(nodesWriter.toString())
            bufferedSink.writeUtf8(linksWriter.toString())
            writeGraphClose(bufferedSink)
        }
    }

    private val prettyPrinter = PrettyPrintHelper(true)

    private fun writeGraphStart(bufferedSink: BufferedSink) {
        bufferedSink.writeUtf8(
            """
            |digraph meta_model {
            |  rankdir=LR
            |  node[shape=plaintext]
            |
            |
            """.trimMargin()
        )
        writeLegend(bufferedSink)
    }

    private fun writeGraphClose(bufferedSink: BufferedSink) {
        bufferedSink.writeUtf8("}")
    }

    private fun writeLegend(bufferedSink: BufferedSink) {
        bufferedSink.writeUtf8(
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