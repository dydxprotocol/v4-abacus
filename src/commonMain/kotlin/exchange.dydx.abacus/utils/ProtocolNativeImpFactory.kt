package exchange.dydx.abacus.utils

import exchange.dydx.abacus.protocols.DYDXChainTransactionsProtocol
import exchange.dydx.abacus.protocols.DataNotificationProtocol
import exchange.dydx.abacus.protocols.FileSystemProtocol
import exchange.dydx.abacus.protocols.FormatterProtocol
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.LoggingProtocol
import exchange.dydx.abacus.protocols.PresentationProtocol
import exchange.dydx.abacus.protocols.RestProtocol
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.protocols.ThreadingProtocol
import exchange.dydx.abacus.protocols.TimerProtocol
import exchange.dydx.abacus.protocols.TrackingProtocol
import exchange.dydx.abacus.protocols.V3PrivateSignerProtocol
import exchange.dydx.abacus.protocols.WebSocketProtocol
import kollections.JsExport

@JsExport
open class ProtocolNativeImpFactory(
    var rest: RestProtocol? = null,
    var webSocket: WebSocketProtocol? = null,
    var chain: DYDXChainTransactionsProtocol? = null,
    var localizer: LocalizerProtocol? = null,
    var formatter: FormatterProtocol? = null,
    var tracking: TrackingProtocol? = null,
    var threading: ThreadingProtocol? = null,
    var timer: TimerProtocol? = null,
    var stateNotification: StateNotificationProtocol? = null,
    var dataNotification: DataNotificationProtocol? = null,
    var fileSystem: FileSystemProtocol? = null,
    var v3Signer: V3PrivateSignerProtocol? = null,
    var presentation: PresentationProtocol? = null,
    var logging: LoggingProtocol? = null,
)

@JsExport
class IOImplementations(
    var rest: RestProtocol?,
    var webSocket: WebSocketProtocol?,
    var chain: DYDXChainTransactionsProtocol?,
    var tracking: TrackingProtocol?,
    var threading: ThreadingProtocol?,
    var timer: TimerProtocol?,
    var fileSystem: FileSystemProtocol?,
    var logging: LoggingProtocol?,
)

@JsExport
class UIImplementations(
    var localizer: LocalizerProtocol?,
    var formatter: FormatterProtocol?,
)
