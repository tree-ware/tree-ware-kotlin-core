package org.treeWare.metaModel.validation

import org.treeWare.metaModel.*
import kotlin.test.Test

class GranularityValidationTests {
    // region no granularity

    @Test
    fun `Granularity must be optional for non-composition single-fields`() {
        val metaModelJson = getNonCompositionMetaModelJson(Multiplicity.OPTIONAL, null)
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Granularity must be optional for composition single-fields`() {
        val metaModelJson = getCompositionMetaModelJson(Multiplicity.OPTIONAL, null)
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Granularity must be optional for composition set-fields`() {
        val metaModelJson = getCompositionMetaModelJson(Multiplicity.SET, null)
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    // endregion

    // region field granularity

    @Test
    fun `Field granularity must pass for non-composition single-fields`() {
        val metaModelJson = getNonCompositionMetaModelJson(Multiplicity.OPTIONAL, Granularity.FIELD)
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Field granularity must pass for composition single-fields`() {
        val metaModelJson = getCompositionMetaModelJson(Multiplicity.OPTIONAL, Granularity.FIELD)
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Field granularity must pass for composition set-fields`() {
        val metaModelJson = getCompositionMetaModelJson(Multiplicity.SET, Granularity.FIELD)
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    // endregion

    // region entity granularity

    @Test
    fun `Entity granularity must fail for non-composition single-fields`() {
        val metaModelJson = getNonCompositionMetaModelJson(Multiplicity.OPTIONAL, Granularity.ENTITY)
        val expectedErrors = listOf(
            "/test.main/test_entity/primitive_field: `entity` granularity is not yet supported",
            "/test.main/test_entity/enumeration_field: `entity` granularity is not yet supported",
            "/test.main/test_entity/association_field: `entity` granularity is not yet supported",
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Entity granularity must fail for composition single-fields`() {
        val metaModelJson = getCompositionMetaModelJson(Multiplicity.OPTIONAL, Granularity.ENTITY)
        val expectedErrors = listOf(
            "/test.main/test_entity/composition_field: `entity` granularity is not yet supported",
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Entity granularity must fail for composition set-fields`() {
        val metaModelJson = getCompositionMetaModelJson(Multiplicity.SET, Granularity.ENTITY)
        val expectedErrors = listOf(
            "/test.main/test_entity/composition_field: `entity` granularity is not yet supported",
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    // endregion

    // region sub-tree granularity

    @Test
    fun `Sub-tree granularity must fail for non-composition single-fields`() {
        val metaModelJson = getNonCompositionMetaModelJson(Multiplicity.OPTIONAL, Granularity.SUB_TREE)
        val expectedErrors = listOf(
            "/test.main/test_entity/primitive_field: `sub_tree` granularity is supported only for composition fields",
            "/test.main/test_entity/enumeration_field: `sub_tree` granularity is supported only for composition fields",
            "/test.main/test_entity/association_field: `sub_tree` granularity is supported only for composition fields",
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Sub-tree granularity must pass for composition single-fields`() {
        val metaModelJson = getCompositionMetaModelJson(Multiplicity.OPTIONAL, Granularity.SUB_TREE)
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Sub-tree granularity must pass for composition set-fields`() {
        val metaModelJson = getCompositionMetaModelJson(Multiplicity.SET, Granularity.SUB_TREE)
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    // endregion
}

private fun getNonCompositionMetaModelJson(multiplicity: Multiplicity, granularity: Granularity?): String {
    val granularityJson = getGranularityJson(granularity)
    val mainPackageJson = """
        {
          "name": "test.main",
          "entities": [
            {
              "name": "test_entity",
              "fields": [
                {
                  "name": "primitive_field",
                  "number": 1,
                  "type": "string",
                  $granularityJson
                  "multiplicity": "${multiplicity.name.lowercase()}"
                },
                {
                  "name": "enumeration_field",
                  "number": 2,
                  "type": "enumeration",
                  "enumeration": {
                    "name": "enumeration1",
                    "package": "test.common"
                  },
                  $granularityJson
                  "multiplicity": "${multiplicity.name.lowercase()}"
                },
                {
                  "name": "association_field",
                  "number": 3,
                  "type": "association",
                  "association": {
                    "entity": "entity1",
                    "package": "test.common"
                  },
                  $granularityJson
                  "multiplicity": "${multiplicity.name.lowercase()}"
                }
              ]
            }
          ]
        }
    """.trimIndent()
    return newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, mainPackageJson)
}

private fun getCompositionMetaModelJson(multiplicity: Multiplicity, granularity: Granularity?): String {
    val granularityJson = getGranularityJson(granularity)
    val mainPackageJson = """
        {
          "name": "test.main",
          "entities": [
            {
              "name": "test_entity",
              "fields": [
                {
                  "name": "composition_field",
                  "number": 1,
                  "type": "composition",
                  "composition": {
                    "entity": "entity2",
                    "package": "test.common"
                  },
                  $granularityJson
                  "multiplicity": "${multiplicity.name.lowercase()}"
                }
              ]
            }
          ]
        }
    """.trimIndent()
    return newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, mainPackageJson)
}

private fun getGranularityJson(granularity: Granularity?) =
    granularity?.let { "\"granularity\": \"${it.name.lowercase()}\"," } ?: ""