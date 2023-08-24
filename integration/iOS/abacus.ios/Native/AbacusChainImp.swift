//
//  AbacusChainImp.swift
//  dydxStateManager
//
//  Created by John Huang on 7/17/23.
//

import Foundation
import Abacus

final public class AbacusChainImp: Abacus.DYDXChainTransactionsProtocol {
    public func connectNetwork(indexerUrl: String, indexerSocketUrl: String, validatorUrl: String, chainId: String, faucetUrl: String?, callback: @escaping (String?) -> Void) {
        CosmoJavascript.shared.connectNetwork(chainId: chainId, validatorUrl: validatorUrl, indexerUrl: indexerUrl, indexerSocketUrl: indexerSocketUrl, faucetUrl: faucetUrl) { result in
            callback(result as? String)
        }
    }

    public func transaction(type: TransactionType, paramsInJson: String?, callback: @escaping (String?) -> Void) {
        CosmoJavascript.shared.call(functionName: type.rawValue, paramsInJson: paramsInJson) { result in
            callback(result as? String)
        }
    }

    public func get(type: QueryType, paramsInJson: String?, callback: @escaping (String?) -> Void) {
        CosmoJavascript.shared.call(functionName: type.rawValue, paramsInJson: paramsInJson) { result in
            callback(result as? String)
        }
    }

}
