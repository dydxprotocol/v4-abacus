//
//  AbacusStateManager.swift
//  abacus.ios
//
//  Created by John Huang on 9/12/22.
//

import Abacus
import Foundation

public class AbacusStateManager: NSObject {
    public static var shared: AbacusStateManager = {
        let state = AbacusStateManager()
        state.start()
        return state
    }()

    private var session: URLSession = {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 30
        return URLSession(configuration: config, delegate: nil, delegateQueue: nil)
    }()

    private var totalLapsed: TimeInterval = 0.0
    private var totalCount: Int = 0

    public var state: PerpetualState? {
        didSet {
            inspectState()
        }
    }

    private var stateMachine: AppStateMachineProtocol?

    private var sockets = [String: WebsocketConnection]()
    private var rests = [String: [String: HttpCall]]()

    private let runner = ThreadRunner(name: "AbacusRunner")
    private var timer: Timer?
    private var timer2: Timer?
    private var pingTimer: Timer?

    private var orderbookCounter = 0

    private func start() {
        runner.execute { [weak self] in
            let testingV4 = true // Set to true to test v4 networks
            let stateMachine = AppStateMachine(appPlatform: .ios)
            stateMachine.setEnvironment(environment: testingV4 ? "dydxprotocol-staging" : "5")
            (stateMachine as? V3AppStateMachineProtocol)?.setSigner(signer: V3PrivateApiSigner())
            self?.stateMachine = stateMachine

            let ethereumAddress = (self?.stateMachine as? V3AppStateMachineProtocol)?.ethereumAddress()

            self?.processResponse(response: stateMachine.setReadyToConnect(readyToConnect: true))
        }

        timer = Timer.scheduledTimer(withTimeInterval: 10.0, repeats: false) { [weak self] _ in
            self?.runAfterwards()
        }
        timer2 = Timer.scheduledTimer(withTimeInterval: 15.0, repeats: false) { [weak self] _ in
            self?.runAfterwards2()
        }
        pingTimer = Timer.scheduledTimer(withTimeInterval: 5.0, repeats: true) { [weak self] _ in
            self?.ping()
        }
        timer = Timer.scheduledTimer(withTimeInterval: 20.0, repeats: false) { [weak self] _ in
            self?.updateHistoricalPnls()
        }
    }

    private func ping() {
        runner.execute { [weak self] in
            self?.processResponse(response: self?.stateMachine?.ping())
        }
    }

    private func runAfterwards() {
        runner.execute { [weak self] in
            self?.processResponse(response: self?.stateMachine?.setMarket(market: "ETH-USD"))
            self?.processResponse(response: self?.stateMachine?.trade(data: "MARKET", type: .type))
            self?.processResponse(response: self?.stateMachine?.trade(data: "1.09", type: .size))

            self?.processResponse(response: (self?.stateMachine as? V4AppStateMachineProtocol)?.setWalletCosmoAddress(cosmoAddress: "dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art"))
            // cosmos1s5uz95gw634sk9ht3yxeqtwh3se3xaua6rltxn
        }
    }

    private func runAfterwards2() {
        runner.execute { [weak self] in
            self?.processResponse(response: (self?.stateMachine as? V3AppStateMachineProtocol)?.setMarket(market: "ETH-USD"))
        }
    }

    private func updateHistoricalPnls() {
        runner.execute { [weak self] in
            self?.processResponse(response: self?.stateMachine?.updateHistoricalPnl())
        }
    }

    private func processResponse(response: AppStateResponse?) {
        let chainHelper = (stateMachine as? V4AppStateMachineProtocol)?.chainHelper
        if let response = response {
            DispatchQueue.main.async { [weak self] in
                if let self = self {
                    self.state = response.state
                    let marketIds = self.state?.marketIds()
                    if let size = marketIds?.count, size > 0 {
                        if let marketIds = marketIds {
                            let marketId = marketIds[0]
                            let market = self.state?.market(marketId: marketId as String)
                            let name = market?.market
                        }
                    }
                    let subaccount = self.state?.subaccount(subaccountNumber: 0)
                    let quoteBalance = subaccount?.quoteBalance?.current

                    self.process(requests: response.networkRequests)
                }
            }
        }
    }

    private func process(requests: NetworkRequests?) {
        processSocketRequests(requests: requests?.socketRequests)
        processHttpRequests(requests: requests?.restRequests)
    }

