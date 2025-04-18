package exchange.dydx.abacus.state.helper

import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.responses.ParsingException
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kollections.JsExport
import kollections.iMutableListOf
import kollections.toIList
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class AbUrl(
    val host: String,
    val port: Int? = null,
    val path: String = "",
    val scheme: String? = null,
    val params: IList<NetworkParam>? = null,
) {
    @Suppress("MemberVisibilityCanBePrivate")
    val urlString: String
        get() {
            val sb = StringBuilder()
            if (scheme != null) {
                sb.append(scheme).append("://")
            }
            sb.append(host)
            if (port != null && (scheme == "https" && port != getDefaultPort())) {
                sb.append(":$port")
            }
            sb.append(path)
            if (params != null) {
                sb.append(params.paramString())
            }
            return sb.toString()
        }

    companion object {
        private fun fromKtor(url: Url) =
            AbUrl(
                host = url.host,
                port = url.port,
                path = url.encodedPath,
                scheme = url.protocol.name,
                params = fromKtorParams(url.parameters),
            )

        fun fromString(urlString: String): AbUrl = fromKtor(URLBuilder(urlString).build())

        private fun fromKtorParams(params: Parameters): IList<NetworkParam>? {
            val converted = iMutableListOf<NetworkParam>()
            params.forEach { key, values ->
                converted.add(NetworkParam(key, values.firstOrNull()))
            }
            return if (converted.size > 0) converted else null
        }
    }

    fun getDefaultPort(): Int? {
        return when (scheme) {
            "https", "wss" -> 443
            "http", "ws" -> 80
            else -> null
        }
    }

    @Throws(Exception::class)
    fun validate(): AbUrl {
        val check = true
        val strict = true
        val validatedAbUrl = if (check) fromString(urlString) else this
        if (strict && validatedAbUrl.urlString != this.urlString) {
            throw ParsingException(
                type = ParsingErrorType.InvalidUrl,
                "$urlString\ndoes not recreate\n$this but instead creates $validatedAbUrl",
            )
        }
        return validatedAbUrl
    }
}

@JsExport
@Serializable
enum class HttpVerb(val rawValue: String) {
    get("GET"),
    post("POST"),
    put("PUT"),
    delete("DELETE");

    companion object {
        operator fun invoke(rawValue: String) =
            values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class NetworkParam(val key: String, val value: String? = null) {
    override fun toString() = "$key=${value ?: ""}"

    companion object {
        internal fun convert(params: IMap<String, String>?): IList<NetworkParam>? {
            if (params == null || params.isEmpty()) {
                return null
            }
            val list = iMutableListOf<NetworkParam>()
            for ((key, value) in params) {
                list.add(NetworkParam(key, value))
            }
            return list
        }

        fun parse(params: String?): IList<NetworkParam>? {
            if (params == null) return null
            return (if (params.startsWith("?")) params.substring(startIndex = 1) else params)
                .split("&")
                .map {
                    val keyAndValue = it.split("=")
                    NetworkParam(
                        keyAndValue.first(),
                        if (keyAndValue.size == 2) keyAndValue.last() else null,
                    )
                }.toIList()
        }
    }
}

internal fun IList<NetworkParam>.paramString(): String {
    if (isEmpty()) {
        return ""
    }
    val sb = StringBuilder()
    forEach {
        sb.append(if (sb.isBlank()) "?" else "&")
            .append(it)
    }
    return sb.toString()
}
