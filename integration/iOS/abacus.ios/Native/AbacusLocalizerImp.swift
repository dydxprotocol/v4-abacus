//
//  AbacusLocalizerImp.swift
//  dydxStateManager
//
//  Created by John Huang on 7/17/23.
//

import Foundation
import Abacus
import Utilities

final public class AbacusLocalizerImp: NSObject, Abacus.LocalizerProtocol {
    private func params(json: String?) -> [String: String]? {
        if let json = json, let data = json.data(using: .utf8) {
            return try? JSONSerialization.jsonObject(with: data, options: []) as? [String: String]
        } else {
            return nil
        }
    }

    public func localize(path: String, paramsAsJson: String?) -> String {
        return DataLocalizer.shared?.localize(path: path, params: params(json: paramsAsJson)) ?? path
    }
}
