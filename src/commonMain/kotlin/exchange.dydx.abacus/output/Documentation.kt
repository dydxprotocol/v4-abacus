package exchange.dydx.abacus.output

import exchange.dydx.abacus.utils.IList
import kollections.iListOf
import kollections.toIList
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
    val questionStringKey: String,
    val answerStringKey: String
)
