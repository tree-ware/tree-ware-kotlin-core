package org.treeWare.model.cursor

import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.getMetaResolved
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.JsonWireFormatEncoder
import org.treeWare.model.encoder.ModelEncodingVisitor
import org.treeWare.model.getMainModelFromJsonFile
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.dispatchLeave
import org.treeWare.model.traversal.dispatchVisit
import org.treeWare.util.getFileReader
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
    val metaModel = newAddressBookMetaModel(null, null).metaModel
        ?: throw IllegalStateException("Meta-model has validation errors")

    val model = getMainModelFromJsonFile(metaModel, inputFilePath)

    val leaderCursor = Leader1ModelCursor(model)
    val followerCursor = FollowerModelCursor(model)

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
    val metaModel = newAddressBookMetaModel(null, null).metaModel
        ?: throw IllegalStateException("Meta-model has validation errors")

    // Create different instances of the model from the same JSON input file.
    val leaderModel = getMainModelFromJsonFile(metaModel, inputFilePath)
    val followerModel = getMainModelFromJsonFile(metaModel, inputFilePath)

    assertNotSame(leaderModel, followerModel)

    val leaderCursor = Leader1ModelCursor(leaderModel)
    val followerCursor = FollowerModelCursor(followerModel)

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
    val metaModel = newAddressBookMetaModel(null, null).metaModel
        ?: throw IllegalStateException("Meta-model has validation errors")

    val leaderModel = getMainModelFromJsonFile(metaModel, leaderFilePath)
    val followerModel = getMainModelFromJsonFile(metaModel, wildcardFilePath)

    val leaderCursor = Leader1ModelCursor(leaderModel)
    val followerCursor = FollowerModelCursor(followerModel)

    val jsonWriter = StringWriter()
    val wireFormatEncoder = JsonWireFormatEncoder(jsonWriter, true)
    val encodingVisitor = ModelEncodingVisitor(wireFormatEncoder, encodePasswords = EncodePasswords.ALL)

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
    val expected = fileReader.readText()
    fileReader.close()
    val actual = jsonWriter.toString()
    assertEquals(expected, actual)
}

// TODO(deepak-nulu): return model path instead of meta-model path
private fun getPath(element: ElementModel?): String? = element?.getMetaResolved()?.fullName
