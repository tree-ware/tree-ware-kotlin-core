package org.treeWare.metaModel

import org.treeWare.metaModel.validation.validate
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableListFieldModel
import org.treeWare.model.core.MutableMainModel

enum class Multiplicity { REQUIRED, OPTIONAL, LIST, SET }

enum class FieldType {
    BOOLEAN,
    UINT8,
    UINT16,
    UINT32,
    UINT64,
    INT8,
    INT16,
    INT32,
    INT64,
    FLOAT,
    DOUBLE,
    BIG_INTEGER,
    BIG_DECIMAL,
    TIMESTAMP,
    STRING,
    UUID,
    BLOB,
    PASSWORD1WAY,
    PASSWORD2WAY,
    ALIAS,
    ENUMERATION,
    ASSOCIATION,
    COMPOSITION
}

private const val META_MODEL_MAIN_PACKAGE = "tree_ware_meta_model.main"

fun newMainMetaMetaModel(): MutableMainModel {
    val mainMeta = newMainMetaMeta()
    populateMain(mainMeta)
    val errors = validate(mainMeta, null, null, mandatoryFieldNumbers = false)
    if (errors.isNotEmpty()) throw IllegalStateException("Meta-meta-model is not valid")
    return mainMeta
}

private fun populateMain(mainMeta: MutableMainModel) {
    newRootMetaMeta(mainMeta, "meta_model", "meta_model", "tree_ware_meta_model.main")
    val packagesMeta = newPackagesMetaMeta(mainMeta)
    populatePackages(packagesMeta)
}

private fun populatePackages(packagesMeta: MutableListFieldModel) {
    val mainPackage = newPackageMetaMeta(packagesMeta, META_MODEL_MAIN_PACKAGE)
    populateMainPackage(mainPackage)
}

private fun populateMainPackage(mainPackage: MutableEntityModel) {
    val entitiesMeta = newEntitiesMetaMeta(mainPackage)
    populateMainEntities(entitiesMeta)
    val enumerationsMeta = newEnumerationsMetaMeta(mainPackage)
    populateMainEnumerations(enumerationsMeta)
}

private fun populateMainEntities(entitiesMeta: MutableListFieldModel) {
    val metaModelEntityMeta = newEntityMetaMeta(entitiesMeta, "meta_model")
    populateMetaModelEntity(metaModelEntityMeta)
    val rootEntityMeta = newEntityMetaMeta(entitiesMeta, "root")
    populateRootEntity(rootEntityMeta)
    val packageEntityMeta = newEntityMetaMeta(entitiesMeta, "package")
    populatePackageEntity(packageEntityMeta)
    val enumerationEntityMeta = newEntityMetaMeta(entitiesMeta, "enumeration")
    populateEnumerationEntity(enumerationEntityMeta)
    val enumerationValueEntityMeta = newEntityMetaMeta(entitiesMeta, "enumeration_value")
    populateEnumerationValueEntity(enumerationValueEntityMeta)
    val entityEntityMeta = newEntityMetaMeta(entitiesMeta, "entity")
    populateEntityEntity(entityEntityMeta)
    val fieldEntityMeta = newEntityMetaMeta(entitiesMeta, "field")
    populateFieldEntity(fieldEntityMeta)
    val enumerationInfoEntityMeta = newEntityMetaMeta(entitiesMeta, "enumeration_info")
    populateEnumerationInfoEntity(enumerationInfoEntityMeta)
    val entityInfoEntityMeta = newEntityMetaMeta(entitiesMeta, "entity_info")
    populateEntityInfoEntity(entityInfoEntityMeta)
}

private fun populateMetaModelEntity(metaModelEntityMeta: MutableEntityModel) {
    val fields = newFieldsMetaMeta(metaModelEntityMeta)
    newCompositionFieldMetaMeta(fields, "root", null, "root", META_MODEL_MAIN_PACKAGE)
    newCompositionFieldMetaMeta(fields, "packages", null, "package", META_MODEL_MAIN_PACKAGE, "set")
}

