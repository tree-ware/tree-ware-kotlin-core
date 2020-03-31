package org.tree_ware.core.schema

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class MultiplicityValidationTests {
    @Test
    fun `Multiplicity min must not be negative`() {
        val testPackage = getTestPackage(-1, 1)
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage, helperPackage))

        val expectedErrors = allFieldPaths.map { "Multiplicity min is less than 0: $it" }

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }

    @Test
    fun `Multiplicity max must not be less than 0`() {
        val testPackage = getTestPackage(0, -1)
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage, helperPackage))

        val expectedErrors = allFieldPaths.map { "Multiplicity max is less than 0: $it" }

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }

    @Test
    fun `Multiplicity max must not be less than min if max is not 0`() {
        val testPackage = getTestPackage(2, 1)
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage, helperPackage))

        val expectedErrors = allFieldPaths.map { "Multiplicity max is less than min: $it" }

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }

    @Test
    fun `Multiplicity max of 0 for unbounded is valid even if min is higher`() {
        val testPackage = getTestPackage(1, 0)
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage, helperPackage))

        assertThat(errors.isEmpty()).isTrue()
    }

    @Test
    fun `Multiplicity is valid if max is equal to min`() {
        val testPackage = getTestPackage(1, 1)
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage, helperPackage))

        assertThat(errors.isEmpty()).isTrue()
    }

    @Test
    fun `Multiplicity is valid if max is greater than min`() {
        val testPackage = getTestPackage(1, 2)
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage, helperPackage))

        assertThat(errors.isEmpty()).isTrue()
    }

    @Test
    fun `Multiplicity must be {1, 1} for key fields`() {
        val testPackage = getTestPackage(0, 2, true)
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage, helperPackage))

        val expectedErrors = nonAssociationFieldPaths.map { "Multiplicity is not [1, 1] for key field: $it" }

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }

    @Test
    fun `Multiplicity {1, 1} is valid for key fields`() {
        val testPackage = getTestPackage(1, 1, true)
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage, helperPackage))

        assertThat(errors.isEmpty()).isTrue()
    }
}

private fun getTestPackage(min: Long, max: Long, isKey: Boolean = false): MutablePackageSchema {
    return MutablePackageSchema(
        name = "test.package",
        entities = listOf(
            MutableEntitySchema(
                name = "entity",
                fields = listOf(
                    MutablePrimitiveFieldSchema(
                        name = "primitive_field",
                        primitive = MutableStringSchema(),
                        isKey = isKey,
                        multiplicity = MutableMultiplicity(min, max)
                    ),
                    MutableAliasFieldSchema(
                        name = "alias_field",
                        packageName = "helper.package",
                        aliasName = "alias1",
                        isKey = isKey,
                        multiplicity = MutableMultiplicity(min, max)
                    ),
                    MutableEnumerationFieldSchema(
                        name = "enumeration_field",
                        packageName = "helper.package",
                        enumerationName = "enumeration1",
                        isKey = isKey,
                        multiplicity = MutableMultiplicity(min, max)
                    ),
                    MutableCompositionFieldSchema(
                        name = "composition_field",
                        packageName = "helper.package",
                        entityName = "entity3",
                        isKey = isKey,
                        multiplicity = MutableMultiplicity(min, max)
                    ),
                    MutableAssociationFieldSchema(
                        name = "association_field",
                        entityPath = listOf("root", "entity1_composition_field"),
                        multiplicity = MutableMultiplicity(min, max)
                    )
                )
            )
        )
    )
}

private val allFieldPaths = listOf(
    "/test.package/entity/primitive_field",
    "/test.package/entity/alias_field",
    "/test.package/entity/enumeration_field",
    "/test.package/entity/composition_field",
    "/test.package/entity/association_field"
)

private val nonAssociationFieldPaths = listOf(
    "/test.package/entity/primitive_field",
    "/test.package/entity/alias_field",
    "/test.package/entity/enumeration_field",
    "/test.package/entity/composition_field"
)
