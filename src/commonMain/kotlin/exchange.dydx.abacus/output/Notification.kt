package exchange.dydx.abacus.output

import exchange.dydx.abacus.utils.IMap
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
enum class NotificationType(val rawValue: String) {
    INFO("INFO"),
    WARNING("WARNING"),
    ERROR("ERROR"),
}

@JsExport
@Serializable
enum class NotificationPriority(val rawValue: Int) {
    NORMAL(0),
    MEDIUM(3),
    HIGH(4),
    URGENT(5),
}


@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
@Serializable
class Notification(
    val type: NotificationType,
    val priority: NotificationPriority,
    val id: String,
    val image: String?,
    val title: String,
    val text: String?,
    val data: IMap<String, String>?,
    val updateTimeInMilliseconds: Double,
) {
}