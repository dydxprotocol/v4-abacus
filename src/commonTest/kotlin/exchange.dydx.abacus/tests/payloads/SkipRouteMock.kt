package exchange.dydx.abacus.tests.payloads

internal class SkipRouteMock {
    internal val payload = """{
    "msgs": [
        {
            "evm_tx": {
                "chain_id": "1",
                "to": "0xBC8552339dA68EB65C8b88B414B5854E0E366cFc",
                "value": "0",
                "data": "d77d6ec00000000000000000000000000000000000000000000000000000000000b19cc000000000000000000000000000000000000000000000000000000000000000040000000000000000000000009dc5ce8a5722795f5723d32b921c53d3bb449348000000000000000000000000a0b86991c6218b36c1d19d4a2e9eb0ce3606eb480000000000000000000000000000000000000000000000000000000000057e40000000000000000000000000691cf4641d5608f085b2c1921172120bb603d074",
                "required_erc20_approvals": [
                    {
                        "token_contract": "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
                        "spender": "0xBC8552339dA68EB65C8b88B414B5854E0E366cFc",
                        "amount": "12000000"
                    }
                ],
                "signer_address": "0x0f7833777bfC9ef72D8B76AE954D3849DD71F829"
            }
        }
    ],
    "txs": [
        {
            "evm_tx": {
                "chain_id": "1",
                "to": "0xBC8552339dA68EB65C8b88B414B5854E0E366cFc",
                "value": "0",
                "data": "d77d6ec00000000000000000000000000000000000000000000000000000000000b19cc000000000000000000000000000000000000000000000000000000000000000040000000000000000000000009dc5ce8a5722795f5723d32b921c53d3bb449348000000000000000000000000a0b86991c6218b36c1d19d4a2e9eb0ce3606eb480000000000000000000000000000000000000000000000000000000000057e40000000000000000000000000691cf4641d5608f085b2c1921172120bb603d074",
                "required_erc20_approvals": [
                    {
                        "token_contract": "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
                        "spender": "0xBC8552339dA68EB65C8b88B414B5854E0E366cFc",
                        "amount": "12000000"
                    }
                ],
                "signer_address": "0x0f7833777bfC9ef72D8B76AE954D3849DD71F829"
            },
            "operations_indices": [
                0
            ]
        }
    ],
    "route": {
        "source_asset_denom": "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
        "source_asset_chain_id": "1",
        "dest_asset_denom": "uusdc",
        "dest_asset_chain_id": "noble-1",
        "amount_in": "12000000",
        "amount_out": "11640000",
        "operations": [
            {
                "cctp_transfer": {
                    "from_chain_id": "1",
                    "to_chain_id": "noble-1",
                    "burn_token": "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
                    "denom_in": "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
                    "denom_out": "uusdc",
                    "bridge_id": "CCTP",
                    "smart_relay": true
                },
                "tx_index": 0,
                "amount_in": "12000000",
                "amount_out": "11640000"
            }
        ],
        "chain_ids": [
            "1",
            "noble-1"
        ],
        "does_swap": false,
        "estimated_amount_out": "11640000",
        "swap_venues": [],
        "txs_required": 1,
        "usd_amount_in": "12.00",
        "usd_amount_out": "11.64",
        "estimated_fees": [
            {
                "fee_type": "SMART_RELAY",
                "bridge_id": "CCTP",
                "amount": "360000",
                "usd_amount": "0.36",
                "origin_asset": {
                    "denom": "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
                    "chain_id": "1",
                    "origin_denom": "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
                    "origin_chain_id": "1",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": false,
                    "is_svm": false,
                    "symbol": "USDC",
                    "name": "USD Coin",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/usdc.svg",
                    "decimals": 6,
                    "token_contract": "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
                    "coingecko_id": "usd-coin",
                    "recommended_symbol": "USDC"
                },
                "chain_id": "1",
                "tx_index": 0
            }
        ],
        "required_chain_addresses": [
            "1",
            "noble-1"
        ]
    }
}"""
    internal val payloadCCTPDydxToNoble = """
        {
    "msgs": [
        {
            "multi_chain_msg": {
                "chain_id": "dydx-mainnet-1",
                "path": [
                    "dydx-mainnet-1",
                    "noble-1"
                ],
                "msg": "{\"source_port\":\"transfer\",\"source_channel\":\"channel-0\",\"token\":{\"denom\":\"ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5\",\"amount\":\"10996029\"},\"sender\":\"dydx1nhzuazjhyfu474er6v4ey8zn6wa5fy6g2dgp7s\",\"receiver\":\"noble1nhzuazjhyfu474er6v4ey8zn6wa5fy6gthndxf\",\"timeout_height\":{},\"timeout_timestamp\":1718308711061386287}",
                "msg_type_url": "/ibc.applications.transfer.v1.MsgTransfer"
            }
        }
    ],
    "txs": [
        {
            "cosmos_tx": {
                "chain_id": "dydx-mainnet-1",
                "path": [
                    "dydx-mainnet-1",
                    "noble-1"
                ],
                "msgs": [
                    {
                        "msg": "{\"source_port\":\"transfer\",\"source_channel\":\"channel-0\",\"token\":{\"denom\":\"ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5\",\"amount\":\"10996029\"},\"sender\":\"dydx1nhzuazjhyfu474er6v4ey8zn6wa5fy6g2dgp7s\",\"receiver\":\"noble1nhzuazjhyfu474er6v4ey8zn6wa5fy6gthndxf\",\"timeout_height\":{},\"timeout_timestamp\":1718308711061386287}",
                        "msg_type_url": "/ibc.applications.transfer.v1.MsgTransfer"
                    }
                ],
                "signer_address": "dydx1nhzuazjhyfu474er6v4ey8zn6wa5fy6g2dgp7s"
            },
            "operations_indices": [
                0
            ]
        }
    ],
    "route": {
        "source_asset_denom": "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
        "source_asset_chain_id": "dydx-mainnet-1",
        "dest_asset_denom": "uusdc",
        "dest_asset_chain_id": "noble-1",
        "amount_in": "10996029",
        "amount_out": "10996029",
        "operations": [
            {
                "transfer": {
                    "port": "transfer",
                    "channel": "channel-0",
                    "from_chain_id": "dydx-mainnet-1",
                    "to_chain_id": "noble-1",
                    "pfm_enabled": false,
                    "supports_memo": true,
                    "denom_in": "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
                    "denom_out": "uusdc",
                    "bridge_id": "IBC",
                    "smart_relay": false,
                    "chain_id": "dydx-mainnet-1",
                    "dest_denom": "uusdc"
                },
                "tx_index": 0,
                "amount_in": "10996029",
                "amount_out": "10996029"
            }
        ],
        "chain_ids": [
            "dydx-mainnet-1",
            "noble-1"
        ],
        "does_swap": false,
        "estimated_amount_out": "10996029",
        "swap_venues": [],
        "txs_required": 1,
        "usd_amount_in": "11.01",
        "usd_amount_out": "11.01",
        "estimated_fees": [],
        "required_chain_addresses": [
            "dydx-mainnet-1",
            "noble-1"
        ]
    }
}
    """.trimIndent()

