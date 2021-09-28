package org.treeWare.model.decoder.stateMachine

import org.apache.logging.log4j.LogManager
import org.treeWare.metaModel.*
import org.treeWare.model.core.*
import org.treeWare.model.decoder.ModelDecoderOptions
import org.treeWare.model.decoder.OnMissingKeys

class BaseEntityStateMachine<Aux>(
    private val isSetElement: Boolean,
    private val parentSetField: MutableSetFieldModel<Aux>?,
    private val baseFactory: () -> MutableBaseEntityModel<Aux>,
    private val stack: DecodingStack,
    private val options: ModelDecoderOptions,
    private val errors: MutableList<String>,
    private val auxStateMachineFactory: () -> AuxDecodingStateMachine<Aux>?,
    private val isWildcardModel: Boolean
) : ValueDecodingStateMachine<Aux>, AbstractDecodingStateMachine(true) {
    private val auxStateMachineMap = HashMap<String, AuxDecodingStateMachine<Aux>>()

    private var base: MutableBaseEntityModel<Aux>? = null
    private var entityMeta: EntityModel<Resolved>? = null
    private val logger = LogManager.getLogger()

    override fun setAux(aux: Aux) {
        assert(base != null)
        base?.aux = aux
    }

    override fun decodeObjectStart(): Boolean {
        base = baseFactory()
        assert(base != null)
        entityMeta = base?.meta
        assert(entityMeta != null)
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
        return if (!isSetElement) {
            // Remove self from stack
            stack.pollFirst()
            true
        } else base?.let {
            try {
                parentSetField?.addValue(it)
                true
            } catch (e: MissingKeysException) {
                errors.add(e.message ?: "Missing keys")
                options.onMissingKeys == OnMissingKeys.SKIP_WITH_ERRORS
            }
        } ?: true
    }

    override fun decodeListStart(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeListEnd(): Boolean {
        if (isSetElement) {
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
        val fieldMeta = entityMeta?.let { getFieldMeta(it, fieldName) }
        if (fieldMeta == null) {
            stack.addFirst(SkipUnknownStateMachine<Aux>(stack))
            return true
        }
        return when (getFieldTypeMeta(fieldMeta)) {
            FieldType.PASSWORD1WAY -> handlePassword1way(fieldMeta)
            FieldType.PASSWORD2WAY -> handlePassword2way(fieldMeta)
            FieldType.ASSOCIATION -> handleAssociation(fieldMeta)
            FieldType.COMPOSITION -> handleComposition(fieldMeta)
            else -> handleScalar(fieldMeta)
        }
    }

    private fun handleScalar(fieldMeta: EntityModel<Resolved>): Boolean {
        val fieldModel = base?.getOrNewField(getMetaName(fieldMeta)) ?: return false
        if (isListFieldMeta(fieldMeta)) {
            val listFieldModel = fieldModel as? MutableListFieldModel<Aux> ?: return false
            val listElementStateMachine = ScalarValueModelStateMachine(
                true,
                {
                    val value = newMutableValueModel(fieldMeta, listFieldModel) as MutableScalarValueModel<Aux>
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
                    val value = newMutableValueModel(fieldMeta, singleFieldModel) as MutableScalarValueModel<Aux>
                    singleFieldModel.setValue(value)
                    value
                },
                stack
            )
            addElementStateMachineToStack(elementStateMachine)
        }
        return true
    }

    private fun handlePassword1way(fieldMeta: EntityModel<Resolved>): Boolean {
        val fieldModel = base?.getOrNewField(getMetaName(fieldMeta)) ?: return false
        if (isListFieldMeta(fieldMeta)) {
            val listFieldModel = fieldModel as? MutableListFieldModel<Aux> ?: return false
            val listElementStateMachine = Password1wayModelStateMachine(
                true,
                {
                    val value = newMutableValueModel(fieldMeta, listFieldModel) as MutablePassword1wayModel<Aux>
                    listFieldModel.addValue(value)
                    value
                },
                stack,
                auxStateMachineFactory
            )
            addListElementStateMachineToStack(listFieldModel, listElementStateMachine, isWrappedElements = false)
        } else {
            val singleFieldModel = fieldModel as? MutableSingleFieldModel<Aux> ?: return false
            val elementStateMachine = Password1wayModelStateMachine(
                false,
                {
                    val value = newMutableValueModel(fieldMeta, singleFieldModel) as MutablePassword1wayModel<Aux>
                    singleFieldModel.setValue(value)
                    value
                },
                stack,
                auxStateMachineFactory
            )
            addElementStateMachineToStack(elementStateMachine)
        }
        return true
    }

    private fun handlePassword2way(fieldMeta: EntityModel<Resolved>): Boolean {
        val fieldModel = base?.getOrNewField(getMetaName(fieldMeta)) ?: return false
        if (isListFieldMeta(fieldMeta)) {
            val listFieldModel = fieldModel as? MutableListFieldModel<Aux> ?: return false
            val listElementStateMachine = Password2wayModelStateMachine(
                true,
                {
                    val value = newMutableValueModel(fieldMeta, listFieldModel) as MutablePassword2wayModel<Aux>
                    listFieldModel.addValue(value)
                    value
                },
                stack,
                auxStateMachineFactory
            )
            addListElementStateMachineToStack(listFieldModel, listElementStateMachine, isWrappedElements = false)
        } else {
            val singleFieldModel = fieldModel as? MutableSingleFieldModel<Aux> ?: return false
            val elementStateMachine = Password2wayModelStateMachine(
                false,
                {
                    val value = newMutableValueModel(fieldMeta, singleFieldModel) as MutablePassword2wayModel<Aux>
                    singleFieldModel.setValue(value)
                    value
                },
                stack,
                auxStateMachineFactory
            )
            addElementStateMachineToStack(elementStateMachine)
        }
        return true
    }

    private fun handleAssociation(fieldMeta: EntityModel<Resolved>): Boolean {
        val fieldModel = base?.getOrNewField(getMetaName(fieldMeta)) ?: return false
        if (isListFieldMeta(fieldMeta)) {
            val listFieldModel = fieldModel as? MutableListFieldModel<Aux> ?: return false
            val listElementStateMachine = AssociationModelStateMachine(
                true,
                {
                    val value = newMutableValueModel(fieldMeta, listFieldModel) as MutableAssociationModel<Aux>
                    listFieldModel.addValue(value)
                    value
                },
                stack,
                options,
                errors,
                auxStateMachineFactory
            )
            addListElementStateMachineToStack(listFieldModel, listElementStateMachine, isWrappedElements = false)
        } else {
            val singleFieldModel = fieldModel as? MutableSingleFieldModel<Aux> ?: return false
            val elementStateMachine = AssociationModelStateMachine(
                false,
                {
                    val value = newMutableValueModel(fieldMeta, singleFieldModel) as MutableAssociationModel<Aux>
                    singleFieldModel.setValue(value)
                    value
                },
                stack,
                options,
                errors,
                auxStateMachineFactory
            )
            addElementStateMachineToStack(elementStateMachine)
        }
        return true
    }

    private fun handleComposition(fieldMeta: EntityModel<Resolved>): Boolean {
        val fieldModel = base?.getOrNewField(getMetaName(fieldMeta)) ?: return false
        if (isSetFieldMeta(fieldMeta)) {
            val setFieldModel = fieldModel as? MutableSetFieldModel<Aux> ?: return false
            val setElementStateMachine =
                BaseEntityStateMachine(
                    true,
                    setFieldModel,
                    {
                        val first = setFieldModel.firstValue()
                        if (isWildcardModel && first != null) first as MutableEntityModel<Aux>
                        else newMutableValueModel(fieldMeta, setFieldModel) as MutableEntityModel<Aux>
                    },
                    stack,
                    options,
                    errors,
                    auxStateMachineFactory,
                    isWildcardModel
                )
            addSetElementStateMachineToStack(setFieldModel, setElementStateMachine, isWrappedElements = false)
        } else {
            val singleFieldModel = fieldModel as? MutableSingleFieldModel<Aux> ?: return false
            val elementStateMachine =
                BaseEntityStateMachine(
                    false,
                    null,
                    {
                        val value = newMutableValueModel(fieldMeta, singleFieldModel) as MutableEntityModel<Aux>
                        singleFieldModel.setValue(value)
                        value
                    },
                    stack,
                    options,
                    errors,
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
        val listStateMachine = CollectionFieldModelStateMachine(listFieldModel, elementStateMachine, stack)
        stack.addFirst(listStateMachine)
    }

    private fun addSetElementStateMachineToStack(
        setFieldModel: MutableSetFieldModel<Aux>,
        setElementStateMachine: ValueDecodingStateMachine<Aux>,
        isWrappedElements: Boolean
    ) {
        val elementStateMachine =
            if (!isWrappedElements) setElementStateMachine
            else ValueAndAuxStateMachine(true, setElementStateMachine, auxStateMachineFactory, stack)
        val setStateMachine = CollectionFieldModelStateMachine(setFieldModel, elementStateMachine, stack)
        stack.addFirst(setStateMachine)
    }

    private fun addElementStateMachineToStack(elementStateMachine: ValueDecodingStateMachine<Aux>) {
        stack.addFirst(elementStateMachine)
    }
}
