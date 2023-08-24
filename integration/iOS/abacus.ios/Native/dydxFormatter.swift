//
//  dydxFormatter.swift
//  abacus.ios
//
//  Created by John Huang on 8/23/23.
//

import Foundation

public final class dydxFormatter: NSObject {
    public static var shared: dydxFormatter = {
        let formatter = dydxFormatter()
        return formatter
    }()

    private var priceFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter
    }()

    private var significantDigitsFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.minimumIntegerDigits = 1
//        formatter.minimumFractionDigits = 2
//        formatter.maximumFractionDigits = 6
        return formatter
    }()

    private var percentFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.roundingMode = .up
        formatter.numberStyle = .decimal
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter
    }()

    private var countFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        return formatter
    }()

    private var decimalFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        return formatter
    }()

    private var rawFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .none
        return formatter
    }()

    private var ordinalFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .ordinal
        return formatter
    }()

    public var intervalFormatter: DateComponentsFormatter = {
        let formatter = DateComponentsFormatter()
        formatter.calendar = Calendar.current
        formatter.unitsStyle = .abbreviated
        formatter.allowedUnits = [.second, .minute, .hour, .day, .month, .year]
        formatter.maximumUnitCount = 1
        formatter.allowsFractionalUnits = false
        formatter.zeroFormattingBehavior = .dropAll
        return formatter
    }()

    public var fullIntervalFormatter: DateComponentsFormatter = {
        let formatter = DateComponentsFormatter()
        formatter.calendar = Calendar.current
        formatter.unitsStyle = .full
        formatter.allowedUnits = [.second, .minute, .hour, .day, .month, .year]
        formatter.maximumUnitCount = 1
        formatter.allowsFractionalUnits = false
        formatter.zeroFormattingBehavior = .dropAll
        return formatter
    }()

    public var timeFormatter: DateComponentsFormatter = {
        let formatter = DateComponentsFormatter()
        formatter.calendar = Calendar.current
        formatter.unitsStyle = .positional
        formatter.allowedUnits = [.second, .minute, .hour]
        formatter.maximumUnitCount = 2
        formatter.allowsFractionalUnits = false
        formatter.zeroFormattingBehavior = .pad
        return formatter
    }()

    public var clockFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.calendar = Calendar.current
        formatter.locale = Locale.current
        formatter.dateFormat = "hh:mm:ss"
        return formatter
    }()

    public var dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.calendar = Calendar.current
        formatter.locale = Locale.current
        formatter.dateStyle = .short
        formatter.timeStyle = .none
        return formatter
    }()

    public var datetimeFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.calendar = Calendar.current
        formatter.locale = Locale.current
        formatter.dateStyle = .short
        formatter.timeStyle = .short
        return formatter
    }()

    public var epochFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.calendar = Calendar.current
        formatter.locale = Locale.current
        formatter.dateFormat = "MMM dd, hh"
        return formatter
    }()

    public var dateIntervalFormatter: DateIntervalFormatter = {
        let formatter = DateIntervalFormatter()
        formatter.timeStyle = .none
        formatter.dateStyle = .medium
        return formatter
    }()

    public func format(number: Double?) -> String? {
        if let number = number {
            return format(number: NSNumber(value: number))
        }
        return nil
    }

    public func format(number: NSNumber?) -> String? {
        if let number = number {
            if let decimal = number as? NSDecimalNumber {
                return "\(decimal)"
            } else {
                if number.doubleValue.isFinite {
                    return "\(number)"
                } else {
                    return "∞"
                }
            }
        } else {
            return nil
        }
    }

    public func condensed(number: Double?, size: String?) -> String? {
        if let number = number {
            return condensed(number: NSNumber(value: number), size: size)
        }
        return nil
    }

    public func condensed(number: NSNumber?, size: String?) -> String? {
        if let double = number?.doubleValue, double < 1000.0 {
            return localFormatted(number: number, size: size)
        } else {
            return condensed(number: number, digits: 2)
        }
    }

    public func condensed(number: Double?, digits: Int = 4) -> String? {
        if let number = number {
            return condensed(number: NSNumber(value: number), digits: digits)
        }
        return nil
    }

    public func condensed(number: NSNumber?, digits: Int = 4) -> String? {
        if let number = number {
            let postfix = ["", "K", "M", "B", "T"]
            var value = number.decimalValue
            var index = 0
            while value > 1000.0 && index < (postfix.count - 1) {
                value = value / 1000.0
                index += 1
            }
            significantDigitsFormatter.minimumFractionDigits = digits
            significantDigitsFormatter.maximumFractionDigits = digits
            if let numberString = significantDigitsFormatter.string(from: NSDecimalNumber(decimal: value)) {
                return "\(numberString)\(postfix[index])"
            }
        }
        return nil
    }

    public func dollarVolume(number: Double?, digits: Int = 2) -> String? {
        if let number = number {
            return dollarVolume(number: NSNumber(value: number), digits: digits)
        }
        return nil
    }

    public func dollarVolume(number: NSNumber?, digits: Int = 2) -> String? {
        return nil
    }

    public func dollar(number: Double?, size: String? = nil) -> String? {
        if let number = number {
            return dollar(number: NSNumber(value: number), size: size)
        }
        return nil
    }

    public func dollar(number: NSNumber?, size: String? = nil) -> String? {
        return nil
    }

    public func dollar(number: Double?, digits: Int) -> String? {
        if let number = number {
            return dollar(number: NSNumber(value: number), digits: digits)
        }
        return nil
    }

    public func dollar(number: NSNumber?, digits: Int) -> String? {
        return nil
    }

    public func localFormatted(number: Double?, size: String?) -> String? {
        if let number = number {
            return localFormatted(number: NSNumber(value: number), size: size)
        }
        return nil
    }

    public func localFormatted(number: NSNumber?, size: String?) -> String? {
        if let size = size {
            let digits = digits(size: size)
            return localDecimal(number: number, digits: digits)
        }
        return localDecimal(number: number, digits: 2)
    }

    public func localFormatted(number: Double?, digits: Int) -> String? {
        if let number = number {
            return localFormatted(number: NSNumber(value: number), digits: digits)
        }
        return nil
    }

    public func localFormatted(number: NSNumber?, digits: Int) -> String? {
        if let number = number {
            let rounded = rounded(number: number, digits: digits)
            return localDecimal(number: rounded, digits: digits)
        } else {
            return nil
        }
    }

    private func localDecimal(number: NSNumber?, digits: Int) -> String? {
        if let number = number {
            decimalFormatter.minimumFractionDigits = max(digits, 0)
            decimalFormatter.maximumFractionDigits = max(digits, 0)
            return decimalFormatter.string(from: number)
        }
        return nil
    }

    public func digits(size: String) -> Int {
        let components = size.components(separatedBy: ".")
        if components.count == 2 {
            return components.last?.count ?? 0
        } else {
            return ((components.first?.count ?? 1) - 1) * -1
        }
    }

    /*
     xxxxxx,yyyyy or xxxxx.yyyyy
     */
    public func raw(number: NSNumber?, size: String?) -> String? {
        if let number = number {
            let size = size ?? "0.01"
            let digits = digits(size: size)
            let rounded = rounded(number: number, digits: digits)
            return raw(number: rounded, digits: digits)
        } else {
            return nil
        }
    }

    /*
     xxxxx.yyyyy
     */
    public func decimalRaw(number: NSNumber?, size: String?) -> String? {
        return raw(number: number, size: size)?.replacingOccurrences(of: ",", with: ".")
    }

    /*
     xxxxxx,yyyyy or xxxxx.yyyyy
     */
    public func raw(number: NSNumber?, digits: Int) -> String? {
        if let value = number?.doubleValue {
            if value.isFinite {
                if let number = number {
                    rawFormatter.minimumFractionDigits = max(digits, 0)
                    rawFormatter.maximumFractionDigits = max(digits, 0)
                    rawFormatter.roundingMode = .halfUp
                    return rawFormatter.string(from: number)
                } else {
                    return nil
                }
            } else {
                return "∞"
            }
        } else {
            return nil
        }
    }

    /*
     xxxxx.yyyyy
     */
    public func naturalRaw(number: NSNumber?) -> String? {
        return number?.description
    }

    /*
     xxxxxx,yyyyy or xxxxx.yyyyy
     */
    public func naturalFormatted(number: NSNumber?) -> String? {
        return number?.description(withLocale: Locale.current)
    }

    /*
     xxx xxx,yyyyy or xx,xxx.yyyyy
     */
    public func naturalLocalFormatted(number: NSNumber?) -> String? {
        if let decimalSeparator = Locale.current.decimalSeparator {
            let localFormatted = localFormatted(number: number, size: "0.01")
            let naturalFormatted = naturalFormatted(number: number)

            let naturalFormattedComponents = naturalFormatted?.components(separatedBy: decimalSeparator)
            if naturalFormattedComponents?.count == 2, let beforeDecimal = localFormatted?.components(separatedBy: decimalSeparator).first, let afterDecimal = naturalFormattedComponents?.last {
                return "\(beforeDecimal)\(decimalSeparator)\(afterDecimal)"
            } else {
                return localFormatted
            }
        } else {
            return naturalFormatted(number: number)
        }
    }

    private func rounded(number: NSNumber, digits: Int) -> NSNumber {
        if number.doubleValue.isFinite {
            if digits >= 0 {
                return number
            } else {
                let double = number.doubleValue
                let reversed = digits * -1
                let divideBy = pow(10, UInt(reversed))
                let roundedUp = Int(double / Double(divideBy)) * divideBy
                return NSNumber(value: roundedUp)
            }
        } else {
            return number
        }
    }

    public func percent(number: Double?, digits: Int, minDigits: Int? = nil) -> String? {
        if let number = number {
            return percent(number: NSNumber(value: number), digits: digits, minDigits: minDigits)
        }
        return nil
    }

    public func percent(number: NSNumber?, digits: Int, minDigits: Int? = nil) -> String? {
        if let number = number {
            if number.doubleValue.isFinite {
                let percent = NSNumber(value: number.doubleValue * 100.0)
                percentFormatter.minimumFractionDigits = minDigits ?? digits
                percentFormatter.maximumFractionDigits = digits
                if let formatted = percentFormatter.string(from: percent) {
                    return "\(formatted)%"
                } else {
                    return nil
                }
            } else {
                return "—"
            }
        } else {
            return nil
        }
    }

    public func leverage(number: Double?, digits: Int = 2) -> String? {
        if let number = number {
            return leverage(number: NSNumber(value: number), digits: digits)
        }
        return nil
    }

    public func leverage(number: NSNumber?, digits: Int = 2) -> String? {
        return nil
    }

    public func interval(time: Date?) -> String? {
        if let time = time {
            var interval = time.timeIntervalSince(Date())
            if interval < 0.0 {
                interval *= -1
            }
            return intervalFormatter.string(from: interval)
        } else {
            return nil
        }
    }

    public func fullInterval(time: Date?) -> String? {
        if let time = time {
            var interval = time.timeIntervalSince(Date())
            if interval < 0.0 {
                interval *= -1
            }
            return fullIntervalFormatter.string(from: interval)
        } else {
            return nil
        }
    }

    public func time(time: Date?) -> String? {
        if let time = time {
            var interval = time.timeIntervalSince(Date())
            if interval < 0.0 {
                interval *= -1
            }
            if interval >= 3600 {
                timeFormatter.allowedUnits = [.second, .minute, .hour]
            } else {
                timeFormatter.allowedUnits = [.second, .minute]
            }
            return timeFormatter.string(from: interval)
        } else {
            return nil
        }
    }

    public func clock(time: Date?) -> String? {
        if let time = time {
            return clockFormatter.string(from: time)
        } else {
            return nil
        }
    }

    public func shorten(ethereumAddress: String?) -> String? {
        return ethereumAddress
    }

    public func marker(ethereumAddress: String?) -> String? {
        return ethereumAddress
    }

    public func range(start: Date?, end: Date?) -> String? {
        if let start = start {
            if let end = end {
                return dateIntervalFormatter.string(from: start, to: end)
            } else {
                return "from \(dateFormatter.string(from: start))"
            }
        } else {
            if let end = end {
                return " to \(dateFormatter.string(from: end))"
            } else {
                return nil
            }
        }
    }

    public func epoch(date: Date?) -> String? {
        if let date = date {
            return dateFormatter.string(from: date)
        } else {
            return nil
        }
    }

    public func dateAndTime(date: Date?) -> String? {
        if let date = date {
            return datetimeFormatter.string(from: date)
        } else {
            return nil
        }
    }

    public func multiple(of tickText: String, is sizeText: String) -> Bool {
        return false
    }

    func pow(_ base: Int, _ power: UInt) -> Int {
        var answer: Int = 1
        for _ in 0 ..< power { answer *= base }
        return answer
    }

    public func ordinal(number: Int?) -> String? {
        if let number = number {
            return ordinalFormatter.string(from: NSNumber(value: number))
        } else {
            return nil
        }
    }
}
