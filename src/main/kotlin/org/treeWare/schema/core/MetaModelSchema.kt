package org.treeWare.schema.core

val multiplicityTypeEnumeration = MutableEnumerationSchema(
    name = "multiplicity_type",
    values = listOf(
        MutableEnumerationValueSchema("required"),
        MutableEnumerationValueSchema("optional"),
        MutableEnumerationValueSchema("list"),
    )
)

val fieldTypeEnumeration = MutableEnumerationSchema(
    name = "field_type",
    values = listOf(
        MutableEnumerationValueSchema("boolean"),
        MutableEnumerationValueSchema("byte"),
        MutableEnumerationValueSchema("short"),
        MutableEnumerationValueSchema("int"),
        MutableEnumerationValueSchema("long"),
        MutableEnumerationValueSchema("float"),
        MutableEnumerationValueSchema("double"),
        MutableEnumerationValueSchema("string"),
        MutableEnumerationValueSchema("password1way"),
        MutableEnumerationValueSchema("password2way"),
        MutableEnumerationValueSchema("uuid"),
        MutableEnumerationValueSchema("blob"),
        MutableEnumerationValueSchema("timestamp"),
        MutableEnumerationValueSchema("enumeration"),
        MutableEnumerationValueSchema("association"),
        MutableEnumerationValueSchema("entity"),
    )
)

val metaModelRoot = MutableRootSchema(
    name = "meta_model",
    packageName = "meta_model.main",
    entityName = "meta_model"
)

val metaModelEntity = MutableEntitySchema(
    name = "meta_model",
    fields = listOf(
        MutableCompositionFieldSchema(
            name = "root",
            packageName = "meta_model.main",
            entityName = "root"
        ),
        MutableCompositionFieldSchema(
            name = "packages",
            packageName = "meta_model.main",
            entityName = "package",
            multiplicity = MutableMultiplicity(0, 0)
        )
    )
)

val rootEntity = MutableEntitySchema(
    name = "root",
    fields = listOf(
        MutablePrimitiveFieldSchema(
            name = "name",
            primitive = MutableStringSchema()
        ),
        MutablePrimitiveFieldSchema(
            name = "info",
            primitive = MutableStringSchema(),
            multiplicity = MutableMultiplicity(0, 1)
        ),
        MutablePrimitiveFieldSchema(
            name = "entity",
            primitive = MutableStringSchema()
        ),
        MutablePrimitiveFieldSchema(
            name = "package",
            primitive = MutableStringSchema()
        ),
    )
)

val packageEntity = MutableEntitySchema(
    name = "package",
    fields = listOf(
        MutablePrimitiveFieldSchema(
            name = "name",
            primitive = MutableStringSchema(),
            isKey = true
        ),
        MutablePrimitiveFieldSchema(
            name = "info",
            primitive = MutableStringSchema(),
            multiplicity = MutableMultiplicity(0, 1)
        ),
        MutableCompositionFieldSchema(
            name = "enumerations",
            packageName = "meta_model.main",
            entityName = "enumeration",
            multiplicity = MutableMultiplicity(0, 0)
        ),
        MutableCompositionFieldSchema(
            name = "entities",
            packageName = "meta_model.main",
            entityName = "entity",
            multiplicity = MutableMultiplicity(0, 0)
        )
    )
)

val enumerationEntity = MutableEntitySchema(
    name = "enumeration",
    fields = listOf(
        MutablePrimitiveFieldSchema(
            name = "name",
            primitive = MutableStringSchema(),
            isKey = true
        ),
        MutablePrimitiveFieldSchema(
            name = "info",
            primitive = MutableStringSchema(),
            multiplicity = MutableMultiplicity(0, 1)
        ),
        MutableCompositionFieldSchema(
            name = "values",
            packageName = "meta_model.main",
            entityName = "enumeration_value",
            multiplicity = MutableMultiplicity(0, 0)
        )
    )
)