    internal val payloadCCTPNobleToDydx = """
        {
            "msgs": [
                {
                    "multi_chain_msg": {
                        "chain_id": "noble-1",
                        "path": [
                            "noble-1",
                            "dydx-mainnet-1"
                        ],
                        "msg": "{\"source_port\":\"transfer\",\"source_channel\":\"channel-33\",\"token\":{\"denom\":\"uusdc\",\"amount\":\"5884\"},\"sender\":\"noble1nhzuazjhyfu474er6v4ey8zn6wa5fy6gthndxf\",\"receiver\":\"dydx1nhzuazjhyfu474er6v4ey8zn6wa5fy6g2dgp7s\",\"timeout_height\":{},\"timeout_timestamp\":1718318348813666048}",
                        "msg_type_url": "/ibc.applications.transfer.v1.MsgTransfer"
                    }
                }
            ],
            "txs": [
                {
                    "cosmos_tx": {
                        "chain_id": "noble-1",
                        "path": [
                            "noble-1",
                            "dydx-mainnet-1"
                        ],
                        "msgs": [
                            {
                                "msg": "{\"source_port\":\"transfer\",\"source_channel\":\"channel-33\",\"token\":{\"denom\":\"uusdc\",\"amount\":\"5884\"},\"sender\":\"noble1nhzuazjhyfu474er6v4ey8zn6wa5fy6gthndxf\",\"receiver\":\"dydx1nhzuazjhyfu474er6v4ey8zn6wa5fy6g2dgp7s\",\"timeout_height\":{},\"timeout_timestamp\":1718318348813666048}",
                                "msg_type_url": "/ibc.applications.transfer.v1.MsgTransfer"
                            }
                        ],
                        "signer_address": "noble1nhzuazjhyfu474er6v4ey8zn6wa5fy6gthndxf"
                    },
                    "operations_indices": [
                        0
                    ]
                }
            ],
            "route": {
                "source_asset_denom": "uusdc",
                "source_asset_chain_id": "noble-1",
                "dest_asset_denom": "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
                "dest_asset_chain_id": "dydx-mainnet-1",
                "amount_in": "5884",
                "amount_out": "5884",
                "operations": [
                    {
                        "transfer": {
                            "port": "transfer",
                            "channel": "channel-33",
                            "from_chain_id": "noble-1",
                            "to_chain_id": "dydx-mainnet-1",
                            "pfm_enabled": true,
                            "supports_memo": true,
                            "denom_in": "uusdc",
                            "denom_out": "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
                            "fee_amount": "0",
                            "usd_fee_amount": "0.0000",
                            "fee_asset": {
                                "denom": "uusdc",
                                "chain_id": "noble-1",
                                "origin_denom": "uusdc",
                                "origin_chain_id": "noble-1",
                                "trace": "",
                                "is_cw20": false,
                                "is_evm": false,
                                "is_svm": false
                            },
                            "bridge_id": "IBC",
                            "smart_relay": false,
                            "chain_id": "noble-1",
                            "dest_denom": "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5"
                        },
                        "tx_index": 0,
                        "amount_in": "5884",
                        "amount_out": "5884"
                    }
                ],
                "chain_ids": [
                    "noble-1",
                    "dydx-mainnet-1"
                ],
                "does_swap": false,
                "estimated_amount_out": "5884",
                "swap_venues": [],
                "txs_required": 1,
                "usd_amount_in": "0.01",
                "usd_amount_out": "0.01",
                "estimated_fees": [],
                "required_chain_addresses": [
                    "noble-1",
                    "dydx-mainnet-1"
                ]
            }
        }
    """.trimIndent()

