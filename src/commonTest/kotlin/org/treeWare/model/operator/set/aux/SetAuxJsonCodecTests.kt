package org.treeWare.model.operator.set.aux

import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.testRoundTrip
import kotlin.test.Test

class SetAuxJsonCodecTests {
    @Test
    fun `SetAux JSON codec round trip must be lossless`() {
        testRoundTrip(
            "org/treeWare/model/operator/set/aux/set_aux_codec_test.json",
            multiAuxEncoder = MultiAuxEncoder(SET_AUX_NAME to SetAuxEncoder()),
            multiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(
                SET_AUX_NAME to { SetAuxStateMachine(it) }
            )
        )
    }
}