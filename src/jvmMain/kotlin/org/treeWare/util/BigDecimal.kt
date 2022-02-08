package org.treeWare.util

actual typealias BigDecimal = java.math.BigDecimal

actual fun toBigDecimal(value: String): BigDecimal = value.toBigDecimal()