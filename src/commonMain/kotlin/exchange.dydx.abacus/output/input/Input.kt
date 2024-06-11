package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class InputType(val rawValue: String) {
    TRADE("trade"),
    CLOSE_POSITION("closePosition"),
    TRANSFER("transfer"),
    TRIGGER_ORDERS("triggerOrders"),
    ADJUST_ISOLATED_MARGIN("adjustIsolatedMargin");

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
    val triggerOrders: TriggerOrdersInput?,
    val adjustIsolatedMargin: AdjustIsolatedMarginInput?,
    val receiptLines: IList<ReceiptLine>?,
    val errors: IList<ValidationError>?,
    val childSubaccountErrors: IList<ValidationError>?
) {
    companion object {
        internal fun create(
            existing: Input?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            environment: V4Environment?,
            internalState: InternalState?
        ): Input? {
            Logger.d { "creating Input\n" }

            data?.let {
                val current = InputType.invoke(parser.asString(data["current"]))
                val trade =
                    TradeInput.create(existing?.trade, parser, parser.asMap(data["trade"]))
                val closePosition =
                    ClosePositionInput.create(existing?.closePosition, parser, parser.asMap(data["closePosition"]))
                val transfer =
                    TransferInput.create(existing?.transfer, parser, parser.asMap(data["transfer"]), environment, internalState?.transfer)
                val triggerOrders =
                    TriggerOrdersInput.create(existing?.triggerOrders, parser, parser.asMap(data["triggerOrders"]))
                val adjustIsolatedMargin =
                    AdjustIsolatedMarginInput.create(existing?.adjustIsolatedMargin, parser, parser.asMap(data["adjustIsolatedMargin"]))
                val errors =
                    ValidationError.create(existing?.errors, parser, parser.asList(data["errors"]))
                val childSubaccountErrors =
                    ValidationError.create(existing?.childSubaccountErrors, parser, parser.asList(data["childSubaccountErrors"]))
                val receiptLines = ReceiptLine.create(parser, parser.asList(data["receiptLines"]))

                return if (existing?.current !== current ||
                    existing?.trade !== trade ||
                    existing?.closePosition !== closePosition ||
                    existing?.transfer !== transfer ||
                    existing?.triggerOrders !== triggerOrders ||
                    existing?.adjustIsolatedMargin !== adjustIsolatedMargin ||
                    existing?.receiptLines != receiptLines ||
                    existing?.errors != errors ||
                    existing?.childSubaccountErrors != childSubaccountErrors
                ) {
                    Input(
                        current,
                        trade,
                        closePosition,
                        transfer,
                        triggerOrders,
                        adjustIsolatedMargin,
                        receiptLines,
                        errors,
                        childSubaccountErrors,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Input not valid" }
            return null
        }
    }
}
