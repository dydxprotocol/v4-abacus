package exchange.dydx.abacus.calculator

import abs
import exchange.dydx.abacus.processor.utils.MarketId
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.filterNotNull
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kotlin.math.max
import kotlin.math.min

/*
Transform position and account
Delta object
{
    "marketId":"ETH-USD",
    "size":-1.2,
    "price":2343.3,
    "usdcSize": -3233.0,
    "feeRate": 0.0023,
    "fee":-0.23,
    "reduceOnly":true
}
 */

internal class SubaccountTransformer {
    private fun executionPrice(
        oraclePrice: Double?,
        limitPrice: Double?,
        isBuying: Boolean,
    ): Double? {
        // use optimistic price by default
        oraclePrice?.let { oraclePrice ->
            limitPrice?.let { limitPrice ->
                return if (isBuying) {
                    min(oraclePrice, limitPrice)
                } else {
                    max(oraclePrice, limitPrice)
                }
            }
        }
        return limitPrice
    }

    private fun deltaFromTrade(
        parser: ParserProtocol,
        trade: Map<String, Any>,
        market: Map<String, Any>?,
        transfer: Double? = null,
        shouldTransferOut: Boolean? = false,
    ): Map<String, Any>? {
        val marketId = parser.asString(trade["marketId"])
        val side = parser.asString(trade["side"])
        if (marketId != null && side != null) {
            parser.asNativeMap(trade["summary"])?.let { summary ->
                if (parser.asBool(summary["filled"]) == true) {
                    val multiplier =
                        (if (side == "BUY") Numeric.double.NEGATIVE else Numeric.double.POSITIVE)
                    val originalPrice = parser.asDouble(summary["price"])
                    val price = market?.let {
                        executionPrice(
                            parser.asDouble(market["oraclePrice"]),
                            originalPrice,
                            side == "BUY",
                        )
                    } ?: originalPrice
                    val size = (
                        parser.asDouble(summary["size"])
                            ?: Numeric.double.ZERO
                        ) * multiplier * Numeric.double.NEGATIVE
                    val usdcSize = (price ?: Numeric.double.ZERO) * (
                        parser.asDouble(summary["size"])
                            ?: Numeric.double.ZERO
                        ) * multiplier + (transfer ?: 0.0)
                    val fee = (
                        parser.asDouble(summary["fee"])
                            ?: Numeric.double.ZERO
                        ) * Numeric.double.NEGATIVE
                    val feeRate = parser.asDouble(summary["feeRate"]) ?: Numeric.double.ZERO
                    if (price != null && size != Numeric.double.ZERO) {
                        return mapOf(
                            "marketId" to marketId,
                            "size" to size,
                            "price" to price,
                            "usdcSize" to if (shouldTransferOut == true) 0.0 else usdcSize,
                            "fee" to fee,
                            "feeRate" to feeRate,
                            "reduceOnly" to (parser.asBool(trade["reduceOnly"]) ?: false),
                        ).filterNotNull()
                    }
                }
            }
            return mapOf(
                "marketId" to marketId,
                "usdcSize" to transfer,
            ).filterNotNull()
        }
        return null
    }

    private fun deltaFromTransfer(
        parser: ParserProtocol,
        transfer: Map<String, Any>
    ): Map<String, Any>? {
        val type = parser.asString(transfer["type"])
        if (type != null) {
            val summary = parser.asMap(transfer["summary"])
            if (summary != null) {
                val multiplier =
                    (if (type == "DEPOSIT") Numeric.double.POSITIVE else Numeric.double.NEGATIVE)
                val usdcSize =
                    (parser.asDouble(summary["usdcSize"]) ?: Numeric.double.ZERO) * multiplier
                val fee = (
                    parser.asDouble(summary["fee"])
                        ?: Numeric.double.ZERO
                    ) * Numeric.double.NEGATIVE
                return mapOf(
                    "usdcSize" to usdcSize,
                    "fee" to fee,
                )
            }
        }
        return null
    }

