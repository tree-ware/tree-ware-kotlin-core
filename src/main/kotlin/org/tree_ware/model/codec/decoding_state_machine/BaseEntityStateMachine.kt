package org.tree_ware.model.codec.decoding_state_machine

import org.apache.logging.log4j.LogManager
import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.common.codec.SkipUnknownStateMachine
import org.tree_ware.model.core.MutableAssociationListFieldModel
import org.tree_ware.model.core.MutableBaseEntityModel
import org.tree_ware.model.core.MutableCompositionListFieldModel
import org.tree_ware.model.core.MutableListFieldModel
import org.tree_ware.schema.core.AssociationFieldSchema
import org.tree_ware.schema.core.CompositionFieldSchema
import org.tree_ware.schema.core.EntitySchema
import org.tree_ware.schema.core.FieldSchema

class BaseEntityStateMachine<Aux>(
    private val isListElement: Boolean,
    private val baseFactory: () -> MutableBaseEntityModel<Aux>,
    private val stack: DecodingStack,
    private val auxStateMachine: AuxDecodingStateMachine<Aux>?
) : ValueDecodingStateMachine<Aux>, AbstractDecodingStateMachine(true) {
    private var base: MutableBaseEntityModel<Aux>? = null
    private var entitySchema: EntitySchema? = null
    private val logger = LogManager.getLogger()

    override fun setAux(aux: Aux) {
        assert(base != null)
        base?.aux = aux
    }

    override fun decodeObjectStart(): Boolean {
        base = baseFactory()
        assert(base != null)
        entitySchema = base?.entitySchema
        assert(entitySchema != null)
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        if (auxStateMachine != null || !isListElement) {
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
            stack.addFirst(SkipUnknownStateMachine<Aux>(stack))
            return true
        }
        return when (fieldSchema) {
            is CompositionFieldSchema -> handleComposition(fieldSchema)
            is AssociationFieldSchema -> handleAssociation(fieldSchema)
            else -> handlePrimitive(fieldSchema)
        }
    }

    private fun handleComposition(fieldSchema: CompositionFieldSchema): Boolean {
        if (fieldSchema.multiplicity.isList()) {
            val listFieldModel = base?.getOrNewListField(fieldSchema.name) ?: return false
            val compositionListFieldModel = listFieldModel as? MutableCompositionListFieldModel ?: return false
            val listElementStateMachine =
                BaseEntityStateMachine(true, { compositionListFieldModel.addEntity() }, stack, auxStateMachine)
            addListElementStateMachineToStack(listFieldModel, listElementStateMachine)
        } else {
            val fieldModel = base?.getOrNewCompositionField(fieldSchema.name) ?: return false
            val entity = fieldModel.value
            val elementStateMachine = BaseEntityStateMachine(false, { entity }, stack, auxStateMachine)
            addElementStateMachineToStack(elementStateMachine)
        }
        return true
    }

    private fun handleAssociation(fieldSchema: AssociationFieldSchema): Boolean {
        if (fieldSchema.multiplicity.isList()) {
            val listFieldModel = base?.getOrNewListField(fieldSchema.name) ?: return false
            val associationListFieldModel = listFieldModel as? MutableAssociationListFieldModel ?: return false
            val listElementStateMachine = AssociationValueStateMachine(
                true,
                { associationListFieldModel.addAssociation() },
                fieldSchema,
                stack,
                auxStateMachine
            )
            addListElementStateMachineToStack(listFieldModel, listElementStateMachine)
        } else {
            val fieldModel = base?.getOrNewAssociationField(fieldSchema.name) ?: return false
            val elementStateMachine = AssociationValueStateMachine(
                false,
                { fieldModel.getOrNewAssociation() },
                fieldSchema,
                stack,
                auxStateMachine
            )
            addElementStateMachineToStack(elementStateMachine)
        }
        return true
    }

    private fun handlePrimitive(fieldSchema: FieldSchema): Boolean {
        if (fieldSchema.multiplicity.isList()) {
            val listFieldModel = base?.getOrNewListField(fieldSchema.name) ?: return false
            val listElementStateMachine = PrimitiveListValueStateMachine(listFieldModel, stack, auxStateMachine)
            addListElementStateMachineToStack(listFieldModel, listElementStateMachine)
        } else {
            val fieldModel = base?.getOrNewScalarField(fieldSchema.name) ?: return false
            val elementStateMachine = PrimitiveValueStateMachine(fieldModel, stack)
            addElementStateMachineToStack(elementStateMachine)
        }
        return true
    }

    private fun addListElementStateMachineToStack(
        listFieldModel: MutableListFieldModel<Aux>,
        listElementStateMachine: ValueDecodingStateMachine<Aux>
    ) {
        if (auxStateMachine == null) {
            val listStateMachine = ListValueStateMachine(listFieldModel, listElementStateMachine, stack)
            stack.addFirst(listStateMachine)
        } else {
            // Wrap the list state machine as well as the list element state machine
            val wrappedElementStateMachine =
                ValueAndAuxStateMachine(true, listElementStateMachine, auxStateMachine, stack)
            val listStateMachine = ListValueStateMachine(listFieldModel, wrappedElementStateMachine, stack)
            val wrappedListStateMachine = ValueAndAuxStateMachine(false, listStateMachine, auxStateMachine, stack)
            stack.addFirst(wrappedListStateMachine)
        }
    }

    private fun addElementStateMachineToStack(elementStateMachine: ValueDecodingStateMachine<Aux>) {
        if (auxStateMachine == null) {
            stack.addFirst(elementStateMachine)
        } else {
            val wrappedElementStateMachine =
                ValueAndAuxStateMachine(false, elementStateMachine, auxStateMachine, stack)
            stack.addFirst(wrappedElementStateMachine)
        }
    }
}
