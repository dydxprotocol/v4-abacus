package exchange.dydx.abacus.tests.payloads

internal class Calculations {
    internal val singleOrder = """
        {
            "quoteBalance": 1700.0
            "orders": [
                {
                    "market": "ETH-USD",
                    "price": 1700,
                    "size": -1,
                    "fee": 1.0
                }
            ]
        }
        """
}
