package org.treeWare.model.decoder.stateMachine

import org.lighthousegames.logging.logging
import org.treeWare.metaModel.*
import org.treeWare.model.core.*
import org.treeWare.model.decoder.ModelDecoderOptions
import org.treeWare.model.decoder.OnDuplicateKeys
import org.treeWare.model.decoder.OnMissingKeys
import org.treeWare.util.assertInDevMode

class BaseEntityStateMachine(
    private val parentCollectionField: MutableCollectionFieldModel?,
    private val baseFactory: () -> MutableBaseEntityModel,
    private val stack: DecodingStack,
    private val options: ModelDecoderOptions,
    private val errors: MutableList<String>,
    private val multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory,
    private val errorOnNull: String?
) : ValueDecodingStateMachine, AbstractDecodingStateMachine(true) {
    private val auxStateMachinesMap = HashMap<String, LinkedHashMap<String, AuxDecodingStateMachine>>()

    private var base: MutableBaseEntityModel? = null
    private var entityMeta: EntityModel? = null
    private val logger = logging()

    override fun setAux(auxName: String, aux: Any?) {
        if (aux == null) return
        assertInDevMode(base != null)
        base?.setAux(auxName, aux)
    }

    override fun decodeObjectStart(): Boolean {
        base = baseFactory()
        entityMeta = base?.meta
        assertInDevMode(entityMeta != null)
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        assertInDevMode(base != null)
        return handleObjectEnd()
    }

    override fun decodeListStart(): Boolean {
        // This method should never get called
        assertInDevMode(false)
        return false
    }

    override fun decodeListEnd(): Boolean {
        if (parentCollectionField != null) {
            // End of the collection needs to be handled by parent collection
            // state machine. So remove self from stack and call decodeListEnd()
            // on the previous (parent) state machine.
            stack.removeFirst()
            val parentStateMachine = stack.firstOrNull()
            if (parentStateMachine == null) {
                logger.error { "No parent decoding state machine" }
                return false
            }
            return parentStateMachine.decodeListEnd()
        } else {
            // This method should never get called
            assertInDevMode(false)
            return false
        }
    }

    override fun decodeKey(name: String): Boolean {
        super.decodeKey(name)

        val key = keyName ?: ""
        val fieldAndAuxNames = getFieldAndAuxNames(key)
        if (fieldAndAuxNames == null) {
            stack.addFirst(SkipUnknownStateMachine(stack))
            return true
        }
        val (fieldName, auxName) = fieldAndAuxNames
        if (auxName != null) {
            val auxStateMachine = multiAuxDecodingStateMachineFactory.newAuxDecodingStateMachine(auxName, stack)
            if (auxStateMachine != null) {
                val auxStateMachines = auxStateMachinesMap.getOrPut(fieldName) { LinkedHashMap() }
                auxStateMachines[auxName] = auxStateMachine
                stack.addFirst(auxStateMachine)
                auxStateMachine.newAux()
            } else stack.addFirst(SkipUnknownStateMachine(stack))
            return true
        }
        val fieldMeta = entityMeta?.let { getFieldMeta(it, fieldName) }
        if (fieldMeta == null) {
            stack.addFirst(SkipUnknownStateMachine(stack))
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

    override fun decodeNullValue(): Boolean {
        if (errorOnNull != null) {
            errors.add(errorOnNull)
            return false
        }
        return handleObjectEnd()
    }

    private fun handleObjectEnd(): Boolean {
        // TODO(deepak-nulu): set aux when found rather than on the way out.
        auxStateMachinesMap.forEach { (fieldName, auxStateMachines) ->
            auxStateMachines.forEach { (auxName, auxStateMachine) ->
                auxStateMachine.getAux()?.also {
                    if (fieldName.isEmpty()) setAux(auxName, it)
                    else base?.getField(fieldName)?.setAux(auxName, it)
                }
            }
        }
        // This state-machine instance gets reused in lists, so clear the map.
        auxStateMachinesMap.clear()
        return when (parentCollectionField?.elementType) {
            ModelElementType.SET_FIELD -> base?.let {
                try {
                    if (options.onDuplicateKeys == OnDuplicateKeys.SKIP_WITH_ERRORS) {
                        val existing = parentCollectionField.getValueMatching(it)
                        if (existing != null) {
                            errors.add("Entity with duplicate keys: ${existing.getMetaResolved()?.fullName}: ${it.getKeyValues()}")
                            return@let true
                        }
                    }
                    parentCollectionField.addValue(it)
                    true
                } catch (e: MissingKeysException) {
                    errors.add(e.message ?: "Missing keys")
                    options.onMissingKeys == OnMissingKeys.SKIP_WITH_ERRORS
                }
            } ?: true
            ModelElementType.LIST_FIELD -> true
            else -> {
                // Remove self from stack
                stack.removeFirst()
                true
            }
        }
    }

    private fun handleScalar(fieldMeta: EntityModel): Boolean {
        val fieldModel = base?.getOrNewField(getMetaName(fieldMeta)) ?: return false
        if (isListFieldMeta(fieldMeta)) {
            val listFieldModel = fieldModel as? MutableListFieldModel ?: return false
            val listElementStateMachine = ScalarValueModelStateMachine(
                true,
                {
                    val value = newMutableValueModel(fieldMeta, listFieldModel) as MutableScalarValueModel
                    listFieldModel.addValue(value)
                    value
                },
                stack
            )
            addListElementStateMachineToStack(fieldModel, listElementStateMachine, isWrappedElements = true)
        } else {
            val singleFieldModel = fieldModel as? MutableSingleFieldModel ?: return false
            val elementStateMachine = ScalarValueModelStateMachine(
                false,
                {
                    val value = newMutableValueModel(fieldMeta, singleFieldModel) as MutableScalarValueModel
                    singleFieldModel.setValue(value)
                    value
                },
                stack
            )
            addElementStateMachineToStack(elementStateMachine)
        }
        return true
    }

    private fun handlePassword1way(fieldMeta: EntityModel): Boolean {
        val fieldModel = base?.getOrNewField(getMetaName(fieldMeta)) ?: return false
        if (isListFieldMeta(fieldMeta)) {
            val listFieldModel = fieldModel as? MutableListFieldModel ?: return false
            val listElementStateMachine = Password1wayModelStateMachine(
                true,
                {
                    val value = newMutableValueModel(fieldMeta, listFieldModel) as MutablePassword1wayModel
                    listFieldModel.addValue(value)
                    value
                },
                stack,
                multiAuxDecodingStateMachineFactory
            )
            addListElementStateMachineToStack(listFieldModel, listElementStateMachine, isWrappedElements = false)
        } else {
            val singleFieldModel = fieldModel as? MutableSingleFieldModel ?: return false
            val elementStateMachine = Password1wayModelStateMachine(
                false,
                {
                    val value = newMutableValueModel(fieldMeta, singleFieldModel) as MutablePassword1wayModel
                    singleFieldModel.setValue(value)
                    value
                },
                stack,
                multiAuxDecodingStateMachineFactory
            )
            addElementStateMachineToStack(elementStateMachine)
        }
        return true
    }

    private fun handlePassword2way(fieldMeta: EntityModel): Boolean {
        val fieldModel = base?.getOrNewField(getMetaName(fieldMeta)) ?: return false
        if (isListFieldMeta(fieldMeta)) {
            val listFieldModel = fieldModel as? MutableListFieldModel ?: return false
            val listElementStateMachine = Password2wayModelStateMachine(
                true,
                {
                    val value = newMutableValueModel(fieldMeta, listFieldModel) as MutablePassword2wayModel
                    listFieldModel.addValue(value)
                    value
                },
                stack,
                multiAuxDecodingStateMachineFactory
            )
            addListElementStateMachineToStack(listFieldModel, listElementStateMachine, isWrappedElements = false)
        } else {
            val singleFieldModel = fieldModel as? MutableSingleFieldModel ?: return false
            val elementStateMachine = Password2wayModelStateMachine(
                false,
                {
                    val value = newMutableValueModel(fieldMeta, singleFieldModel) as MutablePassword2wayModel
                    singleFieldModel.setValue(value)
                    value
                },
                stack,
                multiAuxDecodingStateMachineFactory
            )
            addElementStateMachineToStack(elementStateMachine)
        }
        return true
    }

    private fun handleAssociation(fieldMeta: EntityModel): Boolean {
        val fieldModel = base?.getOrNewField(getMetaName(fieldMeta)) ?: return false
        if (isListFieldMeta(fieldMeta)) {
            val listFieldModel = fieldModel as? MutableListFieldModel ?: return false
            val listElementStateMachine = BaseEntityStateMachine(
                listFieldModel,
                {
                    val association = newMutableValueModel(fieldMeta, listFieldModel) as MutableAssociationModel
                    listFieldModel.addValue(association)
                    association.value
                },
                stack,
                options,
                errors,
                multiAuxDecodingStateMachineFactory,
                null
            )
            addListElementStateMachineToStack(listFieldModel, listElementStateMachine, isWrappedElements = false)
        } else {
            val singleFieldModel = fieldModel as? MutableSingleFieldModel ?: return false
            val elementStateMachine = BaseEntityStateMachine(
                null,
                {
                    val association = newMutableValueModel(fieldMeta, singleFieldModel) as MutableAssociationModel
                    singleFieldModel.setValue(association)
                    association.value
                },
                stack,
                options,
                errors,
                multiAuxDecodingStateMachineFactory,
                null
            )
            addElementStateMachineToStack(elementStateMachine)
        }
        return true
    }

    private fun handleComposition(fieldMeta: EntityModel): Boolean {
        val fieldModel = base?.getOrNewField(getMetaName(fieldMeta)) ?: return false
        if (isSetFieldMeta(fieldMeta)) {
            val setFieldModel = fieldModel as? MutableSetFieldModel ?: return false
            val setElementStateMachine = BaseEntityStateMachine(
                setFieldModel,
                { newMutableValueModel(fieldMeta, setFieldModel) as MutableEntityModel },
                stack,
                options,
                errors,
                multiAuxDecodingStateMachineFactory,
                "Entities must not be null; use empty object {} instead"
            )
            addSetElementStateMachineToStack(setFieldModel, setElementStateMachine)
        } else {
            val singleFieldModel = fieldModel as? MutableSingleFieldModel ?: return false
            val elementStateMachine = BaseEntityStateMachine(
                null,
                {
                    val value = newMutableValueModel(fieldMeta, singleFieldModel) as MutableEntityModel
                    singleFieldModel.setValue(value)
                    value
                },
                stack,
                options,
                errors,
                multiAuxDecodingStateMachineFactory,
                "Entities must not be null; use empty object {} instead"
            )
            addElementStateMachineToStack(elementStateMachine)
        }
        return true
    }

    private fun addListElementStateMachineToStack(
        listFieldModel: MutableListFieldModel,
        listElementStateMachine: ValueDecodingStateMachine,
        isWrappedElements: Boolean
    ) {
        val elementStateMachine =
            if (!isWrappedElements) listElementStateMachine
            else ValueAndAuxStateMachine(true, listElementStateMachine, stack, multiAuxDecodingStateMachineFactory)
        val listStateMachine = CollectionFieldModelStateMachine(listFieldModel, elementStateMachine, stack, errors)
        stack.addFirst(listStateMachine)
    }

    private fun addSetElementStateMachineToStack(
        setFieldModel: MutableSetFieldModel,
        setElementStateMachine: ValueDecodingStateMachine
    ) {
        val setStateMachine = CollectionFieldModelStateMachine(setFieldModel, setElementStateMachine, stack, errors)
        stack.addFirst(setStateMachine)
    }

    private fun addElementStateMachineToStack(elementStateMachine: ValueDecodingStateMachine) {
        stack.addFirst(elementStateMachine)
    }
}