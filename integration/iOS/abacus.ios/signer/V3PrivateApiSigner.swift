//
//  V3PrivateApiSigner.swift
//  abacus.ios
//
//  Created by John Huang on 10/11/22.
//

import Abacus
import CommonCrypto
import CryptoKit
import CryptoSwift

extension String {
    public func hmac(key: Data) -> [UInt8]? {
        let buffer = [UInt8](key)
        return try? HMAC(key: buffer, variant: .sha256).authenticate(bytes)
    }
}

public class V3PrivateApiSigner: V3PrivateSignerProtocol {
    public func sign(text: String, secret: String) -> String? {
        print("Signing \(text)")
        if let decoded = Data(base64Encoded: secret.replacingOccurrences(of: "-", with: "+").replacingOccurrences(of: "_", with: "/"), options: .ignoreUnknownCharacters) {
            let signature = text.hmac(key: decoded)
            let result = NSData(bytes: signature!, length: signature!.count)
            return result.base64EncodedString()
        }
        return nil
    }
}
