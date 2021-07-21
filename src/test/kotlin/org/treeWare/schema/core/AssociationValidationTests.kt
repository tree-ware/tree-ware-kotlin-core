package org.treeWare.schema.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
                            entityPathSchema = MutableEntityPathSchema(listOf())
                        )
                    )
                )
            )
        )
        val schema = MutableSchema(
            newHelperRoot(),
            listOf(testPackage, newHelperPackage())
        )
        val errors = validate(schema)

        val expectedErrors = listOf(
            "Association path is too short: /test.package/test_entity/test_association_field"
        )

        assertEquals(expectedErrors.toString(), errors.toString())
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
                            entityPathSchema = MutableEntityPathSchema(
                                listOf(
                                    "root"
                                )
                            )
                        )
                    )
                )
            )
        )
        val schema = MutableSchema(
            newHelperRoot(),
            listOf(testPackage, newHelperPackage())
        )
        val errors = validate(schema)

        val expectedErrors = listOf(
            "Association path is too short: /test.package/test_entity/test_association_field"
        )

        assertEquals(expectedErrors.toString(), errors.toString())
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
                            entityPathSchema = MutableEntityPathSchema(
                                listOf(
                                    "invalid_root",
                                    "invalid_field_1"
                                )
                            )
                        )
                    )
                )
            )
        )
        val schema = MutableSchema(
            newHelperRoot(),
            listOf(testPackage, newHelperPackage())
        )
        val errors = validate(schema)

        val expectedErrors = listOf(
            "Invalid association path root: /test.package/test_entity/test_association_field"
        )

        assertEquals(expectedErrors.toString(), errors.toString())
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
                            entityPathSchema = MutableEntityPathSchema(
                                listOf(
                                    "root",
                                    "invalid_field_1"
                                )
                            )
                        )
                    )
                )
            )
        )
        val schema = MutableSchema(
            newHelperRoot(),
            listOf(testPackage, newHelperPackage())
        )
        val errors = validate(schema)

        val expectedErrors = listOf(
            "Invalid association path: /test.package/test_entity/test_association_field"
        )

        assertEquals(expectedErrors.toString(), errors.toString())
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
                            entityPathSchema = MutableEntityPathSchema(
                                listOf(
                                    "root",
                                    "entity1_composition_field",
                                    "entity2_composition_field"
                                )
                            )
                        )
                    )
                )
            )
        )
        val schema = MutableSchema(
            newHelperRoot(),
            listOf(testPackage, newHelperPackage())
        )
        val errors = validate(schema)

        assertTrue(errors.isEmpty())
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
                            entityPathSchema = MutableEntityPathSchema(
                                listOf(
                                    "root",
                                    "entity1_composition_field_1_1"
                                )
                            ),
                            multiplicity = MutableMultiplicity(1, 0)
                        )
                    )
                )
            )
        )
        val schema = MutableSchema(
            newAssociationListHelperRoot(),
            listOf(testPackage, newAssociationListHelperPackage())
        )
        val errors = validate(schema)

        val expectedErrors = listOf(
            "Association list entity path does not have keys: /test.package/test_entity/test_association_field"
        )

        assertEquals(expectedErrors.toString(), errors.toString())
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
                            entityPathSchema = MutableEntityPathSchema(
                                listOf(
                                    "root",
                                    "entity1_composition_field_1_1",
                                    "entity2_composition_field_0_0"
                                )
                            ),
                            multiplicity = MutableMultiplicity(10, 10)
                        ),
                        MutableAssociationFieldSchema(
                            name = "test_association_field2",
                            entityPathSchema = MutableEntityPathSchema(
                                listOf(
                                    "root",
                                    "entity1_composition_field_1_10"
                                )
                            ),
                            multiplicity = MutableMultiplicity(0, 0)
                        )
                    )
                )
            )
        )
        val schema = MutableSchema(
            newAssociationListHelperRoot(),
            listOf(testPackage, newAssociationListHelperPackage())
        )
        val errors = validate(schema)

        assertTrue(errors.isEmpty())
    }
}
