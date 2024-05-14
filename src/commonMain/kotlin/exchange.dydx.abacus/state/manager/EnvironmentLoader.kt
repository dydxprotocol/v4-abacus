package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.di.AbacusScope
import exchange.dydx.abacus.di.Deployment
import exchange.dydx.abacus.di.DeploymentUri
import exchange.dydx.abacus.di.EnvironmentId
import exchange.dydx.abacus.di.EnvironmentIdParameter
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.UIImplementations
import kollections.iMutableListOf
import me.tatarka.inject.annotations.Inject

data class EnvAndAppSettings(
    val environmentId: EnvironmentId,
    val environments: List<V4Environment>,
    val appSettings: AppSettings?,
)

@AbacusScope
@Inject
class EnvironmentLoader(
    private val environmentParser: EnvironmentParser,
    configFileLoader: ConfigFileLoader,
) {
    val envAndAppSettings: EnvAndAppSettings =
        // This is a blocking disk-read call.
        // Since this is only at startup, and environment info is absolutely critical, we are okay with this.
        // It would be far more complex to provide the final environment to the graph asynchronously, as all downstream consumers would
        // need to become reactive, and literally everything depends on the environment.
        configFileLoader.load(ConfigFile.ENV, environmentParser::parse).getOrThrow()
}

@Inject
class EnvironmentParser(
    private val deploymentUri: DeploymentUri,
    private val deployment: Deployment,
    private val environmentIdParameter: EnvironmentIdParameter?,
    private val uiImplementations: UIImplementations,
) {

    fun parse(environmentsJson: String): Result<EnvAndAppSettings> {
        val parser = Parser()
        val items = parser.decodeJsonObject(environmentsJson)
        val deployments = parser.asMap(items?.get("deployments")) ?: return Result.failure(RuntimeException("Failure to parse deployments"))
        val target = parser.asMap(deployments[deployment]) ?: return Result.failure(RuntimeException("Failure to parse deployment: $deployment"))
        val targetEnvironments = parser.asList(target["environments"]) ?: return Result.failure(RuntimeException("Failure to parse target environments"))
        val targetDefault = parser.asString(target["default"])

        val tokensData = parser.asNativeMap(items?.get("tokens"))
        val linksData = parser.asNativeMap(items?.get("links"))
        val walletsData = parser.asNativeMap(items?.get("wallets"))
        val governanceData = parser.asNativeMap(items?.get("governance"))

        if (items != null) {
            val environmentsData = parser.asMap(items["environments"]) ?: return Result.failure(RuntimeException("Failure to parse environments"))
            val parsedEnvironments = mutableMapOf<String, V4Environment>()
            for ((key, value) in environmentsData) {
                val data = parser.asMap(value) ?: continue
                val dydxChainId = parser.asString(data["dydxChainId"]) ?: continue
                val environment = V4Environment.parse(
                    key,
                    data,
                    parser,
                    deploymentUri,
                    uiImplementations.localizer,
                    parser.asNativeMap(tokensData?.get(dydxChainId)),
                    parser.asNativeMap(linksData?.get(dydxChainId)),
                    parser.asNativeMap(walletsData?.get(dydxChainId)),
                    parser.asNativeMap(governanceData?.get(dydxChainId)),
                ) ?: continue
                parsedEnvironments[environment.id] = environment
            }
            if (parsedEnvironments.isEmpty()) {
                return Result.failure(RuntimeException("Parsed environments was empty."))
            }
            val environments = iMutableListOf<V4Environment>()
            for (environmentId in targetEnvironments) {
                val environment = parsedEnvironments[parser.asString(environmentId)!!]
                if (environment != null) {
                    environments.add(environment)
                }
            }

            val appSettings = parser.asMap(items["apps"])?.let { AppSettings.parse(it, parser) }

            return Result.success(
                EnvAndAppSettings(
                    environmentId = requireNotNull(environmentIdParameter ?: targetDefault) { "environmentId was null and no target default defined." },
                    environments = environments,
                    appSettings = appSettings,
                ),
            )
        } else {
            return Result.failure(RuntimeException("Failure to env json."))
        }
    }
}
