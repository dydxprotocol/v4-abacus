package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.di.AbacusScope
import exchange.dydx.abacus.output.Documentation
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import kotlin.js.JsExport

@JsExport
@AbacusScope
@Inject
class DocumentationLoader internal constructor(
    configFileLoader: ConfigFileLoader
) {

    val documentation: Documentation? =
        // This is a blocking disk-read. Would be better to access this asynchronously,
        // but that is a larger refactor and this is fairly low prio (not shown on a main screen).
        // We don't need lazy here, because kotlin-inject accessors are handled lazily already.
        configFileLoader.load(ConfigFile.DOCUMENTATION) {
            runCatching { Json.decodeFromString<Documentation>(it) }
        }.getOrNull() // Not the end of the world if we fail to read.
}
