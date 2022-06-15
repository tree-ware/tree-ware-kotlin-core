package org.treeWare.model.operator

import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeaderManyModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import org.treeWare.util.assertInDevMode


/*First input should be original, second input should be new*/
fun difference(oldModel: MainModel, newModel: MainModel): DifferenceModels {
    val differenceVisitor = DifferenceVisitor()
    forEach(listOf(oldModel, newModel), differenceVisitor, false)
    return differenceVisitor.getModels()
}

private class DifferenceVisitor : AbstractLeaderManyModelVisitor<TraversalAction>(
    TraversalAction.CONTINUE
) {
    val createStack = ArrayDeque<MutableElementModel>()
    val deleteStack = ArrayDeque<MutableElementModel>()
    val updateStack = ArrayDeque<MutableElementModel>()
    val inclusionStack = ArrayDeque<InclusionData>() //holds data which trees each element should be added to
    lateinit var createMainModel: MutableMainModel
    lateinit var deleteMainModel: MutableMainModel
    lateinit var updateMainModel: MutableMainModel

    fun getModels(giveNullIfEmpty: Boolean = true): DifferenceModels {
        /*If a result tree is empty, then instead make the value of the tree null*/
        val createModel = when {
            inclusionStack.first().inCreate || !giveNullIfEmpty -> createMainModel
            else -> null
        }
        val deleteModel = when {
            inclusionStack.first().inDelete || !giveNullIfEmpty -> deleteMainModel
            else -> null
        }
        val updateModel = when {
            inclusionStack.first().inUpdate || !giveNullIfEmpty -> updateMainModel
            else -> null
        }
        return DifferenceModels(createModel, deleteModel, updateModel)
    }

    override fun visitMain(leaderMainList: List<MainModel?>): TraversalAction {
        createMainModel = MutableMainModel(leaderMainList.last()?.mainMeta)//what needs to be created
        deleteMainModel = MutableMainModel(leaderMainList.last()?.mainMeta)//what needs to be deleted
        updateMainModel = MutableMainModel(leaderMainList.last()?.mainMeta)//what needs to be updated

        createStack.addFirst(createMainModel)
        deleteStack.addFirst(deleteMainModel)
        updateStack.addFirst(updateMainModel)

        inclusionStack.addFirst(InclusionData())

        return TraversalAction.CONTINUE
    }

    override fun leaveMain(leaderMainList: List<MainModel?>) {
        createStack.removeFirst()
        assertInDevMode(createStack.isEmpty())
        deleteStack.removeFirst()
        assertInDevMode(deleteStack.isEmpty())
        updateStack.removeFirst()
        assertInDevMode(updateStack.isEmpty())
    }

    override fun visitEntity(leaderEntityList: List<EntityModel?>): TraversalAction {

        val includeEntity = InclusionData()
        inclusionStack.addFirst(includeEntity)
        val oldEntity = leaderEntityList.first()
        val newEntity = leaderEntityList.last()

        /**===== Build new entities for the trees =====**/
        val createParent = createStack.first()
        val createEntity = newDisconnectedValue(createParent)
        createStack.addFirst(createEntity)

        val deleteParent = deleteStack.first()
        val deleteEntity = newDisconnectedValue(deleteParent)
        deleteStack.addFirst(deleteEntity)

        val updateParent = updateStack.first()
        val updateEntity = newDisconnectedValue(updateParent)
        updateStack.addFirst(updateEntity)

        /**===== Check if it's absent in the original =====**/
        if (oldEntity == null) { //if the original doesn't have the entity:
            includeEntity.inCreate = true
            copy(checkNotNull(newEntity), createEntity)
            return TraversalAction.ABORT_SUB_TREE
        }

        /**===== Check if it's absent in the new version ====**/
        else if (newEntity == null) {//if the new version doesn't have the entity:
            includeEntity.inDelete = true
            copy(oldEntity, deleteEntity)
            return TraversalAction.ABORT_SUB_TREE
        }

        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntityList: List<EntityModel?>) {
        // NOTE: entities should be created to set-fields only after the entity
        // has key fields.

        val includeElement = inclusionStack.removeFirst()

        /**===== Handle the create tree =====**/
        val createEntity = createStack.removeFirst() as MutableEntityModel
        if (includeElement.inCreate) { //Case: In or above a created node.
            val createParent = createStack.first()
            connectValue(createParent, createEntity)
        }

        /**===== Handle the delete tree =====**/
        val deleteEntity = deleteStack.removeFirst() as MutableEntityModel
        if (includeElement.inDelete) { //Case: In or above a deleted node
            val deleteParent = deleteStack.first()
            connectValue(deleteParent, deleteEntity)
        }

        /**===== Handle the update tree =====**/
        val updateEntity = updateStack.removeFirst() as MutableEntityModel
        if (includeElement.inUpdate) { //Case: In or above a deleted node
            val updateParent = updateStack.first()
            connectValue(updateParent, updateEntity)
        }
        inclusionStack.first().addChildInclusions(includeElement)
    }

    override fun visitSingleField(leaderFieldList: List<SingleFieldModel?>): TraversalAction =
        visitField(leaderFieldList)

    override fun leaveSingleField(leaderFieldList: List<SingleFieldModel?>) =
        leaveField(leaderFieldList)

    override fun visitListField(leaderFieldList: List<ListFieldModel?>): TraversalAction =
        visitField(leaderFieldList)

    override fun leaveListField(leaderFieldList: List<ListFieldModel?>) =
        leaveField(leaderFieldList)

    override fun visitSetField(leaderFieldList: List<SetFieldModel?>): TraversalAction =
        visitField(leaderFieldList)

    override fun leaveSetField(leaderFieldList: List<SetFieldModel?>) =
        leaveField(leaderFieldList)


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


    private fun visitValue(
        leaderValueList: List<ElementModel?>
    ): TraversalAction {
        val createParent = createStack.first() as MutableFieldModel
        val deleteParent = deleteStack.first() as MutableFieldModel
        val updateParent = updateStack.first() as MutableFieldModel
        val oldValue = leaderValueList.first()
        val newValue = leaderValueList.last()

        val parentInclusion = inclusionStack.first()
        /**===== Check if it's absent in the original =====**/
        if (oldValue == null || isKeyField(createParent)) {
            addValueToTree(leaderValueList, createStack)
            if (oldValue == null) parentInclusion.inCreate = true
        }

        /**===== Check if it's absent in the new version =====**/
        if (newValue == null || isKeyField(deleteParent)) {
            addValueToTree(leaderValueList, deleteStack)
            if (newValue == null) parentInclusion.inDelete = true
        }

        /**===== Check if it's been updated =====**/
        val oldAndNewMatch =
            if (oldValue?.elementType != ModelElementType.ASSOCIATION && oldValue != null && newValue != null)
                oldValue.matches(newValue)
            else true

        if (!oldAndNewMatch || isKeyField(updateParent)) {
            addValueToTree(leaderValueList, updateStack)
            if(!oldAndNewMatch) parentInclusion.inUpdate = true
        }
        return TraversalAction.CONTINUE
    }

    private fun addValueToTree(
        leaderValueList: List<ElementModel?>,
        treeStack: ArrayDeque<MutableElementModel>
    ) {
        val lastElement = leaderValueList.lastNotNullOf { it }
        val parent = treeStack.first()
        val newElement = newChildValue(parent)
        copy(lastElement, newElement)
    }


    private fun visitField(leaderFieldList: List<FieldModel?>): TraversalAction {
        val lastLeaderField = leaderFieldList.lastNotNullOf { it }
        val lastLeaderFieldName = getFieldName(lastLeaderField)
        val elementInclusion = InclusionData()
        inclusionStack.addFirst(elementInclusion)
        val oldField = leaderFieldList.first()
        val newField = leaderFieldList.last()

        val createParent = createStack.first() as MutableBaseEntityModel
        val createField = newDisconnectedField(createParent, lastLeaderFieldName)
        createStack.addFirst(createField)

        val deleteParent = deleteStack.first() as MutableBaseEntityModel
        val deleteField = newDisconnectedField(deleteParent, lastLeaderFieldName)
        deleteStack.addFirst(deleteField)

        val updateParent = updateStack.first() as MutableBaseEntityModel
        val updateField = newDisconnectedField(updateParent, lastLeaderFieldName)
        updateStack.addFirst(updateField)

        /**===== Check if it's absent in the original =====**/
        if (oldField == null) { //if the original doesn't have the entity:
            elementInclusion.inCreate = true //this should be in the create tree
            copy(checkNotNull(newField), createField)
            return TraversalAction.ABORT_SUB_TREE
        }

        /**===== Check if it's absent in the new version =====**/
        if (newField == null) {//if the new version doesn't have the entity:
            elementInclusion.inDelete = true //this should be in the delete tree
            copy(oldField, deleteField)
            return TraversalAction.ABORT_SUB_TREE
        }

        return TraversalAction.CONTINUE
    }

    private fun leaveField(leaderFieldList: List<FieldModel?>) {
        val elementInclusion = inclusionStack.removeFirst()

        /**===== Handle the create tree =====**/
        val createField = createStack.removeFirst() as MutableFieldModel
        if ((elementInclusion.inCreate) || isKeyField(createField)) { //Case: In or above a created node
            val createParent = createStack.first() as MutableBaseEntityModel
            //(createField as MutableSingleFieldModel).setValue()
            connectField(createParent, createField)
        }

        /**===== Handle the delete tree =====**/
        val deleteField = deleteStack.removeFirst() as MutableFieldModel
        if ((elementInclusion.inDelete) || isKeyField(deleteField)) { //Case: In or above a deleted node
            val deleteParent = deleteStack.first() as MutableBaseEntityModel
            connectField(deleteParent, deleteField)
        }

        /**===== Handle the update tree =====**/
        val updateField = updateStack.removeFirst() as MutableFieldModel
        if ((elementInclusion.inUpdate) || isKeyField(updateField)) { //Case: In or above an updated node
            val updateParent = updateStack.first() as MutableBaseEntityModel
            connectField(updateParent, updateField)
        }

        inclusionStack.first().addChildInclusions(elementInclusion)
    }
}

private data class InclusionData(
    var inCreate: Boolean = false,
    var inDelete: Boolean = false,
    var inUpdate: Boolean = false
) {
    fun addChildInclusions(child: InclusionData) {
        this.inCreate = this.inCreate || child.inCreate
        this.inDelete = this.inDelete || child.inDelete
        this.inUpdate = this.inUpdate || child.inUpdate
    }
}