package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.output.input.TransferInput

data class CctpChainTokenInfo(
    val chainId: String,
    val tokenAddress: String,
) {
    fun isCctpEnabled(transferInput: TransferInput?) =
        transferInput?.chain == chainId && transferInput.token?.lowercase() == tokenAddress.lowercase()
}

object CctpConfig {
    var cctpChainIds: List<CctpChainTokenInfo>? = null
}
