package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.SubaccountFundingPayment
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import indexer.codegen.IndexerFundingPaymentResponseObject
import kotlinx.datetime.Instant

internal class FundingPaymentsProcessor(
    parser: ParserProtocol,
    private val paymentProcessor: FundingPaymentProcessor = FundingPaymentProcessor(parser = parser)
) : BaseProcessor(parser) {
    fun process(
        existing: List<SubaccountFundingPayment>?,
        payload: List<IndexerFundingPaymentResponseObject>,
    ): List<SubaccountFundingPayment>? {
        val new = payload.mapNotNull { eachPayload ->
            paymentProcessor.process(
                existing = null,
                payload = eachPayload,
            )
        }
        return merge(
            parser = parser,
            existing = existing,
            incoming = new,
            timeField = { item ->
                item?.createdAtMilliseconds?.toLong()?.let {
                    Instant.fromEpochMilliseconds(it)
                }
            },
            ascending = true,
        )
    }
}
