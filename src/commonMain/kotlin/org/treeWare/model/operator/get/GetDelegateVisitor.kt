package org.treeWare.model.operator.get

import org.treeWare.metaModel.FieldType
import org.treeWare.model.core.*
import org.treeWare.model.operator.ElementModelError
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.EntityPathStack
import org.treeWare.model.operator.GetEntityDelegate
import org.treeWare.model.traversal.AbstractLeader1Follower1ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.util.mergeAddToSet
import org.treeWare.util.assertInDevMode

// TODO(cleanup): create a mutable-leader version of forEach and visitor to avoid the typecasts in this file.

class GetDelegateVisitor(
    private val getDelegate: GetDelegate,
    private val entityDelegates: EntityDelegateRegistry<GetEntityDelegate>?
) : AbstractLeader1Follower1ModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val errors = mutableListOf<ElementModelError>()

    private val entityPathStack = EntityPathStack()

    override fun visitMain(leaderMain1: MainModel, followerMain1: MainModel?): TraversalAction =
        visitSingleField(leaderMain1, followerMain1)

    override fun leaveMain(leaderMain1: MainModel, followerMain1: MainModel?) {
        assertInDevMode(entityPathStack.isEmpty())
    }

    override fun visitEntity(leaderEntity1: EntityModel, followerEntity1: EntityModel?): TraversalAction {
        entityPathStack.push(leaderEntity1)
        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntity1: EntityModel, followerEntity1: EntityModel?) {
        entityPathStack.pop()
    }

    override fun visitSingleField(leaderField1: SingleFieldModel, followerField1: SingleFieldModel?): TraversalAction {
        if (followerField1 == null) return TraversalAction.CONTINUE
        if (getFieldType(followerField1) != FieldType.COMPOSITION) return TraversalAction.CONTINUE
        val requestCompositionEntity = followerField1.value as EntityModel
        if (isCompositionKey(requestCompositionEntity)) return TraversalAction.CONTINUE
        val (requestCompositionFields, requestFields) = requestCompositionEntity.fields.values.partition {
            isCompositionField(it)
        }
        val responseParentField = leaderField1 as MutableSingleFieldModel
        val fetchResult = getDelegate.fetchComposition(
            if (entityPathStack.isEmpty()) "/" else entityPathStack.peekEntityPath(),
            entityPathStack.ancestorKeys(),
            requestFields,
            responseParentField
        )
        when (fetchResult) {
            is FetchCompositionResult.Entity ->
                createResponseCompositionFields(requestCompositionFields, fetchResult.entity)
            is FetchCompositionResult.ErrorList ->
                errors.addAll(fetchResult.errorList)
        }
        return if (errors.isEmpty()) TraversalAction.CONTINUE else TraversalAction.ABORT_SUB_TREE
    }

    override fun visitSetField(leaderField1: SetFieldModel, followerField1: SetFieldModel?): TraversalAction {
        if (followerField1 == null) return TraversalAction.CONTINUE
        if (getFieldType(followerField1) != FieldType.COMPOSITION) return TraversalAction.CONTINUE
        followerField1.values.forEach { requestCompositionElement ->
            val requestCompositionEntity = requestCompositionElement as EntityModel
            val (requestCompositionFields, requestFields) = requestCompositionEntity.fields.values.partition {
                isCompositionField(it)
            }
            val requestKeys = requestCompositionEntity.getKeyFields().available
            val requestOther = requestFields.filter { !isKeyField(it) }
            val responseParentField = leaderField1 as MutableSetFieldModel
            val fetchResult = getDelegate.fetchCompositionSet(
                entityPathStack.peekEntityPath(),
                entityPathStack.ancestorKeys(),
                requestKeys,
                requestOther,
                responseParentField
            )
            when (fetchResult) {
                is FetchCompositionSetResult.Entities -> fetchResult.entities.forEach {
                    createResponseCompositionFields(requestCompositionFields, it)
                    mergeAddToSet(it, responseParentField)
                }
                is FetchCompositionSetResult.ErrorList -> errors.addAll(fetchResult.errorList)
            }
        }
        return if (errors.isEmpty()) TraversalAction.CONTINUE else TraversalAction.ABORT_SUB_TREE
    }
}

private fun createResponseCompositionFields(
    requestCompositionFields: List<FieldModel>,
    responseEntity: MutableEntityModel
) {
    requestCompositionFields.forEach { responseEntity.getOrNewField(getFieldName(it)) }
}