package exchange.dydx.abacus.state.v2.supervisor

import exchange.dydx.abacus.protocols.AnalyticsEvent
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.filterNotNull
import exchange.dydx.abacus.utils.iMapOf
import kollections.toIMap
import kotlinx.datetime.Clock

internal interface SubaccountTransactionTrackerProtocol {
    fun trackingParams(interval: Double): IMap<String, Any>
    fun tracking(eventName: String, params: IMap<String, Any?>?)
    fun trackOrderClick(
        analyticsPayload: IMap<String, Any?>?,
        analyticsEvent: AnalyticsEvent,
    ): Double

    fun trackOrderSubmit(
        uiClickTimeMs: Double,
        analyticsPayload: IMap<String, Any>?,
        isCancel: Boolean = false
    ): Double

    fun trackOrderSubmitted(
        error: ParsingError?,
        analyticsPayload: IMap<String, Any>?,
        isCancel: Boolean = false,
    )
}

internal class SubaccountTransactionTracker(
    private val helper: NetworkHelper,
) : SubaccountTransactionTrackerProtocol {
    override fun trackingParams(interval: Double): IMap<String, Any> {
        return iMapOf(
            "roundtripMs" to interval,
        )
    }

    private fun uiTrackingParams(interval: Double): IMap<String, Any> {
        return iMapOf(
            "clickToSubmitOrderDelayMs" to interval,
        )
    }

    override fun tracking(eventName: String, params: IMap<String, Any?>?) {
        val requiredParams = helper.validatorUrl?.let { iMapOf("validatorUrl" to it) } ?: iMapOf()
        val mergedParams =
            params?.let { ParsingHelper.merge(params.filterNotNull(), requiredParams) }
                ?: requiredParams
        val paramsAsString = helper.jsonEncoder.encode(mergedParams)
        helper.ioImplementations.threading?.async(ThreadingType.main) {
            helper.ioImplementations.tracking?.log(eventName, paramsAsString)
        }
    }

    override fun trackOrderClick(
        analyticsPayload: IMap<String, Any?>?,
        analyticsEvent: AnalyticsEvent,
    ): Double {
        val uiClickTimeMs = Clock.System.now().toEpochMilliseconds().toDouble()
        tracking(
            analyticsEvent.rawValue,
            analyticsPayload,
        )
        return uiClickTimeMs
    }

    override fun trackOrderSubmit(
        uiClickTimeMs: Double,
        analyticsPayload: IMap<String, Any>?,
        isCancel: Boolean,
    ): Double {
        val submitTimeMs = Clock.System.now().toEpochMilliseconds().toDouble()
        val uiDelayTimeMs = submitTimeMs - uiClickTimeMs

        tracking(
            if (isCancel) AnalyticsEvent.TradeCancelOrder.rawValue else AnalyticsEvent.TradePlaceOrder.rawValue,
            ParsingHelper.merge(uiTrackingParams(uiDelayTimeMs), analyticsPayload)?.toIMap(),
        )

        return submitTimeMs
    }

    override fun trackOrderSubmitted(
        error: ParsingError?,
        analyticsPayload: IMap<String, Any>?,
        isCancel: Boolean,
    ) {
        if (error != null) {
            tracking(
                if (isCancel) AnalyticsEvent.TradeCancelOrderSubmissionFailed.rawValue else AnalyticsEvent.TradePlaceOrderSubmissionFailed.rawValue,
                ParsingHelper.merge(errorTrackingParams(error), analyticsPayload)?.toIMap(),
            )
        } else {
            tracking(
                if (isCancel) AnalyticsEvent.TradeCancelOrderSubmissionConfirmed.rawValue else AnalyticsEvent.TradePlaceOrderSubmissionConfirmed.rawValue,
                analyticsPayload,
            )
        }
    }

    private fun errorTrackingParams(error: ParsingError): IMap<String, Any> {
        return if (error.stringKey != null) {
            iMapOf(
                "errorType" to error.type.rawValue,
                "errorMessage" to error.message,
                "errorStringKey" to error.stringKey,
            )
        } else {
            iMapOf(
                "errorType" to error.type.rawValue,
                "errorMessage" to error.message,
            )
        }
    }
}
