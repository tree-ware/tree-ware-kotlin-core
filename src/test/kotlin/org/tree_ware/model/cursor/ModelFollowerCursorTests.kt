package org.tree_ware.model.cursor

import org.tree_ware.model.core.ElementModel
import org.tree_ware.model.core.Model
import org.tree_ware.model.getModel
import org.tree_ware.schema.core.NamedElementSchema
import org.tree_ware.schema.core.SchemaTraversalAction
import kotlin.test.*

class ModelFollowerCursorTests {
    @Test
    fun `Follower-cursor follows data-model leader-cursor`() {
        testFollowerCursor<Unit>("src/test/resources/model/address_book_1.json")
    }
}

private fun <Aux> testFollowerCursor(inputFilePath: String) {
    testFollowerSameModelInstance<Aux>(inputFilePath)
    testFollowerDifferentModelInstances<Aux>(inputFilePath)
}

private fun <Aux> testFollowerSameModelInstance(inputFilePath: String) {
    val model = getModel(inputFilePath) as Model<Aux>

    val leader = ModelLeaderCursor(model)
    val follower = ModelFollowerCursor(model)

    val action = SchemaTraversalAction.CONTINUE
    while (true) {
        val leaderMove = leader.next(action) ?: break
        assertNotSame(leader.element, follower.element)
        follower.follow(leaderMove)
        assertSame(leader.element, follower.element)
    }
}

private fun <Aux> testFollowerDifferentModelInstances(inputFilePath: String) {
    // Create different instances of the model from the same JSON input file.
    val leaderModel = getModel(inputFilePath) as Model<Aux>
    val followerModel = getModel(inputFilePath) as Model<Aux>

    assertNotSame(leaderModel, followerModel)

    val leader = ModelLeaderCursor(leaderModel)
    val follower = ModelFollowerCursor(followerModel)

    val action = SchemaTraversalAction.CONTINUE
    while (true) {
        val leaderMove = leader.next(action) ?: break
        // TODO(deepak-nulu): once getPath() returns model-paths, verify that
        // the follower element path is not the same as the leader element path
        follower.follow(leaderMove)
        assertEquals(getPath(leader.element), getPath(follower.element))
    }
}

// TODO(deepak-nulu): return model path instead of schema path
private fun <Aux> getPath(element: ElementModel<Aux>?): String? {
    if (element == null) return null
    val schema = element.schema
    return if (schema is NamedElementSchema) schema.fullName else "/"
}
