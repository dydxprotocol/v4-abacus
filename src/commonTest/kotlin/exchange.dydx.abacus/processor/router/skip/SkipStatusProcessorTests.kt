package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.tests.payloads.SkipStatusMock
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.toJsonPrettyPrint
import kotlin.test.Test
import kotlin.test.assertEquals

class SkipStatusProcessorTests {
    val parser = Parser()
    internal val processor = SkipStatusProcessor(parser = parser, transactionId = null)
    internal val mock = SkipStatusMock()

// ------------------------------------ NON CCTP DEPOSIT TESTS -------------------------------------
    /**
     * Transfer Route: Pending Non CCTP Deposit from ETH on ETH
     * Tests we generate same state as analogous squid status request
     */
    @Test
    fun testReceivedDepositFromEthEthSubmitted() {
        val payload = templateToMap(template = mock.depositFromEthEthSubmitted)
        val result = processor.received(existing = mapOf(), payload = payload)
        val expected = mapOf<String, Any>()
        assertEquals(expected.toJsonPrettyPrint(), result.toJsonPrettyPrint())
    }

    /**
     * Transfer Route: Pending Non CCTP Deposit from ETH on ETH
     * Tests we generate same state as analogous squid status request
     */
    @Test
    fun testReceivedDepositFromEthEthPending() {
        val payload = templateToMap(template = mock.depositFromEthEthPending)
        val result = processor.received(existing = mapOf(), payload = payload)
        val expected = mapOf(
            "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c" to mapOf(
                "squidTransactionStatus" to "ongoing",
                "axelarTransactionUrl" to "https://axelarscan.io/gmp/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                "fromChainStatus" to mapOf(
                    "transactionId" to "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                    "transactionUrl" to "https://etherscan.io/tx/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                ),
                "toChainStatus" to mapOf(
                    "transactionId" to null,
                    "transactionUrl" to null,
                ),
                "routeStatuses" to listOf(
                    mapOf(
                        "chainId" to "1",
                        "txHash" to "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                        "status" to "success",
                    ),
                    mapOf(
                        "chainId" to null,
                        "txHash" to null,
                        "status" to "ongoing",
                    ),
                ),
            ),
        )
        assertEquals(expected.toJsonPrettyPrint(), result.toJsonPrettyPrint())
    }

    /**
     * Route: Successfully completed Non CCTP Deposit from ETH on ETH
     * Tests we generate same state as analogous squid status request
     */
    @Test
    fun testReceivedDepositFromEthEthSuccess() {
        val payload = templateToMap(template = mock.depositFromEthEthSuccess)
        val result = processor.received(existing = mapOf(), payload = payload)
        val expected = mapOf(
            "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c" to mapOf(
                "squidTransactionStatus" to "success",
                "axelarTransactionUrl" to "https://axelarscan.io/gmp/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                "fromChainStatus" to mapOf(
                    "transactionId" to "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                    "transactionUrl" to "https://etherscan.io/tx/0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                ),
                "toChainStatus" to mapOf(
                    "transactionId" to "1AA3F46507A9E0A1D183625BA5C65D6A9AAC546E346382A33D3D15F064349579",
                    "transactionUrl" to "https://www.mintscan.io/dydx/txs/1AA3F46507A9E0A1D183625BA5C65D6A9AAC546E346382A33D3D15F064349579",
                ),
                "routeStatuses" to listOf(
                    mapOf(
                        "chainId" to "1",
                        "txHash" to "0x6792ba9fa4d1a0316ffabad1541dc4c5ddcd8c4bb38b9a1368f08931f9c14e1c",
                        "status" to "success",
                    ),
                    mapOf(
                        "chainId" to "dydx-mainnet-1",
                        "txHash" to "1AA3F46507A9E0A1D183625BA5C65D6A9AAC546E346382A33D3D15F064349579",
                        "status" to "success",
                    ),
                ),
            ),
        )
        assertEquals(expected.toJsonPrettyPrint(), result.toJsonPrettyPrint())
    }

// ----------------------------------- NON CCTP WITHDRAWAL TESTS -----------------------------------
    /**
     * Tests that payload from a status endpoint request
     * for a just submitted withdrawal to BnB on BSC (Binance Smart Chain)
     * is processed to be as similar as the analogous squid status request
     */
    @Test
    fun testReceivedWithdrawToBnbBSCSubmitted() {
        val payload = templateToMap(template = mock.withdrawToBnbBSCSubmitted)
        val result = processor.received(existing = mapOf(), payload = payload)
        val expected = mapOf<String, Any>()
        assertEquals(expected.toJsonPrettyPrint(), result.toJsonPrettyPrint())
    }

