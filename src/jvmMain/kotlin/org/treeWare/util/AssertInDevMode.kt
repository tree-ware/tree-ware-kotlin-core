package org.treeWare.util

actual fun assertInDevMode(value: Boolean) = assert(value)
actual inline fun assertInDevMode(value: Boolean, lazyMessage: () -> Any) = assert(value, lazyMessage)