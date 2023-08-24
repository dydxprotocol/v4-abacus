//
//  AbacusStateManager.swift
//  abacus.ios
//
//  Created by John Huang on 8/23/23.
//

import Foundation
import Abacus

public final class AbacusStateManager: NSObject {
    public static let shared = AbacusStateManager()

    // async
    public lazy var asyncStateManager = {
        UIImplementations.reset(language: nil)
        let url = "https://dydx-v4-shared-resources.vercel.app/config/staging/dev_endpoints.json"
        let file = "/config/staging/dev_endpoints.json"
        let stateManager = AsyncAbacusStateManager(
            environmentsUrl: url,
            environmentsFile: file,
            ioImplementations: IOImplementations.shared!,
            uiImplementations: UIImplementations.shared!,
            stateNotification: self,
            dataNotification: nil)
        return stateManager
    }()
}

extension AbacusStateManager: Abacus.StateNotificationProtocol {
    public func notificationsChanged(notifications: [Abacus.Notification]) {
        
    }
    
    public func environmentsChanged() {
        
    }
    
    public func apiStateChanged(apiState: ApiState?) {
    }

    public func stateChanged(state: PerpetualState?, changes: StateChanges?) {
    }

    public func lastOrderChanged(order: SubaccountOrder?) {
    }

    public func errorsEmitted(errors: [ParsingError]) {
    }
}
