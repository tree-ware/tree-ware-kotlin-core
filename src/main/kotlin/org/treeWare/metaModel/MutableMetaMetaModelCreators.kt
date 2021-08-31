package org.treeWare.metaModel

import org.treeWare.model.core.*

// Functions for creating the meta-meta-model.

fun newMainMetaMeta(): MutableModel<Resolved> {
    val mainMeta = MutableModel<Resolved>(null)
    mainMeta.root = MutableRootModel(null, mainMeta)
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
    val fieldMeta = MutableEntityModel(null, parent)
    newSingleStringField(fieldMeta, "name", name)
    info?.also { newSingleStringField(fieldMeta, "info", it) }
    newSingleEnumerationField(fieldMeta, "type", type)
    multiplicity?.also { newSingleEnumerationField(fieldMeta, "multiplicity", it) }
    newSingleBooleanField(fieldMeta, "is_key", isKey)
    parent.addValue(fieldMeta)
    return fieldMeta
}

// Meta-meta-meta-models for field types.
//
// While a full-blow meta-meta-meta-model is not needed, there is still a need
// for meta-meta-meta-models for primitive field types.
//
// To create meta-meta-meta-model instances for field types, parent instances
// need to be passed to the model constructors. So dummy parent instances are
// created and used.

private val dummyMain = MutableModel<Resolved>(null)
private val dummyRoot = MutableRootModel(null, dummyMain)
private val dummyField = MutableSingleFieldModel(null, dummyRoot)

private val stringFieldMeta = getFieldTypeMeta("string")
private val booleanFieldMeta = getFieldTypeMeta("boolean")

private fun getFieldTypeMeta(type: String): MutableEntityModel<Resolved> {
    val fieldMeta = MutableEntityModel(null, dummyField)
    newSingleEnumerationField(fieldMeta, "type", type)
    return fieldMeta
}

// Helpers
// NOTE: model setters require meta-models. Since we don't have
// meta-meta-meta-models, we cannot use model setters to create
// meta-meta-models.

private fun newSingleEntityField(entity: MutableBaseEntityModel<Resolved>, name: String): MutableEntityModel<Resolved> {
    val field = MutableSingleFieldModel(null, entity)
    entity.fields[name] = field
    val value = MutableEntityModel(null, field)
    field.value = value
    return value
}

private fun newSingleStringField(entity: MutableBaseEntityModel<Resolved>, name: String, value: String) {
    val field = MutableSingleFieldModel(stringFieldMeta, entity)
    entity.fields[name] = field
    val primitive = MutablePrimitiveModel(field)
    field.value = primitive
    primitive.setValue(value)
}

private fun newSingleBooleanField(entity: MutableBaseEntityModel<Resolved>, name: String, value: Boolean) {
    val field = MutableSingleFieldModel(booleanFieldMeta, entity)
    entity.fields[name] = field
    val primitive = MutablePrimitiveModel(field)
    field.value = primitive
    primitive.setValue(value)
}

private fun newSingleEnumerationField(
    entity: MutableBaseEntityModel<Resolved>,
    name: String,
    value: String
) {
    val field = MutableSingleFieldModel(null, entity)
    entity.fields[name] = field
    val enumeration = MutableEnumerationModel(field)
    field.value = enumeration
    enumeration.setValue(value)
}

private fun newListStringField(
    entity: MutableBaseEntityModel<Resolved>,
    name: String
): MutableListFieldModel<Resolved> {
    val field = MutableListFieldModel(stringFieldMeta, entity)
    entity.fields[name] = field
    return field
}

private fun addStringToListField(listField: MutableListFieldModel<Resolved>, value: String) {
    val primitive = MutablePrimitiveModel(listField)
    primitive.setValue(value)
    listField.addValue(primitive)
}

private fun newListEntityField(
    entity: MutableBaseEntityModel<Resolved>,
    name: String
): MutableListFieldModel<Resolved> {
    val field = MutableListFieldModel(null, entity)
    entity.fields[name] = field
    return field
}

private fun newListEntityElement(listField: MutableListFieldModel<Resolved>): MutableEntityModel<Resolved> {
    val entity = MutableEntityModel(null, listField)
    listField.addValue(entity)
    return entity
}
