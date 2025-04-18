package exchange.dydx.abacus.app.manager

import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.protocols.DYDXChainTransactionsProtocol
import exchange.dydx.abacus.protocols.FileLocation
import exchange.dydx.abacus.protocols.FileSystemProtocol
import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.protocols.QueryType
import exchange.dydx.abacus.protocols.RestCallback
import exchange.dydx.abacus.protocols.RestProtocol
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.protocols.ThreadingProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TimerProtocol
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.protocols.WebSocketProtocol
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.StateChanges
import exchange.dydx.abacus.state.manager.ApiState
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Parser
import kollections.iMutableListOf

class TestFileSystem : FileSystemProtocol {
    private val mock = AbacusMockData()
    var cachedFiles = mutableMapOf<String, String>()

    override fun readTextFile(
        location: FileLocation,
        path: String,
    ): String? {
        if (path.contains("env.json")) {
            return mock.environments.environments
        }
        when (location) {
            FileLocation.AppBundle -> {
                when (path) {
                    "/config/localization/en/app.json",
                    "/config/localization/zh/app.json",
                    -> {
                        return """
                            {
                                "APP": "app"
                            }
                        """.trimIndent()
                    }

                    "/config/localizations_native/en/app.json",
                    "/config/localizations_native/zh/app.json",
                    -> {
                        return """
                            {
                                "TOOLTIP": "tooltip"
                            }
                        """.trimIndent()
                    }

                    "/config/localization/languages.json",
                    -> {
                        return """
                            [
                                {
                                    "code": "en",
                                    "name": "English"
                                },
                                {
                                    "code": "fr",
                                    "name": "Français"
                                }
                            ]
                        """.trimIndent()
                    }

                    else -> {
                        return null
                    }
                }
            }

            FileLocation.AppDocs -> {
                when (path) {
                    "/config/localization/en/app.json",
                    "/config/localization/zh/app.json",
                    -> {
                        return """
                            {
                                "APP": "app-doc"
                            }
                        """.trimIndent()
                    }

                    "/config/localizations_native/en/app.json",
                    "/config/localizations_native/zh/app.json",
                    -> {
                        return """
                            {
                                "TOOLTIP": "tooltip-doc"
                            }
                        """.trimIndent()
                    }

                    else -> {
                        return null
                    }
                }
            }
        }
    }

    override fun writeTextFile(path: String, text: String): Boolean {
        return true
    }
}

class TestRest() : RestProtocol {
    private val mock = AbacusMockData()
    private var responses = mutableMapOf<String, String>()
    private val parser = Parser()

    var requests = iMutableListOf<String>()

    init {
        setResponse(
            "https://api.examples.com/configs/markets.json",
            mock.marketsConfigurations.configurations,
        )
        setResponse(
            "https://dydx-v4-shared-resources.vercel.app/config/localization/languages.json",
            """
                [
                    {
                        "code": "en",
                        "name": "English"
                    },
                    {
                        "code": "fr",
                        "name": "Français"
                    },
                    {
                        "code": "de",
                        "name": "Deutsch"
                    },
                    {
                        "code": "ja",
                        "name": "日本語"
                    },
                    {
                        "code": "ko",
                        "name": "한국어"
                    },
                    {
                        "code": "pt",
                        "name": "Português"
                    },
                    {
                        "code": "ru",
                        "name": "Русский"
                    },
                    {
                        "code": "zh",
                        "name": "简体中文",
                        "path": "zh-CN"
                    },
                    {
                        "code": "es",
                        "name": "Español"
                    },
                    {
                        "code": "tr",
                        "name": "Türkçe"
                    }
                ]
            """.trimIndent(),
        )
        setResponse(
            "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
            """
                {
                   "data":{
                      "tradingSeasons":[
                         {
                            "startTimestamp":1701177710,
                            "label":"1",
                            "__typename":"TradingSeason"
                         },
                         {
                            "startTimestamp":1704384000,
                            "label":"2",
                            "__typename":"TradingSeason"
                         }
                      ]
                   }
                }
            """.trimIndent(),
        )
    }

    override fun get(
        url: String,
        headers: IMap<String, String>?,
        callback: RestCallback,
    ) {
        if (url.contains("env.json")) {
            callback(mock.environments.environments, 200, null)
            return
        }
        if (!url.contains("localization")) {
            requests.add(url)
        }
        val response = responses[url]
        if (response != null) {
            responses.remove(url)
            val code = parser.asInt(response)
            if (code != null) {
                callback(null, code, null)
            } else {
                callback(response, 200, null)
            }
        } else {
            callback(null, 404, null)
        }
    }

    override fun post(
        url: String,
        headers: IMap<String, String>?,
        body: String?,
        callback: RestCallback,
    ) {
        requests.add(url)

        val response = responses[url]
        if (response != null) {
            responses.remove(url)
            val code = parser.asInt(response)
            if (code != null) {
                callback(null, code, null)
            } else {
                callback(response, 200, null)
            }
        } else {
            callback(null, 404, null)
        }
    }

    override fun put(
        url: String,
        headers: IMap<String, String>?,
        body: String?,
        callback: RestCallback,
    ) {
        requests.add(url)
    }

    override fun delete(
        url: String,
        headers: IMap<String, String>?,
        callback: RestCallback,
    ) {
        requests.add(url)
    }

    fun setResponse(url: String, response: String) {
        responses[url] = response
    }
}

class TestWebSocket : WebSocketProtocol {
    var connectUrl: String? = null
    var messages = iMutableListOf<String>()

