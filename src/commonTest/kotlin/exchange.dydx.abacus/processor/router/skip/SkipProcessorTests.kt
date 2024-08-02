package exchange.dydx.abacus.processor.router.skip
import RpcConfigsProcessor
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.TransferInputChainResource
import exchange.dydx.abacus.output.input.TransferInputTokenResource
import exchange.dydx.abacus.state.internalstate.InternalTransferInputState
import exchange.dydx.abacus.state.manager.RpcConfigs
import exchange.dydx.abacus.tests.payloads.RpcMock
import exchange.dydx.abacus.tests.payloads.SkipChainsMock
import exchange.dydx.abacus.tests.payloads.SkipRouteMock
import exchange.dydx.abacus.tests.payloads.SkipTokensMock
import exchange.dydx.abacus.utils.DEFAULT_GAS_LIMIT
import exchange.dydx.abacus.utils.DEFAULT_GAS_PRICE
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.toJsonObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal fun templateToMap(template: String): Map<String, Any> {
    return Json.parseToJsonElement(template.trimIndent()).jsonObject.toMap()
}

class SkipProcessorTests {

    internal val internalState = InternalTransferInputState()
    internal val parser = Parser()
    internal val skipProcessor = SkipProcessor(parser = parser, internalState = internalState)
    internal val skipChainsMock = SkipChainsMock()
    internal val skipTokensMock = SkipTokensMock()
    internal val skipRouteMock = SkipRouteMock()
    internal val selectedChainId = "osmosis-1"
    internal val selectedTokenAddress = "selectedTokenDenom"
    internal val selectedTokenSkipDenom = "selected-token-denom-native"
    internal val selectedTokenSymbol = "selectedTokenSymbol"
    internal val selectedTokenDecimals = "15"
    internal val selectedChainAssets = listOf(
        mapOf(
            "denom" to selectedTokenAddress,
            "symbol" to selectedTokenSymbol,
            "decimals" to selectedTokenDecimals,
            "skipDenom" to selectedTokenSkipDenom,
            "name" to "some-name",
            "logo_uri" to "some-logo-uri",
        ),
        mapOf(
            "denom" to "testTokenKeyValue2",
            "symbol" to "ARB",
            "decimals" to 8,
            "name" to "some-name-2",
            "logo_uri" to "some-logo-uri-2",
        ),
        mapOf(
            "denom" to "testTokenKeyValue3",
            "symbol" to "ETH",
            "decimals" to 5,
            "name" to "some-name-3",
            "logo_uri" to "some-logo-uri-3",
        ),
    )

    /**
     * Adds tokens to the skipProcessor instance
     * This is a reduced scope mock that is to be used for UNIT TESTS ONLY.
     * Integration tests should use the skipChainsMock or skipTokensMock structures.
     * The test tokens fixture looks like this:
     * {
     *      "osmosis-1": {
     *          "assets": [
     *              {
     *                  "denom": "selectedTokenDenom",
     *                  "symbol": "selectedTokenSymbol",
     *                  "decimals": 15
     *              },
     *              {
     *                  "denom": "testTokenKeyValue2",
     *                  "symbol": "ARB",
     *                  "decimals": 8
     *              },
     *              {
     *                  "denom": "testTokenKeyValue3"
     *                  "symbol": "ETH",
     *                  "decimals": 5
     *              }
     *          ]
     *      },
     *      "dont-select": {
     *          "assets": [
     *              {"denom": "shouldNotBeSelectedValue1"},
     *              {"denom": "shouldNotBeSelectedValue2"},
     *              {"denom": "shouldNotBeSelectedValue3"}
     *          ]
     *      }
     * }
     *
     * This makes it easy to know what the filteredTokens output should be
     * which in turn helps us know the results of the funs that depend on it.
     */
    internal fun addTokens() {
        skipProcessor.skipTokens = mapOf(
            selectedChainId to mapOf("assets" to selectedChainAssets),
            "dont-select" to mapOf(
                "assets" to listOf(
                    mapOf("shouldNotBeSelected1" to "shouldNotBeSelectedValue1"),
                    mapOf("shouldNotBeSelected2" to "shouldNotBeSelectedValue2"),
                    mapOf("shouldNotBeSelected3" to "shouldNotBeSelectedValue3"),
                ),
            ),
        )
    }

