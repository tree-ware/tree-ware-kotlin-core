package org.treeWare.metaModel.validation

import org.treeWare.metaModel.assertJsonStringValidationErrors
import org.treeWare.metaModel.newTestMetaModelJson
import org.treeWare.metaModel.testMetaModelCommonRootJson
import kotlin.test.Test

class EntityValidationTests {
    @Test
    fun `Entity must have a name`() {
        val testPackageJson = """
            | {
            |   "name": "package1",
            |   "entities": [
            |     {
            |       "fields": [
            |         {
            |           "name": "field1",
            |           "type": "string"
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testMetaModelCommonRootJson, testPackageJson)
        val expectedDecodeErrors =
            listOf("Missing key fields [name] in instance of /org.tree_ware.meta_model.main/entity")
        val expectedErrors = listOf("Entity /org.tree_ware.test.common/entity1 cannot be resolved")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors, expectedDecodeErrors)
    }

    @Test
    fun `Entity fields must be specified`() {
        val testPackageJson = """
            | {
            |   "name": "package1",
            |   "entities": [
            |     {
            |       "name": "entity1"
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testMetaModelCommonRootJson, testPackageJson)
        val expectedErrors = listOf("Package 0 entity 0 fields are missing")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Entity fields must not be empty`() {
        val testPackageJson = """
            | {
            |   "name": "package1",
            |   "entities": [
            |     {
            |       "name": "entity1",
            |       "fields": []
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testMetaModelCommonRootJson, testPackageJson)
        val expectedErrors = listOf("Package 0 entity 0 fields are empty")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }
}
