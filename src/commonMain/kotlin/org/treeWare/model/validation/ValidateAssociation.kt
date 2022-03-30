package org.treeWare.model.validation

import org.treeWare.model.core.AssociationModel
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.getMetaModelResolved
import org.treeWare.model.traversal.forEach

/**
 * Validates an association.
 *
 * An association must:
 * 1. have a single path
 * 2. a non-null target entity
 * 3. a target entity of the type specified in the association meta-model
 */
fun validateAssociation(association: AssociationModel): String? {
    val visitor = AssociationValidationVisitor()
    forEach(association.value, visitor, false)
    val targetEntity = visitor.targetEntity

    return if (visitor.hasMultiplePaths) "association has multiple paths"
    else if (visitor.hasNonKeyFields) "association has non-key fields"
    else if (targetEntity == null) "association does not have a target"
    else if (!isValidTarget(association, targetEntity)) "association has an invalid target type"
    else null
}

private fun isValidTarget(association: AssociationModel, targetEntity: EntityModel): Boolean {
    val associationField = association.parent
    val expectedTargetType = getMetaModelResolved(associationField.meta)?.associationMeta?.targetEntityMeta
        ?: throw IllegalStateException("Association is not resolved")
    val actualTargetType = targetEntity.meta ?: throw IllegalStateException("Meta-model missing for association")
    return expectedTargetType == actualTargetType
}