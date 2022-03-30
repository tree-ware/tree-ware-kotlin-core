package org.treeWare.model.operator.set

import org.treeWare.metaModel.FieldType
import org.treeWare.model.core.*
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.SetEntityDelegate
import org.treeWare.model.operator.set.aux.SetAuxStack
import org.treeWare.model.operator.set.aux.getSetAux
import org.treeWare.model.traversal.AbstractLeader1ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import org.treeWare.util.assertInDevMode

class SetDelegateVisitor(
    private val setDelegate: SetDelegate,
    private val entityDelegates: EntityDelegateRegistry<SetEntityDelegate>?
) : AbstractLeader1ModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val errors = mutableListOf<String>()

    private val entityPathStack = EntityPathStack()
    private val setAuxStack = SetAuxStack()

    override fun visitMain(leaderMain1: MainModel): TraversalAction {
        val setAuxError = setAuxStack.push(getSetAux(leaderMain1))
        assertInDevMode(setAuxError == null)
        return TraversalAction.CONTINUE
    }

    override fun leaveMain(leaderMain1: MainModel) {
        setAuxStack.pop()
        assertInDevMode(entityPathStack.isEmpty())
        assertInDevMode(setAuxStack.isEmpty())
    }

    override fun visitEntity(leaderEntity1: EntityModel): TraversalAction {
        if (isCompositionKey(leaderEntity1)) {
            entityPathStack.push(null)
            setAuxStack.push(null)
            return TraversalAction.ABORT_SUB_TREE
        }
        val entityFullName = getMetaModelFullName(leaderEntity1)
        val entityDelegate = entityDelegates?.get(entityFullName)
        if (entityDelegate?.isSingleValue() == true) {
            entityPathStack.push(null)
            setAuxStack.push(null)
            return TraversalAction.ABORT_SUB_TREE
        }

        val setAuxError = setAuxStack.push(getSetAux(leaderEntity1))
        assertInDevMode(setAuxError == null)
        entityPathStack.push(leaderEntity1)

        val activeSetAux = setAuxStack.peekActive() ?: return TraversalAction.CONTINUE
        val entityPath = entityPathStack.peekEntityPath()
        val keys = entityPathStack.peekKeys()
        val (associations, other) = getNonKeys(leaderEntity1)
        val delegateErrors = setDelegate.setEntity(
            activeSetAux,
            leaderEntity1,
            entityPath,
            entityPathStack.ancestorKeys(),
            keys.available,
            associations,
            other
        )
        delegateErrors.forEach { errors.add("$entityPath: $it") }
        return if (delegateErrors.isEmpty()) TraversalAction.CONTINUE else TraversalAction.ABORT_SUB_TREE
    }

    override fun leaveEntity(leaderEntity1: EntityModel) {
        entityPathStack.pop()
        setAuxStack.pop()
    }

    override fun visitSingleField(leaderField1: SingleFieldModel): TraversalAction {
        if (isCompositionField(leaderField1)) {
            val setAuxError = setAuxStack.push(getSetAux(leaderField1))
            assertInDevMode(setAuxError == null)
        }
        return TraversalAction.CONTINUE
    }

    override fun leaveSingleField(leaderField1: SingleFieldModel) {
        if (isCompositionField(leaderField1)) setAuxStack.pop()
    }

    override fun visitSetField(leaderField1: SetFieldModel): TraversalAction {
        val setAuxError = setAuxStack.push(getSetAux(leaderField1))
        assertInDevMode(setAuxError == null)
        return TraversalAction.CONTINUE
    }

    override fun leaveSetField(leaderField1: SetFieldModel) {
        setAuxStack.pop()
    }

    private fun getNonKeys(entity: EntityModel): NonKeys {
        val visitor = NonKeysVisitor(entityDelegates)
        forEach(entity, visitor, false)
        return NonKeys(visitor.associations, visitor.other)
    }
}

/** Contains associations and non-composition/non-association fields. */
private data class NonKeys(val associations: List<FieldModel>, val other: List<FieldModel>)

private class NonKeysVisitor(
    private val entityDelegates: EntityDelegateRegistry<SetEntityDelegate>?
) : AbstractLeader1ModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val associations = mutableListOf<FieldModel>()
    val other = mutableListOf<FieldModel>()

    private var visitEntityCount = 0

    override fun visitEntity(leaderEntity1: EntityModel): TraversalAction {
        ++visitEntityCount
        assertInDevMode(visitEntityCount == 1)
        return if (visitEntityCount == 1) TraversalAction.CONTINUE else TraversalAction.ABORT_SUB_TREE
    }

    override fun visitSingleField(leaderField1: SingleFieldModel): TraversalAction {
        when (getFieldType(leaderField1)) {
            FieldType.COMPOSITION -> {
                val compositionMeta = getMetaModelResolved(leaderField1.meta)?.compositionMeta
                val entityFullName = getMetaModelResolved(compositionMeta)?.fullName
                val entityDelegate = entityDelegates?.get(entityFullName)
                if (entityDelegate?.isSingleValue() == true) other.add(leaderField1)
            }
            FieldType.ASSOCIATION -> associations.add(leaderField1)
            else -> if (!isKeyField(leaderField1)) other.add(leaderField1)
        }
        return TraversalAction.ABORT_SUB_TREE
    }

    override fun visitListField(leaderField1: ListFieldModel): TraversalAction {
        when (getFieldType(leaderField1)) {
            FieldType.ASSOCIATION -> associations.add(leaderField1)
            else -> other.add(leaderField1)
        }
        return TraversalAction.ABORT_SUB_TREE
    }

    override fun visitSetField(leaderField1: SetFieldModel): TraversalAction {
        return TraversalAction.ABORT_SUB_TREE
    }
}