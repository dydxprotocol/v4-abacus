//
//  NativeRest.swift
//  abacus.ios
//
//  Created by John Huang on 5/25/23.
//

import Foundation
import Abacus

public class NativeRest: RestProtocol {
    private var calls = [String: [String: HttpCall]]()
    
    private var session: URLSession = {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 30
        return URLSession(configuration: config, delegate: nil, delegateQueue: nil)
    }()

    
    public func delete(url: String, headers: [String : String]?, callback: @escaping (String?, KotlinInt) -> Void) {
        run(url: url, verb: .delete_, headers: headers, body: nil, callback: callback)
    }
    
    public func get(url: String, headers: [String : String]?, callback: @escaping (String?, KotlinInt) -> Void) {
        run(url: url, verb: .get, headers: headers, body: nil, callback: callback)
    }
    
    public func post(url: String, headers: [String : String]?, body: String?, callback: @escaping (String?, KotlinInt) -> Void) {
        run(url: url, verb: .post, headers: headers, body: body, callback: callback)
    }
    
    public func put(url: String, headers: [String : String]?, body: String?, callback: @escaping (String?, KotlinInt) -> Void) {
        run(url: url, verb: .put, headers: headers, body: body, callback: callback)
    }
    
    private func run(url: String, verb: HttpVerb, headers: [String : String]?, body: String?, callback: @escaping (String?, KotlinInt) -> Void) {
        
        let call = HttpCall(url: url, session: session)

        call.request(verb: verb, headers: headers, body: body) { [weak self] text, code in
            if url.contains("/v4/addresses") {
                let x = 0
            }
            if let text = text {
                let time = Date()
                DispatchQueue.main.async {
                    self?.calls[url]?.removeValue(forKey: verb.rawValue)
                }
                
                callback(text, KotlinInt(integerLiteral: code))
            }
        }
        DispatchQueue.main.async {
            self.rememberCall(url: url, verb: verb.rawValue, call: call)
        }
    }
    
    private func rememberCall(url: String, verb: String, call: HttpCall) {
        var urlCalls = calls[url] ?? [String: HttpCall]()
        urlCalls[verb] = call
        calls[url] = urlCalls
    }
}