    /**
     * Tests that payload from a status endpoint request
     * for a currently pending withdrawal to BnB on BSC (Binance Smart Chain)
     * is processed to be as similar as the analogous squid status request
     */
    @Test
    fun testReceivedWithdrawToBnbBSCPending() {
        val payload = templateToMap(template = mock.withdrawToBnbBSCPending)
        val result = processor.received(existing = mapOf(), payload = payload)
        val expected = mapOf(
            "7B90D124C9F934E43E7080BB4997854A4C0A0AF4F09A8779A88D0CBDA335D35A" to mapOf(
                "squidTransactionStatus" to "ongoing",
                "axelarTransactionUrl" to "https://axelarscan.io/gmp/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
                "fromChainStatus" to mapOf(
                    "transactionId" to "7B90D124C9F934E43E7080BB4997854A4C0A0AF4F09A8779A88D0CBDA335D35A",
                    "transactionUrl" to "https://www.mintscan.io/dydx/txs/7B90D124C9F934E43E7080BB4997854A4C0A0AF4F09A8779A88D0CBDA335D35A",
                ),
                "toChainStatus" to mapOf(
                    "transactionId" to null,
                    "transactionUrl" to null,
                ),
                "routeStatuses" to listOf(
                    mapOf(
                        "chainId" to "dydx-mainnet-1",
                        "txHash" to "7B90D124C9F934E43E7080BB4997854A4C0A0AF4F09A8779A88D0CBDA335D35A",
                        "status" to "success",
                    ),
                    mapOf(
                        "chainId" to null,
                        "txHash" to null,
                        "status" to "ongoing",
                    ),
                ),
            ),
        )
        assertEquals(expected.toJsonPrettyPrint(), result.toJsonPrettyPrint())
    }

