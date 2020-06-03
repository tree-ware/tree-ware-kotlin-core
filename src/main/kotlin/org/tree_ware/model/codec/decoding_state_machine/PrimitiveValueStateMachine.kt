package org.tree_ware.model.codec.decoding_state_machine

import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.model.core.MutableScalarFieldModel
import java.math.BigDecimal

class PrimitiveValueStateMachine<Aux>(
    private val field: MutableScalarFieldModel<Aux>,
    private val stack: DecodingStack
) : ValueDecodingStateMachine<Aux>, AbstractDecodingStateMachine(true) {
    override fun setAux(aux: Aux) {
        field.aux = aux
    }

    override fun decodeObjectStart(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeObjectEnd(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeListStart(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeListEnd(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeNullValue(): Boolean {
        try {
            return field.setNullValue()
        } finally {
            // Remove self from stack
            stack.pollFirst()
        }
    }

    override fun decodeStringValue(value: String): Boolean {
        try {
            return field.setValue(value)
        } finally {
            // Remove self from stack
            stack.pollFirst()
        }
    }

    override fun decodeNumericValue(value: BigDecimal): Boolean {
        try {
            return field.setValue(value)
        } finally {
            // Remove self from stack
            stack.pollFirst()
        }
    }

    override fun decodeBooleanValue(value: Boolean): Boolean {
        try {
            return field.setValue(value)
        } finally {
            // Remove self from stack
            stack.pollFirst()
        }
    }
}
