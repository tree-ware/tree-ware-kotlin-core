package org.treeWare.metaModel.validation

import org.treeWare.metaModel.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class NumberValidationTests {
    @Test
    fun `Enumeration values must have a number`() {
        val testPackageJson = """
            |{
            |  "name": "org.tree_ware.test.main",
            |  "enumerations": [
            |    {
            |      "name": "test_enumeration",
            |      "values": [
            |        {
            |          "name": "test_value"
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newTestMetaModel(testPackageJson)
        assertNull(metaModel)
        val expectedErrors =
            listOf("Enumeration number is missing for /org.tree_ware.test.main/test_enumeration/test_value")
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `First enumeration value number must be 0`() {
        val testPackageJson = """
            |{
            |  "name": "org.tree_ware.test.main",
            |  "enumerations": [
            |    {
            |      "name": "test_enumeration",
            |      "values": [
            |        {
            |          "name": "test_value",
            |          "number": 1
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newTestMetaModel(testPackageJson)
        assertNull(metaModel)
        val expectedErrors =
            listOf("First Enumeration number 1 is invalid for /org.tree_ware.test.main/test_enumeration/test_value")
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `Numbers must not be repeated within an enumeration`() {
        val testPackageJson = """
            |{
            |  "name": "org.tree_ware.test.main",
            |  "enumerations": [
            |    {
            |      "name": "test_enumeration",
            |      "values": [
            |        {
            |          "name": "test_value_0",
            |          "number": 0
            |        },
            |        {
            |          "name": "test_value_1",
            |          "number": 0
            |        },
            |        {
            |          "name": "test_value_2",
            |          "number": 2
            |        },
            |        {
            |          "name": "test_value_3",
            |          "number": 2
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newTestMetaModel(testPackageJson)
        assertNull(metaModel)
        val expectedErrors = listOf(
            "Enumeration number 0 is a duplicate for /org.tree_ware.test.main/test_enumeration/test_value_1",
            "Enumeration number 2 is a duplicate for /org.tree_ware.test.main/test_enumeration/test_value_3"
        )
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `Numbers may be repeated across enumerations`() {
        val testPackageJson = """
            |{
            |  "name": "org.tree_ware.test.main",
            |  "enumerations": [
            |    {
            |      "name": "test_enumeration_1",
            |      "values": [
            |        {
            |          "name": "test_value_0",
            |          "number": 0
            |        },
            |        {
            |          "name": "test_value_1",
            |          "number": 2
            |        }
            |      ]
            |    },
            |    {
            |      "name": "test_enumeration_2",
            |      "values": [
            |        {
            |          "name": "test_value_0",
            |          "number": 0
            |        },
            |        {
            |          "name": "test_value_1",
            |          "number": 2
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newTestMetaModel(testPackageJson)
        assertNotNull(metaModel)
        val expectedErrors = emptyList<String>()
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `Entity fields must have a number`() {
        val testPackageJson = """
            |{
            |  "name": "org.tree_ware.test.main",
            |  "entities": [
            |    {
            |      "name": "test_entity",
            |      "fields": [
            |        {
            |          "name": "test_field",
            |          "type": "string"
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newTestMetaModel(testPackageJson)
        assertNull(metaModel)
        val expectedErrors = listOf("Field number is missing for /org.tree_ware.test.main/test_entity/test_field")
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `Entity fields must have a valid number`() {
        val testPackageJson = """
            |{
            |  "name": "org.tree_ware.test.main",
            |  "entities": [
            |    {
            |      "name": "test_entity",
            |      "fields": [
            |        {
            |          "name": "test_field_1",
            |          "number": 0,
            |          "type": "string"
            |        },
            |        {
            |          "name": "test_field_2",
            |          "number": 19000,
            |          "type": "string"
            |        },
            |        {
            |          "name": "test_field_3",
            |          "number": 19999,
            |          "type": "string"
            |        },
            |        {
            |          "name": "test_field_4",
            |          "number": 536870912,
            |          "type": "string"
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newTestMetaModel(testPackageJson)
        assertNull(metaModel)
        val expectedErrors = listOf(
            "Field number 0 is out of bounds for /org.tree_ware.test.main/test_entity/test_field_1",
            "Field number 19000 is out of bounds for /org.tree_ware.test.main/test_entity/test_field_2",
            "Field number 19999 is out of bounds for /org.tree_ware.test.main/test_entity/test_field_3",
            "Field number 536870912 is out of bounds for /org.tree_ware.test.main/test_entity/test_field_4"
        )
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `First entity field number need not be 1`() {
        val testPackageJson = """
            |{
            |  "name": "org.tree_ware.test.main",
            |  "entities": [
            |    {
            |      "name": "test_entity",
            |      "fields": [
            |        {
            |          "name": "test_field",
            |          "number": 2,
            |          "type": "string"
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newTestMetaModel(testPackageJson)
        assertNotNull(metaModel)
        val expectedErrors = emptyList<String>()
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `Numbers must not be repeated within an entity`() {
        val testPackageJson = """
            |{
            |  "name": "org.tree_ware.test.main",
            |  "entities": [
            |    {
            |      "name": "test_entity",
            |      "fields": [
            |        {
            |          "name": "test_field_0",
            |          "number": 1,
            |          "type": "string"
            |        },
            |        {
            |          "name": "test_field_1",
            |          "number": 1,
            |          "type": "string"
            |        },
            |        {
            |          "name": "test_field_2",
            |          "number": 3,
            |          "type": "string"
            |        },
            |        {
            |          "name": "test_field_3",
            |          "number": 3,
            |          "type": "string"
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newTestMetaModel(testPackageJson)
        assertNull(metaModel)
        val expectedErrors = listOf(
            "Field number 1 is a duplicate for /org.tree_ware.test.main/test_entity/test_field_1",
            "Field number 3 is a duplicate for /org.tree_ware.test.main/test_entity/test_field_3"
        )
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `Numbers may be repeated across entities`() {
        val testPackageJson = """
            |{
            |  "name": "org.tree_ware.test.main",
            |  "entities": [
            |    {
            |      "name": "test_entity_1",
            |      "fields": [
            |        {
            |          "name": "test_field_0",
            |          "number": 1,
            |          "type": "string"
            |        },
            |        {
            |          "name": "test_field_1",
            |          "number": 3,
            |          "type": "string"
            |        }
            |      ]
            |    },
            |    {
            |      "name": "test_entity_2",
            |      "fields": [
            |        {
            |          "name": "test_field_0",
            |          "number": 1,
            |          "type": "string"
            |        },
            |        {
            |          "name": "test_field_1",
            |          "number": 3,
            |          "type": "string"
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newTestMetaModel(testPackageJson)
        assertNotNull(metaModel)
        val expectedErrors = emptyList<String>()
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `Numbers may be repeated across enumerations and entities`() {
        val testPackageJson = """
            |{
            |  "name": "org.tree_ware.test.main",
            |  "enumerations": [
            |    {
            |      "name": "test_enumeration_1",
            |      "values": [
            |        {
            |          "name": "test_value_0",
            |          "number": 0
            |        },
            |        {
            |          "name": "test_value_1",
            |          "number": 2
            |        }
            |      ]
            |    },
            |    {
            |      "name": "test_enumeration_2",
            |      "values": [
            |        {
            |          "name": "test_value_0",
            |          "number": 0
            |        },
            |        {
            |          "name": "test_value_1",
            |          "number": 2
            |        }
            |      ]
            |    }
            |  ],
            |  "entities": [
            |    {
            |      "name": "test_entity_1",
            |      "fields": [
            |        {
            |          "name": "test_field_0",
            |          "number": 2,
            |          "type": "string"
            |        },
            |        {
            |          "name": "test_field_1",
            |          "number": 3,
            |          "type": "string"
            |        }
            |      ]
            |    },
            |    {
            |      "name": "test_entity_2",
            |      "fields": [
            |        {
            |          "name": "test_field_0",
            |          "number": 2,
            |          "type": "string"
            |        },
            |        {
            |          "name": "test_field_1",
            |          "number": 3,
            |          "type": "string"
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newTestMetaModel(testPackageJson)
        assertNotNull(metaModel)
        val expectedErrors = emptyList<String>()
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }
}

private fun newTestMetaModel(testPackageJson: String): ValidatedMetaModel = newMetaModelFromJsonStrings(
    listOf(newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackageJson)),
    false,
    null,
    null,
    ::addressBookRootEntityFactory,
    emptyList(),
    true
)