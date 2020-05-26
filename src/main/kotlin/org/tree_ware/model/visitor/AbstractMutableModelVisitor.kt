package org.tree_ware.model.visitor

import org.tree_ware.model.core.*
import org.tree_ware.schema.core.*

abstract class AbstractMutableModelVisitor<Aux, Return>(
    private val defaultVisitReturn: Return
) : MutableModelVisitor<Aux, Return> {
    override fun mutableVisit(element: MutableElementModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(element: MutableElementModel<Aux>) {}

    override fun mutableVisit(model: MutableModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(model: MutableModel<Aux>) {}

    override fun mutableVisit(baseEntity: MutableBaseEntityModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(baseEntity: MutableBaseEntityModel<Aux>) {}

    override fun mutableVisit(root: MutableRootModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(root: MutableRootModel<Aux>) {}

    override fun mutableVisit(entity: MutableEntityModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(entity: MutableEntityModel<Aux>) {}

    override fun mutableVisit(field: MutableFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableFieldModel<Aux>) {}

    // Scalar fields

    override fun mutableVisit(field: MutableScalarFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableScalarFieldModel<Aux>) {}

    override fun mutableVisit(field: MutablePrimitiveFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutablePrimitiveFieldModel<Aux>) {}

    override fun mutableVisit(field: MutableAliasFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableAliasFieldModel<Aux>) {}

    override fun mutableVisit(field: MutableEnumerationFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableEnumerationFieldModel<Aux>) {}

    override fun mutableVisit(field: MutableAssociationFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableAssociationFieldModel<Aux>) {}

    override fun mutableVisit(field: MutableCompositionFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableCompositionFieldModel<Aux>) {}

    // List fields

    override fun mutableVisit(field: MutableListFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableListFieldModel<Aux>) {}

    override fun mutableVisit(field: MutablePrimitiveListFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutablePrimitiveListFieldModel<Aux>) {}

    override fun mutableVisit(field: MutableAliasListFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableAliasListFieldModel<Aux>) {}

    override fun mutableVisit(field: MutableEnumerationListFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableEnumerationListFieldModel<Aux>) {}

    override fun mutableVisit(field: MutableAssociationListFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableAssociationListFieldModel<Aux>) {}

    override fun mutableVisit(field: MutableCompositionListFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableCompositionListFieldModel<Aux>) {}

    // Field values

    override fun mutableVisit(value: Any?, fieldSchema: PrimitiveFieldSchema): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(value: Any?, fieldSchema: PrimitiveFieldSchema) {}

    override fun mutableVisit(value: Any?, fieldSchema: AliasFieldSchema): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(value: Any?, fieldSchema: AliasFieldSchema) {}

    override fun mutableVisit(value: EnumerationValueSchema?, fieldSchema: EnumerationFieldSchema): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(value: EnumerationValueSchema?, fieldSchema: EnumerationFieldSchema) {}

    override fun mutableVisit(value: MutableAssociationValueModel<Aux>, fieldSchema: AssociationFieldSchema): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(value: MutableAssociationValueModel<Aux>, fieldSchema: AssociationFieldSchema) {}

    override fun mutableVisit(entityKeys: MutableEntityKeysModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun mutableLeave(entityKeys: MutableEntityKeysModel<Aux>) {}
}
