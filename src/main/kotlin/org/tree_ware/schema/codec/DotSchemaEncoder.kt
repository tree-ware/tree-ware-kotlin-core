package org.tree_ware.schema.codec

import org.tree_ware.schema.core.ElementSchema
import java.io.StringWriter
import java.io.Writer

fun encodeDot(element: ElementSchema, writer: Writer): Boolean {
    val graphWriter = StringWriter()
    val nodesWriter = StringWriter()
    val linksWriter = StringWriter()
    val encodingVisitor =
        DotEncodingVisitor(graphWriter, nodesWriter, linksWriter)

    val encoded = element.accept(encodingVisitor)

    writer.write("digraph schema {")
    writer.write(graphWriter.toString())
    writer.write(nodesWriter.toString())
    writer.write(linksWriter.toString())
    writer.write("}")

    return encoded
}
