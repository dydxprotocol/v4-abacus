//  Converted to Swift 4 by Swiftify v4.2.6846 - https://objectivec2swift.com/
//
//  File.swift
//  Utilities
//
//  Created by Qiang Huang on 10/8/18.
//  Copyright © 2018 dYdX. All rights reserved.
//
import Foundation

public class File {
    public class func exists(_ path: String?) -> Bool {
        if let path = path {
            var isDir: ObjCBool = false
            return FileManager.default.fileExists(atPath: path, isDirectory: &isDir)
        }
        return false
    }

    public class func delete(_ path: String?) {
        if let path = path {
            try? FileManager.default.removeItem(atPath: path)
        }
    }

    public class func copy(_ path: String?, to toPath: String?) -> Bool {
        return copy(path, to: toPath, forced: false)
    }

    public class func copy(_ path: String?, to toPath: String?, forced: Bool) -> Bool {
        if let path = path, let toPath = toPath {
            if forced {
                delete(toPath)
            }
            if exists(path) && !exists(toPath) {
                try? FileManager.default.copyItem(atPath: path, toPath: toPath)
                return true
            }
        }
        return false
    }

    public class func move(_ path: String?, to toPath: String?) -> Bool {
        return move(path, to: toPath, forced: true)
    }

    public class func move(_ path: String?, to toPath: String?, forced: Bool) -> Bool {
        if let path = path, let toPath = toPath {
            if forced {
                delete(toPath)
            }
            if exists(path) && !exists(toPath) {
                try? FileManager.default.copyItem(atPath: path, toPath: toPath)
                return true
            }
        }
        return false
    }
}
