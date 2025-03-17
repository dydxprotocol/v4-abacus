package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable

internal class SkipTrackProcessor(
    parser: ParserProtocol
) : BaseProcessor(parser) {
    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        var txHash = parser.asString(payload["tx_hash"]) ?: return modified
        val explorerLink = parser.asString(payload["explorer_link"])
        if (explorerLink != null && explorerLink.contains("solscan.io")) {
            // Solana tx hashes are case-sensitive
            modified[txHash] = true
            return modified
        }

        if (!txHash.startsWith("0x")) {
            txHash = "0x$txHash"
        }
        modified[txHash.lowercase()] = true
        return modified
    }
}
