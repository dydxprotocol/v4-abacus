package exchange.dydx.abacus.tests.payloads

internal class FeeDiscountsMock {
    internal val call = """
        [{
        	"tier": "I",
        	"symbol": "≥",
        	"balance": 100,
        	"discount": 0.03
        }, {
        	"tier": "II",
        	"symbol": "≥",
        	"balance": 1000,
        	"discount": 0.05
        }, {
        	"tier": "III",
        	"symbol": "≥",
        	"balance": 5000,
        	"discount": 0.1
        }, {
        	"tier": "IV",
        	"symbol": "≥",
        	"balance": 10000,
        	"discount": 0.15
        }, {
        	"tier": "V",
        	"symbol": "≥",
        	"balance": 50000,
        	"discount": 0.2
        }, {
        	"tier": "VI",
        	"symbol": "≥",
        	"balance": 100000,
        	"discount": 0.25
        }, {
        	"tier": "VII",
        	"symbol": "≥",
        	"balance": 200000,
        	"discount": 0.3
        }, {
        	"tier": "VIII",
        	"symbol": "≥",
        	"balance": 500000,
        	"discount": 0.35
        }, {
        	"tier": "IX",
        	"symbol": "≥",
        	"balance": 1000000,
        	"discount": 0.4
        }, {
        	"tier": "X",
        	"symbol": "≥",
        	"balance": 2500000,
        	"discount": 0.45
        }, {
        	"tier": "VIP",
        	"symbol": "≥",
        	"balance": 5000000,
        	"discount": 0.5
        }]
    """.trimIndent()
}