    /**
     * Tests that payload from a status endpoint request
     * for an already successful withdrawal to BnB on BSC (Binance Smart Chain)
     * is processed to be as similar as the analogous squid status request
     */
    @Test
    fun testReceivedWithdrawToBnbBSCSuccess() {
        val expected = mapOf(
            "7B90D124C9F934E43E7080BB4997854A4C0A0AF4F09A8779A88D0CBDA335D35A" to mapOf(
                "squidTransactionStatus" to "success",
                "axelarTransactionUrl" to "https://axelarscan.io/gmp/169170FB7F14E4D9EE4203A332CF29BE90E36E91CFCE5F231EFCD2D99292F974",
                "fromChainStatus" to mapOf(
                    "transactionId" to "7B90D124C9F934E43E7080BB4997854A4C0A0AF4F09A8779A88D0CBDA335D35A",
                    "transactionUrl" to "https://www.mintscan.io/dydx/txs/7B90D124C9F934E43E7080BB4997854A4C0A0AF4F09A8779A88D0CBDA335D35A",
                ),
                "toChainStatus" to mapOf(
                    "transactionId" to "0x5db38d3606bc8bdb487f94fb87feeccf39cf96679f8267899bc70751e08b2edb",
                    "transactionUrl" to "https://bscscan.com/tx/0x5db38d3606bc8bdb487f94fb87feeccf39cf96679f8267899bc70751e08b2edb",
                ),
                "routeStatuses" to listOf(
                    mapOf(
                        "chainId" to "dydx-mainnet-1",
                        "txHash" to "7B90D124C9F934E43E7080BB4997854A4C0A0AF4F09A8779A88D0CBDA335D35A",
                        "status" to "success",
                    ),
                    mapOf(
                        "chainId" to "56",
                        "txHash" to "0x5db38d3606bc8bdb487f94fb87feeccf39cf96679f8267899bc70751e08b2edb",
                        "status" to "success",
                    ),
                ),
            ),
        )
        val payload = templateToMap(template = mock.withdrawToBnbBSCSuccess)
        val result = processor.received(existing = mapOf(), payload = payload)
        assertEquals(expected.toJsonPrettyPrint(), result.toJsonPrettyPrint())
    }

// -----------------------------------    CCTP DEPOSIT TESTS    -----------------------------------
    /**
     * Tests that a payload from a status endpoint request
     * for a pending deposit from USDC on Eth (CCTP deposit)
     * is processed to be as similar as the analogous squid status request
     */
    @Test
    fun testReceivedDepositFromUSDCEthPending() {
        val expected = mapOf(
            "0x092C0BFF67A71D483A3FCC5C0B390E4AA71E5C1CB9CFC1885989734A5975210D" to mapOf(
                "squidTransactionStatus" to "ongoing",
                "axelarTransactionUrl" to null,
                "fromChainStatus" to mapOf(
                    "transactionId" to "0x092C0BFF67A71D483A3FCC5C0B390E4AA71E5C1CB9CFC1885989734A5975210D",
                    "transactionUrl" to "https://etherscan.io/tx/0x092C0BFF67A71D483A3FCC5C0B390E4AA71E5C1CB9CFC1885989734A5975210D",
                ),
                "toChainStatus" to mapOf(
                    "transactionId" to null,
                    "transactionUrl" to null,
                ),
                "routeStatuses" to listOf(
                    mapOf(
                        "chainId" to "1",
                        "txHash" to "0x092C0BFF67A71D483A3FCC5C0B390E4AA71E5C1CB9CFC1885989734A5975210D",
                        "status" to "success",
                    ),
                    mapOf(
                        "chainId" to null,
                        "txHash" to null,
                        "status" to "ongoing",
                    ),
                ),
            ),
        )
        val payload = templateToMap(template = mock.depositFromUSDCEthPending)
        val result = processor.received(existing = mapOf(), payload = payload)
        assertEquals(expected.toJsonPrettyPrint(), result.toJsonPrettyPrint())
    }

    /**
     * Tests that a payload from a status endpoint request
     * for a successfully completed deposit from USDC on Eth (CCTP deposit)
     * is processed to be as similar as the analogous squid status request
     */
    @Test
    fun testReceivedDepositFromUSDCEthSuccess() {
        val expected = mapOf(
            "0x092C0BFF67A71D483A3FCC5C0B390E4AA71E5C1CB9CFC1885989734A5975210D" to mapOf(
                "squidTransactionStatus" to "success",
                "axelarTransactionUrl" to null,
                "fromChainStatus" to mapOf(
                    "transactionId" to "0x092C0BFF67A71D483A3FCC5C0B390E4AA71E5C1CB9CFC1885989734A5975210D",
                    "transactionUrl" to "https://etherscan.io/tx/0x092C0BFF67A71D483A3FCC5C0B390E4AA71E5C1CB9CFC1885989734A5975210D",
                ),
                "toChainStatus" to mapOf(
                    "transactionId" to "A256406470B202AC2152EF195B50F01A2DB25C29A560DA6D89CE80662D338D12",
                    "transactionUrl" to "https://www.mintscan.io/noble/txs/A256406470B202AC2152EF195B50F01A2DB25C29A560DA6D89CE80662D338D12",
                ),
                "routeStatuses" to listOf(
                    mapOf(
                        "chainId" to "1",
                        "txHash" to "0x092C0BFF67A71D483A3FCC5C0B390E4AA71E5C1CB9CFC1885989734A5975210D",
                        "status" to "success",
                    ),
                    mapOf(
                        "chainId" to "noble-1",
                        "txHash" to "A256406470B202AC2152EF195B50F01A2DB25C29A560DA6D89CE80662D338D12",
                        "status" to "success",
                    ),
                ),
            ),
        )
        val payload = templateToMap(template = mock.depositFromUSDCEthSuccess)
        val result = processor.received(existing = mapOf(), payload = payload)
        assertEquals(expected.toJsonPrettyPrint(), result.toJsonPrettyPrint())
    }

// -----------------------------------    CCTP WITHDRAWAL TESTS     ------------------------------------
    @Test
    fun testReceivedWithdrawToUSDCEthPending() {
        val expected = mapOf(
            "E550035D05238AA83BC025240B7CD1370D5516BB2D06BCDB31FA7DB229AA1E25" to mapOf(
                "squidTransactionStatus" to "ongoing",
                "axelarTransactionUrl" to null,
                "fromChainStatus" to mapOf(
                    "transactionId" to "E550035D05238AA83BC025240B7CD1370D5516BB2D06BCDB31FA7DB229AA1E25",
                    "transactionUrl" to "https://www.mintscan.io/noble/txs/E550035D05238AA83BC025240B7CD1370D5516BB2D06BCDB31FA7DB229AA1E25",
                ),
                "toChainStatus" to mapOf(
                    "transactionId" to null,
                    "transactionUrl" to null,
                ),
                "routeStatuses" to listOf(
                    mapOf(
                        "chainId" to "noble-1",
                        "txHash" to "E550035D05238AA83BC025240B7CD1370D5516BB2D06BCDB31FA7DB229AA1E25",
                        "status" to "success",
                    ),
                    mapOf(
                        "chainId" to null,
                        "txHash" to null,
                        "status" to "ongoing",
                    ),
                ),
            ),
        )
        val payload = templateToMap(template = mock.withdrawToUSDCEthPending)
        val result = processor.received(existing = mapOf(), payload = payload)
        assertEquals(expected.toJsonPrettyPrint(), result.toJsonPrettyPrint())
    }

