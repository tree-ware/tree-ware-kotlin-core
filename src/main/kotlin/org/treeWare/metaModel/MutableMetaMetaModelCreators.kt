package org.treeWare.metaModel

import org.treeWare.model.core.*
import org.treeWare.schema.core.*

// Functions for creating the meta-meta-model.

fun newMainMetaMeta(): MutableModel<Resolved> {
    val mainMeta = MutableModel<Resolved>(metaModelSchema, null)
    mainMeta.root = MutableRootModel(mainMeta.schema.root, null, mainMeta)
    return mainMeta
}

fun newRootMetaMeta(mainMeta: MutableModel<Resolved>, name: String, entityName: String, packageName: String) {
    val root = newSingleEntityField(mainMeta.root, "root")
    newSingleStringField(root, "name", name)
    newSingleStringField(root, "entity", entityName)
    newSingleStringField(root, "package", packageName)
}

fun newPackagesMetaMeta(mainMeta: MutableModel<Resolved>): MutableListFieldModel<Resolved> =
    newListEntityField(mainMeta.root, "packages")

fun newPackageMetaMeta(
    packagesMeta: MutableListFieldModel<Resolved>,
    name: String,
    info: String? = null
): MutableEntityModel<Resolved> {
    val pkg = newListEntityElement(packagesMeta)
    newSingleStringField(pkg, "name", name)
    info?.also { newSingleStringField(pkg, "info", it) }
    return pkg
}

fun newEntitiesMetaMeta(packageMeta: MutableEntityModel<Resolved>): MutableListFieldModel<Resolved> =
    newListEntityField(packageMeta, "entities")

fun newEntityMetaMeta(
    entitiesMeta: MutableListFieldModel<Resolved>,
    name: String,
    info: String? = null
): MutableEntityModel<Resolved> {
    val entity = newListEntityElement(entitiesMeta)
    newSingleStringField(entity, "name", name)
    info?.also { newSingleStringField(entity, "info", it) }
    return entity
}

fun newFieldsMetaMeta(entityMeta: MutableEntityModel<Resolved>) =
    newListEntityField(entityMeta, "fields")

fun newEnumerationsMetaMeta(packageMeta: MutableEntityModel<Resolved>): MutableListFieldModel<Resolved> =
    newListEntityField(packageMeta, "enumerations")

data class EnumerationValueMetaMeta(val name: String, val info: String? = null)

fun newEnumerationMetaMeta(
    enumerations: MutableListFieldModel<Resolved>,
    name: String,
    info: String?,
    values: List<EnumerationValueMetaMeta>
) {
    val enumeration = newListEntityElement(enumerations)
    newSingleStringField(enumeration, "name", name)
    info?.also { newSingleStringField(enumeration, "info", it) }
    val valueList = newListEntityField(enumeration, "values")
    values.forEach { (valueName, valueInfo) ->
        val value = newListEntityElement(valueList)
        newSingleStringField(value, "name", valueName)
        valueInfo?.also { newSingleStringField(value, "info", it) }
    }
}

fun newPrimitiveFieldMetaMeta(
    parent: MutableListFieldModel<Resolved>,
    name: String,
    info: String?,
    type: String,
    multiplicity: String? = null,
    isKey: Boolean = false
): MutableEntityModel<Resolved> = newFieldMetaMeta(parent, name, info, type, multiplicity, isKey)

fun newEnumerationFieldMetaMeta(
    parent: MutableListFieldModel<Resolved>,
    name: String,
    info: String?,
    enumerationName: String,
    packageName: String,
    multiplicity: String? = null,
    isKey: Boolean = false
): MutableEntityModel<Resolved> {
    val fieldMeta = newFieldMetaMeta(parent, name, info, "enumeration", multiplicity, isKey)
    val entityMeta = newSingleEntityField(fieldMeta, "enumeration")
    newSingleStringField(entityMeta, "name", enumerationName)
    newSingleStringField(entityMeta, "package", packageName)
    return fieldMeta
}

fun newAssociationFieldMetaMeta(
    parent: MutableListFieldModel<Resolved>,
    name: String,
    info: String?,
    entityPath: List<String>,
    multiplicity: String? = null,
    isKey: Boolean = false
): MutableEntityModel<Resolved> {
    val fieldMeta = newFieldMetaMeta(parent, name, info, "association", multiplicity, isKey)
    val associationMeta = newListStringField(fieldMeta, "association")
    entityPath.forEach { addStringToListField(associationMeta, it) }
    return fieldMeta
}

