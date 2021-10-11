package org.treeWare.model.operator

import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeaderManyFollower0ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import java.util.*

fun <Aux> union(inputs: List<MainModel<Aux>>): MutableMainModel<Aux> {
    val unionVisitor = UnionVisitor<Aux>()
    forEach(inputs, unionVisitor)
    return unionVisitor.unionMain
}

private class UnionVisitor<Aux> : AbstractLeaderManyFollower0ModelVisitor<Aux, TraversalAction>(
    TraversalAction.CONTINUE
) {
    val modelStack = ArrayDeque<MutableElementModel<Aux>>()
    lateinit var unionMain: MutableMainModel<Aux>

    override fun visitMain(leaderMainList: List<MainModel<Aux>?>): TraversalAction {
        unionMain = MutableMainModel(leaderMainList.last()?.meta)
        modelStack.addFirst(unionMain)
        return TraversalAction.CONTINUE
    }

    override fun leaveMain(leaderMainList: List<MainModel<Aux>?>) {
        modelStack.pollFirst()
        assert(modelStack.isEmpty())
    }

    override fun visitRoot(leaderRootList: List<RootModel<Aux>?>): TraversalAction {
        val unionRoot = unionMain.getOrNewRoot()
        modelStack.addFirst(unionRoot)
        return TraversalAction.CONTINUE
    }

    override fun leaveRoot(leaderRootList: List<RootModel<Aux>?>) {
        modelStack.pollFirst()
    }

    override fun visitEntity(leaderEntityList: List<EntityModel<Aux>?>): TraversalAction {
        val parent = modelStack.peekFirst()
        val unionEntity = getNewFieldValue(parent)
        modelStack.addFirst(unionEntity)
        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntityList: List<EntityModel<Aux>?>) {
        val unionEntity = modelStack.pollFirst() as MutableEntityModel<Aux>
        // NOTE: entities should be added to set-fields only after the entity
        // has key fields.
        val parent = modelStack.peekFirst()
        if (parent.elementType == ModelElementType.SET_FIELD) (parent as MutableSetFieldModel<Aux>).addValue(unionEntity)
    }

    override fun visitSingleField(leaderFieldList: List<SingleFieldModel<Aux>?>): TraversalAction =
        visitField(leaderFieldList)

    override fun leaveSingleField(leaderFieldList: List<SingleFieldModel<Aux>?>) {
        modelStack.pollFirst()
    }

    override fun visitListField(leaderFieldList: List<ListFieldModel<Aux>?>): TraversalAction =
        visitField(leaderFieldList)

    override fun leaveListField(leaderFieldList: List<ListFieldModel<Aux>?>) {
        modelStack.pollFirst()
    }

    override fun visitSetField(leaderFieldList: List<SetFieldModel<Aux>?>): TraversalAction =
        visitField(leaderFieldList)

    override fun leaveSetField(leaderFieldList: List<SetFieldModel<Aux>?>) {
        modelStack.pollFirst()
    }

    override fun visitPrimitive(leaderValueList: List<PrimitiveModel<Aux>?>): TraversalAction {
        val lastPrimitive = leaderValueList.lastNotNullOf { it }
        val parent = modelStack.peekFirst()
        val unionPrimitive = getNewFieldValue(parent) as MutablePrimitiveModel<Aux>
        unionPrimitive.copyValueFrom(lastPrimitive)
        return TraversalAction.CONTINUE
    }

    override fun visitAlias(leaderValueList: List<AliasModel<Aux>?>): TraversalAction {
        val lastAlias = leaderValueList.lastNotNullOf { it }
        val parent = modelStack.peekFirst()
        val unionAlias = getNewFieldValue(parent) as MutableAliasModel<Aux>
        unionAlias.copyValueFrom(lastAlias)
        return TraversalAction.CONTINUE
    }

    override fun visitPassword1way(leaderValueList: List<Password1wayModel<Aux>?>): TraversalAction {
        val lastPassword = leaderValueList.lastNotNullOf { it }
        val parent = modelStack.peekFirst()
        val unionPassword = getNewFieldValue(parent) as MutablePassword1wayModel<Aux>
        unionPassword.copyValueFrom(lastPassword)
        return TraversalAction.CONTINUE
    }

    override fun visitPassword2way(leaderValueList: List<Password2wayModel<Aux>?>): TraversalAction {
        val lastPassword = leaderValueList.lastNotNullOf { it }
        val parent = modelStack.peekFirst()
        val unionPassword = getNewFieldValue(parent) as MutablePassword2wayModel<Aux>
        unionPassword.copyValueFrom(lastPassword)
        return TraversalAction.CONTINUE
    }

    override fun visitEnumeration(leaderValueList: List<EnumerationModel<Aux>?>): TraversalAction {
        val lastEnumeration = leaderValueList.lastNotNullOf { it }
        val parent = modelStack.peekFirst()
        val unionEnumeration = getNewFieldValue(parent) as MutableEnumerationModel<Aux>
        unionEnumeration.copyValueFrom(lastEnumeration)
        return TraversalAction.CONTINUE
    }

    override fun visitAssociation(leaderValueList: List<AssociationModel<Aux>?>): TraversalAction {
        val lastAssociation = leaderValueList.lastNotNullOf { it }
        val parent = modelStack.peekFirst()
        val unionAssociation = getNewFieldValue(parent) as MutableAssociationModel<Aux>
        copy(lastAssociation, unionAssociation)
        return TraversalAction.CONTINUE
    }

    // Helpers

    private fun visitField(leaderFieldList: List<FieldModel<Aux>?>): TraversalAction {
        val lastLeaderField = leaderFieldList.lastNotNullOf { it }
        val lastLeaderFieldName = getFieldName(lastLeaderField)
        val parent = modelStack.peekFirst() as MutableBaseEntityModel<Aux>
        val unionField = parent.getOrNewField(lastLeaderFieldName)
        modelStack.addFirst(unionField)
        return TraversalAction.CONTINUE
    }
}
