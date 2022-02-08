package org.treeWare.model.encoder

/**
 * Determines whether passwords should be encoded or not.
 * NOTE: associated aux will always be encoded if present.
 */
enum class EncodePasswords {
    NONE,
    HASHED_AND_ENCRYPTED,
    ALL
}
