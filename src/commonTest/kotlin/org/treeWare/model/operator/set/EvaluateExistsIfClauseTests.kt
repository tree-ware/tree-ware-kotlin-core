package org.treeWare.model.operator.set

import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.metaModel.aux.ExistsIfClause
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.getSingleEntity
import org.treeWare.model.getMainModelFromJsonString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EvaluateExistsIfClauseTests {
    private val equalsClause1 = ExistsIfClause.Equals("last_name_first", "true")
    private val equalsClause2 = ExistsIfClause.Equals("encrypt_hero_name", "false")
    private val equalsClause3 = ExistsIfClause.Equals("background_color", "white")
    private val notClause = ExistsIfClause.Not(equalsClause3)
    private val andClause = ExistsIfClause.And(equalsClause1, equalsClause2)
    private val orClause = ExistsIfClause.Or(equalsClause1, equalsClause2)
    private val mixedClause = ExistsIfClause.And(
        ExistsIfClause.Or(equalsClause1, equalsClause2),
        ExistsIfClause.Not(equalsClause3)
    )

    private fun getSettingsEntity(modelJson: String): EntityModel {
        val model = getMainModelFromJsonString(addressBookMetaModel, modelJson)
        val modelRoot = model.root ?: throw IllegalStateException("Root has not been set")
        return getSingleEntity(modelRoot, "settings")
    }

    // region errors from missing fields in exists_if clauses

    private val missingFieldsJson = """
        |{
        |  "address_book": {
        |    "settings": {
        |    }
        |  }
        |}
    """.trimMargin()
    private val missingFieldsEntity = getSettingsEntity(missingFieldsJson)

    @Test
    fun `Equals clause must return error for missing fields`() {
        val expectedErrors =
            listOf("/address_book/settings: field last_name_first not found; it is needed for validating other fields")
        val result = evaluateExistsIfClause(equalsClause1, missingFieldsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Errors)
        assertEquals(expectedErrors.joinToString("\n"), result.errors.joinToString("\n"))
    }

    @Test
    fun `Not clause must return error for missing fields`() {
        val expectedErrors =
            listOf("/address_book/settings: field background_color not found; it is needed for validating other fields")
        val result = evaluateExistsIfClause(notClause, missingFieldsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Errors)
        assertEquals(expectedErrors.joinToString("\n"), result.errors.joinToString("\n"))
    }

    @Test
    fun `And clause must return error for missing fields`() {
        val expectedErrors =
            listOf("/address_book/settings: field last_name_first not found; it is needed for validating other fields")
        val result = evaluateExistsIfClause(andClause, missingFieldsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Errors)
        assertEquals(expectedErrors.joinToString("\n"), result.errors.joinToString("\n"))
    }

    @Test
    fun `Or clause must return error for missing fields`() {
        val expectedErrors =
            listOf("/address_book/settings: field last_name_first not found; it is needed for validating other fields")
        val result = evaluateExistsIfClause(orClause, missingFieldsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Errors)
        assertEquals(expectedErrors.joinToString("\n"), result.errors.joinToString("\n"))
    }

    @Test
    fun `Mixed clause must return error for missing fields`() {
        val expectedErrors =
            listOf("/address_book/settings: field last_name_first not found; it is needed for validating other fields")
        val result = evaluateExistsIfClause(mixedClause, missingFieldsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Errors)
        assertEquals(expectedErrors.joinToString("\n"), result.errors.joinToString("\n"))
    }

    // endregion

    // region true result

    private val trueFieldsJson = """
        |{
        |  "address_book": {
        |    "settings": {
        |      "last_name_first": true,
        |      "encrypt_hero_name": false,
        |      "background_color": "blue"
        |    }
        |  }
        |}
    """.trimMargin()
    private val trueFieldsEntity = getSettingsEntity(trueFieldsJson)

    @Test
    fun `Equals clause must evaluate to true`() {
        val result = evaluateExistsIfClause(equalsClause1, trueFieldsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Value)
        assertTrue(result.value)
    }

    @Test
    fun `Not clause must evaluate to true`() {
        val result = evaluateExistsIfClause(notClause, trueFieldsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Value)
        assertTrue(result.value)
    }

    @Test
    fun `And clause must evaluate to true`() {
        val result = evaluateExistsIfClause(andClause, trueFieldsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Value)
        assertTrue(result.value)
    }

    @Test
    fun `Or clause must evaluate to true`() {
        val result = evaluateExistsIfClause(orClause, trueFieldsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Value)
        assertTrue(result.value)
    }

    @Test
    fun `Mixed clause must evaluate to true`() {
        val result = evaluateExistsIfClause(mixedClause, trueFieldsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Value)
        assertTrue(result.value)
    }

    // endregion

    // region false result

    private val falseFieldsJson = """
        |{
        |  "address_book": {
        |    "settings": {
        |      "last_name_first": false,
        |      "encrypt_hero_name": true,
        |      "background_color": "white"
        |    }
        |  }
        |}
    """.trimMargin()
    private val falseFieldsEntity = getSettingsEntity(falseFieldsJson)

    @Test
    fun `Equals clause must evaluate to false`() {
        val result = evaluateExistsIfClause(equalsClause1, falseFieldsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Value)
        assertFalse(result.value)
    }

    @Test
    fun `Not clause must evaluate to false`() {
        val result = evaluateExistsIfClause(notClause, falseFieldsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Value)
        assertFalse(result.value)
    }

    @Test
    fun `And clause must evaluate to false`() {
        val result = evaluateExistsIfClause(andClause, falseFieldsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Value)
        assertFalse(result.value)
    }

    @Test
    fun `Or clause must evaluate to false`() {
        val result = evaluateExistsIfClause(orClause, falseFieldsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Value)
        assertFalse(result.value)
    }

    @Test
    fun `Mixed clause must evaluate to false`() {
        val result = evaluateExistsIfClause(mixedClause, falseFieldsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Value)
        assertFalse(result.value)
    }

    // endregion

    // region short-circuiting in binary operators

    // NOTE: short-circuiting is tested by not including the field required by the second branch of the operation.
    // If there was no short-circuiting, then the missing field in the second branch will result in an error.

    @Test
    fun `And clause must evaluate second argument if first argument is true`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "settings": {
            |      "last_name_first": true,
            |      "encrypt_hero_name": true
            |    }
            |  }
            |}
        """.trimMargin()
        val settingsEntity = getSettingsEntity(modelJson)
        val result = evaluateExistsIfClause(andClause, settingsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Value)
        assertFalse(result.value)
    }

    @Test
    fun `And clause must return an error if second argument is missing and first argument is true`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "settings": {
            |      "last_name_first": true
            |    }
            |  }
            |}
        """.trimMargin()
        val settingsEntity = getSettingsEntity(modelJson)
        val expectedErrors =
            listOf("/address_book/settings: field encrypt_hero_name not found; it is needed for validating other fields")
        val result = evaluateExistsIfClause(andClause, settingsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Errors)
        assertEquals(expectedErrors.joinToString("\n"), result.errors.joinToString("\n"))
    }

    @Test
    fun `And clause must short-circuit second argument if first argument is false`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "settings": {
            |      "last_name_first": false
            |    }
            |  }
            |}
        """.trimMargin()
        val settingsEntity = getSettingsEntity(modelJson)
        val result = evaluateExistsIfClause(andClause, settingsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Value)
        assertFalse(result.value)
    }

    @Test
    fun `Or clause must evaluate second argument if first argument is false`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "settings": {
            |      "last_name_first": false,
            |      "encrypt_hero_name": false
            |    }
            |  }
            |}
        """.trimMargin()
        val settingsEntity = getSettingsEntity(modelJson)
        val result = evaluateExistsIfClause(orClause, settingsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Value)
        assertTrue(result.value)
    }

    @Test
    fun `Or clause must return an error if second argument is missing and first argument is false`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "settings": {
            |      "last_name_first": false
            |    }
            |  }
            |}
        """.trimMargin()
        val settingsEntity = getSettingsEntity(modelJson)
        val expectedErrors =
            listOf("/address_book/settings: field encrypt_hero_name not found; it is needed for validating other fields")
        val result = evaluateExistsIfClause(orClause, settingsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Errors)
        assertEquals(expectedErrors.joinToString("\n"), result.errors.joinToString("\n"))
    }

    @Test
    fun `Or clause must short-circuit second argument if first argument is true`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "settings": {
            |      "last_name_first": true
            |    }
            |  }
            |}
        """.trimMargin()
        val settingsEntity = getSettingsEntity(modelJson)
        val result = evaluateExistsIfClause(orClause, settingsEntity, "/address_book/settings")
        assertTrue(result is ExistsIfClauseResult.Value)
        assertTrue(result.value)
    }

    // endregion
}