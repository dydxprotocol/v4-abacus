//
//  Bundle+UIBundle.swift
//  abacus.ios
//
//  Created by John Huang on 8/23/23.
//

import Foundation

#if _macOS
    extension Bundle {
        open func loadNibNamed(_ name: String, owner: Any?, options: [Any]? = nil) -> [Any]? {
            var nibContents: NSArray?
            if Bundle.ui().loadNibNamed(name, owner: owner, topLevelObjects: &nibContents) {
                return nibContents as? [Any]
            }
            return nil
        }
    }
#endif

public extension Bundle {
    @objc static var particles: [Bundle] = {
        var bundles = [Bundle]()
        bundles.append(Bundle.main)
        return bundles
    }()
}

public extension Bundle {
    var version: String? {
        return infoDictionary?["CFBundleShortVersionString"] as? String
    }

    var build: String? {
        return infoDictionary?["CFBundleVersion"] as? String
    }
    
    var versionAndBuild: String? {
        if let version = version {
            if let build = build {
                return "\(version).\(build)"
            } else {
                return version
            }
        }
        return nil
    }

    var versionPretty: String? {
        if let version = version {
            return "v\(version)"
        }
        return nil
    }
    
    func versionCompare(otherVersion: String) -> ComparisonResult {
        guard let version = version else {
            return .orderedAscending
        }

        let versionDelimiter = "."

        var versionComponents = version.components(separatedBy: versionDelimiter) // <1>
        var otherVersionComponents = otherVersion.components(separatedBy: versionDelimiter)

        let zeroDiff = versionComponents.count - otherVersionComponents.count // <2>

        if zeroDiff == 0 { // <3>
            // Same format, compare normally
            return version.compare(otherVersion, options: .numeric)
        } else {
            let zeros = Array(repeating: "0", count: abs(zeroDiff)) // <4>
            if zeroDiff > 0 {
                otherVersionComponents.append(contentsOf: zeros) // <5>
            } else {
                versionComponents.append(contentsOf: zeros)
            }
            return versionComponents.joined(separator: versionDelimiter)
                .compare(otherVersionComponents.joined(separator: versionDelimiter), options: .numeric) // <6>
        }
    }
}
