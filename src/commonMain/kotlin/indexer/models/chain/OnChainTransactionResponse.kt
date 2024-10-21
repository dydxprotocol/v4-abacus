package indexer.models.chain

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.QUANTUM_MULTIPLIER
import kotlinx.serialization.Serializable

// Define the structure of the error message
@Serializable
data class ChainError(
    val message: String,
    val line: Int? = null,
    val column: Int? = null,
    val stack: String? = null
) {
    companion object {
        val unknownError = ChainError(message = "An unknown error occurred", line = null, column = null, stack = null)
    }
}

@Serializable
data class OnChainTransactionErrorResponse(
    val error: ChainError
) {
    companion object {
        private val parser = Parser()

        fun fromPayload(payload: String?): OnChainTransactionErrorResponse? {
            return parser.asTypedObject<OnChainTransactionErrorResponse>(payload)
        }
    }
}

// Define the structure of the success message
@Serializable
data class ChainEvent(
    val type: String,
    val attributes: List<ChainEventAttribute>
)

@Serializable
data class ChainEventAttribute(
    val key: String,
    val value: String
)

@Serializable
data class OnChainTransactionSuccessResponse(
    val height: Int? = null,
    val hash: String? = null,
    val code: Int? = null,
    val tx: String,
    val txIndex: Int? = null,
    val gasUsed: String? = null,
    val gasWanted: String? = null,
    val events: List<ChainEvent>? = null,
) {
    companion object {
        private val parser = Parser()

        fun fromPayload(payload: String?): OnChainTransactionSuccessResponse? {
            return parser.asTypedObject<OnChainTransactionSuccessResponse>(payload)
        }
    }

    val actualWithdrawalAmount: Double?
        get() {
            val withdrawalEvent = events?.firstOrNull { it.type == "withdraw_from_megavault" }
            val amountAttribute = withdrawalEvent?.attributes?.firstOrNull { it.key == "redeemed_quote_quantums" }
            return parser.asDouble(parser.asDecimal(amountAttribute?.value)?.div(QUANTUM_MULTIPLIER))
        }
}
