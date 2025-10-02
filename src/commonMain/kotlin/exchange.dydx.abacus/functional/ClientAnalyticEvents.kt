package exchange.dydx.abacus.functional

import exchange.dydx.abacus.output.TransferStatus
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.output.input.TransferInputSummary

interface ClientTrackableEvent {
    val name: String
    val customParameters: Map<String, Any>
}

class ClientTrackableEventType {

    class AppStart : ClientTrackableEvent {
        override val name: String
            get() = "AppStart"

        override val customParameters: Map<String, Any>
            get() = emptyMap()
    }

    class DeepLinkHandled(private val url: String, private val succeeded: Boolean) :
        ClientTrackableEvent {
        override val name: String get() = "DeeplinkHandled"
        override val customParameters: Map<String, Any>
            get() = mapOf(
                "url" to url,
                "succeeded" to succeeded,
            )
    }

    class NotificationPermissionsChanged(private val isAuthorized: Boolean) : ClientTrackableEvent {
        override val name: String get() = "NotificationPermissionsChanged"
        override val customParameters: Map<String, Any>
            get() = mapOf(
                "is_authorized" to isAuthorized,
            )

        override fun toString(): String = name
    }

    class ModeSelectorEvent(private val fromMode: String, private val toMode: String) :
        ClientTrackableEvent {
        override val name: String get() = "ModeSelectorEvent"
        override val customParameters: Map<String, Any>
            get() = mapOf(
                "from" to fromMode,
                "to" to toMode,
            )
    }

    class OnboardingStepChanged(
        private val step: String,
        private val state: String
    ) : ClientTrackableEvent {
        override val name: String get() = "OnboardingStepChanged"
        override val customParameters: Map<String, Any>
            get() = mapOf(
                "step" to step,
                "state" to state,
            )
    }

    class VaultFormPreviewStep(
        private val amount: Double,
        private val type: String
    ) : ClientTrackableEvent {
        override val name: String get() = "VaultFormPreviewStep"
        override val customParameters: Map<String, Any>
            get() = mapOf(
                "amount" to amount,
                "operation" to type,
            )
    }

    class AttemptVaultOperation(
        private val type: String,
        private val amount: Double?,
        private val slippage: Double?
    ) : ClientTrackableEvent {
        override val name: String get() = "AttemptVaultOperation"

        override val customParameters: Map<String, Any>
            get() =
                mapOf(
                    "operation" to type,
                    "amount" to amount,
                    "slippage" to slippage,
                ).filterValues { it != null } as Map<String, Any>
    }

    class SuccessfulVaultOperation(
        private val type: String,
        private val amount: Double,
        private val amountDiff: Double
    ) : ClientTrackableEvent {
        override val name: String get() = "SuccessfulVaultOperation"
        override val customParameters: Map<String, Any> get() = mapOf(
            "operation" to type,
            "amount" to amount,
            "amountDiff" to amountDiff,
        )

        override fun toString(): String = name
    }

    class VaultOperationProtocolError(
        private val type: String
    ) : ClientTrackableEvent {
        override val name: String get() = "VaultOperationProtocolError"
        override val customParameters: Map<String, Any> get() = mapOf(
            "operation" to type,
        )
    }

    class RoutingEvent(
        private val fromPath: String? = null,
        private val toPath: String,
        private val fromQuery: String? = null,
        private val toQuery: String? = null
    ) : ClientTrackableEvent {
        override val name: String get() = "RoutingEvent"
        override val customParameters: Map<String, Any> get() = mapOf(
            "fromPath" to (fromPath ?: "nil"),
            "toPath" to toPath,
            "fromQuery" to (fromQuery ?: "nil"),
            "toQuery" to (toQuery ?: "nil"),
        )
    }

    class AppModeSurveyEvent(
        private val option1: Boolean,
        private val option2: Boolean,
        private val option3: Boolean,
        private val feedback: String? = null,
        private val isSubmit: Boolean,
        private val isDoNotShowAgain: Boolean
    ) : ClientTrackableEvent {
        override val name: String get() = "AppModeSurveyEvent"
        override val customParameters: Map<String, Any> get() = mapOf(
            "option1" to option1,
            "option2" to option2,
            "option3" to option3,
            "feedback" to (feedback ?: "nil"),
            "isSubmit" to isSubmit,
            "isDoNotShowAgain" to isDoNotShowAgain,
        )
    }

    class DepositInitiatedEvent(
        private val transferInput: TransferInput,
        private val summary: TransferInputSummary?,
        private val isInstantDeposit: Boolean
    ) : ClientTrackableEvent {
        override val name: String get() = "DepositInitiated"
        override val customParameters: Map<String, Any> get() = mapOf(
            "sourceAssetDenom" to transferInput.token,
            "sourceAssetChainId" to transferInput.chain,
            "amountIn" to transferInput.size?.size,
            "amountOut" to summary?.toAmount,
            "usdAmountOut" to summary?.toAmountUSDC,
            "estimatedAmountOut" to summary?.toAmountMin,
            "swapPriceImpactPercent" to summary?.aggregatePriceImpact,
            "estimatedRouteDurationSeconds" to summary?.estimatedRouteDurationSeconds,
            "isInstantDeposit" to isInstantDeposit,
        ).filterValues { it != null } as Map<String, Any>
    }

