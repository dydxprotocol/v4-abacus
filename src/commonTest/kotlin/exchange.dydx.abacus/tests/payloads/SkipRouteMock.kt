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
