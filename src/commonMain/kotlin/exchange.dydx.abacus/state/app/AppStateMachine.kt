package exchange.dydx.abacus.state.app

import co.touchlab.stately.freeze
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.V3PrivateSignerProtocol
import exchange.dydx.abacus.state.app.adaptors.*
import exchange.dydx.abacus.state.app.helper.ChainHelper
import exchange.dydx.abacus.state.app.signer.V3ApiKey
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.modal.*
import exchange.dydx.abacus.utils.IList
import io.ktor.http.*
import kollections.JsExport
import kollections.iListOf
import kollections.iMutableListOf
import kollections.iMutableSetOf
import kollections.toIList
import kotlin.time.ExperimentalTime

internal typealias ApiBlock = (adaptor: ApiAdaptorProtocol) -> AppStateResponse?

@JsExport
enum class AppVersion(val rawValue: String) {
    v3("v3"), v4("v4");

    companion object {
        operator fun invoke(rawValue: String) =
            AppVersion.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
data class IndexerURIs(
    val api: String,
    val socket: String)

@JsExport
data class EnvironmentURIs(
    val indexers: IList<IndexerURIs>?,
    val configs: String?,
    val validators: IList<String>?,
    val faucet: String?,
    val squid: String?,
    val statusPageUrl: String?,
    val marketImageUrl: String?,
    val tosUrl: String?,
    val privacyPolicyUrl: String?,
    val mintscanUrl: String?,
)

@JsExport
open class Environment(
    val environment: String,
    val ethereumChainId: String,
    val dydxChainId: String?,
    val stringKey: String,
    val string: String,
    val isMainNet: Boolean,
    val version: AppVersion,
    val maxSubaccountNumber: Int,
) {
}

@JsExport
class V4Environment(
    environment: String,
    ethereumChainId: String,
    dydxChainId: String?,
    stringKey: String,
    string: String,
    isMainNet: Boolean,
    version: AppVersion,
    maxSubaccountNumber: Int,
    val URIs: EnvironmentURIs,
) : Environment(
    environment,
    ethereumChainId,
    dydxChainId,
    stringKey,
    string,
    isMainNet,
    version,
    maxSubaccountNumber
)

@JsExport
enum class AppPlatform(val rawValue: String) {
    WEB("web"),
    IOS("ios"),
    ANDROID("android");

    companion object {
        operator fun invoke(rawValue: String) =
            AppPlatform.values().firstOrNull { it.rawValue == rawValue }
    }
}

@OptIn(ExperimentalTime::class)
@JsExport
//@Serializable
open class AppStateMachine(private val appPlatform: AppPlatform? = null) : AppStateMachineProtocol,
    V3AppStateMachineProtocol,
    V4AppStateMachineProtocol {
    private val environments: IList<Environment> = iListOf(
        Environment("1", "1", null, "CHAIN.V3_MAINNET", "v3 MainNet", true, AppVersion.v3, 0),
        Environment("5", "5", null, "CHAIN.V3_GOERLI", "v3 Goerli", false, AppVersion.v3, 0),
        Environment(
            "dydxprotocol-staging",
            "5",
            "dydxprotocol-testnet",
            "CHAIN.V4_STAGING",
            "v4 Staging",
            false,
            AppVersion.v4,
            127
        ),
        Environment(
            "dydxprotocol-dev",
            "5",
            "dydxprotocol-testnet",
            "CHAIN.V4_DEVNET",
            "v4 Dev",
            false,
            AppVersion.v4,
            127
        ),
        Environment(
            "dydxprotocol-dev2",
            "5",
            "dydxprotocol-testnet",
            "CHAIN.V4_DEVNET_2",
            "v4 Dev 2",
            false,
            AppVersion.v4,
            127
        ),
        Environment(
            "dydxprotocol-dev3",
            "5",
            "dydxprotocol-testnet",
            "CHAIN.V4_DEVNET_3",
            "v4 Dev 3",
            false,
            AppVersion.v4,
            127
        ),
        Environment(
            "dydxprotocol-dev4",
            "5",
            "dydxprotocol-testnet",
            "CHAIN.V4_DEVNET_4",
            "v4 Dev 4",
            false,
            AppVersion.v4,
            127
        ),
        Environment(
            "dydxprotocol-dev5",
            "5",
            "dydxprotocol-testnet",
            "CHAIN.V4_DEVNET_5",
            "v4 Dev 5",
            false,
            AppVersion.v4,
            127
        ),
        Environment(
            "dydxprotocol-testnet",
            "5",
            "dydxprotocol-testnet",
            "CHAIN.V4_TESTNET",
            "v4 Private Testnet",
            false,
            AppVersion.v4,
            127
        ),
        Environment(
            "dydxprotocol-ibc-testnet",
            "5",
            "dydxprotocol-testnet",
            "CHAIN.V4_IBC_TESTNET",
            "v4 IBC Testnet",
            false,
            AppVersion.v4,
            127
        ),
        Environment(
            "dydxprotocol-testnet-1",
            "5",
            "dydx-testnet-1",
            "CHAIN.V4_TESTNET1",
            "v4 Private Testnet 1",
            false,
            AppVersion.v4,
            127
        )
    )

    override fun localizer(): LocalizerProtocol? {
        return adaptor?.localizer
    }

    override val availableEnvironments: IList<SelectionOption>
        get() = environments.map { environment ->
        SelectionOption(environment.environment, environment.string, null)
    }.toIList()

    private var environmentId: String? = null
        set(newValue) {
            if (field != newValue) {
                field = newValue
                environment = findEnvironment(environmentId)
            }
        }

    private var environment: Environment? = null
        set(newValue) {
            if (field != newValue) {
                field = newValue
                reconnect()
            }
        }

    val chainIds: IList<SelectionOption> = chainIdSelections()

    val ethereumChainId: String? = environment?.ethereumChainId
    val isMainNet: Boolean? = environment?.isMainNet
    val hasFaucet: Boolean = if (isMainNet != null) !isMainNet else false

    private var readyToConnect: Boolean = false
    internal var adaptor: ApiAdaptor? = null

    private var lastApiState: ApiState? = null
    private var lastOrder: SubaccountOrder? = null

    private fun chainIdSelections(): IList<SelectionOption> {
        val selections = iMutableListOf<SelectionOption>()
        val chainIds = iMutableSetOf<String>()
        for (environment in environments) {
            val chainId = environment.dydxChainId ?: environment.ethereumChainId
            if (!chainIds.contains(chainId)) {
                selections.add(SelectionOption(chainId, environment.stringKey, null))
                chainIds.add(chainId)
            }
        }
        return selections
    }

    private fun reconnect() {
        val environment = environment
        adaptor = when (environment?.version) {
            AppVersion.v4 -> {
                V4ApiAdaptor(
                    environment.environment,
                    environment.dydxChainId!!,
                    environment.isMainNet,
                    environment.maxSubaccountNumber,
                    appPlatform
                )
            }

            AppVersion.v3 -> {
                V3ApiAdaptor(
                    environment.environment,
                    environment.isMainNet,
                    environment.maxSubaccountNumber,
                    appPlatform
                )
            }

            else -> {
                null
            }
        }
    }

    private fun findEnvironment(environment: String?): Environment? {
        return environments.firstOrNull { it ->
            it.environment == environment
        }
    }

    override fun currentEnvironment(): Environment? = environment

    internal fun result(response: AppStateResponse?): AppStateResponse {
        return response?.freeze() ?: noChange().freeze()
    }

    private fun noChange(): AppStateResponse {
        return AppStateResponse(
            adaptor?.stateMachine?.state,
            null,
            null,
            null,
            lastApiState,
            lastOrder
        )
    }

    private fun allChanges(): AppStateResponse {
        return AppStateResponse(
            adaptor?.stateMachine?.state,
            StateChanges(adaptor?.allStates() ?: iListOf()),
            null,
            null,
            lastApiState,
            updatedLastOrder()
        )
    }

    private fun updatedLastOrder(): SubaccountOrder? {
        return null
    }

    override fun setEnvironment(environment: String?): AppStateResponse {
        this.environmentId = environment
        val response = adaptor?.setReadyToConnect(readyToConnect)
        lastApiState = null
        lastOrder = null
        return result(
            AppStateResponse(
                response?.state,
                StateChanges(adaptor?.allStates() ?: iListOf()),
                response?.errors,
                response?.networkRequests,
                lastApiState,
                lastOrder
            )
        )
    }

    override fun setChainId(chainId: String): AppStateResponse {
        return setEnvironment(environment(chainId))
    }

    private fun environment(chainId: String?): String? {
        return environments.firstOrNull {
            it.dydxChainId == chainId || it.ethereumChainId == chainId
        }?.environment
    }

    override fun associatedEthereumChainId(): String? {
        return environment?.ethereumChainId
    }

    override fun setReadyToConnect(readyToConnect: Boolean): AppStateResponse {
        return if (this.readyToConnect != readyToConnect) {
            this.readyToConnect = readyToConnect
            result(adaptor?.setReadyToConnect(readyToConnect))
        } else result(null)
    }

    override fun accountAddress(): String? {
        return adaptor?.accountAddress()
    }

    override fun subaccountNumber(): Int {
        return adaptor?.subaccountNumber() ?: 0
    }

    override fun market(): String? {
        return adaptor?.market()
    }

    override fun setMarket(market: String?): AppStateResponse {
        return result(adaptor?.setMarket(market))
    }

    override fun orderbookGrouping(): OrderbookGrouping {
        return adaptor?.orderbookGrouping() ?: OrderbookGrouping.none
    }

    override fun setOrderbookGrouping(orderbookGrouping: OrderbookGrouping): AppStateResponse {
        return result(adaptor?.setOrderbookGrouping(orderbookGrouping))
    }

    override fun historicalPnlPeriod(): HistoricalPnlPeriod {
        return adaptor?.historicalPnlPeriod() ?: HistoricalPnlPeriod.Period1d
    }

    override fun setHistoricalPnlPeriod(historicalPnlPeriod: HistoricalPnlPeriod): AppStateResponse {
        return result(adaptor?.setHistoricalPnlPeriod(historicalPnlPeriod))
    }

    override fun updateHistoricalPnl(): AppStateResponse {
        return result(adaptor?.updateHistoricalPnl())
    }

    override fun candlesResolution(): String {
        return adaptor?.candlesResolution() ?: ""
    }

    override fun setCandlesResolution(candlesResolution: String): AppStateResponse {
        return result(adaptor?.setCandlesResolution(candlesResolution))
    }

    override fun setSocketConnected(url: AbUrl, socketConnected: Boolean): AppStateResponse {
        return result(adaptor?.setSocketConnected(url, socketConnected))
    }

    override fun processSocketResponse(url: AbUrl, text: String): AppStateResponse {
        return result(adaptor?.processSocketResponse(url, text, lastApiState?.height))
    }

    override fun processHttpResponse(url: AbUrl, text: String): AppStateResponse {
        return result(adaptor?.processHttpResponse(url, text, lastApiState?.height))
    }

    override fun trade(data: String?, type: TradeInputField?): AppStateResponse {
        return result(adaptor?.trade(data, type))
    }

    override fun closePosition(data: String?, type: ClosePositionInputField): AppStateResponse {
        return result(adaptor?.closePosition(data, type))
    }

    override fun transfer(data: String?, type: TransferInputField?): AppStateResponse {
        return result(adaptor?.transfer(data, type))
    }

    override fun commit(): AppStateResponse {
        return result(adaptor?.commit())
    }

    override fun faucet(amount: Int): AppStateResponse {
        return result(adaptor?.faucet(amount))
    }

    override fun ping(): AppStateResponse {
        return result(adaptor?.ping())
    }


    // v3

    override fun signer(): V3PrivateSignerProtocol? {
        return (adaptor as? V3ApiAdaptorProtocol)?.signer
    }

    override fun setSigner(signer: V3PrivateSignerProtocol?) {
        (adaptor as? V3ApiAdaptorProtocol)?.signer = signer
    }

    override fun ethereumAddress(): String? = (adaptor as? V3ApiAdaptorProtocol)?.ethereumAddress()

    override fun setWalletEthereumAddress(
        ethereumAddress: String?,
        apiKey: V3ApiKey?,
    ): AppStateResponse {
        val stateResponse = adaptor?.stateMachine?.resetWallet(ethereumAddress)
        val response =
            (adaptor as? V3ApiAdaptorProtocol)?.setWalletEthereumAddress(ethereumAddress, apiKey)
        return result(
            AppStateResponse(
                stateResponse?.state,
                stateResponse?.changes,
                stateResponse?.errors,
                response?.networkRequests,
                lastApiState,
                lastOrder
            )
        )
    }

    // v4

    override val chainHelper: ChainHelper?
        get() = (adaptor as? V4ApiAdaptorProtocol)?.chainHelper

    override fun cosmoAddress(): String? = (adaptor as? V4ApiAdaptorProtocol)?.cosmoAddress()

    override fun setWalletCosmoAddress(cosmoAddress: String?): AppStateResponse {
        return if (this.cosmoAddress() != cosmoAddress) {
            result((adaptor as? V4ApiAdaptorProtocol)?.setWalletCosmoAddress(cosmoAddress))
        } else result(noChange())
    }

    override fun setSubaccountNumber(subaccountNumber: Int): AppStateResponse {
        return result((adaptor as? V4ApiAdaptorProtocol)?.setSubaccountNumber(subaccountNumber))
    }

    override fun transaction(signedTransaction: String): AppStateResponse {
        return result((adaptor as? V4ApiAdaptorProtocol)?.transaction(signedTransaction))
    }

    override fun placeOrderPayload(): V4SubaccountPlaceOrderPayload? {
        return try {
            ((adaptor as? V4ApiAdaptorProtocol)?.placeOrderPayload())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun closePositionPayload(): V4SubaccountPlaceOrderPayload? {
        return try {
            ((adaptor as? V4ApiAdaptorProtocol)?.closePositionPayload())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun cancelOrderPayload(orderId: String): V4SubaccountCancelOrderPayload? {
        return try {
            ((adaptor as? V4ApiAdaptorProtocol)?.cancelOrderPayload(orderId))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun placeOrderPayload2(): V4SubaccountPlaceOrderPayload2? {
        return try {
            ((adaptor as? V4ApiAdaptorProtocol)?.placeOrderPayload2())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun closePositionPayload2(): V4SubaccountPlaceOrderPayload2? {
        return try {
            ((adaptor as? V4ApiAdaptorProtocol)?.closePositionPayload2())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun cancelOrderPayload2(orderId: String): V4SubaccountCancelOrderPayload2? {
        return try {
            ((adaptor as? V4ApiAdaptorProtocol)?.cancelOrderPayload2(orderId))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun transferStatus(hash: String): AppStateResponse {
        return result((adaptor as? V4ApiAdaptorProtocol)?.transferStatus(hash))
    }

    override fun parseOnChainEquityTiers(payload: String): AppStateResponse {
        return result((adaptor as? V4ApiAdaptorProtocol)?.parseOnChainEquityTiers(payload))
    }

    override fun parseOnChainFeeTiers(payload: String): AppStateResponse {
        return result((adaptor as? V4ApiAdaptorProtocol)?.parseOnChainFeeTiers(payload))
    }

    override fun parseOnChainUserFeeTier(payload: String): AppStateResponse {
        return result((adaptor as? V4ApiAdaptorProtocol)?.parseOnChainUserFeeTier(payload))
    }

    override fun parseOnChainUserStats(payload: String): AppStateResponse {
        return result((adaptor as? V4ApiAdaptorProtocol)?.parseOnChainUserStats(payload))
    }
}
