package org.treeWare.model.core

interface MutableMainModelFactory<O : MutableMainModel> {
    fun createInstance(): O
}
