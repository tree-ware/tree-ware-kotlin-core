package org.treeWare.model.operator.set

import org.treeWare.model.operator.Response
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

fun assertSetResponse(expected: Response, actual: Response) {
    assertEquals(expected.errorCode, actual.errorCode)
    when (expected) {
        is Response.Success -> assertIs<Response.Success>(actual)
        is Response.Model -> {
            assertIs<Response.Model>(actual)
            assertTrue(expected.model.matches(actual.model)) // TODO(deepak-nulu): use Difference operator to compare.
        }
        is Response.ErrorList -> {
            assertIs<Response.ErrorList>(actual)
            assertEquals(expected.errorList.joinToString("\n"), actual.errorList.joinToString("\n"))
        }
        is Response.ErrorModel -> TODO()
    }
}