    internal val payloadDydxToEth = """
        {
            "msgs": [
                {
                    "multi_chain_msg": {
                        "chain_id": "dydx-mainnet-1",
                        "path": [
                            "dydx-mainnet-1",
                            "noble-1",
                            "osmosis-1",
                            "1"
                        ],
                        "msg": "{\"source_port\":\"transfer\",\"source_channel\":\"channel-0\",\"token\":{\"denom\":\"ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5\",\"amount\":\"129996028\"},\"sender\":\"dydx1nhzuazjhyfu474er6v4ey8zn6wa5fy6g2dgp7s\",\"receiver\":\"noble1nhzuazjhyfu474er6v4ey8zn6wa5fy6gthndxf\",\"timeout_height\":{},\"timeout_timestamp\":1718399715601228463,\"memo\":\"{\\\"forward\\\":{\\\"channel\\\":\\\"channel-1\\\",\\\"next\\\":{\\\"wasm\\\":{\\\"contract\\\":\\\"osmo1vkdakqqg5htq5c3wy2kj2geq536q665xdexrtjuwqckpads2c2nsvhhcyv\\\",\\\"msg\\\":{\\\"swap_and_action\\\":{\\\"affiliates\\\":[],\\\"min_asset\\\":{\\\"native\\\":{\\\"amount\\\":\\\"37656643372307734\\\",\\\"denom\\\":\\\"ibc/EA1D43981D5C9A1C4AAEA9C23BB1D4FA126BA9BC7020A25E0AE4AA841EA25DC5\\\"}},\\\"post_swap_action\\\":{\\\"ibc_transfer\\\":{\\\"ibc_info\\\":{\\\"memo\\\":\\\"{\\\\\\\"destination_chain\\\\\\\":\\\\\\\"Ethereum\\\\\\\",\\\\\\\"destination_address\\\\\\\":\\\\\\\"0xD397883c12b71ea39e0d9f6755030205f31A1c96\\\\\\\",\\\\\\\"payload\\\\\\\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,15,120,51,119,123,252,158,247,45,139,118,174,149,77,56,73,221,113,248,41],\\\\\\\"type\\\\\\\":2,\\\\\\\"fee\\\\\\\":{\\\\\\\"amount\\\\\\\":\\\\\\\"7692677672185391\\\\\\\",\\\\\\\"recipient\\\\\\\":\\\\\\\"axelar1aythygn6z5thymj6tmzfwekzh05ewg3l7d6y89\\\\\\\"}}\\\",\\\"receiver\\\":\\\"axelar1dv4u5k73pzqrxlzujxg3qp8kvc3pje7jtdvu72npnt5zhq05ejcsn5qme5\\\",\\\"recover_address\\\":\\\"osmo1nhzuazjhyfu474er6v4ey8zn6wa5fy6gt044g4\\\",\\\"source_channel\\\":\\\"channel-208\\\"}}},\\\"timeout_timestamp\\\":1718399715601274600,\\\"user_swap\\\":{\\\"swap_exact_asset_in\\\":{\\\"operations\\\":[{\\\"denom_in\\\":\\\"ibc/498A0751C798A0D9A389AA3691123DADA57DAA4FE165D5C75894505B876BA6E4\\\",\\\"denom_out\\\":\\\"factory/osmo1z0qrq605sjgcqpylfl4aa6s90x738j7m58wyatt0tdzflg2ha26q67k743/wbtc\\\",\\\"pool\\\":\\\"1437\\\"},{\\\"denom_in\\\":\\\"factory/osmo1z0qrq605sjgcqpylfl4aa6s90x738j7m58wyatt0tdzflg2ha26q67k743/wbtc\\\",\\\"denom_out\\\":\\\"ibc/EA1D43981D5C9A1C4AAEA9C23BB1D4FA126BA9BC7020A25E0AE4AA841EA25DC5\\\",\\\"pool\\\":\\\"1441\\\"}],\\\"swap_venue_name\\\":\\\"osmosis-poolmanager\\\"}}}}}},\\\"port\\\":\\\"transfer\\\",\\\"receiver\\\":\\\"osmo1vkdakqqg5htq5c3wy2kj2geq536q665xdexrtjuwqckpads2c2nsvhhcyv\\\",\\\"retries\\\":2,\\\"timeout\\\":1718399715601230847}}\"}",
                        "msg_type_url": "/ibc.applications.transfer.v1.MsgTransfer"
                    }
                }
            ],
            "txs": [
                {
                    "cosmos_tx": {
                        "chain_id": "dydx-mainnet-1",
                        "path": [
                            "dydx-mainnet-1",
                            "noble-1",
                            "osmosis-1",
                            "1"
                        ],
                        "msgs": [
                            {
                                "msg": "{\"source_port\":\"transfer\",\"source_channel\":\"channel-0\",\"token\":{\"denom\":\"ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5\",\"amount\":\"129996028\"},\"sender\":\"dydx1nhzuazjhyfu474er6v4ey8zn6wa5fy6g2dgp7s\",\"receiver\":\"noble1nhzuazjhyfu474er6v4ey8zn6wa5fy6gthndxf\",\"timeout_height\":{},\"timeout_timestamp\":1718399715601228463,\"memo\":\"{\\\"forward\\\":{\\\"channel\\\":\\\"channel-1\\\",\\\"next\\\":{\\\"wasm\\\":{\\\"contract\\\":\\\"osmo1vkdakqqg5htq5c3wy2kj2geq536q665xdexrtjuwqckpads2c2nsvhhcyv\\\",\\\"msg\\\":{\\\"swap_and_action\\\":{\\\"affiliates\\\":[],\\\"min_asset\\\":{\\\"native\\\":{\\\"amount\\\":\\\"37656643372307734\\\",\\\"denom\\\":\\\"ibc/EA1D43981D5C9A1C4AAEA9C23BB1D4FA126BA9BC7020A25E0AE4AA841EA25DC5\\\"}},\\\"post_swap_action\\\":{\\\"ibc_transfer\\\":{\\\"ibc_info\\\":{\\\"memo\\\":\\\"{\\\\\\\"destination_chain\\\\\\\":\\\\\\\"Ethereum\\\\\\\",\\\\\\\"destination_address\\\\\\\":\\\\\\\"0xD397883c12b71ea39e0d9f6755030205f31A1c96\\\\\\\",\\\\\\\"payload\\\\\\\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,15,120,51,119,123,252,158,247,45,139,118,174,149,77,56,73,221,113,248,41],\\\\\\\"type\\\\\\\":2,\\\\\\\"fee\\\\\\\":{\\\\\\\"amount\\\\\\\":\\\\\\\"7692677672185391\\\\\\\",\\\\\\\"recipient\\\\\\\":\\\\\\\"axelar1aythygn6z5thymj6tmzfwekzh05ewg3l7d6y89\\\\\\\"}}\\\",\\\"receiver\\\":\\\"axelar1dv4u5k73pzqrxlzujxg3qp8kvc3pje7jtdvu72npnt5zhq05ejcsn5qme5\\\",\\\"recover_address\\\":\\\"osmo1nhzuazjhyfu474er6v4ey8zn6wa5fy6gt044g4\\\",\\\"source_channel\\\":\\\"channel-208\\\"}}},\\\"timeout_timestamp\\\":1718399715601274600,\\\"user_swap\\\":{\\\"swap_exact_asset_in\\\":{\\\"operations\\\":[{\\\"denom_in\\\":\\\"ibc/498A0751C798A0D9A389AA3691123DADA57DAA4FE165D5C75894505B876BA6E4\\\",\\\"denom_out\\\":\\\"factory/osmo1z0qrq605sjgcqpylfl4aa6s90x738j7m58wyatt0tdzflg2ha26q67k743/wbtc\\\",\\\"pool\\\":\\\"1437\\\"},{\\\"denom_in\\\":\\\"factory/osmo1z0qrq605sjgcqpylfl4aa6s90x738j7m58wyatt0tdzflg2ha26q67k743/wbtc\\\",\\\"denom_out\\\":\\\"ibc/EA1D43981D5C9A1C4AAEA9C23BB1D4FA126BA9BC7020A25E0AE4AA841EA25DC5\\\",\\\"pool\\\":\\\"1441\\\"}],\\\"swap_venue_name\\\":\\\"osmosis-poolmanager\\\"}}}}}},\\\"port\\\":\\\"transfer\\\",\\\"receiver\\\":\\\"osmo1vkdakqqg5htq5c3wy2kj2geq536q665xdexrtjuwqckpads2c2nsvhhcyv\\\",\\\"retries\\\":2,\\\"timeout\\\":1718399715601230847}}\"}",
                                "msg_type_url": "/ibc.applications.transfer.v1.MsgTransfer"
                            }
                        ],
                        "signer_address": "dydx1nhzuazjhyfu474er6v4ey8zn6wa5fy6g2dgp7s"
                    },
                    "operations_indices": [
                        0,
                        1,
                        2,
                        3
                    ]
                }
            ],
            "route": {
                "source_asset_denom": "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
                "source_asset_chain_id": "dydx-mainnet-1",
                "dest_asset_denom": "ethereum-native",
                "dest_asset_chain_id": "1",
                "amount_in": "129996028",
                "amount_out": "30344335835196158",
                "operations": [
                    {
                        "transfer": {
                            "port": "transfer",
                            "channel": "channel-0",
                            "from_chain_id": "dydx-mainnet-1",
                            "to_chain_id": "noble-1",
                            "pfm_enabled": false,
                            "supports_memo": true,
                            "denom_in": "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
                            "denom_out": "uusdc",
                            "bridge_id": "IBC",
                            "smart_relay": false,
                            "chain_id": "dydx-mainnet-1",
                            "dest_denom": "uusdc"
                        },
                        "tx_index": 0,
                        "amount_in": "129996028",
                        "amount_out": "129996028"
                    },
                    {
                        "transfer": {
                            "port": "transfer",
                            "channel": "channel-1",
                            "from_chain_id": "noble-1",
                            "to_chain_id": "osmosis-1",
                            "pfm_enabled": true,
                            "supports_memo": true,
                            "denom_in": "uusdc",
                            "denom_out": "ibc/498A0751C798A0D9A389AA3691123DADA57DAA4FE165D5C75894505B876BA6E4",
                            "fee_amount": "0",
                            "usd_fee_amount": "0.0000",
                            "fee_asset": {
                                "denom": "uusdc",
                                "chain_id": "noble-1",
                                "origin_denom": "uusdc",
                                "origin_chain_id": "noble-1",
                                "trace": "",
                                "is_cw20": false,
                                "is_evm": false,
                                "is_svm": false
                            },
                            "bridge_id": "IBC",
                            "smart_relay": false,
                            "chain_id": "noble-1",
                            "dest_denom": "ibc/498A0751C798A0D9A389AA3691123DADA57DAA4FE165D5C75894505B876BA6E4"
                        },
                        "tx_index": 0,
                        "amount_in": "129996028",
                        "amount_out": "129996028"
                    },
                    {
                        "swap": {
                            "swap_in": {
                                "swap_venue": {
                                    "name": "osmosis-poolmanager",
                                    "chain_id": "osmosis-1",
                                    "logo_uri": "https://raw.githubusercontent.com/skip-mev/skip-api-registry/main/swap-venues/osmosis/logo.png"
                                },
                                "swap_operations": [
                                    {
                                        "pool": "1437",
                                        "denom_in": "ibc/498A0751C798A0D9A389AA3691123DADA57DAA4FE165D5C75894505B876BA6E4",
                                        "denom_out": "factory/osmo1z0qrq605sjgcqpylfl4aa6s90x738j7m58wyatt0tdzflg2ha26q67k743/wbtc"
                                    },
                                    {
                                        "pool": "1441",
                                        "denom_in": "factory/osmo1z0qrq605sjgcqpylfl4aa6s90x738j7m58wyatt0tdzflg2ha26q67k743/wbtc",
                                        "denom_out": "ibc/EA1D43981D5C9A1C4AAEA9C23BB1D4FA126BA9BC7020A25E0AE4AA841EA25DC5"
                                    }
                                ],
                                "swap_amount_in": "129996028",
                                "price_impact_percent": "0.2607"
                            },
                            "estimated_affiliate_fee": "0ibc/EA1D43981D5C9A1C4AAEA9C23BB1D4FA126BA9BC7020A25E0AE4AA841EA25DC5",
                            "from_chain_id": "osmosis-1",
                            "chain_id": "osmosis-1",
                            "denom_in": "ibc/498A0751C798A0D9A389AA3691123DADA57DAA4FE165D5C75894505B876BA6E4",
                            "denom_out": "ibc/EA1D43981D5C9A1C4AAEA9C23BB1D4FA126BA9BC7020A25E0AE4AA841EA25DC5",
                            "swap_venues": [
                                {
                                    "name": "osmosis-poolmanager",
                                    "chain_id": "osmosis-1",
                                    "logo_uri": "https://raw.githubusercontent.com/skip-mev/skip-api-registry/main/swap-venues/osmosis/logo.png"
                                }
                            ]
                        },
                        "tx_index": 0,
                        "amount_in": "129996028",
                        "amount_out": "38037013507381549"
                    },
                    {
                        "axelar_transfer": {
                            "from_chain": "osmosis",
                            "from_chain_id": "osmosis-1",
                            "to_chain": "Ethereum",
                            "to_chain_id": "1",
                            "asset": "weth-wei",
                            "should_unwrap": true,
                            "denom_in": "weth-wei",
                            "denom_out": "ethereum-native",
                            "fee_amount": "7692677672185391",
                            "usd_fee_amount": "26.15",
                            "fee_asset": {
                                "denom": "ethereum-native",
                                "chain_id": "1",
                                "origin_denom": "",
                                "origin_chain_id": "",
                                "trace": "",
                                "is_cw20": false,
                                "is_evm": true,
                                "is_svm": false,
                                "symbol": "ETH",
                                "name": "Ethereum",
                                "logo_uri": "https://raw.githubusercontent.com/cosmos/chain-registry/master/_non-cosmos/ethereum/images/eth-blue.svg",
                                "decimals": 18,
                                "token_contract": ""
                            },
                            "is_testnet": false,
                            "ibc_transfer_to_axelar": {
                                "port": "transfer",
                                "channel": "channel-208",
                                "from_chain_id": "osmosis-1",
                                "to_chain_id": "axelar-dojo-1",
                                "pfm_enabled": true,
                                "supports_memo": true,
                                "denom_in": "ibc/EA1D43981D5C9A1C4AAEA9C23BB1D4FA126BA9BC7020A25E0AE4AA841EA25DC5",
                                "denom_out": "weth-wei",
                                "bridge_id": "IBC",
                                "smart_relay": false,
                                "chain_id": "osmosis-1",
                                "dest_denom": "weth-wei"
                            },
                            "bridge_id": "AXELAR",
                            "smart_relay": false
                        },
                        "tx_index": 0,
                        "amount_in": "38037013507381549",
                        "amount_out": "30344335835196158"
                    }
                ],
                "chain_ids": [
                    "dydx-mainnet-1",
                    "noble-1",
                    "osmosis-1",
                    "1"
                ],
                "does_swap": true,
                "estimated_amount_out": "30344335835196158",
                "swap_venues": [
                    {
                        "name": "osmosis-poolmanager",
                        "chain_id": "osmosis-1",
                        "logo_uri": "https://raw.githubusercontent.com/skip-mev/skip-api-registry/main/swap-venues/osmosis/logo.png"
                    }
                ],
                "txs_required": 1,
                "usd_amount_in": "130.13",
                "usd_amount_out": "103.17",
                "swap_price_impact_percent": "0.2607",
                "warning": {
                    "type": "BAD_PRICE_WARNING",
                    "message": "Difference in USD value of route input and output is large. Input USD value: 130.13 Output USD value: 103.17"
                },
                "estimated_fees": [],
                "required_chain_addresses": [
                    "dydx-mainnet-1",
                    "noble-1",
                    "osmosis-1",
                    "1"
                ],
                "swap_venue": {
                    "name": "osmosis-poolmanager",
                    "chain_id": "osmosis-1",
                    "logo_uri": "https://raw.githubusercontent.com/skip-mev/skip-api-registry/main/swap-venues/osmosis/logo.png"
                }
            }
        }
    """.trimIndent()

