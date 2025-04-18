package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalAccountBalanceState
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.Numeric
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class AccountBalance(
    var denom: String,
    var amount: String,
) {
    companion object {
        internal fun create(
            existing: AccountBalance?,
            parser: ParserProtocol,
            decimals: Int,
            internalState: InternalAccountBalanceState?
        ): AccountBalance? {
            Logger.d { "creating Account Balance\n" }

            val denom = internalState?.denom
            val amount = internalState?.amount
            if (denom != null && amount != null) {
                val decimalAmount = amount * Numeric.decimal.TEN.pow(-1 * decimals)
                val decimalAmountString = parser.asString(decimalAmount)!!
                return if (existing?.denom != denom || existing.amount != decimalAmountString) {
                    AccountBalance(denom, decimalAmountString)
                } else {
                    existing
                }
            }
            Logger.d { "Account Balance not valid" }
            return null
        }
    }
}
