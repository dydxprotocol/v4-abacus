package exchange.dydx.abacus.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

internal fun Instant.nextMonth(): Instant {
    val localTime: LocalDateTime = this.toLocalDateTime(TimeZone.UTC)
    var month = localTime.month
    var year = localTime.year
    var monthNumber = month.number
    monthNumber++
    if (monthNumber >= 12) {
        month = Month.JANUARY
        year += 1
    } else {
        month = Month(monthNumber)
    }
    val nextMonth = LocalDateTime(year, month, 1, 0, 0, 0)
    return nextMonth.toInstant(TimeZone.UTC)
}

internal fun Instant.previousMonth(): Instant {
    val localTime: LocalDateTime = this.toLocalDateTime(TimeZone.UTC)
    var month = localTime.month
    var year = localTime.year
    var monthNumber = month.number
    monthNumber--
    if (monthNumber <= 0) {
        month = Month.DECEMBER
        year -= 1
    } else {
        month = Month(monthNumber)
    }
    val previousMonth = LocalDateTime(year, month, 1, 0, 0, 0)
    return previousMonth.toInstant(TimeZone.UTC)
}
