package indexer.models

import exchange.dydx.abacus.protocols.ParserProtocol
import kotlinx.serialization.Serializable

@Serializable
data class IndexerWsOrderbookUpdateResponse(
    val asks: List<IndexerWsOrderbookUpdateItem>? = null,
    val bids: List<IndexerWsOrderbookUpdateItem>? = null,
)

typealias IndexerWsOrderbookUpdateItem = List<String>

fun IndexerWsOrderbookUpdateItem.getPrice(parser: ParserProtocol): Double? =
    parser.asDouble(this.getOrNull(0))

fun IndexerWsOrderbookUpdateItem.getSize(parser: ParserProtocol): Double? =
    parser.asDouble(this.getOrNull(1))
