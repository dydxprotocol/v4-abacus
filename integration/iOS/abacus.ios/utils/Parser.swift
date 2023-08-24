//
//  Parser.swift
//  abacus.ios
//
//  Created by John Huang on 8/23/23.
//

import Foundation

@objc open class Parser: NSObject {
    static let inputFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.locale = Locale.current
        return formatter
    }()

    @objc public static var standard: Parser = {
        Parser()
    }()

    @objc open func conditioned(_ data: Any?) -> Any? {
        return data
    }

    @objc open func asString(_ data: Any?) -> String? {
        var temp: String?
        if let string = data as? NSString {
            temp = string as String
        } else if let string = data as? String {
            temp = string
        } else if let convertible = data as? CustomStringConvertible {
            temp = convertible.description
        }
        if temp == "<null>" {
            temp = nil
        }
        return temp?.trim()
    }

    @objc open func asValidString(_ data: Any?) -> String? {
        if let string = asString(data), string != "" {
            return string
        }
        return nil
    }

    @objc open func asStrings(_ data: Any?) -> [String]? {
        if let strings = data as? [String] {
            return strings
        } else if let string = asString(data) {
            let lines = string.components(separatedBy: ",")
            var strings = [String]()
            for line in lines {
                if let trimmed = line.trim() {
                    strings.append(trimmed)
                }
            }
            return strings
        }
        return nil
    }

    @objc open func asNumber(_ data: Any?) -> NSNumber? {
        if let number = data as? NSNumber {
            return number
        } else if let int = data as? Int {
            return NSNumber(value: int)
        } else if let float = data as? Float {
            return NSNumber(value: float)
        } else if let double = data as? Double {
            return NSNumber(value: double)
        } else if let string = data as? String {
            if let int = Int(string) {
                return NSNumber(integerLiteral: int)
            } else if let float = Double(string) {
                return NSNumber(floatLiteral: float)
            }
        }
        return nil
    }
    
    @objc open func asNumbers(_ data: Any?) -> [NSNumber]? {
        if let numbers = data as? [NSNumber] {
            return numbers
        } else {
            return asStrings(data)?.compactMap({ string in
                return asNumber(string)
            })
        }
    }

    @objc open func asInputNumber(_ data: Any?) -> NSNumber? {
        return asInputDecimal(data)
//        if let string = asString(data) {
//            return type(of: self).inputFormatter.number(from: string)
//        } else {
//            return asNumber(data)
//        }
    }

    @objc open func asDecimal(_ data: Any?) -> NSDecimalNumber? {
        if let number = data as? NSDecimalNumber {
            return number
        } else if let number = data as? NSNumber {
            return NSDecimalNumber(decimal: number.decimalValue)
        } else if let int = data as? Int {
            return NSDecimalNumber(value: int)
        } else if let float = data as? Float {
            return NSDecimalNumber(value: float)
        } else if let double = data as? Double {
            return NSDecimalNumber(value: double)
        } else if let string = data as? String {
            if let decimal = Decimal(string: string) {
                return NSDecimalNumber(decimal: decimal)
            }
        }
        return nil
    }

    @objc open func asInputDecimal(_ data: Any?) -> NSNumber? {
        if let string = asString(data) {
            let number = NSDecimalNumber(string: string, locale: Locale.current)
            return number.decimalValue.isFinite ? number : nil
        } else {
            return asDecimal(data)
        }
    }

    @objc open func asBoolean(_ data: Any?) -> NSNumber? {
        if let string = (data as? String)?.lowercased() {
            if string == "y" || string == "1" || string == "true" || string == "yes" || string == "on" {
                return true
            } else if string == "n" || string == "0" || string == "false" || string == "no" || string == "off" {
                return false
            }
        } else if let boolean = data as? Bool {
            return NSNumber(value: boolean)
        } else if let boolean = data as? Int {
            return NSNumber(value: boolean)
        }
        return nil
    }

    @objc open func asDictionary(_ data: Any?) -> [String: Any]? {
        return data as? [String: Any]
    }

    @objc open func asArray(_ data: Any?) -> [Any]? {
        if let data = data {
            if data is Dictionary<AnyHashable, Any> {
                return nil
            } else if let data = data as? [Any] {
                return data
            } else {
                return [data]
            }
        }
        return nil
    }

    open func asInt(_ data: Any?) -> Int? {
        if let number = data as? NSNumber {
            return number.intValue
        } else if let int = data as? Int {
            return int
        } else if let float = data as? Float {
            return Int(float + 0.5)
        } else if let double = data as? Double {
            return Int(double + 0.5)
        } else if let string = data as? String {
            if let int = Int(string) {
                return int
            } else if let float = Double(string), float.isFinite, float < Double(Int.max), float > Double(Int.min) {
                return Int(float)
            }
        } else if let date = data as? Date {
            return Int(date.timeIntervalSince1970 * 1000)
        }
        return nil
    }

    @objc open func asURL(_ data: Any?) -> URL? {
        if let string = asString(data) {
            return URL(string: string)
        }
        return nil
    }
}

extension NSObject {
    @objc open var parser: Parser {
        return Parser.standard
    }
}
