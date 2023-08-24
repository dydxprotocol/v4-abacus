//
//  AbacusTrackingImp.swift
//  dydxStateManager
//
//  Created by John Huang on 7/17/23.
//

import Foundation
import Abacus

final public class AbacusTrackingImp: NSObject, Abacus.TrackingProtocol {
    public func log(event: String, data: String?) {
    }

    public func log(event: String, data: [String: Any]) {
//        Tracking.shared?.log(event: event, data: data)
    }
}
