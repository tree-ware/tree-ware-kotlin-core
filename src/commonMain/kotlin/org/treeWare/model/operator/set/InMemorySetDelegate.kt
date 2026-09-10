package org.treeWare.model.operator.set

import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.FieldModel
import org.treeWare.model.core.Keys
import org.treeWare.model.core.MissingKeysException
import org.treeWare.model.core.ModelElementType
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableSetFieldModel
import org.treeWare.model.core.MutableSingleFieldModel
import org.treeWare.model.core.SingleFieldModel
import org.treeWare.model.core.getEntityFieldName
import org.treeWare.model.core.getFieldName
import org.treeWare.model.core.getOrNewMutableSetField
import org.treeWare.model.core.getOrNewMutableSingleEntity
import org.treeWare.model.core.isKeyField
import org.treeWare.model.core.isRootEntity
import org.treeWare.model.operator.ElementModelError
import org.treeWare.model.operator.ErrorCode
import org.treeWare.model.operator.Response
import org.treeWare.model.operator.copy
import org.treeWare.model.operator.set.aux.SetAux

/**
 * A [SetDelegate] that applies set-operations to an in-memory mutable model.
 *
 * The model to be updated is passed in as a constructor parameter and is updated in-place as
 * [setEntity] calls are received. Composition child entities are handled by separate [setEntity]
 * calls from [org.treeWare.model.operator.set], so only keys, associations and other
 * (non-composition) fields are copied for each entity.
 */
class InMemorySetDelegate(private val target: MutableEntityModel) : SetDelegate {
    override fun begin(): Response = Response.Success

    override fun setEntity(
        setAux: SetAux,
        entity: EntityModel,
        fieldPath: String,
        entityPath: String,
        ancestorKeys: List<Keys>,
        keys: List<SingleFieldModel>,
        associations: List<FieldModel>,
        other: List<FieldModel>
    ): Response = try {
        if (isRootEntity(entity)) setRootEntity(setAux, entityPath, associations, other)
        else setNonRootEntity(setAux, entity, entityPath, associations, other)
    } catch (e: MissingKeysException) {
        error(entityPath, e.message ?: "missing keys")
    } catch (e: IllegalStateException) {
        error(entityPath, e.message ?: "unable to set entity")
    }

    override fun end(): Response = Response.Success

    // Helpers

    private fun setRootEntity(
        setAux: SetAux,
        entityPath: String,
        associations: List<FieldModel>,
        other: List<FieldModel>
    ): Response {
        when (setAux) {
            SetAux.CREATE, SetAux.UPDATE -> copyFields(target, associations + other)
            SetAux.DELETE -> target.detachAllFields()
        }
        return Response.Success
    }

    private fun setNonRootEntity(
        setAux: SetAux,
        entity: EntityModel,
        entityPath: String,
        associations: List<FieldModel>,
        other: List<FieldModel>
    ): Response {
        val requestParentField = entity.parent
            ?: return error(entityPath, "entity has no parent field")
        val fieldName = getEntityFieldName(entity)
            ?: return error(entityPath, "entity has no field name")
        val requestParentEntity = requestParentField.parent
            ?: return error(entityPath, "parent field has no parent entity")
        val targetParent = findTargetEntity(requestParentEntity)
            ?: // A delete for a missing ancestor is a no-op: the entity is already gone.
            return if (setAux == SetAux.DELETE) Response.Success
            else error(entityPath, "parent entity not found")
        return if (requestParentField.elementType == ModelElementType.SET_FIELD) {
            setSetEntity(setAux, entity, entityPath, targetParent, fieldName, associations, other)
        } else {
            setSingleEntity(setAux, entity, entityPath, targetParent, fieldName, associations, other)
        }
    }

