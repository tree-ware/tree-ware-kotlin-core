package org.treeWare.model.operator

import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeaderManyModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import org.treeWare.util.assertInDevMode

fun union(inputs: List<MainModel>): MutableMainModel {
    val unionVisitor = UnionVisitor()
    forEach(inputs, unionVisitor)
    return unionVisitor.unionMain
}

private class UnionVisitor : AbstractLeaderManyModelVisitor<TraversalAction>(
    TraversalAction.CONTINUE
) {
    val modelStack = ArrayDeque<MutableElementModel>()
    lateinit var unionMain: MutableMainModel

    override fun visitMain(leaderMainList: List<MainModel?>): TraversalAction {
        unionMain = MutableMainModel(leaderMainList.last()?.mainMeta)
        visitAux(leaderMainList, unionMain)
        modelStack.addFirst(unionMain)
        return TraversalAction.CONTINUE
    }

    override fun leaveMain(leaderMainList: List<MainModel?>) {
        modelStack.removeFirst()
        assertInDevMode(modelStack.isEmpty())
    }

    override fun visitEntity(leaderEntityList: List<EntityModel?>): TraversalAction {
        val parent = modelStack.first()
        val unionEntity = getNewFieldValue(parent)
        visitAux(leaderEntityList, unionEntity)
        modelStack.addFirst(unionEntity)
        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntityList: List<EntityModel?>) {
        val unionEntity = modelStack.removeFirst() as MutableEntityModel
        // NOTE: entities should be added to set-fields only after the entity
        // has key fields.
        val parent = modelStack.first()
        if (parent.elementType == ModelElementType.SET_FIELD) (parent as MutableSetFieldModel).addValue(unionEntity)
    }

    override fun visitSingleField(leaderFieldList: List<SingleFieldModel?>): TraversalAction =
        visitField(leaderFieldList)

    override fun leaveSingleField(leaderFieldList: List<SingleFieldModel?>) {
        modelStack.removeFirst()
    }

    override fun visitListField(leaderFieldList: List<ListFieldModel?>): TraversalAction =
        visitField(leaderFieldList)

    override fun leaveListField(leaderFieldList: List<ListFieldModel?>) {
        modelStack.removeFirst()
    }

    override fun visitSetField(leaderFieldList: List<SetFieldModel?>): TraversalAction =
        visitField(leaderFieldList)

    override fun leaveSetField(leaderFieldList: List<SetFieldModel?>) {
        modelStack.removeFirst()
    }

    override fun visitPrimitive(leaderValueList: List<PrimitiveModel?>): TraversalAction {
        val lastPrimitive = leaderValueList.lastNotNullOf { it }
        val parent = modelStack.first()
        val unionPrimitive = getNewFieldValue(parent) as MutablePrimitiveModel
        unionPrimitive.copyValueFrom(lastPrimitive)
        visitAux(leaderValueList, unionPrimitive)
        return TraversalAction.CONTINUE
    }

    override fun visitAlias(leaderValueList: List<AliasModel?>): TraversalAction {
        val lastAlias = leaderValueList.lastNotNullOf { it }
        val parent = modelStack.first()
        val unionAlias = getNewFieldValue(parent) as MutableAliasModel
        unionAlias.copyValueFrom(lastAlias)
        visitAux(leaderValueList, unionAlias)
        return TraversalAction.CONTINUE
    }

    override fun visitPassword1way(leaderValueList: List<Password1wayModel?>): TraversalAction {
        val lastPassword = leaderValueList.lastNotNullOf { it }
        val parent = modelStack.first()
        val unionPassword = getNewFieldValue(parent) as MutablePassword1wayModel
        unionPassword.copyValueFrom(lastPassword)
        visitAux(leaderValueList, unionPassword)
        return TraversalAction.CONTINUE
    }

    override fun visitPassword2way(leaderValueList: List<Password2wayModel?>): TraversalAction {
        val lastPassword = leaderValueList.lastNotNullOf { it }
        val parent = modelStack.first()
        val unionPassword = getNewFieldValue(parent) as MutablePassword2wayModel
        unionPassword.copyValueFrom(lastPassword)
        visitAux(leaderValueList, unionPassword)
        return TraversalAction.CONTINUE
    }

    override fun visitEnumeration(leaderValueList: List<EnumerationModel?>): TraversalAction {
        val lastEnumeration = leaderValueList.lastNotNullOf { it }
        val parent = modelStack.first()
        val unionEnumeration = getNewFieldValue(parent) as MutableEnumerationModel
        unionEnumeration.copyValueFrom(lastEnumeration)
        visitAux(leaderValueList, unionEnumeration)
        return TraversalAction.CONTINUE
    }

    override fun visitAssociation(leaderValueList: List<AssociationModel?>): TraversalAction {
        val lastAssociation = leaderValueList.lastNotNullOf { it }
        val parent = modelStack.first()
        val unionAssociation = getNewFieldValue(parent) as MutableAssociationModel
        copy(lastAssociation, unionAssociation)
        visitAux(leaderValueList, unionAssociation)
        return TraversalAction.CONTINUE
    }

    // Helpers

    private fun visitField(leaderFieldList: List<FieldModel?>): TraversalAction {
        val lastLeaderField = leaderFieldList.lastNotNullOf { it }
        val lastLeaderFieldName = getFieldName(lastLeaderField)
        val parent = modelStack.first() as MutableBaseEntityModel
        val unionField = parent.getOrNewField(lastLeaderFieldName)
        visitAux(leaderFieldList, unionField)
        modelStack.addFirst(unionField)
        return TraversalAction.CONTINUE
    }

    private fun visitAux(leaderElementList: List<ElementModel?>, unionElement: MutableElementModel) {
        leaderElementList.forEach { leader ->
            leader?.auxs?.forEach { (auxName, aux) -> unionElement.setAux(auxName, aux) }
        }
    }
}