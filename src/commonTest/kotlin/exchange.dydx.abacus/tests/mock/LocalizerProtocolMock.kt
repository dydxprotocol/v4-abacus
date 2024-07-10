package exchange.dydx.abacus.tests.mock

import exchange.dydx.abacus.protocols.LocalizerProtocol

class LocalizerProtocolMock : LocalizerProtocol {
    var localizeCallCount = 0

    override fun localize(path: String, paramsAsJson: String?): String {
        localizeCallCount++
        return path + (paramsAsJson ?: "")
    }
}
