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
        let stateManager = AsyncAbacusStateManager(
            deploymentUri: "https://trader-fe.vercel.app",
            deployment: "DEV",
            appConfigs: AppConfigs.companion.forWeb,
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
