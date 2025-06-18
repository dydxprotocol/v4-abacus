package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.PositionSide
import exchange.dydx.abacus.output.account.SubaccountFundingPayment
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import indexer.codegen.IndexerFundingPaymentResponseObject

internal interface FundingPaymentProcessorProtocol {
    fun process(
        existing: SubaccountFundingPayment?,
        payload: IndexerFundingPaymentResponseObject,
    ): SubaccountFundingPayment?
}

internal class FundingPaymentProcessor(
    parser: ParserProtocol
) : BaseProcessor(parser), FundingPaymentProcessorProtocol {

    override fun process(
        existing: SubaccountFundingPayment?,
        payload: IndexerFundingPaymentResponseObject
    ): SubaccountFundingPayment? {
        val createdAt = parser.asDatetime(payload.createdAt) ?: return null
        val ticker = parser.asString(payload.ticker) ?: return null
        val oraclePrice = parser.asDouble(payload.oraclePrice) ?: return null
        val size = parser.asDouble(payload.size) ?: return null
        val side = PositionSide.invoke(payload.side) ?: return null
        val rate = parser.asDouble(payload.rate) ?: return null
        val payment = parser.asDouble(payload.payment) ?: return null

        return SubaccountFundingPayment(
            marketId = ticker,
            payment = payment,
            rate = rate,
            positionSize = size,
            price = oraclePrice,
            createdAtMilliseconds = createdAt.toEpochMilliseconds().toDouble(),
            side = side,
        )
    }
}
