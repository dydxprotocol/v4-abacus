package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable

internal class SkipTrackProcessor(
    private val hash: String,
    parser: ParserProtocol
) : BaseProcessor(parser) {
    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        var txHash = parser.asString(payload["tx_hash"])
        if (txHash != null) {
            // txHash could be returned with or without the 0x prefix, and cases can be different
            if (txHash.startsWith("0x")) {
                txHash = txHash.substring(2)
            }
            var workingHash = hash
            if (workingHash.startsWith("0x")) {
                workingHash = hash.substring(2)
            }
            if (txHash.uppercase() == workingHash.uppercase()) {
                modified[hash] = true
            }
        }
        return modified
    }
}
