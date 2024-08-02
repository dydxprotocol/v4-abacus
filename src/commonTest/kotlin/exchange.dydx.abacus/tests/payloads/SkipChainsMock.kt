package exchange.dydx.abacus.tests.payloads

internal class SkipChainsMock {
    internal val payload = """{
    "chains": [
        {
          "chain_name": "Solana",
          "chain_id": "solana",
          "pfm_enabled": false,
          "cosmos_module_support": {
            "authz": false,
            "feegrant": false
          },
          "supports_memo": false,
          "logo_uri": "https://raw.githubusercontent.com/skip-mev/skip-go-registry/main/chains/solana/logo.svg",
          "bech32_prefix": "",
          "fee_assets": [],
          "chain_type": "svm",
          "ibc_capabilities": {
            "cosmos_pfm": false,
            "cosmos_ibc_hooks": false,
            "cosmos_memo": false,
            "cosmos_autopilot": false
          },
          "is_testnet": false,
          "pretty_name": "Solana"
        },
        {
            "chain_name": "kujira",
            "chain_id": "kaiyo-1",
            "pfm_enabled": false,
            "cosmos_module_support": {
                "authz": true,
                "feegrant": true
            },
            "supports_memo": true,
            "logo_uri": "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/kaiyo/chain.png",
            "bech32_prefix": "kujira",
            "fee_assets": [
                {
                    "denom": "ibc/47BD209179859CDE4A2806763D7189B6E6FE13A17880FE2B42DE1E6C1E329E23",
                    "gas_price": null
                },
                {
                    "denom": "ibc/EFF323CC632EC4F747C61BCE238A758EFDB7699C3226565F7C20DA06509D59A5",
                    "gas_price": null
                }
            ],
            "chain_type": "cosmos",
            "ibc_capabilities": {
                "cosmos_pfm": false,
                "cosmos_ibc_hooks": false,
                "cosmos_memo": true,
                "cosmos_autopilot": false
            },
            "is_testnet": false
        },
        {
            "chain_name": "cheqd",
            "chain_id": "cheqd-mainnet-1",
            "pfm_enabled": false,
            "cosmos_module_support": {
                "authz": true,
                "feegrant": true
            },
            "supports_memo": true,
            "logo_uri": "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/cheqd-mainnet/chain.png",
            "bech32_prefix": "cheqd",
            "fee_assets": [
                {
                    "denom": "ncheq",
                    "gas_price": {
                        "low": "25",
                        "average": "50",
                        "high": "100"
                    }
                }
            ],
            "chain_type": "cosmos",
            "ibc_capabilities": {
                "cosmos_pfm": false,
                "cosmos_ibc_hooks": false,
                "cosmos_memo": true,
                "cosmos_autopilot": false
            },
            "is_testnet": false
        },
        {
            "chain_name": "osmosis",
            "chain_id": "osmosis-1",
            "pfm_enabled": true,
            "cosmos_module_support": {
                "authz": true,
                "feegrant": false
            },
            "supports_memo": true,
            "logo_uri": "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/osmosis/chain.png",
            "bech32_prefix": "osmo",
            "fee_assets": [
                {
                    "denom": "uosmo",
                    "gas_price": {
                        "low": "0.0025",
                        "average": "0.025",
                        "high": "0.04"
                    }
                }
            ],
            "chain_type": "cosmos",
            "ibc_capabilities": {
                "cosmos_pfm": true,
                "cosmos_ibc_hooks": true,
                "cosmos_memo": true,
                "cosmos_autopilot": false
            },
            "is_testnet": false
        },
        {
            "chain_name": "stride",
            "chain_id": "stride-1",
            "pfm_enabled": true,
            "cosmos_module_support": {
                "authz": false,
                "feegrant": true
            },
            "supports_memo": true,
            "logo_uri": "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/stride/chain.png",
            "bech32_prefix": "stride",
            "fee_assets": [
                {
                    "denom": "stusaga",
                    "gas_price": {
                        "low": "0.01",
                        "average": "0.01",
                        "high": "0.01"
                    }
                },
                {
                    "denom": "stuatom",
                    "gas_price": {
                        "low": "0.0001",
                        "average": "0.001",
                        "high": "0.01"
                    }
                }
            ],
            "chain_type": "cosmos",
            "ibc_capabilities": {
                "cosmos_pfm": true,
                "cosmos_ibc_hooks": false,
                "cosmos_memo": true,
                "cosmos_autopilot": true
            },
            "is_testnet": false
        },
        {
            "chain_name": "aura",
            "chain_id": "xstaxy-1",
            "pfm_enabled": false,
            "cosmos_module_support": {
                "authz": true,
                "feegrant": true
            },
            "supports_memo": true,
            "logo_uri": "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/xstaxy/chain.png",
            "bech32_prefix": "aura",
            "fee_assets": [
                {
                    "denom": "uaura",
                    "gas_price": {
                        "low": "0.001",
                        "average": "0.0025",
                        "high": "0.004"
                    }
                }
            ],
            "chain_type": "cosmos",
            "ibc_capabilities": {
                "cosmos_pfm": false,
                "cosmos_ibc_hooks": false,
                "cosmos_memo": true,
                "cosmos_autopilot": false
            },
            "is_testnet": false
        },
        {
      "chain_name": "Ethereum",
      "chain_id": "1",
      "pfm_enabled": false,
      "cosmos_module_support": {
        "authz": false,
        "feegrant": false
      },
      "supports_memo": false,
      "logo_uri": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/info/logo.png",
      "bech32_prefix": "",
      "fee_assets": [],
      "chain_type": "evm",
      "ibc_capabilities": {
        "cosmos_pfm": false,
        "cosmos_ibc_hooks": false,
        "cosmos_memo": false,
        "cosmos_autopilot": false
      },
      "is_testnet": false
    },
    {
    "chain_name": "Arbitrum",
    "chain_id": "42161",
    "pfm_enabled": false,
    "cosmos_module_support": {
        "authz": false,
        "feegrant": false
    },
    "supports_memo": false,
    "logo_uri": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/arbitrum/info/logo.png",
    "bech32_prefix": "",
    "fee_assets": [],
    "chain_type": "evm",
    "ibc_capabilities": {
        "cosmos_pfm": false,
        "cosmos_ibc_hooks": false,
        "cosmos_memo": false,
        "cosmos_autopilot": false
    },
    "is_testnet": false,
    "pretty_name": "Arbitrum"
}
    ]
}"""
}
