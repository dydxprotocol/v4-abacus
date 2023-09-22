package exchange.dydx.abacus.calculator

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kollections.iMutableMapOf
import kollections.toIMap
import kollections.toIMutableMap

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
    private fun deltaFromTrade(
        parser: ParserProtocol,
        trade: IMap<String, Any>
    ): IMap<String, Any>? {
        val marketId = parser.asString(trade["marketId"])
        val side = parser.asString(trade["side"])
        if (marketId != null && side != null) {
            parser.asMap(trade["summary"])?.let { summary ->
                if (parser.asBool(summary["filled"]) == true) {
                    val multiplier =
                        (if (side == "BUY") Numeric.decimal.NEGATIVE else Numeric.decimal.POSITIVE)
                    val price = parser.asDecimal(summary["price"])
                    val size = (parser.asDecimal(summary["size"])
                        ?: Numeric.decimal.ZERO) * multiplier * Numeric.decimal.NEGATIVE
                    val usdcSize =
                        (parser.asDecimal(summary["usdcSize"]) ?: Numeric.decimal.ZERO) * multiplier
                    val fee = (parser.asDecimal(summary["fee"])
                        ?: Numeric.decimal.ZERO) * Numeric.decimal.NEGATIVE
                    val feeRate = parser.asDecimal(summary["feeRate"]) ?: Numeric.decimal.ZERO
                    if (price != null && size != Numeric.decimal.ZERO) {
                        return iMapOf(
                            "marketId" to marketId,
                            "size" to size,
                            "price" to price,
                            "usdcSize" to usdcSize,
                            "fee" to fee,
                            "feeRate" to feeRate,
                            "reduceOnly" to (parser.asBool(trade["reduceOnly"]) ?: false)
                        ).toIMap()
                    }
                }
            }
            return iMapOf(
                "marketId" to marketId,
            ).toIMap()
        }
        return null
    }

    private fun deltaFromTransfer(
        parser: ParserProtocol,
        transfer: IMap<String, Any>
    ): IMap<String, Any>? {
        val type = parser.asString(transfer["type"])
        if (type != null) {
            val summary = parser.asMap(transfer["summary"])
            if (summary != null) {
                val multiplier =
                    (if (type == "DEPOSIT") Numeric.decimal.POSITIVE else Numeric.decimal.NEGATIVE)
                val usdcSize =
                    (parser.asDecimal(summary["usdcSize"]) ?: Numeric.decimal.ZERO) * multiplier
                val fee = (parser.asDecimal(summary["fee"])
                    ?: Numeric.decimal.ZERO) * Numeric.decimal.NEGATIVE
                return iMapOf(
                    "usdcSize" to usdcSize,
                    "fee" to fee
                ).toIMap()
            }
        }
        return null
    }

    internal fun deltaFromOrder(
        parser: ParserProtocol,
        order: IMap<String, Any>,
        account: IMap<String, Any>
    ): IMap<String, Any>? {
        if (parser.asString(order["status"]) == "OPEN") {
            val marketId = parser.asString(order["marketId"])
            val side = parser.asString(order["side"])
            val price = parser.asDecimal(order["price"])
            val multiplier =
                if (side == "BUY") Numeric.decimal.POSITIVE else Numeric.decimal.NEGATIVE
            val size =
                ((parser.asDecimal(order["remainingSize"]) ?: parser.asDecimal(order["size"]))
                    ?: Numeric.decimal.ZERO) * multiplier
            if (marketId != null && price != null && size != Numeric.decimal.ZERO) {
                val usdcSize = price * size * Numeric.decimal.NEGATIVE
                val feeRate = parser.asDecimal(
                    parser.value(
                        account,
                        when (parser.asString(order["type"])) {
                            "MARKET", "STOP_MARKET", "TAKE_PROFIT_MARKET" -> "user.takerFeeRate"
                            else -> "user.makerFeeRate"
                        }
                    )
                ) ?: Numeric.decimal.ZERO
                val fee = usdcSize * feeRate
                return iMapOf(
                    "marketId" to marketId,
                    "size" to size,
                    "price" to price,
                    "usdcSize" to usdcSize,
                    "feeRate" to feeRate,
                    "fee" to fee,
                    "reduceOnly" to (parser.asBool(order["postOnly"]) ?: false)
                )
            }
        }
        return null
    }

    internal fun applyTransferToWallet(
        wallet: IMap<String, Any>,
        subaccountNumber: Int?,
        transfer: IMap<String, Any>,
        parser: ParserProtocol,
        period: String
    ): IMap<String, Any> {
        val delta = deltaFromTransfer(parser, transfer)
        return if (delta != null) {
            val key = "account.subaccounts.$subaccountNumber"
            val subaccount = parser.asMap(parser.value(wallet, key))
            if (subaccount != null) {
                val modifiedSubaccount = applyDeltaToSubaccount(subaccount, delta, parser, period)
                val modifiedWallet = wallet.mutable()
                modifiedWallet.safeSet(key, modifiedSubaccount)
                modifiedWallet
            } else wallet
        } else wallet
    }

    internal fun applyTradeToSubaccount(
        subaccount: IMap<String, Any>?,
        trade: IMap<String, Any>,
        parser: ParserProtocol,
        period: String
    ): IMap<String, Any>? {
        if (subaccount != null) {
            val delta = deltaFromTrade(parser, trade)
            return applyDeltaToSubaccount(subaccount, delta, parser, period)
        }
        return subaccount
    }

    private fun nullPosition(marketId: String): IMap<String, Any> {
        return iMapOf(
            "id" to marketId,
            "status" to "OPEN",
            "id" to marketId,
            "assetId" to ParsingHelper.asset(marketId)!!,
            "side" to {
                "current" to "NONE"
            },
            "size" to iMapOf(
                "current" to 0.0
            ),
            "entryPrice" to iMapOf(
                "current" to 0.0
            ),
            "realizedPnl" to iMapOf(
                "current" to 0.0
            ),
            "maxSize" to 0.0,
            "netFunding" to 0.0,
            "unrealizedPnl" to 0.0,
            "resources" to iMapOf(
                "sideStringKey" to iMapOf(
                    "current" to "APP.GENERAL.NONE"
                ),
                "indicator" to iMapOf(
                    "current" to "none"
                )
            )
        )
    }

    private fun applyDeltaToPositions(
        positions: IMap<String, Any>,
        delta: IMap<String, Any>?,
        parser: ParserProtocol,
        period: String
    ): IMap<String, Any> {
        val nullDelta = if (delta != null) iMapOf("size" to 0.0) else null
        val modified = iMutableMapOf<String, Any>()
        val deltaMarketId = parser.asString(delta?.get("marketId"))
        for ((marketId, value) in positions) {
            val position = parser.asMap(value)
            if (position != null) {
                val modifiedPosition = applyDeltaToPosition(
                    position,
                    if (deltaMarketId == marketId) delta else nullDelta,
                    parser,
                    period
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
            val modifiedDelta = if (delta != null) transformDelta(
                delta,
                parser.asDecimal(parser.value(position, "size.current")) ?: Numeric.decimal.ZERO,
                parser
            ) else null
            modified[deltaMarketId] = applyDeltaToPosition(position, modifiedDelta, parser, period)
        }

        return modified
    }

    private fun applyDeltaToSubaccount(
        subaccount: IMap<String, Any>,
        delta: IMap<String, Any>?,
        parser: ParserProtocol,
        period: String
    ): IMap<String, Any> {
        val modified = subaccount.mutable()

        val deltaMarketId = parser.asString(delta?.get("marketId"))
        val positions =
            parser.asMap(subaccount["openPositions"])?.mutable() ?: iMutableMapOf()
        val marketPosition = positions[deltaMarketId]
        val modifiedDelta = if (delta != null) transformDelta(
            delta,
            parser.asDecimal(parser.value(marketPosition, "size.current")) ?: Numeric.decimal.ZERO,
            parser
        ) else null

        val modifiedPositions = applyDeltaToPositions(positions, modifiedDelta, parser, period)
        modified["openPositions"] = modifiedPositions
        val usdcSize = parser.asDecimal(modifiedDelta?.get("usdcSize")) ?: Numeric.decimal.ZERO
        if (delta != null && usdcSize != Numeric.decimal.ZERO) {
            val fee = (parser.asDecimal(modifiedDelta?.get("fee")) ?: Numeric.decimal.ZERO)
            val quoteBalance =
                parser.asMap(subaccount["quoteBalance"])?.mutable() ?: iMutableMapOf()
            val quoteBalanceValue =
                (parser.asDecimal(quoteBalance["current"])
                    ?: Numeric.decimal.ZERO) + usdcSize + fee
            quoteBalance[period] = quoteBalanceValue.doubleValue(false)
            modified["quoteBalance"] = quoteBalance
        } else {
            val quoteBalance =
                parser.asMap(subaccount["quoteBalance"])?.mutable() ?: iMutableMapOf()
            quoteBalance.safeSet(period, null)
            modified["quoteBalance"] = quoteBalance
        }
        return modified
    }

    private fun transformDelta(
        delta: IMap<String, Any>,
        positionSize: BigDecimal,
        parser: ParserProtocol
    ): IMap<String, Any> {
        val marketId = parser.asString(delta["marketId"])
        if (parser.asBool(delta["reduceOnly"]) == true && marketId != null) {
            val size = parser.asDecimal(delta["size"]) ?: Numeric.decimal.ZERO
            val price = parser.asDecimal(delta["price"]) ?: Numeric.decimal.ZERO
            val modifiedSize =
                if (positionSize > Numeric.decimal.ZERO && size < Numeric.decimal.ZERO) {
                    maxOf(size, positionSize * Numeric.decimal.NEGATIVE)
                } else if (positionSize < Numeric.decimal.ZERO && size > Numeric.decimal.ZERO) {
                    minOf(size, positionSize * Numeric.decimal.NEGATIVE)
                } else {
                    Numeric.decimal.ZERO
                }
            val usdcSize = modifiedSize * price * Numeric.decimal.NEGATIVE
            val feeRate = parser.asDecimal(delta["feeRate"]) ?: Numeric.decimal.ZERO
            val fee = (usdcSize * feeRate).abs() * Numeric.decimal.NEGATIVE
            return iMapOf(
                "price" to price,
                "size" to size,
                "usdcSize" to usdcSize,
                "fee" to fee,
                "marketId" to marketId
            )
        }
        return delta
    }


    private fun applyDeltaToPosition(
        position: IMap<String, Any>,
        delta: IMap<String, Any>?,
        parser: ParserProtocol,
        period: String
    ): IMap<String, Any> {
        val sizes = parser.asMap(position["size"])
        val modifiedSize = sizes?.toIMutableMap() ?: iMutableMapOf()
        val deltaSize = parser.asDecimal(delta?.get("size"))
        if (delta != null && deltaSize != null) {
            val size = parser.asDecimal(sizes?.get("current")) ?: Numeric.decimal.ZERO
            modifiedSize[period] = size + deltaSize
        } else {
            modifiedSize.safeSet(period, null)
        }
        val modified = position.mutable()
        modified["size"] = modifiedSize
        return modified
    }

    private fun adjustDeltaSize(
        size: BigDecimal,
        deltaSize: BigDecimal,
        reduceOnly: Boolean
    ): BigDecimal {
        return if (reduceOnly) {
            if (size > Numeric.decimal.ZERO && deltaSize < Numeric.decimal.ZERO) {
                maxOf(deltaSize, size * Numeric.decimal.NEGATIVE)
            } else if (size < Numeric.decimal.ZERO && deltaSize > Numeric.decimal.ZERO) {
                minOf(deltaSize, size * Numeric.decimal.NEGATIVE)
            } else {
                Numeric.decimal.ZERO
            }
        } else {
            deltaSize
        }
    }
}
