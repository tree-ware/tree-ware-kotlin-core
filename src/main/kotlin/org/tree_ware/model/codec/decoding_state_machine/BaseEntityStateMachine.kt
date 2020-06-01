package org.tree_ware.model.codec.decoding_state_machine

import org.apache.logging.log4j.LogManager
import org.tree_ware.common.codec.AbstractDecodingStateMachine
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
        if (!isListElement) {
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
        if (isList) {
            val listFieldModel = base?.getOrNewListField(fieldSchema.name) ?: return false
            val compositionListFieldModel = listFieldModel as? MutableCompositionListFieldModel ?: return false
            val listElementStateMachine =
                BaseEntityStateMachine(true, { compositionListFieldModel.addEntity() }, stack, decodeAux)
            stack.addFirst(ListValueStateMachine(listElementStateMachine, stack, decodeAux))
        } else {
            val fieldModel = base?.getOrNewCompositionField(fieldSchema.name) ?: return false
            val entity = fieldModel.value
            stack.addFirst(BaseEntityStateMachine(false, { entity }, stack, decodeAux))
        }
        return true
    }

    private fun handleAssociation(fieldSchema: AssociationFieldSchema): Boolean {
        val isList = fieldSchema.multiplicity.isList()
        if (isList) {
            val listFieldModel = base?.getOrNewListField(fieldSchema.name) ?: return false
            val associationListFieldModel = listFieldModel as? MutableAssociationListFieldModel ?: return false
            val listElementStateMachine = AssociationValueStateMachine(
                true,
                { associationListFieldModel.addAssociation() },
                fieldSchema,
                stack,
                decodeAux
            )
//                AssociationListValueStateMachine(associationListFieldModel, fieldSchema, stack, decodeAux)
            stack.addFirst(ListValueStateMachine(listElementStateMachine, stack, decodeAux))
        } else {
            val fieldModel = base?.getOrNewAssociationField(fieldSchema.name) ?: return false
//            val association = MutableAssociationValueModel<Aux>(fieldSchema)
//            fieldModel.value = association
            stack.addFirst(
                AssociationValueStateMachine(
                    false,
                    { fieldModel.getOrNewAssociation() },
                    fieldSchema,
                    stack,
                    decodeAux
                )
            )
        }
        return true
    }

    private fun handlePrimitive(fieldSchema: FieldSchema): Boolean {
        val isList = fieldSchema.multiplicity.isList()
        if (isList) {
            val listFieldModel = base?.getOrNewListField(fieldSchema.name) ?: return false
            val listElementStateMachine = PrimitiveListValueStateMachine(listFieldModel, stack, decodeAux)
            stack.addFirst(ListValueStateMachine(listElementStateMachine, stack, decodeAux))
        } else {
            val fieldModel = base?.getOrNewScalarField(fieldSchema.name) ?: return false
            stack.addFirst(PrimitiveValueStateMachine(fieldModel, stack, decodeAux))
        }
        return true
    }
}