    @Test
    fun testReceivedWithdrawToUSDCEthSuccess() {
        val expected = mapOf(
            "E550035D05238AA83BC025240B7CD1370D5516BB2D06BCDB31FA7DB229AA1E25" to mapOf(
                "squidTransactionStatus" to "success",
                "axelarTransactionUrl" to null,
                "fromChainStatus" to mapOf(
                    "transactionId" to "E550035D05238AA83BC025240B7CD1370D5516BB2D06BCDB31FA7DB229AA1E25",
                    "transactionUrl" to "https://www.mintscan.io/noble/txs/E550035D05238AA83BC025240B7CD1370D5516BB2D06BCDB31FA7DB229AA1E25",
                ),
                "toChainStatus" to mapOf(
                    "transactionId" to "0x969f344e567146442b28a30c52111937a703c1360db26e48ed57786630addd37",
                    "transactionUrl" to "https://etherscan.io/tx/0x969f344e567146442b28a30c52111937a703c1360db26e48ed57786630addd37",
                ),
                "routeStatuses" to listOf(
                    mapOf(
                        "chainId" to "noble-1",
                        "txHash" to "E550035D05238AA83BC025240B7CD1370D5516BB2D06BCDB31FA7DB229AA1E25",
                        "status" to "success",
                    ),
                    mapOf(
                        "chainId" to "1",
                        "txHash" to "0x969f344e567146442b28a30c52111937a703c1360db26e48ed57786630addd37",
                        "status" to "success",
                    ),
                ),
            ),
        )
        val payload = templateToMap(template = mock.withdrawToUSDCEthSuccess)
        val result = processor.received(existing = mapOf(), payload = payload)
        assertEquals(expected.toJsonPrettyPrint(), result.toJsonPrettyPrint())
    }

    @Test
    fun testReceived404Error() {
        val errorMessage = "some error message. my contents dont matter"
        val txId = "tx-id"
        val expected = mapOf(
            txId to mapOf(
                "squidTransactionStatus" to null,
                "axelarTransactionUrl" to null,
                "fromChainStatus" to null,
                "toChainStatus" to null,
                "routeStatuses" to listOf<Any>(),
                "error" to errorMessage,
            ),
        )
        val payload = templateToMap(
            template = """
            {
                "message": "$errorMessage"
            }
            """.trimIndent(),
        )
        val processor = SkipStatusProcessor(parser = parser, transactionId = txId)
        val result = processor.received(existing = mapOf(), payload = payload)
        assertEquals(expected.toJsonPrettyPrint(), result.toJsonPrettyPrint())
    }
}
