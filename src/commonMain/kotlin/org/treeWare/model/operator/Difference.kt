package org.treeWare.model.operator

import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeaderManyModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import org.treeWare.util.assertInDevMode

fun difference(
    oldModel: MainModel,
    newModel: MainModel,
    mutableMainModelFactory: MutableMainModelFactory
): DifferenceModels {
    val differenceVisitor = DifferenceVisitor(mutableMainModelFactory)
    forEach(listOf(oldModel, newModel), differenceVisitor, false)
    return differenceVisitor.output
}

private class DifferenceVisitor(
    private val mutableMainModelFactory: MutableMainModelFactory
) : AbstractLeaderManyModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val output: DifferenceModels = newDifferenceModels(mutableMainModelFactory)

    private val createStack = ArrayDeque<MutableElementModel?>()
    private val deleteStack = ArrayDeque<MutableElementModel?>()
    private val updateStack = ArrayDeque<MutableElementModel?>()

    private val inclusionStack = ArrayDeque<Inclusions>()

    override fun visitMain(leaderMainList: List<MainModel?>): TraversalAction {
        createStack.addFirst(output.createModel)
        deleteStack.addFirst(output.deleteModel)
        updateStack.addFirst(output.updateModel)
        inclusionStack.addFirst(Inclusions())
        return TraversalAction.CONTINUE
    }

    override fun leaveMain(leaderMainList: List<MainModel?>) {
        val createMain = createStack.removeFirst() as MutableMainModel
        assertInDevMode(createStack.isEmpty())
        if (createMain.isEmpty()) {
            createMain.setValue(null) // ensure null root instead of empty root
        }

        val deleteMain = deleteStack.removeFirst() as MutableMainModel
        assertInDevMode(deleteStack.isEmpty())
        if (deleteMain.isEmpty()) {
            deleteMain.setValue(null) // ensure null root instead of empty root
        }

        val updateMain = updateStack.removeFirst() as MutableMainModel
        assertInDevMode(updateStack.isEmpty())
        if (updateMain.isEmpty()) {
            updateMain.setValue(null) // ensure null root instead of empty root
        }
    }

    override fun visitEntity(leaderEntityList: List<EntityModel?>): TraversalAction {
        val oldEntity = leaderEntityList.first()
        val newEntity = leaderEntityList.last()

        val entityInclusions = Inclusions()
        inclusionStack.addFirst(entityInclusions)

        val createParent = createStack.first() ?: throw IllegalStateException("Create-entity parent is null")
        val createEntity = createParent.getOrNewValue()
        createStack.addFirst(createEntity)

        val deleteParent = deleteStack.first() ?: throw IllegalStateException("Delete-entity parent is null")
        val deleteEntity = deleteParent.getOrNewValue()
        deleteStack.addFirst(deleteEntity)

        val updateParent = updateStack.first() ?: throw IllegalStateException("Update-entity parent is null")
        val updateEntity = updateParent.getOrNewValue()
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

        val parentInclusions = inclusionStack.first()
        parentInclusions.addChildInclusions(entityInclusions)
    }

    override fun visitSingleField(leaderFieldList: List<SingleFieldModel?>): TraversalAction =
        visitField(leaderFieldList)

    override fun leaveSingleField(leaderFieldList: List<SingleFieldModel?>) =
        leaveField()

    override fun visitListField(leaderFieldList: List<ListFieldModel?>): TraversalAction =
        visitField(leaderFieldList)

    override fun leaveListField(leaderFieldList: List<ListFieldModel?>) =
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

        val createParent = createStack.first() as MutableBaseEntityModel?
            ?: throw IllegalStateException("Create-field parent is null")
        val createField = createParent.getOrNewField(fieldName)
        createStack.addFirst(createField)

        val deleteParent = deleteStack.first() as MutableBaseEntityModel?
            ?: throw IllegalStateException("Delete-field parent is null")
        val deleteField = deleteParent.getOrNewField(fieldName)
        deleteStack.addFirst(deleteField)

        val updateParent = updateStack.first() as MutableBaseEntityModel?
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

        if (updateField.elementType == ModelElementType.LIST_FIELD) {
            if (!listsMatch(oldField as ListFieldModel, newField as ListFieldModel)) {
                fieldInclusions.inUpdate = true
                copy(newField, updateField)
                return TraversalAction.ABORT_SUB_TREE
            }
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
            val outputParent = stack.first() as MutableBaseEntityModel
            outputParent.detachField(outputField)
        }
    }

    private fun listsMatch(oldList: ListFieldModel, newList: ListFieldModel): Boolean {
        val oldSize = oldList.values.size
        val newSize = newList.values.size
        if (oldSize != newSize) return false
        if (oldSize == 0) return true

        val oldElement = oldList.values[0]
        return if (oldElement.elementType == ModelElementType.ASSOCIATION) associationListsMatch(oldList, newList)
        else oldList.matches(newList)
    }

    private fun associationListsMatch(oldList: ListFieldModel, newList: ListFieldModel): Boolean {
        if (oldList.values.size != newList.values.size) return false
        oldList.values.forEachIndexed { index, oldElement ->
            val oldAssociation = oldElement as AssociationModel
            val newAssociation = newList.values[index] as AssociationModel
            if (!associationsMatch(oldAssociation, newAssociation)) return false
        }
        return true
    }

    // Uses the difference operator to compare associations.
    private fun associationsMatch(oldAssociation: AssociationModel, newAssociation: AssociationModel): Boolean {
        val oldPathTree = oldAssociation.value
        val newPathTree = newAssociation.value

        // TODO(performance): support difference with EntityModel to avoid copying the associations

        val oldModel = mutableMainModelFactory.getNewInstance()
        val oldRoot = oldModel.getOrNewRoot()
        copy(oldPathTree, oldRoot)

        val newModel = mutableMainModelFactory.getNewInstance()
        val newRoot = newModel.getOrNewRoot()
        copy(newPathTree, newRoot)

        val associationsDifferenceModels = difference(oldModel, newModel, mutableMainModelFactory)
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