    internal fun deltaFromOrder(
        parser: ParserProtocol,
        order: Map<String, Any>,
        account: Map<String, Any>
    ): Map<String, Any>? {
        if (parser.asString(order["status"]) == "OPEN") {
            val marketId = parser.asString(order["marketId"])
            val side = parser.asString(order["side"])
            val price = parser.asDouble(order["price"])
            val multiplier =
                if (side == "BUY") Numeric.double.POSITIVE else Numeric.double.NEGATIVE
            val size =
                (
                    (parser.asDouble(order["remainingSize"]) ?: parser.asDouble(order["size"]))
                        ?: Numeric.double.ZERO
                    ) * multiplier
            if (marketId != null && price != null && size != Numeric.double.ZERO) {
                val usdcSize = price * size * Numeric.double.NEGATIVE
                val feeRate = parser.asDouble(
                    parser.value(
                        account,
                        when (parser.asString(order["type"])) {
                            "MARKET", "STOP_MARKET", "TAKE_PROFIT_MARKET" -> "user.takerFeeRate"
                            else -> "user.makerFeeRate"
                        },
                    ),
                ) ?: Numeric.double.ZERO
                val fee = usdcSize * feeRate
                return mapOf(
                    "marketId" to marketId,
                    "size" to size,
                    "price" to price,
                    "usdcSize" to usdcSize,
                    "feeRate" to feeRate,
                    "fee" to fee,
                    "reduceOnly" to (parser.asBool(order["reduceOnly"]) ?: false),
                )
            }
        }
        return null
    }

    internal fun applyTransferToWallet(
        wallet: Map<String, Any>,
        subaccountNumber: Int?,
        transfer: Map<String, Any>,
        parser: ParserProtocol,
        period: String
    ): Map<String, Any> {
        val delta = deltaFromTransfer(parser, transfer)
        return if (delta != null) {
            val key = "account.subaccounts.$subaccountNumber"
            val subaccount = parser.asNativeMap(parser.value(wallet, key))
            if (subaccount != null) {
                val modifiedSubaccount = applyDeltaToSubaccount(subaccount, delta, parser, period)
                val modifiedWallet = wallet.mutable()
                modifiedWallet.safeSet(key, modifiedSubaccount)
                modifiedWallet
            } else {
                wallet
            }
        } else {
            wallet
        }
    }

    internal fun applyIsolatedMarginAdjustmentToWallet(
        wallet: Map<String, Any>,
        subaccountNumber: Int?,
        delta: Map<String, Double>,
        parser: ParserProtocol,
        period: String
    ): Map<String, Any> {
        val key = "account.subaccounts.$subaccountNumber"
        val subaccount = parser.asNativeMap(parser.value(wallet, key))

        if (subaccount != null) {
            val modifiedSubaccount = applyDeltaToSubaccount(subaccount, delta, parser, period)
            val modifiedWallet = wallet.mutable()
            modifiedWallet.safeSet(key, modifiedSubaccount)
            return modifiedWallet
        }

        return wallet
    }

    internal fun applyTradeToSubaccount(
        subaccount: Map<String, Any>?,
        trade: Map<String, Any>,
        market: Map<String, Any>?,
        parser: ParserProtocol,
        period: String,
        transfer: Double? = null,
        isTransferOut: Boolean? = false,
    ): Map<String, Any>? {
        if (subaccount != null) {
            // when isTransferOut is true, usdcSize is overwritten to 0
            val delta = deltaFromTrade(
                parser,
                trade,
                market,
                transfer,
                isTransferOut,
            )
            return applyDeltaToSubaccount(subaccount, delta, parser, period, hasTransfer = transfer != null)
        }
        return subaccount
    }

