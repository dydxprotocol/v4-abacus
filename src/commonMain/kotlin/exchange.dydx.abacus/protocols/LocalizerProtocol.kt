package exchange.dydx.abacus.protocols

import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.responses.ParsingError
import kollections.JsExport


@JsExport
interface LocalizerProtocol {
    fun localize(path: String, paramsAsJson: String? = null): String
}

interface AbacusLocalizerProtocol: LocalizerProtocol {
    val languages: List<SelectionOption>

    var language: String?

    fun setLanguage(language: String, callback: (successful: Boolean, error: ParsingError?) -> Unit)
}