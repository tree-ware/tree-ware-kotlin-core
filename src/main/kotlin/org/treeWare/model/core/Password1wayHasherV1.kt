package org.treeWare.model.core

import org.mindrot.jbcrypt.BCrypt

class Password1wayHasherV1 {
    fun hash(unhashed: String): String = BCrypt.hashpw(unhashed, BCrypt.gensalt())
    fun verify(unhashed: String, hashed: String): Boolean = BCrypt.checkpw(unhashed, hashed)
}
