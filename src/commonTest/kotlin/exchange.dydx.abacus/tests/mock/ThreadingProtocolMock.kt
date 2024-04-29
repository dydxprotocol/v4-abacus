package exchange.dydx.abacus.tests.mock

import exchange.dydx.abacus.protocols.ThreadingProtocol
import exchange.dydx.abacus.protocols.ThreadingType

class ThreadingProtocolMock : ThreadingProtocol {
    override fun async(type: ThreadingType, block: () -> Unit) {
        block()
    }
}
