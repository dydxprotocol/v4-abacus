package exchange.dydx.abacus.state.app.signer

import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class V3ApiKey(val key: String, val secret: String, val passPhrase: String)
