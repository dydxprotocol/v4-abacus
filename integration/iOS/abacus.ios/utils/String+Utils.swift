//
//  String+Utils.swift
//  abacus.ios
//
//  Created by John Huang on 8/23/23.
//

import Foundation

extension String {
    public var xmlEscaped: String {
        return replacingOccurrences(of: "&", with: "&amp;")
            .replacingOccurrences(of: "\"", with: "&quot;")
            .replacingOccurrences(of: "'", with: "&#39;")
            .replacingOccurrences(of: ">", with: "&gt;")
            .replacingOccurrences(of: "<", with: "&lt;")
    }

    public func encodeUrl() -> String? {
        return addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)
    }
    
    public func decodeUrl() -> String? {
        return removingPercentEncoding
    }
}

extension Optional where Wrapped: Collection {
   public var isNilOrEmpty: Bool {
        self?.isEmpty ?? true
   }
}

extension String {
    public var hasText: Bool {
        return self != ""
    }
    
    public var isNotEmpty: Bool {
        !isEmpty
    }

    public func trim() -> String? {
        let trimmed = trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmed == "" ? nil : trimmed
    }

    public func begins(with string: String) -> Bool {
        return hasPrefix(string)
    }

    public func ends(with string: String) -> Bool {
        return hasSuffix(string)
    }

    public static func same(_ text1: String?, as text2: String?) -> Bool {
        if let text1 = text1 {
            return text1 == text2
        } else {
            return text2 == nil
        }
    }

    public static func hex(of number: Int) -> String? {
        return String(format: "0x%X", number)
    }

    public static func trim(_ lines: [String]) -> [String] {
        return lines.filter({ (line: String) -> Bool in
            line.hasText
        })
    }

    public static func ascending(string1: String?, string2: String?) -> Bool {
        if string1 != nil {
            if string2 != nil {
                return string1! < string2!
            } else {
                return false
            }
        } else {
            return true
        }
    }
}

extension String {
    public var lastPathComponent: String {
        return (self as NSString).lastPathComponent
    }

    public var pathExtension: String {
        return (self as NSString).pathExtension
    }

    public var stringByDeletingLastPathComponent: String {
        return (self as NSString).deletingLastPathComponent
    }

    public var stringByDeletingPathExtension: String {
        return (self as NSString).deletingPathExtension
    }

    public var pathComponents: [String] {
        return (self as NSString).pathComponents
    }

    public func stringByAppendingPathComponent(path: String) -> String {
        let nsSt = self as NSString
        return nsSt.appendingPathComponent(path)
    }

    public func stringByAppendingPathExtension(ext: String) -> String? {
        let nsSt = self as NSString
        return nsSt.appendingPathExtension(ext)
    }
}

extension String {
    public func asInts() -> [Int] {
        let elements = components(separatedBy: ",")
        return elements.compactMap({ (item) -> Int? in
            Int(item)
        })
    }

    public var digits: String {
        filter {
            ("0" ... "9").contains($0)
        }
    }

    private func removingPrefixZeros() -> String {
        let decimalSeparator = Locale.current.decimalSeparator ?? "."
        // Removing preceding 0s, i.e. 050 -> 50
        var input = self
        while input.hasPrefix("0") && !input.hasPrefix("0\(decimalSeparator)") && input.count > 1 {
            input.removeFirst()
        }
        return input
    }
    
    private func removingNegation() -> String {
        var input = self
        let isNegative = input.first == "-"
        if isNegative {
            input.removeFirst()
        }
        return input
    }
    
    private func isValidNumberForParsing() -> Bool {
        NumberFormatter().number(from: self) != nil
    }
    
    /// returns the string as a whole number, truncated (round towards 0)
    /// - Returns: the string as a whole number, truncated
    public func truncateToWholeNumber() -> String? {
        guard self.isValidNumberForParsing() || self == "-" else { return nil }

        let decimalSeparator = Locale.current.decimalSeparator ?? "."
        
        var input = self
        
        let isNegative = input.first == "-"
        input = input.removingNegation()
        
        input = input.removingPrefixZeros()
        
        if let range = input.range(of: decimalSeparator) {
            input = String(input[startIndex..<range.lowerBound])
        }
        
        return isNegative ? "-" + input : input
    }
    
