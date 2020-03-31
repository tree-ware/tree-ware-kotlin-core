package org.tree_ware.core.schema

/** A helper package with root and compositions. */
internal val helperPackage = MutablePackageSchema(
    name = "helper.package",
    root = MutableCompositionFieldSchema(
        name = "root",
        packageName = "helper.package",
        entityName = "entity1"
    ),
    aliases = listOf(
        MutableAliasSchema(
            name = "alias1",
            primitive = MutableStringSchema()
        )
    ),
    enumerations = listOf(
        MutableEnumerationSchema(
            name = "enumeration1",
            values = listOf(
                MutableEnumerationValueSchema("value1"),
                MutableEnumerationValueSchema("value2"),
                MutableEnumerationValueSchema("value3")
            )
        )
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
                    primitive = MutableNumericSchema<Int>(),
                    isKey = true
                )
            )
        )
    )
)