fun newEntityFieldMetaMeta(
    parent: MutableListFieldModel<Resolved>,
    name: String,
    info: String?,
    entityName: String,
    packageName: String,
    multiplicity: String? = null,
    isKey: Boolean = false
): MutableEntityModel<Resolved> {
    val fieldMeta = newFieldMetaMeta(parent, name, info, "entity", multiplicity, isKey)
    val entityMeta = newSingleEntityField(fieldMeta, "entity")
    newSingleStringField(entityMeta, "name", entityName)
    newSingleStringField(entityMeta, "package", packageName)
    return fieldMeta
}

private fun newFieldMetaMeta(
    parent: MutableListFieldModel<Resolved>,
    name: String,
    info: String?,
    type: String,
    multiplicity: String?,
    isKey: Boolean
): MutableEntityModel<Resolved> {
    val entitySchema = MutableEntitySchema("field", fields = listOf())
    val fieldMeta = MutableEntityModel(entitySchema, null, parent)
    newSingleStringField(fieldMeta, "name", name)
    info?.also { newSingleStringField(fieldMeta, "info", it) }
    newSingleEnumerationField(fieldTypeEnumeration, fieldMeta, "type", type)
    multiplicity?.also { newSingleEnumerationField(multiplicityEnumeration, fieldMeta, "multiplicity", it) }
    newSingleBooleanField(fieldMeta, "is_key", isKey)
    parent.addValue(fieldMeta)
    return fieldMeta
}

// Helpers
// NOTE: model setters require meta-models. Since we don't have
// meta-meta-meta-models, we cannot use model setters to create
// meta-meta-models.

private fun newSingleEntityField(entity: MutableBaseEntityModel<Resolved>, name: String): MutableEntityModel<Resolved> {
    val fieldSchema = MutableCompositionFieldSchema(name, null, "", "")
    val entitySchema = MutableEntitySchema(name, null, listOf())
    val field = MutableSingleFieldModel(fieldSchema, null, entity)
    entity.fields.add(field)
    val value = MutableEntityModel(entitySchema, null, field)
    field.value = value
    return value
}

private fun newSingleStringField(entity: MutableBaseEntityModel<Resolved>, name: String, value: String) {
    val fieldSchema = MutablePrimitiveFieldSchema(name, null, MutableStringSchema())
    val field = MutableSingleFieldModel(fieldSchema, null, entity)
    entity.fields.add(field)
    val primitive = MutablePrimitiveModel(fieldSchema, field)
    field.value = primitive
    primitive.setValue(value)
}

private fun newSingleBooleanField(entity: MutableBaseEntityModel<Resolved>, name: String, value: Boolean) {
    val fieldSchema = MutablePrimitiveFieldSchema(name, null, MutableBooleanSchema())
    val field = MutableSingleFieldModel(fieldSchema, null, entity)
    entity.fields.add(field)
    val primitive = MutablePrimitiveModel(fieldSchema, field)
    field.value = primitive
    primitive.setValue(value)
}

private fun newSingleEnumerationField(
    enumerationSchema: MutableEnumerationSchema,
    entity: MutableBaseEntityModel<Resolved>,
    name: String,
    value: String
) {
    val fieldSchema = MutableEnumerationFieldSchema(name, null, "", "")
    fieldSchema.resolvedEnumeration = enumerationSchema
    val field = MutableSingleFieldModel(fieldSchema, null, entity)
    entity.fields.add(field)
    val primitive = MutableEnumerationModel(fieldSchema, field)
    field.value = primitive
    primitive.setValue(value)
}

private fun newListStringField(
    entity: MutableBaseEntityModel<Resolved>,
    name: String
): MutableListFieldModel<Resolved> {
    val fieldSchema = MutablePrimitiveFieldSchema(name, null, MutableStringSchema())
    val field = MutableListFieldModel(fieldSchema, null, entity)
    entity.fields.add(field)
    return field
}

private fun addStringToListField(listField: MutableListFieldModel<Resolved>, value: String) {
    val fieldSchema = MutablePrimitiveFieldSchema("", null, MutableStringSchema())
    val primitive = MutablePrimitiveModel(fieldSchema, listField)
    primitive.setValue(value)
    listField.addValue(primitive)
}

private fun newListEntityField(
    entity: MutableBaseEntityModel<Resolved>,
    name: String
): MutableListFieldModel<Resolved> {
    val fieldSchema = MutableCompositionFieldSchema(name, null, "", "")
    val field = MutableListFieldModel(fieldSchema, null, entity)
    entity.fields.add(field)
    return field
}

private fun newListEntityElement(listField: MutableListFieldModel<Resolved>): MutableEntityModel<Resolved> {
    val entitySchema = MutableEntitySchema("", fields = listOf())
    val entity = MutableEntityModel(entitySchema, null, listField)
    listField.addValue(entity)
    return entity
}
