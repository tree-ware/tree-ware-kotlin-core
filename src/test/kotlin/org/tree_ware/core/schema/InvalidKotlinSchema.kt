package org.tree_ware.core.schema

fun getInvalidKotlinPackages(): List<MutablePackageSchema> {
    val packageA = MutablePackageSchema(
        name = "hyphens-not-allowed-for-packages",
        root = MutableCompositionFieldSchema(
            name = "root1",
            packageName = "hyphens-not-allowed-for-packages",
            entityName = "dots.not_allowed_for.entities"
        ),
        entities = listOf(
            MutableEntitySchema(
                name = "dots.not_allowed_for.entities",
                fields = listOf(
                    MutablePrimitiveFieldSchema(
                        name = "dots.not_allowed_for.primitive_fields",
                        primitive = MutableStringSchema(),
                        isKey = true
                    ),
                    MutablePrimitiveFieldSchema(
                        name = "hyphens-not-allowed-for-primitive-fields",
                        primitive = MutableStringSchema()
                    )
                )
            ),
            MutableEntitySchema(
                name = "hyphens-not-allowed-for-entities",
                fields = listOf(
                    MutableAssociationFieldSchema(
                        name = "dots.not_allowed_for.association_fields",
                        entityPath = listOf()
                    ),
                    MutableAssociationFieldSchema(
                        name = "hyphens-not-allowed-for-association-fields",
                        entityPath = listOf()
                    ),
                    MutableAliasFieldSchema(
                        name = "dots.not_allowed_for.alias_fields",
                        packageName = "package.b",
                        aliasName = "dots.not_allowed_for.aliases"
                    ),
                    MutableAliasFieldSchema(
                        name = "hyphens-not-allowed-for-alias-fields",
                        packageName = "package.b",
                        aliasName = "hyphens-not-allowed-for-aliases"
                    ),
                    MutableCompositionFieldSchema(
                        name = "duplicate_field_name",
                        packageName = "hyphens-not-allowed-for-packages",
                        entityName = "dots.not_allowed_for.entities"
                    ),
                    MutableAliasFieldSchema(
                        name = "duplicate_field_name",
                        packageName = "package.b",
                        aliasName = "hyphens-not-allowed-for-aliases"
                    )
                )
            )
        )
    )

    val packageB = MutablePackageSchema(
        name = "package.b",
        root = MutableCompositionFieldSchema(
            name = "root2",
            packageName = "package.b",
            entityName = "empty_entity"
        ),
        aliases = listOf(
            MutableAliasSchema(
                name = "dots.not_allowed_for.aliases",
                primitive = MutableStringSchema()
            ),
            MutableAliasSchema(
                name = "hyphens-not-allowed-for-aliases",
                primitive = MutableStringSchema()
            ),
            MutableAliasSchema(
                name = "duplicate_alias_name",
                primitive = MutableStringSchema()
            ),
            MutableAliasSchema(
                name = "duplicate_alias_name",
                primitive = MutableStringSchema()
            )
        ),
        enumerations = listOf(
            MutableEnumerationSchema(
                name = "dots.not_allowed_for.enumerations",
                values = listOf(
                    MutableEnumerationValueSchema("value1"),
                    MutableEnumerationValueSchema("value2")
                )
            ),
            MutableEnumerationSchema(
                name = "hyphens-not-allowed-for-enumerations",
                values = listOf(
                    MutableEnumerationValueSchema("value1"),
                    MutableEnumerationValueSchema("value2")
                )
            ),
            MutableEnumerationSchema(
                name = "duplicate_enumeration_name",
                values = listOf(
                    MutableEnumerationValueSchema("value1"),
                    MutableEnumerationValueSchema("value2")
                )
            ),
            MutableEnumerationSchema(
                name = "duplicate_enumeration_name",
                values = listOf(
                    MutableEnumerationValueSchema("value3"),
                    MutableEnumerationValueSchema("value4")
                )
            ),
            MutableEnumerationSchema(
                name = "enumeration_with_duplicate_values",
                values = listOf(
                    MutableEnumerationValueSchema("value1"),
                    MutableEnumerationValueSchema("duplicate_value"),
                    MutableEnumerationValueSchema("value2"),
                    MutableEnumerationValueSchema("duplicate_value")
                )
            ),
            MutableEnumerationSchema(
                name = "enumeration_with_no_values",
                values = listOf()
            )
        ),
        entities = listOf(
            MutableEntitySchema(
                name = "empty_entity",
                fields = listOf()
            )
        )
    )

    val packageC = MutablePackageSchema(
        name = "package.c",
        root = MutableCompositionFieldSchema(
            name = "root3",
            packageName = "package.b",
            entityName = "empty_entity"
        ),
        entities = listOf(
            MutableEntitySchema(
                name = "entity_1",
                fields = listOf(
                    MutableAliasFieldSchema(
                        name = "invalid_alias_field",
                        packageName = "package.b",
                        aliasName = "no_such_alias"
                    ),
                    MutableEnumerationFieldSchema(
                        name = "invalid_enumeration_field",
                        packageName = "package.b",
                        enumerationName = "no_such_enumeration"
                    ),
                    MutableCompositionFieldSchema(
                        name = "invalid_composition_field",
                        packageName = "no.such.package",
                        entityName = "entity_1"
                    )
                )
            )
        )
    )

    return listOf(packageA, packageB, packageC)
}
