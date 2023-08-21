//
//  abacus_iosTests.swift
//  abacus.iosTests
//
//  Created by Rui Huang on 7/1/22.
//

import XCTest
@testable import abacus_ios
import Abacus

class abacus_iosTests: XCTestCase {
    func testAbacus() {
        let perp = PerpTradingStateMachine()
        let mock = AbacusMockData()
        
        testOnce(perp: perp, mock: mock)
        
        testOnce(perp: perp, mock: mock)
    }
    
    func testOnce(perp: PerpTradingStateMachine, mock: AbacusMockData) {
        var time = Date()
        var state = perp.loadMarkets(mock: mock)
        var interval = Date().timeIntervalSince(time)
        print(interval)
        time = Date()
        state = perp.loadMarketsChanged(mock: mock)
        interval = Date().timeIntervalSince(time)
        print(interval)
        time = Date()
        state = perp.loadMarketsConfigurations(mock: mock)
        interval = Date().timeIntervalSince(time)
        print(interval)
        time = Date()
    }
}
