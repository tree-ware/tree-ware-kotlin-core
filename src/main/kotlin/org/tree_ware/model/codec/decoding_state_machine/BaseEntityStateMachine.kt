package org.tree_ware.model.codec.decoding_state_machine

import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.common.codec.SkipUnknownStateMachine
import org.tree_ware.model.core.MutableAssociationValueModel
import org.tree_ware.model.core.MutableBaseEntityModel
import org.tree_ware.schema.core.AssociationFieldSchema
import org.tree_ware.schema.core.CompositionFieldSchema
import org.tree_ware.schema.core.EntitySchema
import java.math.BigDecimal

class BaseEntityStateMachine<Aux>(
    private val base: MutableBaseEntityModel<Aux>,
    private val entitySchema: EntitySchema,
    private val stack: DecodingStack
) : AbstractDecodingStateMachine(true) {
    override fun decodeObjectStart(): Boolean {
        val fieldName = keyName
        val fieldSchema = fieldName?.let { entitySchema.getField(it) }
        if (fieldSchema == null) {
            stack.addFirst(SkipUnknownStateMachine(stack))
            return true
        }
        return when (fieldSchema) {
            is CompositionFieldSchema -> handleCompositionStart(fieldSchema)
            is AssociationFieldSchema -> handleAssociationStart(fieldSchema)
            else -> {
                resetKeyState()
                false
            }
        }
    }

    private fun handleCompositionStart(fieldSchema: CompositionFieldSchema): Boolean {
        val fieldModel = base.getOrNewCompositionField(fieldSchema.name) ?: return false
        val entity = fieldModel.value
        stack.addFirst(BaseEntityStateMachine(entity, entity.schema, stack))
        resetKeyState()
        return true
    }

    private fun handleAssociationStart(fieldSchema: AssociationFieldSchema): Boolean {
        val fieldModel = base.getOrNewAssociationField(fieldSchema.name) ?: return false
        val association = MutableAssociationValueModel<Aux>(fieldSchema)
        fieldModel.value = association
        stack.addFirst(AssociationStateMachine(association, fieldModel.schema, stack))
        resetKeyState()
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        // Remove self from stack
        stack.pollFirst()
        return true
    }

    override fun decodeListStart(): Boolean {
        try {
            val fieldName = keyName
            val fieldSchema = fieldName?.let { entitySchema.getField(it) }
            if (fieldSchema == null) {
                stack.addFirst(SkipUnknownStateMachine(stack))
                return true
            }
            val isList = fieldSchema.multiplicity.isList()
            if (!isList) return false
            val listFieldModel = base.getOrNewListField(fieldName) ?: return false
            stack.addFirst(ListFieldStateMachine(listFieldModel, stack))
            return true
        } finally {
            resetKeyState()
        }
    }

    override fun decodeListEnd(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeNullValue(): Boolean {
        try {
            val fieldName = keyName ?: return true
            val fieldModel = base.getOrNewScalarField(fieldName) ?: return false
            if (!fieldModel.setNullValue()) return false
            return true
        } finally {
            resetKeyState()
        }
    }

    override fun decodeStringValue(value: String): Boolean {
        try {
            val fieldName = keyName ?: return true
            val fieldModel = base.getOrNewScalarField(fieldName) ?: return false
            if (!fieldModel.setValue(value)) return false
            return true
        } finally {
            resetKeyState()
        }
    }

    override fun decodeNumericValue(value: BigDecimal): Boolean {
        try {
            val fieldName = keyName ?: return true
            val fieldModel = base.getOrNewScalarField(fieldName) ?: return false
            if (!fieldModel.setValue(value)) return false
            return true
        } finally {
            resetKeyState()
        }
    }

    override fun decodeBooleanValue(value: Boolean): Boolean {
        try {
            val fieldName = keyName ?: return true
            val fieldModel = base.getOrNewScalarField(fieldName) ?: return false
            if (!fieldModel.setValue(value)) return false
            return true
        } finally {
            resetKeyState()
        }
    }
}
