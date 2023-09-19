package exchange.dydx.abacus.processor.squid

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import kollections.iMutableMapOf

internal class SquidStatusProcessor(
    parser: ParserProtocol,
    private val transactionId: String?,
    ) : BaseProcessor(parser) {

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>,
    ): IMap<String, Any> {
        val modified = existing?.mutable() ?: iMutableMapOf()
        val hash = transactionId ?: parser.asString(parser.value(payload, "fromChain.transactionId"))
        if (hash != null) {
            modified[hash] = payload
        }
        return modified
    }
}