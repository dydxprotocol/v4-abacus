package exchange.dydx.abacus.state.app.signer

import kotlinx.serialization.Serializable
import kollections.JsExport

@JsExport
@Serializable
data class V3ApiKey(val key: String, val secret: String, val passPhrase: String)