    /// Returns the string as a decimal number. Supports trailing decimal and trailing zeros
    /// - Parameter shouldTreatEmptyStringAsZero: If true, empty string will evaluate to "0" instead of ""
    /// - Returns: the string as a decimal number
    public func cleanAsDecimalNumber() -> String? {
        let decimalSeparator = Locale.current.decimalSeparator ?? "."
        guard self.isValidNumberForParsing() || self == decimalSeparator || self == "-" || self == "-\(decimalSeparator)" else { return nil }
        
        var input = self
        
        let isNegative = input.first == "-"
        input = input.removingNegation()
        
        // If the input string starts with a decimal separator, return "0."
        if input.hasPrefix(decimalSeparator) {
            input = "0" + input
        }
        
        input = input.removingPrefixZeros()
        
        return isNegative ? "-" + input : input
    }
}

public extension StringProtocol {
    func index<S: StringProtocol>(of string: S, options: String.CompareOptions = []) -> Index? {
        range(of: string, options: options)?.lowerBound
    }

    func endIndex<S: StringProtocol>(of string: S, options: String.CompareOptions = []) -> Index? {
        range(of: string, options: options)?.upperBound
    }

    func indices<S: StringProtocol>(of string: S, options: String.CompareOptions = []) -> [Index] {
        var indices: [Index] = []
        var startIndex = self.startIndex
        while startIndex < endIndex,
              let range = self[startIndex...]
              .range(of: string, options: options) {
            indices.append(range.lowerBound)
            startIndex = range.lowerBound < range.upperBound ? range.upperBound :
                index(range.lowerBound, offsetBy: 1, limitedBy: endIndex) ?? endIndex
        }
        return indices
    }

    func ranges<S: StringProtocol>(of string: S, options: String.CompareOptions = []) -> [Range<Index>] {
        var result: [Range<Index>] = []
        var startIndex = self.startIndex
        while startIndex < endIndex,
              let range = self[startIndex...]
              .range(of: string, options: options) {
            result.append(range)
            startIndex = range.lowerBound < range.upperBound ? range.upperBound :
                index(range.lowerBound, offsetBy: 1, limitedBy: endIndex) ?? endIndex
        }
        return result
    }
}

public extension String {
    var length: Int {
        return count
    }

    subscript(i: Int) -> String {
        return self[i ..< i + 1]
    }

    func substring(fromIndex: Int) -> String {
        return self[min(fromIndex, length) ..< length]
    }

    func substring(toIndex: Int) -> String {
        return self[0 ..< max(0, toIndex)]
    }

    subscript(r: Range<Int>) -> String {
        let range = Range(uncheckedBounds: (lower: max(0, min(length, r.lowerBound)),
                                            upper: min(length, max(0, r.upperBound))))
        let start = index(startIndex, offsetBy: range.lowerBound)
        let end = index(start, offsetBy: range.upperBound - range.lowerBound)
        return String(self[start ..< end])
    }

    var isNumeric: Bool {
        guard count > 0 else { return false }
        let nums: Set<Character> = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"]
        return Set(self).isSubset(of: nums)
    }

    func slice(seperators: [String]) -> [String?]? {
        if let seperator = seperators.first {
            var remainingSeperators = seperators
            remainingSeperators.removeFirst()
            if let range = range(of: seperator) {
                var slices = [String?]()
                let beforeSeperator = prefix(upTo: range.lowerBound)
                let before = String(beforeSeperator)
                slices.append(before)

                let afterSeperator = suffix(from: range.upperBound)
                let after = String(afterSeperator).trim()
                if let remaining = after?.slice(seperators: remainingSeperators) {
                    slices.append(contentsOf: remaining)
                }
                return slices
            }
        }
        return [self]
    }

    func slice(any: [String]) -> [String]? {
        return slice(any: any, prefix: nil)
    }

