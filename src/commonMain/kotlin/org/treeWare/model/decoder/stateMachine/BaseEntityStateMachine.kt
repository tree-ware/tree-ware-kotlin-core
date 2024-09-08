package org.treeWare.model.decoder.stateMachine

import org.lighthousegames.logging.logging
import org.treeWare.model.core.*
import org.treeWare.model.decoder.ModelDecoderOptions
import org.treeWare.model.decoder.OnDuplicateKeys
import org.treeWare.model.decoder.OnMissingKeys
import org.treeWare.util.assertInDevMode

class BaseEntityStateMachine(
    private val parentCollectionField: MutableCollectionFieldModel?,
    private val baseFactory: () -> MutableEntityModel,
    private val stack: DecodingStack,
    private val options: ModelDecoderOptions,
    private val errors: MutableList<String>,
    private val multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory,
    private val errorOnNull: String?
) : ValueDecodingStateMachine, AbstractDecodingStateMachine(true) {
    private val auxStateMachinesMap = HashMap<String, LinkedHashMap<String, AuxDecodingStateMachine>>()

    private var base: MutableEntityModel? = null
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
        val field = base?.getOrNewField(fieldName) ?: return false
        val fieldStateMachine = getFieldStateMachine(field, errors, stack, options, multiAuxDecodingStateMachineFactory)
        stack.addFirst(fieldStateMachine)
        return true
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
            else -> {
                // Remove self from stack
                stack.removeFirst()
                true
            }
        }
    }
}