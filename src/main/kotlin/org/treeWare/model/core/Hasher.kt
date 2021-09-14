package org.treeWare.model.core

interface Hasher {
    val hashVersion: Int
    fun hash(unhashed: String): String
    fun verify(unhashed: String, hashed: String): Boolean
}
