package exchange.dydx.abacus.output

import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class NotificationType(val rawValue: String) {
    INFO("INFO"),
    WARNING("WARNING"),
    ERROR("ERROR");

    companion object {
        operator fun invoke(rawValue: String?) =
            NotificationType.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
enum class NotificationPriority(val rawValue: Int) {
    NORMAL(0),
    URGENT(5);

    companion object {
        operator fun invoke(rawValue: Int?) =
            NotificationPriority.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class Notification(
    val id: String,
    val type: NotificationType,
    val priority: NotificationPriority,
    val image: String?,
    val title: String,
    val text: String?,
    val link: String?,
    val data: String?, // JSON String
    val updateTimeInMilliseconds: Double,
)
