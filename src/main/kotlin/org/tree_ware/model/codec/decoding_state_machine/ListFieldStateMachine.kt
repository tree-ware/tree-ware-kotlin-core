package org.tree_ware.model.codec.decoding_state_machine

import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.common.codec.SkipUnknownStateMachine
import org.tree_ware.model.core.MutableAssociationListFieldModel
import org.tree_ware.model.core.MutableCompositionListFieldModel
import org.tree_ware.model.core.MutableListFieldModel
import java.math.BigDecimal

class ListFieldStateMachine(
    private val listField: MutableListFieldModel,
    private val stack: DecodingStack
) : AbstractDecodingStateMachine(true) {
    override fun decodeObjectStart(): Boolean {
        return when (listField) {
            is MutableCompositionListFieldModel -> handleCompositionStart(listField)
            is MutableAssociationListFieldModel -> handleAssociationStart(listField)
            else -> {
                resetKeyState()
                false
            }
        }
    }

    private fun handleCompositionStart(compositionListField: MutableCompositionListFieldModel): Boolean {
        val entity = compositionListField.addEntity()
        stack.addFirst(BaseEntityStateMachine(entity, entity.schema, stack))
        resetKeyState()
        return true
    }

    private fun handleAssociationStart(associationListField: MutableAssociationListFieldModel): Boolean {
        val association = associationListField.addAssociation()
        stack.addFirst(AssociationStateMachine(association, associationListField.schema, stack))
        resetKeyState()
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeListStart(): Boolean {
        stack.addFirst(SkipUnknownStateMachine(stack))
        resetKeyState()
        return true
    }

    override fun decodeListEnd(): Boolean {
        // Remove self from stack
        stack.pollFirst()
        return true
    }

    override fun decodeStringValue(value: String): Boolean {
        try {
            if (!listField.addValue(value)) return false
            return true
        } finally {
            resetKeyState()
        }
    }

    override fun decodeNumericValue(value: BigDecimal): Boolean {
        try {
            if (!listField.addValue(value)) return false
            return true
        } finally {
            resetKeyState()
        }
    }

    override fun decodeBooleanValue(value: Boolean): Boolean {
        try {
            if (!listField.addValue(value)) return false
            return true
        } finally {
            resetKeyState()
        }
    }
}
