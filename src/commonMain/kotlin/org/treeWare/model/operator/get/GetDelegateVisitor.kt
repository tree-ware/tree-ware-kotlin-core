package org.treeWare.model.operator.get

import org.treeWare.model.core.*
import org.treeWare.model.operator.ElementModelError
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.ModelPathStack
import org.treeWare.model.operator.SetEntityDelegate
import org.treeWare.model.traversal.AbstractLeader1Follower1ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.util.mergeAddToSet
import org.treeWare.util.assertInDevMode

// TODO(cleanup): create a mutable-leader version of forEach and visitor to avoid the typecasts in this file.

class GetDelegateVisitor(
    private val getDelegate: GetDelegate,
    private val setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?
) : AbstractLeader1Follower1ModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val errors = mutableListOf<ElementModelError>()

    private val modelPathStack = ModelPathStack()

    override fun visitMain(leaderMain1: MainModel, followerMain1: MainModel?): TraversalAction =
        visitSingleField(leaderMain1, followerMain1)

    override fun leaveMain(leaderMain1: MainModel, followerMain1: MainModel?) {
        modelPathStack.popField()
        assertInDevMode(modelPathStack.isEmpty())
    }

    override fun visitEntity(leaderEntity1: EntityModel, followerEntity1: EntityModel?): TraversalAction {
        modelPathStack.pushEntity(leaderEntity1)
        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntity1: EntityModel, followerEntity1: EntityModel?) {
        modelPathStack.popEntity()
    }

    override fun visitSingleField(leaderField1: SingleFieldModel, followerField1: SingleFieldModel?): TraversalAction {
        if (followerField1 == null || !isComposition(followerField1)) {
            modelPathStack.pushField(null)
            return TraversalAction.CONTINUE
        }
        val requestCompositionEntity = followerField1.value as EntityModel
        if (isCompositionKey(requestCompositionEntity)) {
            modelPathStack.pushField(null)
            return TraversalAction.CONTINUE
        }
        modelPathStack.pushField(leaderField1)
        val (requestCompositionFields, requestFields) = requestCompositionEntity.fields.values.partition {
            isComposition(it)
        }
        val responseParentField = leaderField1 as MutableSingleFieldModel
        val fetchResult = getDelegate.getComposition(
            modelPathStack.peekModelPath(),
            modelPathStack.ancestorKeys(),
            requestFields,
            responseParentField
        )
        when (fetchResult) {
            is GetCompositionResult.Entity ->
                createResponseCompositionFields(requestCompositionFields, fetchResult.entity)
            is GetCompositionResult.ErrorList ->
                errors.addAll(fetchResult.errorList)
        }
        return if (errors.isEmpty()) TraversalAction.CONTINUE else TraversalAction.ABORT_SUB_TREE
    }

    override fun leaveSingleField(leaderField1: SingleFieldModel, followerField1: SingleFieldModel?) {
        modelPathStack.popField()
    }

    override fun visitSetField(leaderField1: SetFieldModel, followerField1: SetFieldModel?): TraversalAction {
        if (followerField1 == null || !isComposition(followerField1)) {
            modelPathStack.pushField(null)
            return TraversalAction.CONTINUE
        }
        modelPathStack.pushField(leaderField1)
        followerField1.values.forEach { requestCompositionElement ->
            val requestCompositionEntity = requestCompositionElement as EntityModel
            val (requestCompositionFields, requestFields) = requestCompositionEntity.fields.values.partition {
                isComposition(it)
            }
            val requestKeys = requestCompositionEntity.getKeyFields().available
            val requestOther = requestFields.filter { !isKeyField(it) }
            val responseParentField = leaderField1 as MutableSetFieldModel
            val fetchResult = getDelegate.getCompositionSet(
                modelPathStack.peekModelPath(),
                modelPathStack.ancestorKeys(),
                requestKeys,
                requestOther,
                responseParentField
            )
            when (fetchResult) {
                is GetCompositionSetResult.Entities -> fetchResult.entities.forEach {
                    createResponseCompositionFields(requestCompositionFields, it)
                    mergeAddToSet(it, responseParentField, true)
                }
                is GetCompositionSetResult.ErrorList -> errors.addAll(fetchResult.errorList)
            }
        }
        return if (errors.isEmpty()) TraversalAction.CONTINUE else TraversalAction.ABORT_SUB_TREE
    }

    override fun leaveSetField(leaderField1: SetFieldModel, followerField1: SetFieldModel?) {
        modelPathStack.popField()
    }

    private fun isComposition(field: FieldModel): Boolean {
        if (!isCompositionField(field)) return false
        val compositionMeta = getMetaModelResolved(field.meta)?.compositionMeta
        val entityFullName = getMetaModelResolved(compositionMeta)?.fullName
        val entityDelegate = setEntityDelegates?.get(entityFullName)
        return entityDelegate?.isSingleValue() != true
    }
}

private fun createResponseCompositionFields(
    requestCompositionFields: List<FieldModel>,
    responseEntity: MutableEntityModel
) {
    requestCompositionFields.forEach { responseEntity.getOrNewField(getFieldName(it)) }
}