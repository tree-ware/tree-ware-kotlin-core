package org.treeWare.metaModel

import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher
import org.treeWare.model.core.MainModel

val ADDRESS_BOOK_META_MODEL_FILES = listOf(
    "metaModel/address_book_root.json",
    "metaModel/address_book_main.json",
    "metaModel/address_book_city.json",
)

fun newAddressBookMetaModel(hasher: Hasher?, cipher: Cipher?): MainModel = newMetaModel(
    ADDRESS_BOOK_META_MODEL_FILES,
    false,
    hasher,
    cipher,
    emptyList()
)