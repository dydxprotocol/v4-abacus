package exchange.dydx.abacus.output

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
    MEDIUM(3),
    HIGH(4),
    URGENT(5);

    companion object {
        operator fun invoke(rawValue: Int?) =
            NotificationPriority.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable(with = NotificationSerializer::class)
data class Notification(
    val id: String,
    val type: NotificationType,
    val priority: NotificationPriority,
    val image: String?,
    val title: String,
    val text: String?,
    val obj: Any?,
    val updateTimeInMilliseconds: Double,
) {

}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Notification::class)
object NotificationSerializer : KSerializer<Notification> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Notification") {
        element<String>("id")
        element<String>("type")
        element<Int>("priority")
        element<String>("image", isOptional = true)
        element<String>("title")
        element<String?>("text", isOptional = true)
        element<Double>("updateTimeInMilliseconds")
    }


    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: Notification) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.id)
            encodeStringElement(descriptor, 1, value.type.rawValue)
            encodeIntElement(descriptor, 2, value.priority.rawValue)
            if (value.image != null) {
                encodeStringElement(descriptor, 3, value.image)
            }
            encodeStringElement(descriptor, 4, value.title)
            if (value.text != null) {
                encodeStringElement(descriptor, 5, value.text)
            }
            encodeDoubleElement(descriptor, 6, value.updateTimeInMilliseconds)
        }
    }

    override fun deserialize(decoder: Decoder): Notification {
        return decoder.decodeStructure(descriptor) {
            var type: NotificationType? = null
            var priority: NotificationPriority? = null
            var id: String? = null
            var image: String? = null
            var title: String? = null
            var text: String? = null
            var updateTimeInMilliseconds: Double? = null

            loop@ while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    DECODE_DONE -> break@loop

                    0 -> id = decodeStringElement(descriptor, 0)
                    1 -> type = NotificationType.invoke(decodeStringElement(descriptor, 1))
                    2 -> priority = NotificationPriority.invoke(decodeIntElement(descriptor, 2))
                    3 -> image = decodeStringElement(descriptor, 3)
                    4 -> title = decodeStringElement(descriptor, 4)
                    6 -> text = decodeStringElement(descriptor, 5)
                    7 -> updateTimeInMilliseconds = decodeDoubleElement(descriptor, 6)

                    else -> throw SerializationException("Unexpected index $index")
                }
            }

            Notification(
                id!!,
                type!!,
                priority!!,
                image,
                title!!,
                text,
                null,
                updateTimeInMilliseconds!!
            )
        }
    }
}
