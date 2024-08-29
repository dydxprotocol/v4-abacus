package exchange.dydx.abacus.fakes

import exchange.dydx.abacus.protocols.TrackingProtocol

internal class FakeTrackingProtocol : TrackingProtocol {
    var lastEvent: String? = null
    var lastData: String? = null

    override fun log(event: String, data: String?) {
        lastData = data
        lastEvent = event
    }
}
