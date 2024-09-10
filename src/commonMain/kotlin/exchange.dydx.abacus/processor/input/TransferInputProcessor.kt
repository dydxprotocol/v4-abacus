package exchange.dydx.abacus.processor.input

import exchange.dydx.abacus.calculator.v2.TransferInputCalculatorV2
import exchange.dydx.abacus.output.input.DepositInputOptions
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.TransferInputResources
import exchange.dydx.abacus.output.input.TransferInputSize
import exchange.dydx.abacus.output.input.TransferOutInputOptions
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.output.input.WithdrawalInputOptions
import exchange.dydx.abacus.processor.router.IRouterProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.internalstate.InternalInputState
import exchange.dydx.abacus.state.internalstate.InternalTransferInputState
import exchange.dydx.abacus.state.internalstate.InternalWalletState
import exchange.dydx.abacus.state.internalstate.safeCreate
import exchange.dydx.abacus.state.manager.ExchangeConfig.exchangeList
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.model.TransferInputField
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMutableList
import kollections.iListOf
import kollections.iMutableListOf
import kollections.toIList
import kollections.toIMap

internal class TransferInputProcessor(
    private val parser: ParserProtocol,
    private val calculator: TransferInputCalculatorV2 = TransferInputCalculatorV2(parser = parser),
    private val routerProcessor: IRouterProcessor,
    private val environment: V4Environment?
) {
    fun transfer(
        inputState: InternalInputState,
        walletState: InternalWalletState,
        data: String?,
        inputField: TransferInputField,
        subaccountNumber: Int = 0
    ): InputProcessorResult {
        val error: ParsingError? = null

        if (inputState.currentType != InputType.TRANSFER) {
            inputState.currentType = InputType.TRANSFER

            inputState.transfer.size = null
            inputState.transfer.fastSpeed = false
            inputState.transfer.fee = null
            inputState.transfer.exchange = null
            inputState.transfer.chain = null
            inputState.transfer.token = null
            inputState.transfer.address = null
            inputState.transfer.memo = null
            inputState.transfer.depositOptions = null
            inputState.transfer.withdrawalOptions = null
            inputState.transfer.transferOutOptions = null
            inputState.transfer.summary = null
            inputState.transfer.resources = null
            inputState.transfer.route = null

            inputState.transfer.type = TransferType.deposit
            val chainType = routerProcessor.defaultChainId()
            if (chainType != null) {
                updateTransferToChainType(inputState.transfer, chainType)
            }
            inputState.transfer.token = routerProcessor.defaultTokenAddress(chainType)

            calculator.calculate(
                transfer = inputState.transfer,
                wallet = walletState,
                subaccountNumber = subaccountNumber,
            )
        }

        val transfer = inputState.transfer

        var updated = false
        when (inputField) {
            TransferInputField.type -> {
                val type = TransferType.invoke(data)
                if (transfer.type != type) {
                    transfer.type = type
                    transfer.size =
                        TransferInputSize.safeCreate(transfer.size)
                            .copy(size = null, usdcSize = null)
                    transfer.route = null
                    transfer.memo = null
                    if (type == TransferType.transferOut) {
                        transfer.chain = "chain"
                        transfer.token = "usdc"
                    } else {
                        val chainType = routerProcessor.defaultChainId()
                        if (chainType != null) {
                            updateTransferToChainType(transfer, chainType)
                        }
                        transfer.token = routerProcessor.defaultTokenAddress(chainType)
                    }
                }
                updated = true
            }

            TransferInputField.address -> {
                if (transfer.address != data) {
                    transfer.address = data
                    transfer.route = null
                    updated = true
                }
            }

            TransferInputField.token -> {
                if (transfer.token != data) {
                    transfer.token = data
                    transfer.size = TransferInputSize.safeCreate(transfer.size).copy(
                        size = null,
                        usdcSize = null,
                    )
                    if (data != null) {
                        updateTransferToTokenType(transfer, data)
                    }
                    updated = true
                }
            }

            TransferInputField.usdcSize -> {
                if (data != transfer.size?.usdcSize) {
                    transfer.size =
                        TransferInputSize.safeCreate(transfer.size).copy(usdcSize = data)
                    transfer.route = null
                    updated = true
                }
            }

            TransferInputField.usdcFee -> {
                val fee = parser.asDouble(data)
                if (transfer.fee != fee) {
                    transfer.fee = fee
                    transfer.route = null
                    updated = true
                }
            }

            TransferInputField.size -> {
                if (transfer.size?.size != data) {
                    transfer.size = TransferInputSize.safeCreate(transfer.size).copy(size = data)
                    transfer.route = null
                    if (transfer.type == TransferType.deposit) {
                        transfer.size?.copy(usdcSize = null)
                    }
                    updated = true
                }
            }

            TransferInputField.fastSpeed -> {
                val fastSpeed = parser.asBool(data) ?: false
                if (transfer.fastSpeed != fastSpeed) {
                    transfer.fastSpeed = fastSpeed
                    updated = true
                }
            }

            TransferInputField.chain -> {
                if (data != null) {
                    updateTransferToChainType(transfer, data)
                }
                updated = true
            }

            TransferInputField.exchange -> {
                val exchange = parser.asString(data)
                if (exchange != null) {
                    updateTransferExchangeType(transfer, exchange)
                }
                updated = true
            }

            TransferInputField.MEMO -> {
                if (transfer.memo != data) {
                    transfer.memo = data
                    updated = true
                }
            }
        }

        val type = transfer.type
        if (updated && type != null) {
            when (type) {
                TransferType.deposit -> {
                    updateDepositOptions(transfer)
                }
                TransferType.withdrawal -> {
                    updateWithdrawalOptions(transfer)
                }
                TransferType.transferOut -> {
                    updateTransferOutOptions(transfer)
                }
            }

            transfer.resources = TransferInputResources.safeCreate(transfer.resources)
                .copy(
                    chainResources = transfer.chainResources?.toIMap(),
                    tokenResources = transfer.tokenResources?.toIMap(),
                )
        }

        return InputProcessorResult(
            changes = if (updated) {
                StateChanges(
                    changes = iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                    markets = null,
                    subaccountNumbers = iListOf(subaccountNumber),
                )
            } else {
                null
            },
            error = error,
        )
    }

    private fun updateDepositOptions(
        transfer: InternalTransferInputState,
    ) {
        val chains: IList<SelectionOption> = transfer.chains?.toIList() ?: iListOf()
        val assets: IList<SelectionOption> = transfer.tokens?.toIList() ?: iListOf()
        var exchanges: IMutableList<SelectionOption>? = null
        exchangeList?.let { data ->
            exchanges = iMutableListOf()
            for (i in data.indices) {
                val item = data[i]
                val selection = SelectionOption(item.name, item.label, item.label, item.icon)
                exchanges?.add(selection)
            }
        }
        transfer.depositOptions = DepositInputOptions(
            needsSize = false,
            needsAddress = false,
            needsFastSpeed = false,
            exchanges = exchanges?.toIList(),
            chains = chains,
            assets = assets,
        )
    }

    private fun updateWithdrawalOptions(
        transfer: InternalTransferInputState,
    ) {
        val chains: IList<SelectionOption> = transfer.chains?.toIList() ?: iListOf()
        val assets: IList<SelectionOption> = transfer.tokens?.toIList() ?: iListOf()
        var exchanges: IMutableList<SelectionOption>? = null
        exchangeList?.let { data ->
            exchanges = iMutableListOf()
            for (i in data.indices) {
                val item = data[i]
                val selection = SelectionOption(item.name, item.label, item.label, item.icon)
                exchanges?.add(selection)
            }
        }
        transfer.withdrawalOptions = WithdrawalInputOptions(
            needsSize = false,
            needsAddress = false,
            needsFastSpeed = false,
            exchanges = exchanges?.toIList(),
            chains = chains,
            assets = assets,
        )
    }

    private fun updateTransferOutOptions(
        transfer: InternalTransferInputState,
    ) {
        val chainName = environment?.chainName
        val chainOption: SelectionOption = if (chainName != null) {
            SelectionOption(
                type = "chain",
                string = chainName,
                stringKey = null,
                iconUrl = environment?.chainLogo,
            )
        } else {
            return
        }
        val chains: IList<SelectionOption> = iListOf(chainOption)

        val assets: IList<SelectionOption>? = environment?.tokens?.keys?.map { key ->
            val token = environment.tokens[key]!!
            SelectionOption(
                type = key,
                string = token.name,
                stringKey = null,
                iconUrl = token.imageUrl,
            )
        }
        transfer.transferOutOptions = TransferOutInputOptions(
            needsSize = false,
            needsAddress = false,
            chains = chains,
            assets = assets,
        )
    }

    private fun updateTransferToChainType(
        transfer: InternalTransferInputState,
        chainType: String
    ) {
        val tokenOptions = routerProcessor.tokenOptions(chainType)

        if (transfer.type != TransferType.transferOut) {
            transfer.tokens = tokenOptions
            transfer.chain = chainType
            transfer.token = routerProcessor.defaultTokenAddress(chainType)
            transfer.chainResources = routerProcessor.chainResources(chainType)
            transfer.tokenResources = routerProcessor.tokenResources(chainType)
        }
        transfer.exchange = null
        transfer.size = TransferInputSize.safeCreate(transfer.size).copy(size = null)
        transfer.route = null
        // needed to pass tests, remove later
        transfer.depositOptions = DepositInputOptions.safeCreate(transfer.depositOptions)
            .copy(assets = tokenOptions.toIList())
        transfer.withdrawalOptions = WithdrawalInputOptions.safeCreate(transfer.withdrawalOptions)
            .copy(assets = tokenOptions.toIList())
    }

    private fun updateTransferToTokenType(
        transfer: InternalTransferInputState,
        tokenAddress: String
    ) {
        // Code not used
//        val selectedChainId = transfer.chain
//        if (transfer.type == TransferType.transferOut) {
//            transfer.size = TransferInputSize.safeCreate(transfer.size).copy(
//                size = null, usdcSize = null
//            )
//        } else {
//            transfer.safeSet(
//                "resources.tokenSymbol",
//                routerProcessor.selectedTokenSymbol(tokenAddress = tokenAddress, selectedChainId = selectedChainId),
//            )
//            transfer.safeSet(
//                "resources.tokenDecimals",
//                routerProcessor.selectedTokenDecimals(tokenAddress = tokenAddress, selectedChainId = selectedChainId),
//            )
//        }
        transfer.route = null
    }

    private fun updateTransferExchangeType(
        transfer: InternalTransferInputState,
        exchange: String
    ) {
        val exchangeDestinationChainId = routerProcessor.exchangeDestinationChainId
        val tokenOptions = routerProcessor.tokenOptions(exchangeDestinationChainId)
        if (transfer.type != TransferType.transferOut) {
            transfer.tokens = tokenOptions
            transfer.token = routerProcessor.defaultTokenAddress(exchangeDestinationChainId)
            transfer.tokenResources = routerProcessor.tokenResources(exchangeDestinationChainId)
            // needed to pass tests, remove later
            transfer.depositOptions = DepositInputOptions.safeCreate(transfer.depositOptions)
                .copy(assets = tokenOptions.toIList())
            transfer.withdrawalOptions = WithdrawalInputOptions.safeCreate(transfer.withdrawalOptions)
                .copy(assets = tokenOptions.toIList())
        }

        transfer.exchange = exchange
        transfer.chain = null
        transfer.size = TransferInputSize.safeCreate(transfer.size).copy(size = null)
        transfer.route = null
    }
}
