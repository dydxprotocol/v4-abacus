package exchange.dydx.abacus.processor.router

//
enum class ChainType(val rawValue: String) {
    EVM("evm"),
    COSMOS("cosmos"),
    SVM("svm"),
}
