package org.treeWare.model.codec.decoder.stateMachine

import org.apache.logging.log4j.LogManager
import org.treeWare.common.codec.AbstractDecodingStateMachine
import org.treeWare.common.codec.SkipUnknownStateMachine
import org.treeWare.model.core.*
import org.treeWare.schema.core.AssociationFieldSchema
import org.treeWare.schema.core.CompositionFieldSchema
import org.treeWare.schema.core.EntitySchema
import org.treeWare.schema.core.FieldSchema

class BaseEntityStateMachine<Aux>(
    private val isListElement: Boolean,
    private val baseFactory: () -> MutableBaseEntityModel<Aux>,
    private val stack: DecodingStack,
    private val auxStateMachineFactory: () -> AuxDecodingStateMachine<Aux>?,
    private val isWildcardModel: Boolean
) : ValueDecodingStateMachine<Aux>, AbstractDecodingStateMachine(true) {
    private val auxStateMachineMap = HashMap<String, AuxDecodingStateMachine<Aux>>()

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
        assert(base != null)
        // TODO(deepak-nulu): set aux when found rather than on the way out.
        auxStateMachineMap.forEach { (fieldName, auxStateMachine) ->
            if (fieldName.isEmpty()) base?.aux = auxStateMachine.getAux()
            else base?.getField(fieldName)?.aux = auxStateMachine.getAux()
        }
        // This state-machine instance gets reused in lists, so clear the map.
        auxStateMachineMap.clear()
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

        val key = keyName ?: ""
        val fieldAndAuxNames = getFieldAndAuxNames(key)
        if (fieldAndAuxNames == null) {
            stack.addFirst(SkipUnknownStateMachine<Aux>(stack))
            return true
        }
        val (fieldName, auxName) = fieldAndAuxNames
        if (auxName != null) {
            val auxStateMachine = auxStateMachineFactory()
            if (auxStateMachine != null) {
                auxStateMachineMap[fieldName] = auxStateMachine
                stack.addFirst(auxStateMachine)
                auxStateMachine.newAux()
            } else stack.addFirst(SkipUnknownStateMachine<Aux>(stack))
            return true
        }
        val fieldSchema = fieldName.let { entitySchema?.getField(it) }
        if (fieldSchema == null) {
            stack.addFirst(SkipUnknownStateMachine<Aux>(stack))
            return true
        }
        return when (fieldSchema) {
            is CompositionFieldSchema -> handleComposition(fieldSchema)
            is AssociationFieldSchema -> handleAssociation(fieldSchema)
            else -> handleScalar(fieldSchema)
        }
    }

    private fun handleScalar(fieldSchema: FieldSchema): Boolean {
        if (fieldSchema.multiplicity.isList()) {
            val listFieldModel = base?.getOrNewListField(fieldSchema.name) ?: return false
            val scalarListFieldModel = listFieldModel as? MutableScalarListFieldModel ?: return false
            val listElementStateMachine = ScalarFieldModelStateMachine(
                true,
                { scalarListFieldModel.addElement() },
                stack,
                auxStateMachineFactory
            )
            addListElementStateMachineToStack(listFieldModel, listElementStateMachine, isWrappedElements = true)
        } else {
            val fieldModel = base?.getOrNewScalarField(fieldSchema.name) ?: return false
            val elementStateMachine = ScalarFieldModelStateMachine(false, { fieldModel }, stack, auxStateMachineFactory)
            addElementStateMachineToStack(elementStateMachine)
        }
        return true
    }

    private fun handleAssociation(fieldSchema: AssociationFieldSchema): Boolean {
        if (fieldSchema.multiplicity.isList()) {
            val listFieldModel = base?.getOrNewListField(fieldSchema.name) ?: return false
            val associationListFieldModel = listFieldModel as? MutableAssociationListFieldModel ?: return false
            val listElementStateMachine = AssociationFieldModelStateMachine(
                true,
                { associationListFieldModel.addAssociation() },
                fieldSchema,
                stack,
                auxStateMachineFactory
            )
            addListElementStateMachineToStack(listFieldModel, listElementStateMachine, isWrappedElements = false)
        } else {
            val fieldModel = base?.getOrNewAssociationField(fieldSchema.name) ?: return false
            val elementStateMachine = AssociationFieldModelStateMachine(
                false,
                { fieldModel },
                fieldSchema,
                stack,
                auxStateMachineFactory
            )
            addElementStateMachineToStack(elementStateMachine)
        }
        return true
    }

    private fun handleComposition(fieldSchema: CompositionFieldSchema): Boolean {
        if (fieldSchema.multiplicity.isList()) {
            val listFieldModel = base?.getOrNewListField(fieldSchema.name) ?: return false
            val compositionListFieldModel = listFieldModel as? MutableCompositionListFieldModel ?: return false
            val listElementStateMachine =
                BaseEntityStateMachine(
                    true,
                    {
                        val first = compositionListFieldModel.first()
                        if (isWildcardModel && first != null) first else compositionListFieldModel.addEntity()
                    },
                    stack,
                    auxStateMachineFactory,
                    isWildcardModel
                )
            addListElementStateMachineToStack(listFieldModel, listElementStateMachine, isWrappedElements = false)
        } else {
            val fieldModel = base?.getOrNewCompositionField(fieldSchema.name) ?: return false
            val entity = fieldModel.value
            val elementStateMachine =
                BaseEntityStateMachine(false, { entity }, stack, auxStateMachineFactory, isWildcardModel)
            addElementStateMachineToStack(elementStateMachine)
        }
        return true
    }

    private fun addListElementStateMachineToStack(
        listFieldModel: MutableListFieldModel<Aux>,
        listElementStateMachine: ValueDecodingStateMachine<Aux>,
        isWrappedElements: Boolean
    ) {
        val elementStateMachine =
            if (!isWrappedElements) listElementStateMachine
            else ValueAndAuxStateMachine(true, listElementStateMachine, auxStateMachineFactory, stack)
        val listStateMachine = ListFieldModelStateMachine(listFieldModel, elementStateMachine, stack)
        stack.addFirst(listStateMachine)
    }

    private fun addElementStateMachineToStack(elementStateMachine: ValueDecodingStateMachine<Aux>) {
        stack.addFirst(elementStateMachine)
    }
}
