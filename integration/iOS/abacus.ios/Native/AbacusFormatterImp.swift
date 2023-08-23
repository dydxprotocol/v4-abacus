//
//  AbacusFormatterImp.swift
//  dydxStateManager
//
//  Created by John Huang on 7/17/23.
//

import Foundation
import Abacus
import dydxFormatter

final public class AbacusFormatterImp: Abacus.FormatterProtocol {
    public func percent(value: KotlinDouble?, digits: Int32) -> String? {
        dydxFormatter.shared.percent(number: value?.doubleValue, digits: Int(digits))
    }

    public func dollar(value: KotlinDouble?, tickSize: String?) -> String? {
        dydxFormatter.shared.dollar(number: value?.doubleValue, size: tickSize)
    }
}
