//
//  AsyncStateManager.swift
//  abacus.ios
//
//  Created by John Huang on 5/25/23.
//

import Abacus
import Foundation

class AsyncStateManager: StateNotificationProtocol {
    public static var shared: AsyncStateManager = {
        let state = AsyncStateManager()
        return state
    }()

    public lazy var stateManager: AsyncAbacusStateManager = {
        let factory = ProtocolNativeImpFactory(rest: NativeRest(), webSocket: NativeWebSocket(), chain: nil, localizer: nil, formatter: nil, tracking: nil, threading: NativeThreading(), timer: nil, stateNotification: self, dataNotification: nil, v3Signer: nil, apiKey: nil)
        let stateManager = AsyncAbacusStateManager(_nativeImplementations: factory);
        stateManager.environmentId = "dydxprotocol-staging"
        return stateManager
    }()
    
    public var state: PerpetualState?
    public var apiState: ApiState?

    func stateChanged(state: PerpetualState?, changes: StateChanges?) {
        self.state = state
        if let changeItems = changes?.changes {
            for i in 0 ..< changeItems.count {
                let change = changeItems[i]
                switch change {
                case Changes.input:
                    print("input changed")
                    
                case Changes.wallet:
                    print("wallet changed")
                    
                case Changes.assets:
                    print("asset changed")
                    
                case Changes.subaccount:
                    print("subaccount changed")
                    
                case Changes.candles:
                    print("candles changed")
                    
                case Changes.configs:
                    print("configs changed")
                    
                case Changes.fills:
                    print("fills changed")
                    
                case Changes.fundingpayments:
                    print("funding payments changed")
                    
                case Changes.historicalfundings:
                    print("historical funding changed")
                    
                case Changes.historicalpnl:
                    print("historical PNL changed")
                    
                case Changes.orderbook:
                    print("orderbook changed")
                    
                case Changes.markets:
                    print("markets changed")
                    
                case Changes.sparklines:
                    print("sparklines changed")
                    
                case Changes.trades:
                    print("trades changed")
                    
                case Changes.transfers:
                    print("transfers changed")
                    
                default:
                    print("others changed")
                }
            }
        }
        
    }
    
    func apiStateChanged(apiState: ApiState?) {
        self.apiState = apiState
    }
    
    func errorsEmitted(errors: [ParsingError]) {
    }
    
    func lastOrderChanged(order: SubaccountOrder?) {
    }
    
}
