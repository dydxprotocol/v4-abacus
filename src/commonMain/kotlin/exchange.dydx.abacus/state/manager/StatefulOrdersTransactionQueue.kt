package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.protocols.TransactionType
import kotlinx.datetime.Clock

class TransactionParams(
    val type: TransactionType,
    val payload: String,
    val callback: (response: String, uiDelayTimeMs: Double, submitTimeMs: Double) -> Unit,
    val uiClickTimeMs: Double
)

class StatefulOrdersTransactionQueue(
    private val transaction: (
        type: TransactionType,
        paramsInJson: String?,
        callback: (response: String) -> Unit)
    -> Unit
) {
    private val queue = mutableListOf<TransactionParams>()
    private var isProcessing = false

    fun enqueue(params: TransactionParams) {
        queue.add(params)
        if (!isProcessing) {
            isProcessing = true
            processNext()
        }
    }

    private fun processNext() {
        if (queue.isEmpty()) {
            isProcessing = false
            return
        }

        val currentTransaction = queue.removeAt(0)
        val submitTimeMs = Clock.System.now().toEpochMilliseconds().toDouble()
        val uiDelayTimeMs = submitTimeMs - currentTransaction.uiClickTimeMs

        transaction(currentTransaction.type, currentTransaction.payload) { response ->
            currentTransaction.callback(response, uiDelayTimeMs, submitTimeMs)
            processNext()
        }
    }
}
