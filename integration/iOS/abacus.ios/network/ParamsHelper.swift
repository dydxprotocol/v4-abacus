//
//  ParamsHelper.swift
//  abacus.ios
//
//  Created by John Huang on 8/30/22.
//

import Abacus
import Foundation

public class ParamsHelper {
    static func map(params: [NetworkParam]?) -> [String: Any]? {
        if let params = params {
            var map = [String: Any]()
            for i in 0..<params.count {
                let param = params[i]
                if let value = param.value {
                    map[param.key] = value
                }
            }
            return map
        } else {
            return nil
        }
    }
}
