//  The converted code is limited to 2 KB.
//  Upgrade your plan to remove this limitation.
//
//  Converted to Swift 4 by Swiftify v4.2.6846 - https://objectivec2swift.com/
//
//  Directory.swift
//  Utilities
//
//  Created by Qiang Huang on 10/8/18.
//  Copyright © 2018 dYdX. All rights reserved.
//
import Foundation

@objc public class Directory: NSObject {
    @objc public static var user: String = {
        NSHomeDirectory()
    }()

    @objc public static var document: String? = {
        let paths = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)
        return paths.first
    }()

    @objc public static var library: String? = {
        let paths = NSSearchPathForDirectoriesInDomains(.libraryDirectory, .userDomainMask, true)
        return paths.first
    }()

    @objc public static var cache: String? = {
        let paths = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true)
        return paths.first
    }()

    @objc public static var bundle: String? = {
        Bundle.main.bundlePath
    }()

    @objc public static func download() -> String? {
        return folder(named: "download", in: cache)
    }

    @objc public static func temp() -> String? {
        let folder = NSTemporaryDirectory()
        if folder != "" {
            return folder
        } else {
            return document
        }
    }

    @objc public static func folder(named path: String?, in folder: String?) -> String? {
        if let path = path, let folder = folder as NSString? {
            return folder.appendingPathComponent(path)
        }
        return nil
    }

    @objc public static func userFolder(_ path: String?) -> String? {
        return folder(named: path, in: user)
    }

    @objc public static func documentFolder(_ path: String?) -> String? {
        return folder(named: path, in: document)
    }

    @objc public static func libraryFolder(_ path: String?) -> String? {
        return folder(named: path, in: library)
    }

    @objc public static func downloadFolder(_ path: String?) -> String? {
        return folder(named: path, in: download())
    }

    @objc public static func cacheFolder(_ path: String?) -> String? {
        return folder(named: path, in: cache)
    }

    @objc public static func bundleFolder(_ path: String?) -> String? {
        return folder(named: path, in: bundle)
    }

    @objc public static func pictures() -> String? {
        return folder(named: "pictures", in: user)
    }

    @objc public static func isFolder(_ path: String?) -> Bool {
        if let path = path {
            var isDir: ObjCBool = false
            FileManager.default.fileExists(atPath: path, isDirectory: &isDir)
            return isDir.boolValue
        }
        return false
    }

    @objc public static func exists(_ path: String?) -> Bool {
        var isDir: ObjCBool = false
        let exist: Bool = FileManager.default.fileExists(atPath: path ?? "", isDirectory: &isDir)
        return exist && isDir.boolValue
    }

    @objc public static func parent(of path: String?) -> String? {
        if let path = path as NSString? {
            return path.deletingLastPathComponent
        }
        return nil
    }

    @objc public static func ensure(_ path: String?) -> Bool {
        if let path = path {
            if path == "/" || path == "" {
                return true
            } else if exists(path) {
                return true
            } else {
                let parent = self.parent(of: path)
                if ensure(parent) {
                    try? FileManager.default.createDirectory(atPath: path, withIntermediateDirectories: true, attributes: nil)
                    return true
                } else {
                    return false
                }
            }
        }
        return true
    }

    @objc public static func delete(_ path: String?) {
        if let path = path {
            try? FileManager.default.removeItem(atPath: path)
        }
    }

    @objc public static func all(in parent: String?, extension fileExtension: String? = nil) -> [String]? {
        if let path = parent {
            if let array = try? FileManager.default.contentsOfDirectory(atPath: path) {
                let lowercaseExtension = fileExtension?.lowercased()
                var items: [String] = []

                for child in array {
                    if let fullPath = self.folder(named: child, in: path) {
                        if lowercaseExtension == nil || (fullPath as NSString).pathExtension.lowercased() == lowercaseExtension {
                            items.append(fullPath)
                        }
                    }
                }
                return items
            }
        }
        return nil
    }

    @objc public static func folders(in parent: String?) -> [String]? {
        if let path = parent {
            if let array = try? FileManager.default.contentsOfDirectory(atPath: path) {
                var folders: [String] = []
                for child: String? in array {
                    if let fullPath = self.folder(named: child, in: path) {
                        if isFolder(fullPath) {
                            folders.append(fullPath)
                        }
                    }
                }
                return folders
            }
        }
        return nil
    }

    @objc public static func files(in parent: String?, fileExtension: String?) -> [String]? {
        if let path = parent {
            if let array = try? FileManager.default.contentsOfDirectory(atPath: path) {
                let lowercaseExtension = fileExtension?.lowercased()
                var files: [String] = []
                for child: String in array {
                    if let fullPath = self.folder(named: child, in: path) {
                        if !isFolder(fullPath) {
                            if lowercaseExtension == nil || (fullPath as NSString).pathExtension.lowercased() == lowercaseExtension {
                                files.append(fullPath)
                            }
                        }
                    }
                }
                return files
            }
        }
        return nil
    }
}