private fun populateRootEntity(rootEntityMeta: MutableEntityModel) {
    val fields = newFieldsMetaMeta(rootEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string")
    newPrimitiveFieldMetaMeta(fields, "info", null, "string", "optional")
    newEnumerationFieldMetaMeta(fields, "type", null, "field_type", META_MODEL_MAIN_PACKAGE, "optional")
    newCompositionFieldMetaMeta(fields, "composition", null, "entity_info", META_MODEL_MAIN_PACKAGE, "required")
}

private fun populatePackageEntity(packageEntityMeta: MutableEntityModel) {
    val fields = newFieldsMetaMeta(packageEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string", null, true)
    newPrimitiveFieldMetaMeta(fields, "info", null, "string", "optional")
    newCompositionFieldMetaMeta(fields, "enumerations", null, "enumeration", META_MODEL_MAIN_PACKAGE, "set")
    newCompositionFieldMetaMeta(fields, "entities", null, "entity", META_MODEL_MAIN_PACKAGE, "set")
}

private fun populateEnumerationEntity(enumerationEntityMeta: MutableEntityModel) {
    val fields = newFieldsMetaMeta(enumerationEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string", null, true)
    newPrimitiveFieldMetaMeta(fields, "info", null, "string", "optional")
    newCompositionFieldMetaMeta(fields, "values", null, "enumeration_value", META_MODEL_MAIN_PACKAGE, "set")
}

private fun populateEnumerationValueEntity(enumerationValueEntityMeta: MutableEntityModel) {
    val fields = newFieldsMetaMeta(enumerationValueEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string", null, true)
    newPrimitiveFieldMetaMeta(fields, "number", null, "uint32", "optional")
    newPrimitiveFieldMetaMeta(fields, "info", null, "string", "optional")
}

private fun populateEntityEntity(entityEntityMeta: MutableEntityModel) {
    val fields = newFieldsMetaMeta(entityEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string", null, true)
    newPrimitiveFieldMetaMeta(fields, "info", null, "string", "optional")
    newCompositionFieldMetaMeta(fields, "fields", null, "field", META_MODEL_MAIN_PACKAGE, "set")
}

private fun populateFieldEntity(fieldEntityMeta: MutableEntityModel) {
    val fields = newFieldsMetaMeta(fieldEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string", null, true)
    newPrimitiveFieldMetaMeta(fields, "number", null, "uint32", "optional")
    newPrimitiveFieldMetaMeta(fields, "info", null, "string", "optional")
    newEnumerationFieldMetaMeta(fields, "type", null, "field_type", META_MODEL_MAIN_PACKAGE)
    newCompositionFieldMetaMeta(fields, "enumeration", null, "enumeration_info", META_MODEL_MAIN_PACKAGE, "optional")
    newCompositionFieldMetaMeta(fields, "association", null, "entity_info", META_MODEL_MAIN_PACKAGE, "optional")
    newCompositionFieldMetaMeta(fields, "composition", null, "entity_info", META_MODEL_MAIN_PACKAGE, "optional")
    newPrimitiveFieldMetaMeta(fields, "is_key", null, "boolean", "optional")
    newEnumerationFieldMetaMeta(fields, "multiplicity", null, "multiplicity", META_MODEL_MAIN_PACKAGE, "optional")
    // Constraints
    newPrimitiveFieldMetaMeta(fields, "min_size", "Minimum string length", "uint32", "optional")
    newPrimitiveFieldMetaMeta(fields, "max_size", "Maximum string length", "uint32", "optional")
    newPrimitiveFieldMetaMeta(fields, "regex", "Regular expression that strings must match", "string", "optional")
}

private fun populateEnumerationInfoEntity(enumerationInfoEntityMeta: MutableEntityModel) {
    val fields = newFieldsMetaMeta(enumerationInfoEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string")
    newPrimitiveFieldMetaMeta(fields, "package", null, "string")
}

private fun populateEntityInfoEntity(entityInfoEntityMeta: MutableEntityModel) {
    val fields = newFieldsMetaMeta(entityInfoEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string")
    newPrimitiveFieldMetaMeta(fields, "package", null, "string")
}

private fun populateMainEnumerations(enumerationsMeta: MutableListFieldModel) {
    populateFieldTypeEnumeration(enumerationsMeta)
    populateMultiplicityEnumeration(enumerationsMeta)
}

fun populateFieldTypeEnumeration(enumerationsMeta: MutableListFieldModel) {
    newEnumerationMetaMeta(
        enumerationsMeta,
        "field_type",
        null,
        FieldType.values().map { EnumerationValueMetaMeta(it.name.lowercase()) })
}

private fun populateMultiplicityEnumeration(enumerationsMeta: MutableListFieldModel) {
    newEnumerationMetaMeta(
        enumerationsMeta,
        "multiplicity",
        null,
        Multiplicity.values().map { EnumerationValueMetaMeta(it.name.lowercase()) })
}