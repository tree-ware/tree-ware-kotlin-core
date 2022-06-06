package org.treeWare.metaModel

import org.treeWare.model.core.*

// Functions for creating the meta-meta-model.

fun newMainMetaMeta(): MutableMainModel {
    val mainMeta = MutableMainModel(null)
    mainMeta.root = MutableEntityModel(null, mainMeta)
    return mainMeta
}

fun newRootMetaMeta(mainMeta: MutableMainModel, name: String, entityName: String, packageName: String) {
    val root = newCompositionSingleField(mainMeta.root, "root")
    newStringSingleField(root, "name", name)
    newEnumerationSingleField(root, "type", "composition")
    val composition = newCompositionSingleField(root, "composition")
    newStringSingleField(composition, "entity", entityName)
    newStringSingleField(composition, "package", packageName)
}

fun newPackagesMetaMeta(mainMeta: MutableMainModel): MutableListFieldModel =
    newCompositionListField(mainMeta.root, "packages")

fun newPackageMetaMeta(
    packagesMeta: MutableListFieldModel,
    name: String,
    info: String? = null
): MutableEntityModel {
    val pkg = newCompositionListElement(packagesMeta)
    newStringSingleField(pkg, "name", name)
    info?.also { newStringSingleField(pkg, "info", it) }
    return pkg
}

fun newEntitiesMetaMeta(packageMeta: MutableEntityModel): MutableListFieldModel =
    newCompositionListField(packageMeta, "entities")

fun newEntityMetaMeta(
    entitiesMeta: MutableListFieldModel,
    name: String,
    info: String? = null
): MutableEntityModel {
    val entity = newCompositionListElement(entitiesMeta)
    newStringSingleField(entity, "name", name)
    info?.also { newStringSingleField(entity, "info", it) }
    return entity
}

fun newFieldsMetaMeta(entityMeta: MutableEntityModel) =
    newCompositionListField(entityMeta, "fields")

fun newEnumerationsMetaMeta(packageMeta: MutableEntityModel): MutableListFieldModel =
    newCompositionListField(packageMeta, "enumerations")

data class EnumerationValueMetaMeta(val name: String, val info: String? = null)

fun newEnumerationMetaMeta(
    enumerations: MutableListFieldModel,
    name: String,
    info: String?,
    values: List<EnumerationValueMetaMeta>
) {
    val enumeration = newCompositionListElement(enumerations)
    newStringSingleField(enumeration, "name", name)
    info?.also { newStringSingleField(enumeration, "info", it) }
    val valueList = newCompositionListField(enumeration, "values")
    values.forEach { (valueName, valueInfo) ->
        val value = newCompositionListElement(valueList)
        newStringSingleField(value, "name", valueName)
        valueInfo?.also { newStringSingleField(value, "info", it) }
    }
}

fun newPrimitiveFieldMetaMeta(
    parent: MutableListFieldModel,
    name: String,
    info: String?,
    type: String,
    multiplicity: String? = null,
    isKey: Boolean = false
): MutableEntityModel = newFieldMetaMeta(parent, name, info, type, multiplicity, isKey)

fun newEnumerationFieldMetaMeta(
    parent: MutableListFieldModel,
    name: String,
    info: String?,
    enumerationName: String,
    packageName: String,
    multiplicity: String? = null,
    isKey: Boolean = false
): MutableEntityModel {
    val fieldMeta = newFieldMetaMeta(parent, name, info, "enumeration", multiplicity, isKey)
    val entityMeta = newCompositionSingleField(fieldMeta, "enumeration")
    newStringSingleField(entityMeta, "name", enumerationName)
    newStringSingleField(entityMeta, "package", packageName)
    return fieldMeta
}

fun newCompositionFieldMetaMeta(
    parent: MutableListFieldModel,
    name: String,
    info: String?,
    entityName: String,
    packageName: String,
    multiplicity: String? = null,
    isKey: Boolean = false
): MutableEntityModel {
    val fieldMeta = newFieldMetaMeta(parent, name, info, "composition", multiplicity, isKey)
    val entityMeta = newCompositionSingleField(fieldMeta, "composition")
    newStringSingleField(entityMeta, "entity", entityName)
    newStringSingleField(entityMeta, "package", packageName)
    return fieldMeta
}

private fun newFieldMetaMeta(
    parent: MutableListFieldModel,
    name: String,
    info: String?,
    type: String,
    multiplicity: String?,
    isKey: Boolean
): MutableEntityModel {
    val fieldMeta = MutableEntityModel(null, parent)
    newStringSingleField(fieldMeta, "name", name)
    info?.also { newStringSingleField(fieldMeta, "info", it) }
    newEnumerationSingleField(fieldMeta, "type", type)
    multiplicity?.also { newEnumerationSingleField(fieldMeta, "multiplicity", it) }
    newBooleanSingleField(fieldMeta, "is_key", isKey)
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

private val dummyMain = MutableMainModel(null)
private val dummyRoot = MutableEntityModel(null, dummyMain)
private val dummyField = MutableSingleFieldModel(null, dummyRoot)

private val stringFieldMeta = getFieldTypeMeta("string")
private val booleanFieldMeta = getFieldTypeMeta("boolean")

private fun getFieldTypeMeta(type: String): MutableEntityModel {
    val fieldMeta = MutableEntityModel(null, dummyField)
    newEnumerationSingleField(fieldMeta, "type", type)
    return fieldMeta
}

// Helpers
// NOTE: model setters require meta-models. Since we don't have
// meta-meta-meta-models, we cannot use model setters to create
// meta-meta-models.

private fun newCompositionSingleField(entity: MutableBaseEntityModel, name: String): MutableEntityModel {
    val field = MutableSingleFieldModel(null, entity)
    entity.fields[name] = field
    val value = MutableEntityModel(null, field)
    field.value = value
    return value
}

private fun newStringSingleField(entity: MutableBaseEntityModel, name: String, value: String) {
    val field = MutableSingleFieldModel(stringFieldMeta, entity)
    entity.fields[name] = field
    val primitive = MutablePrimitiveModel(field, value)
    field.value = primitive
}

private fun newBooleanSingleField(entity: MutableBaseEntityModel, name: String, value: Boolean) {
    val field = MutableSingleFieldModel(booleanFieldMeta, entity)
    entity.fields[name] = field
    val primitive = MutablePrimitiveModel(field, value)
    field.value = primitive
}

private fun newEnumerationSingleField(entity: MutableBaseEntityModel, name: String, value: String) {
    val field = MutableSingleFieldModel(null, entity)
    entity.fields[name] = field
    val enumeration = MutableEnumerationModel(field, value)
    field.value = enumeration
}

private fun newStringListField(entity: MutableBaseEntityModel, name: String): MutableListFieldModel {
    val field = MutableListFieldModel(stringFieldMeta, entity)
    entity.fields[name] = field
    return field
}

private fun addStringToListField(listField: MutableListFieldModel, value: String) {
    val primitive = MutablePrimitiveModel(listField, value)
    listField.addValue(primitive)
}

private fun newCompositionListField(entity: MutableBaseEntityModel, name: String): MutableListFieldModel {
    val field = MutableListFieldModel(null, entity)
    entity.fields[name] = field
    return field
}

private fun newCompositionListElement(listField: MutableListFieldModel): MutableEntityModel {
    val entity = MutableEntityModel(null, listField)
    listField.addValue(entity)
    return entity
}