    private func slice(any: [String], prefix: String?) -> [String]? {
        if let seperator = any.first {
            var slices = [String]()
            var rest = any
            rest.removeFirst()

            let components = self.components(separatedBy: seperator)

            for index in 0 ..< components.count {
                let component = components[index]
                if index == 0 {
                    if let subSlices = component.slice(any: rest, prefix: prefix) {
                        slices.append(contentsOf: subSlices)
                    }
                } else {
                    if let subSlices = component.slice(any: rest, prefix: seperator) {
                        slices.append(contentsOf: subSlices)
                    }
                }
            }

            return slices
        } else {
            if let prefix = prefix {
                return ["\(prefix)\(self)"]
            } else {
                return nil
            }
        }
    }
}

public extension String {
    init?(value: Double?) {
        if let value = value {
            self = String(value)
        } else {
            return nil
        }
    }
    
    func substring(with: NSRange) -> String {
        let startIndex = with.lowerBound
        let endIndex = with.upperBound
        let range = startIndex ..< endIndex
        return String(self[range])
    }

    func detectUrl() -> [NSRange]? {
        if let detector = try? NSDataDetector(types: NSTextCheckingResult.CheckingType.link.rawValue) {
            let matches = detector.matches(in: self, options: [], range: NSRange(location: 0, length: utf16.count))
            var ranges = [NSRange]()
            for match in matches {
                ranges.append(match.range)
            }
            return ranges.count > 0 ? ranges : nil
        }
        return nil
    }

    func detectHttpUrl() -> [NSRange]? {
        if let ranges = detectUrl() {
            var httpRanges = [NSRange]()
            for range in ranges {
                let url = substring(with: range)
                if url.lowercased().starts(with: "http:") || url.lowercased().starts(with: "https:") {
                    httpRanges.append(range)
                }
            }
            return httpRanges.count > 0 ? httpRanges : nil
        }
        return nil
    }
}

class DataDetector {
    private class func _find(all type: NSTextCheckingResult.CheckingType,
                             in string: String, iterationClosure: (String) -> Bool) {
        guard let detector = try? NSDataDetector(types: type.rawValue) else { return }
        let range = NSRange(string.startIndex ..< string.endIndex, in: string)
        let matches = detector.matches(in: string, options: [], range: range)
        loop: for match in matches {
            for i in 0 ..< match.numberOfRanges {
                let nsrange = match.range(at: i)
                let startIndex = string.index(string.startIndex, offsetBy: nsrange.lowerBound)
                let endIndex = string.index(string.startIndex, offsetBy: nsrange.upperBound)
                let range = startIndex ..< endIndex
                guard iterationClosure(String(string[range])) else { break loop }
            }
        }
    }

    class func find(all type: NSTextCheckingResult.CheckingType, in string: String) -> [String] {
        var results = [String]()
        _find(all: type, in: string) {
            results.append($0)
            return true
        }
        return results
    }

    class func first(type: NSTextCheckingResult.CheckingType, in string: String) -> String? {
        var result: String?
        _find(all: type, in: string) {
            result = $0
            return false
        }
        return result
    }
}

// MARK: String extension

extension String {
    var detectedLinks: [String] { DataDetector.find(all: .link, in: self) }
    var detectedFirstLink: String? { DataDetector.first(type: .link, in: self) }
    var detectedURLs: [URL] { detectedLinks.compactMap { URL(string: $0) } }
    var detectedFirstURL: URL? {
        guard let urlString = detectedFirstLink else { return nil }
        return URL(string: urlString)
    }
}

extension String {
    public func pad(to length: Int, with: String) -> String {
        if self.length < length {
            return "\(self)\(with)".pad(to: length, with: with)
        } else {
            return self
        }
    }
    
    public func prefix(_ with: String, length: Int) -> String {
        if self.length < length {
            return "\(with)\(self)".prefix(with, length: length)
        } else {
            return self
        }
    }
    
    public func removeTrailing(_ removing: String) -> String {
        if self.ends(with: removing) {
            return self.substring(toIndex: self.length - removing.length).removeTrailing(removing)
        } else {
            return self
        }
    }
}

extension String {
    public var jsonDictionary: [String: Any]? {
        guard let data = data(using: .utf8) else {
            return nil
        }
        return try? JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
    }
}
