package org.tree_ware.model.visitor

import org.tree_ware.model.core.*

abstract class AbstractModelVisitor<Aux, Return>(private val defaultVisitReturn: Return) : ModelVisitor<Aux, Return> {
    override fun visit(element: ElementModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(element: ElementModel<Aux>) {}

    override fun visit(model: Model<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(model: Model<Aux>) {}

    override fun visit(baseEntity: BaseEntityModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(baseEntity: BaseEntityModel<Aux>) {}

    override fun visit(root: RootModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(root: RootModel<Aux>) {}

    override fun visit(entity: EntityModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(entity: EntityModel<Aux>) {}

    override fun visit(field: FieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(field: FieldModel<Aux>) {}

    // Scalar fields

    override fun visit(field: ScalarFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(field: ScalarFieldModel<Aux>) {}

    override fun visit(field: PrimitiveFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(field: PrimitiveFieldModel<Aux>) {}

    override fun visit(field: AliasFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(field: AliasFieldModel<Aux>) {}

    override fun visit(field: EnumerationFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(field: EnumerationFieldModel<Aux>) {}

    override fun visit(field: AssociationFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(field: AssociationFieldModel<Aux>) {}

    override fun visit(field: CompositionFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(field: CompositionFieldModel<Aux>) {}

    // List fields

    override fun visit(field: ListFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(field: ListFieldModel<Aux>) {}

    override fun visit(field: ScalarListFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(field: ScalarListFieldModel<Aux>) {}

    override fun visit(field: PrimitiveListFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(field: PrimitiveListFieldModel<Aux>) {}

    override fun visit(field: AliasListFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(field: AliasListFieldModel<Aux>) {}

    override fun visit(field: EnumerationListFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(field: EnumerationListFieldModel<Aux>) {}

    override fun visit(field: AssociationListFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(field: AssociationListFieldModel<Aux>) {}

    override fun visit(field: CompositionListFieldModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(field: CompositionListFieldModel<Aux>) {}

    // Field values

    override fun visit(entityKeys: EntityKeysModel<Aux>): Return {
        return defaultVisitReturn
    }

    override fun leave(entityKeys: EntityKeysModel<Aux>) {}
}
