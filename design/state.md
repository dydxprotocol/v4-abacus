## At Launch ##

Retrieve network configurations

Given the chain configuration, set up endpoints (Staging vs Production)

Retrieve fixed configurations which do not need refreshing

(not handled by state manager) walletsV2
(v3) epoch start

# Extended goal #

We will have staging vs production and the Stage Manager handles that. FE app can set chainId into
the State Manager, which refreshes all data. This gives the FE the ability to provide multiple
environments without restarting

## Foreground State ##

DYDXStateManager has a foreground Boolean flag.
When foregrounded, the state manager will connect the socket, re-establish all subscriptions, and also update configurations

# Socket #
State manager connects to the socket, and subscribe to
markets
account, if wallet is connected
trades, orders, (v4) candles if a current market is selected

# Configs #
The configurations are REST calls to Veronica, including 
network configurations
markets configurations
fee tiers
fee discounts etc
(v3) countires
(v3) leauges
(v3) promotions
(v3) 0x_assets (for deposit)

# Api #
** 24h market candles for all markets with a single call
server time - it is important to synchronize the device time with server time for signature
(v3, v4 native app) Geo location of the device with IP address
(v3) update the candles of selected market, handled by sockets in v4
(v3, v4 native app) get user restriction data
(v3 not covered by state manager yet) resume timer to check deposit and slow withdrawal status

## Timer ##

Data needing timer update
For mobile app, updating on foregrounding is probably sufficient.
For tablet and desktop, updating on timer may be needed. This covers
** 24h market candles

## UI State ##

UI state needs to be passed into the state manager:

#market#: State manager subscribes to the trades, orderbook, and candles channel(v4) for the current market
#historicalPnlPeriod#: State manager tries to fetch the right number of ticks based on the pnlPeriod
#candlePeriod#: (v3) State manager subscribes to the right candles channel based on the candlePeriod

## User State ##

User states to be passed into the state manager:

# ethereumAddress #
(v3) State manager retrieves user object
(v3) user restrictions
(v3) when ethereumAddress is set, and subscribe to the account socket channel
apiKey, starkKey (v3): State manager keeps the info, for API signing

# account number #
(v4) Subaccounts are supported. By setting the account number (0 to 127), the account object is switched,
with the related account data

## Automatic ##

Reachability (network status): If socket is disconnected due to network connection issue, it will reconnect when network is reachable

## User Input ##
Trade
Transfer
(v3) User Profile
(Action) commit input: POST to index service

## How to use in iOS ##

var stateManager = DYDXv3StateManager.companion.create(networkFactory: NetworkFactory(), stateChange: { state, errors in
    // this should be called at the app level, when app is initialized
    // NetworkFactory is an implementation of NetworkFactoryProtocol
    // State object returns the state, containing markets, assets, account, input etc
    // This is running in a worker thread. Client code is responsible for switching to UI thread and handles display
})