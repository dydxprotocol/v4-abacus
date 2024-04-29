package exchange.dydx.abacus.tests.mock

import exchange.dydx.abacus.protocols.LocalizerProtocol

class LocalizerProtocolMock : LocalizerProtocol {
    override fun localize(path: String, paramsAsJson: String?): String {
        return path + paramsAsJson
    }
}
