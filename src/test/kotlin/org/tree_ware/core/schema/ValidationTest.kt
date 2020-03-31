package org.tree_ware.core.schema

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

// TODO(deepak-nulu): split into multiple files based on type of validation

class ValidationTest {
    @Test
    fun `SchemaManager#addPackages() returns error if root entity is missing`() {
        val noRootPackage = MutablePackageSchema(name = "no.root.package")

        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(noRootPackage))

        val expectedErrors = listOf("No root entity")

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }

    @Test
    fun `SchemaManager#addPackages() validates and returns errors`() {
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(getInvalidKotlinPackages())

        val expectedErrors = listOf(
            "Invalid name: /hyphens-not-allowed-for-packages",

            "Invalid name: /hyphens-not-allowed-for-packages/dots.not_allowed_for.entities",
            "Invalid name: /hyphens-not-allowed-for-packages/dots.not_allowed_for.entities/dots.not_allowed_for.primitive_fields",
            "Invalid name: /hyphens-not-allowed-for-packages/dots.not_allowed_for.entities/hyphens-not-allowed-for-primitive-fields",

            "Invalid name: /hyphens-not-allowed-for-packages/hyphens-not-allowed-for-entities",
            "Invalid name: /hyphens-not-allowed-for-packages/hyphens-not-allowed-for-entities/dots.not_allowed_for.association_fields",
            "Invalid name: /hyphens-not-allowed-for-packages/hyphens-not-allowed-for-entities/hyphens-not-allowed-for-association-fields",
            "Invalid name: /hyphens-not-allowed-for-packages/hyphens-not-allowed-for-entities/dots.not_allowed_for.alias_fields",
            "Invalid name: /hyphens-not-allowed-for-packages/hyphens-not-allowed-for-entities/hyphens-not-allowed-for-alias-fields",
            "Duplicate name: /hyphens-not-allowed-for-packages/hyphens-not-allowed-for-entities/duplicate_field_name",

            "Invalid name: /package.b/dots.not_allowed_for.aliases",
            "Invalid name: /package.b/hyphens-not-allowed-for-aliases",
            "Duplicate name: /package.b/duplicate_alias_name",
            "Invalid name: /package.b/dots.not_allowed_for.enumerations",
            "Invalid name: /package.b/hyphens-not-allowed-for-enumerations",
            "Duplicate name: /package.b/duplicate_enumeration_name",
            "Duplicate name: /package.b/enumeration_with_duplicate_values/duplicate_value",

            "Invalid additional root: /package.b/root2",
            "No enumeration values: /package.b/enumeration_with_no_values",

            "Invalid additional root: /package.c/root3",
            "Unknown field type: /package.c/entity_1/invalid_alias_field",
            "Unknown field type: /package.c/entity_1/invalid_enumeration_field",
            "Unknown field type: /package.c/entity_1/invalid_composition_field",

            "Association path is too short: /hyphens-not-allowed-for-packages/hyphens-not-allowed-for-entities/dots.not_allowed_for.association_fields",
            "Association path is too short: /hyphens-not-allowed-for-packages/hyphens-not-allowed-for-entities/hyphens-not-allowed-for-association-fields"
        )

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }
}
