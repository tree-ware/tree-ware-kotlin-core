package org.tree_ware.core.schema

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

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

            "Multiplicity min is less than 0: /hyphens-not-allowed-for-packages/dots.not_allowed_for.entities/invalid_string_field_multiplicity",

            "Invalid additional root: /package.b/root2",
            "No enumeration values: /package.b/enumeration_with_no_values",
            "Multiplicity max is less than min: /package.b/entity_b/invalid_alias_field_multiplicity",
            "Multiplicity max is less than 0: /package.b/entity_b/invalid_entity_field_multiplicity",
            "Multiplicity max is less than min: /package.b/entity_b/invalid_entity_field_multiplicity",

            "Invalid additional root: /package.c/root3",
            "Multiplicity is not [1, 1] for key field: /package.c/entity_1/invalid_alias_field",
            "Multiplicity is not [1, 1] for key field: /package.c/entity_1/invalid_composition_field",
            "Unknown field type: /package.c/entity_1/invalid_alias_field",
            "Unknown field type: /package.c/entity_1/invalid_enumeration_field",
            "Unknown field type: /package.c/entity_1/invalid_composition_field"
        )

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }
}
