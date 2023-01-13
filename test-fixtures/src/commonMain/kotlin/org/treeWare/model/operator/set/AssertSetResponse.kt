package org.treeWare.model.operator.set

import kotlin.test.assertEquals
import kotlin.test.assertIs

fun assertSetResponse(expected: SetResponse, actual: SetResponse) {
    assertEquals(expected.errorCode, actual.errorCode)
    when (expected) {
        is SetResponse.Success -> assertIs<SetResponse.Success>(actual)
        is SetResponse.ErrorList -> {
            assertIs<SetResponse.ErrorList>(actual)
            assertEquals(expected.errorList.joinToString("\n"), actual.errorList.joinToString("\n"))
        }
        is SetResponse.ErrorModel -> TODO()
    }
}