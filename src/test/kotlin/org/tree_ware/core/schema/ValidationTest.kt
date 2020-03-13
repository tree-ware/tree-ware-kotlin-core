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
                "Invalid name: package.b.dots.not_allowed_for.enumerations",
                "Invalid name: package.b.hyphens-not-allowed-for-enumerations",
                "Duplicate name: package.b.duplicate_enumeration_name",
                "Duplicate name: package.b.enumeration_with_duplicate_values.duplicate_value",

                "Invalid multiplicity: hyphens-not-allowed-for-packages.dots.not_allowed_for.entities.invalid_string_field_multiplicity",
                "No enumeration values: package.b.enumeration_with_no_values",
                "Invalid multiplicity: package.b.entity_b.invalid_alias_field_multiplicity",
                "Invalid multiplicity: package.b.entity_b.invalid_entity_field_multiplicity",

                "Unknown field type: package.c.entity_1.invalid_alias_field",
                "Unknown field type: package.c.entity_1.invalid_enumeration_field",
                "Unknown field type: package.c.entity_1.invalid_entity_field"
        )

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }
}
