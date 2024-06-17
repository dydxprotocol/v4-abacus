package exchange.dydx.abacus.utils

// Account Constants
internal const val NUM_PARENT_SUBACCOUNTS = 128
internal const val MAX_SUBACCOUNT_NUMBER = 128_000

// Short Term Order Duration (Blocks) to add to GTB
internal const val SHORT_TERM_ORDER_DURATION = 10
internal const val QUANTUM_MULTIPLIER = 1_000_000

// Route Param Constants
internal const val SLIPPAGE_PERCENT = "1"

// Isolated Margin Constants
internal const val MAX_LEVERAGE_BUFFER_PERCENT = 0.98

// Order flags
internal const val SHORT_TERM_ORDER_FLAGS = 0
internal const val CONDITIONAL_ORDER_FLAGS = 32