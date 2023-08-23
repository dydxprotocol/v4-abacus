//
//  AbacusStateManager.swift
//  abacus.ios
//
//  Created by John Huang on 8/23/23.
//

import Foundation

public final class AbacusStateManager: NSObject {
    public static let shared = AbacusStateManager()

    // async
    private lazy var asyncStateManager = {
        UIImplementations.reset(language: nil)
        let stateManager = AsyncAbacusStateManager(ioImplementations: IOImplementations.shared!, uiImplementations: UIImplementations.shared!, stateNotification: self, dataNotification: nil, v3signer: nil, apiKey: nil)
        return stateManager
    }()
}

extension AbacusStateManager: Abacus.StateNotificationProtocol {
    public func apiStateChanged(apiState: ApiState?) {
    }

    public func stateChanged(state: PerpetualState?, changes: StateChanges?) {
    }

    public func lastOrderChanged(order: SubaccountOrder?) {
    }

    public func errorsEmitted(errors: [ParsingError]) {
    }
}