    class DepositSubmittedEvent(
        private val transferInput: TransferInput,
        private val summary: TransferInputSummary?,
        private val txHash: String,
        private val isInstantDeposit: Boolean
    ) : ClientTrackableEvent {
        override val name: String get() = "DepositSubmitted"
        override val customParameters: Map<String, Any> get() = mapOf(
            "tokenInDenom" to transferInput.token,
            "tokenInChainId" to transferInput.chain,
            "tokenAmount" to transferInput.size?.size,
            "estimatedAmountUsd" to summary?.toAmountUSDC,
            "isInstantDeposit" to isInstantDeposit,
            "txHash" to txHash,
        ).filterValues { it != null } as Map<String, Any>
    }

    class DepositErrorEvent(
        private val transferInput: TransferInput,
        private val errorMessage: String
    ) : ClientTrackableEvent {
        override val name: String get() = "DepositError"
        override val customParameters: Map<String, Any> get() = mapOf(
            "tokenInDenom" to transferInput.token,
            "tokenInChainId" to transferInput.chain,
            "error" to errorMessage,
        ).filterValues { it != null } as Map<String, Any>
    }

    class DepositFinalizedEvent(
        private val status: TransferStatus
    ) : ClientTrackableEvent {
        override val name: String get() = "DepositFinalized"
        override val customParameters: Map<String, Any> get() = mapOf(
            "tokenInChainId" to status.routeStatuses?.firstOrNull()?.chainId,
        ).filterValues { it != null } as Map<String, Any>

        override fun toString(): String = name
    }

    class WithdrawInitiatedEvent(
        private val transferInput: TransferInput,
        private val summary: TransferInputSummary?
    ) : ClientTrackableEvent {
        override val name: String get() = "WithdrawInitiated"
        override val customParameters: Map<String, Any> get() = mapOf(
            "sourceAssetDenom" to transferInput.token,
            "sourceAssetChainId" to transferInput.chain,
            "amountIn" to transferInput.size?.size,
            "amountOut" to summary?.toAmount,
            "usdAmountOut" to summary?.toAmountUSDC,
            "estimatedAmountOut" to summary?.toAmountMin,
            "swapPriceImpactPercent" to summary?.aggregatePriceImpact,
            "estimatedRouteDurationSeconds" to summary?.estimatedRouteDurationSeconds,
        ).filterValues { it != null } as Map<String, Any>
    }

    class WithdrawSubmittedEvent(
        private val transferInput: TransferInput,
        private val summary: TransferInputSummary?,
        private val txHash: String,
        private val isInstantWithdraw: Boolean
    ) : ClientTrackableEvent {
        override val name: String get() = "WithdrawSubmitted"
        override val customParameters: Map<String, Any> get() = mapOf(
            "destinationChainId" to transferInput.chain,
            "estimatedAmountUsd" to summary?.toAmountUSDC,
            "isInstantWithdraw" to isInstantWithdraw,
            "txHash" to txHash,
        ).filterValues { it != null } as Map<String, Any>
    }

    class WithdrawErrorEvent(
        private val transferInput: TransferInput,
        private val errorMessage: String
    ) : ClientTrackableEvent {
        override val name: String get() = "WithdrawError"
        override val customParameters: Map<String, Any> get() = mapOf(
            "tokenInDenom" to transferInput.token,
            "tokenInChainId" to transferInput.chain,
            "error" to errorMessage,
        ).filterValues { it != null } as Map<String, Any>
    }

    class WithdrawFinalizedEvent(
        private val status: TransferStatus
    ) : ClientTrackableEvent {
        override val name: String get() = "WithdrawFinalized"

        override val customParameters: Map<String, Any> get() = mapOf(
            "tokenInChainId" to status.routeStatuses?.lastOrNull()?.chainId,
        ).filterValues { it != null } as Map<String, Any>
    }

    class FiatDepositShowInputEvent() : ClientTrackableEvent {
        override val name: String get() = "FiatDepositShowInput"
        override val customParameters: Map<String, Any> get() = emptyMap()
    }

    class FiatDepositRouteToProviderCompletedEvent(
        private val amountUsd: Double?,
        private val depositAddress: String?,
        private val provider: String,
    ) : ClientTrackableEvent {
        override val name: String get() = "FiatDepositRouteToProviderCompleted"
        override val customParameters: Map<String, Any> get() = mapOf(
            "amountUsd" to amountUsd,
            "depositAddress" to depositAddress,
            "provider" to provider,
        ).filterValues { it != null } as Map<String, Any>
    }

    class FiatDepositRouteToProviderErrorEvent(
        private val message: String?,
        private val provider: String,
    ) : ClientTrackableEvent {
        override val name: String get() = "FiatDepositRouteToProviderError"
        override val customParameters: Map<String, Any> get() = mapOf(
            "message" to message,
            "provider" to provider,
        ).filterValues { it != null } as Map<String, Any>
    }

    class FiatDepositMoonPayCallbackEvent(
        private val callbackName: String,
        private val data: Map<String, Any>? = null,
    ) : ClientTrackableEvent {
        override val name: String get() = "FiatDepositMoonPayCallback"
        override val customParameters: Map<String, Any> get() = mapOf(
            "callbackName" to callbackName,
            "data" to data,
        ).filterValues { it != null } as Map<String, Any>
    }
}
