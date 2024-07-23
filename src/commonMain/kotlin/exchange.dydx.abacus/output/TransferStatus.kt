package exchange.dydx.abacus.output

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
class TransferStatus(
    val status: String?,
    val gasStatus: String?,
    val axelarTransactionUrl: String?,
    val fromChainStatus: TransferChainStatus?,
    val toChainStatus: TransferChainStatus?,
    var routeStatuses: IList<TransferRouteStatus>?,
    val error: String?,
    val squidTransactionStatus: String?,
) {
    companion object {
        internal fun create(
            existing: TransferStatus?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): TransferStatus {
            val status = parser.asString(data?.get("status"))
            val gasStatus = parser.asString(data?.get("gasStatus"))
            val axelarTransactionUrl = parser.asString(data?.get("axelarTransactionUrl"))

            val routeStatusList = parser.asList(data?.get("routeStatus"))
            val fromChain = parser.asMap(data?.get("fromChain"))
            val toChain = parser.asMap(data?.get("toChain"))

            val fromChainStatus = TransferChainStatus.create(
                existing?.fromChainStatus,
                parser,
                fromChain,
            )
            val toChainStatus = TransferChainStatus.create(
                existing?.toChainStatus,
                parser,
                toChain,
            )

            val routeStatuses = routeStatusList?.map { routeStatus ->
                val routeStatusMap = parser.asMap(routeStatus)
                TransferRouteStatus.create(
                    null,
                    parser,
                    routeStatusMap,
                )
            }

            var error = parser.asString(data?.get("errors"))
            if (error == null) {
                val errorData = data?.get("error")
                val size = parser.asMap(errorData)?.size ?: 0
                if (size > 0) {
                    error = parser.asString(errorData)
                }
            }

            val squidTransactionStatus = parser.asString(data?.get("squidTransactionStatus"))

            return if (existing == null ||
                existing.status != status ||
                existing.gasStatus != gasStatus ||
                existing.axelarTransactionUrl != axelarTransactionUrl ||
                existing.fromChainStatus != fromChainStatus ||
                existing.toChainStatus != toChainStatus ||
                existing.routeStatuses != routeStatuses ||
                existing.error != error ||
                existing.squidTransactionStatus != squidTransactionStatus
            ) {
                TransferStatus(
                    status = status,
                    gasStatus = gasStatus,
                    axelarTransactionUrl = axelarTransactionUrl,
                    fromChainStatus = fromChainStatus,
                    toChainStatus = toChainStatus,
                    routeStatuses = routeStatuses,
                    error = error,
                    squidTransactionStatus = squidTransactionStatus,
                )
            } else {
                existing
            }
        }
    }
}

@JsExport
@Serializable
class TransferChainStatus(
    val transactionUrl: String?,
    val transactionId: String?,
) {
    companion object {
        internal fun create(
            existing: TransferChainStatus?,
            parser: ParserProtocol,
            chain: Map<*, *>?,
        ): TransferChainStatus {
            val transactionUrl = parser.asString(chain?.get("transactionUrl"))
            val transactionId = parser.asString(chain?.get("transactionId"))

            return if (existing == null ||
                existing.transactionUrl != transactionUrl ||
                existing.transactionId != transactionId
            ) {
                TransferChainStatus(transactionUrl, transactionId)
            } else {
                existing
            }
        }
    }
}

@JsExport
@Serializable
class TransferRouteStatus(
    val chainId: String?,
    val txHash: String?,
    val status: String?,
    val action: String?,
) {
    companion object {
        internal fun create(
            existing: TransferRouteStatus?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): TransferRouteStatus {
            val chainId = parser.asString(data?.get("chainId"))
            val txHash = parser.asString(data?.get("txHash"))
            val status = parser.asString(data?.get("status"))
            val action = parser.asString(data?.get("action"))

            return if (existing == null ||
                existing.chainId != chainId ||
                existing.txHash != txHash ||
                existing.status != status ||
                existing.action != action
            ) {
                TransferRouteStatus(chainId, txHash, status, action)
            } else {
                existing
            }
        }
    }
}
