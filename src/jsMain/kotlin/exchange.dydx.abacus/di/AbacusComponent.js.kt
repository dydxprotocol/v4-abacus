package exchange.dydx.abacus.di

import exchange.dydx.abacus.protocols.DataNotificationProtocol
import exchange.dydx.abacus.protocols.PresentationProtocol
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.state.v2.supervisor.AppConfigsV2
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.UIImplementations

internal actual fun createAbacusComponent(
    deploymentUri: DeploymentUri,
    deployment: Deployment,
    appConfigs: AppConfigsV2,
    ioImplementations: IOImplementations,
    uiImplementations: UIImplementations,
    stateNotification: StateNotificationProtocol?,
    dataNotification: DataNotificationProtocol?,
    presentationProtocol: PresentationProtocol?,
): AbacusComponent = AbacusComponent::class.create(
    deploymentUri,
    deployment,
    appConfigs,
    ioImplementations,
    uiImplementations,
    stateNotification,
    dataNotification,
    presentationProtocol,
)
