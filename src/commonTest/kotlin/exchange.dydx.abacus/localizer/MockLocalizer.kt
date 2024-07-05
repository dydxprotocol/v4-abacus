package exchange.dydx.abacus.localizer

import exchange.dydx.abacus.protocols.LocalizerProtocol

class MockLocalizer : LocalizerProtocol {
    override fun localize(path: String, paramsAsJson: String?): String {
        return path
    }
}
