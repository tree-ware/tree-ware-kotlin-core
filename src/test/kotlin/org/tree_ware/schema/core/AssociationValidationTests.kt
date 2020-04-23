package org.tree_ware.schema.core

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.tree_ware.schema.core.*

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
        val errors = schemaManager.addPackages(listOf(testPackage,
            helperPackage
        ))

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
        val errors = schemaManager.addPackages(listOf(testPackage,
            helperPackage
        ))

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
        val errors = schemaManager.addPackages(listOf(testPackage,
            helperPackage
        ))

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
        val errors = schemaManager.addPackages(listOf(testPackage,
            helperPackage
        ))

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
        val errors = schemaManager.addPackages(listOf(testPackage,
            helperPackage
        ))

        assertThat(errors.isEmpty()).isTrue()
    }

    @Test
    fun `Association list entity path must have keys`() {
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
                                "entity1_composition_field_1_1"
                            ),
                            multiplicity = MutableMultiplicity(1, 0)
                        )
                    )
                )
            )
        )
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage,
            associationListHelperPackage
        ))

        val expectedErrors = listOf(
            "Association list entity path does not have keys: /test.package/test_entity/test_association_field"
        )

        assertThat(errors.toString()).isEqualTo(expectedErrors.toString())
    }

    @Test
    fun `Association list is valid if entity path has keys`() {
        val testPackage = MutablePackageSchema(
            name = "test.package",
            entities = listOf(
                MutableEntitySchema(
                    name = "test_entity",
                    fields = listOf(
                        MutableAssociationFieldSchema(
                            name = "test_association_field1",
                            entityPath = listOf(
                                "root",
                                "entity1_composition_field_1_1",
                                "entity2_composition_field_0_0"
                            ),
                            multiplicity = MutableMultiplicity(10, 10)
                        ),
                        MutableAssociationFieldSchema(
                            name = "test_association_field2",
                            entityPath = listOf(
                                "root",
                                "entity1_composition_field_1_10"
                            ),
                            multiplicity = MutableMultiplicity(0, 0)
                        )
                    )
                )
            )
        )
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(testPackage,
            associationListHelperPackage
        ))

        assertThat(errors.isEmpty()).isTrue()
    }
}
