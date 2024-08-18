package exchange.dydx.abacus.utils

import exchange.dydx.abacus.protocols.ParserProtocol
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class GoodTil {
    companion object {
        internal fun duration(goodTil: Map<String, Any>?, parser: ParserProtocol): Duration? {
            if (goodTil === null) return null
            val duration = parser.asInt(goodTil["duration"]) ?: return null
            val timeInterval = when (parser.asString(goodTil["unit"])) {
                "M" -> duration.minutes
                "H" -> duration.hours
                "D" -> duration.days
                "W" -> (duration * 7).days
                else -> return null
            }
            return timeInterval
        }
    }
}
