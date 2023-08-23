//
//  AbacusFileSystemImp.swift
//  dydxStateManager
//
//  Created by John Huang on 7/17/23.
//

import Abacus
import Foundation
import Utilities

public final class AbacusFileSystemImp: Abacus.FileSystemProtocol {
    public func readTextFile(location: FileLocation, path: String) -> String? {
        guard let rootFolder = location.folderPath else {
            return nil
        }

        do {
            return try String(contentsOfFile: rootFolder.stringByAppendingPathComponent(path: path), encoding: .utf8)
        } catch {
            Console.shared.log("AbacusFileSystemImp: unable to read file \(path): \(error)")
            return nil
        }
    }

    public func writeTextFile(path: String, text: String) -> Bool {
        guard let rootFolder = FileLocation.appdocs.folderPath else {
            return false
        }

        do {
            let filePath = rootFolder.stringByAppendingPathComponent(path: path)
            Directory.ensure(filePath.stringByDeletingLastPathComponent)
            File.delete(filePath)
            try text.write(toFile: filePath, atomically: true, encoding: .utf8)
            return true
        } catch {
            Console.shared.log("AbacusFileSystemImp: unable to write file \(path): \(error)")
            return false
        }
    }
}

private extension FileLocation {
    var folderPath: String? {
        switch self {
        case .appbundle:
            return Directory.bundle
        case .appdocs:
            return Directory.document
        default:
            return nil
        }
    }
}
