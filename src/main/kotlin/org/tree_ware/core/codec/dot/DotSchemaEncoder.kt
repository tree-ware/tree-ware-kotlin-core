package org.tree_ware.core.codec.dot

import org.tree_ware.core.codec.common.SchemaEncoder
import org.tree_ware.core.schema.ElementSchema
import java.io.StringWriter
import java.io.Writer

class DotSchemaEncoder(private val writer: Writer) : SchemaEncoder {
    // SchemaEncoder methods

    override fun encode(element: ElementSchema): Boolean {
        val graphWriter = StringWriter()
        val nodesWriter = StringWriter()
        val linksWriter = StringWriter()
        val encodingVisitor = DotEncodingVisitor(graphWriter, nodesWriter, linksWriter)

        val encoded = element.accept(encodingVisitor)

        writer.write("digraph schema {")
        writer.write(graphWriter.toString())
        writer.write(nodesWriter.toString())
        writer.write(linksWriter.toString())
        writer.write("}")

        return encoded
    }
}