    var connected: ((result: Boolean) -> Unit)? = null
    var received: ((message: String) -> Unit)? = null

    override fun connect(
        url: String,
        connected: (result: Boolean) -> Unit,
        received: (message: String) -> Unit,
    ) {
        connectUrl = url
        this.connected = connected
        this.received = received
    }

    override fun disconnect() {
    }

    override fun send(message: String) {
        messages.add(message)
    }

    fun simulateConnected(connected: Boolean) {
        this.connected?.invoke(connected)
        this.received?.invoke(
            """
            {"type":"connected","connection_id":"c98ace50-5f67-4ed8-8096-de0c694eeb1d","message_id":0}
            """.trimIndent(),
        )
    }

    fun simulateReceived(message: String) {
        this.received?.invoke(message)
    }
}

class TestChain : DYDXChainTransactionsProtocol {
    var heightResponse: String? = null
    var placeOrderResponse: String? = null
    var cancelOrderResponse: String? = null
    var transferResponse: String? = null
    var depositResponse: String? = null
    var withdrawResponse: String? = null
    var signCompliancePayload: String? = null

    var transactionCallback: ((response: String?) -> Unit)? = null

    var placeOrderPayloads = mutableListOf<String>()
    var canceldOrderPayloads = mutableListOf<String>()
    var transferPayloads = mutableListOf<String>()

    var requests = mutableListOf<QueryType>()

    val dummySuccess = """
        {
            "success": true
        }
    """.trimIndent()

    val dummyError = """
        {
            "error": {
                "code": 100,
                "message": "dummy error"
            }
        }
    """.trimIndent()

    override fun connectNetwork(
        paramsInJson: String,
        callback: (response: String?) -> Unit,
    ) {
        callback(dummySuccess)
    }

    override fun get(
        type: QueryType,
        paramsInJson: String?,
        callback: (response: String?) -> Unit
    ) {
        requests.add(type)
        when (type) {
            QueryType.Height -> {
                getHeight(callback)
            }

            else -> {}
        }
    }

    override fun transaction(
        type: TransactionType,
        paramsInJson: String?,
        callback: (response: String?) -> Unit
    ) {
        when (type) {
            TransactionType.PlaceOrder -> {
                placeOrder(paramsInJson!!, callback)
            }

            TransactionType.CancelOrder -> {
                cancelOrder(paramsInJson!!, callback)
            }

            TransactionType.SubaccountTransfer -> {
                transfer(paramsInJson!!, callback)
            }

            TransactionType.Deposit -> {
                deposit(paramsInJson!!, callback)
            }

            TransactionType.Withdraw -> {
                withdraw(paramsInJson!!, callback)
            }

            TransactionType.SignCompliancePayload -> {
                signCompliancePayload(paramsInJson!!, callback)
            }

            else -> {}
        }
    }

    fun signCompliancePayload(json: String, callback: (response: String?) -> Unit) {
        if (signCompliancePayload != null) {
            callback(signCompliancePayload)
        } else {
            callback(dummyError)
        }
    }

    fun getHeight(callback: (response: String?) -> Unit) {
        if (heightResponse != null) {
            callback(heightResponse)
        } else {
            callback(dummyError)
        }
    }

    fun placeOrder(json: String, callback: (response: String?) -> Unit) {
        placeOrderPayloads.add(json)
        if (placeOrderResponse != null) {
            callback(placeOrderResponse)
        } else {
            this.transactionCallback = callback
        }
    }

    fun cancelOrder(json: String, callback: (response: String?) -> Unit) {
        canceldOrderPayloads.add(json)
        if (cancelOrderResponse != null) {
            callback(cancelOrderResponse)
        } else {
            this.transactionCallback = callback
        }
    }

    fun transfer(json: String, callback: (response: String?) -> Unit) {
        transferPayloads.add(json)
        if (transferResponse != null) {
            callback(transferResponse)
        } else {
            this.transactionCallback = callback
        }
    }

    fun deposit(json: String, callback: (response: String?) -> Unit) {
        if (depositResponse != null) {
            callback(depositResponse)
        } else {
            this.transactionCallback = callback
        }
    }

    fun withdraw(json: String, callback: (response: String?) -> Unit) {
        if (withdrawResponse != null) {
            callback(withdrawResponse)
        } else {
            this.transactionCallback = callback
        }
    }

    fun simulateTransactionResponse(response: String) {
        this.transactionCallback?.invoke(response)
    }
}

class TestThreading : ThreadingProtocol {
    override fun async(type: ThreadingType, block: () -> Unit) {
        block()
    }
}

class TestLocalTimer : LocalTimerProtocol {
    override fun cancel() {
    }
}

class TestTimer : TimerProtocol {
    override fun schedule(
        delay: Double,
        repeat: Double?,
        block: () -> Boolean
    ): LocalTimerProtocol {
        if (delay == 0.0) {
            block()
        }
        return TestLocalTimer()
    }
}

class TestState : StateNotificationProtocol {
    var state: PerpetualState? = null
    var apiState: ApiState? = null

    override fun environmentsChanged() {
    }

    override fun stateChanged(state: PerpetualState?, changes: StateChanges?) {
        this.state = state
    }

    override fun apiStateChanged(apiState: ApiState?) {
        this.apiState = apiState
    }

    override fun errorsEmitted(errors: IList<ParsingError>) {
    }

    override fun lastOrderChanged(order: SubaccountOrder?) {
    }

    override fun notificationsChanged(notifications: IList<Notification>) {
    }
}
