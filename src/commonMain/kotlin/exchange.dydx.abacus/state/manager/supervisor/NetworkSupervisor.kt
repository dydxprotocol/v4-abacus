package exchange.dydx.abacus.state.manager.supervisor

import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.model.TradingStateMachine

internal open class NetworkSupervisor(
    internal val stateMachine: TradingStateMachine,
    internal val helper: NetworkHelper,
) {
    internal var retainerCount: Int = 1
        set(value) {
            if (field != value) {
                field = value
                didSetRetainerCount(value)
            }
        }

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

    private fun didSetRetainerCount(retainerCount: Int) {
        if (retainerCount == 0) {
            readyToConnect = false
            socketConnected = false
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
                realChanges = stateMachine.update(it)
            }
            if (realChanges != null) {
                helper.ioImplementations.threading?.async(ThreadingType.main) {
                    helper.updateStateChanges(stateMachine, realChanges, oldState)
                }
//                updateTracking(changes = realChanges!!)
//                updateNotifications()
            }
        }
    }
}
