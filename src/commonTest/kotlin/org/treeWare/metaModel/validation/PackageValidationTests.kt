package org.treeWare.metaModel.validation

import org.treeWare.metaModel.assertJsonStringValidationErrors
import org.treeWare.metaModel.newTestMetaModelJson
import org.treeWare.metaModel.testMetaModelCommonPackageJson
import org.treeWare.metaModel.testMetaModelCommonRootJson
import kotlin.test.Test

class PackageValidationTests {
    @Test
    fun `Packages list must be specified`() {
        val metaModelJson = """
            | {
            |   "name": "test_meta_model",
            |   "package": "org.tree_ware.test.main",
            |   "version": {
            |     "semantic": "1.0.0",
            |     "name": "pacific-ocean"
            |   },
            |   "root": {
            |     "entity": "entity1",
            |     "package": "org.tree_ware.test.common"
            |   }
            | }
        """.trimMargin()
        val expectedErrors = listOf("Packages are missing")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Packages list must not be empty`() {
        val metaModelJson = """
            | {
            |   "name": "test_meta_model",
            |   "package": "org.tree_ware.test.main",
            |   "version": {
            |     "semantic": "1.0.0",
            |     "name": "pacific-ocean"
            |   },
            |   "root": {
            |     "entity": "entity1",
            |     "package": "org.tree_ware.test.common"
            |   },
            |   "packages": []
            | }
        """.trimMargin()
        val expectedErrors = listOf("Entity /org.tree_ware.test.common/entity1 cannot be resolved")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Package must have a name`() {
        val metaModelJson = """
            | {
            |   "name": "test_meta_model",
            |   "version": {
            |     "semantic": "1.0.0",
            |     "name": "pacific-ocean"
            |   },
            |   "root": {
            |     "entity": "entity1",
            |     "package": "org.tree_ware.test.common"
            |   },
            |   "packages": [
            |     {},
            |     {"name": "package1"},
            |     {}
            |   ]
            | }
        """.trimMargin()
        val expectedDecodeErrors = listOf(
            "Missing key fields [name] in instance of /org.tree_ware.meta_model.main/package",
            "Missing key fields [name] in instance of /org.tree_ware.meta_model.main/package"
        )
        val expectedErrors = listOf("Entity /org.tree_ware.test.common/entity1 cannot be resolved")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors, expectedDecodeErrors)
    }

    @Test
    fun `Package entities may be empty`() {
        val testPackageJson = """
            | {
            |   "name": "package1",
            |   "entities": []
            | }
        """.trimMargin()
        val metaModelJson =
            newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackageJson)
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Package enumerations may be empty`() {
        val testPackage = """
            | {
            |   "name": "package1",
            |   "enumerations": []
            | }
        """.trimMargin()
        val metaModelJson =
            newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackage)
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }
}
