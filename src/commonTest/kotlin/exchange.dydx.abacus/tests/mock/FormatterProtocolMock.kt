package exchange.dydx.abacus.tests.mock

import exchange.dydx.abacus.protocols.FormatterProtocol

class FormatterProtocolMock : FormatterProtocol {
    override fun percent(value: Double?, digits: Int): String? {
        TODO("Not yet implemented")
    }

    override fun dollar(value: Double?, tickSize: String?): String? {
        TODO("Not yet implemented")
    }
}
