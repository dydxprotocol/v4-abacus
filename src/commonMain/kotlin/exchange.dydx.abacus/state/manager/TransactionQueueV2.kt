package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.protocols.Transaction
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Numeric
import kotlinx.datetime.Clock

class TransactionParamsV2(
    val transactions: IList<Transaction>,
    val callback: (String?, Double, Double) -> Unit,
    val uiClickTimeMs: Double? = null
)

class TransactionQueueV2(
    private val transaction: (
        transactions: IList<Transaction>,
        callback: (response: String) -> Unit
    )
    -> Unit
) {
    private val queue = mutableListOf<TransactionParamsV2>()
    internal var isProcessing = false
    internal val size: Int
        get() = queue.size

    fun enqueue(params: TransactionParamsV2) {
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

        val current = queue.removeAt(0)

        val submitTimeMs = Clock.System.now().toEpochMilliseconds().toDouble()
        val uiDelayTimeMs = if (current.uiClickTimeMs != null)
            submitTimeMs - current.uiClickTimeMs else Numeric.double.ZERO

        transaction(current.transactions) { response ->
            current.callback(response, uiDelayTimeMs, submitTimeMs)
            processNext()
        }
    }
}
