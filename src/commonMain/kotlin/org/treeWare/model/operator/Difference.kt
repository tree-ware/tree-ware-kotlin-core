package org.treeWare.model.operator

import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeaderManyModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach

fun difference(
    oldModel: EntityModel,
    newModel: EntityModel,
    entityFactory: EntityFactory,
): DifferenceModels {
    val differenceVisitor = DifferenceVisitor(entityFactory)
    forEach(listOf(oldModel, newModel), differenceVisitor, false)
    return differenceVisitor.output
}

private class DifferenceVisitor(
    private val entityFactory: EntityFactory,
) : AbstractLeaderManyModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val output: DifferenceModels = newDifferenceModels(entityFactory)

    private val createStack = ArrayDeque<MutableElementModel?>()
    private val deleteStack = ArrayDeque<MutableElementModel?>()
    private val updateStack = ArrayDeque<MutableElementModel?>()

    private val inclusionStack = ArrayDeque<Inclusions>()

    override fun visitEntity(leaderEntityList: List<EntityModel?>): TraversalAction {
        val oldEntity = leaderEntityList.first()
        val newEntity = leaderEntityList.last()

        val entityInclusions = Inclusions()
        inclusionStack.addFirst(entityInclusions)

        val createParent = createStack.firstOrNull()
        val createEntity = createParent?.getOrNewValue() ?: output.createModel
        createStack.addFirst(createEntity)

        val deleteParent = deleteStack.firstOrNull()
        val deleteEntity = deleteParent?.getOrNewValue() ?: output.deleteModel
        deleteStack.addFirst(deleteEntity)

        val updateParent = updateStack.firstOrNull()
        val updateEntity = updateParent?.getOrNewValue() ?: output.updateModel
        updateStack.addFirst(updateEntity)

        if (oldEntity == null) {
            entityInclusions.inCreate = true
            copy(checkNotNull(newEntity), createEntity)
            return TraversalAction.ABORT_SUB_TREE
        }
        if (newEntity == null) {
            entityInclusions.inDelete = true
            copy(oldEntity, deleteEntity)
            return TraversalAction.ABORT_SUB_TREE
        }

        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntityList: List<EntityModel?>) {
        val entityInclusions = inclusionStack.removeFirst()

        leaveEntity(createStack, entityInclusions.inCreate)
        leaveEntity(deleteStack, entityInclusions.inDelete)
        leaveEntity(updateStack, entityInclusions.inUpdate)

        val parentInclusions = inclusionStack.firstOrNull()
        parentInclusions?.addChildInclusions(entityInclusions)
    }

    override fun visitSingleField(leaderFieldList: List<SingleFieldModel?>): TraversalAction =
        visitField(leaderFieldList)

    override fun leaveSingleField(leaderFieldList: List<SingleFieldModel?>) =
        leaveField()

    override fun visitSetField(leaderFieldList: List<SetFieldModel?>): TraversalAction =
        visitField(leaderFieldList)

    override fun leaveSetField(leaderFieldList: List<SetFieldModel?>) =
        leaveField()


    override fun visitPrimitive(leaderValueList: List<PrimitiveModel?>): TraversalAction =
        visitValue(leaderValueList)

    override fun visitAlias(leaderValueList: List<AliasModel?>): TraversalAction =
        visitValue(leaderValueList)

    override fun visitPassword1way(leaderValueList: List<Password1wayModel?>): TraversalAction =
        visitValue(leaderValueList)

    override fun visitPassword2way(leaderValueList: List<Password2wayModel?>): TraversalAction =
        visitValue(leaderValueList)

    override fun visitEnumeration(leaderValueList: List<EnumerationModel?>): TraversalAction =
        visitValue(leaderValueList)

    override fun visitAssociation(leaderValueList: List<AssociationModel?>): TraversalAction =
        visitValue(leaderValueList)


    // Helpers

    private fun leaveEntity(stack: ArrayDeque<MutableElementModel?>, include: Boolean) {
        // Even if there are no differences, keys are added on the way down in case there are differences further below
        // in the tree. On the way up, the entity must be removed if it does not have any other fields. The entity is
        // indirectly removed by detaching all its field.
        val outputEntity = stack.removeFirst() as MutableEntityModel
        if (!include && !isCompositionKey(outputEntity)) {
            outputEntity.detachAllFields()
            return
        }
        val parent = stack.firstOrNull()
        if (parent?.elementType == ModelElementType.SET_FIELD) {
            // Entities must be added to a set-field only after all its key fields have been sent, and so this must
            // be done in leaveEntity() rather than in visitEntity().
            runCatching { (parent as MutableSetFieldModel).addValue(outputEntity) }
        }
    }

    private fun visitValue(
        leaderValueList: List<ElementModel?>
    ): TraversalAction {
        // NOTE: This only populates create and delete trees if the parent is a key field.

        val updateParent = updateStack.first() as MutableFieldModel
        val oldValue = leaderValueList.first()
        val newValue = leaderValueList.last()
        val parentInclusions = inclusionStack.first()

        if (isKeyField(updateParent)) {
            addValueToTree(leaderValueList, createStack)
            addValueToTree(leaderValueList, deleteStack)
        }

        // Check if it's been updated.
        val oldAndNewMatch =
            if (oldValue == null || newValue == null) false
            else if (oldValue.elementType == ModelElementType.ASSOCIATION)
                associationsMatch(oldValue as AssociationModel, newValue as AssociationModel)
            else oldValue.matches(newValue)

        if (!oldAndNewMatch || isKeyField(updateParent)) {
            addValueToTree(leaderValueList, updateStack)
            if (!oldAndNewMatch) parentInclusions.inUpdate = true
        }
        return TraversalAction.CONTINUE
    }

    private fun addValueToTree(
        leaderValueList: List<ElementModel?>,
        treeStack: ArrayDeque<MutableElementModel?>
    ) {
        val lastElement = leaderValueList.lastNotNullOf { it }
        val parent = treeStack.first() ?: throw IllegalStateException("Value parent is null")
        val newElement = parent.getOrNewValue()
        copy(lastElement, newElement)
    }

    private fun visitField(leaderFieldList: List<FieldModel?>): TraversalAction {
        val lastLeaderField = leaderFieldList.lastNotNullOf { it }
        val fieldName = getFieldName(lastLeaderField)

        val oldField = leaderFieldList.first()
        val newField = leaderFieldList.last()
        val fieldInclusions = Inclusions()
        inclusionStack.addFirst(fieldInclusions)

        val createParent = createStack.first() as MutableEntityModel?
            ?: throw IllegalStateException("Create-field parent is null")
        val createField = createParent.getOrNewField(fieldName)
        createStack.addFirst(createField)

        val deleteParent = deleteStack.first() as MutableEntityModel?
            ?: throw IllegalStateException("Delete-field parent is null")
        val deleteField = deleteParent.getOrNewField(fieldName)
        deleteStack.addFirst(deleteField)

        val updateParent = updateStack.first() as MutableEntityModel?
            ?: throw IllegalStateException("Update-field parent is null")
        val updateField = updateParent.getOrNewField(fieldName)
        updateStack.addFirst(updateField)

        if (oldField == null) {
            fieldInclusions.inCreate = true
            copy(checkNotNull(newField), createField)
            return TraversalAction.ABORT_SUB_TREE
        }
        if (newField == null) {
            fieldInclusions.inDelete = true
            copy(oldField, deleteField)
            return TraversalAction.ABORT_SUB_TREE
        }

        return TraversalAction.CONTINUE
    }

    private fun leaveField() {
        val fieldInclusions = inclusionStack.removeFirst()

        leaveField(createStack, fieldInclusions.inCreate)
        leaveField(deleteStack, fieldInclusions.inDelete)
        leaveField(updateStack, fieldInclusions.inUpdate)

        val parentInclusions = inclusionStack.first()
        parentInclusions.addChildInclusions(fieldInclusions)
    }

    private fun leaveField(stack: ArrayDeque<MutableElementModel?>, include: Boolean) {
        val outputField = stack.removeFirst() as MutableFieldModel
        if (!include && !isKeyField(outputField)) {
            val outputParent = stack.first() as MutableEntityModel
            outputParent.detachField(outputField)
        }
    }

    // Uses the difference operator to compare associations.
    private fun associationsMatch(oldAssociation: AssociationModel, newAssociation: AssociationModel): Boolean {
        val oldPathTree = oldAssociation.value
        val newPathTree = newAssociation.value

        // TODO(performance): support difference with EntityModel to avoid copying the associations

        val oldModel = entityFactory(null)
        copy(oldPathTree, oldModel)

        val newModel = entityFactory(null)
        copy(newPathTree, newModel)

        val associationsDifferenceModels = difference(oldModel, newModel, entityFactory)
        return !associationsDifferenceModels.isDifferent()
    }
}

private data class Inclusions(
    var inCreate: Boolean = false,
    var inDelete: Boolean = false,
    var inUpdate: Boolean = false
) {
    fun addChildInclusions(child: Inclusions) {
        this.inCreate = this.inCreate || child.inCreate
        this.inDelete = this.inDelete || child.inDelete
        this.inUpdate = this.inUpdate || child.inUpdate
    }
}