package org.treeWare.model

import org.treeWare.metaModel.addressBookRootEntityMeta
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableEntityModelFactory

object AddressBookMutableEntityModelFactory : MutableEntityModelFactory {
    override fun create(): MutableEntityModel {
        return MutableEntityModel(addressBookRootEntityMeta, null)
    }
}