val enumerationValueEntity = MutableEntitySchema(
    name = "enumeration_value",
    fields = listOf(
        MutablePrimitiveFieldSchema(
            name = "name",
            primitive = MutableStringSchema(),
            isKey = true
        ),
        MutablePrimitiveFieldSchema(
            name = "info",
            primitive = MutableStringSchema(),
            multiplicity = MutableMultiplicity(0, 1)
        )
    )
)

val entityEntity = MutableEntitySchema(
    name = "entity",
    fields = listOf(
        MutablePrimitiveFieldSchema(
            name = "name",
            primitive = MutableStringSchema(),
            isKey = true
        ),
        MutablePrimitiveFieldSchema(
            name = "info",
            primitive = MutableStringSchema(),
            multiplicity = MutableMultiplicity(0, 1)
        ),
        MutableCompositionFieldSchema(
            name = "fields",
            packageName = "meta_model.main",
            entityName = "field",
            multiplicity = MutableMultiplicity(0, 0)
        ),
    ),
)

val fieldEntity = MutableEntitySchema(
    name = "field",
    fields = listOf(
        MutablePrimitiveFieldSchema(
            name = "name",
            primitive = MutableStringSchema(),
            isKey = true
        ),
        MutablePrimitiveFieldSchema(
            name = "info",
            primitive = MutableStringSchema(),
            multiplicity = MutableMultiplicity(0, 1)
        ),
        MutableEnumerationFieldSchema(
            name = "type",
            packageName = "meta_model.main",
            enumerationName = "field_type",
            multiplicity = MutableMultiplicity(0, 1)
        ),
        MutableCompositionFieldSchema(
            name = "enumeration",
            packageName = "meta_model.main",
            entityName = "enumeration_info",
            multiplicity = MutableMultiplicity(0, 1)
        ),
        MutablePrimitiveFieldSchema(
            name = "association",
            primitive = MutableStringSchema(),
            multiplicity = MutableMultiplicity(0, 0)
        ),
        MutableCompositionFieldSchema(
            name = "entity",
            packageName = "meta_model.main",
            entityName = "entity_info",
            multiplicity = MutableMultiplicity(0, 1)
        ),
        MutablePrimitiveFieldSchema(
            name = "is_key",
            primitive = MutableBooleanSchema(),
            multiplicity = MutableMultiplicity(0, 1)
        ),
        MutableEnumerationFieldSchema(
            name = "multiplicity",
            packageName = "meta_model.main",
            enumerationName = "multiplicity_type",
            multiplicity = MutableMultiplicity(0, 1)
        ),
    ),
)

val enumerationInfoEntity = MutableEntitySchema(
    name = "enumeration_info",
    fields = listOf(
        MutablePrimitiveFieldSchema(
            name = "name",
            primitive = MutableStringSchema()
        ),
        MutablePrimitiveFieldSchema(
            name = "package",
            primitive = MutableStringSchema()
        ),
    )
)

val entityInfoEntity = MutableEntitySchema(
    name = "entity_info",
    fields = listOf(
        MutablePrimitiveFieldSchema(
            name = "name",
            primitive = MutableStringSchema()
        ),
        MutablePrimitiveFieldSchema(
            name = "package",
            primitive = MutableStringSchema()
        ),
    )
)

val metaModelMainPackage = MutablePackageSchema(
    name = "meta_model.main",
    entities = listOf(
        metaModelEntity,
        rootEntity,
        packageEntity,
        enumerationEntity,
        enumerationValueEntity,
        entityEntity,
        fieldEntity,
        enumerationInfoEntity,
        entityInfoEntity
    ),
    enumerations = listOf(multiplicityTypeEnumeration, fieldTypeEnumeration)
)

val metaModelSchema = MutableSchema(metaModelRoot, listOf(metaModelMainPackage)).also {
    val errors = validate(it)
    if (errors.isNotEmpty()) throw IllegalStateException()
}
