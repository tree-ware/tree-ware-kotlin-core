package org.treeWare.model.operator

import org.treeWare.metaModel.*
import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeader1MutableModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.mutableForEach

// TODO(deepak-nulu): handle self-referential compositions and longer cycles.

typealias AuxSetter = (entity: MutableBaseEntityModel) -> Unit

/**
 * Populate the sub-tree of the specified entity.
 *
 * @return `false` if sub-tree is not empty and could not be populated, `true` if sub-tree is successfully populated.
 */
fun populateSubTree(
    entity: MutableBaseEntityModel,
    populateNonKeyNonCompositionFields: Boolean,
    auxSetter: AuxSetter? = null
): Boolean {
    if (!entity.hasOnlyKeyFields()) return false
    val visitor = PopulateSubTreeVisitor(populateNonKeyNonCompositionFields, auxSetter)
    mutableForEach(entity, visitor, false)
    return true
}

private class PopulateSubTreeVisitor(
    private val populateNonKeyNonCompositionFields: Boolean,
    private val auxSetter: AuxSetter?
) : AbstractLeader1MutableModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    override fun visitMutableEntity(leaderEntity1: MutableEntityModel): TraversalAction {
        populateKeyFields(leaderEntity1)
        populateNonKeyFields(leaderEntity1, populateNonKeyNonCompositionFields)
        auxSetter?.also { it(leaderEntity1) }
        return TraversalAction.CONTINUE
    }
}

private fun populateKeyFields(entity: MutableEntityModel) {
    val entityMeta = requireNotNull(entity.meta)
    val keyFieldsMeta = getKeyFieldsMeta(entityMeta)
    populateFields(entity, keyFieldsMeta)
}

private fun populateNonKeyFields(entity: MutableEntityModel, populateNonKeyNonCompositionFields: Boolean) {
    val entityMeta = requireNotNull(entity.meta)
    val nonKeyFields = getFieldsMeta(entityMeta).values
        .filterIsInstance<EntityModel>()
        .filter { !isKeyFieldMeta(it) }
        .filter { getMetaModelResolved(it)?.compositionMeta != entityMeta } // drop self-referential compositions
    val fields =
        if (populateNonKeyNonCompositionFields) nonKeyFields else nonKeyFields.filter { isCompositionFieldMeta(it) }
    populateFields(entity, fields)
}

private fun populateFields(entity: MutableEntityModel, fieldsMeta: List<EntityModel>) {
    fieldsMeta.forEach { fieldMeta ->
        val field = entity.getOrNewField(getMetaName(fieldMeta))
        if (isCompositionFieldMeta(fieldMeta)) {
            val childEntity = field.getNewValue() as MutableEntityModel
            populateKeyFields(childEntity)
            if (isSetFieldMeta(fieldMeta)) {
                (field as MutableSetFieldModel).addValue(childEntity)
            }
        }
    }
}