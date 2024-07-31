package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.SubaccountTransfer
import exchange.dydx.abacus.output.account.SubaccountTransferResources
import exchange.dydx.abacus.output.account.TransferRecordType
import exchange.dydx.abacus.tests.mock.LocalizerProtocolMock
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerTransferResponseObject
import indexer.codegen.IndexerTransferResponseObjectSender
import indexer.codegen.IndexerTransferType
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class TransferProcessorTests {
    companion object {
        private val createdAt = Instant.parse("2021-01-01T00:00:00Z")

        val payloadMock = IndexerTransferResponseObject(
            id = "id",
            sender = IndexerTransferResponseObjectSender(
                subaccountNumber = 0,
                address = "senderAddress",
            ),
            recipient = IndexerTransferResponseObjectSender(
                subaccountNumber = 1,
                address = "recipientAddress",
            ),
            size = "1000.0",
            createdAt = createdAt.toString(),
            createdAtHeight = "111",
            symbol = "symbol",
            type = IndexerTransferType.DEPOSIT,
            transactionHash = "transactionHash",
        )

        val transferMock = SubaccountTransfer(
            id = "id",
            type = TransferRecordType.DEPOSIT,
            asset = "symbol",
            amount = 1000.0,
            updatedAtBlock = 111,
            updatedAtMilliseconds = createdAt.toEpochMilliseconds().toDouble(),
            fromAddress = "senderAddress",
            toAddress = "recipientAddress",
            transactionHash = "transactionHash",
            resources = SubaccountTransferResources(
                typeString = "APP.GENERAL.DEPOSIT",
                typeStringKey = "APP.GENERAL.DEPOSIT",
                statusString = null,
                statusStringKey = null,
                blockExplorerUrl = null,
                iconLocal = "Incoming",
                indicator = "confirmed",
            ),
        )
    }

    private val processor = TransferProcessor(
        parser = Parser(),
        localizer = LocalizerProtocolMock(),
    )

    @Test
    fun testProces_deposit() {
        val result = processor.process(payloadMock)
        assertEquals(transferMock, result)
    }

    @Test
    fun testProces_withdrawal() {
        val payloadMock = payloadMock.copy(
            type = IndexerTransferType.WITHDRAWAL,
        )
        val transferMock = transferMock.copy(
            type = TransferRecordType.WITHDRAW,
            resources = transferMock.resources.copy(
                typeString = "APP.GENERAL.WITHDRAW",
                typeStringKey = "APP.GENERAL.WITHDRAW",
                iconLocal = "Outgoing",
            ),
        )
        processor.accountAddress = "recipientAddress"
        val result = processor.process(payloadMock)
        assertEquals(transferMock, result)
    }

    @Test
    fun testProcess_transferOut() {
        val payloadMock = payloadMock.copy(
            type = IndexerTransferType.WITHDRAWAL,
        )
        val transferMock = transferMock.copy(
            type = TransferRecordType.TRANSFER_OUT,
            resources = transferMock.resources.copy(
                typeString = "APP.GENERAL.TRANSFER_OUT",
                typeStringKey = "APP.GENERAL.TRANSFER_OUT",
                iconLocal = "Outgoing",
            ),
        )
        processor.accountAddress = "differentAddress"
        val result = processor.process(payloadMock)
        assertEquals(transferMock, result)
    }
}
