//
//  StringLoader.swift
//  abacus.ios
//
//  Created by John Huang on 8/23/23.
//

import Foundation

@objc public class StringLoader: NSObject {
    @objc public class func load(file: String) -> String? {
        let fileUrl = URL(fileURLWithPath: file)
        guard let data = try? Data(contentsOf: fileUrl) else {
            return nil
        }
        return String(decoding: data, as: UTF8.self)
    }

    @objc public class func load(bundle: Bundle, fileName: String?) -> String? {
        if let fileName = fileName {
            let file = bundle.bundlePath.stringByAppendingPathComponent(path: fileName)
            return load(file: file)
        }
        return nil
    }

    @objc public class func load(bundles: [Bundle], fileName: String?) -> String? {
        var value: String?
        for bundle in bundles {
            value = load(bundle: bundle, fileName: fileName)
            if value != nil {
                break
            }
        }
        return value
    }

    @objc public class func load(bundled fileName: String?) -> String? {
        return load(bundle: Bundle.main, fileName: fileName)
    }
}
