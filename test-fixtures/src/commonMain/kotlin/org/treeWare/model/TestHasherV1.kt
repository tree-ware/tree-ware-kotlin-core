package org.treeWare.model

import org.treeWare.model.core.Hasher

private const val PREFIX = "test-hashed-"

class TestHasherV1 : Hasher {
    override val hashVersion = 1

    override fun hash(unhashed: String): String = "$PREFIX$unhashed"

    override fun verify(unhashed: String, hashed: String): Boolean = hash(unhashed) == hashed
}
