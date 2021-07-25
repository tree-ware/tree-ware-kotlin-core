package org.treeWare.model.operator

import org.openjdk.jmh.annotations.*
import org.treeWare.common.traversal.TraversalAction
import org.treeWare.model.core.EntityKeysModel
import org.treeWare.model.core.Model
import org.treeWare.model.core.MutableEntityKeysModel
import org.treeWare.model.core.MutableModel
import org.treeWare.schema.core.MutableEntitySchema
import org.treeWare.schema.core.MutableRootSchema
import org.treeWare.schema.core.MutableSchema
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
class DispatchBenchmarks {
    private val model = getModel()
    private val entityKeysModel = getEntityKeysModel()
    private val visitor = Visitor()

    @Setup
    fun setUp() {}

    @Benchmark
    fun dispatchFirstMatch() {
        dispatchVisit(model, visitor)
    }

    @Benchmark
    fun dispatchLastMatch() {
        dispatchVisit(entityKeysModel, visitor)
    }
}

fun getModel(): Model<Unit> {
    val rootSchema = MutableRootSchema("test_model", null, "test_package", "test_root")
    val schema = MutableSchema(rootSchema, listOf())
    return MutableModel(schema)
}

fun getEntityKeysModel(): EntityKeysModel<Unit> {
    val entitySchema = MutableEntitySchema("test_entity", null, listOf())
    return MutableEntityKeysModel(entitySchema)
}

class Visitor : AbstractLeader1Follower0ModelVisitor<Unit, TraversalAction>(TraversalAction.ABORT_TREE)
