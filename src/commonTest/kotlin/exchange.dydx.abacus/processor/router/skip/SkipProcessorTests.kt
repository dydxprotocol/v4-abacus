package exchange.dydx.abacus.processor.router.skip
import exchange.dydx.abacus.utils.Parser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

internal fun templateToJson(template: String): Map<String, Any> {
    return Json.parseToJsonElement(template.trimIndent()).jsonObject.toMap()
}

internal fun getSkipProcessor(): SkipProcessor {
    return SkipProcessor(Parser())
}

class SkipProcessorTests {

    @Test
    fun testReceivedChains() {
        val skipProcessor = getSkipProcessor()
        val modified = skipProcessor.receivedChains(
            existing = mapOf(),
            payload = templateToJson(
                """{
    "chains": [
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
        }
    ]
}""",
            ),
        )

        val expected = mapOf(
            "transfer" to mapOf(
                "depositOptions" to mapOf(
                    "chains" to listOf(
                        mapOf("stringKey" to "aura", "type" to "xstaxy-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/xstaxy/chain.png"),
                        mapOf("stringKey" to "cheqd", "type" to "cheqd-mainnet-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/cheqd-mainnet/chain.png"),
                        mapOf("stringKey" to "kujira", "type" to "kaiyo-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/kaiyo/chain.png"),
                        mapOf("stringKey" to "osmosis", "type" to "osmosis-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/osmosis/chain.png"),
                        mapOf("stringKey" to "stride", "type" to "stride-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/stride/chain.png"),
                    ),
                ),
                "withdrawalOptions" to mapOf(
                    "chains" to listOf(
                        mapOf("stringKey" to "aura", "type" to "xstaxy-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/xstaxy/chain.png"),
                        mapOf("stringKey" to "cheqd", "type" to "cheqd-mainnet-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/cheqd-mainnet/chain.png"),
                        mapOf("stringKey" to "kujira", "type" to "kaiyo-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/kaiyo/chain.png"),
                        mapOf("stringKey" to "osmosis", "type" to "osmosis-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/osmosis/chain.png"),
                        mapOf("stringKey" to "stride", "type" to "stride-1", "iconUrl" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/stride/chain.png"),
                    ),
                ),
            ),
        )
        assertEquals(modified, expected)
    }
}
