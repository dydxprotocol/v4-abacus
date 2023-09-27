package exchange.dydx.abacus.state.modal

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.input.OrderTimeInForce
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.utils.GoodTil
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.Rounder
import exchange.dydx.abacus.utils.SHORT_TERM_ORDER_DURATION
import exchange.dydx.abacus.utils.ServerTime
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.math.max
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

@JsExport
@Serializable
enum class ExecutionCondition(val rawValue: Int) {
    EXECUTION_CONDITION_UNSPECIFIED(0),
    EXECUTION_CONDITION_ALL_OR_NONE(1),
    EXECUTION_CONDITION_REDUCE_ONLY(2);

    companion object {
        operator fun invoke(rawValue: Int) =
            ExecutionCondition.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
enum class TransactionOrderSide(val rawValue: Int) {
    BUY(1),
    SELL(2);

    companion object {
        operator fun invoke(rawValue: Int) =
            TransactionOrderSide.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
enum class TimeInForce(val rawValue: Int) {
    TIME_IN_FORCE_UNSPECIFIED(0),
    TIME_IN_FORCE_IOC(1),
    TIME_IN_FORCE_POST_ONLY(2),
    TIME_IN_FORCE_FOK(3);   // TODO: TIME_IN_FORCE_FOK not implemented as of 1/24/2023

    companion object {
        operator fun invoke(rawValue: Int) =
            TimeInForce.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
enum class OrderFlags(val rawValue: Int) {
    SHORT_TERM(0),
    LONG_TERM(64),
    CONDITIONAL(32);

    companion object {
        operator fun invoke(rawValue: Int) =
            OrderFlags.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
enum class ConditionType(val rawValue: Int) {
    CONDITION_TYPE_UNSPECIFIED(0),
    CONDITION_TYPE_STOP_LOSS(1),
    CONDITION_TYPE_TAKE_PROFIT(2);

    companion object {
        operator fun invoke(rawValue: Int) =
            ConditionType.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class PlaceOrderPayload(
    val clobPairId: Int,
    val side: String,
    val quantums: Double,
    val subticks: Double,
    val goodUntilBlock: Int?,
    val goodUntilTime: Double?,
    val clientId: Int,
    val timeInForce: String,
    val orderFlags: String,
    val reduceOnly: Boolean,
    var clientMetadata: Int,
    val conditionType: String,
    val conditionalOrderTriggerSubticks: Double,
) {
}

fun PerpTradingStateMachine.placeOrder(height: Int): PlaceOrderPayload? {
    if (!canPlaceTrade()) return null
    val trade = parser.asMap(input?.get("trade")) ?: return null
    return placeTradeOrder(height, trade)
}

@Throws(IllegalStateException::class)
fun PerpTradingStateMachine.placeTradeOrder(
    height: Int,
    trade: IMap<String, Any>,
): PlaceOrderPayload? {
    val marketId =
        parser.asString(trade["marketId"]) ?: throw IllegalStateException("Cannot get marketId")
    val market = parser.asMap(parser.value(marketsSummary, "markets.$marketId"))
        ?: throw IllegalStateException("Cannot get market")
    val v4Perpetual = parser.asMap(parser.value(market, "configs.v4"))
        ?: throw IllegalStateException("Cannot get perpetual info")
    val clobPairId = parser.asInt(v4Perpetual["clobPairId"])
        ?: throw IllegalStateException("Cannot get clob pair ID")
    val atomicResolution = parser.asInt(v4Perpetual["atomicResolution"])
        ?: throw IllegalStateException("Cannot get atomic resolution")
    val stepBaseQuantums = parser.asDecimal(v4Perpetual["stepBaseQuantums"])
        ?: throw IllegalStateException("Cannot get step base quantums")
    val quantumConversionExponent =
        parser.asInt(v4Perpetual["quantumConversionExponent"])
            ?: throw IllegalStateException("Cannot get quantum conversion exponent")
    val subticksPerTick = parser.asDecimal(v4Perpetual["subticksPerTick"] ?: 1000)
        ?: throw IllegalStateException("Cannot get subticks per tick")
    val summary =
        parser.asMap(trade["summary"]) ?: throw IllegalStateException("Cannot get order summary")
    val triggerPrice = parser.asDecimal(parser.value(trade, "price.triggerPrice"))
    val price =
        parser.asDecimal(summary["payloadPrice"]) ?: throw IllegalStateException("Cannot get payload price")
    val size = parser.asDecimal(summary["size"]) ?: throw IllegalStateException("Cannot get size")
    val quantums = quantum(size, atomicResolution, stepBaseQuantums)
    val subticks = subticks(price, atomicResolution, quantumConversionExponent, subticksPerTick)
        ?: throw IllegalStateException("Cannot get subticks")
    val goodUntilBlock = this.goodUntilBlock(trade, height)
    val goodUntilTime = this.goodUntilTime(trade)
    // The chain actually handles UInt up to UInt.MAX_VALUE. However, JS doesn't handle UInt
    val clientId = Random.nextInt(0, Int.MAX_VALUE)
    val ClientMetadata = calculateClientMetaData(trade)

    val triggerSubticks =
        subticks(triggerPrice, atomicResolution, quantumConversionExponent, subticksPerTick)

    return PlaceOrderPayload(
        clobPairId,
        parser.asString(trade["side"]) ?: "BUY",
        quantums.toDouble(),
        subticks.toDouble(),
        goodUntilBlock,
        goodUntilTime,
        clientId,
        this.timeInForce(trade).toString(),
        this.orderFlags(trade).toString(),
        parser.asBool(trade["reduceOnly"]) ?: false,
        ClientMetadata,
        this.conditionType(trade).toString(),
        triggerSubticks?.toDouble() ?: Numeric.double.ZERO
    )
}

fun PerpTradingStateMachine.closePositionPayload(height: Int): PlaceOrderPayload? {
    if (!canClosePosition()) return null
    val closePosition = parser.asMap(input?.get("closePosition")) ?: return null
    return placeTradeOrder(height, closePosition)
}

@JsExport
@Serializable
data class CancelOrderPayload(
    val clobPairId: Int,
    val clientId: Int,
    val orderFlags: String,
    val goodUntilBlock: Int?,
    val goodUntilTime: Double?,
) {
}

fun PerpTradingStateMachine.cancelOrder(
    orderId: String,
    subaccountNumber: Int,
    height: Int,
): CancelOrderPayload? {
    val order = parser.asMap(
        parser.value(
            data,
            "wallet.account.subaccounts.$subaccountNumber.orders.$orderId"
        )
    ) ?: return null
    val clobPairId = parser.asInt(order["clobPairId"]) ?: return null
    val clientId = parser.asInt(order["clientId"]) ?: return null
    val orderFlags = parser.asInt(order["orderFlags"]) ?: 0
    val goodTilBlock = parser.asInt(order["goodTilBlock"])
    val goodTilBlockTime = parser.asDatetime(order["goodTilBlockTime"])?.epochSeconds?.toDouble()

    return CancelOrderPayload(
        clobPairId,
        clientId,
        OrderFlags.invoke(orderFlags).toString(),
        goodTilBlock,
        goodUntilTime = goodTilBlockTime,
    )
}

private fun PerpTradingStateMachine.flags(
    order: SubaccountOrder,
): OrderFlags {
    // https://www.notion.so/CLOB-Advanced-Orders-Product-Spec-4fbd6e5166ba4f779fca69b9c6ff1582
    return when (order.type) {
        OrderType.market -> OrderFlags.SHORT_TERM

        OrderType.limit -> {
            when (order.timeInForce) {
                /*
                 TODO, Indexer always returns timeInForce as GTT right now. To showcase
                 cancelOrder working with FOK and IOC, return SHORT_TERM regardless of
                 timeInForce
                 */
                OrderTimeInForce.GTT -> OrderFlags.SHORT_TERM
                OrderTimeInForce.FOK, OrderTimeInForce.IOC -> OrderFlags.SHORT_TERM
                else -> OrderFlags.SHORT_TERM
            }
        }

        else -> OrderFlags.CONDITIONAL
    }
}

internal fun PerpTradingStateMachine.calculateClientMetaData(trade: IMap<String, Any>): Int {
    return when (parser.asString(trade["type"])) {
        "LIMIT" -> 0
        "MARKET" -> 1
        else -> 0
    }
}

internal fun PerpTradingStateMachine.canPlaceTrade(throwExceptionOnError: Boolean = false): Boolean {
    return if (parser.asString(input?.get("current")) == "trade") {
        val errors = parser.asList(input?.get("errors"))
        val firstCritical = errors?.firstOrNull { it ->
            val error = parser.asMap(it)
            if (error != null) {
                parser.asString(error["type"]) != "WARNING"
            } else false
        }
        if (firstCritical == null) true else {
            if (throwExceptionOnError) {
                throw IllegalStateException("Cannot place trade.")
            } else false
        }
    } else {
        if (throwExceptionOnError) {
            throw IllegalStateException("Input not in trade state")
        } else false
    }
}

internal fun PerpTradingStateMachine.canClosePosition(throwExceptionOnError: Boolean = false): Boolean {
    return if (parser.asString(input?.get("current")) == "closePosition") {
        val errors = parser.asList(input?.get("errors"))
        val firstCritical = errors?.firstOrNull { it ->
            val error = parser.asMap(it)
            if (error != null) {
                parser.asString(error["type"]) != "WARNING"
            } else false
        }
        if (firstCritical == null) true else {
            if (throwExceptionOnError) {
                throw IllegalStateException("Cannot close position.")
            } else false
        }
    } else {
        if (throwExceptionOnError) {
            throw IllegalStateException("Input not in closePosition state")
        } else false
    }
}

internal fun PerpTradingStateMachine.quantum(
    size: BigDecimal,
    atomicResolution: Int,
    stepBaseQuantums: BigDecimal,
): Long {
    val rawQuantums = size * Numeric.decimal.TEN.pow(-1 * atomicResolution)
    val quantums =
        Rounder.roundDecimal(rawQuantums, stepBaseQuantums).longValue(false)
    return max(quantums, stepBaseQuantums.longValue(false))
}

internal fun PerpTradingStateMachine.subticks(
    price: BigDecimal?,
    atomicResolution: Int,
    quantumConversionExponent: Int,
    subticksPerTick: BigDecimal,
): Long? {
    return if (price != null) {
        val QUOTE_QUANTUMS_ATOMIC_RESOLUTION = -6;
        val exponent =
            atomicResolution - quantumConversionExponent - QUOTE_QUANTUMS_ATOMIC_RESOLUTION
        val rawSubticks = price * Numeric.decimal.TEN.pow(exponent)
        val subticks =
            Rounder.roundDecimal(rawSubticks, subticksPerTick).longValue(false)
        max(subticks, subticksPerTick.longValue(false))
    } else null
}

// https://www.notion.so/dydx/CLOB-Advanced-Orders-Product-Spec-4fbd6e5166ba4f779fca69b9c6ff1582

internal fun PerpTradingStateMachine.executionCondition(
    trade: IMap<String, Any>,
): ExecutionCondition {
    return if (parser.asBool(trade["reduceOnly"]) == true) {
        ExecutionCondition.EXECUTION_CONDITION_REDUCE_ONLY
    } else {
        ExecutionCondition.EXECUTION_CONDITION_UNSPECIFIED
    }
}

internal fun PerpTradingStateMachine.orderSide(
    trade: IMap<String, Any>,
): TransactionOrderSide {
    return when (parser.asString(trade["side"])) {
        "BUY" -> TransactionOrderSide.BUY
        "SELL" -> TransactionOrderSide.SELL
        else -> TransactionOrderSide.BUY
    }
}

internal fun PerpTradingStateMachine.timeInForce(
    trade: IMap<String, Any>,
): TimeInForce {
    return when (orderType(trade)) {
        "MARKET" -> TimeInForce.TIME_IN_FORCE_IOC

        "LIMIT" -> {
            when (parser.asString(trade["timeInForce"])) {
                "GTT" -> {
                    if (parser.asBool(trade["postOnly"]) == true) {
                        TimeInForce.TIME_IN_FORCE_POST_ONLY
                    } else {
                        TimeInForce.TIME_IN_FORCE_UNSPECIFIED
                    }
                }

                "FOK" -> TimeInForce.TIME_IN_FORCE_FOK
                "IOC" -> TimeInForce.TIME_IN_FORCE_IOC
                else -> TimeInForce.TIME_IN_FORCE_UNSPECIFIED
            }
        }

        "STOP_LIMIT", "TAKE_PROFIT_LIMIT" -> {
            when (parser.asString(trade["execution"])) {
                "DEFAULT" -> TimeInForce.TIME_IN_FORCE_UNSPECIFIED
                "POST_ONLY" -> TimeInForce.TIME_IN_FORCE_POST_ONLY
                "FOK" -> TimeInForce.TIME_IN_FORCE_FOK
                "IOC" -> TimeInForce.TIME_IN_FORCE_IOC
                else -> TimeInForce.TIME_IN_FORCE_UNSPECIFIED
            }
        }

        "STOP_MARKET", "TAKE_PROFIT_MARKET" -> {
            when (parser.asString(trade["execution"])) {
                "FOK" -> TimeInForce.TIME_IN_FORCE_FOK
                "IOC" -> TimeInForce.TIME_IN_FORCE_IOC
                else -> TimeInForce.TIME_IN_FORCE_UNSPECIFIED
            }
        }

        else -> TimeInForce.TIME_IN_FORCE_UNSPECIFIED
    }
}

internal fun PerpTradingStateMachine.orderFlags(
    trade: IMap<String, Any>,
): OrderFlags {
    return when (orderType(trade)) {
        "MARKET" -> OrderFlags.SHORT_TERM

        "LIMIT" -> {
            when (parser.asString(trade["timeInForce"])) {
                "GTT" -> OrderFlags.LONG_TERM
                else -> OrderFlags.SHORT_TERM
            }
        }

        else -> OrderFlags.CONDITIONAL
    }
}

internal fun PerpTradingStateMachine.conditionType(
    trade: IMap<String, Any>,
): ConditionType {
    return when (orderType(trade)) {
        "MARKET", "LIMIT" -> ConditionType.CONDITION_TYPE_UNSPECIFIED

        "STOP_LIMIT", "STOP_MARKET" -> ConditionType.CONDITION_TYPE_STOP_LOSS

        "TAKE_PROFIT", "TAKE_PROFIT_MARKET" -> ConditionType.CONDITION_TYPE_TAKE_PROFIT

        else -> ConditionType.CONDITION_TYPE_UNSPECIFIED
    }
}

internal fun PerpTradingStateMachine.goodUntilBlock(trade: IMap<String, Any>, height: Int): Int? {
    return when (orderType(trade)) {
        "MARKET" -> height + SHORT_TERM_ORDER_DURATION

        "LIMIT" -> {
            when (parser.asString(trade["timeInForce"])) {
                "GTT" -> null
                else -> height + SHORT_TERM_ORDER_DURATION
            }
        }

        else -> height + SHORT_TERM_ORDER_DURATION
    }
}

internal fun PerpTradingStateMachine.goodUntilTime(trade: IMap<String, Any>): Double? {
    return when (orderType(trade)) {
        "MARKET" -> null

        "LIMIT" -> {
            when (parser.asString(trade["timeInForce"])) {
                "GTT" -> {
                    val now = ServerTime.now()
                    var timeInterval =
                        GoodTil.duration(parser.asMap(trade["goodUntil"]), parser) ?: return null
                    /*
                        Max GTT is last block time + 90 days, not current time + 90 days.
                        So we minus one minute to be safe.
                        Separate ticket to validate GTT to be <= 90 days
                     */
                    if (timeInterval >= 90.days) {
                        timeInterval = 90.days
                    }
                    val goodUntilTime = now.plus(timeInterval)
                    goodUntilTime.epochSeconds.toDouble()
                }

                else -> null
            }
        }

        else -> null
    }
}

internal fun PerpTradingStateMachine.orderType(trade: IMap<String, Any>): String {
    // for initial demo only. Send LIMIT order with MARKET order options (SHORT_TERM)
//    val type = parser.asString(trade["type"]) ?: "LIMIT"
//    return if (type == "LIMIT") "MARKET" else type

    return parser.asString(trade["type"]) ?: "LIMIT"
}


@JsExport
@Serializable
data class PlaceOrderPayload2(
    val clobPairId: Int,
    val side: Int,
    val quantums: Double,
    val subticks: Double,
    val goodTilBlock: Int?,
    val goodTilBlockTime: Double?,
    val clientId: Int,
    val timeInForce: Int,
    val orderFlags: Int,
    val reduceOnly: Boolean,
    val clientMetadata: Int,
    val conditionType: Int,
    val conditionalOrderTriggerSubticks: Double,
) {
}

fun PerpTradingStateMachine.placeOrder2(height: Int): PlaceOrderPayload2? {
    if (!canPlaceTrade(true)) {
        throw IllegalStateException("Cannot place trade")
    }
    val trade = parser.asMap(input?.get("trade")) ?: return null
    return placeTradeOrder2(height, trade)
}

fun PerpTradingStateMachine.placeTradeOrder2(
    height: Int,
    trade: IMap<String, Any>,
): PlaceOrderPayload2? {
    val marketId = parser.asString(trade["marketId"])
        ?: throw IllegalStateException("Cannot get marketId")
    val market = parser.asMap(parser.value(marketsSummary, "markets.$marketId"))
        ?: throw IllegalStateException("Cannot get market")

    val v4Perpetual = parser.asMap(parser.value(market, "configs.v4"))
        ?: throw IllegalStateException("Cannot get v4Perpetual")
    val clobPairId = parser.asInt(v4Perpetual["clobPairId"])
        ?: throw IllegalStateException("Cannot get clobPairId")

    val orderSide = this.orderSide(trade)
    val atomicResolution = parser.asInt(v4Perpetual["atomicResolution"])
        ?: throw IllegalStateException("Cannot get atomicResolution")
    val stepBaseQuantums = parser.asDecimal(v4Perpetual["stepBaseQuantums"])
        ?: throw IllegalStateException("Cannot get stepBaseQuantums")
    val quantumConversionExponent =
        parser.asInt(v4Perpetual["quantumConversionExponent"])
            ?: throw IllegalStateException("Cannot get quantumConversionExponent")
    val subticksPerTick = parser.asDecimal(v4Perpetual["subticksPerTick"] ?: 1000)
        ?: throw IllegalStateException("Cannot get subticksPerTick")
    val summary = parser.asMap(trade["summary"])
        ?: throw IllegalStateException("Cannot get summary")
    val triggerPrice = parser.asDecimal(parser.value(trade, "price.triggerPrice"))
    val price = parser.asDecimal(summary["price"])
        ?: throw IllegalStateException("Cannot get price")
    val size = parser.asDecimal(summary["size"])
        ?: throw IllegalStateException("Cannot get size")
    val quantums = quantum(size, atomicResolution, stepBaseQuantums)
    val subticks = subticks(price, atomicResolution, quantumConversionExponent, subticksPerTick)
        ?: throw IllegalStateException("Cannot get subticks")
    val goodTilBlock = goodUntilBlock(trade, height)
    val goodTilBlockTime = goodUntilTime(trade)
    // The chain actually handles UInt up to UInt.MAX_VALUE. However, JS doesn't handle UInt
    val clientId = Random.nextInt(0, Int.MAX_VALUE)
    val clientMetadata = calculateClientMetaData(trade)
    val conditionType = conditionType(trade)
    val conditionalOrderTriggerSubticks =
        subticks(triggerPrice, atomicResolution, quantumConversionExponent, subticksPerTick)

    return PlaceOrderPayload2(
        clobPairId,
        orderSide.rawValue,
        quantums.toDouble(),
        subticks.toDouble(),
        goodTilBlock,
        goodTilBlockTime,
        clientId,
        timeInForce(trade).rawValue,
        orderFlags(trade).rawValue,
        parser.asBool(trade["reduceOnly"]) ?: false,
        clientMetadata,
        conditionType.rawValue,
        conditionalOrderTriggerSubticks?.toDouble() ?: Numeric.double.ZERO
    )
}

fun PerpTradingStateMachine.closePositionPayload2(height: Int): PlaceOrderPayload2? {
    if (!canClosePosition()) return null
    val closePosition = parser.asMap(input?.get("closePosition")) ?: return null
    return placeTradeOrder2(height, closePosition)
}

@JsExport
@Serializable
data class CancelOrderPayload2(
    val clobPairId: Int,
    val clientId: Int,
    val orderFlags: Int,
    val goodTilBlock: Int?,
    val goodTilBlockTime: Double?,
) {
}

fun PerpTradingStateMachine.cancelOrder2(
    orderId: String,
    subaccountNumber: Int,
    height: Int,
    throwExceptionOnError: Boolean = false,
): CancelOrderPayload2? {
    val order = parser.asMap(
        parser.value(
            data,
            "wallet.account.subaccounts.$subaccountNumber.orders.$orderId"
        )
    )
    if (order != null) {
        val clobPairId = parser.asInt(order["clobPairId"]) ?: return null
        val clientId = parser.asInt(order["clientId"]) ?: return null
        val orderFlags = parser.asInt(order["orderFlags"]) ?: 0
        val goodTilBlock = parser.asInt(order["goodTilBlock"])
        val goodTilBlockTime =
            parser.asDatetime(order["goodTilBlockTime"])?.epochSeconds?.toDouble()

        return CancelOrderPayload2(
            clobPairId,
            clientId,
            orderFlags,
            goodTilBlock,
            goodTilBlockTime,
        )
    } else {
        if (throwExceptionOnError) {
            throw IllegalStateException("Order $orderId not found")
        }
        return null
    }
}
