package org.tree_ware.core.schema

fun getInvalidNamesKotlinPackages(): List<MutablePackageSchema> {
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
            )
    )

    return listOf(packageA, packageB)
}
