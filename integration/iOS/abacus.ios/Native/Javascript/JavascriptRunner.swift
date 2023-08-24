//
//  JavascriptRunner.swift
//  Utilities
//
//  Created by Qiang Huang on 4/21/21.
//  Copyright © 2021 dYdX. All rights reserved.
//

import JavaScriptCore
import WebKit

public typealias JavascriptCompletion = (_ result: Any?) -> Void

@objc public class JavascriptRunner: NSObject {
    let webView = WKWebView(frame: .zero)
    private var file: String?
    private var initialized: Bool = false

    public static func runner(file: String) -> JavascriptRunner {
        return JavascriptRunner(file: file)
    }

    public init(file: String?) {
        self.file = file
        super.init()
    }

    public func load(completed: @escaping (_ successful: Bool) -> Void) {
        if initialized {
            completed(true)
        } else {
            if let fileScript = StringLoader.load(bundles: Bundle.particles, fileName: file) {
                webView.evaluateJavaScript(fileScript) { [weak self] _, _ in
                    let script = """
                        \(fileScript)
                    
                        return Promise.resolve()
                    """
                    self?.webView.callAsyncJavaScript(script, arguments: [:], in: nil, in: .defaultClient) { [weak self] result in
                        switch result {
                        case .success:
                            self?.initialized = true
                            completed(true)
                            
                        case .failure:
                            self?.initialized = false
                            completed(false)
                        }
                    }
                }
            } else {
                initialized = true
                completed(true)
            }
        }
    }

    public func run(script: String, completion: JavascriptCompletion?) {
        let time = Date()
        webView.evaluateJavaScript(script) { result, error in
            DispatchQueue.main.async {
                let interval = Date().timeIntervalSince(time)
                // Console.shared.log("Javascript time interval: \(interval)")
                if error == nil {
                    completion?(result)
                } else {
                    completion?(nil)
                }
            }
        }
    }

    private func buildFunction(function named: String, params: [Any]?) -> String {
        if let params = params {
            let parser = Parser()
            let paramStrings: [String] = params.map { item in
                if item is String {
                    return "'\(item)'"
                } else {
                    return parser.asString(item) ?? "null"
                }
            }
            let paramString = paramStrings.joined(separator: ", ")
            return "\(named)(\(paramString))"
        } else {
            return "\(named)()"
        }
    }

    public func invoke(className: String?, function named: String, params: [Any]?, completion: JavascriptCompletion?) {
        let time = Date()
        let function = self.buildFunction(function: named, params: params)
        let script = (className != nil) ? ("""
                return \(className!).\(function);
            """) : ("""
                return \(function);
            """)
        self.webView.callAsyncJavaScript(script, in: nil, in: .defaultClient) { result in
            DispatchQueue.main.async {
                let interval = Date().timeIntervalSince(time)
                // Console.shared.log("Javascript time interval: \(interval)")
                switch result {
                case let .success(data):
                    completion?(data)
                    
                case .failure:
                    completion?(nil)
                }
            }
        }
    }
}
