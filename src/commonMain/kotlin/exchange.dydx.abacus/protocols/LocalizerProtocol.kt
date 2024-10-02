package exchange.dydx.abacus.protocols

import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.utils.toJsonPrettyPrint
import kollections.JsExport

@JsExport
interface LocalizerProtocol {
    fun localize(path: String, paramsAsJson: String? = null): String
}

fun LocalizerProtocol.localizeWithParams(path: String, params: Map<String, String>?): String =
    if (params == null) localize(path = path) else localize(path = path, paramsAsJson = params.toJsonPrettyPrint())

interface AbacusLocalizerProtocol : LocalizerProtocol {
    val languages: List<SelectionOption>

    var language: String?

    fun setLanguage(language: String, callback: (successful: Boolean, error: ParsingError?) -> Unit)
}
