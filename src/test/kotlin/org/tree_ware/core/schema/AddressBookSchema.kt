package org.tree_ware.core.schema

val addressBookPackage = MutablePackageSchema(
    name = "address_book",
    info = "Schema for storing address book information",
    root = MutableRootSchema(
        name = "address_book",
        packageName = "address_book",
        entityName = "address_book_root"
    ),
    entities = listOf(
        MutableEntitySchema(
            name = "address_book_root",
            fields = listOf(
                MutableCompositionFieldSchema(
                    name = "person",
                    packageName = "address_book",
                    entityName = "person",
                    multiplicity = MutableMultiplicity(0, 0)
                )
            )
        ),
        MutableEntitySchema(
            name = "person",
            fields = listOf(
                MutablePrimitiveFieldSchema(
                    name = "id",
                    primitive = MutableUuidSchema(),
                    isKey = true
                ),
                MutablePrimitiveFieldSchema(
                    name = "first_name",
                    primitive = MutableStringSchema()
                ),
                MutablePrimitiveFieldSchema(
                    name = "last_name",
                    primitive = MutableStringSchema()
                ),
                MutablePrimitiveFieldSchema(
                    name = "email",
                    primitive = MutableStringSchema(),
                    multiplicity = MutableMultiplicity(0, 1)
                ),
                MutableCompositionFieldSchema(
                    name = "relation",
                    packageName = "address_book",
                    entityName = "relation",
                    multiplicity = MutableMultiplicity(0, 0)
                )
            )
        ),
        MutableEntitySchema(
            name = "relation",
            fields = listOf(
                MutablePrimitiveFieldSchema(
                    name = "id",
                    primitive = MutableUuidSchema(),
                    isKey = true
                ),
                MutableEnumerationFieldSchema(
                    name = "relationship",
                    packageName = "address_book",
                    enumerationName = "relationship"
                ),
                MutableAssociationFieldSchema(
                    name = "person",
                    entityPath = listOf(
                        "address_book",
                        "person"
                    )
                )
            )
        )
    ),
    enumerations = listOf(
        MutableEnumerationSchema(
            name = "relationship",
            values = listOf(
                MutableEnumerationValueSchema(
                    name = "parent"
                ),
                MutableEnumerationValueSchema(
                    name = "child"
                ),
                MutableEnumerationValueSchema(
                    name = "spouse"
                ),
                MutableEnumerationValueSchema(
                    name = "sibling"
                ),
                MutableEnumerationValueSchema(
                    name = "family"
                ),
                MutableEnumerationValueSchema(
                    name = "friend"
                ),
                MutableEnumerationValueSchema(
                    name = "colleague"
                )
            )
        )
    )
)