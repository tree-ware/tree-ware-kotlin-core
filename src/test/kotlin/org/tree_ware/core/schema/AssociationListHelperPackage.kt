package org.tree_ware.core.schema

/**
 * A helper package for association list validation tests.
 */
internal val associationListHelperPackage = MutablePackageSchema(
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
                MutableCompositionFieldSchema(
                    name = "entity1_composition_field_1_1",
                    packageName = "helper.package",
                    entityName = "entity2"
                ),
                MutableCompositionFieldSchema(
                    name = "entity1_composition_field_1_10",
                    packageName = "helper.package",
                    entityName = "entity3",
                    multiplicity = MutableMultiplicity(1, 10)
                )
            )
        ),
        MutableEntitySchema(
            name = "entity2",
            fields = listOf(
                MutableCompositionFieldSchema(
                    name = "entity2_composition_field_0_0",
                    packageName = "helper.package",
                    entityName = "entity3",
                    multiplicity = MutableMultiplicity(0, 0)
                )
            )
        ),
        MutableEntitySchema(
            name = "entity3",
            fields = listOf(
                MutablePrimitiveFieldSchema(
                    name = "int_field",
                    primitive = MutableNumericSchema<Int>(),
                    isKey = true
                )
            )
        )
    )
)
