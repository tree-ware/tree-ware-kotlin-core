package org.treeWare.util

actual typealias BigInteger = java.math.BigInteger

actual fun toBigInteger(value: String): BigInteger = value.toBigInteger()