    @BeforeTest
    internal fun setUp() {
        addTokens()
    }

// //////////////////    UNIT TESTS     //////////////////////
    @Test
    fun testFilteredTokens() {
        val result = skipProcessor.filteredTokens(chainId = selectedChainId)
        val expected = selectedChainAssets
        assertEquals(expected, result)
    }

    @Test
    fun testSelectedTokenSymbol() {
        val result = skipProcessor.selectedTokenSymbol(tokenAddress = selectedTokenAddress, selectedChainId = selectedChainId)
        val expected = selectedTokenSymbol
        assertEquals(expected, result)
    }

    @Test
    fun testSelectedTokenDecimals() {
        val result = skipProcessor.selectedTokenDecimals(tokenAddress = selectedTokenSkipDenom, selectedChainId = selectedChainId)
        val expected = selectedTokenDecimals
        assertEquals(expected, result)
    }

    @Test
    fun testSelectedTokenDecimalsUsingSkipDenom() {
        val result = skipProcessor.selectedTokenDecimals(tokenAddress = selectedTokenAddress, selectedChainId = selectedChainId)
        val expected = selectedTokenDecimals
        assertEquals(expected, result)
    }

    @Test
    fun testDefaultTokenAddress() {
        val result = skipProcessor.defaultTokenAddress(selectedChainId)
        val expected = selectedTokenAddress
        assertEquals(expected, result)
    }

    @Test
    fun testTokenResources() {
        val result = skipProcessor.tokenResources(selectedChainId)
        val expected = mapOf(
            selectedTokenAddress to TransferInputTokenResource(
                address = selectedTokenAddress,
                symbol = selectedTokenSymbol,
                decimals = parser.asInt(selectedTokenDecimals),
                name = "some-name",
                iconUrl = "some-logo-uri",
            ),
            "testTokenKeyValue2" to TransferInputTokenResource(
                address = "testTokenKeyValue2",
                symbol = "ARB",
                decimals = 8,
                name = "some-name-2",
                iconUrl = "some-logo-uri-2",
            ),
            "testTokenKeyValue3" to TransferInputTokenResource(
                address = "testTokenKeyValue3",
                symbol = "ETH",
                decimals = 5,
                name = "some-name-3",
                iconUrl = "some-logo-uri-3",
            ),
        )
        assertEquals(expected, result)
    }

    @Test
    fun testTokenOptions() {
        val result = skipProcessor.tokenOptions(selectedChainId)
        val expected = listOf(
            SelectionOption(
                stringKey = "some-name",
                string = "some-name",
                type = selectedTokenAddress,
                iconUrl = "some-logo-uri",
            ),
            SelectionOption(
                stringKey = "some-name-2",
                string = "some-name-2",
                type = "testTokenKeyValue2",
                iconUrl = "some-logo-uri-2",
            ),
            SelectionOption(
                stringKey = "some-name-3",
                string = "some-name-3",
                type = "testTokenKeyValue3",
                iconUrl = "some-logo-uri-3",
            ),
        )
        assertEquals(expected, result)
    }

    // /////////////// INTEGRATION TESTS ////////////////////

