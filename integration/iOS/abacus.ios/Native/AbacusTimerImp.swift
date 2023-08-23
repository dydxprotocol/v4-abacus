//
//  AbacusTimerImp.swift
//  dydxStateManager
//
//  Created by John Huang on 7/29/23.
//

import Foundation

import Abacus
import Utilities

public final class AbacusTimerImp: Abacus.TimerProtocol {
    public func schedule(delay: Double, repeat: KotlinDouble?, block: @escaping () -> KotlinBoolean) -> LocalTimerProtocol {
        return LocalTimer(delay: delay, timeInterval: `repeat`?.doubleValue, block: {
            let `continue` = block()
            return `continue`.boolValue
        })
    }
}

public final class LocalTimer: LocalTimerProtocol {
    private let delay: Double
    private let timeInterval: Double?
    private let block: () -> Bool
    private var timer: Timer?

    init(delay: Double, timeInterval: Double?, block: @escaping () -> Bool) {
        self.delay = delay
        self.timeInterval = timeInterval
        self.block = block
        DispatchQueue.main.async {[weak self] in
            self?.timer = Timer.scheduledTimer(withTimeInterval: delay, repeats: false, block: { [weak self] _ in
                if let self = self {
                    let `continue` = block()
                    if `continue`, let timeInterval = timeInterval {
                        DispatchQueue.main.async {
                            self.timer = Timer.scheduledTimer(withTimeInterval: timeInterval, repeats: true, block: { [weak self] _ in
                                let `continue` = block()
                                if !`continue` {
                                    self?.cancel()
                                }
                            })
                        }
                    } else {
                        self.cancel()
                    }
                }
            })
        }
    }

    deinit {
        cancel()
    }

    public func cancel() {
        timer?.invalidate()
        timer = nil
    }
}
