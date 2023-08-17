package exchange.dydx.abacus.tests.payloads

class SquidRouteMock {
    internal val errors_payload = """
        {
	"errors": [{
		"path": "toChain",
		"message": "toChain: dydxprotocol-testnet-1 unsupported chain id",
		"errorType": "InputValidationError"
	}, {
		"path": "toToken",
		"message": "toToken: ibc/39549F06486BACA7494C9ACDD53CDD30AA9E723AB657674DBD388F867B61CA7B is not a supported token for the given chain id",
		"errorType": "InputValidationError"
	}]
}
        """
    internal val payload = """
    {
	"route": {
		"estimate": {
			"fromAmount": "1000000000000000000",
			"sendAmount": "806353047",
			"toAmount": "806203047",
			"toAmountMin": "798139517",
			"fromAmountUSD": "2,109.64",
			"route": {
				"fromChain": [{
					"type": "SWAP",
					"dex": {
						"chainName": "Ethereum-2",
						"dexName": "UniswapV2",
						"swapRouter": "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D",
						"factory": "0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f",
						"isCrypto": true
					},
					"target": "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D",
					"path": ["0xB4FBF271143F4FBf7B91A5ded31805e42b2208d6", "0x254d06f33bDc5b8ee05b2ea472107E300226659A"],
					"swapType": "crypto",
					"squidCallType": 2,
					"fromToken": {
						"chainId": 5,
						"address": "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE",
						"name": "Ethereum",
						"symbol": "ETH",
						"decimals": 18,
						"logoURI": "https://tokens.1inch.io/0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee.png",
						"coingeckoId": "ethereum",
						"commonKey": "eth-wei"
					},
					"toToken": {
						"chainId": 5,
						"address": "0x254d06f33bDc5b8ee05b2ea472107E300226659A",
						"name": "Axelar USDC",
						"symbol": "aUSDC",
						"decimals": 6,
						"logoURI": "https://assets.coingecko.com/coins/images/6319/thumb/USD_Coin_icon.png?1547042389",
						"coingeckoId": "axlusdc",
						"commonKey": "uausdc"
					},
					"fromAmount": "1000000000000000000",
					"toAmount": "806353047",
					"toAmountMin": "798289517",
					"exchangeRate": "980.88",
					"priceImpact": "17.79",
					"dynamicSlippage": 1
				}],
				"toChain": []
			},
			"feeCosts": [{
				"name": "Axelar Fee",
				"description": "Axelar bridge fee",
				"percentage": "0",
				"token": {
					"chainId": 5,
					"address": "0x254d06f33bDc5b8ee05b2ea472107E300226659A",
					"name": "Axelar USDC",
					"symbol": "aUSDC",
					"decimals": 6,
					"logoURI": "https://assets.coingecko.com/coins/images/6319/thumb/USD_Coin_icon.png?1547042389",
					"coingeckoId": "axlusdc",
					"commonKey": "uausdc"
				},
				"amount": "150000",
				"amountUSD": "0.1504"
			}],
			"gasCosts": [{
				"type": "executeCall",
				"token": {
					"chainId": 5,
					"address": "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE",
					"name": "Ethereum",
					"symbol": "ETH",
					"decimals": 18,
					"logoURI": "https://tokens.1inch.io/0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee.png",
					"coingeckoId": "ethereum",
					"commonKey": "eth-wei"
				},
				"amount": "3106212894920000",
				"amountUSD": "6.5509",
				"gasPrice": "6076129012",
				"maxFeePerGas": "13592595232",
				"maxPriorityFeePerGas": "1500000000",
				"estimate": "410000",
				"limit": "410000"
			}],
			"estimatedRouteDuration": 960,
			"exchangeRate": "806.203047",
			"aggregatePriceImpact": "17.79",
			"toAmountUSD": "808.6217"
		},
		"params": {
			"slippage": 1,
			"toAddress": "osmo1mk2ska8ldrqtg6qj6vfr9lsjenncwe073a25a7",
			"fromAmount": "1000000000000000000",
			"toToken": {
				"chainId": "osmo-test-4",
				"address": "uausdc",
				"name": "aUSDC",
				"symbol": "aUSDC",
				"decimals": 6,
				"logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
				"coingeckoId": "axlusdc",
				"commonKey": "uausdc",
				"ibcDenom": "ibc/75C8E3091D507A5A111C652F9C76C2E53059E24759A98B523723E02FA33EEF51"
			},
			"fromToken": {
				"chainId": 5,
				"address": "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE",
				"name": "Ethereum",
				"symbol": "ETH",
				"decimals": 18,
				"logoURI": "https://tokens.1inch.io/0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee.png",
				"coingeckoId": "ethereum",
				"commonKey": "eth-wei"
			},
			"toChain": "osmo-test-4",
			"fromChain": "5",
			"enableForecall": "true"
		},
		"transactionRequest": {
			"routeType": "CALL_BRIDGE",
			"targetAddress": "0xe25e5ae59592bFbA3b5359000fb72E6c21D3228E",
			"data": "0xf35af1f800000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000de0b6b3a764000000000000000000000000000000000000000000000000000000000000000000c00000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000016000000000000000000000000000000000000000000000000000000000000001a000000000000000000000000000000000000000000000000000000000000000096f736d6f7369732d350000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002b6f736d6f316d6b32736b61386c647271746736716a36766672396c736a656e6e6377653037336132356137000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000561555344430000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000020000000000000000000000007a250d5630b4cf539739df2c5dacb4c659f2488d000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000000001c000000000000000000000000000000000000000000000000000000000000000e47ff36ab5000000000000000000000000000000000000000000000000000000002f94ee6d0000000000000000000000000000000000000000000000000000000000000080000000000000000000000000e25e5ae59592bfba3b5359000fb72e6c21d3228e0000000000000000000000000000000000000000000000000000018781bdd8ff0000000000000000000000000000000000000000000000000000000000000002000000000000000000000000b4fbf271143f4fbf7b91a5ded31805e42b2208d6000000000000000000000000254d06f33bdc5b8ee05b2ea472107e300226659a000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
			"value": "1000000000000000000",
			"gasLimit": "410000",
			"gasPrice": "6076129012",
			"maxFeePerGas": "13592595232",
			"maxPriorityFeePerGas": "1500000000"
		}
	}
}
    """
}