//
//  JsonLoader.swift
//  abacus.ios
//
//  Created by John Huang on 8/23/23.
//

import Foundation

@objc public class JsonLoader: NSObject {
    @objc public class func load(file: String) -> Any? {
        let fileUrl = URL(fileURLWithPath: file)
        guard let data = try? Data(contentsOf: fileUrl) else {
            return nil
        }
        return try? JSONSerialization.jsonObject(with: data, options: [])
    }

    @objc public class func load(bundle: Bundle, fileName: String?) -> Any? {
        if let fileName = fileName {
            let file = bundle.bundlePath.stringByAppendingPathComponent(path: fileName)
            return load(file: file)
        }
        return nil
    }

    @objc public class func load(bundles: [Bundle], fileName: String?) -> Any? {
        var value: Any?
        for bundle in bundles {
            value = load(bundle: bundle, fileName: fileName)
            if value != nil {
                break
            }
        }
        return value
    }

    @objc public class func load(bundled fileName: String?) -> Any? {
        return load(bundle: Bundle.main, fileName: fileName)
    }
}
