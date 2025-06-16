package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.FundingPayment
import exchange.dydx.abacus.output.account.PositionSide
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import indexer.codegen.IndexerFundingPaymentResponseObject

internal interface FundingPaymentProcessorProtocol {
    fun process(
        existing: FundingPayment?,
        payload: IndexerFundingPaymentResponseObject,
    ): FundingPayment?
}

internal class FundingPaymentProcessor(
    parser: ParserProtocol
) : BaseProcessor(parser), FundingPaymentProcessorProtocol {

    override fun process(
        existing: FundingPayment?,
        payload: IndexerFundingPaymentResponseObject
    ): FundingPayment? {
        val createdAt = parser.asDatetime(payload.createdAt) ?: return null
        val ticker = parser.asString(payload.ticker) ?: return null
        val oraclePrice = parser.asDouble(payload.oraclePrice) ?: return null
        val size = parser.asDouble(payload.size) ?: return null
        val side = PositionSide.invoke(payload.side) ?: return null
        val rate = parser.asDouble(payload.rate) ?: return null
        val payment = parser.asDouble(payload.payment) ?: return null

        return FundingPayment(
            createdAtInMilliseconds = createdAt.toEpochMilliseconds().toDouble(),
            ticker = ticker,
            oraclePrice = oraclePrice,
            size = size,
            side = side,
            rate = rate,
            payment = payment,
        )
    }
}
