package org.tree_ware.model.cursor

import org.tree_ware.model.core.ElementModel
import org.tree_ware.model.getModel
import org.tree_ware.schema.core.NamedElementSchema
import org.tree_ware.schema.core.SchemaTraversalAction
import kotlin.test.*

class ModelFollowerCursorTests {
    @Test
    fun `Follower-cursor on same data-model follows leader-cursor`() {
        testFollowerSameModelInstance("src/test/resources/model/address_book_1.json")
    }

    @Test
    fun `Follower-cursor on different data-model follows leader-cursor`() {
        testFollowerDifferentModelInstances("src/test/resources/model/address_book_1.json")
    }
}

private fun testFollowerSameModelInstance(inputFilePath: String) {
    val model = getModel<Unit>(inputFilePath)

    val leaderCursor = LeaderModelCursor(model)
    val followerCursor = FollowerModelCursor<Unit, Unit>(model)

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

private fun testFollowerDifferentModelInstances(inputFilePath: String) {
    // Create different instances of the model from the same JSON input file.
    val leaderModel = getModel<Unit>(inputFilePath)
    val followerModel = getModel<Unit>(inputFilePath)

    assertNotSame(leaderModel, followerModel)

    val leaderCursor = LeaderModelCursor(leaderModel)
    val followerCursor = FollowerModelCursor<Unit, Unit>(followerModel)

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
