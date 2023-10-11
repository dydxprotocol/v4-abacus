package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.DebugLogger
import exchange.dydx.abacus.utils.IList
import kollections.JsExport
import kotlinx.serialization.Serializable


@JsExport
@Serializable
enum class InputType(val rawValue: String) {
    TRADE("trade"),
    CLOSE_POSITION("closePosition"),
    TRANSFER("transfer");

    companion object {
        operator fun invoke(rawValue: String?) =
            InputType.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class Input(
    val current: InputType?,
    val trade: TradeInput?,
    val closePosition: ClosePositionInput?,
    val transfer: TransferInput?,
    val receiptLines: IList<ReceiptLine>?,
    val errors: IList<ValidationError>?
) {
    companion object {
        internal fun create(
            existing: Input?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            environment: V4Environment?
        ): Input? {
            DebugLogger.log("creating Input\n")

            data?.let {
                val current = InputType.invoke(parser.asString(data["current"]))
                val trade =
                    TradeInput.create(existing?.trade, parser, parser.asMap(data["trade"]))
                val closePosition =
                    ClosePositionInput.create(existing?.closePosition, parser, parser.asMap(data["closePosition"]))
                val transfer =
                    TransferInput.create(existing?.transfer, parser, parser.asMap(data["transfer"]), environment)
                val errors =
                    ValidationError.create(existing?.errors, parser, parser.asList(data["errors"]))
                val receiptLines = ReceiptLine.create(parser, parser.asList(data["receiptLines"]))
                return if (existing?.current !== current ||
                    existing?.trade !== trade ||
                    existing?.closePosition !== closePosition ||
                    existing?.transfer !== transfer ||
                    existing?.receiptLines != receiptLines ||
                    existing?.errors != errors
                ) {
                    Input(
                        current,
                        trade,
                        closePosition,
                        transfer,
                        receiptLines,
                        errors
                    )
                } else {
                    existing
                }
            }
            DebugLogger.debug("Input not valid")
            return null
        }
    }
}