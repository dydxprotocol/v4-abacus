package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import kollections.iMutableListOf

@Suppress("UNCHECKED_CAST")
internal class HistoricalPNLsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = HistoricalPNLProcessor(parser = parser)

    override fun received(
        existing: IList<Any>?,
        payload: IList<Any>
    ): IList<Any>? {
        val history = iMutableListOf<Any>()
        for (item in payload.reversed()) {
            parser.asMap(item)?.let {
                history.add(itemProcessor.received(null, it))
            }
        }
        return merge(
            parser,
            existing,
            history,
            "createdAt",
            true
        )
    }
}