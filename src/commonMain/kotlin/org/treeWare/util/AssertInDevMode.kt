package org.treeWare.util

expect fun assertInDevMode(value: Boolean)
expect inline fun assertInDevMode(value: Boolean, lazyMessage: () -> Any)