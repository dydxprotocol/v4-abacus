//
//  NativeFileSystem.swift
//  abacus.ios
//
//  Created by John Huang on 6/1/23.
//

import Foundation

import Abacus
import Foundation

extension String {
    public var stringByDeletingLastPathComponent: String {
        return (self as NSString).deletingLastPathComponent
    }
}

final class NativeFileSystem: NSObject, Abacus.FileSystemProtocol {
    func deleteFile(location: FileLocation, path: String, callback: @escaping (KotlinBoolean) -> Void) {
        let filePath = (location == FileLocation.appbundle) ? Directory.bundleFolder(path) : Directory.documentFolder(path)
        if File.exists(filePath) {
            File.delete(filePath)
        }
        callback(KotlinBoolean(bool: true))
    }

    func itemExists(location: FileLocation, path: String, callback: @escaping (KotlinBoolean, KotlinBoolean) -> Void) {
        let filePath = (location == FileLocation.appbundle) ? Directory.bundleFolder(path) : Directory.documentFolder(path)
        let exists = File.exists(filePath)
        let isDir = exists ? Directory.isFolder(filePath) : false
        callback(KotlinBoolean(bool: exists), KotlinBoolean(bool: isDir))
    }

    func readDirectory(location: FileLocation, path: String, callback: @escaping (String?) -> Void) {
        var jsonString: String?
        if let directoryPath = (location == FileLocation.appbundle) ? Directory.bundleFolder(path) : Directory.documentFolder(path), Directory.isFolder(directoryPath), let items = Directory.folders(in: directoryPath) {
            let pathes = items.map { dir in
                dir.replacingOccurrences(of: directoryPath, with: "")
            }
            jsonString = string(json: pathes)
        }
        callback(jsonString)
    }

    private func string(json: Any?) -> String? {
        if let json = json, let data = try? JSONSerialization.data(withJSONObject: json, options: JSONSerialization.WritingOptions.prettyPrinted) {
            return String(data: data, encoding: String.Encoding.utf8)
        }
        return nil
    }

    func readTextFile(location: FileLocation, path: String, callback: @escaping (String?) -> Void) {
        var text: String?
        if let file = (location == FileLocation.appbundle) ? Directory.bundleFolder(path) : Directory.documentFolder(path), !Directory.isFolder(file) {
            text = try? String(contentsOfFile: file)
        }
        callback(text)
    }

    func writeTextFile(location: FileLocation, path: String, text: String, callback: @escaping (KotlinBoolean) -> Void) {
        var written = false
        if let file = (location == FileLocation.appbundle) ? Directory.bundleFolder(path) : Directory.documentFolder(path) {
            File.delete(file)
            _ = Directory.ensure(file.stringByDeletingLastPathComponent)
            if let data = text.data(using: .utf8) {
                do {
                    try data.write(to: URL(fileURLWithPath: file))
                    written = true
                } catch {
                }
            }
        }
        callback(KotlinBoolean(bool: written))
    }
}
