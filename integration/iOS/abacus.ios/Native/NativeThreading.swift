//
//  NativeThreading.swift
//  abacus.ios
//
//  Created by John Huang on 5/25/23.
//

import Foundation
import Abacus

public class NativeThreading: ThreadingProtocol {
    private var abacusQueue = DispatchQueue(label: "abacus")
    private var networkQueue = DispatchQueue(label: "network")
    public func async(type: ThreadingType, block: @escaping () -> Void) {
        switch type {
        case .main:
            DispatchQueue.main.async {
                block()
            }
            
        case .abacus:
            abacusQueue.async {
                block()
            }
            
        case .network:
            networkQueue.async {
                block()
            }
            
        default:
            break
        }
    }
    
    public func schedule(delay: Double, repeat: Bool, block: @escaping () -> Void) {
        
    }
}
