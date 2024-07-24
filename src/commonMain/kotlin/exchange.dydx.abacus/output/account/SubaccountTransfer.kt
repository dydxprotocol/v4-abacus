package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.ParsingHelper
import kollections.JsExport
import kollections.toIList
import kotlinx.serialization.Serializable

/*
debit and credit info are set depending on the type of transfer
*/
@JsExport
@Serializable
data class SubaccountTransfer(
    val id: String,
    val type: TransferRecordType,
    val asset: String?,
    val amount: Double?,
    val updatedAtBlock: Int?,
    val updatedAtMilliseconds: Double,
    val fromAddress: String?,
    val toAddress: String?,
    val transactionHash: String?,
    val resources: SubaccountTransferResources,
) {
    companion object {
        internal fun create(
            existing: SubaccountTransfer?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): SubaccountTransfer? {
            Logger.d { "creating Account Transfer\n" }
            data?.let {
                val id = parser.asString(data["id"])
                val updatedAt =
                    parser.asDatetime(data["confirmedAt"]) ?: parser.asDatetime(data["createdAt"])
                val updatedAtMilliseconds = updatedAt?.toEpochMilliseconds()?.toDouble()
                val resources = parser.asMap(data["resources"])?.let {
                    SubaccountTransferResources.create(existing?.resources, parser, it)
                }
                if (id != null && updatedAtMilliseconds != null && resources != null) {
                    val type =
                        TransferRecordType.invoke(parser.asString(data["type"])) ?: return null
                    val asset = parser.asString(data["asset"])
                    val amount = parser.asDouble(data["amount"])
                    val fromAddress = parser.asString(data["fromAddress"])
                    val toAddress = parser.asString(data["toAddress"])
                    val updatedAtBlock = parser.asInt(data["updatedAtBlock"])
                    val transactionHash = parser.asString(data["transactionHash"])
                    return if (existing?.id != id ||
                        existing.type !== type ||
                        existing.asset != asset ||
                        existing.amount != amount ||
                        existing.updatedAtBlock != updatedAtBlock ||
                        existing.updatedAtMilliseconds != updatedAtMilliseconds ||
                        existing.fromAddress != fromAddress ||
                        existing.toAddress != toAddress ||
                        existing.transactionHash != transactionHash ||
                        existing.resources !== resources
                    ) {
                        SubaccountTransfer(
                            id,
                            type,
                            asset,
                            amount,
                            updatedAtBlock,
                            updatedAtMilliseconds,
                            fromAddress,
                            toAddress,
                            transactionHash,
                            resources,
                        )
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "Account Transfer not valid" }
            return null
        }

        fun create(
            existing: IList<SubaccountTransfer>?,
            parser: ParserProtocol,
            data: List<Map<String, Any>>?,
        ): IList<SubaccountTransfer>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as SubaccountTransfer).updatedAtMilliseconds
                val time2 =
                    (
                        parser.asDatetime(itemData["confirmedAt"])
                            ?: parser.asDatetime(itemData["createdAt"])
                        )?.toEpochMilliseconds()
                        ?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, false)
            }, { _, obj, itemData ->
                obj ?: SubaccountTransfer.create(
                    null,
                    parser,
                    parser.asMap(itemData),
                )
            })?.toIList()
        }
    }
}

/*
typeStringKey, statusStringKey, iconLocal and indicator are set to optional, in case
BE returns new transfer type enum values or status enum values which Abacus doesn't recognize
*/
@JsExport
@Serializable
data class SubaccountTransferResources(
    val typeString: String?,
    val statusString: String?,
    val typeStringKey: String?,
    val blockExplorerUrl: String?,
    val statusStringKey: String?,
    val iconLocal: String?,
    val indicator: String?,
) {
    companion object {
        internal fun create(
            existing: SubaccountTransferResources?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol? = null,
        ): SubaccountTransferResources? {
            Logger.d { "creating Account Transfer Resources\n" }

            data?.let {
                val typeStringKey = parser.asString(data["typeStringKey"])
                val blockExplorerUrl = parser.asString(data["blockExplorerUrl"])
                val statusStringKey = parser.asString(data["statusStringKey"])
                val iconLocal = parser.asString(data["iconLocal"])
                val indicator = parser.asString(data["indicator"])
                return if (existing?.typeStringKey != typeStringKey ||
                    existing?.blockExplorerUrl != blockExplorerUrl ||
                    existing?.statusStringKey != statusStringKey ||
                    existing?.iconLocal != iconLocal ||
                    existing?.indicator != indicator
                ) {
                    val typeString =
                        if (typeStringKey != null) localizer?.localize(typeStringKey) else null
                    val statusString =
                        if (statusStringKey != null) localizer?.localize(statusStringKey) else null
                    SubaccountTransferResources(
                        typeString,
                        statusString,
                        typeStringKey,
                        blockExplorerUrl,
                        statusStringKey,
                        iconLocal,
                        indicator,
                    )
                } else {
                    existing
                }
            }

            Logger.d { "Account Transfer Resources not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
enum class TransferRecordType(val rawValue: String) {
    DEPOSIT("DEPOSIT"),
    WITHDRAW("WITHDRAWAL"),
    TRANSFER_IN("TRANSFER_IN"),
    TRANSFER_OUT("TRANSFER_OUT");

    companion object {
        operator fun invoke(rawValue: String?) =
            TransferRecordType.values().firstOrNull { it.rawValue == rawValue }
    }
}
