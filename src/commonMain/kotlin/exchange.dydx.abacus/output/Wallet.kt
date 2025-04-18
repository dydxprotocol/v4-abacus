package exchange.dydx.abacus.output

import exchange.dydx.abacus.state.InternalWalletState
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class User(
    val isRegistered: Boolean,
    val email: String?,
    val username: String?,
    val feeTierId: String?,
    val makerFeeRate: Double,
    val takerFeeRate: Double,
    val makerVolume30D: Double,
    val takerVolume30D: Double,
    val fees30D: Double,
    val isEmailVerified: Boolean,
    val country: String?,
    val favorited: IList<String>?,
    val walletId: String?
)

@JsExport
@Serializable
data class LaunchIncentivePoint(
    val incentivePoints: Double,
    val marketMakingIncentivePoints: Double
)

@JsExport
@Serializable
data class LaunchIncentivePoints(
    val points: IMap<String, LaunchIncentivePoint>,
)

/*
ethereumeAddress is passed in from client. All other fields
are filled when socket v4_subaccounts channel is subscribed
*/
@JsExport
@Serializable
data class Wallet(
    val walletAddress: String?,
    val user: User?,
) {
    companion object {
        internal fun create(
            internalState: InternalWalletState,
        ): Wallet? {
            Logger.d { "creating Wallet\n" }

            val interalUserState = internalState.user ?: return null

            val walletAddress = internalState.walletAddress
            val user = User(
                isRegistered = false,
                email = null,
                username = null,
                feeTierId = interalUserState.feeTierId,
                makerFeeRate = interalUserState.makerFeeRate ?: 0.0,
                takerFeeRate = interalUserState.takerFeeRate ?: 0.0,
                makerVolume30D = interalUserState.makerVolume30D ?: 0.0,
                takerVolume30D = interalUserState.takerVolume30D ?: 0.0,
                fees30D = 0.0,
                isEmailVerified = false,
                country = null,
                favorited = null,
                walletId = null,
            )
            return Wallet(
                walletAddress = walletAddress,
                user = user,
            )
        }
    }
}
