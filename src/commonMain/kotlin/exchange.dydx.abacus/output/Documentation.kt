package exchange.dydx.abacus.output

import exchange.dydx.abacus.utils.IList
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class Documentation(
    val tradingRewardsFAQs: IList<FAQ>
)

@JsExport
@Serializable
data class FAQ(
    val questionLocalizationKey: String,
    val answerLocalizationKey: String
)
