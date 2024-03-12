package exchange.dydx.abacus.tests.payloads

internal class SquidV2RouteMock {
    internal val payload = """
    {
	"route": {
		"estimate": {
			"actions": [{
				"type": "bridge",
				"chainType": "evm",
				"data": {
					"name": "cctp-5-to-grand-1",
					"provider": "cctp",
					"type": "noble-cctp",
					"calls": [{
						"chainType": "evm",
						"target": "0xD0C3da58f55358142b8d3e06C1C30c5C6114EFE8",
						"callType": 0,
						"payload": {
							"tokenAddress": "0x07865c6E87B9F70255377e024ace6630C1Eaa37F",
							"inputPos": 0
						},
						"value": "0",
						"callData": "0x6fd3504e000000000000000000000000000000000000000000000000000000027c3e366200000000000000000000000000000000000000000000000000000000000000040000000000000000000000001027c683721a2e5dc7382904bb0e755acde2435500000000000000000000000007865c6e87b9f70255377e024ace6630c1eaa37f",
						"estimatedGas": "150000"
					}]
				},
				"fromChain": "5",
				"toChain": "grand-1",
				"fromToken": {
					"type": "evm",
					"chainId": "5",
					"address": "0x07865c6E87B9F70255377e024ace6630C1Eaa37F",
					"name": "Circle USDC",
					"symbol": "USDC",
					"decimals": 6,
					"logoURI": "https://raw.githubusercontent.com/0xsquid/assets/main/images/tokens/usdc.svg",
					"coingeckoId": "usd-coin",
					"subGraphId": "uusdc",
					"subGraphOnly": true,
					"usdPrice": 0.999669
				},
				"toToken": {
					"type": "cosmos",
					"chainId": "grand-1",
					"address": "uusdc",
					"name": "USD Coin",
					"symbol": "USDC",
					"decimals": 6,
					"logoURI": "https://assets.coingecko.com/coins/images/6319/small/USD_Coin_icon.png?1547042389",
					"coingeckoId": "usd-coin",
					"subGraphId": "uusdc",
					"subGraphOnly": true,
					"usdPrice": 0.999669
				},
				"fromAmount": "10674386530",
				"toAmount": "10674386530",
				"toAmountMin": "10674386530",
				"exchangeRate": "1.0",
				"priceImpact": "0.0",
				"stage": 0,
				"provider": "cctp",
				"description": "Bridge USDC to USDC in grand-1"
			}],
			"fromAmount": "10674386530",
			"toAmount": "10674386530",
			"toAmountMin": "10674386530",
			"sendAmount": "10674386530",
			"exchangeRate": "1.0",
			"aggregatePriceImpact": "0.0",
			"estimatedRouteDuration": 20,
			"aggregateSlippage": 0,
			"fromToken": {
				"type": "evm",
				"chainId": "5",
				"address": "0x07865c6E87B9F70255377e024ace6630C1Eaa37F",
				"name": "Circle USDC",
				"symbol": "USDC",
				"decimals": 6,
				"logoURI": "https://raw.githubusercontent.com/0xsquid/assets/main/images/tokens/usdc.svg",
				"coingeckoId": "usd-coin",
				"subGraphId": "uusdc",
				"subGraphOnly": true
			},
			"toToken": {
				"type": "cosmos",
				"chainId": "grand-1",
				"address": "uusdc",
				"name": "USD Coin",
				"symbol": "USDC",
				"decimals": 6,
				"logoURI": "https://assets.coingecko.com/coins/images/6319/small/USD_Coin_icon.png?1547042389",
				"coingeckoId": "usd-coin",
				"subGraphId": "uusdc",
				"subGraphOnly": true
			},
			"isBoostSupported": false,
			"feeCosts": [],
			"gasCosts": [{
				"type": "executeCall",
				"token": {
					"type": "evm",
					"chainId": "5",
					"address": "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE",
					"name": "Ethereum",
					"symbol": "ETH",
					"decimals": 18,
					"logoURI": "https://raw.githubusercontent.com/0xsquid/assets/main/images/tokens/eth.svg",
					"coingeckoId": "ethereum",
					"subGraphId": "eth-wei",
					"subGraphOnly": false,
					"usdPrice": 2025.28
				},
				"amount": "618750007837500",
				"gasLimit": "412500",
				"amountUsd": "1.253"
			}]
		},
		"transactionRequest": {
			"routeType": "BRIDGE",
			"target": "0xD0C3da58f55358142b8d3e06C1C30c5C6114EFE8",
			"data": "0x6fd3504e000000000000000000000000000000000000000000000000000000027c3e366200000000000000000000000000000000000000000000000000000000000000040000000000000000000000001027c683721a2e5dc7382904bb0e755acde2435500000000000000000000000007865c6e87b9f70255377e024ace6630c1eaa37f",
			"value": "0",
			"gasLimit": "412500",
			"gasPrice": "19",
			"maxFeePerGas": "1500000034",
			"maxPriorityFeePerGas": "1500000000"
		},
		"params": {
			"fromChain": "5",
			"toChain": "grand-1",
			"fromToken": "0x07865c6E87B9F70255377e024ace6630C1Eaa37F",
			"toToken": "uusdc",
			"fromAmount": "10674386530",
			"fromAddress": "0xb13CD07B22BC5A69F8500a1Cb3A1b65618d50B22",
			"toAddress": "noble1zqnudqmjrgh9m3ec9yztkrn4ttx7ys64p87kkx",
			"slippageConfig": {
				"autoMode": 1
			},
			"enableBoost": true,
			"prefer": []
		}
	}
}
    """
}
