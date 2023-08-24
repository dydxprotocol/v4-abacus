package exchange.dydx.abacus.output

import exchange.dydx.abacus.utils.IMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlin.js.JsExport

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
    val data: IMap<String, String>?,
    val updateTimeInMilliseconds: Double,
) {
}
