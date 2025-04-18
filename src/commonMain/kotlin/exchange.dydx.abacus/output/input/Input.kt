package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalState
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
) {
    companion object {
        internal fun create(
            existing: Input?,
            parser: ParserProtocol,
            internalState: InternalState?,
        ): Input? {
            Logger.d { "creating Input\n" }
            if (internalState?.input?.currentType == null) {
                return null
            }

            val current =
                internalState?.input?.currentType

            val trade =
                TradeInput.create(state = internalState?.input?.trade)

            val closePosition =
                ClosePositionInput.create(state = internalState?.input?.closePosition)

            val transfer = TransferInput.create(
                existing = existing?.transfer,
                parser = parser,
                internalState = internalState?.input?.transfer,
            )

            val triggerOrders =
                TriggerOrdersInput.create(state = internalState?.input?.triggerOrders)

            val adjustIsolatedMargin =
                AdjustIsolatedMarginInput.create(
                    parser = parser,
                    data = internalState?.input?.adjustIsolatedMargin,
                )

            var errors = internalState?.input?.errors?.toIList()
            if (internalState?.input?.currentType == InputType.TRADE && internalState.input.trade.marginMode == MarginMode.Isolated) {
                errors = internalState.input.childSubaccountErrors?.toIList()
            }
            if (internalState?.input?.currentType == InputType.CLOSE_POSITION && internalState.input.closePosition.marginMode == MarginMode.Isolated) {
                errors = internalState.input.childSubaccountErrors?.toIList()
            }

            val receiptLines =
                internalState?.input?.receiptLines?.toIList()

            return if (existing?.current !== current ||
                existing?.trade !== trade ||
                existing?.closePosition !== closePosition ||
                existing?.transfer !== transfer ||
                existing?.triggerOrders !== triggerOrders ||
                existing?.adjustIsolatedMargin !== adjustIsolatedMargin ||
                existing?.receiptLines != receiptLines ||
                existing?.errors != errors
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
                )
            } else {
                existing
            }
        }
    }
}
