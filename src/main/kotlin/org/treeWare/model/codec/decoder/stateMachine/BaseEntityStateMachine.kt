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
    private var entityMeta: EntityModel<Resolved>? = null
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
        entityMeta = base?.meta
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
        val fieldMeta = entityMeta?.let { getFieldMeta(it, fieldName) }
        return when (fieldSchema) {
            is CompositionFieldSchema -> handleComposition(fieldSchema, fieldMeta)
            is AssociationFieldSchema -> handleAssociation(fieldSchema, fieldMeta)
            else -> handleScalar(fieldSchema, fieldMeta)
        }
    }

    private fun handleScalar(fieldSchema: FieldSchema, fieldMeta: EntityModel<Resolved>?): Boolean {
        val fieldModel = base?.getOrNewField(fieldSchema.name) ?: return false
        if (fieldSchema.multiplicity.isList()) {
            val listFieldModel = fieldModel as? MutableListFieldModel<Aux> ?: return false
            val listElementStateMachine = ScalarValueModelStateMachine(
                true,
                {
                    val value =
                        newMutableValueModel(fieldSchema, fieldMeta, listFieldModel) as MutableScalarValueModel<Aux>
                    listFieldModel.addValue(value)
                    value
                },
                stack
            )
            addListElementStateMachineToStack(fieldModel, listElementStateMachine, isWrappedElements = true)
        } else {
            val singleFieldModel = fieldModel as? MutableSingleFieldModel<Aux> ?: return false
            val elementStateMachine = ScalarValueModelStateMachine(
                false,
                {
                    val value =
                        newMutableValueModel(fieldSchema, fieldMeta, singleFieldModel) as MutableScalarValueModel<Aux>
                    singleFieldModel.setValue(value)
                    value
                },
                stack
            )
            addElementStateMachineToStack(elementStateMachine)
        }
        return true
    }

    private fun handleAssociation(fieldSchema: AssociationFieldSchema, fieldMeta: EntityModel<Resolved>?): Boolean {
        val fieldModel = base?.getOrNewField(fieldSchema.name) ?: return false
        if (fieldSchema.multiplicity.isList()) {
            val listFieldModel = fieldModel as? MutableListFieldModel<Aux> ?: return false
            val listElementStateMachine = AssociationModelStateMachine(
                true,
                {
                    val value =
                        newMutableValueModel(fieldSchema, fieldMeta, listFieldModel) as MutableAssociationModel<Aux>
                    listFieldModel.addValue(value)
                    value
                },
                fieldSchema,
                stack,
                auxStateMachineFactory
            )
            addListElementStateMachineToStack(listFieldModel, listElementStateMachine, isWrappedElements = false)
        } else {
            val singleFieldModel = fieldModel as? MutableSingleFieldModel<Aux> ?: return false
            val elementStateMachine = AssociationModelStateMachine(
                false,
                {
                    val value =
                        newMutableValueModel(fieldSchema, fieldMeta, singleFieldModel) as MutableAssociationModel<Aux>
                    singleFieldModel.setValue(value)
                    value
                },
                fieldSchema,
                stack,
                auxStateMachineFactory
            )
            addElementStateMachineToStack(elementStateMachine)
        }
        return true
    }

    private fun handleComposition(fieldSchema: CompositionFieldSchema, fieldMeta: EntityModel<Resolved>?): Boolean {
        val fieldModel = base?.getOrNewField(fieldSchema.name) ?: return false
        if (fieldSchema.multiplicity.isList()) {
            val listFieldModel = fieldModel as? MutableListFieldModel<Aux> ?: return false
            val listElementStateMachine =
                BaseEntityStateMachine(
                    true,
                    {
                        val first = listFieldModel.firstValue()
                        if (isWildcardModel && first != null) first as MutableEntityModel<Aux>
                        else {
                            val value = newMutableValueModel(
                                fieldSchema.resolvedEntity,
                                fieldMeta?.let { getResolvedEntityMeta(fieldMeta) },
                                listFieldModel
                            ) as MutableEntityModel<Aux>
                            listFieldModel.addValue(value)
                            value
                        }
                    },
                    stack,
                    auxStateMachineFactory,
                    isWildcardModel
                )
            addListElementStateMachineToStack(listFieldModel, listElementStateMachine, isWrappedElements = false)
        } else {
            val singleFieldModel = fieldModel as? MutableSingleFieldModel<Aux> ?: return false
            val elementStateMachine =
                BaseEntityStateMachine(
                    false,
                    {
                        val value = newMutableValueModel(
                            fieldSchema.resolvedEntity,
                            fieldMeta?.let { getResolvedEntityMeta(fieldMeta) },
                            singleFieldModel
                        ) as MutableEntityModel<Aux>
                        singleFieldModel.setValue(value)
                        value
                    },
                    stack,
                    auxStateMachineFactory,
                    isWildcardModel
                )
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
