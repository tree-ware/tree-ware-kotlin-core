package org.treeWare.metaModel

import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher

val ADDRESS_BOOK_META_MODEL_FILES = listOf(
    "metaModel/address_book_root.json",
    "metaModel/address_book_main.json",
    "metaModel/address_book_city.json",
)

fun newAddressBookMetaModel(hasher: Hasher?, cipher: Cipher?): ValidatedMetaModel = newMetaModelFromJsonFiles(
    ADDRESS_BOOK_META_MODEL_FILES,
    false,
    hasher,
    cipher,
    emptyList(),
    true
)

val addressBookMetaModel = newAddressBookMetaModel(null, null).metaModel
    ?: throw IllegalStateException("Meta-model has validation errors")