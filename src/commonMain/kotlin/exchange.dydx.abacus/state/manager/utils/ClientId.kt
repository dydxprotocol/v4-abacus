package exchange.dydx.abacus.state.manager.utils

import kotlin.random.Random
import kotlin.random.nextUInt

object ClientId {

    /**
     * Client ID on the protocol is a fixed32. And is returned from the indexer as an unsigned int string.
     *
     * This value is returned as a String for compaitiblity with JS. JS number seems to always interpret as signed.
     */
    fun generate() = Random.nextUInt().toString()
}