    internal val payloadNobleToUSDCEthWithdrawal = """
        {
            "msgs": [],
            "txs": [
                {
                    "cosmos_tx": {
                        "chain_id": "noble-1",
                        "path": [
                            "noble-1",
                            "1"
                        ],
                        "msgs": [
                            {
                                "msg": "{\"from\":\"noble1nhzuazjhyfu474er6v4ey8zn6wa5fy6gthndxf\",\"amount\":\"59995433\",\"destination_domain\":0,\"mint_recipient\":\"AAAAAAAAAAAAAAAAD3gzd3v8nvcti3aulU04Sd1x+Ck=\",\"burn_token\":\"uusdc\",\"destination_caller\":\"AAAAAAAAAAAAAAAA/AWtdMb+LnBG4JHWrU9mDSoVl2I=\"}",
                                "msg_type_url": "/circle.cctp.v1.MsgDepositForBurnWithCaller"
                            },
                            {
                                "msg": "{\"from_address\":\"noble1nhzuazjhyfu474er6v4ey8zn6wa5fy6gthndxf\",\"to_address\":\"noble1dyw0geqa2cy0ppdjcxfpzusjpwmq85r5a35hqe\",\"amount\":[{\"denom\":\"uusdc\",\"amount\":\"40000000\"}]}",
                                "msg_type_url": "/cosmos.bank.v1beta1.MsgSend"
                            }
                        ],
                        "signer_address": "noble1nhzuazjhyfu474er6v4ey8zn6wa5fy6gthndxf"
                    },
                    "operations_indices": [
                        0
                    ]
                }
            ],
            "route": {
                "source_asset_denom": "uusdc",
                "source_asset_chain_id": "noble-1",
                "dest_asset_denom": "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
                "dest_asset_chain_id": "1",
                "amount_in": "99995433",
                "amount_out": "59995433",
                "operations": [
                    {
                        "cctp_transfer": {
                            "from_chain_id": "noble-1",
                            "to_chain_id": "1",
                            "burn_token": "uusdc",
                            "denom_in": "uusdc",
                            "denom_out": "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
                            "bridge_id": "CCTP",
                            "smart_relay": true
                        },
                        "tx_index": 0,
                        "amount_in": "99995433",
                        "amount_out": "59995433"
                    }
                ],
                "chain_ids": [
                    "noble-1",
                    "1"
                ],
                "does_swap": false,
                "estimated_amount_out": "59995433",
                "swap_venues": [],
                "txs_required": 1,
                "usd_amount_in": "99.99",
                "usd_amount_out": "59.99",
                "warning": {
                    "type": "BAD_PRICE_WARNING",
                    "message": "Difference in USD value of route input and output is large. Input USD value: 99.99 Output USD value: 59.99"
                },
                "estimated_fees": [
                    {
                        "fee_type": "SMART_RELAY",
                        "bridge_id": "CCTP",
                        "amount": "40000000",
                        "usd_amount": "40.00",
                        "origin_asset": {
                            "denom": "uusdc",
                            "chain_id": "noble-1",
                            "origin_denom": "uusdc",
                            "origin_chain_id": "noble-1",
                            "trace": "",
                            "is_cw20": false,
                            "is_evm": false,
                            "is_svm": false,
                            "symbol": "USDC",
                            "name": "USDC",
                            "logo_uri": "https://raw.githubusercontent.com/cosmos/chain-registry/master/noble/images/USDCoin.png",
                            "decimals": 6,
                            "description": "USD Coin",
                            "coingecko_id": "usd-coin",
                            "recommended_symbol": "USDC"
                        },
                        "chain_id": "noble-1",
                        "tx_index": 0
                    }
                ],
                "required_chain_addresses": [
                    "noble-1",
                    "1"
                ]
            }
        }
    """.trimIndent()