    private fun nullPosition(marketId: String): Map<String, Any> {
        return mapOf(
            "id" to marketId,
            "status" to "OPEN",
            "id" to marketId,
            "displayId" to MarketId.getDisplayId(marketId),
            "assetId" to MarketId.getAssetId(marketId)!!,
            "side" to {
                "current" to "NONE"
            },
            "size" to mapOf(
                "current" to 0.0,
            ),
            "entryPrice" to mapOf(
                "current" to 0.0,
            ),
            "realizedPnl" to mapOf(
                "current" to 0.0,
            ),
            "maxSize" to 0.0,
            "netFunding" to 0.0,
            "unrealizedPnl" to 0.0,
            "resources" to mapOf(
                "sideStringKey" to mapOf(
                    "current" to "APP.GENERAL.NONE",
                ),
                "indicator" to mapOf(
                    "current" to "none",
                ),
            ),
        )
    }

    internal fun applyTransferToSubaccount(
        subaccount: Map<String, Any>,
        transfer: Double,
        parser: ParserProtocol,
        period: String
    ): Map<String, Any> {
        return applyDeltaToSubaccount(
            subaccount,
            mapOf("usdcSize" to transfer),
            parser,
            period,
        )
    }

    private fun applyDeltaToPositions(
        positions: Map<String, Any>,
        delta: Map<String, Any>?,
        parser: ParserProtocol,
        period: String
    ): Map<String, Any> {
        val deltaMarketId = parser.asString(delta?.get("marketId"))
        val size = parser.asDouble(delta?.get("size"))
        val nullDelta = if (deltaMarketId != null) {
            // Trade input
            if (delta != null) {
                if (size != null) {
                    mapOf("size" to 0.0)
                } else {
                    mapOf()
                }
            } else {
                null
            }
        } else {
            // Not a trade input. So we want the postOrder positions to be the same as the current positions
            mapOf("size" to 0.0)
        }
        val modified = mutableMapOf<String, Any>()
        for ((marketId, value) in positions) {
            val position = parser.asNativeMap(value)
            if (position != null) {
                val modifiedPosition = applyDeltaToPosition(
                    position,
                    if (deltaMarketId == marketId) delta else nullDelta,
                    parser,
                    period,
                )
                if (deltaMarketId == marketId) {
                    modified[marketId] = modifiedPosition
                } else if (parser.asDouble(parser.value(position, "size.current")) != null) {
                    modified[marketId] = modifiedPosition
                }
            }
        }
        if (modified[deltaMarketId] == null && deltaMarketId != null) {
            // position didn't exists
            val position = nullPosition(deltaMarketId)
            val modifiedDelta = if (delta != null) {
                transformDelta(
                    delta,
                    parser.asDouble(parser.value(position, "size.current")) ?: Numeric.double.ZERO,
                    parser,
                )
            } else {
                null
            }
            modified[deltaMarketId] = applyDeltaToPosition(position, modifiedDelta, parser, period)
        }

        return removeNullPositions(parser, modified, deltaMarketId)
    }

    private fun removeNullPositions(
        parser: ParserProtocol,
        positions: Map<String, Any>,
        exceptMarketId: String?
    ): Map<String, Any> {
        return positions.filterValues { position ->
            val marketId = parser.asString(parser.value(position, "id"))
            val current = parser.asDouble(parser.value(position, "size.current")) ?: 0.0
            val postOrder = parser.asDouble(parser.value(position, "size.postOrder")) ?: 0.0
            (marketId != exceptMarketId) || (current != 0.0 || postOrder != 0.0)
        }
    }

