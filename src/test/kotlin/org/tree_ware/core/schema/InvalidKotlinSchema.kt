package org.tree_ware.core.schema

fun getInvalidKotlinPackages(): List<MutablePackageSchema> {
    val packageA = MutablePackageSchema(
            name = "hyphens-not-allowed-for-packages",
            entities = listOf(
                    MutableEntitySchema(
                            name = "dots.not_allowed_for.entities",
                            fields = listOf(
                                    MutablePrimitiveFieldSchema(
                                            name = "dots.not_allowed_for.primitive_fields",
                                            primitive = MutableStringSchema()
                                    ),
                                    MutablePrimitiveFieldSchema(
                                            name = "hyphens-not-allowed-for-primitive-fields",
                                            primitive = MutableStringSchema()
                                    ),
                                    MutablePrimitiveFieldSchema(
                                            name = "invalid_string_field_multiplicity",
                                            primitive = MutableStringSchema(),
                                            multiplicity = MutableMultiplicity(-1, 0)
                                    )
                            )
                    ),
                    MutableEntitySchema(
                            name = "hyphens-not-allowed-for-entities",
                            fields = listOf(
                                    MutableEntityFieldSchema(
                                            name = "dots.not_allowed_for.entity_fields",
                                            packageName = "hyphens-not-allowed-for-packages",
                                            entityName = "dots.not_allowed_for.entities"
                                    ),
                                    MutableEntityFieldSchema(
                                            name = "hyphens-not-allowed-for-entity-fields",
                                            packageName = "hyphens-not-allowed-for-packages",
                                            entityName = "dots.not_allowed_for.entities"
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
                                    MutableEntityFieldSchema(
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
            entities = listOf(
                    MutableEntitySchema(
                            name = "entity_b",
                            fields = listOf(
                                    MutableEntityFieldSchema(
                                            name = "invalid_entity_field_multiplicity",
                                            packageName = "package.b",
                                            entityName = "empty_entity",
                                            multiplicity = MutableMultiplicity(0, -1)
                                    ),
                                    MutableAliasFieldSchema(
                                            name = "invalid_alias_field_multiplicity",
                                            packageName = "package.b",
                                            aliasName = "hyphens-not-allowed-for-aliases",
                                            multiplicity = MutableMultiplicity(2, 1)
                                    )
                            )
                    ),
                    MutableEntitySchema(
                            name = "empty_entity",
                            fields = listOf()
                    )
            )
    )

    val packageC = MutablePackageSchema(
            name = "package.c",
            entities = listOf(
                    MutableEntitySchema(
                            name = "entity_1",
                            fields = listOf(
                                    MutableEntityFieldSchema(
                                            name = "invalid_entity_field",
                                            packageName = "no.such.package",
                                            entityName = "entity_1",
                                            multiplicity = MutableMultiplicity(0, 1)
                                    ),
                                    MutableAliasFieldSchema(
                                            name = "invalid_alias_field",
                                            packageName = "package.b",
                                            aliasName = "no_such_alias",
                                            multiplicity = MutableMultiplicity(1, 10)
                                    )
                            )
                    )
            )
    )

    return listOf(packageA, packageB, packageC)
}
