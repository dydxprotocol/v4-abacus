package exchange.dydx.abacus.utils

import exchange.dydx.abacus.protocols.FormatterProtocol
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ThreadingProtocol
import exchange.dydx.abacus.protocols.ThreadingType

class Threading : ThreadingProtocol {
    override fun async(type: ThreadingType, block: (() -> Unit)) {
        block()
    }
}

class DummyLocalizer : LocalizerProtocol {
    override fun localize(path: String, paramsAsJson: String?): String {
        return path
    }
}

class DummyFormatter : FormatterProtocol {
    override fun percent(value: Double?, digits: Int): String? {
        return value.toString()
    }

    override fun dollar(value: Double?, tickSize: String?): String? {
        return value.toString()
    }
}
