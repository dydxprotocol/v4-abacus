package exchange.dydx.abacus.state.supervisor

import exchange.dydx.abacus.output.Compliance
import exchange.dydx.abacus.output.ComplianceAction
import exchange.dydx.abacus.output.ComplianceStatus
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.output.UsageRestriction
import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import exchange.dydx.abacus.state.machine.TradingStateMachine
import exchange.dydx.abacus.state.manager.utils.Address
import exchange.dydx.abacus.state.manager.utils.DydxAddress
import exchange.dydx.abacus.state.manager.utils.EvmAddress
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.toJsonPrettyPrint
import kollections.iListOf
import kollections.iSetOf
import kollections.toIMap
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

internal class AccountScreener(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    analyticsUtils: AnalyticsUtils,
    private val screeningUpdate: Boolean, // do compliance screening and update user compliance status
    internal val accountAddress: String,
    private val complianceUpdated: () -> Unit,
) : DynamicNetworkSupervisor(stateMachine, helper, analyticsUtils) {

    private var sourceAddressRestriction: Restriction? = null
        set(value) {
            if (field != value) {
                field = value
                didSetSourceAddressRestriction(value)
            }
        }

    internal var addressRestriction: UsageRestriction? = null
        set(value) {
            if (field != value) {
                field = value
                didSetAddressRestriction(value)
            }
        }

    internal var restriction: UsageRestriction = UsageRestriction.noRestriction
        set(value) {
            if (field != value) {
                field = value
                didSetRestriction(value)
            }
        }

    private var compliance: Compliance = Compliance(null, ComplianceStatus.COMPLIANT, null, null)
        set(value) {
            if (field != value) {
                field = value
                didSetComplianceStatus(value)
            }
        }

    private var screenAccountAddressTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    private var accountAddressRestriction: Restriction? = null
        set(value) {
            if (field != value) {
                field = value
                didSetAccountAddressRestriction(value)
            }
        }

    private val addressRetryDuration = 10.0
    private val addressContinuousMonitoringDuration = 60.0 * 60.0

    private var screenSourceAddressTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    var sourceAddress: String? = null
        internal set(value) {
            if (field != value) {
                val oldValue = field
                field = value
                didSetSourceAddress(sourceAddress, oldValue)
            }
        }

    init {
        screenAccountAddress()
        if (screeningUpdate) {
            complianceScreen(DydxAddress(accountAddress), ComplianceAction.CONNECT)
        }
    }

    override fun didSetIndexerConnected(indexerConnected: Boolean) {
        super.didSetIndexerConnected(indexerConnected)

        if (indexerConnected) {
            doComplianceScreening()
        } else {
            screenAccountAddressTimer = null
            screenSourceAddressTimer = null
        }
    }

    internal fun screen(address: String, callback: ((Restriction) -> Unit)) {
        val url = screenUrl() ?: return

        helper.get(
            url,
            mapOf("address" to address),
            null,
            callback = { _, response, httpCode, _ ->
                if (helper.success(httpCode) && response != null) {
                    val payload = helper.parser.decodeJsonObject(response)?.toIMap()
                    if (payload != null) {
                        val restricted =
                            helper.parser.asBool(payload["restricted"]) ?: false
                        callback(
                            if (restricted) {
                                Restriction.USER_RESTRICTED
                            } else {
                                Restriction.NO_RESTRICTION
                            },
                        )
                    } else {
                        callback(Restriction.USER_RESTRICTION_UNKNOWN)
                    }
                } else {
                    if (httpCode == 403) {
                        // It could be 403 due to GEOBLOCKED
                        val usageRestriction = restrictionReason(response)
                        callback(usageRestriction.restriction)
                    } else {
                        callback(Restriction.USER_RESTRICTION_UNKNOWN)
                    }
                }
            },
        )
    }

    private fun handleComplianceResponse(response: String?, httpCode: Int, address: Address?): ComplianceStatus {
        var complianceStatus = ComplianceStatus.UNKNOWN
        var updatedAt: String? = null
        var expiresAt: String? = null
        if (helper.success(httpCode) && response != null) {
            val res = helper.parser.decodeJsonObject(response)?.toIMap()
            complianceStatus =
                helper.parser.asString(res?.get("status"))?.let { ComplianceStatus.valueOf(it) }
                    ?: ComplianceStatus.UNKNOWN
            updatedAt = helper.parser.asString(res?.get("updatedAt"))
            if (updatedAt != null) {
                expiresAt =
                    try {
                        Instant.parse(updatedAt).plus(7.days).toString()
                    } catch (e: IllegalArgumentException) {
                        Logger.e { "Error parsing compliance updatedAt: $updatedAt" }
                        null
                    }
            }
        }
        // If we are screening an EVM address we only update when the compliance status is blocked
        if (address is DydxAddress || complianceStatus == ComplianceStatus.BLOCKED) {
            compliance =
                compliance.copy(
                    status = complianceStatus,
                    updatedAt = updatedAt,
                    expiresAt = expiresAt,
                )
        }
        return complianceStatus
    }

    private fun updateCompliance(
        address: DydxAddress,
        status: ComplianceStatus,
        complianceAction: ComplianceAction
    ) {
        val chainId = helper.environment.dydxChainId
        val message = "Verify account ownership"
        val payload =
            helper.jsonEncoder.encode(
                mapOf(
                    "message" to message,
                    "action" to complianceAction.toString(),
                    "status" to status.toString(),
                    "chainId" to chainId.toString(),
                ),
            )
        helper.transaction(
            TransactionType.SignCompliancePayload,
            payload,
        ) { additionalPayload ->
            val error = helper.parseTransactionResponse(additionalPayload)
            val result = helper.parser.decodeJsonObject(additionalPayload)

            if (error == null && result != null) {
                val signedMessage = helper.parser.asString(result["signedMessage"])
                val publicKey = helper.parser.asString(result["publicKey"])
                val timestamp = helper.parser.asString(result["timestamp"])
                val isKeplr = helper.parser.asBool(result["isKeplr"])
                val url = if (isKeplr == true) complianceGeoblockKeplrUrl() else complianceGeoblockUrl()

                val isUrlAndKeysPresent =
                    url != null &&
                        signedMessage != null &&
                        publicKey != null

                val isKeplrOrHasTimestamp = (timestamp != null || isKeplr == true)

                val isStatusValid = status != ComplianceStatus.UNKNOWN

                if (isUrlAndKeysPresent && isKeplrOrHasTimestamp && isStatusValid) {
                    val body: Map<String, String> =
                        if (isKeplr == true) {
                            iMapOf(
                                "address" to address.rawAddress,
                                "message" to message,
                                "action" to complianceAction.toString(),
                                "signedMessage" to signedMessage!!,
                                "pubkey" to publicKey!!,
                            )
                        } else {
                            iMapOf(
                                "address" to address.rawAddress,
                                "message" to message,
                                "currentStatus" to status.toString(),
                                "action" to complianceAction.toString(),
                                "signedMessage" to signedMessage!!,
                                "pubkey" to publicKey!!,
                                "timestamp" to timestamp!!,
                            )
                        }
                    val header =
                        iMapOf(
                            "Content-Type" to "application/json",
                        )
                    helper.post(
                        url = url!!,
                        headers = header,
                        body = body.toJsonPrettyPrint(),
                        callback = { _, response, httpCode, _ ->
                            handleComplianceResponse(response, httpCode, address)
                            // retrieve the subaccounts if it does not exist yet. It is possible that the initial
                            // subaccount retrieval failed due to 403 before updating the compliance status.
                            if (helper.success(httpCode) && response != null) {
                                complianceUpdated()
                            }
                        },
                    )
                } else {
                    compliance = compliance.copy(status = ComplianceStatus.UNKNOWN)
                }
            } else {
                compliance = compliance.copy(status = ComplianceStatus.UNKNOWN)
            }
        }
    }

    private fun complianceScreen(address: Address, action: ComplianceAction? = null) {
        val url = complianceScreenUrl(address.rawAddress) ?: return

        helper.get(
            url = url,
            params = null,
            headers = null,
            callback = { _, response, httpCode, _ ->
                val complianceStatus = handleComplianceResponse(response, httpCode, address)
                if (address is DydxAddress && action != null) {
                    updateCompliance(address, complianceStatus, action)
                }
            },
        )
    }

    private fun complianceScreenUrl(address: String): String? {
        val url = helper.configs.publicApiUrl("complianceScreen") ?: return null
        return "$url/$address"
    }

    private fun complianceGeoblockUrl(): String? {
        return helper.configs.publicApiUrl("complianceGeoblock")
    }

    private fun complianceGeoblockKeplrUrl(): String? {
        return helper.configs.publicApiUrl("complianceGeoblockKeplr")
    }

    private fun screenSourceAddress() {
        val address = sourceAddress
        if (address != null) {
            screen(address) { restriction ->
                when (restriction) {
                    Restriction.USER_RESTRICTED,
                    Restriction.NO_RESTRICTION,
                    Restriction.USER_RESTRICTION_UNKNOWN -> {
                        sourceAddressRestriction = restriction
                    }
                    else -> {
                        throw IllegalArgumentException("Unexpected restriction value")
                    }
                }
                rerunAddressScreeningDelay(sourceAddressRestriction)?.let {
                    val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
                    screenSourceAddressTimer =
                        timer.schedule(it, it) {
                            screenSourceAddress()
                            true
                        }
                }
            }
        } else {
            sourceAddressRestriction = Restriction.NO_RESTRICTION
        }
    }

    private fun didSetAccountAddressRestriction(accountAddressRestriction: Restriction?) {
        updateAddressRestriction()
    }

    private fun screenAccountAddress() {
        val address = accountAddress
        screen(address) { restriction ->
            when (restriction) {
                Restriction.USER_RESTRICTED,
                Restriction.NO_RESTRICTION,
                Restriction.USER_RESTRICTION_UNKNOWN -> {
                    accountAddressRestriction = restriction
                }
                else -> {
                    throw IllegalArgumentException("Unexpected restriction value")
                }
            }
            rerunAddressScreeningDelay(accountAddressRestriction)?.let {
                val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
                screenAccountAddressTimer =
                    timer.schedule(it, it) {
                        screenAccountAddress()
                        true
                    }
            }
        }
    }

    private fun rerunAddressScreeningDelay(restriction: Restriction?): Double? {
        return when (restriction) {
            Restriction.NO_RESTRICTION -> addressContinuousMonitoringDuration
            Restriction.USER_RESTRICTION_UNKNOWN -> addressRetryDuration
            else -> null
        }
    }

    private fun screenUrl(): String? {
        return helper.configs.publicApiUrl("screen")
    }

    private fun restrictionReason(response: String?): UsageRestriction {
        return if (response != null) {
            val json = helper.parser.decodeJsonObject(response)
            val errors = helper.parser.asList(helper.parser.value(json, "errors"))
            val geoRestriciton =
                errors?.firstOrNull { error ->
                    val code = helper.parser.asString(helper.parser.value(error, "code"))
                    code?.contains("GEOBLOCKED") == true
                }

            if (geoRestriciton !== null) {
                UsageRestriction.http403Restriction
            } else {
                UsageRestriction.userRestriction
            }
        } else {
            UsageRestriction.http403Restriction
        }
    }

    private fun didSetSourceAddress(sourceAddress: String?, oldValue: String?) {
        screenSourceAddressTimer = null
        sourceAddressRestriction = null
        doComplianceScreening()
    }

    private var complianceScreeningAddress: String? = null

    private fun doComplianceScreening() {
        val sourceAddress = sourceAddress
        if (sourceAddress != null && indexerConnected && complianceScreeningAddress != sourceAddress) {
            screenSourceAddress()
            if (screeningUpdate) {
                complianceScreen(EvmAddress(sourceAddress))
            }
            complianceScreeningAddress = sourceAddress
        }
    }

    private fun didSetSourceAddressRestriction(sourceAddressRestriction: Restriction?) {
        updateAddressRestriction()
    }

    private fun updateAddressRestriction() {
        val restrictions: Set<Restriction?> =
            iSetOf(accountAddressRestriction, sourceAddressRestriction)
        addressRestriction =
            if (restrictions.contains(Restriction.USER_RESTRICTED)) {
                UsageRestriction.userRestriction
            } else if (restrictions.contains(Restriction.USER_RESTRICTION_UNKNOWN)) {
                UsageRestriction.userRestrictionUnknown
            } else {
                if (sourceAddressRestriction == null && accountAddressRestriction == null) {
                    null
                } else {
                    UsageRestriction.noRestriction
                }
            }
    }

    private fun didSetAddressRestriction(addressRestriction: UsageRestriction?) {
        updateRestriction()
    }

    internal open fun updateRestriction() {
        restriction = addressRestriction ?: UsageRestriction.noRestriction
    }

    private fun didSetRestriction(restriction: UsageRestriction?) {
        val state = stateMachine.state ?: PerpetualState.newState()
        stateMachine.state = state.copy(restriction = restriction)
        helper.ioImplementations.threading?.async(ThreadingType.main) {
            helper.stateNotification?.stateChanged(
                state = stateMachine.state,
                changes = StateChanges(
                    iListOf(Changes.restriction),
                ),
            )
        }
    }

    private fun didSetComplianceStatus(compliance: Compliance) {
        val state = stateMachine.state ?: PerpetualState.newState()
        stateMachine.state = state.copy(
            compliance = Compliance(
                geo = state?.compliance?.geo,
                status = compliance.status,
                updatedAt = compliance.updatedAt,
                expiresAt = compliance.expiresAt,
            ),
        )
        helper.ioImplementations.threading?.async(ThreadingType.main) {
            helper.stateNotification?.stateChanged(
                state = stateMachine.state,
                changes = StateChanges(
                    iListOf(Changes.compliance),
                ),
            )
        }
    }
}
