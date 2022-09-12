package org.treeWare.model.operator.set

import org.treeWare.metaModel.*
import org.treeWare.model.core.*
import org.treeWare.model.operator.ElementModelError
import org.treeWare.model.operator.GranularityStack
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
    private val granularityStack = GranularityStack()
    private val setAuxStack = SetAuxStack()

    override fun visitMain(leaderMain1: MainModel): TraversalAction {
        modelPathStack.pushField(leaderMain1)
        val setAuxError = setAuxStack.push(getSetAux(leaderMain1))
        assertInDevMode(setAuxError == null)
        return TraversalAction.CONTINUE
    }

    override fun leaveMain(leaderMain1: MainModel) {
        setAuxStack.pop()
        modelPathStack.popField()
        assertInDevMode(setAuxStack.isEmpty())
        assertInDevMode(granularityStack.isEmpty())
        assertInDevMode(modelPathStack.isEmpty())
        if (setAuxStack.nothingToSet) errors.add(
            ElementModelError("/", "set_ aux not attached to any composition field or entity")
        )
    }

    override fun visitEntity(leaderEntity1: EntityModel): TraversalAction {
        if (isCompositionKey(leaderEntity1)) {
            modelPathStack.pushEntity(leaderEntity1, true)
            granularityStack.push(leaderEntity1)
            setAuxStack.push(null)
            return TraversalAction.CONTINUE
        }

        modelPathStack.pushEntity(leaderEntity1)
        val entityPath = modelPathStack.peekModelPath()
        val missingKeys = modelPathStack.peekKeys().missing
        if (missingKeys.isNotEmpty()) errors.add(ElementModelError(entityPath, "missing keys: $missingKeys"))

        val previousGranularity = granularityStack.peekActive()
        granularityStack.push(leaderEntity1)
        val currentGranularity = granularityStack.peekActive()

        val setAuxError = setAuxStack.push(getSetAux(leaderEntity1), true, previousGranularity)
        if (setAuxError != null) {
            errors.add(ElementModelError(entityPath, setAuxError))
        } else when (setAuxStack.peekActive()) {
            SetAux.CREATE -> errors.addAll(validateFieldExistence(leaderEntity1, entityPath, true, true, true))
            SetAux.UPDATE -> errors.addAll(
                validateFieldExistence(
                    leaderEntity1,
                    entityPath,
                    currentGranularity == Granularity.SUB_TREE,
                    false,
                    true
                )
            )

            SetAux.DELETE -> {}
            null -> {}
        }

        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntity1: EntityModel) {
        setAuxStack.pop()
        granularityStack.pop()
        modelPathStack.popEntity()
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
        return when (getFieldType(leaderField1)) {
            FieldType.STRING -> validateListStringConstraints(leaderField1)
            FieldType.ASSOCIATION -> validateListAssociation(leaderField1)
            else -> TraversalAction.CONTINUE
        }
    }

    override fun leaveListField(leaderField1: ListFieldModel) {}

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
        val primitive = field.value as PrimitiveModel?
        if (primitive == null) {
            errors.add(ElementModelError(fieldPath, "string values must not be null in set-requests"))
        } else validateStringValue(fieldPath, primitive.value as String, minSize, maxSize, regex)

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

        field.values.forEachIndexed { index, elementModel ->
            modelPathStack.pushField(field, index)
            val fieldPath = modelPathStack.peekModelPath()
            val value = (elementModel as PrimitiveModel).value as String
            validateStringValue(fieldPath, value, minSize, maxSize, regex)
            modelPathStack.popField()
        }

        return TraversalAction.CONTINUE
    }

    private fun validateStringValue(
        fieldPath: String,
        value: String,
        minSize: Int?,
        maxSize: Int?,
        regex: String?
    ) {
        val length = value.length
        if (minSize != null && length < minSize) {
            errors.add(
                ElementModelError(fieldPath, "length $length of string '$value' is less than minimum size $minSize")
            )
        }
        if (maxSize != null && length > maxSize) {
            errors.add(
                ElementModelError(fieldPath, "length $length of string '$value' is more than maximum size $maxSize")
            )
        }
        if (regex != null && !Regex(regex).matches(value)) {
            errors.add(ElementModelError(fieldPath, "string '$value' does not match regex '$regex'"))
        }
    }

    private fun validateSingleAssociation(field: SingleFieldModel): TraversalAction {
        val value = field.value as AssociationModel
        val error = validateAssociation(value)
        error?.also {
            val fieldPath = modelPathStack.peekModelPath()
            errors.add(ElementModelError(fieldPath, it))
        }
        return TraversalAction.CONTINUE
    }

    private fun validateListAssociation(field: ListFieldModel): TraversalAction {
        field.values.forEachIndexed { index, elementModel ->
            modelPathStack.pushField(field, index)
            val fieldPath = modelPathStack.peekModelPath()
            val value = elementModel as AssociationModel
            val error = validateAssociation(value)
            error?.also {
                errors.add(ElementModelError(fieldPath, it))
            }
            modelPathStack.popField()
        }
        return TraversalAction.CONTINUE
    }

    private fun validateCompositionField(field: FieldModel): TraversalAction {
        val setAuxError = setAuxStack.push(getSetAux(field))
        setAuxError?.also {
            val fieldPath = modelPathStack.peekModelPath()
            errors.add(ElementModelError(fieldPath, it))
        }
        return TraversalAction.CONTINUE
    }
}