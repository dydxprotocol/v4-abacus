package exchange.dydx.abacus.state.v2.supervisor

import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.filterNotNull
import kollections.iListOf

internal open class NetworkSupervisor(
    internal val stateMachine: TradingStateMachine,
    internal val helper: NetworkHelper,
    internal val analyticsUtils: AnalyticsUtils,
) {
    internal var readyToConnect: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                didSetReadyToConnect(field)
            }
        }

    internal var indexerConnected: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                didSetIndexerConnected(indexerConnected)
            }
        }

    internal var socketConnected: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                didSetSocketConnected(socketConnected)
            }
        }

    internal var validatorConnected: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                didSetValidatorConnected(validatorConnected)
            }
        }

    internal open fun didSetReadyToConnect(readyToConnect: Boolean) {
    }

    internal open fun didSetIndexerConnected(indexerConnected: Boolean) {
    }

    internal open fun didSetSocketConnected(socketConnected: Boolean) {
    }

    internal open fun didSetValidatorConnected(validatorConnected: Boolean) {
    }

    internal open fun dispose() {
        socketConnected = false
        validatorConnected = false
        indexerConnected = false
        readyToConnect = false
    }

    internal fun update(changes: StateChanges?, oldState: PerpetualState?) {
        if (changes != null) {
            var realChanges = changes
            changes.let {
                realChanges = stateMachine.updateStateChanges(it)
            }
            if (realChanges != null) {
                helper.ioImplementations.threading?.async(ThreadingType.main) {
                    helper.updateStateChanges(stateMachine, realChanges, oldState)
                }
                updateTracking(changes = realChanges!!)
                updateNotifications()
            }
        }
    }

    internal fun tracking(eventName: String, params: IMap<String, Any?>?) {
        val requiredParams = helper.apiStateParams()
        val mergedParams = params?.let { ParsingHelper.merge(params.filterNotNull(), requiredParams) } ?: requiredParams
        val paramsAsString = helper.jsonEncoder.encode(mergedParams)
        helper.ioImplementations.threading?.async(ThreadingType.main) {
            helper.ioImplementations.tracking?.log(eventName, paramsAsString)
        }
    }

    internal open fun updateTracking(changes: StateChanges) {
    }

    internal open fun updateNotifications() {
    }

    internal fun emitError(error: ParsingError) {
        helper.ioImplementations.threading?.async(ThreadingType.main) {
            helper.stateNotification?.errorsEmitted(iListOf(error))
            helper.dataNotification?.errorsEmitted(iListOf(error))
        }
    }
}

internal open class DynamicNetworkSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    analyticsUtils: AnalyticsUtils,
) : NetworkSupervisor(stateMachine, helper, analyticsUtils) {

    internal var retainCount: Int = 1
        set(value) {
            if (field != value) {
                field = value
                didSetRetainCount()
            }
        }

    internal fun retain() {
        retainCount++
    }

    internal fun release() {
        retainCount--
    }

    internal fun forceRelease() {
        retainCount = 0
    }

    internal open fun didSetRetainCount() {
        if (retainCount == 0) {
            readyToConnect = false
            indexerConnected = false
            validatorConnected = false
            socketConnected = false
        }
    }
}
