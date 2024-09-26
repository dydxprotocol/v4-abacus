package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
import kollections.toIList
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
            entries.firstOrNull { it.rawValue == rawValue }
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
            internalState: InternalState?,
            staticTyping: Boolean,
        ): Input? {
            Logger.d { "creating Input\n" }
            if (staticTyping && internalState?.input?.currentType == null) {
                return null
            }

            if (staticTyping || data != null) {
                val current = if (staticTyping) {
                    internalState?.input?.currentType
                } else {
                    InputType.invoke(parser.asString(data?.get("current")))
                }

                val trade = if (staticTyping) {
                    TradeInput.create(state = internalState?.input?.trade)
                } else {
                    TradeInput.create(existing?.trade, parser, parser.asMap(data?.get("trade")))
                }

                val closePosition = if (staticTyping) {
                    ClosePositionInput.create(state = internalState?.input?.closePosition)
                } else {
                    ClosePositionInput.create(existing?.closePosition, parser, parser.asMap(data?.get("closePosition")))
                }

                val transfer = TransferInput.create(
                    existing = existing?.transfer,
                    parser = parser,
                    data = parser.asMap(data?.get("transfer")),
                    environment = environment,
                    internalState = internalState?.input?.transfer,
                    staticTyping = staticTyping,
                )

                val triggerOrders = if (staticTyping) {
                    TriggerOrdersInput.create(state = internalState?.input?.triggerOrders)
                } else {
                    TriggerOrdersInput.create(
                        existing?.triggerOrders,
                        parser,
                        parser.asMap(data?.get("triggerOrders")),
                    )
                }

                val adjustIsolatedMargin = if (staticTyping) {
                    AdjustIsolatedMarginInput.create(
                        parser = parser,
                        data = internalState?.input?.adjustIsolatedMargin,
                    )
                } else {
                    AdjustIsolatedMarginInput.create(
                        existing?.adjustIsolatedMargin,
                        parser,
                        parser.asMap(
                            data?.get("adjustIsolatedMargin"),
                        ),
                    )
                }

                val errors = if (staticTyping) {
                    internalState?.input?.errors?.toIList()
                } else {
                    ValidationError.create(existing?.errors, parser, parser.asList(data?.get("errors")))
                }

                val childSubaccountErrors = if (staticTyping) {
                    internalState?.input?.childSubaccountErrors?.toIList()
                } else {
                    ValidationError.create(
                        existing?.childSubaccountErrors,
                        parser,
                        parser.asList(
                            data?.get("childSubaccountErrors"),
                        ),
                    )
                }

                val receiptLines = if (staticTyping) {
                    internalState?.input?.receiptLines?.toIList()
                } else {
                    ReceiptLine.create(parser, parser.asList(data?.get("receiptLines")))
                }

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
