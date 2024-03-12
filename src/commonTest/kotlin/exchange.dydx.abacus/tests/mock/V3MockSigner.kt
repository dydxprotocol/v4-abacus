package exchange.dydx.abacus.tests.mock

import exchange.dydx.abacus.protocols.V3PrivateSignerProtocol

class V3MockSigner : V3PrivateSignerProtocol {
    override fun sign(text: String, secret: String): String {
        return "Dummy"
    }
}