    private fun setSetEntity(
        setAux: SetAux,
        entity: EntityModel,
        entityPath: String,
        targetParent: MutableEntityModel,
        fieldName: String,
        associations: List<FieldModel>,
        other: List<FieldModel>
    ): Response {
        val targetSetField = targetParent.getField(fieldName) as? MutableSetFieldModel
        when (setAux) {
            SetAux.CREATE -> {
                if (targetSetField?.getValueMatching(entity) != null)
                    return error(entityPath, "entity already exists")
                val setField = targetSetField ?: getOrNewMutableSetField(targetParent, fieldName)
                val newEntity = setField.getOrNewValue() as MutableEntityModel
                copyKeysAndFields(newEntity, entity, associations, other)
                setField.addValue(newEntity)
            }
            SetAux.UPDATE -> {
                val existing = targetSetField?.getValueMatching(entity) as? MutableEntityModel
                    ?: return error(entityPath, "entity not found")
                copyFields(existing, associations + other)
            }
            SetAux.DELETE -> {
                // A delete for a missing entity is a no-op: the entity is already gone.
                // This also handles children of an already-deleted parent.
                if (targetSetField == null) return Response.Success
                targetSetField.removeValue(entity)
            }
        }
        return Response.Success
    }

    private fun setSingleEntity(
        setAux: SetAux,
        entity: EntityModel,
        entityPath: String,
        targetParent: MutableEntityModel,
        fieldName: String,
        associations: List<FieldModel>,
        other: List<FieldModel>
    ): Response {
        when (setAux) {
            SetAux.CREATE -> {
                val existing = targetParent.getField(fieldName) as? MutableSingleFieldModel
                if (existing?.value != null) return error(entityPath, "entity already exists")
                val newEntity = getOrNewMutableSingleEntity(targetParent, fieldName)
                copyKeysAndFields(newEntity, entity, associations, other)
            }
            SetAux.UPDATE -> {
                val existing = targetParent.getField(fieldName) as? MutableSingleFieldModel
                val existingEntity = existing?.value as? MutableEntityModel
                    ?: return error(entityPath, "entity not found")
                copyFields(existingEntity, associations + other)
            }
            SetAux.DELETE -> {
                val existing = targetParent.getField(fieldName) as? MutableSingleFieldModel
                // A delete for a missing entity is a no-op: the entity is already gone.
                existing?.detachFromParent()
            }
        }
        return Response.Success
    }

    /** Finds the target entity matching the specified request entity, or null if it is missing. */
    private fun findTargetEntity(requestEntity: EntityModel): MutableEntityModel? {
        if (isRootEntity(requestEntity)) return target
        // Build the path from the request root to the request entity using parent links.
        val chain = ArrayDeque<EntityModel>()
        var current: EntityModel? = requestEntity
        while (current != null) {
            chain.addFirst(current)
            current = current.parent?.parent
        }
        var targetCurrent = target
        // The first element is the request root, which maps to the target root.
        for (index in 1 until chain.size) {
            val requestChild = chain[index]
            val fieldName = getEntityFieldName(requestChild) ?: return null
            val targetField = targetCurrent.getField(fieldName) ?: return null
            targetCurrent = when (targetField.elementType) {
                ModelElementType.SINGLE_FIELD ->
                    (targetField as? MutableSingleFieldModel)?.value as? MutableEntityModel
                        ?: return null
                ModelElementType.SET_FIELD -> try {
                    (targetField as? MutableSetFieldModel)?.getValueMatching(requestChild) as? MutableEntityModel
                } catch (e: MissingKeysException) {
                    null
                } ?: return null
                else -> return null
            }
        }
        return targetCurrent
    }

    /**
     * Copies top-level key fields from the request entity along with the specified
     * association and other fields into a new target entity. Key fields are copied from the
     * request entity (rather than the flattened `keys` list) so that nested composition
     * keys are preserved.
     */
    private fun copyKeysAndFields(
        targetEntity: MutableEntityModel,
        requestEntity: EntityModel,
        associations: List<FieldModel>,
        other: List<FieldModel>
    ) {
        requestEntity.fields.values.forEach { requestField ->
            if (isKeyField(requestField)) copyField(targetEntity, requestField)
        }
        copyFields(targetEntity, associations + other)
    }

    private fun copyFields(targetEntity: MutableEntityModel, requestFields: List<FieldModel>) {
        requestFields.forEach { requestField -> copyField(targetEntity, requestField) }
    }

    private fun copyField(targetEntity: MutableEntityModel, requestField: FieldModel) {
        copy(requestField, targetEntity.getOrNewField(getFieldName(requestField)))
    }

    private fun error(entityPath: String, message: String): Response =
        Response.ErrorList(ErrorCode.CLIENT_ERROR, listOf(ElementModelError(entityPath, message)))
}
