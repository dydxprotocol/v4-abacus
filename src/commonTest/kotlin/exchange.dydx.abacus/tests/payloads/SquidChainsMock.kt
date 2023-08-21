package exchange.dydx.abacus.tests.payloads

internal class SquidChainsMock {
    internal val payload = """
{
  "chains": [
    {
      "chainName": "Ethereum",
      "chainType": "evm",
      "rpc": "https://eth-rpc.gateway.pokt.network",
      "networkName": "Ethereum",
      "chainId": 1,
      "nativeCurrency": {
        "name": "Ethereum",
        "symbol": "ETH",
        "decimals": 18,
        "icon": "https://tokens.1inch.io/0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://tokens.1inch.io/0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee.png",
      "blockExplorerUrls": [
        "https://etherscan.io/"
      ],
      "chainNativeContracts": {
        "wrappedNativeToken": "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2",
        "ensRegistry": "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e",
        "multicall": "0x5e227AD1969Ea493B43F840cfF78d08a6fc17796",
        "usdcToken": "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48"
      },
      "axelarContracts": {
        "gateway": "0x4F4495243837681061C4743b74B3eEdf548D56A5",
        "forecallable": ""
      },
      "estimatedRouteDuration": 960,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "Arbitrum",
      "chainType": "evm",
      "rpc": "https://arb1.arbitrum.io/rpc",
      "networkName": "Arbitrum",
      "chainId": 42161,
      "nativeCurrency": {
        "name": "Arbitrum",
        "symbol": "ETH",
        "decimals": 18,
        "icon": "https://s2.coinmarketcap.com/static/img/coins/64x64/11841.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/11841.png",
      "blockExplorerUrls": [
        "https://arbiscan.io/"
      ],
      "chainNativeContracts": {
        "wrappedNativeToken": "0x82af49447d8a07e3bd95bd0d56f35241523fbab1",
        "ensRegistry": "",
        "multicall": "0xcA11bde05977b3631167028862bE2a173976CA11",
        "usdcToken": "0xff970a61a04b1ca14834a43f5de4533ebddb5cc8"
      },
      "axelarContracts": {
        "gateway": "0xe432150cce91c13a887f7D836923d5597adD8E31",
        "forecallable": "0x2d5d7d31F671F86C782533cc367F14109a082712"
      },
      "estimatedRouteDuration": 1800,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "Avalanche",
      "chainType": "evm",
      "rpc": "https://api.avax.network/ext/bc/C/rpc",
      "networkName": "Avalanche",
      "chainId": 43114,
      "nativeCurrency": {
        "name": "Avalanche",
        "symbol": "AVAX",
        "decimals": 18,
        "icon": "https://s2.coinmarketcap.com/static/img/coins/64x64/5805.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://assets.coingecko.com/coins/images/12559/small/coin-round-red.png?1604021818",
      "blockExplorerUrls": [
        "https://snowtrace.io/"
      ],
      "chainNativeContracts": {
        "wrappedNativeToken": "0xB31f66AA3C1e785363F0875A1B74E27b85FD66c7",
        "ensRegistry": "0xa7eebb2926d22d34588497769889cbc2be0a5d97",
        "multicall": "0xcA11bde05977b3631167028862bE2a173976CA11",
        "usdcToken": "0xB97EF9Ef8734C71904D8002F8b6Bc66Dd9c48a6E"
      },
      "axelarContracts": {
        "gateway": "0x5029C0EFf6C34351a0CEc334542cDb22c7928f78",
        "forecallable": ""
      },
      "estimatedRouteDuration": 90,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "Polygon",
      "chainType": "evm",
      "rpc": "https://polygon-rpc.com",
      "networkName": "Polygon",
      "chainId": 137,
      "nativeCurrency": {
        "name": "Polygon",
        "symbol": "MATIC",
        "decimals": 18,
        "icon": "https://s2.coinmarketcap.com/static/img/coins/64x64/3890.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/3890.png",
      "blockExplorerUrls": [
        "https://polygonscan.com/"
      ],
      "chainNativeContracts": {
        "wrappedNativeToken": "0x0d500B1d8E8eF31E21C99d1Db9A6444d3ADf1270",
        "ensRegistry": "",
        "multicall": "0xcA11bde05977b3631167028862bE2a173976CA11",
        "usdcToken": "0x2791Bca1f2de4661ED88A30C99A7a9449Aa84174"
      },
      "axelarContracts": {
        "gateway": "0x6f015F16De9fC8791b234eF68D486d2bF203FBA8",
        "forecallable": "0x2d5d7d31F671F86C782533cc367F14109a082712"
      },
      "estimatedRouteDuration": 360,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "binance",
      "chainType": "evm",
      "rpc": "https://bsc-dataseed.binance.org",
      "networkName": "Binance",
      "chainId": 56,
      "nativeCurrency": {
        "name": "BNB",
        "symbol": "BNB",
        "decimals": 18,
        "icon": "https://s2.coinmarketcap.com/static/img/coins/64x64/1839.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://axelarscan.io/logos/chains/binance.svg",
      "blockExplorerUrls": [
        "https://bscscan.com/"
      ],
      "chainNativeContracts": {
        "wrappedNativeToken": "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c",
        "ensRegistry": "",
        "multicall": "0xcA11bde05977b3631167028862bE2a173976CA11",
        "usdcToken": "0x55d398326f99059fF775485246999027B3197955"
      },
      "axelarContracts": {
        "gateway": "0x304acf330bbE08d1e512eefaa92F6a57871fD895",
        "forecallable": "0x2d5d7d31F671F86C782533cc367F14109a082712"
      },
      "estimatedRouteDuration": 150,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "Fantom",
      "chainType": "evm",
      "rpc": "https://rpc.ankr.com/fantom",
      "networkName": "Fantom",
      "chainId": 250,
      "nativeCurrency": {
        "name": "FTM",
        "symbol": "FTM",
        "decimals": 18,
        "icon": "https://assets.coingecko.com/coins/images/4001/small/Fantom_round.png?1669652346"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/3513.png",
      "blockExplorerUrls": [
        "https://ftmscan.com/"
      ],
      "chainNativeContracts": {
        "wrappedNativeToken": "0x21be370D5312f44cB42ce377BC9b8a0cEF1A4C83",
        "ensRegistry": "",
        "multicall": "0xcA11bde05977b3631167028862bE2a173976CA11",
        "usdcToken": "0x04068DA6C83AFCFA0e13ba15A6696662335D5B75"
      },
      "axelarContracts": {
        "gateway": "0x304acf330bbE08d1e512eefaa92F6a57871fD895",
        "forecallable": "0x2d5d7d31F671F86C782533cc367F14109a082712"
      },
      "estimatedRouteDuration": 70,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "Moonbeam",
      "chainType": "evm",
      "rpc": "https://rpc.api.moonbeam.network",
      "networkName": "Moonbeam",
      "chainId": 1284,
      "nativeCurrency": {
        "name": "Moonbeam",
        "symbol": "GLMR",
        "decimals": 18,
        "icon": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "blockExplorerUrls": [
        "https://moonscan.io/"
      ],
      "chainNativeContracts": {
        "wrappedNativeToken": "0xAcc15dC74880C9944775448304B263D191c6077F",
        "ensRegistry": "",
        "multicall": "0x6477204E12A7236b9619385ea453F370aD897bb2",
        "usdcToken": "0x931715fee2d06333043d11f658c8ce934ac61d0c"
      },
      "axelarContracts": {
        "gateway": "0x4F4495243837681061C4743b74B3eEdf548D56A5",
        "forecallable": ""
      },
      "estimatedRouteDuration": 120,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "celo",
      "chainType": "evm",
      "rpc": "https://forno.celo.org",
      "networkName": "Celo",
      "chainId": 42220,
      "nativeCurrency": {
        "name": "CELO",
        "symbol": "CELO",
        "decimals": 18,
        "icon": "https://assets.coingecko.com/coins/images/11090/small/icon-celo-CELO-color-500.png?1592293590"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://assets.coingecko.com/coins/images/11090/small/icon-celo-CELO-color-500.png?1592293590",
      "blockExplorerUrls": [
        "https://celoscan.io/"
      ],
      "chainNativeContracts": {
        "wrappedNativeToken": "0x471EcE3750Da237f93B8E339c536989b8978a438",
        "ensRegistry": "",
        "multicall": "0xcA11bde05977b3631167028862bE2a173976CA11",
        "usdcToken": "0x765DE816845861e75A25fCA122bb6898B8B1282a"
      },
      "axelarContracts": {
        "gateway": "0xe432150cce91c13a887f7D836923d5597adD8E31",
        "forecallable": "0x2d5d7d31F671F86C782533cc367F14109a082712"
      },
      "estimatedRouteDuration": 90,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "kava",
      "chainType": "evm",
      "rpc": "https://evm2.kava.io",
      "networkName": "Kava",
      "chainId": 2222,
      "nativeCurrency": {
        "name": "Kava",
        "symbol": "KAVA",
        "decimals": 6,
        "icon": "https://s2.coinmarketcap.com/static/img/coins/64x64/4846.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4846.png",
      "blockExplorerUrls": [
        "https://explorer.kava.io/"
      ],
      "chainNativeContracts": {
        "wrappedNativeToken": "0xc86c7C0eFbd6A49B35E8714C5f59D99De09A225b",
        "ensRegistry": "",
        "multicall": "0x30A62aA52Fa099C4B227869EB6aeaDEda054d121",
        "usdcToken": "0xfA9343C3897324496A05fC75abeD6bAC29f8A40f"
      },
      "axelarContracts": {
        "gateway": "0xe432150cce91c13a887f7D836923d5597adD8E31",
        "forecallable": ""
      },
      "estimatedRouteDuration": 120,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "osmosis",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/osmosis",
      "rest": "https://osmosis-1--lcd--full.datahub.figment.io/apikey/6d8baa3d3e97e427db4bd7ffcfb21be4",
      "networkName": "Osmosis",
      "chainId": "osmosis-1",
      "nativeCurrency": {
        "name": "Osmosis",
        "symbol": "OSMO",
        "decimals": 6,
        "icon": "https://assets.coingecko.com/coins/images/16724/small/osmo.png?1632763885"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://assets.coingecko.com/coins/images/16724/small/osmo.png?1632763885",
      "blockExplorerUrls": [
        "https://www.mintscan.io/osmosis"
      ],
      "bip44": {
        "coinType": 118
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "osmo",
        "bech32PrefixAccPub": "osmopub",
        "bech32PrefixValAddr": "osmovaloper",
        "bech32PrefixValPub": "osmovaloperpub",
        "bech32PrefixConsAddr": "osmovalcons",
        "bech32PrefixConsPub": "osmovalconspub"
      },
      "feeCurrencies": [
        {
          "coinDenom": "OSMO",
          "coinMinimalDenom": "uosmo",
          "coinDecimals": 6,
          "coingeckoId": "osmosis"
        }
      ],
      "stakeCurrency": {
        "coinDenom": "OSMO",
        "coinMinimalDenom": "uosmo",
        "coinDecimals": 6,
        "coingeckoId": "osmosis"
      },
      "currencies": [
        {
          "coinDenom": "OSMO",
          "coinMinimalDenom": "uosmo",
          "coinDecimals": 6,
          "coingeckoId": "osmosis"
        }
      ],
      "coinType": 118,
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-208",
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "crescent",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/crescent",
      "rest": "https://mainnet.crescent.network:1317",
      "networkName": "Crescent",
      "chainId": "crescent-1",
      "nativeCurrency": {
        "name": "Crescent",
        "symbol": "CRE",
        "decimals": 6,
        "icon": "https://static-resources.crescent.network/CRE.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://static-resources.crescent.network/CRE.png",
      "blockExplorerUrls": [
        "https://www.mintscan.io/crescent"
      ],
      "bip44": {
        "coinType": 118
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "cre",
        "bech32PrefixAccPub": "crepub",
        "bech32PrefixValAddr": "crevaloper",
        "bech32PrefixValPub": "crevaloperpub",
        "bech32PrefixConsAddr": "crevalcons",
        "bech32PrefixConsPub": "crevalconspub"
      },
      "currencies": [
        {
          "coinDenom": "CRE",
          "coinMinimalDenom": "ucre",
          "coinDecimals": 6,
          "coingeckoId": "crescent"
        }
      ],
      "feeCurrencies": [
        {
          "coinDenom": "CRE",
          "coinMinimalDenom": "ucre",
          "coinDecimals": 6,
          "coingeckoId": "crescent"
        }
      ],
      "stakeCurrency": {
        "coinDenom": "CRE",
        "coinMinimalDenom": "ucre",
        "coinDecimals": 6,
        "coingeckoId": "crescent"
      },
      "coinType": 118,
      "gasPriceStep": {
        "low": 1,
        "average": 1,
        "high": 1
      },
      "features": [
        "stargate",
        "ibc-transfer",
        "no-legacy-stdTx"
      ],
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30,
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-4"
    },
    {
      "chainName": "kujira",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/kujira",
      "rest": "https://lcd.kaiyo.kujira.setten.io",
      "networkName": "Kujira",
      "chainId": "kaiyo-1",
      "nativeCurrency": {
        "name": "Kuji",
        "symbol": "KUJI",
        "decimals": 6,
        "icon": "https://assets.coingecko.com/coins/images/20685/small/kuji-200x200.png?1637557201"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://axelarscan.io/logos/chains/kujira.svg",
      "blockExplorerUrls": [
        "https://kujira.explorers.guru/"
      ],
      "bip44": {
        "coinType": 118
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "kujira",
        "bech32PrefixAccPub": "kujirapub",
        "bech32PrefixValAddr": "kujiravaloper",
        "bech32PrefixValPub": "kujiravaloperpub",
        "bech32PrefixConsAddr": "kujiravalcons",
        "bech32PrefixConsPub": "kujiravalconspub"
      },
      "currencies": [
        {
          "coinDenom": "KUJI",
          "coinMinimalDenom": "ukuji",
          "coinDecimals": 6,
          "coingeckoId": "kujira"
        }
      ],
      "feeCurrencies": [
        {
          "coinDenom": "KUJI",
          "coinMinimalDenom": "ukuji",
          "coinDecimals": 6,
          "coingeckoId": "kujira"
        }
      ],
      "stakeCurrency": {
        "coinDenom": "KUJI",
        "coinMinimalDenom": "ukuji",
        "coinDecimals": 6,
        "coingeckoId": "kujira"
      },
      "coinType": 118,
      "gasPriceStep": {
        "low": 0.01,
        "average": 0.025,
        "high": 0.03
      },
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-9",
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "terra-2",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/terra",
      "rest": "https://phoenix-lcd.terra.dev",
      "networkName": "Terra",
      "chainId": "phoenix-1",
      "nativeCurrency": {
        "name": "Luna",
        "symbol": "LUNA",
        "decimals": 6,
        "icon": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/terra-2.svg"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/terra-2.svg",
      "blockExplorerUrls": [
        "http://finder.terra.money/"
      ],
      "stakeCurrency": {
        "coinDenom": "LUNA",
        "coinMinimalDenom": "uluna",
        "coinDecimals": 6,
        "coingeckoId": "terra-luna-2"
      },
      "bip44": {
        "coinType": 330
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "terra",
        "bech32PrefixAccPub": "terrapub",
        "bech32PrefixValAddr": "terravaloper",
        "bech32PrefixValPub": "terravaloperpub",
        "bech32PrefixConsAddr": "terravalcons",
        "bech32PrefixConsPub": "terravalconspub"
      },
      "currencies": [
        {
          "coinDenom": "LUNA",
          "coinMinimalDenom": "uluna",
          "coinDecimals": 6,
          "coingeckoId": "terra-luna-2"
        }
      ],
      "feeCurrencies": [
        {
          "coinDenom": "LUNA",
          "coinMinimalDenom": "uluna",
          "coinDecimals": 6,
          "coingeckoId": "terra-luna-2"
        }
      ],
      "coinType": 330,
      "gasPriceStep": {
        "low": 5.665,
        "average": 5.665,
        "high": 7
      },
      "features": [
        "stargate",
        "ibc-transfer",
        "no-legacy-stdTx"
      ],
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-6",
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "juno",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/juno",
      "rest": "https://lcd-juno.itastakers.com",
      "networkName": "Juno",
      "chainId": "juno-1",
      "nativeCurrency": {
        "name": "Juno",
        "symbol": "JUNO",
        "decimals": 6,
        "icon": "https://assets.coingecko.com/coins/images/19249/small/juno.png?1642838082"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://axelarscan.io/logos/chains/juno.svg",
      "blockExplorerUrls": [
        "https://www.mintscan.io/juno"
      ],
      "bip44": {
        "coinType": 118
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "juno",
        "bech32PrefixAccPub": "junopub",
        "bech32PrefixValAddr": "junovaloper",
        "bech32PrefixValPub": "junovaloperpub",
        "bech32PrefixConsAddr": "junovalcons",
        "bech32PrefixConsPub": "junovalconspub"
      },
      "currencies": [
        {
          "coinDenom": "JUNO",
          "coinMinimalDenom": "ujuno",
          "coinDecimals": 6,
          "coingeckoId": "juno-network"
        }
      ],
      "feeCurrencies": [
        {
          "coinDenom": "JUNO",
          "coinMinimalDenom": "ujuno",
          "coinDecimals": 6,
          "coingeckoId": "juno-network"
        }
      ],
      "stakeCurrency": {
        "coinDenom": "JUNO",
        "coinMinimalDenom": "ujuno",
        "coinDecimals": 6,
        "coingeckoId": "juno-network"
      },
      "gasPriceStep": {
        "low": 5000000000,
        "average": 25000000000,
        "high": 40000000000
      },
      "coinType": 118,
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-71",
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "UMEE",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/umee",
      "rest": "https://umee-api.polkachu.com",
      "nativeCurrency": {
        "name": "Umee",
        "symbol": "UMEE",
        "decimals": 6,
        "icon": "https://s2.coinmarketcap.com/static/img/coins/64x64/16389.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/16389.png",
      "blockExplorerUrls": [
        "https://www.mintscan.io/umee/",
        "https://ping.pub/umee",
        "https://umee.explorers.guru",
        "https://atomscan.com/umee"
      ],
      "chainId": "umee-1",
      "networkName": "Umee",
      "bip44": {
        "coinType": 118
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "umee",
        "bech32PrefixAccPub": "umeepub",
        "bech32PrefixValAddr": "umeevaloper",
        "bech32PrefixValPub": "umeevaloperpub",
        "bech32PrefixConsAddr": "umeevalcons",
        "bech32PrefixConsPub": "umeevalconspub"
      },
      "currencies": [
        {
          "coinDenom": "UMEE",
          "coinMinimalDenom": "uumee",
          "coinDecimals": 6,
          "coingeckoId": "pool:uumee"
        }
      ],
      "feeCurrencies": [
        {
          "coinDenom": "UMEE",
          "coinMinimalDenom": "uumee",
          "coinDecimals": 6,
          "coingeckoId": "pool:uumee"
        }
      ],
      "stakeCurrency": {
        "coinDenom": "UMEE",
        "coinMinimalDenom": "uumee",
        "coinDecimals": 6,
        "coingeckoId": "pool:uumee"
      },
      "coinType": 118,
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-33",
      "features": [
        "stargate",
        "ibc-transfer",
        "no-legacy-stdTx",
        "ibc-go"
      ],
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "secret",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/secret",
      "rest": "https://secret-4.api.trivium.network:1317",
      "networkName": "Secret Network",
      "chainId": "secret-4",
      "nativeCurrency": {
        "name": "Secret",
        "symbol": "SCRT",
        "decimals": 6,
        "icon": "https://assets.coingecko.com/coins/images/11871/small/Secret.png?1595520186"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://axelarscan.io/logos/chains/secret.svg",
      "blockExplorerUrls": [
        "https://www.mintscan.io/secret"
      ],
      "bip44": {
        "coinType": 529
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "secret",
        "bech32PrefixAccPub": "secretpub",
        "bech32PrefixValAddr": "secretvaloper",
        "bech32PrefixValPub": "secretvaloperpub",
        "bech32PrefixConsAddr": "secretvalcons",
        "bech32PrefixConsPub": "secretvalconspub"
      },
      "feeCurrencies": [
        {
          "coinDenom": "SCRT",
          "coinMinimalDenom": "uscrt",
          "coinDecimals": 6,
          "coingeckoId": "secret"
        }
      ],
      "stakeCurrency": {
        "coinDenom": "SCRT",
        "coinMinimalDenom": "uscrt",
        "coinDecimals": 6,
        "coingeckoId": "secret"
      },
      "currencies": [
        {
          "coinDenom": "SCRT",
          "coinMinimalDenom": "uscrt",
          "coinDecimals": 6,
          "coingeckoId": "secret"
        }
      ],
      "coinType": 529,
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-20",
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "COMDEX",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/comdex",
      "rest": "https://rest.comdex.one",
      "chainId": "comdex-1",
      "networkName": "Comdex",
      "nativeCurrency": {
        "name": "Comdex",
        "symbol": "CMDX",
        "decimals": 6,
        "icon": "https://raw.githubusercontent.com/cosmos/chain-registry/master/comdex/images/cmdx.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://axelarscan.io/logos/chains/comdex.svg",
      "blockExplorerUrls": [
        "https://ezstaking.tools/comdex",
        "https://www.mintscan.io/comdex",
        "https://comdex.aneka.io/",
        "https://ping.pub/comdex",
        "https://atomscan.com/comdex"
      ],
      "stakeCurrency": {
        "coinDenom": "CMDX",
        "coinMinimalDenom": "ucmdx",
        "coinDecimals": 6,
        "coingeckoId": "cmdx"
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "comdex",
        "bech32PrefixAccPub": "comdexpub",
        "bech32PrefixValAddr": "comdexvaloper",
        "bech32PrefixValPub": "comdexvaloperpub",
        "bech32PrefixConsAddr": "comdexvalcons",
        "bech32PrefixConsPub": "comdexvalconspub"
      },
      "bip44": {
        "coinType": 118
      },
      "currencies": [
        {
          "coinDenom": "CMDX",
          "coinMinimalDenom": "ucmdx",
          "coinDecimals": 6,
          "coingeckoId": "comdex"
        }
      ],
      "feeCurrencies": [
        {
          "coinDenom": "CMDX",
          "coinMinimalDenom": "ucmdx",
          "coinDecimals": 6,
          "coingeckoId": "comdex"
        }
      ],
      "gasPriceStep": {
        "low": 0.01,
        "average": 0.03,
        "high": 0.05
      },
      "coinType": 118,
      "features": [
        "stargate",
        "no-legacy-stdTx",
        "ibc-transfer"
      ],
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-34",
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "EVMOS",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/evmos",
      "rest": "https://rest.bd.evmos.org:1317",
      "nativeCurrency": {
        "name": "Evmos",
        "symbol": "EVMOS",
        "decimals": 6,
        "icon": "https://raw.githubusercontent.com/cosmos/chain-registry/master/evmos/images/evmos.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://axelarscan.io/logos/chains/evmos.svg",
      "networkName": "Evmos",
      "chainId": "evmos_9001-2",
      "blockExplorerUrls": [
        "https://ezstaking.tools/evmos",
        "https://www.mintscan.io/evmos",
        "https://evmos.bigdipper.live",
        "https://evm.evmos.org",
        "https://ping.pub/evmos",
        "https://evmos.explorers.guru",
        "https://atomscan.com/evmos",
        "https://evmos.tcnetwork.io"
      ],
      "stakeCurrency": {
        "coinDenom": "EVMOS",
        "coinMinimalDenom": "aevmos",
        "coinDecimals": 18,
        "coingeckoId": "evmos"
      },
      "walletUrl": "https://wallet.keplr.app/chains/evmos",
      "walletUrlForStaking": "https://wallet.keplr.app/chains/evmos",
      "bip44": {
        "coinType": 60
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "evmos",
        "bech32PrefixAccPub": "evmospub",
        "bech32PrefixValAddr": "evmosvaloper",
        "bech32PrefixValPub": "evmosvaloperpub",
        "bech32PrefixConsAddr": "evmosvalcons",
        "bech32PrefixConsPub": "evmosvalconspub"
      },
      "currencies": [
        {
          "coinDenom": "EVMOS",
          "coinMinimalDenom": "aevmos",
          "coinDecimals": 18,
          "coingeckoId": "evmos"
        }
      ],
      "feeCurrencies": [
        {
          "coinDenom": "EVMOS",
          "coinMinimalDenom": "aevmos",
          "coinDecimals": 18,
          "coingeckoId": "evmos"
        }
      ],
      "gasPriceStep": {
        "low": 25000000000,
        "average": 25000000000,
        "high": 40000000000
      },
      "coinType": 60,
      "features": [
        "ibc-transfer",
        "ibc-go",
        "eth-address-gen",
        "eth-key-sign"
      ],
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-21",
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "REGEN",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/regen",
      "rest": "https://rest-regen.ecostake.com",
      "nativeCurrency": {
        "name": "Regen Network",
        "symbol": "REGEN",
        "decimals": 6,
        "icon": "https://raw.githubusercontent.com/cosmos/chain-registry/master/regen/images/regen.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://axelarscan.io/logos/chains/regen.svg",
      "blockExplorerUrls": [
        "https://www.mintscan.io/regen",
        "https://ping.pub/regen",
        "https://regen.bigdipper.live/",
        "https://atomscan.com/regen-network"
      ],
      "chainId": "regen-1",
      "networkName": "Regen",
      "stakeCurrency": {
        "coinDenom": "REGEN",
        "coinMinimalDenom": "uregen",
        "coinDecimals": 6,
        "coingeckoId": "regen"
      },
      "walletUrl": "https://wallet.keplr.app/chains/regen",
      "walletUrlForStaking": "https://wallet.keplr.app/chains/regen",
      "bip44": {
        "coinType": 118
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "regen",
        "bech32PrefixAccPub": "regenpub",
        "bech32PrefixValAddr": "regenvaloper",
        "bech32PrefixValPub": "regenvaloperpub",
        "bech32PrefixConsAddr": "regenvalcons",
        "bech32PrefixConsPub": "regenvalconspub"
      },
      "currencies": [
        {
          "coinDenom": "REGEN",
          "coinMinimalDenom": "uregen",
          "coinDecimals": 6,
          "coingeckoId": "regen"
        }
      ],
      "feeCurrencies": [
        {
          "coinDenom": "REGEN",
          "coinMinimalDenom": "uregen",
          "coinDecimals": 6,
          "coingeckoId": "regen"
        }
      ],
      "gasPriceStep": {
        "low": 0.015,
        "average": 0.025,
        "high": 0.04
      },
      "coinType": 118,
      "features": [
        "ibc-go",
        "ibc-transfer"
      ],
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-48",
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "STARGAZE",
      "chainType": "cosmos",
      "rpc": "https://mainnet-rpc-router.axelar-dev.workers.dev/?chain=stargaze",
      "rest": "https://rest.stargaze-apis.com",
      "nativeCurrency": {
        "name": "Stargaze",
        "symbol": "STARS",
        "decimals": 6,
        "icon": "https://raw.githubusercontent.com/cosmos/chain-registry/master/stargaze/images/stars.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://axelarscan.io/logos/chains/stargaze.svg",
      "blockExplorerUrls": [
        "https://ezstaking.tools/stargaze",
        "https://www.mintscan.io/stargaze/",
        "https://ping.pub/stargaze",
        "https://atomscan.com/stargaze"
      ],
      "chainId": "stargaze-1",
      "networkName": "Stargaze",
      "stakeCurrency": {
        "coinDenom": "STARS",
        "coinMinimalDenom": "ustars",
        "coinDecimals": 6,
        "coingeckoId": "stargaze"
      },
      "walletUrl": "https://wallet.keplr.app/chains/stargaze",
      "walletUrlForStaking": "https://wallet.keplr.app/chains/stargaze",
      "bip44": {
        "coinType": 118
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "stars",
        "bech32PrefixAccPub": "starspub",
        "bech32PrefixValAddr": "starsvaloper",
        "bech32PrefixValPub": "starsvaloperpub",
        "bech32PrefixConsAddr": "starsvalcons",
        "bech32PrefixConsPub": "starsvalconspub"
      },
      "currencies": [
        {
          "coinDenom": "STARS",
          "coinMinimalDenom": "ustars",
          "coinDecimals": 6,
          "coingeckoId": "stargaze"
        }
      ],
      "feeCurrencies": [
        {
          "coinDenom": "STARS",
          "coinMinimalDenom": "ustars",
          "coinDecimals": 6,
          "coingeckoId": "stargaze"
        }
      ],
      "coinType": 118,
      "features": [
        "ibc-transfer",
        "ibc-go"
      ],
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-50",
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "carbon",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/carbon",
      "rest": "https://api.carbon.network",
      "nativeCurrency": {
        "name": "swth",
        "symbol": "SWTH",
        "decimals": 6,
        "icon": "https://s2.coinmarketcap.com/static/img/coins/64x64/2620.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/2620.png",
      "blockExplorerUrls": [
        "https://scan.carbon.network"
      ],
      "chainId": "carbon-1",
      "networkName": "Carbon",
      "stakeCurrency": {
        "coinDenom": "SWTH",
        "coinMinimalDenom": "uswth",
        "coinDecimals": 6,
        "coingeckoId": "switcheo"
      },
      "bip44": {
        "coinType": 118
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "swth",
        "bech32PrefixAccPub": "swthpub",
        "bech32PrefixValAddr": "swthvaloper",
        "bech32PrefixValPub": "swthvaloperpub",
        "bech32PrefixConsAddr": "swthvalcons",
        "bech32PrefixConsPub": "swthvalconspub"
      },
      "currencies": [
        {
          "coinDenom": "SWTH",
          "coinMinimalDenom": "uswth",
          "coinDecimals": 6,
          "coingeckoId": "switcheo"
        }
      ],
      "feeCurrencies": [
        {
          "coinDenom": "SWTH",
          "coinMinimalDenom": "uswth",
          "coinDecimals": 6,
          "coingeckoId": "switcheo"
        }
      ],
      "coinType": 118,
      "features": [
        "ibc-transfer",
        "ibc-go"
      ],
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-7",
      "estimatedRouteDuration": 60,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "ASSETMANTLE",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/assetmantle",
      "rest": "https://rest.assetmantle.one",
      "chainId": "mantle-1",
      "networkName": "AssetMantle",
      "nativeCurrency": {
        "name": "AssetMantle",
        "symbol": "MNTL",
        "decimals": 6,
        "icon": "https://s2.coinmarketcap.com/static/img/coins/64x64/19686.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/19686.png",
      "blockExplorerUrls": [
        "https://ezstaking.tools/assetmantle",
        "https://www.mintscan.io/asset-mantle",
        "https://explorer.postcapitalist.io/AssetMantle",
        "https://explorer.assetmantle.one",
        "https://assetmantle.explorers.guru",
        "https://atomscan.com/assetmantle"
      ],
      "stakeCurrency": {
        "coinDenom": "MNTL",
        "coinMinimalDenom": "umntl",
        "coinDecimals": 6,
        "coingeckoId": "assetmantle"
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "mantle",
        "bech32PrefixAccPub": "mantlepub",
        "bech32PrefixValAddr": "mantlevaloper",
        "bech32PrefixValPub": "mantlevaloperpub",
        "bech32PrefixConsAddr": "mantlevalcons",
        "bech32PrefixConsPub": "mantlevalconspub"
      },
      "bip44": {
        "coinType": 118
      },
      "currencies": [
        {
          "coinDenom": "MNTL",
          "coinMinimalDenom": "umntl",
          "coinDecimals": 6,
          "coingeckoId": "assetmantle"
        }
      ],
      "feeCurrencies": [
        {
          "coinDenom": "MNTL",
          "coinMinimalDenom": "umntl",
          "coinDecimals": 6,
          "coingeckoId": "assetmantle"
        }
      ],
      "coinType": 118,
      "gasPriceStep": {
        "low": 0.05,
        "average": 0.125,
        "high": 0.2
      },
      "features": [
        "stargate",
        "no-legacy-stdTx",
        "ibc-transfer"
      ],
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-10",
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "Axelarnet",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/axelar",
      "rest": "https://axelar-lcd.quickapi.com",
      "chainId": "axelar-dojo-1",
      "networkName": "Axelar",
      "nativeCurrency": {
        "name": "Axelar",
        "symbol": "AXL",
        "decimals": 6,
        "icon": "https://raw.githubusercontent.com/cosmos/chain-registry/master/axelar/images/axl.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://axelarscan.io/logos/chains/axelar.svg",
      "blockExplorerUrls": [
        "https://axelarscan.io",
        "https://www.mintscan.io/axelar",
        "https://axelar.explorers.guru",
        "https://atomscan.com/axelar"
      ],
      "stakeCurrency": {
        "coinDenom": "AXL",
        "coinMinimalDenom": "uaxl",
        "coinDecimals": 6,
        "coingeckoId": "axelar"
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "axelar",
        "bech32PrefixAccPub": "axelarpub",
        "bech32PrefixValAddr": "axelarvaloper",
        "bech32PrefixValPub": "axelarvaloperpub",
        "bech32PrefixConsAddr": "axelarvalcons",
        "bech32PrefixConsPub": "axelarvalconspub"
      },
      "bip44": {
        "coinType": 118
      },
      "currencies": [
        {
          "coinDenom": "AXL",
          "coinMinimalDenom": "uaxl",
          "coinDecimals": 6,
          "coingeckoId": "axelar"
        }
      ],
      "feeCurrencies": [
        {
          "coinDenom": "AXL",
          "coinMinimalDenom": "uaxl",
          "coinDecimals": 6,
          "coingeckoId": "axelar"
        }
      ],
      "coinType": 118,
      "gasPriceStep": {
        "low": 0.05,
        "average": 0.125,
        "high": 0.2
      },
      "features": [
        "stargate",
        "no-legacy-stdTx",
        "ibc-transfer"
      ],
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-0",
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "cosmoshub",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/cosmoshub",
      "rest": "https://api.cosmos.network",
      "networkName": "Cosmos Hub",
      "chainId": "cosmoshub-4",
      "nativeCurrency": {
        "name": "cosmoshub",
        "symbol": "ATOM",
        "decimals": 6,
        "icon": "https://s2.coinmarketcap.com/static/img/coins/64x64/3794.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://axelarscan.io/logos/chains/cosmoshub.svg",
      "blockExplorerUrls": [
        "https://www.mintscan.io/cosmos"
      ],
      "currencies": [
        {
          "coinDenom": "ATOM",
          "coinMinimalDenom": "uatom",
          "coinDecimals": 6,
          "coingeckoId": "cosmos"
        }
      ],
      "stakeCurrency": {
        "coinDenom": "ATOM",
        "coinMinimalDenom": "uatom",
        "coinDecimals": 6,
        "coingeckoId": "cosmos"
      },
      "bip44": {
        "coinType": 118
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "cosmos",
        "bech32PrefixAccPub": "cosmospub",
        "bech32PrefixValAddr": "cosmosvaloper",
        "bech32PrefixValPub": "cosmosvaloperpub",
        "bech32PrefixConsAddr": "cosmosvalcons",
        "bech32PrefixConsPub": "cosmosvalconspub"
      },
      "feeCurrencies": [
        {
          "coinDenom": "ATOM",
          "coinMinimalDenom": "uatom",
          "coinDecimals": 6,
          "coingeckoId": "cosmos"
        }
      ],
      "coinType": 118,
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30,
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-293"
    },
    {
      "chainName": "injective",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/injective",
      "rest": "https://lcd.injective.network",
      "networkName": "Injective",
      "chainId": "injective-1",
      "nativeCurrency": {
        "name": "Injective",
        "symbol": "INJ",
        "decimals": 6,
        "icon": "https://assets.coingecko.com/coins/images/12882/small/Secondary_Symbol.png?1628233237"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://axelarscan.io/logos/chains/injective.svg",
      "blockExplorerUrls": [
        "https://www.mintscan.io/injective"
      ],
      "stakeCurrency": {
        "coinDenom": "INJ",
        "coinMinimalDenom": "inj",
        "coinDecimals": 18,
        "coingeckoId": "injective-protocol"
      },
      "walletUrl": "https://hub.injective.network/",
      "walletUrlForStaking": "https://hub.injective.network/",
      "bip44": {
        "coinType": 60
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "inj",
        "bech32PrefixAccPub": "injpub",
        "bech32PrefixValAddr": "injvaloper",
        "bech32PrefixValPub": "injvaloperpub",
        "bech32PrefixConsAddr": "injvalcons",
        "bech32PrefixConsPub": "injvalconspub"
      },
      "currencies": [
        {
          "coinDenom": "INJ",
          "coinMinimalDenom": "inj",
          "coinDecimals": 18,
          "coingeckoId": "injective-protocol"
        }
      ],
      "feeCurrencies": [
        {
          "coinDenom": "INJ",
          "coinMinimalDenom": "inj",
          "coinDecimals": 18,
          "coingeckoId": "injective-protocol"
        }
      ],
      "gasPriceStep": {
        "low": 5000000000,
        "average": 25000000000,
        "high": 40000000000
      },
      "coinType": 60,
      "features": [
        "ibc-transfer",
        "ibc-go"
      ],
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-84",
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "Agoric",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/agoric",
      "rest": "https://main.api.agoric.net",
      "networkName": "Agoric",
      "chainId": "agoric-3",
      "nativeCurrency": {
        "name": "Agoric",
        "symbol": "BLD",
        "decimals": 6,
        "icon": "https://raw.githubusercontent.com/cosmos/chain-registry/master/agoric/images/bld.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://axelarscan.io/logos/chains/agoric.svg",
      "blockExplorerUrls": [
        "https://agoric.bigdipper.live/",
        "https://agoric.explorers.guru",
        "https://atomscan.com/agoric/"
      ],
      "bip44": {
        "coinType": 564
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "agoric",
        "bech32PrefixAccPub": "agoricpub",
        "bech32PrefixValAddr": "agoricvaloper",
        "bech32PrefixValPub": "agoricvaloperpub",
        "bech32PrefixConsAddr": "agoricvalcons",
        "bech32PrefixConsPub": "agoricvalconspub"
      },
      "currencies": [
        {
          "coinDenom": "BLD",
          "coinMinimalDenom": "ubld",
          "coinDecimals": 6,
          "coingeckoId": "agoric"
        }
      ],
      "feeCurrencies": [
        {
          "coinDenom": "BLD",
          "coinMinimalDenom": "ubld",
          "coinDecimals": 6,
          "coingeckoId": "agoric",
          "gasPriceStep": {
            "low": 0.03,
            "average": 0.05,
            "high": 0.07
          }
        },
        {
          "coinDenom": "IST",
          "coinMinimalDenom": "uist",
          "coinDecimals": 6,
          "coingeckoId": "inter-stable-token",
          "gasPriceStep": {
            "low": 0.0034,
            "average": 0.007,
            "high": 0.02
          }
        }
      ],
      "stakeCurrency": {
        "coinDenom": "BLD",
        "coinMinimalDenom": "ubld",
        "coinDecimals": 6,
        "coingeckoId": "agoric"
      },
      "coinType": 564,
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-9",
      "features": [
        "stargate",
        "ibc-transfer",
        "no-legacy-stdTx",
        "ibc-go"
      ],
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "fetch",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/fetch",
      "rest": "https://rest-fetchhub.fetch.ai:443",
      "nativeCurrency": {
        "name": "fetch-ai",
        "symbol": "FET",
        "decimals": 6,
        "icon": "https://assets.coingecko.com/coins/images/5681/small/Fetch.jpg?1572098136"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://axelarscan.io/logos/chains/fetch.svg",
      "networkName": "Fetch",
      "blockExplorerUrls": [
        "https://www.mintscan.io/fetchai",
        "https://explore-fetchhub.fetch.ai",
        "https://ping.pub/fetchhub",
        "https://atomscan.com/fetchai"
      ],
      "chainId": "fetchhub-4",
      "stakeCurrency": {
        "coinDenom": "FET",
        "coinMinimalDenom": "afet",
        "coinDecimals": 18,
        "coingeckoId": "fetch-ai"
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "fetch",
        "bech32PrefixAccPub": "fetchpub",
        "bech32PrefixValAddr": "fetchvaloper",
        "bech32PrefixValPub": "fetchvaloperpub",
        "bech32PrefixConsAddr": "fetchvalcons",
        "bech32PrefixConsPub": "fetchvalconspub"
      },
      "bip44": {
        "coinType": 118
      },
      "currencies": [
        {
          "coinDenom": "FET",
          "coinMinimalDenom": "afet",
          "coinDecimals": 18,
          "coingeckoId": "fetch-ai"
        }
      ],
      "feeCurrencies": [
        {
          "coinDenom": "FET",
          "coinMinimalDenom": "afet",
          "coinDecimals": 18,
          "coingeckoId": "fetch-ai"
        }
      ],
      "gasPriceStep": {
        "low": 0.05,
        "average": 0.125,
        "high": 0.2
      },
      "features": [
        "stargate",
        "no-legacy-stdTx",
        "ibc-transfer"
      ],
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-14",
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30
    },
    {
      "chainName": "KI",
      "chainType": "cosmos",
      "rpc": "https://mainnet.rpc.axelar.dev/chain/ki",
      "rest": "https://api-mainnet.blockchain.ki",
      "nativeCurrency": {
        "name": "Ki",
        "symbol": "XKI",
        "decimals": 6,
        "icon": "https://raw.githubusercontent.com/cosmos/chain-registry/master/kichain/images/xki.png"
      },
      "swapAmountForGas": "2000000",
      "chainIconURI": "https://axelarscan.io/logos/chains/kichain.svg",
      "blockExplorerUrls": [
        "https://ezstaking.tools/kichain",
        "https://www.mintscan.io/ki-chain",
        "https://ping.pub/kichain",
        "https://atomscan.com/ki-chain"
      ],
      "chainId": "kichain-2",
      "networkName": "Ki",
      "stakeCurrency": {
        "coinDenom": "XKI",
        "coinMinimalDenom": "uxki",
        "coinDecimals": 6,
        "coingeckoId": "ki"
      },
      "bech32Config": {
        "bech32PrefixAccAddr": "ki",
        "bech32PrefixAccPub": "kipub",
        "bech32PrefixValAddr": "kivaloper",
        "bech32PrefixValPub": "kivaloperpub",
        "bech32PrefixConsAddr": "kivalcons",
        "bech32PrefixConsPub": "kivalconspub"
      },
      "bip44": {
        "coinType": 118
      },
      "currencies": [
        {
          "coinDenom": "XKI",
          "coinMinimalDenom": "uxki",
          "coinDecimals": 6,
          "coingeckoId": "ki"
        }
      ],
      "feeCurrencies": [
        {
          "coinDenom": "XKI",
          "coinMinimalDenom": "uxki",
          "coinDecimals": 6,
          "coingeckoId": "ki"
        }
      ],
      "coinType": 118,
      "gasPriceStep": {
        "low": 0.05,
        "average": 0.125,
        "high": 0.2
      },
      "features": [
        "stargate",
        "no-legacy-stdTx",
        "ibc-transfer"
      ],
      "axelarContracts": {
        "gateway": ""
      },
      "chainToAxelarChannelId": "channel-19",
      "estimatedRouteDuration": 180,
      "estimatedExpressRouteDuration": 30
    }
  ]
}        
        """
}
