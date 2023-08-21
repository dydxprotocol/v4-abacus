//
//  Utils.swift
//  abacus.ios
//
//  Created by John Huang on 8/31/22.
//

import Foundation

public class NotificationToken {
    let notificationCenter: NotificationCenter
    let token: NSObjectProtocol

    init(notificationCenter: NotificationCenter = .default, token: NSObjectProtocol) {
        self.notificationCenter = notificationCenter
        self.token = token
    }

    deinit {
        notificationCenter.removeObserver(token)
    }
}

public extension NotificationCenter {
    func observe(_ obj: Any? = nil, notification: NSNotification.Name?, queue: OperationQueue? = nil, do block: @escaping (Notification) -> Void) -> NotificationToken {
        let token = addObserver(forName: notification, object: obj, queue: queue, using: block)
        return NotificationToken(notificationCenter: self, token: token)
    }
}
