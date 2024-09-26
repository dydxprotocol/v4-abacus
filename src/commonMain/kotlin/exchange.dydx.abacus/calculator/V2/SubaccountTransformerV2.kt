package exchange.dydx.abacus.calculator.v2

import abs
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.TradeStatesWithStringValues
import exchange.dydx.abacus.output.account.SubaccountPositionResources
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPosition
import exchange.dydx.abacus.state.internalstate.InternalPositionCalculated
import exchange.dydx.abacus.state.internalstate.InternalSubaccountCalculated
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.state.internalstate.InternalTransferInputState
import exchange.dydx.abacus.state.internalstate.InternalWalletState
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.mutable
import indexer.codegen.IndexerPerpetualPositionStatus
import kotlin.math.max
import kotlin.math.min

data class Delta(
    val marketId: String? = null,
    val size: Double? = null,
    val price: Double? = null,
    val usdcSize: Double? = null,
    val fee: Double? = null,
    val feeRate: Double? = null,
    val reduceOnly: Boolean? = null,
)

internal class SubaccountTransformerV2(
    val parser: ParserProtocol
) {
    fun applyTradeToSubaccount(
        subaccount: InternalSubaccountState?,
        trade: InternalTradeInputState,
        market: InternalMarketState?,
        period: CalculationPeriod,
        transfer: Double? = null,
        isTransferOut: Boolean? = false,
    ) {
        if (subaccount != null) {
            // when isTransferOut is true, usdcSize is overwritten to 0
            val delta = deltaFromTrade(
                trade = trade,
                market = market,
                transfer = transfer,
                shouldTransferOut = isTransferOut,
            )
            applyDeltaToSubaccount(
                subaccount = subaccount,
                delta = delta,
                period = period,
                hasTransfer = transfer != null,
            )
        }
    }

    fun applyTransferToSubaccount(
        subaccount: InternalSubaccountState,
        transfer: Double,
        period: CalculationPeriod,
    ) {
        applyDeltaToSubaccount(
            subaccount = subaccount,
            delta = Delta(usdcSize = transfer),
            period = period,
        )
    }

    fun applyIsolatedMarginAdjustmentToWallet(
        wallet: InternalWalletState,
        subaccountNumber: Int?,
        delta: Delta,
        period: CalculationPeriod,
    ): InternalWalletState {
        val subaccountNumber = subaccountNumber ?: return wallet
        val subaccount = wallet.account.subaccounts[subaccountNumber]
        if (subaccount != null) {
            applyDeltaToSubaccount(subaccount, delta, period)
        }
        return wallet
    }

    fun applyTransferToWallet(
        wallet: InternalWalletState,
        subaccountNumber: Int?,
        transfer: InternalTransferInputState,
        parser: ParserProtocol,
        period: CalculationPeriod,
    ): InternalWalletState {
        val delta = deltaFromTransfer(transfer) ?: return wallet
        val subaccount = wallet.account.subaccounts[subaccountNumber] ?: return wallet

        applyDeltaToSubaccount(
            subaccount = subaccount,
            delta = delta,
            period = period,
        )
        return wallet
    }

    private fun deltaFromTransfer(
        transfer: InternalTransferInputState,
    ): Delta? {
        val type = transfer.type ?: return null
        val summary = transfer.summary ?: return null
        val multiplier =
            (if (type == TransferType.deposit) Numeric.double.POSITIVE else Numeric.double.NEGATIVE)
        val usdcSize =
            (summary.usdcSize ?: Numeric.double.ZERO) * multiplier
        val fee = (summary.fee ?: Numeric.double.ZERO) * Numeric.double.NEGATIVE
        return Delta(
            usdcSize = usdcSize,
            fee = fee,
        )
    }

    private fun deltaFromTrade(
        trade: InternalTradeInputState,
        market: InternalMarketState?,
        transfer: Double? = null,
        shouldTransferOut: Boolean? = false,
    ): Delta? {
        val marketId = trade.marketId ?: return null
        val side = trade.side ?: return null

        val summary = trade.summary

        if (summary != null && summary.filled) {
            val multiplier = if (side == OrderSide.Buy) Numeric.double.NEGATIVE else Numeric.double.POSITIVE
            val originalPrice = summary.price
            val price = if (market != null) {
                executionPrice(
                    oraclePrice = market.perpetualMarket?.oraclePrice,
                    limitPrice = originalPrice,
                    isBuying = side == OrderSide.Buy,
                )
            } else {
                originalPrice
            }
            val size = (summary.size ?: Numeric.double.ZERO) * multiplier * Numeric.double.NEGATIVE
            val usdcSize = (price ?: Numeric.double.ZERO) * (
                summary.size ?: Numeric.double.ZERO
                ) * multiplier + (transfer ?: 0.0)
            val fee = (summary.fee ?: Numeric.double.ZERO) * Numeric.double.NEGATIVE
            val feeRate = summary.feeRate ?: Numeric.double.ZERO

            if (price != null && size != Numeric.double.ZERO) {
                return Delta(
                    marketId = marketId,
                    size = size,
                    price = price,
                    usdcSize = if (shouldTransferOut == true) 0.0 else usdcSize,
                    fee = fee,
                    feeRate = feeRate,
                    reduceOnly = trade.reduceOnly,
                )
            }
        }

        return Delta(
            marketId = marketId,
            usdcSize = transfer,
        )
    }

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

    private fun applyDeltaToSubaccount(
        subaccount: InternalSubaccountState,
        delta: Delta?,
        period: CalculationPeriod,
        hasTransfer: Boolean = false,
    ) {
        val deltaMarketId = delta?.marketId
        val positions = subaccount.openPositions

        val marketPosition = positions?.get(deltaMarketId)
        val modifiedDelta = if (delta != null) {
            val positionSize = marketPosition?.calculated?.get(CalculationPeriod.current)?.size ?: Numeric.double.ZERO
            transformDelta(
                delta = delta,
                positionSize = positionSize,
                hasTransfer = hasTransfer,
            )
        } else {
            null
        }

        if (positions != null) {
            subaccount.openPositions = applyDeltaToPositions(
                positions = positions,
                delta = modifiedDelta,
                period = period,
            )
        }

        val calculatedAtPeriod = subaccount.calculated[period] ?: InternalSubaccountCalculated()
        val usdcSize = modifiedDelta?.usdcSize ?: Numeric.double.ZERO
        if (delta != null && usdcSize != Numeric.double.ZERO) {
            val fee = modifiedDelta?.fee ?: Numeric.double.ZERO
            val quoteBalance = subaccount.calculated[CalculationPeriod.current]?.quoteBalance ?: Numeric.double.ZERO
            calculatedAtPeriod.quoteBalance = quoteBalance + usdcSize + fee
        } else {
            calculatedAtPeriod.quoteBalance = null
        }
        subaccount.calculated[period] = calculatedAtPeriod
    }

    private fun transformDelta(
        delta: Delta,
        positionSize: Double,
        hasTransfer: Boolean = false,
    ): Delta {
        val marketId = delta.marketId
        if (delta.reduceOnly == true && !hasTransfer) {
            val size = delta.size ?: Numeric.double.ZERO
            val price = delta.price ?: Numeric.double.ZERO
            val modifiedSize =
                if (positionSize > Numeric.double.ZERO && size < Numeric.double.ZERO) {
                    maxOf(size, positionSize * Numeric.double.NEGATIVE)
                } else if (positionSize < Numeric.double.ZERO && size > Numeric.double.ZERO) {
                    minOf(size, positionSize * Numeric.double.NEGATIVE)
                } else {
                    Numeric.double.ZERO
                }
            val usdcSize = modifiedSize * price * Numeric.double.NEGATIVE
            val feeRate = delta.feeRate ?: Numeric.double.ZERO
            val fee = (usdcSize * feeRate).abs() * Numeric.double.NEGATIVE
            return Delta(
                marketId = marketId,
                size = size,
                price = price,
                usdcSize = usdcSize,
                fee = fee,
                feeRate = feeRate,
                reduceOnly = true,
            )
        }
        return delta
    }

    private fun applyDeltaToPositions(
        positions: Map<String, InternalPerpetualPosition>,
        delta: Delta?,
        period: CalculationPeriod,
    ): Map<String, InternalPerpetualPosition> {
        val modified = positions.mutable()

        val deltaMarketId = delta?.marketId
        val size = delta?.size
        val nullDelta = if (deltaMarketId != null) {
            // Trade input
            if (delta != null) {
                if (size != null) {
                    Delta(size = 0.0)
                } else {
                    Delta()
                }
            } else {
                null
            }
        } else {
            // Not a trade input. So we want the postOrder positions to be the same as the current positions
            Delta(size = 0.0)
        }

        val openPositions = modified.filterValues {
            it.status == IndexerPerpetualPositionStatus.OPEN
        }
        for ((marketId, position) in openPositions) {
            val currentSize = position.calculated[CalculationPeriod.current]?.size
            if (marketId == deltaMarketId || currentSize != null) {
                applyDeltaToPosition(
                    position = position,
                    delta = if (deltaMarketId == marketId) delta else nullDelta,
                    period = period,
                )
            }
        }

        if (openPositions[deltaMarketId] == null && deltaMarketId != null) {
            // position didn't exists
            val position = nullPosition(deltaMarketId)
            val modifiedDelta = if (delta != null) {
                transformDelta(
                    delta = delta,
                    positionSize = Numeric.double.ZERO,
                )
            } else {
                null
            }
            modified[deltaMarketId] = applyDeltaToPosition(
                position = position,
                delta = modifiedDelta,
                period = period,
            )
        }

        return removeNullPositions(
            positions = modified,
            exceptMarketId = deltaMarketId,
        )
    }

    private fun removeNullPositions(
        positions: Map<String, InternalPerpetualPosition>,
        exceptMarketId: String?
    ): Map<String, InternalPerpetualPosition> {
        return positions.filterValues { position ->
            val marketId = position.market
            val current = position.calculated[CalculationPeriod.current]?.size ?: position.size ?: 0.0
            val postOrder = position.calculated[CalculationPeriod.post]?.size ?: 0.0
            (marketId != exceptMarketId) || (current != 0.0 || postOrder != 0.0)
        }
    }

    private fun applyDeltaToPosition(
        position: InternalPerpetualPosition,
        delta: Delta?,
        period: CalculationPeriod,
    ): InternalPerpetualPosition {
        val deltaSize = delta?.size
        val calculatedAtPeriod = position.calculated[period] ?: InternalPositionCalculated()
        if (delta != null && deltaSize != null) {
            val currentSize = position.calculated[CalculationPeriod.current]?.size ?: position.size ?: Numeric.double.ZERO
            calculatedAtPeriod.size = currentSize + deltaSize
        } else {
            calculatedAtPeriod.size = null
        }
        position.calculated[period] = calculatedAtPeriod
        return position
    }

    private fun nullPosition(marketId: String): InternalPerpetualPosition {
        return InternalPerpetualPosition(
            market = marketId,
            status = IndexerPerpetualPositionStatus.OPEN,
            side = null,
            size = 0.0,
            maxSize = 0.0,
            entryPrice = 0.0,
            realizedPnl = 0.0,
            createdAt = null,
            createdAtHeight = null,
            sumOpen = null,
            sumClose = null,
            netFunding = 0.0,
            unrealizedPnl = 0.0,
            closedAt = null,
            exitPrice = null,
            subaccountNumber = null,
            resources = SubaccountPositionResources(
                sideStringKey = TradeStatesWithStringValues(
                    current = "APP.GENERAL.NONE",
                    postOrder = null,
                    postAllOrders = null,
                ),
                indicator = TradeStatesWithStringValues(
                    current = "none",
                    postOrder = null,
                    postAllOrders = null,
                ),
                sideString = TradeStatesWithStringValues(
                    current = null,
                    postOrder = null,
                    postAllOrders = null,
                ),
            ),
            calculated = mutableMapOf(
                CalculationPeriod.current to InternalPositionCalculated(
                    valueTotal = 0.0,
                    notionalTotal = 0.0,
                    adjustedImf = 0.0,
                    adjustedMmf = 0.0,
                    initialRiskTotal = 0.0,
                    maxLeverage = 0.0,
                    unrealizedPnl = 0.0,
                    unrealizedPnlPercent = 0.0,
                    marginValue = 0.0,
                    realizedPnlPercent = 0.0,
                    leverage = 0.0,
                    size = 0.0,
                    liquidationPrice = 0.0,
                    buyingPower = 0.0,

                ),
            ),

        )
    }
}
