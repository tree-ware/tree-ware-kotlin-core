package org.tree_ware.core.schema

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class AssociationValidationTests {
    @Test
    fun `Association must specify a path`() {
        val testPackage = MutablePackageSchema(
            name = "test.package",
            entities = listOf(
                MutableEntitySchema(
                    name = "test_entity",
                    fields = listOf(
                        MutableAssociationFieldSchema(
                            name = "test_association_field",
                            entityPath = listOf()
                        )
                    )
                )
            )
        )
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage, helperPackage))

        val expectedErrors = listOf(
            "Association path is too short: /test.package/test_entity/test_association_field"
        )

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }

    @Test
    fun `Association must specify more than a root`() {
        val testPackage = MutablePackageSchema(
            name = "test.package",
            entities = listOf(
                MutableEntitySchema(
                    name = "test_entity",
                    fields = listOf(
                        MutableAssociationFieldSchema(
                            name = "test_association_field",
                            entityPath = listOf(
                                "root"
                            )
                        )
                    )
                )
            )
        )
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage, helperPackage))

        val expectedErrors = listOf(
            "Association path is too short: /test.package/test_entity/test_association_field"
        )

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }

    @Test
    fun `Association must specify a valid root`() {
        val testPackage = MutablePackageSchema(
            name = "test.package",
            entities = listOf(
                MutableEntitySchema(
                    name = "test_entity",
                    fields = listOf(
                        MutableAssociationFieldSchema(
                            name = "test_association_field",
                            entityPath = listOf(
                                "invalid_root",
                                "invalid_field_1"
                            )
                        )
                    )
                )
            )
        )
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage, helperPackage))

        val expectedErrors = listOf(
            "Invalid association path root: /test.package/test_entity/test_association_field"
        )

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }

    @Test
    fun `Association must specify a valid path`() {
        val testPackage = MutablePackageSchema(
            name = "test.package",
            entities = listOf(
                MutableEntitySchema(
                    name = "test_entity",
                    fields = listOf(
                        MutableAssociationFieldSchema(
                            name = "test_association_field",
                            entityPath = listOf(
                                "root",
                                "invalid_field_1"
                            )
                        )
                    )
                )
            )
        )
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage, helperPackage))

        val expectedErrors = listOf(
            "Invalid association path: /test.package/test_entity/test_association_field"
        )

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }

    @Test
    fun `Valid association must not result in errors`() {
        val testPackage = MutablePackageSchema(
            name = "test.package",
            entities = listOf(
                MutableEntitySchema(
                    name = "test_entity",
                    fields = listOf(
                        MutableAssociationFieldSchema(
                            name = "test_association_field",
                            entityPath = listOf(
                                "root",
                                "entity1_composition_field",
                                "entity2_composition_field"
                            )
                        )
                    )
                )
            )
        )
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage, helperPackage))

        assertThat(errors.isEmpty()).isTrue()
    }
}

private val helperPackage = MutablePackageSchema(
    name = "helper.package",
    root = MutableCompositionFieldSchema(
        name = "root",
        packageName = "helper.package",
        entityName = "entity1"
    ),
    entities = listOf(
        MutableEntitySchema(
            name = "entity1",
            fields = listOf(
                MutablePrimitiveFieldSchema(
                    name = "string_field",
                    primitive = MutableStringSchema()
                ),
                MutableCompositionFieldSchema(
                    name = "entity1_composition_field",
                    packageName = "helper.package",
                    entityName = "entity2"
                )
            )
        ),
        MutableEntitySchema(
            name = "entity2",
            fields = listOf(
                MutablePrimitiveFieldSchema(
                    name = "boolean_field",
                    primitive = MutableBooleanSchema()
                ),
                MutableCompositionFieldSchema(
                    name = "entity2_composition_field",
                    packageName = "helper.package",
                    entityName = "entity3"
                )
            )
        ),
        MutableEntitySchema(
            name = "entity3",
            fields = listOf(
                MutablePrimitiveFieldSchema(
                    name = "int_field",
                    primitive = MutableNumericSchema<Int>()
                )
            )
        )
    )
)
