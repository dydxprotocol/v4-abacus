package exchange.dydx.abacus.state.machine

import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class WalletConnectionType(val rawValue: String) { Ethereum("Ethereum"), Cosmos("Cosmos"), Solana("Solana") }
