package org.treeWare.metaModel

import io.github.z4kn4fein.semver.Version
import org.lighthousegames.logging.logging
import org.treeWare.metaModel.validation.validate
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableSetFieldModel

enum class Multiplicity { REQUIRED, OPTIONAL, SET }

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

enum class UniqueType {
    GLOBAL
}

enum class ExistsIfOperator { EQUALS, AND, OR, NOT }

enum class Granularity { FIELD, ENTITY, SUB_TREE }

private const val META_MODEL_PACKAGE = "org.tree_ware.meta_model"
private const val META_MODEL_MAIN_PACKAGE = "org.tree_ware.meta_model.main"

fun newMetaMetaModel(): MutableEntityModel {
    val metaMeta = newMetaMeta("meta_model", META_MODEL_PACKAGE)
    populateMetaMeta(metaMeta)
    val errors = validate(metaMeta, null, null, ::metaModelRootEntityFactory, mandatoryFieldNumbers = false)
    if (errors.isNotEmpty()) {
        val logger = logging()
        errors.forEach { logger.error { it } }
        throw IllegalStateException("Meta-meta-model is not valid")
    }
    return metaMeta
}

private fun populateMetaMeta(metaMeta: MutableEntityModel) {
    newVersionMetaMeta(metaMeta, Version(1, 0, 0))
    newRootMetaMeta(metaMeta, "meta_model", META_MODEL_MAIN_PACKAGE)
    val packagesMeta = newPackagesMetaMeta(metaMeta)
    populatePackages(packagesMeta)
}

private fun populatePackages(packagesMeta: MutableSetFieldModel) {
    val mainPackage = newPackageMetaMeta(packagesMeta, META_MODEL_MAIN_PACKAGE)
    populateMainPackage(mainPackage)
}

private fun populateMainPackage(mainPackage: MutableEntityModel) {
    val entitiesMeta = newEntitiesMetaMeta(mainPackage)
    populateMainEntities(entitiesMeta)
    val enumerationsMeta = newEnumerationsMetaMeta(mainPackage)
    populateMainEnumerations(enumerationsMeta)
}

private fun populateMainEntities(entitiesMeta: MutableSetFieldModel) {
    val metaModelEntityMeta = newEntityMetaMeta(entitiesMeta, "meta_model")
    populateMetaModelEntity(metaModelEntityMeta)
    val versionEntityMeta = newEntityMetaMeta(entitiesMeta, "version")
    populateVersionEntity(versionEntityMeta)
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
    val uniqueEntityMeta = newEntityMetaMeta(entitiesMeta, "unique")
    populateUniqueEntity(uniqueEntityMeta)
    val uniqueFieldEntityMeta = newEntityMetaMeta(entitiesMeta, "unique_field")
    populateUniqueFieldEntity(uniqueFieldEntityMeta)
    val enumerationInfoEntityMeta = newEntityMetaMeta(entitiesMeta, "enumeration_info")
    populateEnumerationInfoEntity(enumerationInfoEntityMeta)
    val entityInfoEntityMeta = newEntityMetaMeta(entitiesMeta, "entity_info")
    populateEntityInfoEntity(entityInfoEntityMeta)
    val existsIfClauseEntityMeta = newEntityMetaMeta(entitiesMeta, "exists_if_clause")
    populateExistsIfClauseEntity(existsIfClauseEntityMeta)
}

