package exchange.dydx.abacus.tests.payloads

class EnvironmentsMock {
    internal val environments = """
        {
            "environments":[
               {
                  "comment":"V3 Mainnet",
                  "environment":"1",
                  "ethereumChainId":"1",
                  "string":"v3 MainNet",
                  "stringKey":"CHAIN.V3_MAINNET",
                  "isMainNet":true,
                  "version":"v3",
                  "maxSubaccountNumber":0,
                  "endpoints":{
                     "indexers":[
                        {
                            "api":"https://api.dydx.exchange",
                            "socket":"wss://api.dydx.exchange"
                        }
                     ],
                     "configs":"https://dydx-v4-shared-resources.vercel.app",
                     "marketImageUrl": "https://trader-fe.vercel.app/currenties/{asset}.svg"
                  }
               },
               {
                  "comment":"V3 Staging",
                  "environment":"5",
                  "ethereumChainId":"5",
                  "string":"v3 Staging",
                  "stringKey":"CHAIN.V3_GOERLI",
                  "isMainNet":false,
                  "version":"v3",
                  "maxSubaccountNumber":0,
                  "endpoints":{
                     "indexers":[
                        {                     
                            "api":"https://api.stage.dydx.exchange",
                            "socket":"wss://api.stage.dydx.exchange"
                        }
                     ],
                     "configs":"https://dydx-v4-shared-resources.vercel.app",
                     "marketImageUrl": "https://trader-fe.vercel.app/currenties/{asset}.svg"
                  }
               },
               {
                  "comment":"V4 Dev",
                  "environment":"dydxprotocol-dev",
                  "ethereumChainId":"5",
                  "dydxChainId":"dydxprotocol-testnet",
                  "string":"v4 Dev",
                  "stringKey":"CHAIN.V4_DEVNET",
                  "isMainNet":false,
                  "version":"v4",
                  "maxSubaccountNumber":127,
                  "endpoints":{
                     "indexers":[
                        {
                           "api":"http://indexer.v4dev.dydx.exchange",
                           "socket":"wss://indexer.v4dev.dydx.exchange"
                        }
                     ],
                     "faucet":"http://faucet.v4dev.dydx.exchange",
                     "validators":[
                        "http://validator.v4dev.dydx.exchange"
                     ],
                     "0xsquid":"https://testnet.api.0xsquid.com",
                     "configs":"https://dydx-v4-shared-resources.vercel.app",
                     "marketImageUrl": "https://trader-fe.vercel.app/currenties/{asset}.svg"
                  }
               },
               {
                  "comment":"V4 Dev 2",
                  "environment":"dydxprotocol-dev-2",
                  "ethereumChainId":"5",
                  "dydxChainId":"dydxprotocol-testnet",
                  "string":"v4 Dev 2",
                  "stringKey":"CHAIN.V4_DEVNET_2",
                  "isMainNet":false,
                  "version":"v4",
                  "maxSubaccountNumber":127,
                  "endpoints":{
                     "indexers":[
                        {
                           "api":"http://dev2-indexer-apne1-lb-public-2076363889.ap-northeast-1.elb.amazonaws.com",
                           "socket":"ws://dev2-indexer-apne1-lb-public-2076363889.ap-northeast-1.elb.amazonaws.com"
                        }
                     ],
                     "validators":[
                        "http://35.75.227.118"
                     ],
                     "0xsquid":"https://testnet.api.0xsquid.com",
                     "configs":"https://dydx-v4-shared-resources.vercel.app",
                     "marketImageUrl": "https://trader-fe.vercel.app/currenties/{asset}.svg"
                  }
               },
               {
                  "comment":"V4 Dev 3",
                  "environment":"dydxprotocol-dev-3",
                  "ethereumChainId":"5",
                  "dydxChainId":"dydxprotocol-testnet",
                  "string":"v4 Dev 3",
                  "stringKey":"CHAIN.V4_DEVNET_3",
                  "isMainNet":false,
                  "version":"v4",
                  "maxSubaccountNumber":127,
                  "endpoints":{
                     "0xsquid":"https://testnet.api.0xsquid.com",
                     "configs":"https://dydx-v4-shared-resources.vercel.app",
                     "marketImageUrl": "https://trader-fe.vercel.app/currenties/{asset}.svg"
                  }
               },
               {
                  "comment":"V4 Dev 4",
                  "environment":"dydxprotocol-dev-4",
                  "ethereumChainId":"5",
                  "dydxChainId":"dydxprotocol-testnet",
                  "string":"v4 Dev 4",
                  "stringKey":"CHAIN.V4_DEVNET_4",
                  "isMainNet":false,
                  "version":"v4",
                  "maxSubaccountNumber":127,
                  "endpoints":{
                     "indexers":[
                        {
                           "api":"http://indexer.v4dev4.dydx.exchange",
                           "socket":"ws://indexer.v4dev4.dydx.exchange"
                        }
                     ],
                     "validators":[
                        "http://validator.v4dev4.dydx.exchange"
                     ],
                     "0xsquid":"https://testnet.api.0xsquid.com",
                     "configs":"https://dydx-v4-shared-resources.vercel.app",
                     "marketImageUrl": "https://trader-fe.vercel.app/currenties/{asset}.svg"
                  }
               },
               {
                  "comment":"V4 Dev 5",
                  "environment":"dydxprotocol-dev-5",
                  "ethereumChainId":"5",
                  "dydxChainId":"dydxprotocol-testnet",
                  "string":"v4 Dev 5",
                  "stringKey":"CHAIN.V4_DEVNET_5",
                  "isMainNet":false,
                  "version":"v4",
                  "maxSubaccountNumber":127,
                  "endpoints":{
                     "indexers":[
                        {
                           "api":"http://dev5-indexer-apne1-lb-public-1721328151.ap-northeast-1.elb.amazonaws.com",
                           "socket":"ws://dev5-indexer-apne1-lb-public-1721328151.ap-northeast-1.elb.amazonaws.com"
                        }
                     ],
                     "validators":[
                        "http://18.223.78.50"
                     ],
                     "0xsquid":"https://testnet.api.0xsquid.com",
                     "configs":"https://dydx-v4-shared-resources.vercel.app",
                     "marketImageUrl": "https://trader-fe.vercel.app/currenties/{asset}.svg"
                  }
               },
               {
                  "comment":"V4 Staging",
                  "environment":"dydxprotocol-staging",
                  "ethereumChainId":"5",
                  "dydxChainId":"dydxprotocol-testnet",
                  "string":"v4 Staging",
                  "stringKey":"CHAIN.V4_STAGING",
                  "isMainNet":false,
                  "version":"v4",
                  "maxSubaccountNumber":127,
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
                     "configs":"https://dydx-v4-shared-resources.vercel.app",
                     "marketImageUrl": "https://trader-fe.vercel.app/currenties/{asset}.svg"
                  }
               },
               {
                  "comment":"V4 Public Testnet #2",
                  "environment":"dydxprotocol-testnet",
                  "ethereumChainId":"5",
                  "dydxChainId":"dydx-testnet-2",
                  "string":"v4 Public Testnet #2",
                  "stringKey":"CHAIN.V4_TESTNET2",
                  "isMainNet":false,
                  "version":"v4",
                  "maxSubaccountNumber":127,
                  "endpoints":{
                     "indexers":[
                        {
                           "api":"https://indexer.v4testnet2.dydx.exchange",
                           "socket":"wss://indexer.v4testnet2.dydx.exchange"
                        }
                     ],
                     "validators":[
                        "https://validator.v4testnet2.dydx.exchange",
                        "https://dydx-testnet.nodefleet.org",
                        "https://dydx-testnet-archive.allthatnode.com:26657"
                     ],
                     "0xsquid":"https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app",
                     "configs":"https://dydx-v4-shared-resources.vercel.app",
                     "faucet":"https://faucet.v4testnet2.dydx.exchange",
                     "marketImageUrl": "https://trader-fe.vercel.app/currenties/{asset}.svg"
                  }
               }
            ],
            "defaultEnvironment":"dydxprotocol-staging"
         }
    """.trimIndent()
}