    @Test
    fun testReceivedChains() {
        val testApiKey = "testApiKey"
        assertEquals(0, RpcConfigs.chainRpcMap.size)
        RpcConfigs.chainRpcMap = RpcConfigsProcessor(parser, testApiKey).received(RpcMock.json)
        assertEquals(276, RpcConfigs.chainRpcMap.size)

        val payload = templateToMap(
            skipChainsMock.payload,
        )
        val modified = skipProcessor.receivedChains(
            existing = mapOf(),
            payload = payload,
        )
        val expectedChains = listOf(
            SelectionOption(stringKey = "Arbitrum", string = "Arbitrum", type = "42161", iconUrl = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/arbitrum/info/logo.png"),
            SelectionOption(stringKey = "Ethereum", string = "Ethereum", type = "1", iconUrl = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/info/logo.png"),
            SelectionOption(type = "solana", string = "Solana", stringKey = "Solana", iconUrl = "https://raw.githubusercontent.com/skip-mev/skip-go-registry/main/chains/solana/logo.svg"),
        )
        val expectedChainResources = mapOf(
            "1" to TransferInputChainResource(
                chainName = "Ethereum",
                chainId = 1,
                rpc = "https://eth-mainnet.g.alchemy.com/v2/$testApiKey",
                iconUrl = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/info/logo.png",
            ),
        )
        val expectedModified = mapOf(
            "transfer" to mapOf(
                "depositOptions" to mapOf(
                    "chains" to expectedChains,
                ),
                "withdrawalOptions" to mapOf(
                    "chains" to expectedChains,
                ),
                "chain" to "1",
            ),
        )

        assertEquals(expectedChains, internalState.chains)
        assertEquals(payload["chains"], skipProcessor.chains)
        assertEquals(expectedChainResources, internalState.chainResources)
        assertEquals(expectedModified, modified)
    }

    @Test
    fun testReceivedTokens() {
        val payload = templateToMap(skipTokensMock.payload)
        skipProcessor.skipTokens = null
        skipProcessor.chains = listOf(
            mapOf(
                "chain_name" to "aura",
                "chain_id" to "1",
                "pfm_enabled" to false,
                "supports_memo" to true,
                "logo_uri" to "https ://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/xstaxy/chain.png",
                "bech32_prefix" to "aura",
                "chain_type" to "cosmos",
                "is_testnet" to false,
            ),
        )
        val modified = skipProcessor.receivedTokens(
            existing = mapOf(),
            payload = payload,
        )
        val expectedTokens = listOf(
            SelectionOption(
                stringKey = "ZEthereum",
                string = "ZEthereum",
                type = "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE",
                iconUrl = "https://raw.githubusercontent.com/cosmos/chain-registry/master/_non-cosmos/ethereum/images/eth-blue.svg",
            ),
            SelectionOption(
                stringKey = "Euro Coin",
                string = "Euro Coin",
                type = "0x1aBaEA1f7C830bD89Acc67eC4af516284b1bC33c",
                iconUrl = "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/euroc.svg",
            ),
            SelectionOption(
                stringKey = "Real Yield USD",
                string = "Real Yield USD",
                type = "0x97e6E0a40a3D02F12d1cEC30ebfbAE04e37C119E",
                iconUrl = "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/yieldusd.svg",
            ),
            SelectionOption(
                stringKey = "Umee native token",
                string = "Umee native token",
                type = "0x923e030f951A2401426a3407a9bcc7EB715d9a0b",
                iconUrl = "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/umee.svg",
            ),
        )
        val expectedModified = mapOf(
            "transfer" to mapOf(
                "token" to "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE",
                "depositOptions" to mapOf(
                    "tokens" to expectedTokens,
                ),
                "withdrawalOptions" to mapOf(
                    "tokens" to expectedTokens,
                ),
            ),
        )
        val expectedTokenResources = mapOf(
            "0x97e6E0a40a3D02F12d1cEC30ebfbAE04e37C119E" to TransferInputTokenResource(
                name = "Real Yield USD",
                address = "0x97e6E0a40a3D02F12d1cEC30ebfbAE04e37C119E",
                symbol = "YieldUSD",
                decimals = 18,
                iconUrl = "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/yieldusd.svg",
            ),
            "0x1aBaEA1f7C830bD89Acc67eC4af516284b1bC33c" to TransferInputTokenResource(
                name = "Euro Coin",
                address = "0x1aBaEA1f7C830bD89Acc67eC4af516284b1bC33c",
                symbol = "EUROC",
                decimals = 6,
                iconUrl = "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/euroc.svg",
            ),
            "0x923e030f951A2401426a3407a9bcc7EB715d9a0b" to TransferInputTokenResource(
                name = "Umee native token",
                address = "0x923e030f951A2401426a3407a9bcc7EB715d9a0b",
                symbol = "UMEE",
                decimals = 6,
                iconUrl = "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/umee.svg",
            ),
            "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE" to TransferInputTokenResource(
                name = "ZEthereum",
                address = "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE",
                symbol = "ETH",
                decimals = 18,
                iconUrl = "https://raw.githubusercontent.com/cosmos/chain-registry/master/_non-cosmos/ethereum/images/eth-blue.svg",
            ),
        )

        assertEquals(expectedModified, modified)
        assertEquals(payload["chain_to_assets_map"], skipProcessor.skipTokens)
        assertEquals(expectedTokens, internalState.tokens)
        assertEquals(expectedTokenResources, internalState.tokenResources)
    }

    @Test
    fun testReceivedRoute() {
        val payload = templateToMap(skipRouteMock.payload)
        val result = skipProcessor.receivedRoute(
            existing = mapOf(),
            payload = payload,
            requestId = null,
        )
        val expected = mapOf(
            "transfer" to mapOf(
                "route" to mapOf(
//                    TODO: set up text properly so we get a decimals value
                    "toAmountUSD" to 11.64,
                    "toAmount" to "11640000",
                    "bridgeFee" to 0.36,
                    "slippage" to "1",
                    "requestPayload" to mapOf(
                        "targetAddress" to "0xBC8552339dA68EB65C8b88B414B5854E0E366cFc",
                        "data" to "0xd77d6ec00000000000000000000000000000000000000000000000000000000000b19cc000000000000000000000000000000000000000000000000000000000000000040000000000000000000000009dc5ce8a5722795f5723d32b921c53d3bb449348000000000000000000000000a0b86991c6218b36c1d19d4a2e9eb0ce3606eb480000000000000000000000000000000000000000000000000000000000057e40000000000000000000000000691cf4641d5608f085b2c1921172120bb603d074",
                        "value" to "0",
                        "fromChainId" to "1",
                        "fromAddress" to "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
                        "toChainId" to "noble-1",
                        "toAddress" to "uusdc",
                        "gasPrice" to DEFAULT_GAS_PRICE,
                        "gasLimit" to DEFAULT_GAS_LIMIT,
                    ),
                ),
            ),
        )
        assertEquals(expected, result)
    }

    @Test
    fun testGetChainById() {
        skipProcessor.chains = parser.asNativeList(parser.asNativeMap(templateToMap(skipChainsMock.payload))?.get("chains"))
        val result = skipProcessor.getChainById("kaiyo-1")
        val expected = mapOf(
            "chain_name" to "kujira",
            "chain_id" to "kaiyo-1",
            "pfm_enabled" to false,
            "cosmos_module_support" to mapOf(
                "authz" to true,
                "feegrant" to true,
            ),
            "supports_memo" to true,
            "logo_uri" to "https://raw.githubusercontent.com/chainapsis/keplr-chain-registry/main/images/kaiyo/chain.png",
            "bech32_prefix" to "kujira",
            "fee_assets" to listOf(
                mapOf(
                    "denom" to "ibc/47BD209179859CDE4A2806763D7189B6E6FE13A17880FE2B42DE1E6C1E329E23",
                    "gas_price" to null,
                ),
                mapOf(
                    "denom" to "ibc/EFF323CC632EC4F747C61BCE238A758EFDB7699C3226565F7C20DA06509D59A5",
                    "gas_price" to null,
                ),
            ),
            "chain_type" to "cosmos",
            "ibc_capabilities" to mapOf(
                "cosmos_pfm" to false,
                "cosmos_ibc_hooks" to false,
                "cosmos_memo" to true,
                "cosmos_autopilot" to false,
            ),
            "is_testnet" to false,
        ).toJsonObject()
        assertEquals(expected, result)
    }
}
