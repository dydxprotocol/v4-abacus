package exchange.dydx.abacus.responses

import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import exchange.dydx.abacus.utils.IList
import kollections.JsExport
import kollections.iListOf
import kollections.toIList
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class SocketInfo(
    val type: String?,
    val channel: String?,
    val id: String?,
    val childSubaccountNumber: Int?,
)

@JsExport
@Serializable
class StateResponse(
    val state: PerpetualState?,
    val changes: StateChanges?,
    val errors: IList<ParsingError>? = null,
    val info: SocketInfo? = null
) {
    fun merge(earlierResponse: StateResponse): StateResponse {
        val mergedChanges = this.changes?.merge(earlierResponse.changes ?: StateChanges(iListOf<Changes>())) ?: earlierResponse.changes
        val mergedErrors = this.errors?.toSet()?.union(earlierResponse.errors?.toSet() ?: setOf())?.toIList() ?: earlierResponse.errors
        return StateResponse(
            state = this.state,
            changes = mergedChanges,
            errors = mergedErrors,
            info = this.info ?: earlierResponse.info,
        )
    }
}
