package org.treeWare.model.decoder

enum class OnMissingKeys {
    SKIP_WITH_ERRORS,
    ABORT_WITH_ERROR
}

enum class OnDuplicateKeys {
    SKIP_WITH_ERRORS,
    OVERWRITE
}

data class ModelDecoderOptions(
    val onMissingKeys: OnMissingKeys = OnMissingKeys.SKIP_WITH_ERRORS,
    val onDuplicateKeys: OnDuplicateKeys = OnDuplicateKeys.SKIP_WITH_ERRORS
)