    internal val payloadCCTPSolanaToNoble = """
        {
            "msgs": [
                {
                    "svm_tx": {
                        "chain_id": "solana",
                        "tx": "mock-encoded-solana-tx",
                        "signer_address": "26qv4GCcx98RihuK3c4T6ozB3J7L6VwCuFVc7Ta2A3Uo"
                    }
                }
            ],
            "txs": [
                {
                    "svm_tx": {
                        "chain_id": "solana",
                        "tx": "mock-encoded-solana-tx",
                        "signer_address": "26qv4GCcx98RihuK3c4T6ozB3J7L6VwCuFVc7Ta2A3Uo"
                    },
                    "operations_indices": [
                        0
                    ]
                }
            ],
            "route": {
                "source_asset_denom": "98bVPZQCHZmCt9v3ni9kwtjKgLuzHBpstQkdPyAucBNx",
                "source_asset_chain_id": "solana",
                "dest_asset_denom": "uusdc",
                "dest_asset_chain_id": "noble-1",
                "amount_in": "1500000000",
                "amount_out": "1499800000",
                "operations": [
                    {
                        "cctp_transfer": {
                            "from_chain_id": "solana",
                            "to_chain_id": "noble-1",
                            "burn_token": "98bVPZQCHZmCt9v3ni9kwtjKgLuzHBpstQkdPyAucBNx",
                            "denom_in": "98bVPZQCHZmCt9v3ni9kwtjKgLuzHBpstQkdPyAucBNx",
                            "denom_out": "uusdc",
                            "bridge_id": "CCTP",
                            "smart_relay": true
                        },
                        "tx_index": 0,
                        "amount_in": "1500000000",
                        "amount_out": "1499800000"
                    }
                ],
                "chain_ids": [
                    "solana",
                    "noble-1"
                ],
                "does_swap": false,
                "estimated_amount_out": "1499800000",
                "swap_venues": [],
                "txs_required": 1,
                "usd_amount_in": "1498.38",
                "usd_amount_out": "1498.18",
                "estimated_fees": [
                    {
                        "fee_type": "SMART_RELAY",
                        "bridge_id": "CCTP",
                        "amount": "200000",
                        "usd_amount": "0.20",
                        "origin_asset": {
                            "denom": "98bVPZQCHZmCt9v3ni9kwtjKgLuzHBpstQkdPyAucBNx",
                            "chain_id": "solana",
                            "origin_denom": "98bVPZQCHZmCt9v3ni9kwtjKgLuzHBpstQkdPyAucBNx",
                            "origin_chain_id": "solana",
                            "trace": "",
                            "is_cw20": false,
                            "is_evm": false,
                            "is_svm": false,
                            "symbol": "USDC",
                            "name": "USD Coin",
                            "logo_uri": "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/98bVPZQCHZmCt9v3ni9kwtjKgLuzHBpstQkdPyAucBNx/logo.png",
                            "decimals": 6,
                            "coingecko_id": "usd-coin",
                            "recommended_symbol": "USDC"
                        },
                        "chain_id": "solana",
                        "tx_index": 0
                    }
                ],
                "required_chain_addresses": [
                    "solana",
                    "noble-1"
                ],
                "estimated_route_duration_seconds": 25
            }
        }
    """.trimIndent()

    internal val payloadError = """
    {
  "code": 3,
  "message": "difference in usd value of route input and output is too large. input usd value: 100000.00 output usd value: 98811.81",
  "details": [
    {
      "@type": "type.googleapis.com/google.rpc.ErrorInfo",
      "reason": "BAD_PRICE_ERROR",
      "domain": "skip.money",
      "metadata": {}
    }
  ]
}"""
}
