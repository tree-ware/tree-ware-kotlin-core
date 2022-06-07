package org.treeWare.metaModel.encoder

import org.treeWare.metaModel.*
import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MainModel
import org.treeWare.model.core.getMetaModelResolved
import java.io.Writer

fun encodeDot(mainMeta: MainModel, writer: Writer) {
    val dotWriter = DotWriter()
    dotWriter.nodesIndent()
    // TODO(deepak-nulu): rewrite with `forEach()` and `AbstractLeader1MetaModelVisitor`
    encodePackages(mainMeta, dotWriter)
    dotWriter.nodesUnindent()
    dotWriter.writeAll(writer)
}

private fun encodePackages(mainMeta: MainModel, dotWriter: DotWriter) {
    val packagesMeta = getPackagesMeta(mainMeta)
    packagesMeta.values.forEach { encodePackage(it, dotWriter) }
}

private fun encodePackage(packageElementMeta: ElementModel, dotWriter: DotWriter) {
    val packageMeta = packageElementMeta as EntityModel
    val fullName = getMetaModelResolved(packageMeta)?.fullName
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

private fun encodeEnumerations(packageMeta: EntityModel, dotWriter: DotWriter) {
    val enumerationsMeta = getEnumerationsMeta(packageMeta)
    enumerationsMeta?.values?.forEach { encodeEnumeration(it, dotWriter) }
}

private fun encodeEnumeration(enumerationElementMeta: ElementModel, dotWriter: DotWriter) {
    val enumerationMeta = enumerationElementMeta as EntityModel
    val fullName = getMetaModelResolved(enumerationMeta)?.fullName
    val name = getMetaName(enumerationMeta)

    dotWriter.nodesWriteLine(""""$fullName" [label=<""")
    dotWriter.nodesIndent()
    dotWriter.nodesWriteLine("""<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0">""")
    dotWriter.nodesIndent()
    dotWriter.nodesWriteLine("""<TR><TD ALIGN="LEFT" PORT="0" COLSPAN="2" BGCOLOR="khaki1"><B>$name (enum)  </B></TD></TR>""")

    encodeEnumerationValues(enumerationMeta, dotWriter)

    dotWriter.nodesUnindent()
    dotWriter.nodesWriteLine("</TABLE>")
    dotWriter.nodesUnindent()
    dotWriter.nodesWriteLine(">]")
}

private fun encodeEnumerationValues(enumerationMeta: EntityModel, dotWriter: DotWriter) {
    val enumerationValuesMeta = getEnumerationValuesMeta(enumerationMeta)
    enumerationValuesMeta.values.forEach { encodeEnumerationValue(it, dotWriter) }
}

private fun encodeEnumerationValue(enumerationValueElementMeta: ElementModel, dotWriter: DotWriter) {
    val enumerationValueMeta = enumerationValueElementMeta as EntityModel
    val number = getMetaNumber(enumerationValueMeta)
    val name = getMetaName(enumerationValueMeta)
    dotWriter.nodesWriteLine("""<TR><TD ALIGN="RIGHT">$number</TD><TD ALIGN="LEFT">$name</TD></TR>""")
}

private fun encodeEntities(packageMeta: EntityModel, dotWriter: DotWriter) {
    val entitiesMeta = getEntitiesMeta(packageMeta)
    entitiesMeta?.values?.forEach { encodeEntity(it, dotWriter) }
}

private fun encodeEntity(entityElementMeta: ElementModel, dotWriter: DotWriter) {
    val entityMeta = entityElementMeta as EntityModel
    val fullName = getMetaModelResolved(entityMeta)?.fullName
    val name = getMetaName(entityMeta)

    dotWriter.nodesWriteLine(""""$fullName" [label=<""")
    dotWriter.nodesIndent()
    dotWriter.nodesWriteLine("""<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0">""")
    dotWriter.nodesIndent()
    dotWriter.nodesWriteLine("""<TR><TD ALIGN="LEFT" PORT="0" COLSPAN="5" BGCOLOR="cadetblue1"><B>$name (entity)</B></TD></TR>""")

    encodeFields(entityMeta, dotWriter)

    dotWriter.nodesUnindent()
    dotWriter.nodesWriteLine("</TABLE>")
    dotWriter.nodesUnindent()
    dotWriter.nodesWriteLine(">]")
}

private fun encodeFields(entityMeta: EntityModel, dotWriter: DotWriter) {
    val fieldsMeta = getFieldsMeta(entityMeta)
    fieldsMeta.values.forEach { encodeField(it, dotWriter) }
}

private fun encodeField(fieldElementMeta: ElementModel, dotWriter: DotWriter) {
    val fieldMeta = fieldElementMeta as EntityModel
    when (val type = getFieldTypeMeta(fieldMeta)) {
        FieldType.ENUMERATION -> encodeEnumerationField(fieldMeta, dotWriter)
        FieldType.ASSOCIATION -> encodeAssociationField(fieldMeta, dotWriter)
        FieldType.COMPOSITION -> encodeCompositionField(fieldMeta, dotWriter)
        else -> encodeFieldRow(fieldMeta, type?.name?.lowercase() ?: "", dotWriter)
    }
}

private fun encodeEnumerationField(fieldMeta: EntityModel, dotWriter: DotWriter) {
    val type = getMetaName(getMetaModelResolved(fieldMeta)?.enumerationMeta)
    encodeFieldRow(fieldMeta, type, dotWriter)
}

private fun encodeAssociationField(fieldMeta: EntityModel, dotWriter: DotWriter) {
    val resolvedEntity = getMetaModelResolved(fieldMeta)?.associationMeta?.targetEntityMeta
    val type = getMetaName(resolvedEntity)
    encodeFieldRow(fieldMeta, type, dotWriter)

    val entityFullName = getMetaModelResolved(fieldMeta.parent.parent)?.fullName
    val fullName = getMetaModelResolved(resolvedEntity)?.fullName
    val name = getMetaName(fieldMeta)
    if (entityFullName == fullName) {
        dotWriter.linksWriteLine("""  "$entityFullName":"$name:e" -> "$fullName":0:n [style="dashed" color=sienna]""")
    } else {
        dotWriter.linksWriteLine("""  "$entityFullName":"$name:e" -> "$fullName":0 [style="dashed" color=sienna]""")
    }
}

private fun encodeCompositionField(fieldMeta: EntityModel, dotWriter: DotWriter) {
    val resolvedEntity = getMetaModelResolved(fieldMeta)?.compositionMeta
    val type = getMetaName(resolvedEntity)
    encodeFieldRow(fieldMeta, type, dotWriter)

    val entityFullName = getMetaModelResolved(fieldMeta.parent.parent)?.fullName
    val fullName = getMetaModelResolved(resolvedEntity)?.fullName
    val name = getMetaName(fieldMeta)

    if(entityFullName == fullName){
        dotWriter.linksWriteLine("""  "$entityFullName":"$name:e" -> "$fullName":0:n [dir=both arrowtail=diamond color=orangered]""")
    }
    else {
        dotWriter.linksWriteLine("""  "$entityFullName":"$name:e" -> "$fullName":0 [dir=both arrowtail=diamond color=orangered]""")
    }
}

private fun encodeFieldRow(fieldMeta: EntityModel, type: String, dotWriter: DotWriter) {
    val number = getMetaNumber(fieldMeta)
    val name = getMetaName(fieldMeta)
    val multiplicity = getMultiplicityMeta(fieldMeta).name.lowercase()
    val keyIcon = if (isKeyFieldMeta(fieldMeta)) "key" else ""

    dotWriter.nodesWriteLine("<TR>")
    dotWriter.nodesIndent()
    dotWriter.nodesWriteLine("""<TD ALIGN="LEFT">$keyIcon</TD>""")
    dotWriter.nodesWriteLine("""<TD ALIGN="RIGHT">$number</TD>""")
    dotWriter.nodesWriteLine("""<TD ALIGN="LEFT">$name</TD>""")
    dotWriter.nodesWriteLine("""<TD ALIGN="LEFT">$type</TD>""")
    dotWriter.nodesWriteLine("""<TD ALIGN="LEFT" PORT="$name">$multiplicity</TD>""")

    dotWriter.nodesUnindent()
    dotWriter.nodesWriteLine("</TR>")
}