private fun populateMetaModelEntity(metaModelEntityMeta: MutableEntityModel) {
    val fields = newFieldsMetaMeta(metaModelEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string", "required")
    newPrimitiveFieldMetaMeta(fields, "package", null, "string", "required")
    newCompositionFieldMetaMeta(fields, "version", null, "version", META_MODEL_MAIN_PACKAGE)
    newCompositionFieldMetaMeta(fields, "root", null, "root", META_MODEL_MAIN_PACKAGE, "required")
    newCompositionFieldMetaMeta(fields, "packages", null, "package", META_MODEL_MAIN_PACKAGE, "set")
}

fun populateVersionEntity(versionEntityMeta: MutableEntityModel) {
    val fields = newFieldsMetaMeta(versionEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "semantic", null, "string")
    newPrimitiveFieldMetaMeta(fields, "name", null, "string", "optional")
}

private fun populateRootEntity(rootEntityMeta: MutableEntityModel) {
    val fields = newFieldsMetaMeta(rootEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "entity", null, "string")
    newPrimitiveFieldMetaMeta(fields, "package", null, "string")
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
    newCompositionFieldMetaMeta(fields, "uniques", null, "unique", META_MODEL_MAIN_PACKAGE, "set")
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
    newCompositionFieldMetaMeta(fields, "exists_if", null, "exists_if_clause", META_MODEL_MAIN_PACKAGE, "optional")
    newEnumerationFieldMetaMeta(fields, "granularity", null, "granularity", META_MODEL_MAIN_PACKAGE)
}

private fun populateUniqueEntity(uniqueEntityMeta: MutableEntityModel) {
    val fields = newFieldsMetaMeta(uniqueEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string", null, true)
    newEnumerationFieldMetaMeta(fields, "type", null, "unique_type", META_MODEL_MAIN_PACKAGE, "optional")
    newCompositionFieldMetaMeta(fields, "fields", null, "unique_field", META_MODEL_MAIN_PACKAGE, "set")
}

private fun populateUniqueFieldEntity(uniqueFieldEntityMeta: MutableEntityModel) {
    val fields = newFieldsMetaMeta(uniqueFieldEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string", null, true)
}

private fun populateEnumerationInfoEntity(enumerationInfoEntityMeta: MutableEntityModel) {
    val fields = newFieldsMetaMeta(enumerationInfoEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string")
    newPrimitiveFieldMetaMeta(fields, "package", null, "string")
}

private fun populateEntityInfoEntity(entityInfoEntityMeta: MutableEntityModel) {
    val fields = newFieldsMetaMeta(entityInfoEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "entity", null, "string")
    newPrimitiveFieldMetaMeta(fields, "package", null, "string")
}

fun populateExistsIfClauseEntity(existsIfEntityMeta: MutableEntityModel) {
    val fields = newFieldsMetaMeta(existsIfEntityMeta)
    newEnumerationFieldMetaMeta(fields, "operator", null, "exists_if_operator", META_MODEL_MAIN_PACKAGE)
    newPrimitiveFieldMetaMeta(fields, "field", null, "string", "optional")
    newPrimitiveFieldMetaMeta(fields, "value", null, "string", "optional")
    newCompositionFieldMetaMeta(fields, "arg1", null, "exists_if_clause", META_MODEL_MAIN_PACKAGE, "optional")
    newCompositionFieldMetaMeta(fields, "arg2", null, "exists_if_clause", META_MODEL_MAIN_PACKAGE, "optional")
}

private fun populateMainEnumerations(enumerationsMeta: MutableSetFieldModel) {
    populateFieldTypeEnumeration(enumerationsMeta)
    populateMultiplicityEnumeration(enumerationsMeta)
    populateUniqueTypeEnumeration(enumerationsMeta)
    populateExistsIfOperatorEnumeration(enumerationsMeta)
    populateGranularityEnumeration(enumerationsMeta)
}

fun populateFieldTypeEnumeration(enumerationsMeta: MutableSetFieldModel) {
    newEnumerationMetaMeta(
        enumerationsMeta,
        "field_type",
        null,
        FieldType.values().map { EnumerationValueMetaMeta(it.name.lowercase()) })
}

private fun populateMultiplicityEnumeration(enumerationsMeta: MutableSetFieldModel) {
    newEnumerationMetaMeta(
        enumerationsMeta,
        "multiplicity",
        null,
        Multiplicity.values().map { EnumerationValueMetaMeta(it.name.lowercase()) })
}

fun populateUniqueTypeEnumeration(enumerationsMeta: MutableSetFieldModel) {
    newEnumerationMetaMeta(
        enumerationsMeta,
        "unique_type",
        null,
        UniqueType.values().map { EnumerationValueMetaMeta(it.name.lowercase()) })
}

fun populateExistsIfOperatorEnumeration(enumerationsMeta: MutableSetFieldModel) {
    newEnumerationMetaMeta(
        enumerationsMeta,
        "exists_if_operator",
        null,
        ExistsIfOperator.values().map { EnumerationValueMetaMeta(it.name.lowercase()) })
}

fun populateGranularityEnumeration(enumerationsMeta: MutableSetFieldModel) {
    newEnumerationMetaMeta(
        enumerationsMeta,
        "granularity",
        null,
        Granularity.values().map { EnumerationValueMetaMeta(it.name.lowercase()) })
}