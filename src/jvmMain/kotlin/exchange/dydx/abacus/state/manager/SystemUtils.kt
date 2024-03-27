package exchange.dydx.abacus.state.manager

actual class SystemUtils {
    actual companion object {
        // Temp hack: Hard-coding this value for all JVM consumers is not what we want.
        // Ultimately, this value is passed into the txnMemo. This should just be a configurable
        // field when creating AsyncAbacusStateManager.
        actual val platform: Platform = Platform.android
    }
}
