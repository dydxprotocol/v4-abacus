//
//  Websocket.swift
//  abacus.ios
//
//  Created by John Huang on 8/30/22.
//

import Abacus
import Foundation

public class WebsocketConnection: NSObject {
    public var url: String

    public var connected: ((Bool) -> Void)?
    public var received: ((String) -> Void)?

    private var dataTask: URLSessionWebSocketTask? {
        didSet {
            if dataTask !== oldValue {
                dataTask?.maximumMessageSize = 16 * 1024 * 1024
                oldValue?.cancel(with: .normalClosure, reason: nil)
                if let dataTask = dataTask {
                    dataTask.resume()
                    receive()
                }
            }
        }
    }

    private var isConnected: Bool? {
        didSet {
            if isConnected != oldValue {
                connected?(isConnected == true)
            }
        }
    }

    public init(url: String) {
        self.url = url
        super.init()
    }

    public func close() {
        dataTask?.cancel(with: .normalClosure, reason: nil)
        dataTask = nil
    }

    public func connect(connected: @escaping (Bool) -> Void, received: @escaping (String) -> Void) {
        self.connected = connected
        self.received = received
        connect()
    }
    
    private func connect() {
        if let url = URL(string: url) {
            let urlSession = URLSession(configuration: .default, delegate: self, delegateQueue: nil)
            dataTask = urlSession.webSocketTask(with: url)
        } else {
        }
    }

    private func reset() {
        close()
        connect()
    }

    public func send(data: Any?) {
        if let string = data as? String {
            let message = URLSessionWebSocketTask.Message.string(string)
            dataTask?.send(message, completionHandler: { [weak self] error in
                if error != nil {
                    self?.reset()
                }
            })
        } else if let data = data as? Data {
            let message = URLSessionWebSocketTask.Message.data(data)
            dataTask?.send(message, completionHandler: { [weak self] error in
                if error != nil {
                    self?.reset()
                }
            })
        } else if let data = data as? [String: Any] {
            if let string = string(json: data) {
                let message = URLSessionWebSocketTask.Message.string(string)
                dataTask?.send(message, completionHandler: { [weak self] error in
                    if error != nil {
                        self?.reset()
                    }
                })
            }
        }
    }

    private func string(json: Any?) -> String? {
        if let json = json {
            if let data = try? JSONSerialization.data(withJSONObject: json, options: JSONSerialization.WritingOptions.prettyPrinted) {
                return String(data: data, encoding: String.Encoding.utf8)
            }
        }
        return nil
    }

    private func receive() {
        dataTask?.receive { [weak self] result in
            if let self = self {
                switch result {
                case .failure:
                    self.dataTask = nil

                case let .success(message):
                    switch message {
                    case let .string(text):
                        self.dispatch(text: text)

                    case let .data(data):
                        self.dispatch(data: data)

                    @unknown default:
                        break
                    }
                    self.receive()
                }
            }
        }
    }

    open func dispatch(text: String) {
        DispatchQueue.main.async { [weak self] in
            self?.received?(text)
        }
    }

    open func dispatch(data: Data) {
    }
}

@available(iOS 13.0, *)
extension WebsocketConnection: URLSessionWebSocketDelegate {
    public func urlSession(_ session: URLSession, webSocketTask: URLSessionWebSocketTask, didOpenWithProtocol protocol: String?) {
        DispatchQueue.main.async {[weak self] in
            self?.isConnected = true
        }
    }

    public func urlSession(_ session: URLSession, webSocketTask: URLSessionWebSocketTask, didCloseWith closeCode: URLSessionWebSocketTask.CloseCode, reason: Data?) {
        DispatchQueue.main.async {[weak self] in
            self?.isConnected = false
        }
    }
}
