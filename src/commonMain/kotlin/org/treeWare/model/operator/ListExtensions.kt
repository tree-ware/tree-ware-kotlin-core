package org.treeWare.model.operator

fun <T, R : Any> List<T>.lastNotNullOf(transform: (T) -> R?): R = lastNotNullOfOrNull(transform)
    ?: throw NoSuchElementException("No element of the list was transformed to a non-null value.")

fun <T, R : Any> List<T>.lastNotNullOfOrNull(transform: (T) -> R?): R? {
    val iterator = this.listIterator(this.size)
    while (iterator.hasPrevious()) {
        val result = transform(iterator.previous())
        if (result != null) return result
    }
    return null
}
