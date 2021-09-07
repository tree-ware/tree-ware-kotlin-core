package org.treeWare.model.cursor

import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.core.ElementModel
import org.treeWare.model.encoder.JsonWireFormatEncoder
import org.treeWare.model.encoder.ModelEncodingVisitor
import org.treeWare.model.getFileReader
import org.treeWare.model.getMainModel
import org.treeWare.model.operator.TraversalAction
import org.treeWare.model.operator.dispatchLeave
import org.treeWare.model.operator.dispatchVisit
import java.io.StringWriter
import kotlin.test.*

class FollowerModelCursorTests {
    @Test
    fun `Follower-cursor on same data-model follows leader-cursor`() {
        testFollowerSameModelInstance("model/address_book_1.json")
    }

    @Test
    fun `Follower-cursor on different data-model follows leader-cursor`() {
        testFollowerDifferentModelInstances("model/address_book_1.json")
    }

    @Test
    fun `Follower-cursor on wildcard model follows leader-cursor`() {
        testFollowerWildcardModelInstance(
            "model/address_book_1.json",
            "model/address_book_filter_all_model.json"
        )
    }
}

private fun testFollowerSameModelInstance(inputFilePath: String) {
    val metaModel = newAddressBookMetaModel()
    val metaModelErrors = org.treeWare.metaModel.validation.validate(metaModel)
    assertTrue(metaModelErrors.isEmpty())

    val model = getMainModel<Unit>(metaModel, inputFilePath)

    val leaderCursor = LeaderModelCursor(model)
    val followerCursor = FollowerModelCursor<Unit, Unit>(model)

    val action = TraversalAction.CONTINUE
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
    val metaModel = newAddressBookMetaModel()
    val metaModelErrors = org.treeWare.metaModel.validation.validate(metaModel)
    assertTrue(metaModelErrors.isEmpty())

    // Create different instances of the model from the same JSON input file.
    val leaderModel = getMainModel<Unit>(metaModel, inputFilePath)
    val followerModel = getMainModel<Unit>(metaModel, inputFilePath)

    assertNotSame(leaderModel, followerModel)

    val leaderCursor = LeaderModelCursor(leaderModel)
    val followerCursor = FollowerModelCursor<Unit, Unit>(followerModel)

    val action = TraversalAction.CONTINUE
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

/**
 * Verifies that the wildcard follower allows every leader element to be visited.
 * To verify this, JSON is generated from the leader cursor and compared against
 * the original JSON file (leaderFilePath).
 */
private fun testFollowerWildcardModelInstance(leaderFilePath: String, wildcardFilePath: String) {
    val metaModel = newAddressBookMetaModel()
    val metaModelErrors = org.treeWare.metaModel.validation.validate(metaModel)
    assertTrue(metaModelErrors.isEmpty())

    val leaderModel = getMainModel<Unit>(metaModel, leaderFilePath)
    val followerModel = getMainModel<Unit>(metaModel, wildcardFilePath)

    val leaderCursor = LeaderModelCursor(leaderModel)
    val followerCursor = FollowerModelCursor<Unit, Unit>(followerModel)

    val jsonWriter = StringWriter()
    val wireFormatEncoder = JsonWireFormatEncoder(jsonWriter, true)
    val encodingVisitor = ModelEncodingVisitor<Unit>(wireFormatEncoder, null)

    var action = TraversalAction.CONTINUE
    while (action != TraversalAction.ABORT_TREE) {
        val leaderMove = leaderCursor.next(action) ?: break
        val followerMove = followerCursor.follow(leaderMove)
        assertNotNull(followerMove)

        action = when (leaderMove.direction) {
            CursorMoveDirection.VISIT -> dispatchVisit(leaderMove.element, encodingVisitor)
                ?: TraversalAction.ABORT_TREE
            CursorMoveDirection.LEAVE -> {
                dispatchLeave(leaderMove.element, encodingVisitor)
                TraversalAction.CONTINUE
            }
        }
    }

    val fileReader = getFileReader(leaderFilePath)
    assertNotNull(fileReader)
    val expected = fileReader.readText()
    fileReader.close()
    val actual = jsonWriter.toString()
    assertEquals(expected, actual)
}

// TODO(deepak-nulu): return model path instead of meta-model path
private fun <Aux> getPath(element: ElementModel<Aux>?): String? = element?.meta?.aux?.fullName
