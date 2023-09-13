package exchange.dydx.abacus.state.app.helper

import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.protocols.AbacusLocalizerProtocol
import exchange.dydx.abacus.protocols.FileLocation
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.readCachedTextFile
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.utils.DebugLogger
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.iMutableMapOf
import kollections.iListOf
import kollections.iMutableListOf
import kollections.toIList
import kollections.toIMutableMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

data class Language(val code: String, val name: String, val path: String?)

class DynamicLocalizer(
    private val ioImplementations: IOImplementations,
    private val systemLanguage: String,
    private val path: String,
    private val endpoint: String,
    private val loadLocalOnly: Boolean = false,
) : AbacusLocalizerProtocol {
    private val parser = Parser()
    private var _languages: IList<Language> = iListOf()
        set(value) {
            if (field != value) {
                field = value
                val languages = iMutableListOf<SelectionOption>()
                val languagesMap = iMutableMapOf<String, Language>()
                for (item in value) {
                    languages.add(SelectionOption(item.code, item.name, null))
                    languagesMap[item.code] = item
                }
                this.languages = languages
                this._languagesMap = languagesMap
            }
        }

    private var _languagesMap: IMap<String, Language> = iMapOf()
        set(value) {
            field = value
            matchLanguage()
        }

    override var languages: List<SelectionOption> = iListOf()
        private set

    private var _loadingLanguage: String? = null

    override var language: String? = null
        set(value) {
            if (field != value) {
                field = value
                updateLanguageData()
            }
        }

    // For all languages
    // language -> path -> data
    private var languagesData: IMap<String, IMap<String, IMap<String, Any>>> = iMapOf()
        set(value) {
            field = value
            updateLanguageData()
        }

    // For selected language
    // path -> data
    private var languageData: IMap<String, IMap<String, Any>>? = null

    init {
        val languagePath = "/localization/languages.json"
        val filePath = "$path$languagePath"
        ioImplementations.threading?.async(ThreadingType.abacus) {
            // Try to read from local
            val result = if (loadLocalOnly) {
                ioImplementations.fileSystem?.readTextFile(FileLocation.AppBundle, filePath)
            } else {
                ioImplementations.fileSystem?.readCachedTextFile(filePath)
            }
            if (result != null) {
                val list = Json.parseToJsonElement(result).jsonArray.toIList()
                if (list.size != 0) {
                    _languages = parseLanguages(list)
                }
            }
        }
        if (!loadLocalOnly) {
            // Try to update from online
            val url = "$endpoint$languagePath"
            ioImplementations.rest?.get(url, null) { response, code ->
                if (code in 200..299 && response != null) {
                    val list = Json.parseToJsonElement(response).jsonArray.toIList()
                    if (list.size != 0) {
                        ioImplementations.threading?.async(ThreadingType.main) {
                            _languages = parseLanguages(list)
                            ioImplementations.fileSystem?.writeTextFile(filePath, response)
                        }
                    }
                }
            }
        }
    }

    private fun parseLanguages(items: IList<Any>): IList<Language> {
        val languages = iMutableListOf<Language>()
        for (item in items) {
            val map = parser.asMap(item)
            if (map != null) {
                val code = parser.asString(map["code"])
                val name = parser.asString(map["name"])
                val path = parser.asString(map["path"])
                if (code != null && name != null) {
                    languages.add(Language(code, name, path))
                }
            }
        }
        return languages
    }

    private fun matchLanguage() {
        val language = primaryLanguageCode(systemLanguage) ?: "en"   // Default to English
        setLanguage(language) { successful, _ ->
            if (!successful) {
                if (language != "en") {
                    setLanguage("en") { successful, _ ->
                        if (!successful) {
                            DebugLogger.error("Unable to set language: $language", null)
                        }
                    }
                } else {
                    DebugLogger.error("Unable to set language: $language", null)
                }
            }
        }
    }

    private fun loadLocalLanguage(language: String): IMap<String, IMap<String, Any>>? {
        val files = filePaths(language).map { "$path/$it" }
        return loadLocalLanguageFiles(files)
    }

    private fun filePaths(language: String): IList<String> {
        val languageCode = _languagesMap[language]?.path ?: language
        return iListOf(
            "localization/$languageCode/app.json",
            "localization/$languageCode/tooltips.json",
            "localizations_native/$languageCode/app.json",
            "localization_notifications/$languageCode/app.json",
        )
    }



    private fun loadLocalLanguageFiles(filePaths: IList<String>): IMap<String, IMap<String, Any>>? {
        val result = iMutableMapOf<String, IMap<String, Any>>()
        for (filePath in filePaths) {
            val data = loadLocalLanguageFile(filePath)
            if (data != null) {
                result[filePath] = data
            }
        }
        return if (result.size > 0) result else null
    }

    private fun loadLocalLanguageFile(filePath: String): IMap<String, Any>? {
        val result = if (loadLocalOnly) {
            ioImplementations.fileSystem?.readTextFile(FileLocation.AppBundle, filePath)
        } else {
            ioImplementations.fileSystem?.readCachedTextFile(filePath)
        }
        if (result != null) {
            return parser.asMap(Json.parseToJsonElement(result).jsonObject)
        }
        return null
    }

    private fun refreshLanguage(
        language: String,
        callback: ((successful: Boolean, error: ParsingError?) -> Unit)?,
    ) {
        val files = filePaths(language)
        var resultCount = 0
        for (file in files) {
            val url = "$endpoint/$file"
            ioImplementations.rest?.get(url, null) { response, code ->
                ioImplementations.threading?.async(ThreadingType.main) {
                    if (code in 200..299 && response != null) {
                        val data = parser.asMap(Json.parseToJsonElement(response).jsonObject)
                        if (data != null && data.size != 0) {
                            mergeLanguageData(data, file, language)
                        }
                    }
                    resultCount += 1
                    if (resultCount == files.size) {
                        callback?.invoke((resultCount != 0), null)
                    }
                }
            }
        }
    }

    private fun mergeLanguageData(
        data: IMap<String, Any>,
        filePath: String,
        language: String,
    ) {
        val modified = languagesData.toIMutableMap()
        val languageData = modified[language]?.toIMutableMap() ?: iMutableMapOf()
        languageData[filePath] = data
        modified[language] = languageData
        languagesData = modified
    }

    private fun updateLanguageData() {
        languageData = if (language != null) languagesData[language] else null
    }

    override fun setLanguage(
        language: String,
        callback: (successful: Boolean, error: ParsingError?) -> Unit,
    ) {
        if (_languagesMap[language] != null) {
            if (language != _loadingLanguage) {
                _loadingLanguage = language

                if (languagesData[language] != null) {
                    // If the language is already loaded, no need to read from file system again
                    this.language = language
                    callback(true, null)
                    refreshLanguage(language, null)
                } else {
                    // Load the language from file system
                    val languageData = loadLocalLanguage(language)
                    if (languageData != null) {
                        // If the language is loaded from file system
                        // It's OK to use the localizer. Refresh in the background
                        val modified = languagesData.toIMutableMap()
                        modified[language] = languageData
                        languagesData = modified
                        this.language = language
                        callback(true, null)
                        refreshLanguage(language, null)
                    } else {
                        // If the language doesn't exist locally, try to refresh it
                        // Only call the callback when the refresh is done
                        refreshLanguage(language) { successful, error ->
                            if (successful) {
                                this.language = language
                            }
                            callback(successful, error)
                        }
                    }
                }
            } else {
                // Not a valid language
                callback(false, null)
            }
        }
    }

    private fun primaryLanguageCode(language: String): String {
        val elements = language.split("-")
        return elements.firstOrNull() ?: language
    }

    override fun localize(
        path: String,
        paramsAsJson: String?,
    ): String {
        val params = if (paramsAsJson != null) parser.decodeJsonObject(paramsAsJson) else null
        val languageData = languageData
        if (languageData != null) {
            for ((_, data) in languageData) {
                val value = localize(path, params, data)
                if (value != null) {
                    return value
                }
            }
            DebugLogger.debug("Unable to localize path: $path")
            return path
        } else {
            DebugLogger.debug("Unable to localize path: $path")
            return path
        }
    }

    private fun localize(
        path: String,
        params: IMap<String, Any>?,
        data: IMap<String, Any>,
    ): String? {
        val value = parser.value(data, path)
        return if (value != null) {
            val text = parser.asString(value)
            if (text != null) {
                if (params != null) {
                    var result = text
                    for ((_key, _value) in params) {
                        val key = "{" + _key + "}"
                        result = result?.replace(key, parser.asString(_value) ?: "")
                    }
                    result ?: text
                } else {
                    text
                }
            } else null
        } else null
    }
}
