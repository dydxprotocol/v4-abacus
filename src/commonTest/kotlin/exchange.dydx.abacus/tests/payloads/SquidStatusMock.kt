package exchange.dydx.abacus.tests.payloads

class SquidStatusMock {
    internal val payload = """
{
    "id": "0xe97ef6ebead02bff36523249d7a72a9dba8f30a789cba640272e728b1c5ce300_30_100",
    "status": "destination_executed",
    "gasStatus": "gas_paid_enough_gas",
    "isGMPTransaction": true,
    "axelarTransactionUrl": "https://testnet.axelarscan.io/gmp/0xe97ef6ebead02bff36523249d7a72a9dba8f30a789cba640272e728b1c5ce300",
    "fromChain": {
        "transactionId": "0xe97ef6ebead02bff36523249d7a72a9dba8f30a789cba640272e728b1c5ce300",
        "blockNumber": 9419627,
        "callEventStatus": "",
        "callEventLog": [],
        "chainData": {
            "chainName": "Ethereum-2",
            "chainType": "evm",
            "rpc": "https://rpc.ankr.com/eth_goerli",
            "networkName": "ETH Goerli Testnet",
            "chainId": 5,
            "nativeCurrency": {
                "name": "Ethereum",
                "symbol": "ETH",
                "decimals": 18,
                "icon": "https://tokens.1inch.io/0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee.png"
            },
            "swapAmountForGas": "2000000",
            "chainIconURI": "https://tokens.1inch.io/0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee.png",
            "blockExplorerUrls": [
                "https://goerli.etherscan.io/"
            ],
            "chainNativeContracts": {
                "wrappedNativeToken": "0xB4FBF271143F4FBf7B91A5ded31805e42b2208d6",
                "ensRegistry": "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e",
                "multicall": "0xcA11bde05977b3631167028862bE2a173976CA11",
                "usdcToken": "0x254d06f33bDc5b8ee05b2ea472107E300226659A"
            },
            "axelarContracts": {
                "gateway": "0xBC6fcce7c5487d43830a219CA6E7B83238B41e71",
                "forecallable": ""
            },
            "squidContracts": {
                "squidRouter": "0x481A2AAE41cd34832dDCF5A79404538bb2c02bC8",
                "defaultCrosschainToken": "0x254d06f33bDc5b8ee05b2ea472107E300226659A",
                "squidMulticall": "0xd9b7849d3a49e287c8E448cea0aAe852861C4545"
            },
            "estimatedRouteDuration": 960,
            "estimatedExpressRouteDuration": 20
        },
        "transactionUrl": "https://goerli.etherscan.io/tx/0xe97ef6ebead02bff36523249d7a72a9dba8f30a789cba640272e728b1c5ce300"
    },
    "toChain": {
        "transactionId": "1DDBD983CAE6D4ACF3012FF17C71EE4A884DE4A0CBA9F28CF85382CA01BF8CAB",
        "blockNumber": 1888874,
        "callEventStatus": "",
        "callEventLog": [],
        "chainData": {
            "chainName": "osmosis-6",
            "chainType": "cosmos",
            "rpc": "https://rpc.osmotest5.osmosis.zone",
            "rest": "https://testnet-rest.osmosis.zone",
            "networkName": "Osmosis Testnet",
            "chainId": "osmo-test-5",
            "nativeCurrency": {
                "name": "Osmosis",
                "symbol": "OSMO",
                "decimals": 6,
                "icon": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/main/public/images/chains/osmosis.svg"
            },
            "swapAmountForGas": "2000000",
            "chainIconURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/main/public/images/chains/osmosis.svg",
            "blockExplorerUrls": [
                "https://testnet.mintscan.io/osmosis-testnet/"
            ],
            "stakeCurrency": {
                "coinDenom": "OSMO",
                "coinMinimalDenom": "uosmo",
                "coinDecimals": 6,
                "coingeckoId": "osmosis"
            },
            "bip44": {
                "coinType": 118
            },
            "bech32Config": {
                "bech32PrefixAccAddr": "osmo",
                "bech32PrefixAccPub": "osmopub",
                "bech32PrefixValAddr": "osmovaloper",
                "bech32PrefixValPub": "osmovaloperpub",
                "bech32PrefixConsAddr": "osmovalcons",
                "bech32PrefixConsPub": "osmovalconspub"
            },
            "currencies": [
                {
                    "coinDenom": "OSMO",
                    "coinMinimalDenom": "uosmo",
                    "coinDecimals": 6,
                    "coingeckoId": "osmosis"
                },
                {
                    "coinDenom": "ION",
                    "coinMinimalDenom": "uion",
                    "coinDecimals": 6,
                    "coingeckoId": "ion"
                }
            ],
            "feeCurrencies": [
                {
                    "coinDenom": "OSMO",
                    "coinMinimalDenom": "uosmo",
                    "coinDecimals": 6,
                    "coingeckoId": "osmosis"
                }
            ],
            "coinType": 118,
            "gasPriceStep": {
                "low": 0,
                "average": 0,
                "high": 0.025
            },
            "features": [
                "ibc-transfer",
                "ibc-go"
            ],
            "squidContracts": {
                "defaultCrosschainToken": "uausdc"
            },
            "axelarContracts": {
                "gateway": ""
            },
            "chainToAxelarChannelId": "channel-312",
            "estimatedRouteDuration": 180,
            "estimatedExpressRouteDuration": 20
        },
        "transactionUrl": "https://testnet.mintscan.io/osmosis-testnet/txs/1DDBD983CAE6D4ACF3012FF17C71EE4A884DE4A0CBA9F28CF85382CA01BF8CAB"
    },
    "timeSpent": {
        "call_confirm": 1562,
        "total": 1584
    },
    "routeStatus": [
        {
            "chainId": 5,
            "txHash": "0xe97ef6ebead02bff36523249d7a72a9dba8f30a789cba640272e728b1c5ce300",
            "status": "success",
            "action": "call"
        },
        {
            "chainId": "axelar-testnet-lisbon-3",
            "txHash": "5475BF0FE4856D951C21E12B00575D5D8B8A809D6C37AE44B846BF766D2F9A01",
            "status": "success",
            "action": "executed"
        },
        {
            "chainId": "osmo-test-5",
            "txHash": "1DDBD983CAE6D4ACF3012FF17C71EE4A884DE4A0CBA9F28CF85382CA01BF8CAB",
            "status": "success",
            "action": "executed"
        },
        {
            "chainId": "grand-1",
            "txHash": "BAFFE3F10D2A5AF68697B6B93A983F25F8D2A2BCC732E11393CB22A2E6DDA21F",
            "status": "success",
            "action": "executed"
        },
        {
            "chainId": "dydx-testnet-1",
            "txHash": "C737E15259D056E057E3837B1E81609C9FDEA06F9F8C1F4298FA3E11E1AC4CBD",
            "status": "success",
            "action": "executed"
        }
    ],
    "error": {},
    "squidTransactionStatus": "success"
}
    """
}