package org.treeWare.model.operator.rbac.aux

import org.treeWare.model.encoder.AuxEncoder
import org.treeWare.model.encoder.WireFormatEncoder

class PermissionsAuxEncoder : AuxEncoder {
    override fun encode(fieldName: String?, auxName: String, aux: Any?, wireFormatEncoder: WireFormatEncoder) {
        if (aux == null) return
        val auxFieldName = wireFormatEncoder.getAuxFieldName(fieldName, auxName)
        val permissionsAux = aux as PermissionsAux
        wireFormatEncoder.encodeObjectStart(auxFieldName)
        permissionsAux.create?.also { encode(PERMISSIONS_AUX_CODEC_CREATE_NAME, it, wireFormatEncoder) }
        permissionsAux.read?.also { encode(PERMISSIONS_AUX_CODEC_READ_NAME, it, wireFormatEncoder) }
        permissionsAux.update?.also { encode(PERMISSIONS_AUX_CODEC_UPDATE_NAME, it, wireFormatEncoder) }
        permissionsAux.delete?.also { encode(PERMISSIONS_AUX_CODEC_DELETE_NAME, it, wireFormatEncoder) }
        permissionsAux.crud?.also { encode(PERMISSIONS_AUX_CODEC_CRUD_NAME, it, wireFormatEncoder) }
        permissionsAux.all?.also { encode(PERMISSIONS_AUX_CODEC_ALL_NAME, it, wireFormatEncoder) }
        wireFormatEncoder.encodeObjectEnd()
    }

    private fun encode(name: String, scope: PermissionScope, wireFormatEncoder: WireFormatEncoder) {
        wireFormatEncoder.encodeStringField(name, scope.name.lowercase())
    }
}