package exchange.dydx.abacus.tests.mock

import exchange.dydx.abacus.protocols.PresentationProtocol
import exchange.dydx.abacus.protocols.Toast

class PresentationProtocolMock : PresentationProtocol {
    var showToastCallCount = 0
    var toasts: MutableList<Toast> = mutableListOf()

    override fun showToast(toast: Toast) {
        showToastCallCount++
        toasts.add(toast)
    }
}