    private fun applyDeltaToSubaccount(
        subaccount: Map<String, Any>,
        delta: Map<String, Any>?,
        parser: ParserProtocol,
        period: String,
        hasTransfer: Boolean = false,
    ): Map<String, Any> {
        val modified = subaccount.mutable()

        val deltaMarketId = parser.asString(delta?.get("marketId"))
        val positions =
            parser.asNativeMap(subaccount["openPositions"])?.mutable() ?: mutableMapOf()
        val marketPosition = positions[deltaMarketId]
        val modifiedDelta = if (delta != null) {
            transformDelta(
                delta,
                parser.asDouble(parser.value(marketPosition, "size.current")) ?: Numeric.double.ZERO,
                parser,
                hasTransfer,
            )
        } else {
            null
        }

        val modifiedPositions = applyDeltaToPositions(positions, modifiedDelta, parser, period)
        modified["openPositions"] = modifiedPositions
        val usdcSize = parser.asDouble(modifiedDelta?.get("usdcSize")) ?: Numeric.double.ZERO
        if (delta != null && usdcSize != Numeric.double.ZERO) {
            val fee = (parser.asDouble(modifiedDelta?.get("fee")) ?: Numeric.double.ZERO)
            val quoteBalance =
                parser.asNativeMap(subaccount["quoteBalance"])?.mutable() ?: mutableMapOf()
            val quoteBalanceValue =
                (
                    parser.asDouble(quoteBalance["current"])
                        ?: Numeric.double.ZERO
                    ) + usdcSize + fee
            quoteBalance[period] = quoteBalanceValue
            modified["quoteBalance"] = quoteBalance
        } else {
            val quoteBalance =
                parser.asNativeMap(subaccount["quoteBalance"])?.mutable() ?: mutableMapOf()
            quoteBalance.safeSet(period, null)
            modified["quoteBalance"] = quoteBalance
        }
        return modified
    }

    private fun transformDelta(
        delta: Map<String, Any>,
        positionSize: Double,
        parser: ParserProtocol,
        hasTransfer: Boolean = false,
    ): Map<String, Any> {
        val marketId = parser.asString(delta["marketId"])
        if (parser.asBool(delta["reduceOnly"]) == true && marketId != null && !hasTransfer) {
            val size = parser.asDouble(delta["size"]) ?: Numeric.double.ZERO
            val price = parser.asDouble(delta["price"]) ?: Numeric.double.ZERO
            val modifiedSize =
                if (positionSize > Numeric.double.ZERO && size < Numeric.double.ZERO) {
                    maxOf(size, positionSize * Numeric.double.NEGATIVE)
                } else if (positionSize < Numeric.double.ZERO && size > Numeric.double.ZERO) {
                    minOf(size, positionSize * Numeric.double.NEGATIVE)
                } else {
                    Numeric.double.ZERO
                }
            val usdcSize = modifiedSize * price * Numeric.double.NEGATIVE
            val feeRate = parser.asDouble(delta["feeRate"]) ?: Numeric.double.ZERO
            val fee = (usdcSize * feeRate).abs() * Numeric.double.NEGATIVE
            return mapOf(
                "price" to price,
                "size" to size,
                "usdcSize" to usdcSize,
                "fee" to fee,
                "marketId" to marketId,
            )
        }
        return delta
    }

    private fun applyDeltaToPosition(
        position: Map<String, Any>,
        delta: Map<String, Any>?,
        parser: ParserProtocol,
        period: String
    ): Map<String, Any> {
        val sizes = parser.asNativeMap(position["size"])
        val modifiedSize = sizes?.toMutableMap() ?: mutableMapOf()
        val deltaSize = parser.asDouble(delta?.get("size"))
        if (delta != null && deltaSize != null) {
            val size = parser.asDouble(sizes?.get("current")) ?: Numeric.double.ZERO
            modifiedSize[period] = size + deltaSize
        } else {
            modifiedSize.safeSet(period, null)
        }
        val modified = position.mutable()
        modified["size"] = modifiedSize
        return modified
    }

    private fun adjustDeltaSize(
        size: Double,
        deltaSize: Double,
        reduceOnly: Boolean
    ): Double {
        return if (reduceOnly) {
            if (size > Numeric.double.ZERO && deltaSize < Numeric.double.ZERO) {
                maxOf(deltaSize, size * Numeric.double.NEGATIVE)
            } else if (size < Numeric.double.ZERO && deltaSize > Numeric.double.ZERO) {
                minOf(deltaSize, size * Numeric.double.NEGATIVE)
            } else {
                Numeric.double.ZERO
            }
        } else {
            deltaSize
        }
    }
}
