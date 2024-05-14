package exchange.dydx.abacus.di

import exchange.dydx.abacus.output.Documentation
import exchange.dydx.abacus.protocols.DataNotificationProtocol
import exchange.dydx.abacus.protocols.PresentationProtocol
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.state.manager.DocumentationLoader
import exchange.dydx.abacus.state.manager.EnvironmentLoader
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.v2.manager.AsyncAbacusStateManagerV2
import exchange.dydx.abacus.state.v2.supervisor.AppConfigsV2
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.UIImplementations
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.js.JsExport

// kotlin-inject handles qualifiers via typealiases (though Dagger-style @Qualifier annotations are coming soon)
typealias DeploymentUri = String
typealias Deployment = String // MAINNET, TESTNET, DEV
typealias EnvironmentId = String // final computed env id
typealias EnvironmentIdParameter = String // env id passed in by clients

@Scope
@Target(CLASS, FUNCTION, PROPERTY_GETTER)
annotation class AbacusScope

@JsExport
object AbacusFactory {
    fun create(
        deploymentUri: DeploymentUri,
        deployment: Deployment,
        appConfigs: AppConfigsV2,
        ioImplementations: IOImplementations,
        uiImplementations: UIImplementations,
        stateNotification: StateNotificationProtocol? = null,
        dataNotification: DataNotificationProtocol? = null,
        presentationProtocol: PresentationProtocol? = null,
        environmentIdParameter: EnvironmentIdParameter? = null,
    ): AbacusComponent = AbacusComponent::class.create(
        deploymentUri,
        deployment,
        appConfigs,
        ioImplementations,
        uiImplementations,
        stateNotification,
        dataNotification,
        presentationProtocol,
        environmentIdParameter,
    )
}

@JsExport
@AbacusScope
@Component
abstract class AbacusComponent(
    @get:Provides protected val deploymentUri: DeploymentUri,
    @get:Provides protected val deployment: Deployment,
    @get:Provides protected val appConfigs: AppConfigsV2,
    @get:Provides protected val ioImplementations: IOImplementations,
    @get:Provides protected val uiImplementations: UIImplementations,
    @get:Provides protected val stateNotification: StateNotificationProtocol?,
    @get:Provides protected val dataNotification: DataNotificationProtocol?,
    @get:Provides protected val presentationProtocol: PresentationProtocol?,
    @get:Provides protected val environmentIdParameter: EnvironmentIdParameter?,
) {
    abstract val stateManager: AsyncAbacusStateManagerV2
    abstract val documentation: Documentation?

    @Provides protected fun provideV4Environment(environmentLoader: EnvironmentLoader): V4Environment =
        environmentLoader.envAndAppSettings.run { environments.first { it.id == environmentId } }

    @Provides protected fun provideDocumentation(documentationLoader: DocumentationLoader): Documentation? =
        documentationLoader.documentation
}
