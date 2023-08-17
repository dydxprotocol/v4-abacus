package exchange.dydx.abacus.responses

import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.IList
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class SocketInfo(val type: String?, val channel: String?, val id: String?) {
}

@JsExport
@Serializable
class StateResponse(
    val state: PerpetualState?,
    val changes: StateChanges?,
    val errors: IList<ParsingError>? = null,
    val info: SocketInfo? = null
) {
}