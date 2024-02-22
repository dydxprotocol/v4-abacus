package exchange.dydx.abacus.state.manager.supervisor

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.manager.SubaccountConfigs
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.historicalPnl
import exchange.dydx.abacus.state.model.receivedFills
import exchange.dydx.abacus.state.model.receivedTransfers
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import kollections.toIMap
import kotlin.time.Duration.Companion.days

internal class SubaccountSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    private val configs: SubaccountConfigs,
    private val accountAddress: String,
    private val subaccountNumber: Int
) : NetworkSupervisor(stateMachine, helper) {
    override fun didSetIndexerConnected(indexerConnected: Boolean) {
        super.didSetIndexerConnected(indexerConnected)
        if (indexerConnected) {
            if (configs.retrieveFills) {
                retrieveFills()
            }
            if (configs.retrieveTransfers) {
                retrieveTransfers()
            }
            if (configs.retrieveHistoricalPnls) {
                retrieveHistoricalPnls()
            }
        }
    }

    override fun didSetSocketConnected(socketConnected: Boolean) {
        if (configs.subscribeToSubaccount) {
            subaccountChannelSubscription(socketConnected)
        }
    }

    private fun retrieveFills() {
        val oldState = stateMachine.state
        val url = helper.configs.privateApiUrl("fills")
        val params = subaccountParams()
        if (url != null && params != null) {
            helper.get(url, params, null, callback = { _, response, httpCode ->
                if (helper.success(httpCode) && response != null) {
                    val fills = helper.parser.decodeJsonObject(response)?.toIMap()
                    if (fills != null && fills.size != 0) {
                        update(stateMachine.receivedFills(fills, subaccountNumber), oldState)
                    }
                }
            })
        }
    }

    private fun subaccountParams(): IMap<String, String>? {
        val accountAddress = accountAddress
        val subaccountNumber = subaccountNumber
        return if (accountAddress != null) iMapOf(
            "address" to accountAddress,
            "subaccountNumber" to "$subaccountNumber",
        );
        else null
    }

    private fun retrieveTransfers() {
        val oldState = stateMachine.state
        val url = helper.configs.privateApiUrl("transfers")
        val params = subaccountParams()
        if (url != null && params != null) {
            helper.get(url, params, null, callback = { _, response, httpCode ->
                if (helper.success(httpCode) && response != null) {
                    val tranfers = helper.parser.decodeJsonObject(response)
                    if (tranfers != null && tranfers.size != 0) {
                        update(stateMachine.receivedTransfers(tranfers, subaccountNumber), oldState)
                    }
                }
            })
        }
    }


    private fun retrieveHistoricalPnls(previousUrl: String? = null) {
        val url = helper.configs.privateApiUrl("historical-pnl") ?: return
        val params = subaccountParams()
        val historicalPnl = helper.parser.asNativeList(
            helper.parser.value(
                stateMachine.data,
                "wallet.account.subaccounts.$subaccountNumber.historicalPnl"
            )
        )?.mutable()

        if (historicalPnl != null) {
            val last = helper.parser.asMap(historicalPnl.lastOrNull())
            if (helper.parser.asBool(last?.get("calculated")) == true) {
                historicalPnl.removeLast()
            }
        }

        helper.retrieveTimed(
            url,
            historicalPnl,
            "createdAt",
            1.days,
            180.days,
            "createdBeforeOrAt",
            "createdAtOrAfter",
            params,
            previousUrl
        ) { url, response, httpCode ->
            val oldState = stateMachine.state
            if (helper.success(httpCode) && !response.isNullOrEmpty()) {
                val changes = stateMachine.historicalPnl(
                    payload = response,
                    subaccountNumber = subaccountNumber
                )
                update(changes, oldState)
                if (changes.changes.contains(Changes.historicalPnl)) {
                    retrieveHistoricalPnls(url)
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun subaccountChannelSubscription(subscribe: Boolean = true,
    ) {
        val channel =
            helper.configs.subaccountChannel() ?: throw Exception("subaccount channel is null")
        helper.socket(
            helper.socketAction(subscribe),
            channel,
            subaccountChannelParams(accountAddress, subaccountNumber)
        )
    }

    private fun subaccountChannelParams(
        accountAddress: String,
        subaccountNumber: Int,
    ): IMap<String, Any> {
        return iMapOf("id" to "$accountAddress/$subaccountNumber")
    }


}
