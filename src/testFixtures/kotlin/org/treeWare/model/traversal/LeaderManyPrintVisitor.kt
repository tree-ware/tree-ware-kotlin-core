package org.treeWare.model.traversal

import org.treeWare.metaModel.*
import org.treeWare.model.core.*
import java.io.Writer
import java.util.*

class LeaderManyPrintVisitor(private val writer: Writer) :
    AbstractLeaderManyModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    private fun print(key: String, values: List<String?> = listOf("TODO")) {
        writer.write(key)
        writer.write(": ")
        writer.write(values.joinToString(", "))
        writer.write("\n")
    }

    private fun printFieldNames(key: String, leaderFieldList: List<FieldModel?>): TraversalAction {
        val fieldNames = leaderFieldList.map { field -> field?.meta?.let { getMetaName(it) } }
        print(key, fieldNames)
        return TraversalAction.CONTINUE
    }

    override fun visitMain(leaderMainList: List<MainModel?>): TraversalAction {
        val rootNames = leaderMainList.map { main -> main?.meta?.let { getRootMeta(it) }?.let { getMetaName(it) } }
        print("Main", rootNames)
        return TraversalAction.CONTINUE
    }

    override fun visitRoot(leaderRootList: List<RootModel?>): TraversalAction {
        val rootEntityNames = leaderRootList.map { root -> root?.meta?.let { getMetaName(it) } }
        print("Root", rootEntityNames)
        return TraversalAction.CONTINUE
    }

    override fun visitEntity(leaderEntityList: List<EntityModel?>): TraversalAction {
        val entityNames = leaderEntityList.map { entity ->
            entity?.meta?.let {
                val packageName = getPackageName(it)
                val entityName = getMetaName(it)
                "$packageName/$entityName"
            }
        }
        print("Entity", entityNames)
        return TraversalAction.CONTINUE
    }

    override fun visitSingleField(leaderFieldList: List<SingleFieldModel?>): TraversalAction =
        printFieldNames("Single field", leaderFieldList)

    override fun visitListField(leaderFieldList: List<ListFieldModel?>): TraversalAction =
        printFieldNames("List field", leaderFieldList)

    override fun visitSetField(leaderFieldList: List<SetFieldModel?>): TraversalAction =
        printFieldNames("Set field", leaderFieldList)

    override fun visitPrimitive(leaderValueList: List<PrimitiveModel?>): TraversalAction {
        val values = leaderValueList.map { valueElement ->
            valueElement?.value?.let { value ->
                val fieldType = valueElement.parent.meta?.let { getFieldTypeMeta(it) }
                if (fieldType == FieldType.BLOB) Base64.getEncoder().encodeToString(value as ByteArray)
                else value.toString()
            }
        }
        print("Primitive", values)
        return TraversalAction.CONTINUE
    }

    override fun visitAlias(leaderValueList: List<AliasModel?>): TraversalAction {
        print("Alias")
        return TraversalAction.CONTINUE
    }

    override fun visitPassword1way(leaderValueList: List<Password1wayModel?>): TraversalAction {
        val values = leaderValueList.map { it?.let { "{${it.unhashed}, ${it.hashed}, ${it.hashVersion}}" } }
        print("Password1way", values)
        return TraversalAction.CONTINUE
    }

    override fun visitPassword2way(leaderValueList: List<Password2wayModel?>): TraversalAction {
        val values = leaderValueList.map { it?.let { "{${it.unencrypted}, ${it.encrypted}, ${it.cipherVersion}}" } }
        print("Password2way", values)
        return TraversalAction.CONTINUE
    }

    override fun visitEnumeration(leaderValueList: List<EnumerationModel?>): TraversalAction {
        val values = leaderValueList.map { it?.value }
        print("Enumeration", values)
        return TraversalAction.CONTINUE
    }

    override fun visitAssociation(leaderValueList: List<AssociationModel?>): TraversalAction {
        val values = leaderValueList.map { association ->
            association?.parent?.meta?.let { associationMeta -> getAssociationInfoMeta(associationMeta) }?.values?.map {
                (it as PrimitiveModel).value
            }?.toString()
        }
        print("Association", values)
        return TraversalAction.CONTINUE
    }
}
