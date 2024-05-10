package exchange.dydx.abacus.tests.mock

import exchange.dydx.abacus.protocols.ThreadingProtocol
import exchange.dydx.abacus.protocols.ThreadingType

class ThreadingProtocolMock : ThreadingProtocol {
    var asyncCallCount = 0

    override fun async(type: ThreadingType, block: () -> Unit) {
        asyncCallCount++
        block()
    }
}
