package org.treeWare.model.operator

import org.treeWare.model.core.AssociationModel
import org.treeWare.model.core.EntityModel
import org.treeWare.model.traversal.forEach
import org.treeWare.model.validation.AssociationValidationVisitor

fun getAssociationTargetEntity(association: AssociationModel): EntityModel {
    val visitor = AssociationValidationVisitor()
    forEach(association.value, visitor, false)
    return visitor.targetEntity ?: throw IllegalArgumentException("Association does not have a valid target entity")
}