package org.treeWare.model.codec.decoding_state_machine

data class FieldAndAuxNames(val fieldName: String, val auxName: String?)

// TODO(deepak-nulu): should be implemented in wire-format-decoders.
// because the functionality is wire-format specific. The wire-format-decoders
// need to be passed to the state-machines (they are currently not passed).
fun getFieldAndAuxNames(name: String): FieldAndAuxNames? {
    if (!name.endsWith("_")) return FieldAndAuxNames(name, null)
    val splits = name.split("__")
    return when (splits.size) {
        1 -> FieldAndAuxNames("", name)
        2 -> {
            val (fieldName, auxTrailingUnderscore) = splits
            val auxName = if (auxTrailingUnderscore.isBlank()) null else auxTrailingUnderscore.dropLast(1)
            FieldAndAuxNames(fieldName, auxName)
        }
        else -> null
    }
}
