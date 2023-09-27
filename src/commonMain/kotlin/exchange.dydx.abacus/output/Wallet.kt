package exchange.dydx.abacus.output

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.DebugLogger
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
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
) {
    companion object {
        internal fun create(
            existing: User?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): User? {
            DebugLogger.log("creating Account User\n")
            data?.let {
                val isRegistered = parser.asBool(data["isRegistered"]) ?: false
                val email = parser.asString(data["email"])
                val username = parser.asString(data["username"])
                val feeTierId = parser.asString(data["feeTierId"])
                val makerFeeRate = parser.asDouble(data["makerFeeRate"])
                val takerFeeRate = parser.asDouble(data["takerFeeRate"])
                val makerVolume30D = parser.asDouble(data["makerVolume30D"]) ?: 0.0
                val takerVolume30D = parser.asDouble(data["takerVolume30D"]) ?: 0.0
                val fees30D = parser.asDouble(data["fees30D"]) ?: 0.0
                val isEmailVerified = parser.asBool(data["isEmailVerified"]) ?: false
                val country = parser.asString(data["country"])
                val favorited = parser.asStrings(data["favorited"])
                val walletId = parser.asString(data["walletId"])
                if (makerFeeRate != null && takerFeeRate != null) {
                    return if (existing?.isRegistered != isRegistered ||
                        existing.email != email ||
                        existing.username != username ||
                        existing.feeTierId != feeTierId ||
                        existing.makerFeeRate != makerFeeRate ||
                        existing.takerFeeRate != takerFeeRate ||
                        existing.makerVolume30D != makerVolume30D ||
                        existing.takerVolume30D != takerVolume30D ||
                        existing.fees30D != fees30D ||
                        existing.isEmailVerified != isEmailVerified ||
                        existing.country != country ||
                        existing.favorited != favorited ||
                        existing.walletId != walletId
                    ) {
                        User(
                            isRegistered,
                            email,
                            username,
                            feeTierId,
                            makerFeeRate,
                            takerFeeRate,
                            makerVolume30D,
                            takerVolume30D,
                            fees30D,
                            isEmailVerified,
                            country,
                            favorited,
                            walletId
                        )
                    } else {
                        existing
                    }
                }
            }
            DebugLogger.debug("Account User not valid")
            return null
        }
    }
}

/*
ethereumeAddress is passed in from client. All other fields
are filled when socket v3_accounts channel is subscribed
*/
@JsExport
@Serializable
data class Wallet(
    val walletAddress: String?,
    val user: User?
) {
    companion object {
        internal fun create(
            existing: Wallet?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): Wallet? {
            DebugLogger.log("creating Wallet\n")

            data?.let {
                val walletAddress = parser.asString(data["walletAddress"])

                val user = (parser.asMap(data["user"]))?.let {
                    User.create(existing?.user, parser, it)
                }
                val wallet = Wallet(
                    walletAddress,
                    user
                )
                return wallet
            }
            DebugLogger.debug("Wallet not valid")
            return null
        }
    }
}