package org.treeWare.model

import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.core.MutableMainModelFactory

object AddressBookMutableMainModelFactory : MutableMainModelFactory {
    override fun getNewInstance(): MutableMainModel {
        return MutableMainModel(addressBookMetaModel)
    }
}
