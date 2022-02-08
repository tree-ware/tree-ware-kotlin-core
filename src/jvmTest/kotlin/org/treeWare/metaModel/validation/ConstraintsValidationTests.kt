package org.treeWare.metaModel.validation

import org.treeWare.metaModel.*
import kotlin.test.Test

private const val FIELD_ID = "Package 1 entity 0 field 0"

class ConstraintsValidationTests {
    @Test
    fun `Constraint min_size must be valid only for string fields`() {
        val constraintName = "min_size"
        val mainExpectedError = "$FIELD_ID cannot have $constraintName string constraint"
        for (fieldType in FieldType.values()) {
            val metaModelJson = getMetaModelJson(fieldType.name.lowercase(), constraintName, "10")
            val expectedErrors = when (fieldType) {
                FieldType.STRING -> emptyList()
                FieldType.ENUMERATION -> listOf("$FIELD_ID enumeration info is missing", mainExpectedError)
                FieldType.ASSOCIATION -> listOf("$FIELD_ID association info is missing", mainExpectedError)
                FieldType.COMPOSITION -> listOf("$FIELD_ID composition info is missing", mainExpectedError)
                else -> listOf(mainExpectedError)
            }
            assertJsonStringValidationErrors(metaModelJson, expectedErrors)
        }
    }

    @Test
    fun `Constraint max_size must be valid only for string fields`() {
        val constraintName = "max_size"
        val mainExpectedError = "$FIELD_ID cannot have $constraintName string constraint"
        for (fieldType in FieldType.values()) {
            val metaModelJson = getMetaModelJson(fieldType.name.lowercase(), constraintName, "10")
            val expectedErrors = when (fieldType) {
                FieldType.STRING -> emptyList()
                FieldType.ENUMERATION -> listOf("$FIELD_ID enumeration info is missing", mainExpectedError)
                FieldType.ASSOCIATION -> listOf("$FIELD_ID association info is missing", mainExpectedError)
                FieldType.COMPOSITION -> listOf("$FIELD_ID composition info is missing", mainExpectedError)
                else -> listOf(mainExpectedError)
            }
            assertJsonStringValidationErrors(metaModelJson, expectedErrors)
        }
    }

    @Test
    fun `Constraint regex must be valid only for string fields`() {
        val constraintName = "regex"
        val mainExpectedError = "$FIELD_ID cannot have $constraintName string constraint"
        for (fieldType in FieldType.values()) {
            val metaModelJson = getMetaModelJson(fieldType.name.lowercase(), constraintName, "\"[a-z0-9]*\"")
            val expectedErrors = when (fieldType) {
                FieldType.STRING -> emptyList()
                FieldType.ENUMERATION -> listOf("$FIELD_ID enumeration info is missing", mainExpectedError)
                FieldType.ASSOCIATION -> listOf("$FIELD_ID association info is missing", mainExpectedError)
                FieldType.COMPOSITION -> listOf("$FIELD_ID composition info is missing", mainExpectedError)
                else -> listOf(mainExpectedError)
            }
            assertJsonStringValidationErrors(metaModelJson, expectedErrors)
        }
    }
}

private fun getMetaModelJson(fieldType: String, constraintName: String, constraintValue: String): String {
    val mainPackageJson = """
        |{
        |  "name": "test.main",
        |  "entities": [
        |    {
        |      "name": "entity1",
        |      "fields": [
        |        {
        |          "name": "field1",
        |          "type": "$fieldType",
        |          "$constraintName": $constraintValue
        |        }
        |      ]
        |    }
        |  ]
        |}
    """.trimMargin()
    return newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, mainPackageJson)
}