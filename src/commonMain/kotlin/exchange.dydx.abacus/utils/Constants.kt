package exchange.dydx.abacus.utils

// Account Constants
internal const val NUM_PARENT_SUBACCOUNTS = 128
internal const val MAX_SUBACCOUNT_NUMBER = 128_000

// Short Term Order Duration (Blocks) to add to GTB
internal const val SHORT_TERM_ORDER_DURATION = 10
internal const val QUANTUM_MULTIPLIER = 1_000_000

// Route Param Constants
internal const val SLIPPAGE_PERCENT = "1"

// Trade Constants
internal const val MAX_FREE_CROSS_COLLATERAL_BUFFER_PERCENT = 0.95

// Isolated Margin Constants
internal const val MAX_FREE_ISOLATED_COLLATERAL_BUFFER_PERCENT = 0.93
internal const val MAX_LEVERAGE_BUFFER_PERCENT = 0.98
internal const val MARGIN_COLLATERALIZATION_CHECK_BUFFER = 0.01;

// Order flags
internal const val SHORT_TERM_ORDER_FLAGS = 0
internal const val CONDITIONAL_ORDER_FLAGS = 32

// Asset Constants
internal const val NATIVE_TOKEN_DEFAULT_ADDRESS = "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"
internal const val ETHEREUM_CHAIN_ID = "1"

// Polling durations
internal const val GEO_POLLING_DURATION_SECONDS = 600.0

// Autosweep Constants
internal const val MIN_USDC_AMOUNT_FOR_AUTO_SWEEP = 50000

// Gas Constants based on historical Squid responses
internal const val DEFAULT_GAS_LIMIT = 1500000
internal const val DEFAULT_GAS_PRICE = 1520000000

// Limit Close GTT duration in Days
internal const val LIMIT_CLOSE_ORDER_DEFAULT_DURATION_DAYS = 28.0
