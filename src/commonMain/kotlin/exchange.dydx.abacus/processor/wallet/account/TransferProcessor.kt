package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableMap
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.iMutableMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

internal class TransferProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val transferKeyMap = iMapOf(
        "string" to iMapOf(
            "id" to "id",
            "clientId" to "clientId",
            "type" to "type",
            "symbol" to "asset",
            "fromAddress" to "fromAddress",
            "toAddress" to "toAddress",
            "senderWallet" to "fromAddress",
            "recipientWallet" to "toAddress",
            "status" to "status",
            "transactionHash" to "transactionHash"
        ),
        "datetime" to iMapOf(
            "createdAtHeight" to "updatedAtBlock",
            "createdAt" to "createdAt",
            "confirmedAt" to "confirmedAt"
        ),
        "double" to iMapOf(
            "size" to "amount",
        )
    )

    private val typeMap = iMapOf(
        "DEPOSIT" to "APP.GENERAL.DEPOSIT",
        "WITHDRAWAL" to "APP.GENERAL.WITHDRAW",
        "FAST_WITHDRAWAL" to "APP.GENERAL.FAST_WITHDRAW",
        "TRANSFER_OUT" to "APP.GENERAL.TRANSFER_OUT",
        "TRANSFER_IN" to "APP.GENERAL.TRANSFER_IN"
    )

    private val typeIconMap = iMapOf(
        "DEPOSIT" to "Incoming",
        "WITHDRAWAL" to "Outgoing",
        "FAST_WITHDRAWAL" to "Outgoing",
        "TRANSFER_OUT" to "Outgoing",
        "TRANSFER_IN" to "Incoming"
    )

    private val statusMap = iMapOf(
        "PENDING" to "pending",
        "CONFIRMED" to "confirmed",
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        val modified = transform(existing, payload, transferKeyMap).mutable()

        val sender = parser.asMap(payload["sender"])
        val recipient = parser.asMap(payload["recipient"])

        sender?.let { 
            modified.safeSet("fromAddress", parser.asString(it["address"]))
        }
        recipient?.let { 
            modified.safeSet("toAddress", parser.asString(it["address"]))
        }
        
        // for v3
        val debitAmount = parser.asDouble(payload["debitAmount"])
        val creditAmount = parser.asDouble(payload["creditAmount"])
        if (debitAmount != null && debitAmount != Numeric.double.ZERO) {
            modified["amount"] = debitAmount
            modified.safeSet("asset", parser.asString(payload["debitAsset"]))
        } else if (creditAmount != null  && debitAmount != Numeric.double.ZERO) {
            modified["amount"] = creditAmount
            modified.safeSet("asset", parser.asString(payload["creditAsset"]))
        }

        if (modified["status"] == null) {
            modified["status"] = "CONFIRMED"
        }
        updateResource(modified)
        return modified
    }

    private fun updateResource(transfer: IMutableMap<String, Any>) {
        val resources = iMutableMapOf<String, Any>()
        parser.asString(transfer["type"])?.let { type ->
            val modifiedType = when (type) {
                // For DEPOSIT, we always show as "Deposit"
                "WITHDRAWAL" -> {
                    val toAddress = parser.asString(transfer["toAddress"])
                    if (toAddress == accountAddress || toAddress == null) {
                        type
                    } else {
                        "TRANSFER_OUT"
                    }
                }
                else -> type
            }
            typeMap[modifiedType]?.let {
                resources["typeStringKey"] = it
            }
            typeIconMap[modifiedType]?.let {
                resources["iconLocal"] = it
            }
        }
        transfer["status"]?.let {
            statusMap[it]?.let {
                resources["indicator"] = it
            }
        }
        parser.asString(transfer["transactionHash"])?.let {
            resources["blockExplorerUrl"] = "https://etherscan.org?transactionHas=${it}"
        }

        transfer["resources"] = resources
    }
}
