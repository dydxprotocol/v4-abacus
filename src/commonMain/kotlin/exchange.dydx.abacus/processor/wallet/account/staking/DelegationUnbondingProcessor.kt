package exchange.dydx.abacus.processor.wallet.account.staking

import exchange.dydx.abacus.output.account.UnbondingDelegation
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMutableList
import indexer.models.chain.OnChainUnbondingResponse
import kollections.iMutableListOf

internal interface DelegationUnbondingProcessorProtocol {
    fun process(
        existing: List<UnbondingDelegation>?,
        payload: OnChainUnbondingResponse?,
    ): List<UnbondingDelegation>?
}

internal class DelegationUnbondingProcessor(
    parser: ParserProtocol,
) : BaseProcessor(parser), DelegationUnbondingProcessorProtocol {

    override fun process(
        existing: List<UnbondingDelegation>?,
        payload: OnChainUnbondingResponse?,
    ): List<UnbondingDelegation>? {
        if (payload?.unbondingResponses == null) {
            return existing
        }
        val unbondingDelegations: IMutableList<UnbondingDelegation> = iMutableListOf()
        for (response in payload.unbondingResponses) {
            val validatorAddress = response.validatorAddress ?: continue
            for (entry in response.entries ?: emptyList()) {
                val completionTime = entry.completionTime ?: continue
                val balance = entry.balance ?: continue
                unbondingDelegations.add(
                    UnbondingDelegation(
                        validatorAddress,
                        completionTime,
                        balance,
                    ),
                )
            }
        }
        return unbondingDelegations
    }
}
