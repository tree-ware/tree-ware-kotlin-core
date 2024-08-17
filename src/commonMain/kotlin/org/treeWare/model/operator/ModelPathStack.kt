package org.treeWare.model.operator

import org.treeWare.metaModel.FieldType
import org.treeWare.model.core.*
import org.treeWare.util.encodeBase64

class ModelPathStack {
    fun peekKeys(): Keys = keysStack.first()
    fun peekModelPath(): String = pathStack.first()
    fun isEmpty(): Boolean = pathStack.isEmpty()
    fun ancestorKeys(): List<Keys> = keysStack

    /**
     * Adds the specified field to the path.
     *
     * Pass null to add a dummy entry. This is useful when aborting the sub-tree; the leave method will get called
     * and the leave method can pop the stack without needing to check if the visit method aborted the sub-tree.
     */
    fun pushField(field: FieldModel?, listFieldIndex: Int? = null) {
        if (field == null) {
            pathStack.addFirst("")
            return
        }
        val parentPath = pathStack.firstOrNull() ?: ""
        val newPart = getFieldPathPart(field, listFieldIndex)
        val newPath = if (parentPath == "/") newPart else "$parentPath$newPart"
        pathStack.addFirst(newPath)
    }

    fun popField() {
        pathStack.removeFirst()
    }

    /**
     * Adds the specified entity to the path.
     *
     * Pass null to add a dummy entry. This is useful when aborting the sub-tree; the leave method will get called
     * and the leave method can pop the stack without needing to check if the visit method aborted the sub-tree.
     */
    fun pushEntity(entity: EntityModel?, isCompositionKey: Boolean = false) {
        if (entity == null) {
            keysStack.addFirst(EmptyKeys)
            pathStack.addFirst("")
            return
        }
        val parentPath = pathStack.firstOrNull()
        if (parentPath == null) { // root entity
            keysStack.addFirst(EmptyKeys)
            pathStack.addFirst("/")
            return
        }
        val keys = if (isCompositionKey) EmptyKeys else entity.getKeyFields(true)
        val newPart = getEntityPathPart(entity, keys.available)
        val newPath = "$parentPath$newPart"
        keysStack.addFirst(keys)
        pathStack.addFirst(newPath)
    }

    fun popEntity() {
        keysStack.removeFirst()
        pathStack.removeFirst()
    }

    private val keysStack = ArrayDeque<Keys>()
    private val pathStack = ArrayDeque<String>()
}

private fun getFieldPathPart(field: FieldModel, listFieldIndex: Int?): String {
    val partBuilder = StringBuilder("/")
    val fieldName = getFieldName(field)
    partBuilder.append(fieldName)
    listFieldIndex?.also { partBuilder.append("/").append(it) }
    return partBuilder.toString()
}

private fun getEntityPathPart(entity: EntityModel, keys: List<SingleFieldModel>): String {
    if (keys.isEmpty()) return ""
    val partBuilder = StringBuilder("/")
    keys.forEachIndexed { index, key ->
        if (index != 0) partBuilder.append("/")
        val keyValue = getEntityPathKeyValue(key)
        escapeAndAppend(keyValue, partBuilder)
    }
    return partBuilder.toString()
}

private fun getEntityPathKeyValue(key: SingleFieldModel): String {
    val keyValue = key.value ?: throw IllegalStateException("Null key value")
    return when (getFieldType(key)) {
        FieldType.BOOLEAN,
        FieldType.UINT8,
        FieldType.UINT16,
        FieldType.UINT32,
        FieldType.UINT64,
        FieldType.INT8,
        FieldType.INT16,
        FieldType.INT32,
        FieldType.INT64,
        FieldType.FLOAT,
        FieldType.DOUBLE,
        FieldType.BIG_INTEGER,
        FieldType.BIG_DECIMAL,
        FieldType.TIMESTAMP,
        FieldType.STRING,
        FieldType.UUID -> (keyValue as PrimitiveModel).value.toString()
        FieldType.BLOB -> encodeBase64((keyValue as PrimitiveModel).value as ByteArray)
        FieldType.PASSWORD1WAY,
        FieldType.PASSWORD2WAY -> throw IllegalStateException("Passwords are not supported as keys")
        FieldType.ALIAS -> throw IllegalStateException("Aliases are not yet supported")
        FieldType.ENUMERATION -> (keyValue as EnumerationModel).value
        FieldType.ASSOCIATION -> throw IllegalStateException("Associations are not supported as keys")
        FieldType.COMPOSITION -> throw IllegalStateException("Compositions are not supported as keys")
    }
}

private fun escapeAndAppend(value: String, builder: StringBuilder) {
    value.forEach { character ->
        when (character) {
            '/', '\\', '*' -> builder.append('\\')
        }
        builder.append(character)
    }
}