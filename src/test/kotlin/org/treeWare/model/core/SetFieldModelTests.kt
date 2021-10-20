package org.treeWare.model.core

import kotlin.test.Test

class SetFieldModelTests {
    @Test
    fun `Finding set elements must succeed`() {
        val hashMap = LinkedHashMap<ElementModelId, MutableElementModel>()

    }
}

private fun newMutableSetFieldModel(): MutableSetFieldModel {
    val dummyMain = MutableMainModel(null)
    val dummyRoot = MutableRootModel(null, dummyMain)
    return MutableSetFieldModel(null, dummyRoot)
}
