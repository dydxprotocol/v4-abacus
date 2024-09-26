package exchange.dydx.abacus.tests.payloads

class EnvironmentsMock {
    internal val environments = """
        {
           "deployments":{
              "MAINNET":null,
              "TESTNET":{
                 "environments":[
                    "dydxprotocol-testnet"
                 ],
                 "default":"dydxprotocol-testnet"
              },
              "DEV":{
                 "environments":[
                    "dydxprotocol-staging",
                    "dydxprotocol-testnet"
                 ],
                 "default":"dydxprotocol-staging"
              }
           },
           "tokens": {
               "dydxprotocol-testnet": {
                   "chain": {
                       "name": "DYDX",
                       "denom": "dv4tnt",
                       "image": "/currencies/dydx.png"
                   },
                   "usdc": {
                       "name": "USDC",
                       "denom": "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
                       "image": "/currencies/usdc.png"
                   }
               }
           },
           "links": {
               "dydxprotocol-testnet": {
                    "tos":"https://dydx.exchange/v4-terms",
                    "privacy":"https://dydx.exchange/privacy",
                    "mintscan":"https://testnet.mintscan.io/dydx-testnet/txs/{tx_hash}",
                    "mintscanBase":"https://testnet.mintscan.io/dydx-testnet",
                    "documentation":"https://v4-teacher.vercel.app/",
                    "community":"https://discord.com/invite/dydx",
                    "feedback":"https://docs.google.com/forms/d/e/1FAIpQLSezLsWCKvAYDEb7L-2O4wOON1T56xxro9A2Azvl6IxXHP_15Q/viewform",
                    "launchIncentive":"https://dydx.exchange/v4-launch-incentive",
                    "equityTiersLearnMore":"https://help.dydx.trade/en/articles/171918-equity-tiers-and-rate-limits"
               }
           },
           "wallets": {
               "dydxprotocol-testnet": {
                    "walletconnect":{
                       "client":{
                          "name":"dYdX v4",
                          "description":"dYdX v4 App",
                          "iconUrl":"/logos/dydx-x.png"
                       },
                       "v1":{
                          "bridgeUrl":"wss://api.stage.dydx.exchange/wc/"
                       },
                       "v2":{
                          "projectId":"47559b2ec96c09aed9ff2cb54a31ab0e"
                       }
                    },
                    "walletSegue":{
                       "callbackUrl":"/walletsegue"
                    },
                    "images":"/wallets/"
               }
           },
           "environments":{
              "dydxprotocol-staging":{
                 "name":"v4 Staging",
                 "ethereumChainId":"5",
                 "dydxChainId":"dydxprotocol-testnet",
                 "squidIntegratorId": "dYdX-api",
                 "rewardsHistoryStartDateMs": "1704844800000",
                 "isMainNet":false,
                 "endpoints":{
                    "indexers":[
                       {
                          "api":"https://indexer.v4staging.dydx.exchange",
                          "socket":"wss://indexer.v4staging.dydx.exchange"
                       }
                    ],
                    "faucet":"https://faucet.v4staging.dydx.exchange",
                    "validators":[
                       "https://validator.v4staging.dydx.exchange"
                    ],
                    "0xsquid":"https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app",
                    "geo": "https://api.dydx.exchange/v4/geo"
                 },
                 "featureFlags":{
                 },
                 "governance": {
                    "newMarketProposal": {
                       "initialDepositAmount": 10000000,
                       "delayBlocks": 900,
                       "newMarketsMethodology": "https://docs.google.com/spreadsheets/d/1zjkV9R7R_7KMItuzqzvKGwefSBRfE-ZNAx1LH55OcqY/edit?usp=sharing"
                    }
                 }
              },
              "dydxprotocol-testnet":{
                 "name":"v4 Public Testnet",
                 "ethereumChainId":"5",
                 "dydxChainId":"dydxprotocol-testnet",
                 "squidIntegratorId": "dYdX-api",
                 "rewardsHistoryStartDateMs": "1704844800000",
                 "isMainNet":false,
                 "endpoints":{
                    "indexers":[
                       {
                          "api":"https://dydx-testnet.imperator.co",
                          "socket":"wss://dydx-testnet.imperator.co"
                       }
                    ],
                    "validators":[
                       "https://dydx-testnet.nodefleet.org",
                       "https://dydx-testnet-archive.allthatnode.com:26657/XZvMM41hESf8PJrEQiTzbCOMVyFca79R",
                       "https://test-dydx.kingnodes.com/"
                    ],
                    "0xsquid":"https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app",
                    "faucet":"https://faucet.v4testnet.dydx.exchange",
                    "geo": "https://api.dydx.exchange/v4/geo"
                 },
                 "featureFlags":{
                  },
                 "governance": {
                    "newMarketProposal": {
                       "initialDepositAmount": 10000000,
                       "delayBlocks": 900,
                       "newMarketsMethodology": "https://docs.google.com/spreadsheets/d/1zjkV9R7R_7KMItuzqzvKGwefSBRfE-ZNAx1LH55OcqY/edit?usp=sharing"
                    }
                 }
              }
           }
        }
    """.trimIndent()
}
