package org.treeWare.metaModel

import org.treeWare.metaModel.validation.validate
import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.core.Resolved
import org.treeWare.model.getMainModelFromJsonFile
import org.treeWare.model.operator.union

val ADDRESS_BOOK_META_MODEL_FILES = listOf(
    "metaModel/address_book_root.json",
    "metaModel/address_book_main.json",
    "metaModel/address_book_city.json",
)

fun newAddressBookMetaModel(hasher: Hasher?, cipher: Cipher?): MutableMainModel<Resolved> {
    val metaMetaModel = newMainMetaMetaModel()
    val metaModelParts = ADDRESS_BOOK_META_MODEL_FILES.map { getMainModelFromJsonFile<Resolved>(metaMetaModel, it) }
    val metaModel = union(metaModelParts)
    val errors = validate(metaModel, hasher, cipher)
    if (errors.isNotEmpty()) throw IllegalStateException("Address-book meta-model is not valid")
    return metaModel
}
