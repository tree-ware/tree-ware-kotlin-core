package org.tree_ware.core.schema

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class ValidationTest {
    @Test
    fun `SchemaManager#addPackages() validates and returns errors`() {
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(getInvalidKotlinPackages())

        val expectedErrors = listOf(
                "Invalid name: hyphens-not-allowed-for-packages",

                "Invalid name: hyphens-not-allowed-for-packages.dots.not_allowed_for.entities",
                "Invalid name: hyphens-not-allowed-for-packages.dots.not_allowed_for.entities.dots.not_allowed_for.primitive_fields",
                "Invalid name: hyphens-not-allowed-for-packages.dots.not_allowed_for.entities.hyphens-not-allowed-for-primitive-fields",

                "Invalid name: hyphens-not-allowed-for-packages.hyphens-not-allowed-for-entities",
                "Invalid name: hyphens-not-allowed-for-packages.hyphens-not-allowed-for-entities.dots.not_allowed_for.entity_fields",
                "Invalid name: hyphens-not-allowed-for-packages.hyphens-not-allowed-for-entities.hyphens-not-allowed-for-entity-fields",
                "Invalid name: hyphens-not-allowed-for-packages.hyphens-not-allowed-for-entities.dots.not_allowed_for.alias_fields",
                "Invalid name: hyphens-not-allowed-for-packages.hyphens-not-allowed-for-entities.hyphens-not-allowed-for-alias-fields",
                "Duplicate name: hyphens-not-allowed-for-packages.hyphens-not-allowed-for-entities.duplicate_field_name",

                "Invalid name: package.b.dots.not_allowed_for.aliases",
                "Invalid name: package.b.hyphens-not-allowed-for-aliases",
                "Duplicate name: package.b.duplicate_alias_name",

                "Unknown field type: package.c.entity_1.invalid_entity_field",
                "Unknown field type: package.c.entity_1.invalid_alias_field"
        )

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }
}
