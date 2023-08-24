//
//  CosmoJavascript.swift
//  dydxModels
//
//  Created by John Huang on 11/30/22.
//  Copyright © 2022 dYdX Trading Inc. All rights reserved.
//

import JavaScriptCore

public final class CosmoJavascript: NSObject {
    public static var shared: CosmoJavascript = CosmoJavascript()

    private var v4ClientInitialized: Bool = false
    public var v4ClientRunner: JavascriptRunner? = {
        JavascriptRunner.runner(file: "v4-native-client.js")
    }()

    public func loadV4Client(completion: @escaping JavascriptCompletion) {
        if v4ClientInitialized {
            completion(nil)
        } else {
            if let runner = v4ClientRunner {
                runner.load { [weak self] successful in
                    self?.v4ClientInitialized = successful
                    completion(true)
                }
            } else {
                completion(nil)
            }
        }
    }

    public func deriveCosmosKey(
        chainId: String,
        signature: String,
        completion: @escaping JavascriptCompletion) {
        callNativeClient(functionName: "deriveMnemomicFromEthereumSignature", params: [signature], completion: completion)
    }

    public func getAccountBalance(completion: @escaping JavascriptCompletion) {
        callNativeClient(functionName: "getAccountBalance", params: [], completion: completion)
    }
    
    public func withdrawToIBC(subaccount: Int,
                              amount: Double,
                              payload: String,
                              completion: @escaping JavascriptCompletion) {
        if let data = payload.data(using: .utf8) {
            let base64String = data.base64EncodedString()
            callNativeClient(functionName: "withdrawToIBC", params: [ subaccount, amount, base64String ], completion: completion)
        } else {
            assertionFailure("Invalid data")
        }
    }
    
    public func depositToSubaccount(subaccount: Int,
                                    amount: Double,
                                    completion: @escaping JavascriptCompletion) {
        let json = "{\"subaccountNumber\": \(subaccount),\"amount\": \(amount)}"
        callNativeClient(functionName: "deposit", params: [ json ], completion: completion)
    }
    
    public func encodeAccountRequestData(address: String?, completion: @escaping JavascriptCompletion) {
        loadV4Client { [weak self] _ in
            if let address = address, let runner = self?.v4ClientRunner {
                let script = "NativeClient.encodeAccountRequestData('\(address)')"
                runner.run(script: script, completion: { result in
                    if (result as? String) == "undefined" || result == nil {
                        let error = NSError(domain: "encode.account.address", code: 0, userInfo: [
                            "address": address,
                        ])
//                        ErrorLogging.shared?.log(error)
                    }
                    completion(result)
                })
            } else {
                completion(nil)
            }
        }
    }

    public func decodeAccountResponseValue(value: String?, completion: @escaping JavascriptCompletion) {
        loadV4Client { [weak self] _ in
            if let value = value, let runner = self?.v4ClientRunner {
                let script = "NativeClient.decodeAccountResponseValue('\(value)')"
                runner.run(script: script, completion: { result in
                    if (result as? String) == "undefined" || result == nil {
                        let error = NSError(domain: "decode.account.value", code: 0, userInfo: [
                            "value": value,
                        ])
//                        ErrorLogging.shared?.log(error)
                    }
                    completion(result)
                })
            } else {
                completion(nil)
            }
        }
    }

    public func signPlaceOrderTransaction(
        chainId: String,
        address: String,
        mnemonic: String,
        accountNumber: Int,
        sequence: Int,
        subaccountNumber: Int,
        clobPairId: Int,
        side: String,
        quantums: Int,
        subticks: Int,
        goodTilBlock: Int?,
        goodTilBlockTime: Double?,
        clientId: Int,
        timeInForce: String,
        orderFlags: String,
        reduceOnly: Bool,
        completion: @escaping JavascriptCompletion) {
        callNativeClient(functionName: "signPlaceOrderAsync", params: [chainId, address, mnemonic, accountNumber, sequence, subaccountNumber, clobPairId, side, quantums, subticks, goodTilBlock ?? 0, goodTilBlockTime ?? 0, clientId, timeInForce, orderFlags, reduceOnly], completion: completion)
    }

    public func signCancelOrderTransaction(
        chainId: String,
        address: String,
        mnemonic: String,
        accountNumber: Int,
        sequence: Int,
        subaccountNumber: Int,
        clobPairId: Int,
        clientId: Int,
        orderFlags: String,
        goodTilBlock: Int,
        goodTilBlockTime: Int,
        completion: @escaping JavascriptCompletion) {
        callNativeClient(functionName: "signCancelOrderAsync", params: [chainId, address, mnemonic, accountNumber, sequence, subaccountNumber, clobPairId, clientId, orderFlags, goodTilBlock, goodTilBlockTime], completion: completion)
    }

    private func callNativeClient(functionName: String, params: [Any], completion: @escaping JavascriptCompletion) {
        loadV4Client { [weak self] _ in
            DispatchQueue.main.async {
                if let runner = self?.v4ClientRunner {
                    runner.invoke(className: nil, function: functionName, params: params) { result in
                        DispatchQueue.main.async {
                            completion(result)
                        }
                    }
                } else {
                    completion(nil)
                }
            }
        }
    }

    public func test(completion: @escaping JavascriptCompletion) {
        callNativeClient(functionName: "connectClient", params: ["dydxprotocol-staging"]) { result in
            self.callNativeClient(functionName: "getPerpetualMarkets", params: []) { result in
                completion(result)
            }
        }
    }
    
    public func connectNetwork(chainId: String, validatorUrl: String, indexerUrl: String, indexerSocketUrl: String, faucetUrl: String?, completion: @escaping JavascriptCompletion) {
        let params = (faucetUrl != nil) ? [chainId, validatorUrl, indexerUrl, indexerSocketUrl, faucetUrl!] : [chainId, validatorUrl, indexerUrl, indexerSocketUrl]
        callNativeClient(functionName: "connectNetwork", params: params) { result in
            completion(result)
        }
    }
    
    public func connectWallet(mnemonic: String, completion: @escaping JavascriptCompletion) {
        callNativeClient(functionName: "connectWallet", params: [mnemonic]) { result in
            completion(result)
        }
    }
    
    public func call(functionName: String, paramsInJson: String?, completion: @escaping JavascriptCompletion) {
        callNativeClient(functionName: functionName, params: paramsInJson != nil ? [paramsInJson!] : []) { result in
            completion(result)
        }
    }
}

/* to test
 Add this code somewhere
 CosmoJavascript.shared.test { result in
     Console.shared.log(result)
 }
 */
