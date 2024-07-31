package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.ParsingHelper
import kollections.JsExport
import kollections.toIList
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class SubaccountFundingPayment(
    val marketId: String,
    val payment: Double,
    val rate: Double,
    val positionSize: Double,
    val price: Double?,
    val effectiveAtMilliSeconds: Double,
) {
    companion object {
        internal fun create(
            existing: SubaccountFundingPayment?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): SubaccountFundingPayment? {
            Logger.d { "creating Account Funding Payment\n" }

            data?.let {
                val marketId = parser.asString(data["marketId"])
                val payment = parser.asDouble(data["payment"])
                val rate = parser.asDouble(data["rate"])
                val positionSize = parser.asDouble(data["positionSize"])
                val price = parser.asDouble(data["price"])
                val effectiveAtMilliSeconds =
                    parser.asDatetime(data["effectiveAt"])?.toEpochMilliseconds()?.toDouble()
                if (marketId != null && payment != null && rate != null && positionSize != null && effectiveAtMilliSeconds != null) {
                    return if (existing?.marketId != marketId ||
                        existing.payment != payment ||
                        existing.rate != rate ||
                        existing.positionSize != positionSize ||
                        existing.price != price ||
                        existing.effectiveAtMilliSeconds != effectiveAtMilliSeconds
                    ) {
                        SubaccountFundingPayment(
                            marketId,
                            payment,
                            rate,
                            positionSize,
                            price,
                            effectiveAtMilliSeconds,
                        )
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "Account Funding Payment not valid" }
            return null
        }

        fun create(
            existing: IList<SubaccountFundingPayment>?,
            parser: ParserProtocol,
            data: List<Map<String, Any>>?,
        ): IList<SubaccountFundingPayment>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as SubaccountFundingPayment).effectiveAtMilliSeconds
                val time2 =
                    parser.asDatetime(itemData["effectiveAt"])?.toEpochMilliseconds()
                        ?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, true)
            }, { _, obj, itemData ->
                obj ?: SubaccountFundingPayment.create(
                    null,
                    parser,
                    parser.asMap(itemData),
                )
            })?.toIList()
        }
    }
}
