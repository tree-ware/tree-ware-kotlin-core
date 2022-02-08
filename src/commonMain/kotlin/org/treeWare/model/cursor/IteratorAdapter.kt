package org.treeWare.model.cursor

class IteratorAdapter<T, R>(
    private val adapteeFactory: () -> Iterator<T>,
    private val transform: (T) -> R
) : Iterator<R> {
    private var _adaptee: Iterator<T>? = null
    private val adaptee: Iterator<T> get() = _adaptee ?: adapteeFactory().also { _adaptee = it }
    override fun hasNext(): Boolean = adaptee.hasNext()
    override fun next(): R = transform(adaptee.next())
}
