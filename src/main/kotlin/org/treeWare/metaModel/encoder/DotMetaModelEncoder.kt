package org.treeWare.metaModel.encoder

import org.treeWare.metaModel.*
import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.Model
import org.treeWare.model.core.Resolved
import java.io.Writer

fun encodeDot(mainMeta: Model<Resolved>, writer: Writer) {
    val dotWriter = DotWriter()
    dotWriter.nodesIndent()
    encodePackages(mainMeta, dotWriter)
    dotWriter.nodesUnindent()
    dotWriter.writeAll(writer)
}

private fun encodePackages(mainMeta: Model<Resolved>, dotWriter: DotWriter) {
    val packagesMeta = getPackagesMeta(mainMeta)
    packagesMeta.values.forEach { encodePackage(it, dotWriter) }
}

private fun encodePackage(packageElementMeta: ElementModel<Resolved>, dotWriter: DotWriter) {
    val packageMeta = packageElementMeta as EntityModel<Resolved>
    val fullName = packageMeta.aux?.fullName
    val name = getMetaName(packageMeta)

    dotWriter.nodesWriteLine("""subgraph "cluster_${fullName}" {""")
    dotWriter.nodesIndent()
    dotWriter.nodesWriteLine("""label="$name"""")
    dotWriter.nodesWriteLine("color=lightgray")

    encodeEnumerations(packageMeta, dotWriter)
    encodeEntities(packageMeta, dotWriter)

    dotWriter.nodesUnindent()
    dotWriter.nodesWriteLine("}")
    dotWriter.nodesWriteLine("")
}

private fun encodeEnumerations(packageMeta: EntityModel<Resolved>, dotWriter: DotWriter) {
    val enumerationsMeta = getEnumerationsMeta(packageMeta)
    enumerationsMeta?.values?.forEach { encodeEnumeration(it, dotWriter) }

}

private fun encodeEnumeration(enumerationElementMeta: ElementModel<Resolved>, dotWriter: DotWriter) {
    val enumerationMeta = enumerationElementMeta as EntityModel<Resolved>
    val fullName = enumerationMeta.aux?.fullName
    val name = getMetaName(enumerationMeta)

    dotWriter.nodesWriteLine(""""$fullName" [label=<""")
    dotWriter.nodesIndent()
    dotWriter.nodesWriteLine("""<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0">""")
    dotWriter.nodesIndent()
    dotWriter.nodesWriteLine("""<TR><TD ALIGN="LEFT" PORT="0" BGCOLOR="khaki1"><B>$name (enum)  </B></TD></TR>""")

    encodeEnumerationValues(enumerationMeta, dotWriter)

    dotWriter.nodesUnindent()
    dotWriter.nodesWriteLine("</TABLE>")
    dotWriter.nodesUnindent()
    dotWriter.nodesWriteLine(">]")
}

private fun encodeEnumerationValues(enumerationMeta: EntityModel<Resolved>, dotWriter: DotWriter) {
    val enumerationValuesMeta = getEnumerationValuesMeta(enumerationMeta)
    enumerationValuesMeta.values.forEach { encodeEnumerationValue(it, dotWriter) }
}

private fun encodeEnumerationValue(enumerationValueElementMeta: ElementModel<Resolved>, dotWriter: DotWriter) {
    val enumerationValueMeta = enumerationValueElementMeta as EntityModel<Resolved>
    val name = getMetaName(enumerationValueMeta)
    dotWriter.nodesWriteLine("""<TR><TD ALIGN="LEFT">$name</TD></TR>""")
}

private fun encodeEntities(packageMeta: EntityModel<Resolved>, dotWriter: DotWriter) {
    val entitiesMeta = getEntitiesMeta(packageMeta)
    entitiesMeta?.values?.forEach { encodeEntity(it, dotWriter) }
}

private fun encodeEntity(entityElementMeta: ElementModel<Resolved>, dotWriter: DotWriter) {
    val entityMeta = entityElementMeta as EntityModel<Resolved>
    val fullName = entityMeta.aux?.fullName
    val name = getMetaName(entityMeta)

    dotWriter.nodesWriteLine(""""$fullName" [label=<""")
    dotWriter.nodesIndent()
    dotWriter.nodesWriteLine("""<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0">""")
    dotWriter.nodesIndent()
    dotWriter.nodesWriteLine("""<TR><TD ALIGN="LEFT" PORT="0" COLSPAN="4" BGCOLOR="cadetblue1"><B>$name (entity)</B></TD></TR>""")

    encodeFields(entityMeta, dotWriter)

    dotWriter.nodesUnindent()
    dotWriter.nodesWriteLine("</TABLE>")
    dotWriter.nodesUnindent()
    dotWriter.nodesWriteLine(">]")
}

private fun encodeFields(entityMeta: EntityModel<Resolved>, dotWriter: DotWriter) {
    val fieldsMeta = getFieldsMeta(entityMeta)
    fieldsMeta.values.forEach { encodeField(it, dotWriter) }
}

private fun encodeField(fieldElementMeta: ElementModel<Resolved>, dotWriter: DotWriter) {
    val fieldMeta = fieldElementMeta as EntityModel<Resolved>
    when (val type = getFieldTypeMeta(fieldMeta)) {
        "enumeration" -> encodeEnumerationField(fieldMeta, dotWriter)
        "association" -> encodeAssociationField(fieldMeta, dotWriter)
        "entity" -> encodeCompositionField(fieldMeta, dotWriter)
        else -> encodeFieldRow(fieldMeta, type, dotWriter)
    }
}

private fun encodeEnumerationField(fieldMeta: EntityModel<Resolved>, dotWriter: DotWriter) {
    val type = getMetaName(fieldMeta.aux?.enumerationMeta)
    encodeFieldRow(fieldMeta, type, dotWriter)
}

private fun encodeAssociationField(fieldMeta: EntityModel<Resolved>, dotWriter: DotWriter) {
    val resolvedEntity = fieldMeta.aux?.associationMeta?.target
    val type = getMetaName(resolvedEntity)
    encodeFieldRow(fieldMeta, type, dotWriter)

    val entityFullName = fieldMeta.parent.parent.aux?.fullName
    val fullName = resolvedEntity?.aux?.fullName
    val name = getMetaName(fieldMeta)

    dotWriter.linksWriteLine("""  "$entityFullName":"$name:e" -> "$fullName":0 [style="dashed" color=sienna]""")
}

private fun encodeCompositionField(fieldMeta: EntityModel<Resolved>, dotWriter: DotWriter) {
    val resolvedEntity = fieldMeta.aux?.entityMeta
    val type = getMetaName(resolvedEntity)
    encodeFieldRow(fieldMeta, type, dotWriter)

    val entityFullName = fieldMeta.parent.parent.aux?.fullName
    val fullName = resolvedEntity?.aux?.fullName
    val name = getMetaName(fieldMeta)

    dotWriter.linksWriteLine("""  "$entityFullName":"$name:e" -> "$fullName":0 [dir=both arrowtail=diamond color=orangered]""")
}

private fun encodeFieldRow(fieldMeta: EntityModel<Resolved>, type: String, dotWriter: DotWriter) {
    val name = getMetaName(fieldMeta)
    val multiplicity = getMultiplicityMeta(fieldMeta)
    val keyIcon = if (isKeyFieldMeta(fieldMeta)) "key" else ""

    dotWriter.nodesWriteLine("<TR>")
    dotWriter.nodesIndent()
    dotWriter.nodesWriteLine("""<TD ALIGN="LEFT">$keyIcon</TD>""")
    dotWriter.nodesWriteLine("""<TD ALIGN="LEFT">$name</TD>""")
    dotWriter.nodesWriteLine("""<TD ALIGN="LEFT">$type</TD>""")
    dotWriter.nodesWriteLine("""<TD ALIGN="LEFT" PORT="$name">$multiplicity</TD>""")

    dotWriter.nodesUnindent()
    dotWriter.nodesWriteLine("</TR>")
}
