package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import indexer.codegen.IndexerFundingPaymentResponse
import kollections.iListOf

internal fun TradingStateMachine.fundingPayments(payload: String, subaccountNumber: Int): StateChanges {
    val response = parser.asTypedObject<IndexerFundingPaymentResponse>(payload)
    if (response != null && response.fundingPayments.isNullOrEmpty().not()) {
        walletProcessor.processFundingPayments(
            existing = internalState.wallet,
            payload = response.fundingPayments?.toList(),
            subaccountNumber = subaccountNumber,
        )
        return StateChanges(iListOf(Changes.fundingPayments), null, iListOf(subaccountNumber))
    } else {
        return StateChanges(iListOf<Changes>())
    }
}
