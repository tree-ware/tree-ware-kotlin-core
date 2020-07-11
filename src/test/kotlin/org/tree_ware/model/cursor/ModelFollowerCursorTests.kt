package org.tree_ware.model.cursor

import org.tree_ware.model.core.ElementModel
import org.tree_ware.model.core.Model
import org.tree_ware.model.getModel
import org.tree_ware.schema.core.NamedElementSchema
import org.tree_ware.schema.core.SchemaTraversalAction
import kotlin.test.*

class ModelFollowerCursorTests {
    @Test
    fun `Follower-cursor on same data-model follows leader-cursor`() {
        testFollowerSameModelInstance<Unit>("src/test/resources/model/address_book_1.json")
    }

    @Test
    fun `Follower-cursor on different data-model follows leader-cursor`() {
        testFollowerDifferentModelInstances<Unit>("src/test/resources/model/address_book_1.json")
    }
}

private fun <Aux> testFollowerSameModelInstance(inputFilePath: String) {
    val model = getModel(inputFilePath) as Model<Aux>

    val leaderCursor = LeaderModelCursor(model)
    val followerCursor = FollowerModelCursor(model)

    val action = SchemaTraversalAction.CONTINUE
    while (true) {
        val leaderMove = leaderCursor.next(action) ?: break
        assertNotSame(leaderCursor.element, followerCursor.element)
        val followerMove = followerCursor.follow(leaderMove)
        assertNotNull(followerMove)
        assertSame(leaderMove.element, followerMove.element)
        assertSame(leaderCursor.element, followerCursor.element)
    }
}

private fun <Aux> testFollowerDifferentModelInstances(inputFilePath: String) {
    // Create different instances of the model from the same JSON input file.
    val leaderModel = getModel(inputFilePath) as Model<Aux>
    val followerModel = getModel(inputFilePath) as Model<Aux>

    assertNotSame(leaderModel, followerModel)

    val leaderCursor = LeaderModelCursor(leaderModel)
    val followerCursor = FollowerModelCursor(followerModel)

    val action = SchemaTraversalAction.CONTINUE
    while (true) {
        val leaderMove = leaderCursor.next(action) ?: break
        // TODO(deepak-nulu): once getPath() returns model-paths, verify that
        // the follower element path is not the same as the leader element path
        val followerMove = followerCursor.follow(leaderMove)
        assertNotNull(followerMove)
        assertEquals(getPath(leaderMove.element), getPath(followerMove.element))
        assertEquals(getPath(leaderCursor.element), getPath(followerCursor.element))
    }
}

// TODO(deepak-nulu): return model path instead of schema path
private fun <Aux> getPath(element: ElementModel<Aux>?): String? {
    if (element == null) return null
    val schema = element.schema
    return if (schema is NamedElementSchema) schema.fullName else "/"
}
