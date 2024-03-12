package exchange.dydx.abacus.tests.payloads

internal class FeeTiersMock {
    internal val call = """
        [{
        	"tier": "Free",
        	"symbol": "<",
        	"volume": 100000,
        	"maker": 0,
        	"taker": 0
        }, {
        	"tier": "I",
        	"symbol": "≥",
        	"volume": 100000,
        	"maker": 0.0002,
        	"taker": 0.0005
        }, {
        	"tier": "II",
        	"symbol": "≥",
        	"volume": 1000000,
        	"maker": 0.00015,
        	"taker": 0.0004
        }, {
        	"tier": "III",
        	"symbol": "≥",
        	"volume": 5000000,
        	"maker": 0.0001,
        	"taker": 0.00035
        }, {
        	"tier": "IV",
        	"symbol": "≥",
        	"volume": 10000000,
        	"maker": 0.00005,
        	"taker": 0.0003
        }, {
        	"tier": "V",
        	"symbol": "≥",
        	"volume": 50000000,
        	"maker": 0,
        	"taker": 0.00025
        }, {
        	"tier": "VIP",
        	"symbol": "≥",
        	"volume": 200000000,
        	"maker": 0,
        	"taker": 0.0002
        }]
    """.trimIndent()
}
