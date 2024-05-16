package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.di.DeploymentUri
import exchange.dydx.abacus.protocols.FileLocation
import exchange.dydx.abacus.protocols.readCachedTextFile
import exchange.dydx.abacus.state.v2.supervisor.AppConfigsV2
import exchange.dydx.abacus.utils.IOImplementations
import me.tatarka.inject.annotations.Inject

@Inject
class ConfigFileLoader(
    private val deploymentUri: DeploymentUri,
    private val appConfigs: AppConfigsV2,
    private val ioImplementations: IOImplementations,
) {
    fun <T> load(configFile: ConfigFile, parse: (String) -> Result<T>): Result<T> {
        val config = if (appConfigs.loadRemote) {
            loadFromCachedConfigFile(configFile).also {
                fetchRemoteConfigFile(configFile, parse)
            }
        } else {
            loadFromBundledLocalConfigFile(configFile)
        }
        return config?.let { parse(it) } ?: Result.failure(RuntimeException("Could not parse config file."))
    }

    private fun <T> fetchRemoteConfigFile(configFile: ConfigFile, parse: (String) -> Result<T>) {
        val path = configFile.path
        val configFileUrl = "$deploymentUri$path"
        ioImplementations.rest?.get(configFileUrl, null, callback = { response, httpCode, _ ->
            if (httpCode in 200..299 && response != null) {
                if (parse(response).isSuccess) {
                    writeToLocalFile(response, path)
                }
            }
        })
    }

    private fun loadFromCachedConfigFile(configFile: ConfigFile): String? {
        return ioImplementations.fileSystem?.readCachedTextFile(
            configFile.path,
        )
    }

    private fun loadFromBundledLocalConfigFile(configFile: ConfigFile): String? {
        return ioImplementations.fileSystem?.readTextFile(
            FileLocation.AppBundle,
            configFile.path,
        )
    }

    private fun writeToLocalFile(response: String, file: String) {
        ioImplementations.fileSystem?.writeTextFile(
            file,
            response,
        )
    }
}
