package org.treeWare.model.traversal

import org.openjdk.jmh.annotations.*
import org.treeWare.metaModel.newMainMetaMeta
import org.treeWare.model.core.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
class DispatchBenchmarks {
    private val model = getMainModel()
    private val entityKeysModel = getEntityKeysModel()
    private val visitor = Visitor()

    @Setup
    fun setUp() {
    }

    @Benchmark
    fun dispatchFirstMatch() {
        dispatchVisit(model, visitor)
    }

    @Benchmark
    fun dispatchLastMatch() {
        dispatchVisit(entityKeysModel, visitor)
    }
}

fun getMainModel(): MainModel {
    return MutableMainModel(newMainMetaMeta())
}

fun getEntityKeysModel(): EntityKeysModel {
    val dummyParent =
        MutableAssociationModel(MutableSingleFieldModel(null, MutableRootModel(null, MutableMainModel(null))))
    return MutableEntityKeysModel(null, dummyParent)
}

class Visitor : AbstractLeader1ModelVisitor<TraversalAction>(TraversalAction.ABORT_TREE)
