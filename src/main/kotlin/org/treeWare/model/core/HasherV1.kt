package org.treeWare.model.core

import org.mindrot.jbcrypt.BCrypt

class HasherV1 : Hasher {
    override val hashVersion = 1
    override fun hash(unhashed: String): String = BCrypt.hashpw(unhashed, BCrypt.gensalt())
    override fun verify(unhashed: String, hashed: String): Boolean = BCrypt.checkpw(unhashed, hashed)
}