    private func processSocketRequests(requests: [SocketRequest]?) {
        if let requests = requests {
            for request in requests {
                processSocketRequest(request: request)
            }
        }
    }

    private func processSocketRequest(request: SocketRequest) {
        let url = request.url
        switch request.type {
        case .socketconnect:
            let socket = WebsocketConnection(url: url.urlString)
            sockets[url.urlString] = socket
            socket.connect(connected: { [weak self] connected in
                let time = Date()
                self?.runner.execute {
                    if let stateResponse = self?.stateMachine?.setSocketConnected(url: url, socketConnected: connected) {
                        let lapsed = Date().timeIntervalSince(time)
                        self?.printAverageTime(lapsed: lapsed)
                        self?.processResponse(response: stateResponse)
                    }
                }
                if connected == false {
                    if self?.sockets[url.urlString] == socket {
                        self?.sockets[url.urlString] = nil
                    }
                }
            }, received: { [weak self] data in
                let time = Date()
                self?.runner.execute {
                    if let stateResponse = self?.stateMachine?.processSocketResponse(url: url, text: data) {
                        let lapsed = Date().timeIntervalSince(time)
                        self?.printAverageTime(lapsed: lapsed)
                        self?.processResponse(response: stateResponse)
                    }
                }
            })

        case .sockettext:
            sockets[url.urlString]?.send(data: request.text)

        case .socketclose:
            sockets[url.urlString]?.close()
            sockets.removeValue(forKey: url.urlString)

        default:
            break
        }
    }

    private func printAverageTime(lapsed: TimeInterval) {
        totalLapsed += lapsed
        totalCount += 1
        let average = totalLapsed / Double(totalCount)
        print("Lapsed average \(lapsed), \(average)")
    }

    private func processHttpRequests(requests: [RestRequest]?) {
        if let requests = requests {
            for request in requests {
                processHttpRequest(request: request)
            }
        }
    }

    private func processHttpRequest(request: RestRequest) {
        let url = request.url
        let verb = request.verb
        if rests[url.urlString] == nil {
            rests[url.urlString] = [String: HttpCall]()
        }
        let call = HttpCall(url: url.urlString, session: session)
        
        let headersMap = (request.headers != nil) ? ParamsHelper.map(params: request.headers!)?.mapValues({ value in
            "\(value)"
        }) : nil

        call.request(verb: request.verb, headers: headersMap, body: request.body) { [weak self] text, _ in
            if let text = text {
                let time = Date()
                self?.runner.execute {
                    if let stateResponse = self?.stateMachine?.processHttpResponse(url: url, text: text) {
                        let lapsed = Date().timeIntervalSince(time)
                        self?.printAverageTime(lapsed: lapsed)
                        self?.processResponse(response: stateResponse)
                    }
                }
            }
            DispatchQueue.main.async {
                self?.rests[url.urlString]?.removeValue(forKey: verb.rawValue)
            }
        }
        rests[url.urlString]?[verb.rawValue] = call
    }

    public func setMarket(market: String) {
        runner.execute { [weak self] in
            if let response = self?.stateMachine?.setMarket(market: market) {
                self?.processResponse(response: response)
            }
        }
    }

    private func inspectState() {
        let market = state?.market(marketId: "ETH-USD")
        let orderbook = state?.marketOrderbook(marketId: "ETH-USD")
        if orderbook != nil {
            orderbookCounter += 1
        }
        if let asks = orderbook?.asks {
//            print("-----------Orderbook start \(orderbookCounter)-----------")
//            var previousPrice = 0.0
//            for i in 0 ..< asks.a.size {
//                let ask = asks.a.get(index: i)
//                if let size = ask?.size, let price = ask?.price, let depth = ask?.depth {
//                    print("Price: \(price), Size: \(size), Depth: \(depth)")
//                    previousPrice = price
//                }
//            }
            print("Total Asks: \(asks.count)")
//            print("-----------Orderbook end-----------")
        }
        if let bids = orderbook?.bids {
//            print("-----------Orderbook start \(orderbookCounter)-----------")
//            orderbookCounter += 1
//            var previousPrice = 0.0
//            for i in 0 ..< asks.a.size {
//                let ask = asks.a.get(index: i)
//                if let size = ask?.size, let price = ask?.price, let depth = ask?.depth {
//                    print("Price: \(price), Size: \(size), Depth: \(depth)")
//                    previousPrice = price
//                }
//            }
            print("Total Bids: \(bids.count)")
//            print("-----------Orderbook end-----------")
        }
    }
}
