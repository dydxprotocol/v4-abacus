package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.DebugLogger
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class TriggerOrdersInput(
        val test: String?,
) {
    companion object {
        internal fun create(
                existing: TriggerOrdersInput?,
                parser: ParserProtocol,
                data: Map<*, *>?,
                environment: V4Environment?
        ): TriggerOrdersInput? {
            DebugLogger.log("creating Trigger Orders Input\n")

            data?.let {
                return if (true) {
                    TriggerOrdersInput(existing?.test)
                } else {
                    existing
                }
            }
            DebugLogger.log("Trigger Orders Input not valid\n")
            return null
        }
    }
}
