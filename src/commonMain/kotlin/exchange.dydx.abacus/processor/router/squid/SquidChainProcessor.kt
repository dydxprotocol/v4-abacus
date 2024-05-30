package exchange.dydx.abacus.processor.router.squid

import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.protocols.ParserProtocol

internal class SquidChainProcessor(
    private val parser: ParserProtocol
) {
    fun received(
        payload: Map<String, Any>
    ): SelectionOption {
        return SelectionOption(
            stringKey = parser.asString(payload["networkIdentifier"]) ?: parser.asString(payload["chainName"]),
            string = parser.asString(payload["networkIdentifier"]) ?: parser.asString(payload["chainName"]),
            type = parser.asString(payload["chainId"]) ?: "",
            iconUrl = parser.asString(payload["chainIconURI"]),
        )
    }
}
