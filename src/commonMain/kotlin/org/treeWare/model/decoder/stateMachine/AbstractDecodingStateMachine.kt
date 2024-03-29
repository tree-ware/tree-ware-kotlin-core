package org.treeWare.model.decoder.stateMachine

abstract class AbstractDecodingStateMachine(private val defaultReturn: Boolean) : DecodingStateMachine {
    protected var keyName: String? = null

    protected open fun setKeyState(keyName: String) {
        this.keyName = keyName
    }

    override fun decodeKey(name: String): Boolean {
        setKeyState(name)
        return defaultReturn
    }

    override fun decodeNullValue(): Boolean {
        return defaultReturn
    }

    override fun decodeStringValue(value: String): Boolean {
        return defaultReturn
    }

    override fun decodeNumericValue(value: String): Boolean {
        return defaultReturn
    }

    override fun decodeBooleanValue(value: Boolean): Boolean {
        return defaultReturn
    }
}