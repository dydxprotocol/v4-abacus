package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.DebugLogger
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

@Suppress("UNCHECKED_CAST")
internal class MarketCalculator(val parser: ParserProtocol) {
    internal fun calculate(
        marketsSummary: Map<String, Any>?,
        assets: Map<String, Any>?,
        keys: Set<String>? = null
    ): Map<String, Any>? {
        val markets = parser.asNativeMap(marketsSummary?.get("markets"))
        if (markets == null) {
            DebugLogger.warning("Cannot calculate markets with null data")
            return marketsSummary
        }

        val modifiedMarkets = if (assets != null) {
            markets.mutable()
        } else null
        var volume24HUSDC = Numeric.double.ZERO
        var openInterestUSDC = Numeric.double.ZERO
        var trades24H = 0
        for ((key, value) in markets) {
            val market = parser.asNativeMap(value)
            if (market == null) {
                DebugLogger.warning("Expected a map, got: $value")
                continue
            }
            if (assets == null) {
                DebugLogger.warning("Expecting assets")
            } else {
                val marketCaps = calculateMarketCaps(market, assets)
                modifiedMarkets?.safeSet(key, marketCaps)
            }
            val perpetual = parser.asNativeMap(market["perpetual"])
            if (perpetual != null) {
                volume24HUSDC += parser.asDouble(perpetual["volume24H"]) ?: Numeric.double.ZERO
                openInterestUSDC += parser.asDouble(perpetual["openInterestUSDC"])
                    ?: Numeric.double.ZERO
                trades24H += parser.asInt(perpetual["trades24H"]) ?: 0
            }
        }

        val modified = marketsSummary?.mutable() ?: mutableMapOf()
        modified["volume24HUSDC"] = volume24HUSDC
        modified["openInterestUSDC"] = openInterestUSDC
        modified["trades24H"] = trades24H
        modified["markets"] = modifiedMarkets ?: markets
        return modified
    }

    private fun calculateMarketCaps(
        market: Map<String, Any>,
        assets: Map<String, Any>
    ): Map<String, Any> {
        val assetId = parser.asString(market["assetId"]) ?: return market
        val asset = parser.asNativeMap(assets[assetId]) ?: return market
        val indexPrice =
            parser.asDouble(market["indexPrice"]) ?: parser.asDouble(market["oraclePrice"])
            ?: return market
        val circulatingSupply = parser.asDouble(asset["circulatingSupply"]) ?: return market

        val modified = market.mutable()
        modified["marketCaps"] = indexPrice * circulatingSupply
        return modified
    }

}