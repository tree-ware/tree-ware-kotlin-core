package org.tree_ware.core.schema

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class RootValidationTests {
    @Test
    fun `Root must have a valid name`() {
        val testPackage = MutablePackageSchema(
            name = "test.package",
            root = MutableRootSchema(
                name = "invalid.root-name",
                packageName = "helper.package",
                entityName = "helper_entity"
            )
        )
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage, localHelperPackage))

        val expectedErrors = listOf(
            "Invalid name: /test.package/invalid.root-name"
        )

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }

    @Test
    fun `Root must point to a valid entity`() {
        val testPackage = MutablePackageSchema(
            name = "test.package",
            root = MutableRootSchema(
                name = "test_root",
                packageName = "invalid.package",
                entityName = "invalid_entity"
            )
        )
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage, localHelperPackage))

        val expectedErrors = listOf(
            "Unknown root type: /test.package/test_root"
        )

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }

    @Test
    fun `A valid root must not result in errors`() {
        val testPackage = MutablePackageSchema(
            name = "test.package",
            root = MutableRootSchema(
                name = "test_root",
                packageName = "helper.package",
                entityName = "helper_entity"
            )
        )
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage, localHelperPackage))

        assertThat(errors.isEmpty()).isTrue()
    }
}

private val localHelperPackage = MutablePackageSchema(
    name = "helper.package",
    entities = listOf(
        MutableEntitySchema(
            name = "helper_entity",
            fields = listOf(
                MutablePrimitiveFieldSchema(
                    name = "boolean_field",
                    primitive = MutableBooleanSchema()
                )
            )
        )
    )
)
