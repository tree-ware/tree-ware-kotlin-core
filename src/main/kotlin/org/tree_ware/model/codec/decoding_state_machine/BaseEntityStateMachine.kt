package org.tree_ware.model.codec.decoding_state_machine

import org.apache.logging.log4j.LogManager
import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.common.codec.DecodingStateMachine
import org.tree_ware.common.codec.SkipUnknownStateMachine
import org.tree_ware.model.core.MutableAssociationListFieldModel
import org.tree_ware.model.core.MutableBaseEntityModel
import org.tree_ware.model.core.MutableCompositionListFieldModel
import org.tree_ware.schema.core.AssociationFieldSchema
import org.tree_ware.schema.core.CompositionFieldSchema
import org.tree_ware.schema.core.EntitySchema
import org.tree_ware.schema.core.FieldSchema

class BaseEntityStateMachine<Aux>(
    private val isListElement: Boolean,
    private val baseFactory: () -> MutableBaseEntityModel<Aux>,
    private val stack: DecodingStack,
    private val decodeAux: Boolean
) : AbstractDecodingStateMachine(true) {
    private var base: MutableBaseEntityModel<Aux>? = null
    private var entitySchema: EntitySchema? = null
    private val logger = LogManager.getLogger()

    override fun decodeObjectStart(): Boolean {
        base = baseFactory()
        assert(base != null)
        entitySchema = base?.entitySchema
        assert(entitySchema != null)
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        if (decodeAux || !isListElement) {
            // Remove self from stack
            stack.pollFirst()
        }
        return true
    }

    override fun decodeListStart(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeListEnd(): Boolean {
        if (isListElement) {
            // End of the list needs to be handled by parent state machine.
            // So remove self from stack and call decodeListEnd() on previous
            // state machine.
            stack.pollFirst()
            val parentStateMachine = stack.peekFirst()
            if (parentStateMachine == null) {
                logger.error("No parent decoding state machine")
                return false
            }
            return parentStateMachine.decodeListEnd()
        } else {
            // This method should never get called
            assert(false)
            return false
        }
    }

    override fun decodeKey(name: String): Boolean {
        super.decodeKey(name)

        val fieldName = keyName
        val fieldSchema = fieldName?.let { entitySchema?.getField(it) }
        if (fieldSchema == null) {
            stack.addFirst(SkipUnknownStateMachine(stack))
            return true
        }
        return when (fieldSchema) {
            is CompositionFieldSchema -> handleComposition(fieldSchema)
            is AssociationFieldSchema -> handleAssociation(fieldSchema)
            else -> handlePrimitive(fieldSchema)
        }
    }

    private fun handleComposition(fieldSchema: CompositionFieldSchema): Boolean {
        val isList = fieldSchema.multiplicity.isList()
        val elementStateMachine = if (isList) {
            val listFieldModel = base?.getOrNewListField(fieldSchema.name) ?: return false
            val compositionListFieldModel = listFieldModel as? MutableCompositionListFieldModel ?: return false
            BaseEntityStateMachine(true, { compositionListFieldModel.addEntity() }, stack, decodeAux)
        } else {
            val fieldModel = base?.getOrNewCompositionField(fieldSchema.name) ?: return false
            val entity = fieldModel.value
            BaseEntityStateMachine(false, { entity }, stack, decodeAux)
        }
        addElementStateMachineToStack(elementStateMachine, isList)
        return true
    }

    private fun handleAssociation(fieldSchema: AssociationFieldSchema): Boolean {
        val isList = fieldSchema.multiplicity.isList()
        val elementStateMachine = if (isList) {
            val listFieldModel = base?.getOrNewListField(fieldSchema.name) ?: return false
            val associationListFieldModel = listFieldModel as? MutableAssociationListFieldModel ?: return false
            AssociationValueStateMachine(
                true,
                { associationListFieldModel.addAssociation() },
                fieldSchema,
                stack,
                decodeAux
            )
        } else {
            val fieldModel = base?.getOrNewAssociationField(fieldSchema.name) ?: return false
            AssociationValueStateMachine(
                false,
                { fieldModel.getOrNewAssociation() },
                fieldSchema,
                stack,
                decodeAux
            )
        }
        addElementStateMachineToStack(elementStateMachine, isList)
        return true
    }

    private fun handlePrimitive(fieldSchema: FieldSchema): Boolean {
        val isList = fieldSchema.multiplicity.isList()
        val elementStateMachine = if (isList) {
            val listFieldModel = base?.getOrNewListField(fieldSchema.name) ?: return false
            PrimitiveListValueStateMachine(listFieldModel, stack, decodeAux)
        } else {
            val fieldModel = base?.getOrNewScalarField(fieldSchema.name) ?: return false
            PrimitiveValueStateMachine(fieldModel, stack, decodeAux)
        }
        addElementStateMachineToStack(elementStateMachine, isList)
        return true
    }

    private fun addElementStateMachineToStack(elementStateMachine: DecodingStateMachine, isList: Boolean) {
        if (isList) {
            if (!decodeAux) {
                val listStateMachine = ListValueStateMachine(elementStateMachine, stack, decodeAux)
                stack.addFirst(listStateMachine)
            } else {
                // Wrap the list state machine as well as the list element state machine
                val wrappedElementStateMachine = ValueAndAuxStateMachine(true, elementStateMachine, stack)
                val listStateMachine = ListValueStateMachine(wrappedElementStateMachine, stack, decodeAux)
                val wrappedListStateMachine = ValueAndAuxStateMachine(false, listStateMachine, stack)
                stack.addFirst(wrappedListStateMachine)
            }
        } else {
            if (!decodeAux) {
                stack.addFirst(elementStateMachine)
            } else {
                val wrappedElementStateMachine = ValueAndAuxStateMachine(false, elementStateMachine, stack)
                stack.addFirst(wrappedElementStateMachine)
            }
        }
    }
}
