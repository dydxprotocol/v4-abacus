package exchange.dydx.abacus.state.manager.utils

import kotlin.random.Random
import kotlin.random.nextUInt

object ClientId {

    fun generate() = Random.nextUInt()
}
