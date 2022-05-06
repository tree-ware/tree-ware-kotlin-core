package org.treeWare.model.cursor

class SingleValueIteratorAdapter<T, R>(
    private val valueFactory: () -> T?,
    private val transform: (T) -> R
) : Iterator<R> {
    private var isUsed = false
    private var isFetched = false
    private var _value: T? = null
    private val value: T?
        get() = if (isFetched) _value else {
            _value = valueFactory()
            isFetched = true
            _value
        }

    override fun hasNext(): Boolean = !isUsed && value != null
    override fun next(): R {
        isUsed = true
        return value?.let { transform(it) } ?: throw IllegalStateException("Null value in iterator next()")
    }
}