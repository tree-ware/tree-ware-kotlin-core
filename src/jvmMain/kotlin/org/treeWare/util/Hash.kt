package org.treeWare.util

import java.util.*

actual fun hash(values: List<Any?>): Int = Objects.hash(*values.toTypedArray())