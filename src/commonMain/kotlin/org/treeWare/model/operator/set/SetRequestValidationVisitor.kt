package org.treeWare.model.operator.set

import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.getMaxSizeConstraint
import org.treeWare.metaModel.getMinSizeConstraint
import org.treeWare.metaModel.getRegexConstraint
import org.treeWare.model.core.*
import org.treeWare.model.operator.ElementModelError
import org.treeWare.model.operator.ModelPathStack
import org.treeWare.model.operator.set.aux.SetAux
import org.treeWare.model.operator.set.aux.SetAuxStack
import org.treeWare.model.operator.set.aux.getSetAux
import org.treeWare.model.traversal.AbstractLeader1ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.validation.validateAssociation
import org.treeWare.util.assertInDevMode

/**
 * Validates a set-request model.
 *
 * Validates the following:
 * 1. All required fields are specified.
 *    a. Keys are required fields, but since it is not possible to create a model without keys, they do not have to be
 *       validated here.
 * 2. Field constraints are met.
 * 3. Associations are valid.
 * 5. Nested set_ aux types are valid.
 * 6. Entities to be deleted do not contain child entities.
 */
class SetRequestValidationVisitor : AbstractLeader1ModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val errors = mutableListOf<ElementModelError>()

    private val modelPathStack = ModelPathStack()
    private val setAuxStack = SetAuxStack()

    override fun visitMain(leaderMain1: MainModel): TraversalAction {
        modelPathStack.pushField(leaderMain1)
        val setAuxError = setAuxStack.push(getSetAux(leaderMain1))
        assertInDevMode(setAuxError == null)
        return TraversalAction.CONTINUE
    }

    override fun leaveMain(leaderMain1: MainModel) {
        modelPathStack.popField()
        setAuxStack.pop()
        assertInDevMode(modelPathStack.isEmpty())
        assertInDevMode(setAuxStack.isEmpty())
        if (setAuxStack.nothingToSet) errors.add(
            ElementModelError("/", "set_ aux not attached to any composition field or entity")
        )
    }

    override fun visitEntity(leaderEntity1: EntityModel): TraversalAction {
        if (isCompositionKey(leaderEntity1)) {
            modelPathStack.pushEntity(leaderEntity1, true)
            setAuxStack.push(null)
            return TraversalAction.CONTINUE
        }

        modelPathStack.pushEntity(leaderEntity1)
        val entityPath = modelPathStack.peekModelPath()
        val missingKeys = modelPathStack.peekKeys().missing
        if (missingKeys.isNotEmpty()) errors.add(ElementModelError(entityPath, "missing keys: $missingKeys"))

        val setAuxError = setAuxStack.push(getSetAux(leaderEntity1), true)
        if (setAuxError != null) {
            errors.add(ElementModelError(entityPath, setAuxError))
        } else when (setAuxStack.peekActive()) {
            SetAux.CREATE -> errors.addAll(validateFieldExistence(leaderEntity1, entityPath, true, true, true))
            SetAux.UPDATE -> errors.addAll(validateFieldExistence(leaderEntity1, entityPath, false, false, true))
            SetAux.DELETE -> {}
            null -> {}
        }

        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntity1: EntityModel) {
        modelPathStack.popEntity()
        setAuxStack.pop()
    }

    override fun visitSingleField(leaderField1: SingleFieldModel): TraversalAction {
        modelPathStack.pushField(leaderField1)
        return when (getFieldType(leaderField1)) {
            FieldType.STRING -> validateSingleStringConstraints(leaderField1)
            FieldType.ASSOCIATION -> validateSingleAssociation(leaderField1)
            FieldType.COMPOSITION -> validateCompositionField(leaderField1)
            else -> TraversalAction.CONTINUE
        }
    }

    override fun leaveSingleField(leaderField1: SingleFieldModel) {
        modelPathStack.popField()
        if (getFieldType(leaderField1) == FieldType.COMPOSITION) setAuxStack.pop()
    }

    override fun visitListField(leaderField1: ListFieldModel): TraversalAction {
        modelPathStack.pushField(leaderField1)
        return when (getFieldType(leaderField1)) {
            FieldType.STRING -> validateListStringConstraints(leaderField1)
            FieldType.ASSOCIATION -> validateListAssociation(leaderField1)
            else -> TraversalAction.CONTINUE
        }
    }

    override fun leaveListField(leaderField1: ListFieldModel) {
        modelPathStack.popField()
    }

    override fun visitSetField(leaderField1: SetFieldModel): TraversalAction {
        modelPathStack.pushField(leaderField1)
        return validateCompositionField(leaderField1)
    }

    override fun leaveSetField(leaderField1: SetFieldModel) {
        modelPathStack.popField()
        setAuxStack.pop()
    }

    private fun validateSingleStringConstraints(field: SingleFieldModel): TraversalAction {
        // Skip if there are no constraints.
        val fieldMeta = field.meta ?: throw IllegalStateException("Meta-model is missing for field")
        // TODO(performance): need a single flag to avoid individual checks
        val minSize = getMinSizeConstraint(fieldMeta)?.toInt()
        val maxSize = getMaxSizeConstraint(fieldMeta)?.toInt()
        val regex = getRegexConstraint(fieldMeta)
        if (minSize == null && maxSize == null && regex == null) return TraversalAction.CONTINUE

        val fieldPath = modelPathStack.peekModelPath()
        val value = (field.value as PrimitiveModel).value as String
        validateStringValue(fieldPath, null, value, minSize, maxSize, regex)

        return TraversalAction.CONTINUE
    }

    private fun validateListStringConstraints(field: ListFieldModel): TraversalAction {
        // Skip if there are no constraints.
        val fieldMeta = field.meta ?: throw IllegalStateException("Meta-model is missing for field")
        // TODO(performance): need a single flag to avoid individual checks
        val minSize = getMinSizeConstraint(fieldMeta)?.toInt()
        val maxSize = getMaxSizeConstraint(fieldMeta)?.toInt()
        val regex = getRegexConstraint(fieldMeta)
        if (minSize == null && maxSize == null && regex == null) return TraversalAction.CONTINUE

        val fieldPath = modelPathStack.peekModelPath()
        val fieldName = getFieldName(field)
        field.values.forEachIndexed { index, elementModel ->
            val value = (elementModel as PrimitiveModel).value as String
            validateStringValue(fieldPath, index, value, minSize, maxSize, regex)
        }

        return TraversalAction.CONTINUE
    }

    private fun validateStringValue(
        fieldPath: String,
        index: Int?,
        value: String,
        minSize: Int?,
        maxSize: Int?,
        regex: String?
    ) {
        var fieldId: String? = null
        val length = value.length
        if (minSize != null && length < minSize) {
            fieldId = getFieldId(fieldPath, index)
            errors.add(
                ElementModelError(
                    fieldId,
                    "length $length of string '$value' is less than minimum size $minSize"
                )
            )
        }
        if (maxSize != null && length > maxSize) {
            fieldId = fieldId ?: getFieldId(fieldPath, index)
            errors.add(
                ElementModelError(
                    fieldId,
                    "length $length of string '$value' is more than maximum size $maxSize"
                )
            )
        }
        if (regex != null && !Regex(regex).matches(value)) {
            fieldId = fieldId ?: getFieldId(fieldPath, index)
            errors.add(ElementModelError(fieldId, "string '$value' does not match regex '$regex'"))
        }
    }

    private fun validateSingleAssociation(field: SingleFieldModel): TraversalAction {
        val value = field.value as AssociationModel
        val error = validateAssociation(value)
        error?.also {
            val fieldPath = modelPathStack.peekModelPath()
            val fieldName = getFieldName(field)
            val fieldId = getFieldId(fieldPath, null)
            errors.add(ElementModelError(fieldId, it))
        }
        return TraversalAction.CONTINUE
    }

    private fun validateListAssociation(field: ListFieldModel): TraversalAction {
        val fieldPath = modelPathStack.peekModelPath()
        val fieldName = getFieldName(field)
        field.values.forEachIndexed { index, elementModel ->
            val value = elementModel as AssociationModel
            val error = validateAssociation(value)
            error?.also {
                val fieldId = getFieldId(fieldPath, index)
                errors.add(ElementModelError(fieldId, it))
            }
        }
        return TraversalAction.CONTINUE
    }

    private fun validateCompositionField(field: FieldModel): TraversalAction {
        val setAuxError = setAuxStack.push(getSetAux(field))
        setAuxError?.also {
            val fieldPath = modelPathStack.peekModelPath()
            val fieldName = getFieldName(field)
            val fieldId = getFieldId(fieldPath, null)
            errors.add(ElementModelError(fieldId, it))
        }
        return TraversalAction.CONTINUE
    }
}

private fun getFieldId(fieldPath: String, index: Int?): String {
    val indexPart = if (index == null) "" else "[$index]"
    return "$fieldPath$indexPart"
}