package exchange.dydx.abacus.app.manager.v2

import exchange.dydx.abacus.app.manager.TestChain
import exchange.dydx.abacus.app.manager.TestRest
import exchange.dydx.abacus.app.manager.TestState
import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.state.manager.ApiStatus
import exchange.dydx.abacus.state.manager.NetworkStatus
import exchange.dydx.abacus.state.v2.manager.AsyncAbacusStateManagerV2
import exchange.dydx.abacus.state.v2.supervisor.AppConfigsV2
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class V4HeightTests {
    val mock = AbacusMockData()
    private val testCosmoAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"
    private var stateManager: AsyncAbacusStateManagerV2 = resetStateManager()
    private var ioImplementations = stateManager.ioImplementations
    private var testRest = stateManager.ioImplementations.rest as? TestRest
    private var testChain = stateManager.ioImplementations.chain as? TestChain
    private var testState = stateManager.stateNotification as? TestState
    private var v4Adapter = stateManager.adaptor

    @BeforeTest
    fun reset() {
        stateManager = resetStateManager()
        ioImplementations = stateManager.ioImplementations
        testRest = stateManager.ioImplementations.rest as? TestRest
        testChain = stateManager.ioImplementations.chain as? TestChain
        testState = stateManager.stateNotification as? TestState
        v4Adapter = stateManager.adaptor
    }

    fun resetStateManager(): AsyncAbacusStateManagerV2 {
        val ioImplementations = BaseTests.testIOImplementations()
        val localizer = BaseTests.testLocalizer(ioImplementations)
        val uiImplementations = BaseTests.testUIImplementations(localizer)
        stateManager = AsyncAbacusStateManagerV2(
            "https://api.examples.com",
            "DEV",
            AppConfigsV2.forApp,
            ioImplementations,
            uiImplementations,
            TestState(),
            null,
        )
        stateManager.environmentId = "dydxprotocol-staging"
        return stateManager
    }

    private fun setStateMachineReadyToConnect(stateManager: AsyncAbacusStateManagerV2) {
        stateManager.readyToConnect = true
    }

    @Test
    fun testNoHeight() {
        /* no height yet */
        setStateMachineReadyToConnect(stateManager)

        assertEquals(ApiStatus.UNKNOWN, testState?.apiState?.status)
        assertNull(testState?.apiState?.haltedBlock)
        assertNull(testState?.apiState?.trailingBlocks)
        assertNull(testState?.apiState?.height)

        assertEquals(NetworkStatus.UNKNOWN, v4Adapter?.indexerState?.status)
        assertNull(v4Adapter?.indexerState?.blockAndTime?.block)

        assertEquals(NetworkStatus.UNKNOWN, v4Adapter?.validatorState?.status)
        assertNull(v4Adapter?.validatorState?.blockAndTime?.block)
    }

    @Test
    fun testIndexerHeight() {
        /* Only indexer height */
        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/height",
            mock.heightMock.indexerHeight,
        )
        stateManager.readyToConnect = true

        assertEquals(ApiStatus.NORMAL, testState?.apiState?.status)
        assertNull(testState?.apiState?.haltedBlock)
        assertNull(testState?.apiState?.trailingBlocks)

        assertNotNull(v4Adapter)
        assertEquals(NetworkStatus.NORMAL, v4Adapter?.indexerState?.status)
        assertEquals(16750, v4Adapter?.indexerState?.blockAndTime?.block)

        assertEquals(ApiStatus.NORMAL, testState?.apiState?.status)
        assertEquals(16750, testState?.apiState?.height)
    }

    @Test
    fun testValidatorHeight() {
        testChain?.heightResponse = mock.heightMock.v4ClientValidatorHeight
        stateManager.readyToConnect = true

        assertEquals(ApiStatus.NORMAL, testState?.apiState?.status)
        assertNull(testState?.apiState?.haltedBlock)
        assertNull(testState?.apiState?.trailingBlocks)

        assertNotNull(v4Adapter)
        assertEquals(NetworkStatus.UNKNOWN, v4Adapter?.indexerState?.status)
        assertNull(v4Adapter?.indexerState?.blockAndTime?.block)

        assertEquals(NetworkStatus.NORMAL, v4Adapter?.validatorState?.status)
        assertEquals(16753, v4Adapter?.validatorState?.blockAndTime?.block)

        assertEquals(ApiStatus.NORMAL, testState?.apiState?.status)
        assertEquals(16753, testState?.apiState?.height)
    }

    @Test
    fun testBothIndexerAndValidatorHeight() {
        /* Only indexer height */
        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/height",
            mock.heightMock.indexerHeight,
        )
        testChain?.heightResponse = mock.heightMock.v4ClientValidatorHeight
        stateManager.readyToConnect = true

        assertEquals(ApiStatus.NORMAL, testState?.apiState?.status)
        assertNull(testState?.apiState?.haltedBlock)
        assertNull(testState?.apiState?.trailingBlocks)

        assertNotNull(v4Adapter)
        assertEquals(NetworkStatus.NORMAL, v4Adapter?.indexerState?.status)
        assertEquals(16750, v4Adapter?.indexerState?.blockAndTime?.block)

        assertEquals(NetworkStatus.NORMAL, v4Adapter?.validatorState?.status)
        assertEquals(16753, v4Adapter?.validatorState?.blockAndTime?.block)

        assertEquals(ApiStatus.NORMAL, testState?.apiState?.status)
        assertEquals(16753, testState?.apiState?.height)

        // Halting Indexer
        for (i in 0..10) {
            // This causes 10x same height from validator
            stateManager.readyToConnect = false
            testRest?.setResponse(
                "https://indexer.v4staging.dydx.exchange/v4/height",
                mock.heightMock.indexerHeight,
            )
            stateManager.readyToConnect = true
        }

        assertEquals(ApiStatus.VALIDATOR_HALTED, testState?.apiState?.status)
        assertEquals(16753, testState?.apiState?.haltedBlock)
        assertEquals(NetworkStatus.HALTED, v4Adapter?.indexerState?.status)

        // Halting Validator
        for (i in 0..10) {
            // This causes 10x same height from validator
            stateManager.readyToConnect = false
            testChain?.heightResponse = mock.heightMock.v4ClientValidatorHeight
            stateManager.readyToConnect = true
        }

        assertEquals(ApiStatus.VALIDATOR_HALTED, testState?.apiState?.status)
        assertEquals(16753, testState?.apiState?.haltedBlock)
        assertEquals(NetworkStatus.HALTED, v4Adapter?.validatorState?.status)

        // Indexer Advances
        stateManager.readyToConnect = false
        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/height",
            mock.heightMock.indexerHeight2,
        )
        stateManager.readyToConnect = true

        assertEquals(ApiStatus.VALIDATOR_HALTED, testState?.apiState?.status)

        // Validator Advances
        stateManager.readyToConnect = false
        testChain?.heightResponse = mock.heightMock.v4ClientValidatorHeight2
        stateManager.readyToConnect = true

        assertEquals(ApiStatus.INDEXER_TRAILING, testState?.apiState?.status)
        assertEquals(153, testState?.apiState?.trailingBlocks)
    }
}
