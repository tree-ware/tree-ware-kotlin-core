package org.treeWare.model.core

import kotlin.test.Test

class SetFieldModelTests {
    @Test
    fun `Finding set elements must succeed`() {
        val hashMap = LinkedHashMap<ElementModelId, MutableElementModel<Unit>>()
        
    }
}

private fun newMutableSetFieldModel(): MutableSetFieldModel<Unit> {
    val dummyMain = MutableMainModel<Unit>(null)
    val dummyRoot = MutableRootModel(null, dummyMain)
    return MutableSetFieldModel(null, dummyRoot)
}
