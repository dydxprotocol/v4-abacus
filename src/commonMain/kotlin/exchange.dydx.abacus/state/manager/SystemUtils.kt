package exchange.dydx.abacus.state.manager

enum class Platform(val rawValue: String) {
    ios("ios"),
    android("android"),
    web("web"),
}

expect class SystemUtils {
    companion object {
        val platform: Platform
    }
}
