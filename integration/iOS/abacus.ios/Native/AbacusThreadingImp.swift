//
//  AbacusThreadingImp.swift
//  dydxStateManager
//
//  Created by John Huang on 7/17/23.
//

import Foundation

import Abacus
import Utilities

final public class AbacusThreadingImp: Abacus.ThreadingProtocol {
    private let abacusQueue = DispatchQueue(label: "Abacus")
    private let networkQueue = DispatchQueue(label: "Network")
    public func async(type: ThreadingType, block: @escaping () -> Void) {
        switch type {
        case .main:
            DispatchQueue.main.async {
                block()
            }
        case .abacus:
            DispatchQueue.main.async {
                block()
            }

        case .network:
            DispatchQueue.main.async {
                block()
            }
        default:
            break
        }
    }
}
