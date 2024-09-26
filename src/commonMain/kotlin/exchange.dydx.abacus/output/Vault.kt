package exchange.dydx.abacus.output

import exchange.dydx.abacus.functional.vault.VaultAccount
import exchange.dydx.abacus.functional.vault.VaultDetails
import exchange.dydx.abacus.functional.vault.VaultPositions
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class Vault(
    val details: VaultDetails? = null,
    val positions: VaultPositions? = null,
    val account: VaultAccount? = null
)
