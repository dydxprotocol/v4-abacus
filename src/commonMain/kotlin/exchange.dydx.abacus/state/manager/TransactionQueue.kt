package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.utils.Numeric
import kotlinx.datetime.Clock

class TransactionParams(
    val type: TransactionType,
    val payload: String,
    val callback: (String?, Double, Double) -> Unit,
    val uiClickTimeMs: Double? = null
)

class TransactionQueue(
    private val transaction: (
        type: TransactionType,
        paramsInJson: String?,
        callback: (response: String) -> Unit
    )
    -> Unit
) {
    private val queue = mutableListOf<TransactionParams>()
    internal var isProcessing = false
    internal val size: Int
        get() = queue.size

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
        val uiDelayTimeMs = if (currentTransaction.uiClickTimeMs != null)
            submitTimeMs - currentTransaction.uiClickTimeMs else Numeric.double.ZERO

        transaction(currentTransaction.type, currentTransaction.payload) { response ->
            currentTransaction.callback(response, uiDelayTimeMs, submitTimeMs)
            processNext()
        }
    }
}
