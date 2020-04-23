package org.tree_ware.schema.core

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class RootValidationTests {
    @Test
    fun `Root must have a valid name`() {
        val schema = MutableSchema(
            MutableRootSchema(
                name = "invalid.root-name",
                packageName = "helper.package",
                entityName = "helper_entity"
            ),
            listOf(newLocalHelperPackage())
        )
        val errors = validate(schema)

        val expectedErrors = listOf(
            "Invalid name: /invalid.root-name"
        )

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }

    @Test
    fun `Root must point to a valid entity`() {
        val schema = MutableSchema(
            MutableRootSchema(
                name = "test_root",
                packageName = "invalid.package",
                entityName = "invalid_entity"
            ),
            listOf(newLocalHelperPackage())
        )
        val errors = validate(schema)

        val expectedErrors = listOf(
            "Unknown root type: /test_root"
        )

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }

    @Test
    fun `A valid root must not result in errors`() {
        val schema = MutableSchema(
            MutableRootSchema(
                name = "test_root",
                packageName = "helper.package",
                entityName = "helper_entity"
            ),
            listOf(newLocalHelperPackage())
        )
        val errors = validate(schema)

        assertThat(errors.isEmpty()).isTrue()
    }
}

private fun newLocalHelperPackage() = MutablePackageSchema(
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
