package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.protocols.TransactionType

class TransactionParams(
    val type: TransactionType,
    val payload: String,
    val callback: (String?) -> Unit,
    val onSubmit: (() -> Unit?)? = null,
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
        currentTransaction.onSubmit?.invoke()
        transaction(currentTransaction.type, currentTransaction.payload) { response ->
            currentTransaction.callback(response)
            processNext()
        }
    }
}
