//
//  ThreadRunner.swift
//  abacus.ios
//
//  Created by Rui Huang on 9/13/22.
//

import Abacus
import Foundation

final class ThreadRunner {
    private var blocks = [() -> Void]()
    private var thread: Thread?
    
    private let lock = NSLock()
    private let semaphore = DispatchSemaphore(value: 0)
    private let threadName: String
    
    init(name: String) {
        threadName = name
    }
    
    func execute(block: @escaping () -> Void) {
        lock.lock()
        blocks.append(block)
        lock.unlock()
        
        semaphore.signal()
        
        if thread == nil {
            thread = Thread(block: { [weak self] in
                while true {
                    self?.semaphore.wait()
                    if let item = self?.blocks.first {
                        item()
                        self?.lock.lock()
                        self?.blocks.removeFirst()
                        self?.lock.unlock()
                    } else {
                        assertionFailure("Synchronization error: item not available.")
                    }
                }
            })
            thread?.name = threadName
            thread?.start()
        }
    }
}
