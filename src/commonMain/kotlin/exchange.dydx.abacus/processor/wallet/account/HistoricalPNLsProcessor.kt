package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

@Suppress("UNCHECKED_CAST")
internal class HistoricalPNLsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = HistoricalPNLProcessor(parser = parser)

    override fun received(
        existing: List<Any>?,
        payload: List<Any>
    ): List<Any>? {
        val history = mutableListOf<Any>()
        for (item in payload.reversed()) {
            parser.asNativeMap(item)?.let {
                history.add(itemProcessor.received(null, it))
            }
        }
        return merge(
            parser,
            existing,
            history,
            "createdAt",
            true,
        )
    }
}
