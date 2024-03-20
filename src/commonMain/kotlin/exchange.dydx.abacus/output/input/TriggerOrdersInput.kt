package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.DebugLogger
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class TriggerOrdersInputPrice(
    val limitPrice: Double?,
    val triggerPrice: Double?,
    val triggerPercent: Double?,
    val triggerInput: String?,
) {
  companion object {
    internal fun create(
        existing: TriggerOrdersInputPrice?,
        parser: ParserProtocol,
        data: Map<*, *>?,
    ): TriggerOrdersInputPrice? {
      DebugLogger.log("creating Trigger Orders Input Price\n")

      data?.let {
        val limitPrice = parser.asDouble(data["limitPrice"])
        val triggerPrice = parser.asDouble(data["triggerPrice"])
        val triggerPercent = parser.asDouble(data["triggerPercent"])
        val triggerInput = parser.asString(data["triggerInput"]) // xcxc I don't think this goes here
        
        return if (existing?.limitPrice != limitPrice ||
                existing?.triggerPrice != triggerPrice ||
                existing?.triggerPercent != triggerPercent ||
                existing?.triggerInput != triggerInput
        ) {
          TriggerOrdersInputPrice(limitPrice, triggerPrice, triggerPercent, triggerInput)
        } else {
          existing
        }
      }
      DebugLogger.log("Trigger Orders Input Price not valid\n")
      return null
    }
  }
}

@JsExport
@Serializable
data class TriggerOrdersInput(
    val marketId: String?,
    val size: Double?,
    val stopLossPrice: TriggerOrdersInputPrice?,
    val takeProfitPrice: TriggerOrdersInputPrice?,
) {
  companion object {
    internal fun create(
        existing: TriggerOrdersInput?,
        parser: ParserProtocol,
        data: Map<*, *>?,
    ): TriggerOrdersInput? {
      DebugLogger.log("creating Trigger Orders Input\n")

      data?.let {
        val marketId = parser.asString(data["marketId"])
        val size = parser.asDouble(data["size"])

        val stopLossPrice =
            TriggerOrdersInputPrice.create(
                existing?.stopLossPrice,
                parser,
                parser.asMap(data["stopLossPrice"])
            )
        val takeProfitPrice =
            TriggerOrdersInputPrice.create(
                existing?.takeProfitPrice,
                parser,
                parser.asMap(data["takeProfitPrice"])
            )

        return if (existing?.marketId != marketId ||
                existing?.size != size ||
                existing?.stopLossPrice != stopLossPrice ||
                existing?.takeProfitPrice != takeProfitPrice
        ) {
          TriggerOrdersInput(marketId, size, stopLossPrice, takeProfitPrice)
        } else {
          existing
        }
      }
      DebugLogger.log("Trigger Orders Input not valid\n")
      return null
    }
  }
}
