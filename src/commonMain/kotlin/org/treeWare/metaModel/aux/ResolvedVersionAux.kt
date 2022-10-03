package org.treeWare.metaModel.aux

import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.toVersionOrNull
import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.getAux

const val RESOLVED_VERSION_AUX_NAME = "resolved_version"

enum class SemanticVersionError { INVALID, HIGHER_THAN_SUPPORTED }

data class ResolvedVersionAux(val semantic: Version, val name: String?) {
    val supportedVersion: String = semantic.toString()

    fun validateModelSemanticVersion(modelVersionString: String): SemanticVersionError? {
        val modelSemanticVersion = modelVersionString.toVersionOrNull(false) ?: return SemanticVersionError.INVALID
        return if (modelSemanticVersion <= semantic) null else SemanticVersionError.HIGHER_THAN_SUPPORTED
    }
}

fun getResolvedVersionAux(elementMeta: ElementModel): ResolvedVersionAux =
    requireNotNull(elementMeta.getAux(RESOLVED_VERSION_AUX_NAME))

fun setResolvedVersionAux(elementMeta: ElementModel, aux: ResolvedVersionAux) {
    elementMeta.setAux(RESOLVED_VERSION_AUX_NAME, aux)
}