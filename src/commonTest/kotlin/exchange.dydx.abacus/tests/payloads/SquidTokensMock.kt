package exchange.dydx.abacus.tests.payloads

internal class SquidTokensMock {
    internal val payload = """
        {
  "tokens": [
    {
      "chainId": 42161,
      "address": "0x912CE59144191C1204E64559FE8253a0e49E6548",
      "name": "Arbitrum",
      "symbol": "ARB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16547/small/arbitrium.png?1624418103",
      "coingeckoId": "arbitrum"
    },
    {
      "chainId": 42161,
      "address": "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE",
      "name": "ETH",
      "symbol": "ETH",
      "decimals": 18,
      "logoURI": "https://tokens.1inch.io/0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee.png",
      "coingeckoId": "ethereum"
    },
    {
      "chainId": 42161,
      "address": "0x82af49447d8a07e3bd95bd0d56f35241523fbab1",
      "name": "Wrapped ETH",
      "symbol": "WETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth"
    },
    {
      "chainId": 42161,
      "name": "USDCoin",
      "address": "0xff970a61a04b1ca14834a43f5de4533ebddb5cc8",
      "symbol": "USDC",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48/logo.png",
      "coingeckoId": "usd-coin"
    },
    {
      "chainId": 42161,
      "address": "0xEB466342C4d449BC9f53A865D5Cb90586f405215",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://assets.coingecko.com/coins/images/6319/thumb/USD_Coin_icon.png?1547042389",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc"
    },
    {
      "address": "0xFd086bC7CD5C481DCC9C85ebE478A1C0b69FCbb9",
      "chainId": 42161,
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "name": "Tether USD",
      "symbol": "USDT",
      "coingeckoId": "tether"
    },
    {
      "address": "0x17FC002b466eEc40DaE837Fc4bE5c67993ddBd6F",
      "chainId": 42161,
      "name": "Frax",
      "symbol": "FRAX",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/13422/thumb/frax_logo.png?1608476506",
      "coingeckoId": "frax"
    },
    {
      "address": "0x2f2a2543B76A4166549F7aaB2e75Bef0aefC5B0f",
      "chainId": 42161,
      "decimals": 8,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/btc.jpg",
      "name": "Wrapped BTC",
      "symbol": "WBTC",
      "coingeckoId": "wrapped-bitcoin"
    },
    {
      "address": "0xd4d42F0b6DEF4CE0383636770eF773390d85c61A",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/sushi.jpg",
      "name": "SushiToken",
      "symbol": "SUSHI",
      "coingeckoId": "sushi"
    },
    {
      "address": "0x6C2C06790b3E3E3c38e12Ee22F8183b37a13EE55",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/arbitrum/assets/0x6C2C06790b3E3E3c38e12Ee22F8183b37a13EE55/logo.png",
      "name": "Dopex Governance Token",
      "symbol": "DPX",
      "coingeckoId": "dopex"
    },
    {
      "address": "0x876Ec6bE52486Eeec06bc06434f3E629D695c6bA",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/arbitrum/assets/0x876Ec6bE52486Eeec06bc06434f3E629D695c6bA/logo.png",
      "name": "FluidFi",
      "symbol": "FLUID",
      "coingeckoId": "fluidfi"
    },
    {
      "address": "0x8D9bA570D6cb60C7e3e0F31343Efe75AB8E65FB1",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/arbitrum/assets/0x8D9bA570D6cb60C7e3e0F31343Efe75AB8E65FB1/logo.png",
      "name": "Governance OHM",
      "symbol": "gOHM",
      "coingeckoId": "governance-ohm"
    },
    {
      "address": "0x07E49d5dE43DDA6162Fa28D24d5935C151875283",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/arbitrum/0x07E49d5dE43DDA6162Fa28D24d5935C151875283.jpg",
      "name": "GOVI",
      "symbol": "GOVI",
      "coingeckoId": "govi"
    },
    {
      "address": "0x662d0f9Ff837A51cF89A1FE7E0882a906dAC08a3",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/arbitrum/0x662d0f9Ff837A51cF89A1FE7E0882a906dAC08a3.jpg",
      "name": "Jones ETH",
      "symbol": "jETH",
      "coingeckoId": ""
    },
    {
      "address": "0x10393c20975cF177a3513071bC110f7962CD67da",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/arbitrum/0x10393c20975cF177a3513071bC110f7962CD67da.jpg",
      "name": "Jones DAO",
      "symbol": "JONES",
      "coingeckoId": "jones-dao"
    },
    {
      "address": "0x93C15cd7DE26f07265f0272E0b831C5D7fAb174f",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/arbitrum/0x93C15cd7DE26f07265f0272E0b831C5D7fAb174f.jpg",
      "name": "Liquid",
      "symbol": "LIQD",
      "coingeckoId": "liquid-finance"
    },
    {
      "address": "0x73700aeCfC4621E112304B6eDC5BA9e36D7743D3",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/arbitrum/0x73700aeCfC4621E112304B6eDC5BA9e36D7743D3.jpg",
      "name": "liquid ETH",
      "symbol": "lqETH",
      "coingeckoId": ""
    },
    {
      "address": "0x539bdE0d7Dbd336b79148AA742883198BBF60342",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/14783.png",
      "name": "MAGIC",
      "symbol": "MAGIC",
      "coingeckoId": "magic",
      "crosschain": false
    },
    {
      "address": "0xFEa7a6a0B346362BF88A9e4A88416B77a57D6c2A",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/arbitrum/0xFEa7a6a0B346362BF88A9e4A88416B77a57D6c2A.jpg",
      "name": "Magic Internet Money",
      "symbol": "MIM",
      "coingeckoId": "magic-internet-money"
    },
    {
      "address": "0x51318B7D00db7ACc4026C88c3952B66278B6A67F",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/arbitrum/0x51318B7D00db7ACc4026C88c3952B66278B6A67F.jpg",
      "name": "Plutus",
      "symbol": "PLS",
      "coingeckoId": "ipulse"
    },
    {
      "address": "0x51fC0f6660482Ea73330E414eFd7808811a57Fa2",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/premia.jpg",
      "name": "Premia",
      "symbol": "PREMIA",
      "coingeckoId": "premia"
    },
    {
      "address": "0x0C4681e6C0235179ec3D4F4fc4DF3d14FDD96017",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/arbitrum/0x0C4681e6C0235179ec3D4F4fc4DF3d14FDD96017.jpg",
      "name": "Radiant",
      "symbol": "RDNT",
      "coingeckoId": "radiant-capital"
    },
    {
      "address": "0x32Eb7902D4134bf98A28b963D26de779AF92A212",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/arbitrum/0x32Eb7902D4134bf98A28b963D26de779AF92A212.jpg",
      "name": "Dopex Rebate Token",
      "symbol": "RDPX",
      "coingeckoId": "dopex-rebate-token"
    },
    {
      "address": "0x3E6648C5a70A150A88bCE65F4aD4d506Fe15d2AF",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/arbitrum/0x3E6648C5a70A150A88bCE65F4aD4d506Fe15d2AF.jpg",
      "name": "Spell Token",
      "symbol": "SPELL",
      "coingeckoId": "spell-token"
    },
    {
      "address": "0x9f20de1fc9b161b34089cbEAE888168B44b03461",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/arbitrum/assets/0x9f20de1fc9b161b34089cbEAE888168B44b03461/logo.png",
      "name": "Arbis",
      "symbol": "ARBIS",
      "coingeckoId": "arbis-finance"
    },
    {
      "address": "0x86A1012d437BBFf84fbDF62569D12d4FD3396F8c",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/arbitrum/assets/0x86A1012d437BBFf84fbDF62569D12d4FD3396F8c/logo.png",
      "name": "Arbys",
      "symbol": "ARBYS",
      "coingeckoId": "arbys"
    },
    {
      "address": "0x99C409E5f62E4bd2AC142f17caFb6810B8F0BAAE",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/arbitrum/assets/0x99C409E5f62E4bd2AC142f17caFb6810B8F0BAAE/logo.png",
      "name": "Beefy Finance",
      "symbol": "BIFI",
      "coingeckoId": "beefy-finance"
    },
    {
      "address": "0xAFD871f684F21Ab9D7137608C71808f83D75e6fc",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/arbitrum/assets/0xAFD871f684F21Ab9D7137608C71808f83D75e6fc/logo.png",
      "name": "Arbucks",
      "symbol": "BUCK",
      "coingeckoId": "arbucks"
    },
    {
      "address": "0xc136E6B376a9946B156db1ED3A34b08AFdAeD76d",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/arbitrum/assets/0xc136E6B376a9946B156db1ED3A34b08AFdAeD76d/logo.png",
      "name": "CreDA Protocol Token",
      "symbol": "CREDA",
      "coingeckoId": "creda"
    },
    {
      "address": "0xDA10009cBd5D07dd0CeCc66161FC93D7c9000da1",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/arbitrum/assets/0xDA10009cBd5D07dd0CeCc66161FC93D7c9000da1/logo.png",
      "name": "DAI Stablecoin",
      "symbol": "DAI",
      "coingeckoId": "dai"
    },
    {
      "address": "0xAeEBa475eDC438f8Eeb6BFBc3164c1C7716Fb304",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/arbitrum/0xAeEBa475eDC438f8Eeb6BFBc3164c1C7716Fb304.jpg",
      "name": "Party Dice",
      "symbol": "DICE",
      "coingeckoId": "klaydice"
    },
    {
      "address": "0x123389C2f0e9194d9bA98c21E63c375B67614108",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/arbitrum/0x123389C2f0e9194d9bA98c21E63c375B67614108.jpg",
      "name": "EthereumMax",
      "symbol": "EMAX",
      "coingeckoId": "ethereummax"
    },
    {
      "address": "0xB41bd4C99dA73510d9e081C5FADBE7A27Ac1F814",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/arbitrum/assets/0xB41bd4C99dA73510d9e081C5FADBE7A27Ac1F814/logo.png",
      "name": "Ideamarket",
      "symbol": "IMO",
      "coingeckoId": "ideamarket"
    },
    {
      "address": "0xf97f4df75117a78c1A5a0DBb814Af92458539FB4",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/link.jpg",
      "name": "ChainLink Token",
      "symbol": "LINK",
      "coingeckoId": "chainlink"
    },
    {
      "address": "0xeD3fB761414DA74b74F33e5c5a1f78104b188DfC",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/nyan.jpg",
      "name": "ArbiNYAN",
      "symbol": "NYAN",
      "coingeckoId": "arbinyan"
    },
    {
      "address": "0xA72159FC390f0E3C6D415e658264c7c4051E9b87",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/arbitrum/assets/0xA72159FC390f0E3C6D415e658264c7c4051E9b87/logo.png",
      "name": "Tracer",
      "symbol": "TCR",
      "coingeckoId": "tecracoin"
    },
    {
      "address": "0xFa7F8980b0f1E64A2062791cc3b0871572f1F7f0",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/uni.jpg",
      "name": "Uniswap",
      "symbol": "UNI",
      "coingeckoId": "unicorn-token"
    },
    {
      "address": "0x3B475F6f2f41853706afc9Fa6a6b8C5dF1a2724c",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/28943/small/logo_with_bg.png?1675569328",
      "name": "ZyberToken",
      "symbol": "ZYB",
      "coingeckoId": "zyberswap"
    },
    {
      "address": "0xfc5a1a6eb076a2c7ad06ed22c90d7e710e35ad0a",
      "chainId": 42161,
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/18323/small/arbit.png?1631532468",
      "name": "GMX",
      "symbol": "GMX",
      "coingeckoId": "gmx"
    },
    {
      "chainId": 42161,
      "address": "0x5402B5F40310bDED796c7D0F3FF6683f5C0cFfdf",
      "decimals": 18,
      "name": "Staked GLP",
      "symbol": "sGLP",
      "logoURI": "https://assets.coingecko.com/coins/images/18323/small/arbit.png?1631532468",
      "coingeckoId": "GLP"
    },
    {
      "chainId": 43114,
      "address": "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE",
      "name": "Avalanche",
      "symbol": "AVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "avalanche-2",
      "commonKey": "wavax-wei"
    },
    {
      "chainId": 43114,
      "address": "0xB31f66AA3C1e785363F0875A1B74E27b85FD66c7",
      "name": "Wrapped AVAX",
      "symbol": "WAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei"
    },
    {
      "chainId": 43114,
      "address": "0xB97EF9Ef8734C71904D8002F8b6Bc66Dd9c48a6E",
      "decimals": 6,
      "name": "USD Coin",
      "symbol": "USDC",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
      "coingeckoId": "usd-coin-avalanche-bridged-usdc-e"
    },
    {
      "chainId": 43114,
      "address": "0x9702230A8Ea53601f5cD2dc00fDBc13d4dF4A8c7",
      "decimals": 6,
      "name": "Tether USD",
      "symbol": "USDT",
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/usdt.jpg",
      "coingeckoId": "tether"
    },
    {
      "chainId": 43114,
      "address": "0xfaB550568C688d5D8A52C7d794cb93Edc26eC0eC",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc"
    },
    {
      "chainId": 43114,
      "address": "0xc7198437980c041c805A1EDcbA50c1Ce5db95118",
      "decimals": 6,
      "name": "Tether USD",
      "symbol": "USDT.e",
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/usdt.jpg",
      "coingeckoId": "tether"
    },
    {
      "chainId": 43114,
      "address": "0xA7D7079b0FEaD91F3e65f86E8915Cb59c1a4C664",
      "decimals": 6,
      "name": "USD Coin",
      "symbol": "USDC.e",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
      "coingeckoId": "usd-coin-avalanche-bridged-usdc-e"
    },
    {
      "chainId": 43114,
      "address": "0xd586E7F844cEa2F87f50152665BCbc2C279D8d70",
      "name": "DAI.e",
      "symbol": "DAI.e",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4943.png",
      "coingeckoId": "dai"
    },
    {
      "chainId": 43114,
      "address": "0x130966628846BFd36ff31a822705796e8cb8C18D",
      "decimals": 18,
      "name": "Magic Internet Money",
      "symbol": "MIM",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/162.png",
      "coingeckoId": "magic-internet-money"
    },
    {
      "chainId": 43114,
      "address": "0x50b7545627a5162F82A992c33b87aDc75187B218",
      "decimals": 8,
      "name": "Wrapped BTC",
      "symbol": "WBTC.e",
      "logoURI": "https://raw.githubusercontent.com/pangolindex/tokens/main/assets/43114/0x50b7545627a5162F82A992c33b87aDc75187B218/logo_24.png",
      "coingeckoId": "wrapped-bitcoin"
    },
    {
      "chainId": 43114,
      "address": "0x49D5c2BdFfac6CE2BFdB6640F4F80f226bc10bAB",
      "decimals": 18,
      "name": "Wrapped Ether",
      "symbol": "WETH.e",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth"
    },
    {
      "chainId": 43114,
      "address": "0x60781C2586D68229fde47564546784ab3fACA982",
      "decimals": 18,
      "name": "Pangolin",
      "symbol": "PNG",
      "logoURI": "https://raw.githubusercontent.com/pangolindex/tokens/main/assets/43114/0x60781C2586D68229fde47564546784ab3fACA982/logo_24.png",
      "coingeckoId": "pangolin"
    },
    {
      "chainId": 43114,
      "address": "0xd1c3f94DE7e5B45fa4eDBBA472491a9f4B166FC4",
      "decimals": 18,
      "name": "Avalaunch",
      "symbol": "XAVA",
      "logoURI": "https://raw.githubusercontent.com/pangolindex/tokens/main/assets/43114/0xd1c3f94DE7e5B45fa4eDBBA472491a9f4B166FC4/logo_24.png",
      "coingeckoId": "avalaunch"
    },
    {
      "chainId": 43114,
      "address": "0xB1466d4cf0DCfC0bCdDcf3500F473cdACb88b56D",
      "decimals": 18,
      "name": "Weble Ecosystem Token",
      "symbol": "WET",
      "logoURI": "https://raw.githubusercontent.com/pangolindex/tokens/main/assets/43114/0xB1466d4cf0DCfC0bCdDcf3500F473cdACb88b56D/logo_24.png",
      "coingeckoId": "weble-ecosystem-token"
    },
    {
      "chainId": 43114,
      "address": "0x59414b3089ce2AF0010e7523Dea7E2b35d776ec7",
      "decimals": 18,
      "name": "Yak Token",
      "symbol": "YAK",
      "logoURI": "https://raw.githubusercontent.com/pangolindex/tokens/main/assets/43114/0x59414b3089ce2AF0010e7523Dea7E2b35d776ec7/logo_24.png",
      "coingeckoId": "yield-yak"
    },
    {
      "chainId": 43114,
      "address": "0x8729438EB15e2C8B576fCc6AeCdA6A148776C0F5",
      "decimals": 18,
      "name": "BENQI",
      "symbol": "QI",
      "logoURI": "https://raw.githubusercontent.com/pangolindex/tokens/main/assets/43114/0x8729438EB15e2C8B576fCc6AeCdA6A148776C0F5/logo_24.png",
      "coingeckoId": "benqi"
    },
    {
      "chainId": 43114,
      "address": "0x5947BB275c521040051D82396192181b413227A3",
      "decimals": 18,
      "name": "Chainlink Token",
      "symbol": "LINK.e",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1975.png",
      "coingeckoId": "chainlink"
    },
    {
      "chainId": 43114,
      "address": "0x2b2C81e08f1Af8835a78Bb2A90AE924ACE0eA4bE",
      "decimals": 18,
      "name": "Staked AVAX | benqi",
      "symbol": "sAVAX",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/18523.png",
      "coingeckoId": "benqi-liquid-staked-avax"
    },
    {
      "chainId": 43114,
      "address": "0x1B88D7aD51626044Ec62eF9803EA264DA4442F32",
      "decimals": 18,
      "name": "ZooToken",
      "symbol": "ZOO",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/9488.png",
      "coingeckoId": "cryptozoo"
    },
    {
      "chainId": 43114,
      "address": "0x6e84a6216eA6dACC71eE8E6b0a5B7322EEbC0fDd",
      "decimals": 18,
      "name": "JoeToken",
      "symbol": "JOE",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/11396.png",
      "coingeckoId": "joe"
    },
    {
      "chainId": 43114,
      "address": "0xb599c3590F42f8F995ECfa0f85D2980B76862fc1",
      "decimals": 6,
      "name": "TerraClassicUSD",
      "symbol": "UST",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/7129.png",
      "coingeckoId": "terrausd"
    },
    {
      "chainId": 43114,
      "address": "0x111111111111ed1D73f860F57b2798b683f2d325",
      "decimals": 18,
      "name": "YUSD Stablecoin",
      "symbol": "YUSD",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/19577.png",
      "coingeckoId": "yusd-stablecoin"
    },
    {
      "chainId": 43114,
      "address": "0x264c1383EA520f73dd837F915ef3a732e204a493",
      "decimals": 18,
      "name": "Binance",
      "symbol": "BNB",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0x264c1383EA520f73dd837F915ef3a732e204a493/logo.png",
      "coingeckoId": "binancecoin"
    },
    {
      "chainId": 43114,
      "address": "0x63a72806098Bd3D9520cC43356dD78afe5D386D9",
      "decimals": 18,
      "name": "Aave Token",
      "symbol": "AAVE.e",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0x63a72806098Bd3D9520cC43356dD78afe5D386D9/logo.png",
      "coingeckoId": "aave"
    },
    {
      "chainId": 43114,
      "address": "0xfB98B335551a418cD0737375a2ea0ded62Ea213b",
      "decimals": 18,
      "name": "Pendle",
      "symbol": "PENDLE",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0xfB98B335551a418cD0737375a2ea0ded62Ea213b/logo.png",
      "coingeckoId": "pendle"
    },
    {
      "chainId": 43114,
      "address": "0xA32608e873F9DdEF944B24798db69d80Bbb4d1ed",
      "decimals": 18,
      "name": "Crabada Token",
      "symbol": "CRA",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0xA32608e873F9DdEF944B24798db69d80Bbb4d1ed/logo.png",
      "coingeckoId": "crabada"
    },
    {
      "chainId": 43114,
      "address": "0x321E7092a180BB43555132ec53AaA65a5bF84251",
      "decimals": 18,
      "name": "Governance OHM",
      "symbol": "gOHM",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0x321E7092a180BB43555132ec53AaA65a5bF84251/logo.png",
      "coingeckoId": "governance-ohm"
    },
    {
      "chainId": 43114,
      "address": "0xec3492a2508DDf4FDc0cD76F31f340b30d1793e6",
      "decimals": 18,
      "name": "Colony Token",
      "symbol": "CLY",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0xec3492a2508DDf4FDc0cD76F31f340b30d1793e6/logo.png",
      "coingeckoId": "celery"
    },
    {
      "chainId": 43114,
      "address": "0x22d4002028f537599bE9f666d1c4Fa138522f9c8",
      "decimals": 18,
      "name": "Platypus",
      "symbol": "PTP",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0x22d4002028f537599bE9f666d1c4Fa138522f9c8/logo.png",
      "coingeckoId": "platypus-finance"
    },
    {
      "chainId": 43114,
      "address": "0x2147EFFF675e4A4eE1C2f918d181cDBd7a8E208f",
      "decimals": 18,
      "name": "Alpha Venture DAO",
      "symbol": "ALPHA.e",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0x2147EFFF675e4A4eE1C2f918d181cDBd7a8E208f/logo.png",
      "coingeckoId": "alpha-finance"
    },
    {
      "chainId": 43114,
      "address": "0xF891214fdcF9cDaa5fdC42369eE4F27F226AdaD6",
      "decimals": 18,
      "name": "Imperium Empires Token",
      "symbol": "IME",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0xF891214fdcF9cDaa5fdC42369eE4F27F226AdaD6/logo.png",
      "coingeckoId": "imperium-empires"
    },
    {
      "chainId": 43114,
      "address": "0x7761E2338B35bCEB6BdA6ce477EF012bde7aE611",
      "decimals": 18,
      "name": "Chikn Egg",
      "symbol": "EGG",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0x7761E2338B35bCEB6BdA6ce477EF012bde7aE611/logo.png",
      "coingeckoId": "chikn-egg"
    },
    {
      "chainId": 43114,
      "address": "0x62edc0692BD897D2295872a9FFCac5425011c661",
      "decimals": 18,
      "name": "GMX",
      "symbol": "GMX",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/11857.png",
      "coingeckoId": "gmx"
    },
    {
      "chainId": 43114,
      "address": "0x8F47416CaE600bccF9530E9F3aeaA06bdD1Caa79",
      "decimals": 18,
      "name": "THOR v2",
      "symbol": "THOR",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/15789.png",
      "coingeckoId": "thor"
    },
    {
      "chainId": 43114,
      "address": "0xed2b42d3c9c6e97e11755bb37df29b6375ede3eb",
      "decimals": 18,
      "name": "Hon Token",
      "symbol": "HON",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/17969.png",
      "coingeckoId": "heroes-of-nft"
    },
    {
      "chainId": 43114,
      "address": "0xfcc6ce74f4cd7edef0c5429bb99d38a3608043a5",
      "decimals": 18,
      "name": "The Phoenix",
      "symbol": "FIRE",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0xfcc6CE74f4cd7eDEF0C5429bB99d38A3608043a5/logo.png",
      "coingeckoId": "the-phoenix"
    },
    {
      "chainId": 43114,
      "address": "0x83a283641C6B4DF383BCDDf807193284C84c5342",
      "decimals": 18,
      "name": "VaporNodes",
      "symbol": "VPND",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0x83a283641C6B4DF383BCDDf807193284C84c5342/logo.png",
      "coingeckoId": "vapornodes"
    },
    {
      "chainId": 43114,
      "address": "0x5817D4F0b62A59b17f75207DA1848C2cE75e7AF4",
      "decimals": 18,
      "name": "Vector",
      "symbol": "VTX",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0x5817D4F0b62A59b17f75207DA1848C2cE75e7AF4/logo.png",
      "coingeckoId": "vector-finance"
    },
    {
      "chainId": 43114,
      "address": "0xeb8343d5284caec921f035207ca94db6baaacbcd",
      "decimals": 18,
      "name": "Echidna",
      "symbol": "ECD",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0xeb8343d5284caec921f035207ca94db6baaacbcd/logo.png",
      "coingeckoId": "echidna"
    },
    {
      "chainId": 43114,
      "address": "0x6121191018BAf067c6Dc6B18D42329447a164F05",
      "decimals": 18,
      "name": "Pizza",
      "symbol": "PIZZA",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/18216.png",
      "coingeckoId": "pizza-game"
    },
    {
      "chainId": 43114,
      "address": "0x4Bfc90322dD638F81F034517359BD447f8E0235a",
      "decimals": 18,
      "name": "New Order",
      "symbol": "NEWO",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0x4Bfc90322dD638F81F034517359BD447f8E0235a/logo.png",
      "coingeckoId": "new-order"
    },
    {
      "chainId": 43114,
      "address": "0x77777777777d4554c39223C354A05825b2E8Faa3",
      "decimals": 18,
      "name": "Yeti Finance",
      "symbol": "YETI",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/19576.png",
      "coingeckoId": "yeti-finance"
    },
    {
      "chainId": 43114,
      "address": "0xab592d197ACc575D16C3346f4EB70C703F308D1E",
      "decimals": 18,
      "name": "Chikn Feed",
      "symbol": "FEED",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/17088.png",
      "coingeckoId": "chikn-feed"
    },
    {
      "chainId": 43114,
      "address": "0xb279f8DD152B99Ec1D84A489D32c35bC0C7F5674",
      "decimals": 18,
      "name": "STEAK",
      "symbol": "STEAK",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0xb279f8DD152B99Ec1D84A489D32c35bC0C7F5674/logo.png",
      "coingeckoId": "steakhut-finance"
    },
    {
      "chainId": 43114,
      "address": "0x152b9d0FdC40C096757F570A51E494bd4b943E50",
      "decimals": 8,
      "name": "Bitcoin",
      "symbol": "BTC.b",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0x152b9d0FdC40C096757F570A51E494bd4b943E50/logo.png",
      "coingeckoId": "bitcoin-avalanche-bridged-btc-b"
    },
    {
      "chainId": 43114,
      "address": "0xcCf719c44e2C36E919335692E89d22Cf13D6aaEB",
      "decimals": 18,
      "name": "Openblox Token",
      "symbol": "OBX",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0xcCf719c44e2C36E919335692E89d22Cf13D6aaEB/logo.png",
      "coingeckoId": "openblox"
    },
    {
      "chainId": 43114,
      "address": "0x47536F17F4fF30e64A96a7555826b8f9e66ec468",
      "decimals": 18,
      "name": "Curve DAO Token",
      "symbol": "CRV",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0x47536F17F4fF30e64A96a7555826b8f9e66ec468/logo.png",
      "coingeckoId": "curve-dao-token"
    },
    {
      "chainId": 43114,
      "address": "0xC7f4debC8072e23fe9259A5C0398326d8EfB7f5c",
      "decimals": 18,
      "name": "HeroesChained",
      "symbol": "HeC",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0xC7f4debC8072e23fe9259A5C0398326d8EfB7f5c/logo.png",
      "coingeckoId": "heroeschained"
    },
    {
      "chainId": 43114,
      "address": "0x6ca558bd3eaB53DA1B25aB97916dd14bf6CFEe4E",
      "decimals": 18,
      "name": "Ripae AVAX",
      "symbol": "pAVAX",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/20673.png",
      "coingeckoId": "ripae-avax"
    },
    {
      "chainId": 43114,
      "address": "0x6D923f688C7FF287dc3A5943CAeefc994F97b290",
      "decimals": 18,
      "name": "SmarterCoin",
      "symbol": "SMRTr",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/13567.png",
      "coingeckoId": "smart-coin-smrtr"
    },
    {
      "chainId": 43114,
      "address": "0x5a15Bdcf9a3A8e799fa4381E666466a516F2d9C8",
      "decimals": 18,
      "name": "Snail Trail",
      "symbol": "SLIME",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/19958.png",
      "coingeckoId": "snail-trail"
    },
    {
      "chainId": 43114,
      "address": "0xC17c30e98541188614dF99239cABD40280810cA3",
      "decimals": 18,
      "name": "EverRise",
      "symbol": "RISE",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/15257.png",
      "coingeckoId": "everrise"
    },
    {
      "chainId": 43114,
      "address": "0x42006Ab57701251B580bDFc24778C43c9ff589A1",
      "decimals": 18,
      "name": "EVO",
      "symbol": "EVO",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0x42006Ab57701251B580bDFc24778C43c9ff589A1/logo.png",
      "coingeckoId": "evo-finance"
    },
    {
      "chainId": 43114,
      "address": "0x48f88A3fE843ccb0b5003e70B4192c1d7448bEf0",
      "decimals": 18,
      "name": "Colony Avalanche Index",
      "symbol": "CAI",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21726.png",
      "coingeckoId": "colony-avalanche-index"
    },
    {
      "chainId": 43114,
      "address": "0x9A8E0217cD870783c3f2317985C57Bf570969153",
      "decimals": 18,
      "name": "Magic",
      "symbol": "MAGIC",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0x9A8E0217cD870783c3f2317985C57Bf570969153/logo.png",
      "coingeckoId": "cosmic-universe-magic-token"
    },
    {
      "chainId": 43114,
      "address": "0x9C9e5fD8bbc25984B178FdCE6117Defa39d2db39",
      "decimals": 18,
      "name": "BUSD Token",
      "symbol": "BUSD",
      "logoURI": "https://raw.githubusercontent.com/traderjoe-xyz/joe-tokenlists/main/logos/0x9C9e5fD8bbc25984B178FdCE6117Defa39d2db39/logo.png",
      "coingeckoId": "binance-usd"
    },
    {
      "chainId": 43114,
      "address": "0xaE64d55a6f09E4263421737397D1fdFA71896a69",
      "decimals": 18,
      "name": "Staked GLP",
      "symbol": "sGLP",
      "logoURI": "https://assets.coingecko.com/coins/images/18323/small/arbit.png?1631532468",
      "coingeckoId": "GLP"
    },
    {
      "chainId": 56,
      "name": "BNB",
      "symbol": "BNB",
      "address": "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1839.png",
      "coingeckoId": "binancecoin",
      "commonKey": "wbnb-wei"
    },
    {
      "chainId": 56,
      "name": "WBNB",
      "symbol": "WBNB",
      "address": "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1839.png",
      "coingeckoId": "binancecoin",
      "commonKey": "wbnb-wei"
    },
    {
      "chainId": 56,
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "address": "0x4268B8F0B87b6Eae5d897996E6b845ddbD99Adf3",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc"
    },
    {
      "chainId": 56,
      "name": "Binance Pegged BUSD",
      "symbol": "BUSD",
      "address": "0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56.png",
      "coingeckoId": "binance-usd"
    },
    {
      "chainId": 56,
      "name": "Binance Pegged USDT",
      "symbol": "USDT",
      "address": "0x55d398326f99059fF775485246999027B3197955",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x55d398326f99059fF775485246999027B3197955.png",
      "coingeckoId": "tether"
    },
    {
      "chainId": 56,
      "name": "Binance Pegged USD Coin",
      "symbol": "USDC",
      "address": "0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d.png",
      "coingeckoId": "usd-coin"
    },
    {
      "chainId": 56,
      "name": "Binance Pegged Bitcoin",
      "symbol": "BTCB",
      "address": "0x7130d2A12B9BCbFAe4f2634d864A1Ee1Ce3Ead9c",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x7130d2A12B9BCbFAe4f2634d864A1Ee1Ce3Ead9c.png",
      "coingeckoId": "binance-bitcoin"
    },
    {
      "chainId": 56,
      "name": "Binance Pegged ETH",
      "symbol": "ETH",
      "address": "0x2170Ed0880ac9A755fd29B2688956BD959F933F8",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x2170Ed0880ac9A755fd29B2688956BD959F933F8.png",
      "coingeckoId": "ethereum"
    },
    {
      "name": "Hay Destablecoin",
      "symbol": "HAY",
      "address": "0x0782b6d8c4551B9760e74c0545a9bCD90bdc41E5",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x0782b6d8c4551B9760e74c0545a9bCD90bdc41E5.png",
      "coingeckoId": "helio-protocol-hay"
    },
    {
      "name": "Decentralized USD",
      "symbol": "USDD",
      "address": "0xd17479997F34dd9156Deef8F95A52D81D265be9c",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/25380/small/UUSD.jpg?1651823371",
      "coingeckoId": "usdd"
    },
    {
      "chainId": 56,
      "name": "PancakeSwap Token",
      "symbol": "CAKE",
      "address": "0x0E09FaBB73Bd3Ade0a17ECC321fD13a19e81cE82",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x0E09FaBB73Bd3Ade0a17ECC321fD13a19e81cE82.png",
      "coingeckoId": "pancakeswap-token"
    },
    {
      "chainId": 56,
      "name": "Venus Token",
      "symbol": "XVS",
      "address": "0xcF6BB5389c92Bdda8a3747Ddb454cB7a64626C63",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xcF6BB5389c92Bdda8a3747Ddb454cB7a64626C63.png",
      "coingeckoId": "venus"
    },
    {
      "chainId": 56,
      "name": "Polkadot Token",
      "symbol": "DOT",
      "address": "0x7083609fCE4d1d8Dc0C979AAb8c869Ea2C873402",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x7083609fCE4d1d8Dc0C979AAb8c869Ea2C873402.png",
      "coingeckoId": "binance-peg-polkadot"
    },
    {
      "chainId": 56,
      "name": "Binance Pegged XRP Token",
      "symbol": "XRP",
      "address": "0x1D2F0da169ceB9fC7B3144628dB156f3F6c60dBE",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x1D2F0da169ceB9fC7B3144628dB156f3F6c60dBE.png",
      "coingeckoId": "binance-peg-xrp"
    },
    {
      "chainId": 56,
      "name": "Binance Pegged Litecoin Token",
      "symbol": "LTC",
      "address": "0x4338665CBB7B2485A8855A139b75D5e34AB0DB94",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x4338665CBB7B2485A8855A139b75D5e34AB0DB94.png",
      "coingeckoId": "binance-peg-litecoin"
    },
    {
      "chainId": 56,
      "name": "Binance Pegged Cardano Token",
      "symbol": "ADA",
      "address": "0x3EE2200Efb3400fAbB9AacF31297cBdD1d435D47",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x3EE2200Efb3400fAbB9AacF31297cBdD1d435D47.png",
      "coingeckoId": "binance-peg-cardano"
    },
    {
      "chainId": 56,
      "name": "ChainLink Token",
      "symbol": "LINK",
      "address": "0xF8A0BF9cF54Bb92F17374d9e9A321E6a111a51bD",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xF8A0BF9cF54Bb92F17374d9e9A321E6a111a51bD.png",
      "coingeckoId": "chainlink"
    },
    {
      "chainId": 56,
      "name": "Trust Wallet",
      "symbol": "TWT",
      "address": "0x4B0F1812e5Df2A09796481Ff14017e6005508003",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x4B0F1812e5Df2A09796481Ff14017e6005508003.png",
      "coingeckoId": "trust-wallet-token"
    },
    {
      "chainId": 56,
      "name": "Binance-Pegged Uniswap",
      "symbol": "UNI",
      "address": "0xBf5140A22578168FD562DCcF235E5D43A02ce9B1",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xBf5140A22578168FD562DCcF235E5D43A02ce9B1.png",
      "coingeckoId": "uniswap"
    },
    {
      "chainId": 56,
      "name": "Filecoin",
      "symbol": "FIL",
      "address": "0x0D8Ce2A99Bb6e3B7Db580eD848240e4a0F9aE153",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x0D8Ce2A99Bb6e3B7Db580eD848240e4a0F9aE153.png",
      "coingeckoId": "binance-peg-filecoin"
    },
    {
      "chainId": 56,
      "name": "Injective Protocol",
      "symbol": "INJ",
      "address": "0xa2B726B1145A4773F68593CF171187d8EBe4d495",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xa2B726B1145A4773F68593CF171187d8EBe4d495.png",
      "coingeckoId": "injective-protocol"
    },
    {
      "chainId": 56,
      "name": "Swipe",
      "symbol": "SXP",
      "address": "0x47BEAd2563dCBf3bF2c9407fEa4dC236fAbA485A",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x47BEAd2563dCBf3bF2c9407fEa4dC236fAbA485A.png",
      "coingeckoId": "swipe"
    },
    {
      "chainId": 56,
      "name": "Standard BTC Hashrate Token",
      "symbol": "BTCST",
      "address": "0x78650B139471520656b9E7aA7A5e9276814a38e9",
      "decimals": 17,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x78650B139471520656b9E7aA7A5e9276814a38e9.png",
      "coingeckoId": "btc-standard-hashrate-token"
    },
    {
      "chainId": 56,
      "name": "SafePal Token",
      "symbol": "SFP",
      "address": "0xD41FDb03Ba84762dD66a0af1a6C8540FF1ba5dfb",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xD41FDb03Ba84762dD66a0af1a6C8540FF1ba5dfb.png",
      "coingeckoId": "safepal"
    },
    {
      "chainId": 56,
      "name": "Belt",
      "symbol": "BELT",
      "address": "0xE0e514c71282b6f4e823703a39374Cf58dc3eA4f",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xE0e514c71282b6f4e823703a39374Cf58dc3eA4f.png",
      "coingeckoId": "belt"
    },
    {
      "chainId": 56,
      "name": "TokoCrypto",
      "symbol": "TKO",
      "address": "0x9f589e3eabe42ebC94A44727b3f3531C0c877809",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x9f589e3eabe42ebC94A44727b3f3531C0c877809.png",
      "coingeckoId": "tokocrypto"
    },
    {
      "chainId": 56,
      "name": "My Neigbor Alice",
      "symbol": "ALICE",
      "address": "0xAC51066d7bEC65Dc4589368da368b212745d63E8",
      "decimals": 6,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xAC51066d7bEC65Dc4589368da368b212745d63E8.png",
      "coingeckoId": "my-neighbor-alice"
    },
    {
      "chainId": 56,
      "name": "Dusk",
      "symbol": "DUSK",
      "address": "0xB2BD0749DBE21f623d9BABa856D3B0f0e1BFEc9C",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xB2BD0749DBE21f623d9BABa856D3B0f0e1BFEc9C.png",
      "coingeckoId": "dusk-network"
    },
    {
      "chainId": 56,
      "name": "Horizon Protocol",
      "symbol": "HZN",
      "address": "0xC0eFf7749b125444953ef89682201Fb8c6A917CD",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xC0eFf7749b125444953ef89682201Fb8c6A917CD.png",
      "coingeckoId": "horizon-protocol"
    },
    {
      "chainId": 56,
      "name": "Dogecoin",
      "symbol": "DOGE",
      "address": "0xbA2aE424d960c26247Dd6c32edC70B295c744C43",
      "decimals": 8,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xbA2aE424d960c26247Dd6c32edC70B295c744C43.png",
      "coingeckoId": "binance-peg-dogecoin"
    },
    {
      "chainId": 56,
      "name": "RFOX",
      "symbol": "RFOX",
      "address": "0x0a3A21356793B49154Fd3BbE91CBc2A16c0457f5",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x0a3A21356793B49154Fd3BbE91CBc2A16c0457f5.png",
      "coingeckoId": "redfox-labs-2"
    },
    {
      "chainId": 56,
      "name": "Mobox",
      "symbol": "MBOX",
      "address": "0x3203c9E46cA618C8C1cE5dC67e7e9D75f5da2377",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x3203c9E46cA618C8C1cE5dC67e7e9D75f5da2377.png",
      "coingeckoId": "mobox"
    },
    {
      "chainId": 56,
      "name": "Hotbit",
      "symbol": "HTB",
      "address": "0x4e840AADD28DA189B9906674B4Afcb77C128d9ea",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x4e840AADD28DA189B9906674B4Afcb77C128d9ea.png",
      "coingeckoId": "hotbit-token"
    },
    {
      "chainId": 56,
      "name": "Wootrade",
      "symbol": "WOO",
      "address": "0x4691937a7508860F876c9c0a2a617E7d9E945D4B",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x4691937a7508860F876c9c0a2a617E7d9E945D4B.png",
      "coingeckoId": "woo-network"
    },
    {
      "chainId": 56,
      "name": "BSCPad",
      "symbol": "BSCPAD",
      "address": "0x5A3010d4d8D3B5fB49f8B6E57FB9E48063f16700",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x5A3010d4d8D3B5fB49f8B6E57FB9E48063f16700.png",
      "coingeckoId": "bscpad"
    },
    {
      "chainId": 56,
      "name": "Axie Infinity Shard",
      "symbol": "AXS",
      "address": "0x715D400F88C167884bbCc41C5FeA407ed4D2f8A0",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x715D400F88C167884bbCc41C5FeA407ed4D2f8A0.png",
      "coingeckoId": "axie-infinity"
    },
    {
      "chainId": 56,
      "name": "Coin98",
      "symbol": "C98",
      "address": "0xaEC945e04baF28b135Fa7c640f624f8D90F1C3a6",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xaEC945e04baF28b135Fa7c640f624f8D90F1C3a6.png",
      "coingeckoId": "coin98"
    },
    {
      "chainId": 56,
      "name": "Splintershards",
      "symbol": "SPS",
      "address": "0x1633b7157e7638C4d6593436111Bf125Ee74703F",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x1633b7157e7638C4d6593436111Bf125Ee74703F.png",
      "coingeckoId": "splinterlands"
    },
    {
      "chainId": 56,
      "name": "Binamon",
      "symbol": "BMON",
      "address": "0x08ba0619b1e7A582E0BCe5BBE9843322C954C340",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x08ba0619b1e7A582E0BCe5BBE9843322C954C340.png",
      "coingeckoId": "binamon"
    },
    {
      "chainId": 56,
      "name": "BabyCake",
      "symbol": "BABYCAKE",
      "address": "0xdB8D30b74bf098aF214e862C90E647bbB1fcC58c",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xdB8D30b74bf098aF214e862C90E647bbB1fcC58c.png",
      "coingeckoId": "baby-cake"
    },
    {
      "chainId": 56,
      "name": "MetaHero",
      "symbol": "HERO",
      "address": "0xD40bEDb44C081D2935eebA6eF5a3c8A31A1bBE13",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xD40bEDb44C081D2935eebA6eF5a3c8A31A1bBE13.png",
      "coingeckoId": "metahero"
    },
    {
      "chainId": 56,
      "name": "Seedify",
      "symbol": "SFUND",
      "address": "0x477bC8d23c634C154061869478bce96BE6045D12",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x477bC8d23c634C154061869478bce96BE6045D12.png",
      "coingeckoId": "seedify-fund"
    },
    {
      "chainId": 56,
      "name": "Telos",
      "symbol": "TLOS",
      "address": "0xb6C53431608E626AC81a9776ac3e999c5556717c",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xb6C53431608E626AC81a9776ac3e999c5556717c.png",
      "coingeckoId": "telos"
    },
    {
      "chainId": 56,
      "name": "Lightning",
      "symbol": "LIGHT",
      "address": "0x037838b556d9c9d654148a284682C55bB5f56eF4",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x037838b556d9c9d654148a284682C55bB5f56eF4.png",
      "coingeckoId": "lightning-protocol"
    },
    {
      "chainId": 56,
      "name": "Beta Finance",
      "symbol": "BETA",
      "address": "0xBe1a001FE942f96Eea22bA08783140B9Dcc09D28",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xBe1a001FE942f96Eea22bA08783140B9Dcc09D28.png",
      "coingeckoId": "beta-finance"
    },
    {
      "chainId": 56,
      "name": "Mines of Dalarnia",
      "symbol": "DAR",
      "address": "0x23CE9e926048273eF83be0A3A8Ba9Cb6D45cd978",
      "decimals": 6,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x23CE9e926048273eF83be0A3A8Ba9Cb6D45cd978.png",
      "coingeckoId": "mines-of-dalarnia"
    },
    {
      "chainId": 56,
      "name": "CryptoMines Eternal",
      "symbol": "ETERNAL",
      "address": "0xD44FD09d74cd13838F137B590497595d6b3FEeA4",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xD44FD09d74cd13838F137B590497595d6b3FEeA4.png",
      "coingeckoId": "cryptomines-eternal"
    },
    {
      "chainId": 56,
      "name": "Sheesha Finance",
      "symbol": "SHEESHA",
      "address": "0x232FB065D9d24c34708eeDbF03724f2e95ABE768",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x232FB065D9d24c34708eeDbF03724f2e95ABE768.png",
      "coingeckoId": "sheesha-finance"
    },
    {
      "chainId": 56,
      "name": "FC Santos Fan Token",
      "symbol": "SANTOS",
      "address": "0xA64455a4553C9034236734FadDAddbb64aCE4Cc7",
      "decimals": 8,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xA64455a4553C9034236734FadDAddbb64aCE4Cc7.png",
      "coingeckoId": "santos-fc-fan-token"
    },
    {
      "chainId": 56,
      "name": "Thetan Gem",
      "symbol": "THG",
      "address": "0x9fD87aEfe02441B123c3c32466cD9dB4c578618f",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x9fD87aEfe02441B123c3c32466cD9dB4c578618f.png",
      "coingeckoId": "thetan-arena"
    },
    {
      "chainId": 56,
      "name": "Singularity Dao",
      "symbol": "SDAO",
      "address": "0x90Ed8F1dc86388f14b64ba8fb4bbd23099f18240",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x90Ed8F1dc86388f14b64ba8fb4bbd23099f18240.png",
      "coingeckoId": "singularitydao"
    },
    {
      "chainId": 56,
      "name": "BitBook",
      "symbol": "BBT",
      "address": "0xD48474E7444727bF500a32D5AbE01943f3A59A64",
      "decimals": 8,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xD48474E7444727bF500a32D5AbE01943f3A59A64.png",
      "coingeckoId": "bitbook-token"
    },
    {
      "chainId": 56,
      "name": "Woonkly Power",
      "symbol": "WOOP",
      "address": "0x8b303d5BbfBbf46F1a4d9741E491e06986894e18",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x8b303d5BbfBbf46F1a4d9741E491e06986894e18.png",
      "coingeckoId": "woonkly-power"
    },
    {
      "chainId": 56,
      "name": "Radio Caca V2",
      "symbol": "RACA",
      "address": "0x12BB890508c125661E03b09EC06E404bc9289040",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x12BB890508c125661E03b09EC06E404bc9289040.png",
      "coingeckoId": "radio-caca"
    },
    {
      "chainId": 56,
      "name": "Era Token",
      "symbol": "ERA",
      "address": "0x6f9F0c4ad9Af7EbD61Ac5A1D4e0F2227F7B0E5f9",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x6f9F0c4ad9Af7EbD61Ac5A1D4e0F2227F7B0E5f9.png",
      "coingeckoId": "era7-game-of-truth"
    },
    {
      "chainId": 56,
      "name": "PearDAO",
      "symbol": "PEX",
      "address": "0x6a0b66710567b6beb81A71F7e9466450a91a384b",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x6a0b66710567b6beb81A71F7e9466450a91a384b.png",
      "coingeckoId": "peardao"
    },
    {
      "chainId": 56,
      "name": "Tiny Coin",
      "symbol": "TINC",
      "address": "0x05aD6E30A855BE07AfA57e08a4f30d00810a402e",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x05aD6E30A855BE07AfA57e08a4f30d00810a402e.png",
      "coingeckoId": "tiny-coin"
    },
    {
      "chainId": 56,
      "name": "CEEK",
      "symbol": "CEEK",
      "address": "0xe0F94Ac5462997D2BC57287Ac3a3aE4C31345D66",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xe0F94Ac5462997D2BC57287Ac3a3aE4C31345D66.png",
      "coingeckoId": "ceek"
    },
    {
      "chainId": 56,
      "name": "Galxe",
      "symbol": "GAL",
      "address": "0xe4Cc45Bb5DBDA06dB6183E8bf016569f40497Aa5",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xe4Cc45Bb5DBDA06dB6183E8bf016569f40497Aa5.png",
      "coingeckoId": "project-galaxy"
    },
    {
      "chainId": 56,
      "name": "Metis Token",
      "symbol": "Metis",
      "address": "0xe552Fb52a4F19e44ef5A967632DBc320B0820639",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xe552Fb52a4F19e44ef5A967632DBc320B0820639.png",
      "coingeckoId": "metis-token"
    },
    {
      "chainId": 56,
      "name": "Staked BNB",
      "symbol": "stkBNB",
      "address": "0xc2E9d07F66A89c44062459A47a0D2Dc038E4fb16",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0xc2E9d07F66A89c44062459A47a0D2Dc038E4fb16.png",
      "coingeckoId": "pstake-staked-bnb"
    },
    {
      "chainId": 56,
      "name": "Spintop",
      "symbol": "SPIN",
      "address": "0x6AA217312960A21aDbde1478DC8cBCf828110A67",
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x6AA217312960A21aDbde1478DC8cBCf828110A67.png",
      "coingeckoId": "spintop"
    },
    {
      "name": "Nafter",
      "symbol": "NAFT",
      "address": "0xD7730681B1DC8f6F969166B29D8A5EA8568616a3",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://assets-cdn.trustwallet.com/blockchains/smartchain/assets/0xD7730681B1DC8f6F969166B29D8A5EA8568616a3/logo.png",
      "coingeckoId": "nafter"
    },
    {
      "name": "PAID Network",
      "symbol": "PAID",
      "address": "0xAD86d0E9764ba90DDD68747D64BFfBd79879a238",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/8329.png",
      "coingeckoId": "paid-network"
    },
    {
      "name": "Alien Worlds Trilium",
      "symbol": "TLM",
      "address": "0x2222227E22102Fe3322098e4CBfE18cFebD57c95",
      "chainId": 56,
      "decimals": 4,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x2222227E22102Fe3322098e4CBfE18cFebD57c95.png",
      "coingeckoId": "alien-worlds"
    },
    {
      "name": "Shirtum",
      "symbol": "SHI",
      "address": "0x7269d98Af4aA705e0B1A5D8512FadB4d45817d5a",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://assets-cdn.trustwallet.com/blockchains/smartchain/assets/0x7269d98Af4aA705e0B1A5D8512FadB4d45817d5a/logo.png",
      "coingeckoId": "shirtum"
    },
    {
      "name": "NFTB",
      "symbol": "NFTB",
      "address": "0xde3dbBE30cfa9F437b293294d1fD64B26045C71A",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://assets-cdn.trustwallet.com/blockchains/smartchain/assets/0xde3dbBE30cfa9F437b293294d1fD64B26045C71A/logo.png",
      "coingeckoId": "nftb"
    },
    {
      "name": "Baby Doge Coin",
      "symbol": "BABYDOGE",
      "address": "0xc748673057861a797275CD8A068AbB95A902e8de",
      "chainId": 56,
      "decimals": 9,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/10407.png",
      "coingeckoId": "baby-doge-coin"
    },
    {
      "name": "FLOKI",
      "symbol": "FLOKI",
      "address": "0x2B3F34e9D4b127797CE6244Ea341a83733ddd6E4",
      "chainId": 56,
      "decimals": 9,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/13074.png",
      "coingeckoId": "baby-moon-floki"
    },
    {
      "name": "Harmony ONE",
      "symbol": "ONE",
      "address": "0x03fF0ff224f904be3118461335064bB48Df47938",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://tokens.pancakeswap.finance/images/0x03fF0ff224f904be3118461335064bB48Df47938.png",
      "coingeckoId": "harmony"
    },
    {
      "name": "Mist",
      "symbol": "MIST",
      "address": "0x68E374F856bF25468D365E539b700b648Bf94B67",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://assets-cdn.trustwallet.com/blockchains/smartchain/assets/0x68E374F856bF25468D365E539b700b648Bf94B67/logo.png",
      "coingeckoId": "mist"
    },
    {
      "name": "My DeFi Pet Token",
      "symbol": "DPET",
      "address": "0xfb62AE373acA027177D1c18Ee0862817f9080d08",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://assets-cdn.trustwallet.com/blockchains/smartchain/assets/0xfb62AE373acA027177D1c18Ee0862817f9080d08/logo.png",
      "coingeckoId": "my-defi-pet"
    },
    {
      "name": "UniCrypt on xDai on BSC",
      "symbol": "UNCX",
      "address": "0x09a6c44c3947B69E2B45F4D51b67E6a39ACfB506",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://assets-cdn.trustwallet.com/blockchains/smartchain/assets/0x09a6c44c3947B69E2B45F4D51b67E6a39ACfB506/logo.png",
      "coingeckoId": "unicrypt-2"
    },
    {
      "name": "MiniFootball",
      "symbol": "MINIFOOTBALL",
      "address": "0xD024Ac1195762F6F13f8CfDF3cdd2c97b33B248b",
      "chainId": 56,
      "decimals": 9,
      "logoURI": "https://assets-cdn.trustwallet.com/blockchains/smartchain/assets/0xD024Ac1195762F6F13f8CfDF3cdd2c97b33B248b/logo.png",
      "coingeckoId": "minifootball"
    },
    {
      "name": "FaraCrystal",
      "symbol": "FARA",
      "address": "0xF4Ed363144981D3A65f42e7D0DC54FF9EEf559A1",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://assets-cdn.trustwallet.com/blockchains/smartchain/assets/0xF4Ed363144981D3A65f42e7D0DC54FF9EEf559A1/logo.png",
      "coingeckoId": "faraland"
    },
    {
      "name": "Altura",
      "symbol": "ALU",
      "address": "0x8263CD1601FE73C066bf49cc09841f35348e3be0",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://assets-cdn.trustwallet.com/blockchains/smartchain/assets/0x8263CD1601FE73C066bf49cc09841f35348e3be0/logo.png",
      "coingeckoId": "altura"
    },
    {
      "name": "KmonCoin",
      "symbol": "KMON",
      "address": "0xc732B6586A93b6B7CF5FeD3470808Bc74998224D",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://assets-cdn.trustwallet.com/blockchains/smartchain/assets/0xc732B6586A93b6B7CF5FeD3470808Bc74998224D/logo.png",
      "coingeckoId": "kryptomon"
    },
    {
      "name": "Hunny Token",
      "symbol": "HUNNY",
      "address": "0x565b72163f17849832A692A3c5928cc502f46D69",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://assets-cdn.trustwallet.com/blockchains/smartchain/assets/0x565b72163f17849832A692A3c5928cc502f46D69/logo.png",
      "coingeckoId": "pancake-hunny"
    },
    {
      "name": "WEYU",
      "symbol": "WEYU",
      "address": "0xFAfD4CB703B25CB22f43D017e7e0d75FEBc26743",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/11553.png",
      "coingeckoId": "weyu"
    },
    {
      "name": "UNCL on xDai on BSC",
      "symbol": "UNCL",
      "address": "0x0E8D5504bF54D9E44260f8d153EcD5412130CaBb",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://assets-cdn.trustwallet.com/blockchains/smartchain/assets/0x0E8D5504bF54D9E44260f8d153EcD5412130CaBb/logo.png",
      "coingeckoId": "uncl"
    },
    {
      "name": "BANANA Token",
      "symbol": "BANANA",
      "address": "0x603c7f932ED1fc6575303D8Fb018fDCBb0f39a95",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/ApeSwapFinance/apeswap-token-lists/main/assets/BANANA.svg",
      "coingeckoId": "apeswap-finance"
    },
    {
      "name": "Binance-Peg Ethereum Token",
      "symbol": "WETH",
      "address": "0x2170ed0880ac9a755fd29b2688956bd959f933f8",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/ApeSwapFinance/apeswap-token-lists/main/assets/ETH.svg",
      "coingeckoId": "weth"
    },
    {
      "name": "Matic Token",
      "symbol": "MATIC",
      "address": "0xcc42724c6683b7e57334c4e856f4c9965ed682bd",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/ApeSwapFinance/apeswap-token-lists/main/assets/MATIC.svg",
      "coingeckoId": "matic-network"
    },
    {
      "name": "CEEK",
      "symbol": "CEEK",
      "address": "0xe0f94ac5462997d2bc57287ac3a3ae4c31345d66",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/ApeSwapFinance/apeswap-token-lists/main/assets/CEEK.png",
      "coingeckoId": "ceek"
    },
    {
      "name": "Coinary Token (Dragonary)",
      "symbol": "CYT",
      "address": "0xd9025e25bb6cf39f8c926a704039d2dd51088063",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/ApeSwapFinance/apeswap-token-lists/main/assets/CYT.svg",
      "coingeckoId": "coinary-token"
    },
    {
      "name": "GAMER",
      "symbol": "GMR",
      "address": "0xadca52302e0a6c2d5d68edcdb4ac75deb5466884",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/ApeSwapFinance/apeswap-token-lists/main/assets/GMR.svg",
      "coingeckoId": "gamer"
    },
    {
      "name": "CryptoMines Reborn",
      "symbol": "CRUX",
      "address": "0xe0191fEfdd0D2B39b1a2E4E029cCDA8A481b7995",
      "chainId": 56,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/ApeSwapFinance/apeswap-token-lists/main/assets/CRUX.svg",
      "coingeckoId": "cryptomines-reborn"
    },
    {
      "chainId": 42220,
      "address": "0x471ece3750da237f93b8e339c536989b8978a438",
      "name": "Celo",
      "symbol": "CELO",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/11090/small/icon-celo-CELO-color-500.png?1592293590",
      "coingeckoId": "celo"
    },
    {
      "chainId": 42220,
      "address": "0xEB466342C4d449BC9f53A865D5Cb90586f405215",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonkey": "uusdc"
    },
    {
      "chainId": 42220,
      "address": "0x765DE816845861e75A25fCA122bb6898B8B1282a",
      "name": "Celo Dollar",
      "symbol": "cUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/7236.png",
      "coingeckoId": "celo-dollar"
    },
    {
      "chainId": 42220,
      "address": "0x37f750B7cC259A2f741AF45294f6a16572CF5cAd",
      "name": "USD Coin | Wormhole",
      "symbol": "USDC.wh",
      "decimals": "6",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
      "coingeckoId": "usd-coin"
    },
    {
      "chainId": 42220,
      "address": "0x617f3112bf5397D0467D315cC709EF968D9ba546",
      "name": "Tether USD | Wormhole",
      "symbol": "USDT.wh",
      "decimals": "6",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/825.png",
      "coingeckoId": "tether"
    },
    {
      "address": "0x66803FB87aBd4aaC3cbB3fAd7C3aa01f6F3FB207",
      "chainId": 42220,
      "name": "WETH.wh | Wormhole",
      "symbol": "WETH",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/2396.png",
      "coingeckoId": "ethereum-wormhole"
    },
    {
      "chainId": 42220,
      "address": "0xD8763CBa276a3738E6DE85b4b3bF5FDed6D6cA73",
      "name": "Celo Euro",
      "symbol": "cEUR",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/9467.png",
      "coingeckoId": "celo-euro"
    },
    {
      "chainId": 42220,
      "address": "0x00be915b9dcf56a3cbe739d9b9c202ca692409ec",
      "name": "Ubeswap",
      "symbol": "UBE",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/10808.png",
      "coingeckoId": "ubeswap"
    },
    {
      "address": "0x918146359264C492BD6934071c6Bd31C854EDBc3",
      "chainId": 42220,
      "name": "Moola cUSD | Moolamarket",
      "symbol": "mcUSD",
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/16983.png",
      "coingeckoId": "moola-celo-dollars"
    },
    {
      "address": "0xE273Ad7ee11dCfAA87383aD5977EE1504aC07568",
      "name": "Moola cEUR | Moolamarket",
      "symbol": "mcEUR",
      "chainId": 42220,
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/16982.png",
      "coingeckoId": "mceur"
    },
    {
      "address": "0x122013fd7dF1C6F636a5bb8f03108E876548b455",
      "chainId": 42220,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/celo/0x122013fd7dF1C6F636a5bb8f03108E876548b455.jpg",
      "name": "Wrapped Ether",
      "symbol": "WETH",
      "coingeckoId": "weth"
    },
    {
      "address": "0x20677d4f3d0f08e735ab512393524a3cfceb250c",
      "chainId": 42220,
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/21762/small/ari.PNG?1639990515",
      "name": "Ari Swap",
      "symbol": "ARI",
      "coingeckoId": "ari-swap"
    },
    {
      "chainId": "agoric-3",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "ibc/295548A78785A1007F232DE286149A6FF512F180AF5657780FC89C009E2C348F"
    },
    {
      "chainId": "mantle-1",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "ibc/616E26A85AD20A3DDEAEBDDE7262E3BA9356C557BC15CACEA86768D7D51FA703"
    },
    {
      "chainId": "axelar-dojo-1",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "uusdc"
    },
    {
      "chainId": "carbon-1",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "ibc/7C0807A56073C4A27B0DE1C21BA3EB75DF75FD763F4AD37BC159917FC01145F0"
    },
    {
      "chainId": "comdex-1",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "ibc/E1616E7C19EA474C565737709A628D6F8A23FF9D3E9A7A6871306CF5E0A5341E"
    },
    {
      "chainId": "cosmoshub-4",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "ibc/932D6003DA334ECBC5B23A071B4287D0A5CC97331197FE9F1C0689BA002A8421"
    },
    {
      "chainId": "crescent-1",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "ibc/BFF0D3805B50D93E2FA5C0B2DDF7E0B30A631076CD80BC12A48C0E95404B4A41"
    },
    {
      "chainId": "evmos_9001-2",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "ibc/63C53CBDF471D4E867366ABE2E631197257118D1B2BEAD1946C8A408F96464C3"
    },
    {
      "chainId": "fetchhub-4",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "ibc/8AF69BC1E1D72B447738B50C28B382F62F2AF65DE303021E45C0B7C851B4B2E1"
    },
    {
      "chainId": "injective-1",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "ibc/7E1AF94AD246BE522892751046F0C959B768642E5671CC3742264068D49553C0"
    },
    {
      "chainId": "juno-1",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "ibc/EAC38D55372F38F1AFD68DF7FE9EF762DCF69F26520643CF3F9D292A738D8034"
    },
    {
      "chainId": "kichain-2",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "ibc/E1E3674A0E4E1EF9C69646F9AF8D9497173821826074622D831BAB73CCB99A2D"
    },
    {
      "chainId": "kaiyo-1",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "ibc/295548A78785A1007F232DE286149A6FF512F180AF5657780FC89C009E2C348F"
    },
    {
      "chainId": "osmosis-1",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "ibc/D189335C6E4A68B513C10AB227BF1C1D38C746766278BA3EEB4FB14124F1D858"
    },
    {
      "chainId": "regen-1",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "ibc/334740505537E9894A64E8561030695016481830D7B36E6A9B6D13C608B55653"
    },
    {
      "chainId": "secret-4",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "ibc/CD7B6B7D85FDF5F72A78E806BCFB8060B561EEF27677B111E0A610626056451E"
    },
    {
      "chainId": "stargaze-1",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://assets.coingecko.com/coins/images/26476/small/axlUSDC.png?1658207579",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "ibc/96274e25174ee93314d8b5636d2d2f70963e207c22f643ec41949a3cbeda4c72"
    },
    {
      "chainId": "phoenix-1",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc"
    },
    {
      "chainId": "umee-1",
      "address": "uusdc",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc",
      "ibcDenom": "ibc/49788C29CD84E08D25CA7BE960BC1F61E88FEFC6333F58557D236D693398466A"
    },
    {
      "chainId": "agoric-3",
      "address": "weth-wei",
      "name": "Axelar WETH",
      "symbol": "axlWETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei",
      "ibcDenom": "ibc/1B38805B1C75352B28169284F96DF56BDEBD9E8FAC005BDCC8CF0378C82AA8E7"
    },
    {
      "chainId": "mantle-1",
      "address": "weth-wei",
      "name": "Axelar WETH",
      "symbol": "axlWETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei",
      "ibcDenom": "ibc/3EFE89848528B4A5665D0102DB818C6B19E04E17455197E92BECC3C41A7F7D78"
    },
    {
      "chainId": "axelar-dojo-1",
      "address": "weth-wei",
      "name": "Axelar WETH",
      "symbol": "axlWETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei",
      "ibcDenom": "weth-wei"
    },
    {
      "chainId": "comdex-1",
      "address": "weth-wei",
      "name": "Axelar WETH",
      "symbol": "axlWETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei",
      "ibcDenom": "ibc/81C3A46287D7664A8FD19843AC8D0CFD6C284EF1F750C661C48B3544277B1B29"
    },
    {
      "chainId": "cosmoshub-4",
      "address": "weth-wei",
      "name": "Axelar WETH",
      "symbol": "axlWETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei",
      "ibcDenom": "ibc/3C168643B15498A2F8BA843649D7CF207EA2F5A7C8AE77BC175EC2FBF21B1BAA"
    },
    {
      "chainId": "crescent-1",
      "address": "weth-wei",
      "name": "Axelar WETH",
      "symbol": "axlWETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei",
      "ibcDenom": "ibc/F1806958CA98757B91C3FA1573ECECD24F6FA3804F074A6977658914A49E65A3"
    },
    {
      "chainId": "evmos_9001-2",
      "address": "weth-wei",
      "name": "Axelar WETH",
      "symbol": "axlWETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei",
      "ibcDenom": "ibc/356EDE917394B2AEF7F915EB24FA683A0CCB8D16DD4ECCEDC2AD0CEC6B66AC81"
    },
    {
      "chainId": "fetchhub-4",
      "address": "weth-wei",
      "name": "Axelar WETH",
      "symbol": "axlWETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei",
      "ibcDenom": "ibc/74712D58FE426053FE962D71BCA5BE80BF83F1BC3508E5E16EBE70241D4E73BE"
    },
    {
      "chainId": "injective-1",
      "address": "weth-wei",
      "name": "Axelar WETH",
      "symbol": "axlWETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei",
      "ibcDenom": "ibc/65A6973F7A4013335AE5FFE623FE019A78A1FEEE9B8982985099978837D764A7"
    },
    {
      "chainId": "juno-1",
      "address": "weth-wei",
      "name": "Axelar WETH",
      "symbol": "axlWETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei",
      "ibcDenom": "ibc/95A45A81521EAFDBEDAEEB6DA975C02E55B414C95AD3CE50709272366A90CA17"
    },
    {
      "chainId": "kichain-2",
      "address": "weth-wei",
      "name": "Axelar WETH",
      "symbol": "axlWETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei",
      "ibcDenom": "ibc/9B68CC79EFF12D25AF712EB805C5062B8F97B2CCE5F3FE55B107EE03095514A3"
    },
    {
      "chainId": "kaiyo-1",
      "address": "weth-wei",
      "name": "Axelar WETH",
      "symbol": "axlWETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei",
      "ibcDenom": "ibc/1B38805B1C75352B28169284F96DF56BDEBD9E8FAC005BDCC8CF0378C82AA8E7"
    },
    {
      "chainId": "osmosis-1",
      "address": "weth-wei",
      "name": "Axelar WETH",
      "symbol": "axlWETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei",
      "ibcDenom": "ibc/EA1D43981D5C9A1C4AAEA9C23BB1D4FA126BA9BC7020A25E0AE4AA841EA25DC5"
    },
    {
      "chainId": "regen-1",
      "address": "weth-wei",
      "name": "Axelar WETH",
      "symbol": "axlWETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei",
      "ibcDenom": "ibc/62B27C470C859CBCB57DC12FCBBD357DD44CAD673362B47503FAA77523ABA028"
    },
    {
      "chainId": "secret-4",
      "address": "weth-wei",
      "name": "Axelar WETH",
      "symbol": "axlWETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei",
      "ibcDenom": "ibc/3665ACBA97B115133C35F060DB67E9671035E9ED48B2FC9140260C122D0C4E03"
    },
    {
      "chainId": "phoenix-1",
      "address": "weth-wei",
      "name": "Axelar WETH",
      "symbol": "axlWETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei",
      "ibcDenom": "ibc/BC8A77AFBD872FDC32A348D3FB10CC09277C266CFE52081DE341C7EC6752E674"
    },
    {
      "chainId": "umee-1",
      "address": "weth-wei",
      "name": "Axelar WETH",
      "symbol": "axlWETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei",
      "ibcDenom": "ibc/04CE51E6E02243E565AE676DD60336E48D455F8AAD0611FA0299A22FDAC448D6"
    },
    {
      "chainId": "agoric-3",
      "address": "dai-wei",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei",
      "ibcDenom": "ibc/3914BDEF46F429A26917E4D8D434620EC4817DC6B6E68FB327E190902F1E9242"
    },
    {
      "chainId": "mantle-1",
      "address": "dai-wei",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei",
      "ibcDenom": "ibc/E4C169A198288D55F756DA32B0EBF0B70C46F634261E288FA34217B7EB8E4947"
    },
    {
      "chainId": "axelar-dojo-1",
      "address": "dai-wei",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei",
      "ibcDenom": "dai-wei"
    },
    {
      "chainId": "comdex-1",
      "address": "dai-wei",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei",
      "ibcDenom": "ibc/54DEF693B7C4BF171E7FFF3ABFE2B54D6A3B8A047A32BAAE9F1417A378594EC6"
    },
    {
      "chainId": "cosmoshub-4",
      "address": "dai-wei",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei",
      "ibcDenom": "ibc/4A98C8AC2C35498162346F28EEBF3206CBEF81F44725FE62A3DB0CC10E88E695"
    },
    {
      "chainId": "crescent-1",
      "address": "dai-wei",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei",
      "ibcDenom": "ibc/2017AFA149C1C42DBF54EC910DA168E9E4F928DF0D3A8E841189994A9339FED9"
    },
    {
      "chainId": "evmos_9001-2",
      "address": "dai-wei",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei",
      "ibcDenom": "ibc/CBA4784581AD4BEF308C536A3CD44D4A940A520E61B0D1E4FB115C539F61DEE5"
    },
    {
      "chainId": "fetchhub-4",
      "address": "dai-wei",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei",
      "ibcDenom": "ibc/32C90ACA7008E4602398C02619D60C5ED36F7F4A245BA6ED870B6640FE418FC2"
    },
    {
      "chainId": "injective-1",
      "address": "dai-wei",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei",
      "ibcDenom": "ibc/265ABC4B9F767AF45CAC6FB76E930548D835EDA3E94BC56B70582A55A73D8C90"
    },
    {
      "chainId": "juno-1",
      "address": "dai-wei",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei",
      "ibcDenom": "ibc/171E8F6687D290D378678310F9F15D367DCD245BF06184532B703A92054A8A4F"
    },
    {
      "chainId": "kichain-2",
      "address": "dai-wei",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei",
      "ibcDenom": "ibc/B5172730CC62ACC6BCB4853D9B6F2C723438A6D7EA9009F44D8096D41FF04166"
    },
    {
      "chainId": "kaiyo-1",
      "address": "dai-wei",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei",
      "ibcDenom": "ibc/3914BDEF46F429A26917E4D8D434620EC4817DC6B6E68FB327E190902F1E9242"
    },
    {
      "chainId": "osmosis-1",
      "address": "dai-wei",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei",
      "ibcDenom": "ibc/0CD3A0285E1341859B5E86B6AB7682F023D03E97607CCC1DC95706411D866DF7"
    },
    {
      "chainId": "regen-1",
      "address": "dai-wei",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei",
      "ibcDenom": "ibc/3C147E71BD9FEC5AAAED09BF022F1C06F52D360580D602F79A5389DA471E7BA3"
    },
    {
      "chainId": "secret-4",
      "address": "dai-wei",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei",
      "ibcDenom": "ibc/8161CB553A9AF7494CA20237CD7A52027409AA1A3B55A37F968CCE99C7C5BDF2"
    },
    {
      "chainId": "phoenix-1",
      "address": "dai-wei",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei",
      "ibcDenom": "ibc/E46EF5449878F6B81219163F211E7329CC0729AA99DA8A589A865F82F754ADE8"
    },
    {
      "chainId": "umee-1",
      "address": "dai-wei",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei",
      "ibcDenom": "ibc/C86651B4D30C1739BF8B061E36F4473A0C9D60380B52D01E56A6874037A5D060"
    },
    {
      "chainId": "agoric-3",
      "address": "uusdt",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt",
      "ibcDenom": "ibc/F2331645B9683116188EF36FC04A809C28BD36B54555E8705A37146D0182F045"
    },
    {
      "chainId": "mantle-1",
      "address": "uusdt",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt",
      "ibcDenom": "ibc/EF1D13E950ADFB6D87A786383D6574529180E34D5EE29459BB578A2A553C21B9"
    },
    {
      "chainId": "axelar-dojo-1",
      "address": "uusdt",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt",
      "ibcDenom": "uusdt"
    },
    {
      "chainId": "comdex-1",
      "address": "uusdt",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt",
      "ibcDenom": "ibc/F0BEB4540AF9E0C6C4DD6FA1635C9AE1F99FCA15366671C9219B1140AAC3FCB1"
    },
    {
      "chainId": "cosmoshub-4",
      "address": "uusdt",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt",
      "ibcDenom": "ibc/5662412372381F56C5F83A0404DC7209E5143ABD32EF67B5705DBE8D9C2BF001"
    },
    {
      "chainId": "crescent-1",
      "address": "uusdt",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt",
      "ibcDenom": "ibc/11FB4C0BC2FCCFF2B01976C0070F468D82DAE8D1F565F80E64063BFDBEE4A5BD"
    },
    {
      "chainId": "evmos_9001-2",
      "address": "uusdt",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt",
      "ibcDenom": "ibc/F11C8CB7743E4B5FDCEA7C97F3B2C115E1931C5614B84C183DAC439B4C919D94"
    },
    {
      "chainId": "fetchhub-4",
      "address": "uusdt",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt",
      "ibcDenom": "ibc/E22116A7B0450692B8B8F9BBA6D987EB46CA48F5EDDEEE683D15C34F6B4E55B6"
    },
    {
      "chainId": "injective-1",
      "address": "uusdt",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt",
      "ibcDenom": "ibc/90C6F06139D663CFD7949223D257C5B5D241E72ED61EBD12FFDDA6F068715E47"
    },
    {
      "chainId": "juno-1",
      "address": "uusdt",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt",
      "ibcDenom": "ibc/B22D08F0E3D08968FB3CBEE2C1E993581A99AAAA60D0490C1AF7DCE567D5FDDA"
    },
    {
      "chainId": "kichain-2",
      "address": "uusdt",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt",
      "ibcDenom": "ibc/386A4031D68DE6370B85F9FF7E89CEF8DE7CDE01CC193CBD87BD3ED60F6662CE"
    },
    {
      "chainId": "kaiyo-1",
      "address": "uusdt",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt",
      "ibcDenom": "ibc/F2331645B9683116188EF36FC04A809C28BD36B54555E8705A37146D0182F045"
    },
    {
      "chainId": "osmosis-1",
      "address": "uusdt",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt",
      "ibcDenom": "ibc/8242AD24008032E457D2E12D46588FD39FB54FB29680C6C7663D296B383C37C4"
    },
    {
      "chainId": "regen-1",
      "address": "uusdt",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt",
      "ibcDenom": "ibc/E2CAC8B785E3E496891ABC7AAB1659F239B5023C1072BA21196AAA443F0F5F23"
    },
    {
      "chainId": "secret-4",
      "address": "uusdt",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt",
      "ibcDenom": "ibc/BFB5BBB93D43AC6458BA9C8871B3F3FE5D4B81301A1BEA2BDA2297C1D6A5D47F"
    },
    {
      "chainId": "phoenix-1",
      "address": "uusdt",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt",
      "ibcDenom": "ibc/CBF67A2BCF6CAE343FDF251E510C8E18C361FC02B23430C121116E0811835DEF"
    },
    {
      "chainId": "umee-1",
      "address": "uusdt",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt",
      "ibcDenom": "ibc/223420B0E8CF9CC47BCAB816AB3A20AE162EED27C1177F4B2BC270C83E11AD8D"
    },
    {
      "chainId": "agoric-3",
      "address": "busd-wei",
      "name": "Axelar BUSD",
      "symbol": "axlBUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4687.png",
      "coingeckoId": "weth",
      "commonKey": "busd-wei",
      "ibcDenom": "ibc/65CD60D7E37EF830BC6B6A6DF4E3E3884A96C0905A7D271C48DC0440B1989EC7"
    },
    {
      "chainId": "mantle-1",
      "address": "busd-wei",
      "name": "Axelar BUSD",
      "symbol": "axlBUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4687.png",
      "coingeckoId": "weth",
      "commonKey": "busd-wei",
      "ibcDenom": "ibc/7ACA93F806B54D0833AAEF0C35A91AF112EA8CA0A34A17B584E6D4F0C22372EE"
    },
    {
      "chainId": "axelar-dojo-1",
      "address": "busd-wei",
      "name": "Axelar BUSD",
      "symbol": "axlBUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4687.png",
      "coingeckoId": "weth",
      "commonKey": "busd-wei",
      "ibcDenom": "busd-wei"
    },
    {
      "chainId": "comdex-1",
      "address": "busd-wei",
      "name": "Axelar BUSD",
      "symbol": "axlBUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4687.png",
      "coingeckoId": "weth",
      "commonKey": "busd-wei",
      "ibcDenom": "ibc/F960753B47A20B18DF578FFDD51FB8B32AF5BF77FF8718580421333F7458E690"
    },
    {
      "chainId": "cosmoshub-4",
      "address": "busd-wei",
      "name": "Axelar BUSD",
      "symbol": "axlBUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4687.png",
      "coingeckoId": "weth",
      "commonKey": "busd-wei",
      "ibcDenom": "ibc/242AFD9038727F23A8CD966BFACAED25609FFA67DDD6BA5F3FB8FBEE1ED66FE3"
    },
    {
      "chainId": "crescent-1",
      "address": "busd-wei",
      "name": "Axelar BUSD",
      "symbol": "axlBUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4687.png",
      "coingeckoId": "weth",
      "commonKey": "busd-wei",
      "ibcDenom": "ibc/A7A2B8871CD2E999EB1D9E901B4F744617C80816CE94DE84CA1200109651C903"
    },
    {
      "chainId": "evmos_9001-2",
      "address": "busd-wei",
      "name": "Axelar BUSD",
      "symbol": "axlBUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4687.png",
      "coingeckoId": "weth",
      "commonKey": "busd-wei",
      "ibcDenom": "ibc/3A7C0D680D0F50A98115966A22C01594897FAE924FAF324E13C5FFB89F6864BF"
    },
    {
      "chainId": "fetchhub-4",
      "address": "busd-wei",
      "name": "Axelar BUSD",
      "symbol": "axlBUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4687.png",
      "coingeckoId": "weth",
      "commonKey": "busd-wei",
      "ibcDenom": "ibc/4DBD7165A95A4CE5DF31575D9745AF85060A68D6E91050CEB6326958EC92CFDF"
    },
    {
      "chainId": "injective-1",
      "address": "busd-wei",
      "name": "Axelar BUSD",
      "symbol": "axlBUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4687.png",
      "coingeckoId": "weth",
      "commonKey": "busd-wei",
      "ibcDenom": "ibc/A62F794AAEC56B6828541224D91DA3E21423AB0DC4D21ECB05E4588A07BD934C"
    },
    {
      "chainId": "juno-1",
      "address": "busd-wei",
      "name": "Axelar BUSD",
      "symbol": "axlBUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4687.png",
      "coingeckoId": "weth",
      "commonKey": "busd-wei",
      "ibcDenom": "ibc/01D29B33757B631D6E02A4AE8A852969273E2476ED83CB3F947D4AA5DB9F151E"
    },
    {
      "chainId": "kichain-2",
      "address": "busd-wei",
      "name": "Axelar BUSD",
      "symbol": "axlBUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4687.png",
      "coingeckoId": "weth",
      "commonKey": "busd-wei",
      "ibcDenom": "ibc/D51641D8F6C96ADE0858FC97F9F002F8436CB3E2EE9B08DAE04E1D942349C777"
    },
    {
      "chainId": "kaiyo-1",
      "address": "busd-wei",
      "name": "Axelar BUSD",
      "symbol": "axlBUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4687.png",
      "coingeckoId": "weth",
      "commonKey": "busd-wei",
      "ibcDenom": "ibc/65CD60D7E37EF830BC6B6A6DF4E3E3884A96C0905A7D271C48DC0440B1989EC7"
    },
    {
      "chainId": "osmosis-1",
      "address": "busd-wei",
      "name": "Axelar BUSD",
      "symbol": "axlBUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4687.png",
      "coingeckoId": "weth",
      "commonKey": "busd-wei",
      "ibcDenom": "ibc/6329DD8CF31A334DD5BE3F68C846C9FE313281362B37686A62343BAC1EB1546D"
    },
    {
      "chainId": "regen-1",
      "address": "busd-wei",
      "name": "Axelar BUSD",
      "symbol": "axlBUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4687.png",
      "coingeckoId": "weth",
      "commonKey": "busd-wei",
      "ibcDenom": "ibc/C0B2F6309C73078A793F8DB25B72028728F9CE1244A60E0D356DDF92861503CB"
    },
    {
      "chainId": "secret-4",
      "address": "busd-wei",
      "name": "Axelar BUSD",
      "symbol": "axlBUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4687.png",
      "coingeckoId": "weth",
      "commonKey": "busd-wei",
      "ibcDenom": "ibc/ABC6463CAA1EA8C15167DBD278C82B1E6193D9D7847DEC458DE87E12A5BE978E"
    },
    {
      "chainId": "phoenix-1",
      "address": "busd-wei",
      "name": "Axelar BUSD",
      "symbol": "axlBUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4687.png",
      "coingeckoId": "weth",
      "commonKey": "busd-wei",
      "ibcDenom": "ibc/FDDF98401F29AC63212C1742F9EC86D3AA1E1BE94BF9EB2F72B990C490303F42"
    },
    {
      "chainId": "umee-1",
      "address": "busd-wei",
      "name": "Axelar BUSD",
      "symbol": "axlBUSD",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4687.png",
      "coingeckoId": "weth",
      "commonKey": "busd-wei",
      "ibcDenom": "ibc/D35E5113823942EE8655934A5476B2C4C1C84C8E83AEF04A89C256FC51437463"
    },
    {
      "chainId": "agoric-3",
      "address": "wavax-wei",
      "name": "Axelar WAVAX",
      "symbol": "axlWAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei",
      "ibcDenom": "ibc/004EBF085BBED1029326D56BE8A2E67C08CECE670A94AC1947DF413EF5130EB2"
    },
    {
      "chainId": "mantle-1",
      "address": "wavax-wei",
      "name": "Axelar WAVAX",
      "symbol": "axlWAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei",
      "ibcDenom": "ibc/6EB0413C3DF3032748A30897930CB98993549B4475E0E61E49CFA661C071BB60"
    },
    {
      "chainId": "axelar-dojo-1",
      "address": "wavax-wei",
      "name": "Axelar WAVAX",
      "symbol": "axlWAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei",
      "ibcDenom": "wavax-wei"
    },
    {
      "chainId": "comdex-1",
      "address": "wavax-wei",
      "name": "Axelar WAVAX",
      "symbol": "axlWAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei",
      "ibcDenom": "ibc/1D5738BD39B3189714B7A5C7CE8A206861ECAE79F7E29C45DA98151571F017E7"
    },
    {
      "chainId": "cosmoshub-4",
      "address": "wavax-wei",
      "name": "Axelar WAVAX",
      "symbol": "axlWAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei",
      "ibcDenom": "ibc/E8F578B93A25BAE12A8BAD4C6973CF6D3BEB9AC019C8C77E566CE1FFB8F010F3"
    },
    {
      "chainId": "crescent-1",
      "address": "wavax-wei",
      "name": "Axelar WAVAX",
      "symbol": "axlWAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei",
      "ibcDenom": "ibc/0886E3462B7DD438353781848DBDF90E58BB7DE90266E3F95E41B3FA8ED1B453"
    },
    {
      "chainId": "evmos_9001-2",
      "address": "wavax-wei",
      "name": "Axelar WAVAX",
      "symbol": "axlWAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei",
      "ibcDenom": "ibc/990770DB97A9567A0B794EB5A3A9BD02C939CE538661FA2DB44DD791CF16DC0E"
    },
    {
      "chainId": "fetchhub-4",
      "address": "wavax-wei",
      "name": "Axelar WAVAX",
      "symbol": "axlWAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei",
      "ibcDenom": "ibc/D3D50F09F6F9A6339A8827A8A89462CAA0C349754B94EABC46D0AEEAF0E41E11"
    },
    {
      "chainId": "injective-1",
      "address": "wavax-wei",
      "name": "Axelar WAVAX",
      "symbol": "axlWAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei",
      "ibcDenom": "ibc/A4FF8E161D2835BA06A7522684E874EFC91004AD0CD14E038F37940562158D73"
    },
    {
      "chainId": "juno-1",
      "address": "wavax-wei",
      "name": "Axelar WAVAX",
      "symbol": "axlWAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei",
      "ibcDenom": "ibc/02B88E41C96FCADA33F15642CEE961EE17A63866EDCA4098EDDB6F9C6671EB92"
    },
    {
      "chainId": "kichain-2",
      "address": "wavax-wei",
      "name": "Axelar WAVAX",
      "symbol": "axlWAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei",
      "ibcDenom": "ibc/496812EE3F92871345EAFC70A2E747D30B13B1D99DB19538076F954DEF4B5B1D"
    },
    {
      "chainId": "kaiyo-1",
      "address": "wavax-wei",
      "name": "Axelar WAVAX",
      "symbol": "axlWAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei",
      "ibcDenom": "ibc/004EBF085BBED1029326D56BE8A2E67C08CECE670A94AC1947DF413EF5130EB2"
    },
    {
      "chainId": "osmosis-1",
      "address": "wavax-wei",
      "name": "Axelar WAVAX",
      "symbol": "axlWAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei",
      "ibcDenom": "ibc/6F62F01D913E3FFE472A38C78235B8F021B511BC6596ADFF02615C8F83D3B373"
    },
    {
      "chainId": "regen-1",
      "address": "wavax-wei",
      "name": "Axelar WAVAX",
      "symbol": "axlWAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei",
      "ibcDenom": "ibc/47E16DE770374BE6ABE72A5264231DCEC92FD2711ACEB29B86574DBCCC228052"
    },
    {
      "chainId": "secret-4",
      "address": "wavax-wei",
      "name": "Axelar WAVAX",
      "symbol": "axlWAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei",
      "ibcDenom": "ibc/045E01C8D691C2E404F6D2CCBB7722A8ED511F0818E180E029143D58E72EA5F7"
    },
    {
      "chainId": "phoenix-1",
      "address": "wavax-wei",
      "name": "Axelar WAVAX",
      "symbol": "axlWAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei",
      "ibcDenom": "ibc/F992067A054C819B42D2DAB57F5CCE347D38352EB90453E59D566BFE64F1614B"
    },
    {
      "chainId": "umee-1",
      "address": "wavax-wei",
      "name": "Axelar WAVAX",
      "symbol": "axlWAVAX",
      "decimals": 18,
      "logoURI": "https://axelarscan.io/logos/chains/avalanche.svg",
      "coingeckoId": "wrapped-avax",
      "commonKey": "wavax-wei",
      "ibcDenom": "ibc/5B771473DCD5BAFE9D3C01AFC4C42872D3B104D9CFA7924A9D02E5DEAB8D20E3"
    },
    {
      "chainId": "agoric-3",
      "address": "wglmr-wei",
      "name": "Axelar WGLMR",
      "symbol": "axlWGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei",
      "ibcDenom": "ibc/C8D63703F5805CE6A2B20555139CF6ED9CDFA870389648EB08D688B94B0AE2C1"
    },
    {
      "chainId": "mantle-1",
      "address": "wglmr-wei",
      "name": "Axelar WGLMR",
      "symbol": "axlWGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei",
      "ibcDenom": "ibc/ECE7689D69D6EEB7354B975B75F5402A840A30C0E01AE9E9493FB1E8A886FA17"
    },
    {
      "chainId": "axelar-dojo-1",
      "address": "wglmr-wei",
      "name": "Axelar WGLMR",
      "symbol": "axlWGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei",
      "ibcDenom": "wglmr-wei"
    },
    {
      "chainId": "comdex-1",
      "address": "wglmr-wei",
      "name": "Axelar WGLMR",
      "symbol": "axlWGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei",
      "ibcDenom": "ibc/14308B897F7966AD643E337853EC613200E9A123D159984DE7B59FE151BCE867"
    },
    {
      "chainId": "cosmoshub-4",
      "address": "wglmr-wei",
      "name": "Axelar WGLMR",
      "symbol": "axlWGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei",
      "ibcDenom": "ibc/6CB279447A96B991FA8986DC4C22C866D215DE1DCDF5F833B81180329FE8001A"
    },
    {
      "chainId": "crescent-1",
      "address": "wglmr-wei",
      "name": "Axelar WGLMR",
      "symbol": "axlWGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei",
      "ibcDenom": "ibc/A7C06A800850847DBCC36213185EC5AAD3C719D42D1F0623F9C1F9EFF456F673"
    },
    {
      "chainId": "evmos_9001-2",
      "address": "wglmr-wei",
      "name": "Axelar WGLMR",
      "symbol": "axlWGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei",
      "ibcDenom": "ibc/E9528EEB1589F209D5EA99BA6BDB1634A65DFD883769D53072DDD26FE7DE8CA3"
    },
    {
      "chainId": "fetchhub-4",
      "address": "wglmr-wei",
      "name": "Axelar WGLMR",
      "symbol": "axlWGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei",
      "ibcDenom": "ibc/BD3F897C555871388A0F8CCA1B4AA0F02280FA9DD2F34E62BBCC7947A89442AD"
    },
    {
      "chainId": "injective-1",
      "address": "wglmr-wei",
      "name": "Axelar WGLMR",
      "symbol": "axlWGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei",
      "ibcDenom": "ibc/8FF72FB47F07B4AFA8649500A168683BEFCB9EE164BD331FA597D26224D51055"
    },
    {
      "chainId": "juno-1",
      "address": "wglmr-wei",
      "name": "Axelar WGLMR",
      "symbol": "axlWGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei",
      "ibcDenom": "ibc/5539E7CB6FF8FDA12AE6BF20E8862513D787BF1712296EB4AA06DD86920FFBC1"
    },
    {
      "chainId": "kichain-2",
      "address": "wglmr-wei",
      "name": "Axelar WGLMR",
      "symbol": "axlWGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei",
      "ibcDenom": "ibc/927DA5BD557C059E3FA6816B2023B24EE4C1B149CDBFBC70A771F8C425DBB91A"
    },
    {
      "chainId": "kaiyo-1",
      "address": "wglmr-wei",
      "name": "Axelar WGLMR",
      "symbol": "axlWGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei",
      "ibcDenom": "ibc/C8D63703F5805CE6A2B20555139CF6ED9CDFA870389648EB08D688B94B0AE2C1"
    },
    {
      "chainId": "osmosis-1",
      "address": "wglmr-wei",
      "name": "Axelar WGLMR",
      "symbol": "axlWGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei",
      "ibcDenom": "ibc/1E26DB0E5122AED464D98462BD384FCCB595732A66B3970AE6CE0B58BAE0FC49"
    },
    {
      "chainId": "regen-1",
      "address": "wglmr-wei",
      "name": "Axelar WGLMR",
      "symbol": "axlWGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei",
      "ibcDenom": "ibc/417455B944F2C3A47811DB1C6AFA740911198939A97A987F0DEF94326D38E4D5"
    },
    {
      "chainId": "secret-4",
      "address": "wglmr-wei",
      "name": "Axelar WGLMR",
      "symbol": "axlWGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei",
      "ibcDenom": "ibc/A79A703A34F0F6F3316FBF80D31F2D1070C0B61F0945DA91D89D0F0923243B60"
    },
    {
      "chainId": "phoenix-1",
      "address": "wglmr-wei",
      "name": "Axelar WGLMR",
      "symbol": "axlWGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei",
      "ibcDenom": "ibc/D54CE4CD2927F744CDCA844DD0E1A5DF88762274C55CD9AAB13E504A29BE8933"
    },
    {
      "chainId": "umee-1",
      "address": "wglmr-wei",
      "name": "Axelar WGLMR",
      "symbol": "axlWGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei",
      "ibcDenom": "ibc/A629BAD41F2473B47BB6D340A1E58D1C02372DAF005DD4B7AC1BD1F44B2593E2"
    },
    {
      "chainId": "agoric-3",
      "address": "wmatic-wei",
      "name": "Axelar WMATIC",
      "symbol": "axlWMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei",
      "ibcDenom": "ibc/A64467480BBE4CCFC3CF7E25AD1446AA9BDBD4F5BCB9EF6038B83D6964C784E6"
    },
    {
      "chainId": "mantle-1",
      "address": "wmatic-wei",
      "name": "Axelar WMATIC",
      "symbol": "axlWMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei",
      "ibcDenom": "ibc/81AD1D148D8567540BE2EAF522A26F93105D453C9C4D4F35DCE11CC3B1B94E50"
    },
    {
      "chainId": "axelar-dojo-1",
      "address": "wmatic-wei",
      "name": "Axelar WMATIC",
      "symbol": "axlWMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei",
      "ibcDenom": "wmatic-wei"
    },
    {
      "chainId": "comdex-1",
      "address": "wmatic-wei",
      "name": "Axelar WMATIC",
      "symbol": "axlWMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei",
      "ibcDenom": "ibc/E8F0355CBC21AFD4C758E93383D28404D19AEB81E8251A63FAA0C250672ADBEF"
    },
    {
      "chainId": "cosmoshub-4",
      "address": "wmatic-wei",
      "name": "Axelar WMATIC",
      "symbol": "axlWMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei",
      "ibcDenom": "ibc/BB5D7FBBA895E6E43EAD8D49E084319663139CA438E41796A0ACB657AE64E8F3"
    },
    {
      "chainId": "crescent-1",
      "address": "wmatic-wei",
      "name": "Axelar WMATIC",
      "symbol": "axlWMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei",
      "ibcDenom": "ibc/C322C7D0867CC3EE6FA3495DC9685E5A0F49B506369341287FDA1E110841A950"
    },
    {
      "chainId": "evmos_9001-2",
      "address": "wmatic-wei",
      "name": "Axelar WMATIC",
      "symbol": "axlWMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei",
      "ibcDenom": "ibc/7883D6C40128A175BF42226F013671C0B190F2AC2CA9215896EBD6F7F7097A77"
    },
    {
      "chainId": "fetchhub-4",
      "address": "wmatic-wei",
      "name": "Axelar WMATIC",
      "symbol": "axlWMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei",
      "ibcDenom": "ibc/F4B35F5F93407AED0909071A36ADDBBFF7757DFBFFDF4AD134539CA415407D30"
    },
    {
      "chainId": "injective-1",
      "address": "wmatic-wei",
      "name": "Axelar WMATIC",
      "symbol": "axlWMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei",
      "ibcDenom": "ibc/7E23647941230DA0AB4ED10F599647D9BE34E1C991D0DA032B5A1522941EBA73"
    },
    {
      "chainId": "juno-1",
      "address": "wmatic-wei",
      "name": "Axelar WMATIC",
      "symbol": "axlWMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei",
      "ibcDenom": "ibc/C3A8C0BA97F3CD808F828E422CCBB39A5206644DF0A65FA79160E4413684EE14"
    },
    {
      "chainId": "kichain-2",
      "address": "wmatic-wei",
      "name": "Axelar WMATIC",
      "symbol": "axlWMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei",
      "ibcDenom": "ibc/45368D217CE1F76A1214FA6F1F31493B5F127793E6AB4873B39A81A8CE21A18E"
    },
    {
      "chainId": "kaiyo-1",
      "address": "wmatic-wei",
      "name": "Axelar WMATIC",
      "symbol": "axlWMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei",
      "ibcDenom": "ibc/A64467480BBE4CCFC3CF7E25AD1446AA9BDBD4F5BCB9EF6038B83D6964C784E6"
    },
    {
      "chainId": "osmosis-1",
      "address": "wmatic-wei",
      "name": "Axelar WMATIC",
      "symbol": "axlWMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei",
      "ibcDenom": "ibc/AB589511ED0DD5FA56171A39978AFBF1371DB986EC1C3526CE138A16377E39BB"
    },
    {
      "chainId": "regen-1",
      "address": "wmatic-wei",
      "name": "Axelar WMATIC",
      "symbol": "axlWMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei",
      "ibcDenom": "ibc/08F89698ED1AEB855854C63901306D16E98186756A842828733252405675AF13"
    },
    {
      "chainId": "secret-4",
      "address": "wmatic-wei",
      "name": "Axelar WMATIC",
      "symbol": "axlWMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei",
      "ibcDenom": "ibc/044FB7DDE7236498107023152F9F235E5DB50D9E999761CB3D4CF8C217F938F6"
    },
    {
      "chainId": "phoenix-1",
      "address": "wmatic-wei",
      "name": "Axelar WMATIC",
      "symbol": "axlWMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei",
      "ibcDenom": "ibc/14E4FD1AB72DE9BF1D6725CBA18373C406CB9A7DA17955299F3F4DC5C6131A4E"
    },
    {
      "chainId": "umee-1",
      "address": "wmatic-wei",
      "name": "Axelar WMATIC",
      "symbol": "axlWMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei",
      "ibcDenom": "ibc/FAEC929814E0D916C019EB4B8BE58360EC3B6AB6A2B3185CB1EA0B54832DEE68"
    },
    {
      "chainId": "agoric-3",
      "address": "wbtc-satoshi",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi",
      "ibcDenom": "ibc/301DAF9CB0A9E247CD478533EF0E21F48FF8118C4A51F77C8BC3EB70E5566DBC"
    },
    {
      "chainId": "mantle-1",
      "address": "wbtc-satoshi",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi",
      "ibcDenom": "ibc/366CBD559121EDAE36D7A1202DD1520143E3EF3493810D5FBB7D075F24E03EF3"
    },
    {
      "chainId": "axelar-dojo-1",
      "address": "wbtc-satoshi",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi",
      "ibcDenom": "wbtc-satoshi"
    },
    {
      "chainId": "comdex-1",
      "address": "wbtc-satoshi",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi",
      "ibcDenom": "ibc/0A6F20FA34BEBB63568E44C81C6E154C63ED061BA45E7EBC144B24C0DBBD0A4F"
    },
    {
      "chainId": "cosmoshub-4",
      "address": "wbtc-satoshi",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi",
      "ibcDenom": "ibc/97F15E1BC69D4AAD938CAAA6CA5C963F6159C93059481246A8A26A113BC6BD2C"
    },
    {
      "chainId": "crescent-1",
      "address": "wbtc-satoshi",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi",
      "ibcDenom": "ibc/7FFC60524C4513A3A8E0A407CC89BFF5A861EC624209D72EB26FC10ADAEBA70E"
    },
    {
      "chainId": "evmos_9001-2",
      "address": "wbtc-satoshi",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi",
      "ibcDenom": "ibc/C834CD421B4FD910BBC97E06E86B5E6F64EA2FE36D6AE0E4304C2E1FB1E7333C"
    },
    {
      "chainId": "fetchhub-4",
      "address": "wbtc-satoshi",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi",
      "ibcDenom": "ibc/036052021926396A2AC57F52171B24C6A8DBF79755A3926DB0E2FE8B57F389C6"
    },
    {
      "chainId": "injective-1",
      "address": "wbtc-satoshi",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi",
      "ibcDenom": "ibc/4C8A332AE4FDE42709649B5F9A2A336192158C4465DF74B4513F5AD0C583EA6F"
    },
    {
      "chainId": "juno-1",
      "address": "wbtc-satoshi",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi",
      "ibcDenom": "ibc/5EF597EA4E863132BFD3E051AC6BAA0175F00913D3256A41F11DC425C39527D6"
    },
    {
      "chainId": "kichain-2",
      "address": "wbtc-satoshi",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi",
      "ibcDenom": "ibc/911B721F15A40ABB29636CBF8AE630076DDF62841ACE7E6D879405CA8870CEA2"
    },
    {
      "chainId": "kaiyo-1",
      "address": "wbtc-satoshi",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi",
      "ibcDenom": "ibc/301DAF9CB0A9E247CD478533EF0E21F48FF8118C4A51F77C8BC3EB70E5566DBC"
    },
    {
      "chainId": "osmosis-1",
      "address": "wbtc-satoshi",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi",
      "ibcDenom": "ibc/D1542AA8762DB13087D8364F3EA6509FD6F009A34F00426AF9E4F9FA85CBBF1F"
    },
    {
      "chainId": "regen-1",
      "address": "wbtc-satoshi",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi",
      "ibcDenom": "ibc/9D7B59A9F02B0D2F45FD1AA4441AB283E91F6B963F5E45883B1287C6FEBA9575"
    },
    {
      "chainId": "secret-4",
      "address": "wbtc-satoshi",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi",
      "ibcDenom": "ibc/1B26C5CEE7509C05EDFB4AED90997C84A4F9E8DCED7E544C8D2C630486686405"
    },
    {
      "chainId": "phoenix-1",
      "address": "wbtc-satoshi",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi",
      "ibcDenom": "ibc/05D299885B07905B6886F554B39346EA6761246076A1120B1950049B92B922DD"
    },
    {
      "chainId": "umee-1",
      "address": "wbtc-satoshi",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi",
      "ibcDenom": "ibc/153B97FE395140EAAA2D7CAC537AF1804AEC5F0595CBC5F1603094018D158C0C"
    },
    {
      "chainId": "agoric-3",
      "address": "wbnb-wei",
      "name": "Axelar WBNB",
      "symbol": "axlWBNB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12591/small/binance-coin-logo.png?1600947313",
      "coingeckoId": "wbnb",
      "commonKey": "wbnb-wei",
      "ibcDenom": "ibc/DADB399E742FCEE71853E98225D13E44E90292852CD0033DF5CABAB96F80B833"
    },
    {
      "chainId": "mantle-1",
      "address": "wbnb-wei",
      "name": "Axelar WBNB",
      "symbol": "axlWBNB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12591/small/binance-coin-logo.png?1600947313",
      "coingeckoId": "wbnb",
      "commonKey": "wbnb-wei",
      "ibcDenom": "ibc/C50DBE8B3FEF01C20C8049754E1066A89EC57BC15122699C2DDAA6D7581F2EAE"
    },
    {
      "chainId": "axelar-dojo-1",
      "address": "wbnb-wei",
      "name": "Axelar WBNB",
      "symbol": "axlWBNB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12591/small/binance-coin-logo.png?1600947313",
      "coingeckoId": "wbnb",
      "commonKey": "wbnb-wei",
      "ibcDenom": "wbnb-wei"
    },
    {
      "chainId": "comdex-1",
      "address": "wbnb-wei",
      "name": "Axelar WBNB",
      "symbol": "axlWBNB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12591/small/binance-coin-logo.png?1600947313",
      "coingeckoId": "wbnb",
      "commonKey": "wbnb-wei",
      "ibcDenom": "ibc/EC7576E3F8D254787264F0972E6518E42CFFB5305EC9D0BC7DD7B7FFEFACB28A"
    },
    {
      "chainId": "cosmoshub-4",
      "address": "wbnb-wei",
      "name": "Axelar WBNB",
      "symbol": "axlWBNB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12591/small/binance-coin-logo.png?1600947313",
      "coingeckoId": "wbnb",
      "commonKey": "wbnb-wei",
      "ibcDenom": "ibc/771FB23883042F959CDCB02F3D0501CC7F32EF4E28835EE4D7DA8CA7E8CF16F6"
    },
    {
      "chainId": "crescent-1",
      "address": "wbnb-wei",
      "name": "Axelar WBNB",
      "symbol": "axlWBNB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12591/small/binance-coin-logo.png?1600947313",
      "coingeckoId": "wbnb",
      "commonKey": "wbnb-wei",
      "ibcDenom": "ibc/3D4499D811B055223D0EFB06D2211F84772CAEF0FB987F71BAE716191714B391"
    },
    {
      "chainId": "evmos_9001-2",
      "address": "wbnb-wei",
      "name": "Axelar WBNB",
      "symbol": "axlWBNB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12591/small/binance-coin-logo.png?1600947313",
      "coingeckoId": "wbnb",
      "commonKey": "wbnb-wei",
      "ibcDenom": "ibc/5BDA280DA1EA865301F0DB343F87971D6E6C399152B335D8CE475EEA2BA38D21"
    },
    {
      "chainId": "fetchhub-4",
      "address": "wbnb-wei",
      "name": "Axelar WBNB",
      "symbol": "axlWBNB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12591/small/binance-coin-logo.png?1600947313",
      "coingeckoId": "wbnb",
      "commonKey": "wbnb-wei",
      "ibcDenom": "ibc/26786027D954FD05D66A965F3081891D513001B5B2487BD01820E0109598E07E"
    },
    {
      "chainId": "injective-1",
      "address": "wbnb-wei",
      "name": "Axelar WBNB",
      "symbol": "axlWBNB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12591/small/binance-coin-logo.png?1600947313",
      "coingeckoId": "wbnb",
      "commonKey": "wbnb-wei",
      "ibcDenom": "ibc/B877B8EF095028B807370AB5C7790CA0C328777C9FF09AA7F5436BA7FAE4A86F"
    },
    {
      "chainId": "juno-1",
      "address": "wbnb-wei",
      "name": "Axelar WBNB",
      "symbol": "axlWBNB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12591/small/binance-coin-logo.png?1600947313",
      "coingeckoId": "wbnb",
      "commonKey": "wbnb-wei",
      "ibcDenom": "ibc/735AFF12D7AF5EEC8F4339448BBF001547AEA05CCA6F1CAA60C139AE87828EB1"
    },
    {
      "chainId": "kichain-2",
      "address": "wbnb-wei",
      "name": "Axelar WBNB",
      "symbol": "axlWBNB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12591/small/binance-coin-logo.png?1600947313",
      "coingeckoId": "wbnb",
      "commonKey": "wbnb-wei",
      "ibcDenom": "ibc/F4B1551A3470D93A725460F109FB57990702B703790D8A21C7DC66AEF3BACBF4"
    },
    {
      "chainId": "kaiyo-1",
      "address": "wbnb-wei",
      "name": "Axelar WBNB",
      "symbol": "axlWBNB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12591/small/binance-coin-logo.png?1600947313",
      "coingeckoId": "wbnb",
      "commonKey": "wbnb-wei",
      "ibcDenom": "ibc/DADB399E742FCEE71853E98225D13E44E90292852CD0033DF5CABAB96F80B833"
    },
    {
      "chainId": "osmosis-1",
      "address": "wbnb-wei",
      "name": "Axelar WBNB",
      "symbol": "axlWBNB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12591/small/binance-coin-logo.png?1600947313",
      "coingeckoId": "wbnb",
      "commonKey": "wbnb-wei",
      "ibcDenom": "ibc/F4A070A6D78496D53127EA85C094A9EC87DFC1F36071B8CCDDBD020F933D213D"
    },
    {
      "chainId": "regen-1",
      "address": "wbnb-wei",
      "name": "Axelar WBNB",
      "symbol": "axlWBNB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12591/small/binance-coin-logo.png?1600947313",
      "coingeckoId": "wbnb",
      "commonKey": "wbnb-wei",
      "ibcDenom": "ibc/E83EB9C4EC33A836E4E9B0F3216A85BF54996A8891F366F2F677EE0E012AADC2"
    },
    {
      "chainId": "secret-4",
      "address": "wbnb-wei",
      "name": "Axelar WBNB",
      "symbol": "axlWBNB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12591/small/binance-coin-logo.png?1600947313",
      "coingeckoId": "wbnb",
      "commonKey": "wbnb-wei",
      "ibcDenom": "ibc/4870D3BE3BD3C44C7069588BEC579928D399D983E9D02F0113A4878DAF135F0A"
    },
    {
      "chainId": "phoenix-1",
      "address": "wbnb-wei",
      "name": "Axelar WBNB",
      "symbol": "axlWBNB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12591/small/binance-coin-logo.png?1600947313",
      "coingeckoId": "wbnb",
      "commonKey": "wbnb-wei",
      "ibcDenom": "ibc/1319C6B38CA613C89D78C2D1461B305038B1085F6855E8CD276FE3F7C9600B4C"
    },
    {
      "chainId": "umee-1",
      "address": "wbnb-wei",
      "name": "Axelar WBNB",
      "symbol": "axlWBNB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12591/small/binance-coin-logo.png?1600947313",
      "coingeckoId": "wbnb",
      "commonKey": "wbnb-wei",
      "ibcDenom": "ibc/8184469200C5E667794375F5B0EC3B9ABB6FF79082941BF5D0F8FF59FEBA862E"
    },
    {
      "chainId": "agoric-3",
      "address": "wftm-wei",
      "name": "Axelar WFTM",
      "symbol": "axlWFTM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16036/small/Fantom.png?1622679930",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei",
      "ibcDenom": "ibc/E67ADA2204A941CD4743E70771BA08E24885E1ADD6FD140CE1F9E0FEBB68C6B2"
    },
    {
      "chainId": "mantle-1",
      "address": "wftm-wei",
      "name": "Axelar WFTM",
      "symbol": "axlWFTM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16036/small/Fantom.png?1622679930",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei",
      "ibcDenom": "ibc/7A6F5C3C7459DAB639CF605D605CF5D291944B72DF233284C5150DB548B2018C"
    },
    {
      "chainId": "axelar-dojo-1",
      "address": "wftm-wei",
      "name": "Axelar WFTM",
      "symbol": "axlWFTM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16036/small/Fantom.png?1622679930",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei",
      "ibcDenom": "wftm-wei"
    },
    {
      "chainId": "comdex-1",
      "address": "wftm-wei",
      "name": "Axelar WFTM",
      "symbol": "axlWFTM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16036/small/Fantom.png?1622679930",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei",
      "ibcDenom": "ibc/78A0828C273648513517BC6C10D9F7F2768472DD5C0F88B27CB54E346CB57D59"
    },
    {
      "chainId": "cosmoshub-4",
      "address": "wftm-wei",
      "name": "Axelar WFTM",
      "symbol": "axlWFTM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16036/small/Fantom.png?1622679930",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei",
      "ibcDenom": "ibc/80A8DBDCDC0AD1CF1781110E8438D894199BA2E4240A65DBB833A665E41620CB"
    },
    {
      "chainId": "crescent-1",
      "address": "wftm-wei",
      "name": "Axelar WFTM",
      "symbol": "axlWFTM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16036/small/Fantom.png?1622679930",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei",
      "ibcDenom": "ibc/23B62EFD1B9444733889B42362570C774801430A1C656A0A3F8D6D69AE93ED8B"
    },
    {
      "chainId": "evmos_9001-2",
      "address": "wftm-wei",
      "name": "Axelar WFTM",
      "symbol": "axlWFTM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16036/small/Fantom.png?1622679930",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei",
      "ibcDenom": "ibc/B389DF077401C819F7A4235167AC1399790FB819983191A3AFC646C7364D24C9"
    },
    {
      "chainId": "fetchhub-4",
      "address": "wftm-wei",
      "name": "Axelar WFTM",
      "symbol": "axlWFTM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16036/small/Fantom.png?1622679930",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei",
      "ibcDenom": "ibc/D504766328F350B25FD8189529ADACB32C365EBEC92D9A719D151BFD0B016E47"
    },
    {
      "chainId": "injective-1",
      "address": "wftm-wei",
      "name": "Axelar WFTM",
      "symbol": "axlWFTM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16036/small/Fantom.png?1622679930",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei",
      "ibcDenom": "ibc/31E8DDA49D53535F358B29CFCBED1B9224DAAFE82788C0477930DCDE231DA878"
    },
    {
      "chainId": "juno-1",
      "address": "wftm-wei",
      "name": "Axelar WFTM",
      "symbol": "axlWFTM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16036/small/Fantom.png?1622679930",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei",
      "ibcDenom": "ibc/BCA8E085B8D4D9D89D5316165E51545B826C5E034EACD6C00A7464C58F318379"
    },
    {
      "chainId": "kichain-2",
      "address": "wftm-wei",
      "name": "Axelar WFTM",
      "symbol": "axlWFTM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16036/small/Fantom.png?1622679930",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei",
      "ibcDenom": "ibc/CC7B0778EABFED87BA0B91C38A9127524DB191BFD6C230FA1862456BE04424A4"
    },
    {
      "chainId": "kaiyo-1",
      "address": "wftm-wei",
      "name": "Axelar WFTM",
      "symbol": "axlWFTM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16036/small/Fantom.png?1622679930",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei",
      "ibcDenom": "ibc/E67ADA2204A941CD4743E70771BA08E24885E1ADD6FD140CE1F9E0FEBB68C6B2"
    },
    {
      "chainId": "osmosis-1",
      "address": "wftm-wei",
      "name": "Axelar WFTM",
      "symbol": "axlWFTM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16036/small/Fantom.png?1622679930",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei",
      "ibcDenom": "ibc/5E2DFDF1734137302129EA1C1BA21A580F96F778D4F021815EA4F6DB378DA1A4"
    },
    {
      "chainId": "regen-1",
      "address": "wftm-wei",
      "name": "Axelar WFTM",
      "symbol": "axlWFTM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16036/small/Fantom.png?1622679930",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei",
      "ibcDenom": "ibc/E8FF33FF39F5AD98A45CBE679B02ADB861D477B418896002243B32DCD042FF26"
    },
    {
      "chainId": "secret-4",
      "address": "wftm-wei",
      "name": "Axelar WFTM",
      "symbol": "axlWFTM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16036/small/Fantom.png?1622679930",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei",
      "ibcDenom": "ibc/6B9DEBE62EBA182F2AD66E1CEAE506B8F3046F86968F938DC797438014622D85"
    },
    {
      "chainId": "phoenix-1",
      "address": "wftm-wei",
      "name": "Axelar WFTM",
      "symbol": "axlWFTM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16036/small/Fantom.png?1622679930",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei",
      "ibcDenom": "ibc/19E687E77D1AE3CADBB3DE487277AFEC0E340A84334D6ED3F216EF25A7075746"
    },
    {
      "chainId": "umee-1",
      "address": "wftm-wei",
      "name": "Axelar WFTM",
      "symbol": "axlWFTM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16036/small/Fantom.png?1622679930",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei",
      "ibcDenom": "ibc/87FAA671A952F1203496AEF3787AC23A06592B2B52F79149AA67C621470673E6"
    },
    {
      "chainId": 1,
      "address": "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE",
      "name": "Ethereum",
      "symbol": "ETH",
      "decimals": 18,
      "logoURI": "https://tokens.1inch.io/0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee.png",
      "coingeckoId": "ethereum",
      "commonKey": "weth-wei"
    },
    {
      "chainId": 1,
      "address": "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2",
      "name": "Wrapped ETH",
      "symbol": "WETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei"
    },
    {
      "name": "Dai Stablecoin",
      "address": "0x6B175474E89094C44Da98b954EedeAC495271d0F",
      "symbol": "DAI",
      "decimals": 18,
      "chainId": 1,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei"
    },
    {
      "name": "Tether USD",
      "address": "0xdAC17F958D2ee523a2206206994597C13D831ec7",
      "symbol": "USDT",
      "decimals": 6,
      "chainId": 1,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt"
    },
    {
      "name": "USDCoin",
      "address": "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
      "symbol": "USDC",
      "decimals": 6,
      "chainId": 1,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48/logo.png",
      "coingeckoId": "usd-coin",
      "commonKey": "uusdc"
    },
    {
      "chainId": 1,
      "address": "0xB50721BCf8d664c30412Cfbc6cf7a15145234ad1",
      "name": "Arbitrum",
      "symbol": "ARB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/16547/small/arbitrium.png?1624418103",
      "coingeckoId": "arbitrum"
    },
    {
      "name": "Wrapped BTC",
      "address": "0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599",
      "symbol": "WBTC",
      "decimals": 8,
      "chainId": 1,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi"
    },
    {
      "chainId": 1,
      "address": "0x853d955aCEf822Db058eb8505911ED77F175b99e",
      "name": "Frax",
      "symbol": "FRAX",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/13422/thumb/frax_logo.png?1608476506",
      "coingeckoId": "frax"
    },
    {
      "name": "Curve DAO Token",
      "address": "0xD533a949740bb3306d119CC777fa900bA034cd52",
      "symbol": "CRV",
      "decimals": 18,
      "chainId": 1,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xD533a949740bb3306d119CC777fa900bA034cd52/logo.png",
      "coingeckoId": "curve-dao-token"
    },
    {
      "name": "Uniswap",
      "address": "0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984",
      "symbol": "UNI",
      "decimals": 18,
      "chainId": 1,
      "logoURI": "https://assets.coingecko.com/coins/images/12504/small/uniswap-uni.png?1600306604",
      "coingeckoId": "uniswap"
    },
    {
      "name": "Maker",
      "address": "0x9f8F72aA9304c8B593d555F12eF6589cC3A579A2",
      "symbol": "MKR",
      "decimals": 18,
      "chainId": 1,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x9f8F72aA9304c8B593d555F12eF6589cC3A579A2/logo.png",
      "coingeckoId": "maker"
    },
    {
      "name": "ChainLink Token",
      "address": "0x514910771AF9Ca656af840dff83E8264EcF986CA",
      "symbol": "LINK",
      "decimals": 18,
      "chainId": 1,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x514910771AF9Ca656af840dff83E8264EcF986CA/logo.png",
      "coingeckoId": "chainlink"
    },
    {
      "name": "Reputation Augur v2",
      "address": "0x221657776846890989a759BA2973e427DfF5C9bB",
      "symbol": "REPv2",
      "decimals": 18,
      "chainId": 1,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x221657776846890989a759BA2973e427DfF5C9bB/logo.png",
      "coingeckoId": "augur"
    },
    {
      "chainId": 1,
      "address": "0x0bc529c00C6401aEF6D220BE8C6Ea1667F6Ad93e",
      "name": "yearn finance",
      "symbol": "YFI",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/11849/thumb/yfi-192x192.png?1598325330",
      "coingeckoId": "yearn-finance"
    },
    {
      "name": "Republic Token",
      "address": "0x408e41876cCCDC0F92210600ef50372656052a38",
      "symbol": "REN",
      "decimals": 18,
      "chainId": 1,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x408e41876cCCDC0F92210600ef50372656052a38/logo.png",
      "coingeckoId": "republic-protocol"
    },
    {
      "name": "Fidu",
      "address": "0x6a445E9F40e0b97c92d0b8a3366cEF1d67F700BF",
      "symbol": "FIDU",
      "decimals": 18,
      "chainId": 1,
      "logoURI": "https://assets.coingecko.com/coins/images/25944/small/GFI-asset-icon.png?1654827482",
      "coingeckoId": "fidu"
    },
    {
      "chainId": 1,
      "address": "0xc944E90C64B2c07662A292be6244BDf05Cda44a7",
      "name": "The Graph",
      "symbol": "GRT",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/13397/thumb/Graph_Token.png?1608145566",
      "coingeckoId": "green-ride-token"
    },
    {
      "name": "Synthetix Network Token",
      "address": "0xC011a73ee8576Fb46F5E1c5751cA3B9Fe0af2a6F",
      "symbol": "SNX",
      "decimals": 18,
      "chainId": 1,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xC011a73ee8576Fb46F5E1c5751cA3B9Fe0af2a6F/logo.png",
      "coingeckoId": "havven"
    },
    {
      "chainId": 1,
      "address": "0x0F5D2fB29fb7d3CFeE444a200298f468908cC942",
      "name": "Decentraland",
      "symbol": "MANA",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/878/thumb/decentraland-mana.png?1550108745",
      "coingeckoId": "decentraland"
    },
    {
      "name": "Aragon",
      "address": "0xa117000000f279D81A1D3cc75430fAA017FA5A2e",
      "symbol": "ANT",
      "decimals": 18,
      "chainId": 1,
      "logoURI": "https://assets.coingecko.com/coins/images/681/thumb/JelZ58cv_400x400.png?1601449653",
      "coingeckoId": "aragon"
    },
    {
      "chainId": 1,
      "address": "0xec67005c4E498Ec7f55E092bd1d35cbC47C91892",
      "name": "Melon",
      "symbol": "MLN",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/605/thumb/melon.png?1547034295",
      "coingeckoId": "melon"
    },
    {
      "name": "Synth sUSD",
      "address": "0x57Ab1ec28D129707052df4dF418D58a2D46d5f51",
      "symbol": "sUSD",
      "decimals": 18,
      "chainId": 1,
      "logoURI": "https://assets.coingecko.com/coins/images/5013/thumb/sUSD.png?1616150765",
      "coingeckoId": "nusd"
    },
    {
      "chainId": 1,
      "address": "0x7D1AfA7B718fb893dB30A3aBc0Cfc608AaCfeBB0",
      "name": "Polygon",
      "symbol": "MATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/4713/thumb/matic-token-icon.png?1624446912",
      "coingeckoId": "matic-network"
    },
    {
      "chainId": 1,
      "address": "0x111111111117dC0aa78b770fA6A738034120C302",
      "name": "1inch",
      "symbol": "1INCH",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/13469/thumb/1inch-token.png?1608803028",
      "coingeckoId": "1inch"
    },
    {
      "chainId": 1,
      "address": "0x5732046A883704404F284Ce41FfADd5b007FD668",
      "name": "Bluzelle",
      "symbol": "BLZ",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/2848/thumb/ColorIcon_3x.png?1622516510",
      "coingeckoId": "bluzelle"
    },
    {
      "chainId": 1,
      "address": "0x03ab458634910AaD20eF5f1C8ee96F1D6ac54919",
      "name": "Rai Reflex Index",
      "symbol": "RAI",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/14004/thumb/RAI-logo-coin.png?1613592334",
      "coingeckoId": "rai"
    },
    {
      "chainId": 1,
      "address": "0xDDB3422497E61e13543BeA06989C0789117555c5",
      "name": "COTI",
      "symbol": "COTI",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/2962/thumb/Coti.png?1559653863",
      "coingeckoId": "coti"
    },
    {
      "chainId": 1,
      "address": "0xe53EC727dbDEB9E2d5456c3be40cFF031AB40A55",
      "name": "SuperFarm",
      "symbol": "SUPER",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/14040/thumb/6YPdWn6.png?1613975899",
      "coingeckoId": "superciety"
    },
    {
      "chainId": 1,
      "address": "0x84cA8bc7997272c7CfB4D0Cd3D55cd942B3c9419",
      "name": "DIA",
      "symbol": "DIA",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/11955/thumb/image.png?1646041751",
      "coingeckoId": "dia-data"
    },
    {
      "chainId": 1,
      "address": "0xA4EED63db85311E22dF4473f87CcfC3DaDCFA3E3",
      "name": "Rubic",
      "symbol": "RBC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12629/thumb/200x200.png?1607952509",
      "coingeckoId": "rubic"
    },
    {
      "chainId": 1,
      "address": "0xF629cBd94d3791C9250152BD8dfBDF380E2a3B9c",
      "name": "Enjin Coin",
      "symbol": "ENJ",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/1102/thumb/enjin-coin-logo.png?1547035078",
      "coingeckoId": "enjincoin"
    },
    {
      "chainId": 1,
      "address": "0x2e9d63788249371f1DFC918a52f8d799F4a38C94",
      "name": "Tokemak",
      "symbol": "TOKE",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/17495/thumb/tokemak-avatar-200px-black.png?1628131614",
      "coingeckoId": "non-fungible-toke"
    },
    {
      "chainId": 1,
      "address": "0xBBc2AE13b23d715c30720F079fcd9B4a74093505",
      "name": "Ethernity Chain",
      "symbol": "ERN",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/14238/thumb/LOGO_HIGH_QUALITY.png?1647831402",
      "coingeckoId": "ethernity-chain"
    },
    {
      "chainId": 1,
      "address": "0x7DD9c5Cba05E151C895FDe1CF355C9A1D5DA6429",
      "name": "Golem",
      "symbol": "GLM",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/542/thumb/Golem_Submark_Positive_RGB.png?1606392013",
      "coingeckoId": "golem"
    },
    {
      "chainId": 1,
      "address": "0x71Ab77b7dbB4fa7e017BC15090b2163221420282",
      "name": "Highstreet",
      "symbol": "HIGH",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/18973/thumb/logosq200200Coingecko.png?1634090470",
      "coingeckoId": "highstreet"
    },
    {
      "chainId": 1,
      "address": "0x41D5D79431A913C4aE7d69a668ecdfE5fF9DFB68",
      "name": "Inverse Finance",
      "symbol": "INV",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/14205/thumb/inverse_finance.jpg?1614921871",
      "coingeckoId": "inverse-finance"
    },
    {
      "chainId": 1,
      "address": "0x83e6f1E41cdd28eAcEB20Cb649155049Fac3D5Aa",
      "name": "Polkastarter",
      "symbol": "POLS",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12648/thumb/polkastarter.png?1609813702",
      "coingeckoId": "polkastarter"
    },
    {
      "chainId": 1,
      "address": "0x4a220E6096B25EADb88358cb44068A3248254675",
      "name": "Quant",
      "symbol": "QNT",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/3370/thumb/5ZOu7brX_400x400.jpg?1612437252",
      "coingeckoId": "quant-network"
    },
    {
      "chainId": 1,
      "address": "0xf1f955016EcbCd7321c7266BccFB96c68ea5E49b",
      "name": "Rally",
      "symbol": "RLY",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12843/thumb/image.png?1611212077",
      "coingeckoId": "rally-2"
    },
    {
      "chainId": 1,
      "address": "0x8f8221aFbB33998d8584A2B05749bA73c37a938a",
      "name": "Request",
      "symbol": "REQ",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/1031/thumb/Request_icon_green.png?1643250951",
      "coingeckoId": "request-network"
    },
    {
      "chainId": 1,
      "address": "0x6123B0049F904d730dB3C36a31167D9d4121fA6B",
      "name": "Ribbon Finance",
      "symbol": "RBN",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/15823/thumb/RBN_64x64.png?1633529723",
      "coingeckoId": "ribbon-finance"
    },
    {
      "chainId": 1,
      "address": "0xc770EEfAd204B5180dF6a14Ee197D99d808ee52d",
      "name": "ShapeShift FOX Token",
      "symbol": "FOX",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/9988/thumb/FOX.png?1574330622",
      "coingeckoId": "farmers-only"
    },
    {
      "chainId": 1,
      "address": "0x95aD61b0a150d79219dCF64E1E6Cc01f0B64C4cE",
      "name": "Shiba Inu",
      "symbol": "SHIB",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/11939/thumb/shiba.png?1622619446",
      "coingeckoId": "shiba-inu"
    },
    {
      "chainId": 1,
      "address": "0x6B3595068778DD592e39A122f4f5a5cF09C90fE2",
      "name": "Sushi",
      "symbol": "SUSHI",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12271/thumb/512x512_Logo_no_chop.png?1606986688",
      "coingeckoId": "sushi"
    },
    {
      "chainId": 1,
      "address": "0x3432B6A60D23Ca0dFCa7761B7ab56459D9C964D0",
      "name": "Frax Share",
      "symbol": "FXS",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/13423/thumb/frax_share.png?1608478989",
      "coingeckoId": "frax-share"
    },
    {
      "chainId": 1,
      "address": "0x967da4048cD07aB37855c090aAF366e4ce1b9F48",
      "name": "Ocean Protocol",
      "symbol": "OCEAN",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/3687/thumb/ocean-protocol-logo.jpg?1547038686",
      "coingeckoId": "ocean-protocol"
    },
    {
      "chainId": 1,
      "address": "0x45804880De22913dAFE09f4980848ECE6EcbAf78",
      "name": "PAX Gold",
      "symbol": "PAXG",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/9519/thumb/paxg.PNG?1568542565",
      "coingeckoId": "pax-gold"
    },
    {
      "chainId": 1,
      "address": "0x3845badAde8e6dFF049820680d1F14bD3903a5d0",
      "name": "The Sandbox",
      "symbol": "SAND",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12129/thumb/sandbox_logo.jpg?1597397942",
      "coingeckoId": "san-diego-coin"
    },
    {
      "chainId": 1,
      "address": "0x18aAA7115705e8be94bfFEBDE57Af9BFc265B998",
      "name": "Audius",
      "symbol": "AUDIO",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12913/thumb/AudiusCoinLogo_2x.png?1603425727",
      "coingeckoId": "audius"
    },
    {
      "chainId": 1,
      "address": "0x761D38e5ddf6ccf6Cf7c55759d5210750B5D60F3",
      "name": "Dogelon Mars",
      "symbol": "ELON",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/14962/thumb/6GxcPRo3_400x400.jpg?1619157413",
      "coingeckoId": "dogelon-mars"
    },
    {
      "chainId": 1,
      "address": "0x949D48EcA67b17269629c7194F4b727d4Ef9E5d6",
      "name": "Merit Circle",
      "symbol": "MC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/19304/thumb/Db4XqML.png?1634972154",
      "coingeckoId": "mechaverse"
    },
    {
      "chainId": 1,
      "address": "0xDf801468a808a32656D2eD2D2d80B72A129739f4",
      "name": "Somnium Space CUBEs",
      "symbol": "CUBE",
      "decimals": 8,
      "logoURI": "https://assets.coingecko.com/coins/images/10687/thumb/CUBE_icon.png?1617026861",
      "coingeckoId": "cube-network"
    },
    {
      "chainId": 1,
      "address": "0xADE00C28244d5CE17D72E40330B1c318cD12B7c3",
      "name": "Ambire AdEx",
      "symbol": "ADX",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/847/thumb/Ambire_AdEx_Symbol_color.png?1655432540",
      "coingeckoId": "adex"
    },
    {
      "chainId": 1,
      "address": "0xfB7B4564402E5500dB5bB6d63Ae671302777C75a",
      "name": "DexTools",
      "symbol": "DEXT",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/11603/thumb/dext.png?1605790188",
      "coingeckoId": "dextools"
    },
    {
      "chainId": 1,
      "address": "0x1494CA1F11D487c2bBe4543E90080AeBa4BA3C2b",
      "name": "DeFi Pulse Index",
      "symbol": "DPI",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/12465/thumb/defi_pulse_index_set.png?1600051053",
      "coingeckoId": "defipulse-index"
    },
    {
      "chainId": 1,
      "name": "Metis",
      "symbol": "METIS",
      "logoURI": "https://assets.coingecko.com/coins/images/15595/thumb/metis.jpeg?1660285312",
      "address": "0x9E32b13ce7f2E80A01932B42553652E053D6ed8e",
      "decimals": 18,
      "coingeckoId": "metis-token"
    },
    {
      "chainId": 1,
      "name": "Monavale",
      "symbol": "MONA",
      "logoURI": "https://assets.coingecko.com/coins/images/13298/thumb/monavale_logo.jpg?1607232721",
      "address": "0x275f5Ad03be0Fa221B4C6649B8AeE09a42D9412A",
      "decimals": 18,
      "coingeckoId": "mona"
    },
    {
      "chainId": 1,
      "name": "Muse DAO",
      "symbol": "MUSE",
      "logoURI": "https://assets.coingecko.com/coins/images/13230/thumb/muse_logo.png?1606460453",
      "address": "0xB6Ca7399B4F9CA56FC27cBfF44F4d2e4Eef1fc81",
      "decimals": 18,
      "coingeckoId": "muse-2"
    },
    {
      "chainId": 1,
      "name": "Rook",
      "symbol": "ROOK",
      "logoURI": "https://assets.coingecko.com/coins/images/13005/thumb/keeper_dao_logo.jpg?1604316506",
      "address": "0xfA5047c9c78B8877af97BDcb85Db743fD7313d4a",
      "decimals": 18,
      "coingeckoId": "rook"
    },
    {
      "chainId": 1,
      "name": "Sylo",
      "symbol": "SYLO",
      "logoURI": "https://assets.coingecko.com/coins/images/6430/thumb/SYLO.svg?1589527756",
      "address": "0xf293d23BF2CDc05411Ca0edDD588eb1977e8dcd4",
      "decimals": 18,
      "coingeckoId": "sylo"
    },
    {
      "chainId": 1,
      "name": "The Virtua Kolect",
      "symbol": "TVK",
      "logoURI": "https://assets.coingecko.com/coins/images/13330/thumb/virtua_original.png?1656043619",
      "address": "0xd084B83C305daFD76AE3E1b4E1F1fe2eCcCb3988",
      "decimals": 18,
      "coingeckoId": "the-virtua-kolect"
    },
    {
      "chainId": 1,
      "name": "WOO Network",
      "symbol": "WOO",
      "logoURI": "https://assets.coingecko.com/coins/images/12921/thumb/w2UiemF__400x400.jpg?1603670367",
      "address": "0x4691937a7508860F876c9c0a2a617E7d9E945D4B",
      "decimals": 18,
      "coingeckoId": "woo-network"
    },
    {
      "chainId": 1,
      "name": "Chain",
      "symbol": "XCN",
      "logoURI": "https://assets.coingecko.com/coins/images/24210/thumb/Chain_icon_200x200.png?1646895054",
      "address": "0xA2cd3D43c775978A96BdBf12d733D5A1ED94fb18",
      "decimals": 18,
      "coingeckoId": "chain-2"
    },
    {
      "chainId": 1,
      "name": "BUSD",
      "symbol": "BUSD",
      "logoURI": "https://assets.coingecko.com/coins/images/9576/thumb/BUSD.png?1568947766",
      "address": "0x4Fabb145d64652a948d72533023f6E7A623C7C53",
      "decimals": 18,
      "coingeckoId": "binance-usd",
      "commonKey": "busd-wei"
    },
    {
      "address": "0x7Fc66500c84A76Ad7e9c93437bFc5Ac33E2DDaE9",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/aave.jpg",
      "name": "AAVE",
      "symbol": "AAVE",
      "coingeckoId": "aave"
    },
    {
      "address": "0xdBdb4d16EdA451D0503b854CF79D55697F90c8DF",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/alcx.jpg",
      "name": "Alchemix",
      "symbol": "ALCX",
      "coingeckoId": "alchemix"
    },
    {
      "address": "0xa1faa113cbE53436Df28FF0aEe54275c13B40975",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0xa1faa113cbE53436Df28FF0aEe54275c13B40975/logo.png",
      "name": "Alpha Finance",
      "symbol": "ALPHA",
      "coingeckoId": "aavegotchi-alpha"
    },
    {
      "address": "0xfF20817765cB7f73d4bde2e66e067E58D11095C2",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/amp.jpg",
      "name": "Amp",
      "symbol": "AMP",
      "coingeckoId": "amp-token"
    },
    {
      "address": "0x4104b135DBC9609Fc1A9490E61369036497660c8",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x4104b135DBC9609Fc1A9490E61369036497660c8/logo.png",
      "name": "APWine Token",
      "symbol": "APW",
      "coingeckoId": "apwine"
    },
    {
      "address": "0x97Bbbc5d96875fB78D2F14b7FF8d7a3a74106F17",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/ethereum/0x97Bbbc5d96875fB78D2F14b7FF8d7a3a74106F17.jpg",
      "name": "Astrafer",
      "symbol": "ASTRAFER",
      "coingeckoId": "astrafer"
    },
    {
      "address": "0xA9B1Eb5908CfC3cdf91F9B8B3a74108598009096",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/auction.jpg",
      "name": "Bounce Token",
      "symbol": "AUCTION",
      "coingeckoId": "auction"
    },
    {
      "address": "0xBA11D00c5f74255f56a5E366F4F77f5A186d7f55",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/band.jpg",
      "name": "Band Protocol",
      "symbol": "BAND",
      "coingeckoId": "band-protocol"
    },
    {
      "address": "0x24A6A37576377F63f194Caa5F518a60f45b42921",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x24A6A37576377F63f194Caa5F518a60f45b42921/logo.png",
      "name": "Float Bank",
      "symbol": "BANK",
      "coingeckoId": "bankless-dao"
    },
    {
      "address": "0x374CB8C27130E2c9E04F44303f3c8351B9De61C1",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x374CB8C27130E2c9E04F44303f3c8351B9De61C1/logo.png",
      "name": "Bao Finance",
      "symbol": "BAO",
      "coingeckoId": "bao"
    },
    {
      "address": "0x0309c98B1bffA350bcb3F9fB9780970CA32a5060",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x0309c98B1bffA350bcb3F9fB9780970CA32a5060/logo.png",
      "name": "BasketDAO DeFi Index",
      "symbol": "BDI",
      "coingeckoId": "basketdao"
    },
    {
      "address": "0xF17e65822b568B3903685a7c9F496CF7656Cc6C2",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0xF17e65822b568B3903685a7c9F496CF7656Cc6C2/logo.png",
      "name": "Biconomy Token",
      "symbol": "BICO",
      "coingeckoId": "biconomy"
    },
    {
      "address": "0x2791BfD60D232150Bff86b39B7146c0eaAA2BA81",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/bifi.jpg",
      "name": "BiFi",
      "symbol": "BIFI",
      "coingeckoId": "beefy-finance"
    },
    {
      "address": "0x1A4b46696b2bB4794Eb3D4c26f1c55F9170fa4C5",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/ethereum/0x1A4b46696b2bB4794Eb3D4c26f1c55F9170fa4C5.jpg",
      "name": "BitDAO",
      "symbol": "BIT",
      "coingeckoId": "biconomy-exchange-token"
    },
    {
      "address": "0x8a6D4C8735371EBAF8874fBd518b56Edd66024eB",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x8a6D4C8735371EBAF8874fBd518b56Edd66024eB/logo.png",
      "name": "Blocks Dao",
      "symbol": "BLOCKS",
      "coingeckoId": "blocks"
    },
    {
      "address": "0x725C263e32c72dDC3A19bEa12C5a0479a81eE688",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x725C263e32c72dDC3A19bEa12C5a0479a81eE688/logo.png",
      "name": "Bridge Mutual",
      "symbol": "BMI",
      "coingeckoId": "bridge-mutual"
    },
    {
      "address": "0x0eC9F76202a7061eB9b3a7D6B59D36215A7e37da",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x0eC9F76202a7061eB9b3a7D6B59D36215A7e37da/logo.png",
      "name": "BlackPool Token",
      "symbol": "BPT",
      "coingeckoId": "blackpool-token"
    },
    {
      "address": "0xAE12C5930881c53715B369ceC7606B70d8EB229f",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0xAE12C5930881c53715B369ceC7606B70d8EB229f/logo.png",
      "name": "Coin98",
      "symbol": "C98",
      "coingeckoId": "coin98"
    },
    {
      "address": "0xCB56b52316041A62B6b5D0583DcE4A8AE7a3C629",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0xCB56b52316041A62B6b5D0583DcE4A8AE7a3C629/logo.png",
      "name": "Cigarette Token",
      "symbol": "CIG",
      "coingeckoId": "cigarette-token"
    },
    {
      "address": "0xc00e94Cb662C3520282E6f5717214004A7f26888",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/comp.jpg",
      "name": "Compound",
      "symbol": "COMP",
      "coingeckoId": "compound-coin"
    },
    {
      "address": "0x2ba592F78dB6436527729929AAf6c908497cB200",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/cream.jpg",
      "name": "Cream",
      "symbol": "CREAM",
      "coingeckoId": "cream-2"
    },
    {
      "address": "0x4e3FBD56CD56c3e72c1403e103b45Db9da5B9D2B",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x4e3FBD56CD56c3e72c1403e103b45Db9da5B9D2B/logo.png",
      "name": "Convex Token",
      "symbol": "CVX",
      "coingeckoId": "convex-finance"
    },
    {
      "address": "0xE00639A1f59B52773b7d39d9F9beF07F6248dbAe",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/ethereum/0xE00639A1f59B52773b7d39d9F9beF07F6248dbAe.jpg",
      "name": "The DAOX Index",
      "symbol": "DAOX",
      "coingeckoId": "the-daox-index"
    },
    {
      "address": "0x9EA3b5b4EC044b70375236A281986106457b20EF",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x9EA3b5b4EC044b70375236A281986106457b20EF/logo.png",
      "name": "DELTA",
      "symbol": "DELTA",
      "coingeckoId": "delta-financial"
    },
    {
      "address": "0xBAac2B4491727D78D2b78815144570b9f2Fe8899",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0xBAac2B4491727D78D2b78815144570b9f2Fe8899/logo.png",
      "name": "The Doge NFT",
      "symbol": "DOG",
      "coingeckoId": "dog"
    },
    {
      "address": "0xad32A8e6220741182940c5aBF610bDE99E737b2D",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0xad32A8e6220741182940c5aBF610bDE99E737b2D/logo.png",
      "name": "PieDAO",
      "symbol": "DOUGH",
      "coingeckoId": "dough"
    },
    {
      "address": "0x92D6C1e31e14520e676a687F0a93788B716BEff5",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x92D6C1e31e14520e676a687F0a93788B716BEff5/logo.png",
      "name": "dYdX",
      "symbol": "DYDX",
      "coingeckoId": "dydx"
    },
    {
      "address": "0x1559FA1b8F28238FD5D76D9f434ad86FD20D1559",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/eden.jpg",
      "name": "Eden",
      "symbol": "EDEN",
      "coingeckoId": "eden"
    },
    {
      "address": "0xC18360217D8F7Ab5e7c516566761Ea12Ce7F9D72",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0xC18360217D8F7Ab5e7c516566761Ea12Ce7F9D72/logo.png",
      "name": "Ethereum Name Service",
      "symbol": "ENS",
      "coingeckoId": "ethereum-name-service"
    },
    {
      "address": "0xb05097849BCA421A3f51B249BA6CCa4aF4b97cb9",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0xb05097849BCA421A3f51B249BA6CCa4aF4b97cb9/logo.png",
      "name": "Float FLOAT",
      "symbol": "FLOAT",
      "coingeckoId": "float-protocol-float"
    },
    {
      "address": "0x7f280daC515121DcdA3EaC69eB4C13a52392CACE",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x7f280daC515121DcdA3EaC69eB4C13a52392CACE/logo.png",
      "name": "Fancy Games",
      "symbol": "FNC",
      "coingeckoId": "fancy-games"
    },
    {
      "address": "0x4C2e59D098DF7b6cBaE0848d66DE2f8A4889b9C3",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x4C2e59D098DF7b6cBaE0848d66DE2f8A4889b9C3/logo.png",
      "name": "Fodl",
      "symbol": "FODL",
      "coingeckoId": "fodl-finance"
    },
    {
      "address": "0xd084944d3c05CD115C09d072B9F44bA3E0E45921",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0xd084944d3c05CD115C09d072B9F44bA3E0E45921/logo.png",
      "name": "Manifold Finance",
      "symbol": "FOLD",
      "coingeckoId": "manifold-finance"
    },
    {
      "address": "0x4E15361FD6b4BB609Fa63C81A2be19d873717870",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/ftm.jpg",
      "name": "Fantom Token",
      "symbol": "FTM",
      "coingeckoId": "fantom"
    },
    {
      "address": "0x50D1c9771902476076eCFc8B2A83Ad6b9355a4c9",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x50D1c9771902476076eCFc8B2A83Ad6b9355a4c9/logo.png",
      "name": "FTX Token",
      "symbol": "FTT",
      "coingeckoId": "ftx-token"
    },
    {
      "address": "0x8a854288a5976036A725879164Ca3e91d30c6A1B",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x8a854288a5976036A725879164Ca3e91d30c6A1B/logo.png",
      "name": "Guaranteed Entrance Token",
      "symbol": "GET",
      "coingeckoId": "get"
    },
    {
      "address": "0xccC8cb5229B0ac8069C51fd58367Fd1e622aFD97",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0xccC8cb5229B0ac8069C51fd58367Fd1e622aFD97/logo.png",
      "name": "Gods Unchained",
      "symbol": "GODS",
      "coingeckoId": "gods-unchained"
    },
    {
      "address": "0x9AB7bb7FdC60f4357ECFef43986818A2A3569c62",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x9AB7bb7FdC60f4357ECFef43986818A2A3569c62/logo.png",
      "name": "Guild of Guardians",
      "symbol": "GOG",
      "coingeckoId": "guild-of-guardians"
    },
    {
      "address": "0xc5102fE9359FD9a28f877a67E36B0F050d81a3CC",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/ethereum/0xc5102fE9359FD9a28f877a67E36B0F050d81a3CC.jpg",
      "name": "Hop",
      "symbol": "HOP",
      "coingeckoId": "hop-protocol"
    },
    {
      "address": "0xf16e81dce15B08F326220742020379B855B87DF9",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/ice.jpg",
      "name": "IceToken",
      "symbol": "ICE",
      "coingeckoId": "decentral-games-ice"
    },
    {
      "address": "0x903bEF1736CDdf2A537176cf3C64579C3867A881",
      "chainId": 1,
      "decimals": 9,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x903bEF1736CDdf2A537176cf3C64579C3867A881/logo.png",
      "name": "ichi.farm",
      "symbol": "ICHI",
      "coingeckoId": "ichi-farm"
    },
    {
      "address": "0x0acC0FEE1D86D2cD5AF372615bf59b298D50cd69",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/ethereum/0x0acC0FEE1D86D2cD5AF372615bf59b298D50cd69.jpg",
      "name": "Invest Like Stakeborg Index",
      "symbol": "ILSI",
      "coingeckoId": "invest-like-stakeborg-index"
    },
    {
      "address": "0x767FE9EDC9E0dF98E07454847909b5E959D7ca0E",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x767FE9EDC9E0dF98E07454847909b5E959D7ca0E/logo.png",
      "name": "Illuvium",
      "symbol": "ILV",
      "coingeckoId": "illuvium"
    },
    {
      "address": "0xF57e7e7C23978C3cAEC3C3548E3D615c346e79fF",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0xF57e7e7C23978C3cAEC3C3548E3D615c346e79fF/logo.png",
      "name": "Immutable X",
      "symbol": "IMX",
      "coingeckoId": "immutable-x"
    },
    {
      "address": "0xe28b3B32B6c345A34Ff64674606124Dd5Aceca30",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/inj.jpg",
      "name": "Injective Protocol",
      "symbol": "INJ",
      "coingeckoId": "injective-protocol"
    },
    {
      "address": "0x579CEa1889991f68aCc35Ff5c3dd0621fF29b0C9",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x579CEa1889991f68aCc35Ff5c3dd0621fF29b0C9/logo.png",
      "name": "Everipedia IQ",
      "symbol": "IQ",
      "coingeckoId": "everipedia"
    },
    {
      "address": "0xE80C0cd204D654CEbe8dd64A4857cAb6Be8345a3",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0xE80C0cd204D654CEbe8dd64A4857cAb6Be8345a3/logo.png",
      "name": "JPEGd Governance Token",
      "symbol": "JPEG",
      "coingeckoId": "jpeg-d"
    },
    {
      "address": "0x1cEB5cB57C4D4E2b2433641b95Dd330A33185A44",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/kp3r.jpg",
      "name": "Keep3rV1",
      "symbol": "KP3R",
      "coingeckoId": "keep3rv1"
    },
    {
      "address": "0x5A98FcBEA516Cf06857215779Fd812CA3beF1B32",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x5A98FcBEA516Cf06857215779Fd812CA3beF1B32/logo.png",
      "name": "Lido DAO",
      "symbol": "LDO",
      "coingeckoId": "lido-dao"
    },
    {
      "address": "0x01BA67AAC7f75f647D94220Cc98FB30FCc5105Bf",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x01BA67AAC7f75f647D94220Cc98FB30FCc5105Bf/logo.png",
      "name": "Lyra Token",
      "symbol": "LYRA",
      "coingeckoId": "lyra-finance"
    },
    {
      "address": "0x4Af698B479D0098229DC715655c667Ceb6cd8433",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/maid.jpg",
      "name": "MaidCoin",
      "symbol": "MAID",
      "coingeckoId": ""
    },
    {
      "address": "0x16CDA4028e9E872a38AcB903176719299beAed87",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x16CDA4028e9E872a38AcB903176719299beAed87/logo.png",
      "name": "MARS4",
      "symbol": "MARS4",
      "coingeckoId": "mars4"
    },
    {
      "address": "0x06F3C323f0238c72BF35011071f2b5B7F43A054c",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/8376.png",
      "name": "MASQ",
      "symbol": "MASQ",
      "coingeckoId": "masq"
    },
    {
      "address": "0x99D8a9C45b2ecA8864373A26D1459e3Dff1e17F3",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/mim.jpg",
      "name": "Magic Internet Money",
      "symbol": "MIM",
      "coingeckoId": "magic-internet-money"
    },
    {
      "address": "0x65Ef703f5594D2573eb71Aaf55BC0CB548492df4",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x65Ef703f5594D2573eb71Aaf55BC0CB548492df4/logo.png",
      "name": "Multichain",
      "symbol": "MULTI",
      "coingeckoId": "multichain"
    },
    {
      "address": "0xDFDb7f72c1F195C5951a234e8DB9806EB0635346",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0xDFDb7f72c1F195C5951a234e8DB9806EB0635346/logo.png",
      "name": "Feisty Doge NFT",
      "symbol": "NFD",
      "coingeckoId": "feisty-doge-nft"
    },
    {
      "address": "0x3c8D2FCE49906e11e71cB16Fa0fFeB2B16C29638",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x3c8D2FCE49906e11e71cB16Fa0fFeB2B16C29638/logo.png",
      "name": "Nifty League",
      "symbol": "NFTL",
      "coingeckoId": "nftlaunch"
    },
    {
      "address": "0x0De05F6447ab4D22c8827449EE4bA2D5C288379B",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x0De05F6447ab4D22c8827449EE4bA2D5C288379B/logo.png",
      "name": "OOKI",
      "symbol": "OOKI",
      "coingeckoId": "ooki"
    },
    {
      "address": "0x808507121B80c02388fAd14726482e061B8da827",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x808507121B80c02388fAd14726482e061B8da827/logo.png",
      "name": "Pendle",
      "symbol": "PENDLE",
      "coingeckoId": "pendle"
    },
    {
      "address": "0x429881672B9AE42b8EbA0E26cD9C73711b891Ca5",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/pickle.jpg",
      "name": "Pickle Token",
      "symbol": "PICKLE",
      "coingeckoId": "pickle-finance"
    },
    {
      "address": "0x57B946008913B82E4dF85f501cbAeD910e58D26C",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x57B946008913B82E4dF85f501cbAeD910e58D26C/logo.png",
      "name": "Marlin POND",
      "symbol": "POND",
      "coingeckoId": "marlin"
    },
    {
      "address": "0x6399C842dD2bE3dE30BF99Bc7D1bBF6Fa3650E70",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x6399C842dD2bE3dE30BF99Bc7D1bBF6Fa3650E70/logo.png",
      "name": "Premia",
      "symbol": "PREMIA",
      "coingeckoId": "premia"
    },
    {
      "address": "0xDb0f18081b505A7DE20B18ac41856BCB4Ba86A1a",
      "chainId": 1,
      "decimals": 9,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/pwing.jpg",
      "name": "Poly Ontology Wing Token",
      "symbol": "pWING",
      "coingeckoId": ""
    },
    {
      "address": "0x44709a920fCcF795fbC57BAA433cc3dd53C44DbE",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x44709a920fCcF795fbC57BAA433cc3dd53C44DbE/logo.png",
      "name": "DappRadar",
      "symbol": "RADAR",
      "coingeckoId": "dappradar"
    },
    {
      "address": "0xe76C6c83af64e4C60245D8C7dE953DF673a7A33D",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0xe76C6c83af64e4C60245D8C7dE953DF673a7A33D/logo.png",
      "name": "Rail",
      "symbol": "RAIL",
      "coingeckoId": "railgun"
    },
    {
      "address": "0x607F4C5BB672230e8672085532f7e901544a7375",
      "chainId": 1,
      "decimals": 9,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/rlc.jpg",
      "name": "RLC",
      "symbol": "RLC",
      "coingeckoId": "iexec-rlc"
    },
    {
      "address": "0x3155BA85D5F96b2d030a4966AF206230e46849cb",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/rune.jpg",
      "name": "THORChain ETH.RUNE",
      "symbol": "RUNE",
      "coingeckoId": "thorchain"
    },
    {
      "address": "0xe9F84dE264E91529aF07Fa2C746e934397810334",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0xe9F84dE264E91529aF07Fa2C746e934397810334/logo.png",
      "name": "Sake",
      "symbol": "SAK3",
      "coingeckoId": "sak3"
    },
    {
      "address": "0x3b484b82567a09e2588A13D54D032153f0c0aEe0",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x3b484b82567a09e2588A13D54D032153f0c0aEe0/logo.png",
      "name": "OpenDao SOS",
      "symbol": "SOS",
      "coingeckoId": "opendao"
    },
    {
      "address": "0x090185f2135308BaD17527004364eBcC2D37e5F6",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x090185f2135308BaD17527004364eBcC2D37e5F6/logo.png",
      "name": "Spell Token",
      "symbol": "SPELL",
      "coingeckoId": "spell-token"
    },
    {
      "address": "0x00813E3421E1367353BfE7615c7f7f133C89df74",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x00813E3421E1367353BfE7615c7f7f133C89df74/logo.png",
      "name": "Splintershards",
      "symbol": "SPS",
      "coingeckoId": "splinterlands"
    },
    {
      "address": "0x476c5E26a75bd202a9683ffD34359C0CC15be0fF",
      "chainId": 1,
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/srm.jpg",
      "name": "Serum",
      "symbol": "SRM",
      "coingeckoId": "serum"
    },
    {
      "address": "0x44017598f2AF1bD733F9D87b5017b4E7c1B28DDE",
      "chainId": 1,
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x44017598f2AF1bD733F9D87b5017b4E7c1B28DDE/logo.png",
      "name": "pSTAKE Staked Atom",
      "symbol": "stkATOM",
      "coingeckoId": "pstake-staked-atom"
    },
    {
      "address": "0x0f2D719407FdBeFF09D87557AbB7232601FD9F29",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x0f2D719407FdBeFF09D87557AbB7232601FD9F29/logo.png",
      "name": "Synapse",
      "symbol": "SYN",
      "coingeckoId": "synapse-2"
    },
    {
      "address": "0x108a850856Db3f85d0269a2693D896B394C80325",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x108a850856Db3f85d0269a2693D896B394C80325/logo.png",
      "name": "THORWallet Governance Token",
      "symbol": "TGT",
      "coingeckoId": "thorwallet"
    },
    {
      "address": "0xa5f2211B9b8170F694421f2046281775E8468044",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/ethereum/0xa5f2211B9b8170F694421f2046281775E8468044.jpg",
      "name": "THORSwap Token",
      "symbol": "THOR",
      "coingeckoId": "thor"
    },
    {
      "address": "0x7825e833D495F3d1c28872415a4aee339D26AC88",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/ethereum/0x7825e833D495F3d1c28872415a4aee339D26AC88.jpg",
      "name": "Telos",
      "symbol": "TLOS",
      "coingeckoId": "telos"
    },
    {
      "address": "0x4C19596f5aAfF459fA38B0f7eD92F11AE6543784",
      "chainId": 1,
      "decimals": 8,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/ethereum/0x4C19596f5aAfF459fA38B0f7eD92F11AE6543784.jpg",
      "name": "TrueFi",
      "symbol": "TRU",
      "coingeckoId": "truebit-protocol"
    },
    {
      "address": "0x0000000000085d4780B73119b644AE5ecd22b376",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/ethereum/0x0000000000085d4780B73119b644AE5ecd22b376.jpg",
      "name": "TrueUSD",
      "symbol": "TUSD",
      "coingeckoId": "true-usd"
    },
    {
      "address": "0x04Fa0d235C4abf4BcF4787aF4CF447DE572eF828",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/uma.jpg",
      "name": "UMA Voting Token v1",
      "symbol": "UMA",
      "coingeckoId": "uma"
    },
    {
      "address": "0x55C08ca52497e2f1534B59E2917BF524D4765257",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/ethereum/0x55C08ca52497e2f1534B59E2917BF524D4765257.jpg",
      "name": "UwU Lend",
      "symbol": "UwU",
      "coingeckoId": "uwu-lend"
    },
    {
      "address": "0xcB84d72e61e383767C4DFEb2d8ff7f4FB89abc6e",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0xcB84d72e61e383767C4DFEb2d8ff7f4FB89abc6e/logo.png",
      "name": "Vega",
      "symbol": "VEGA",
      "coingeckoId": "vega-coin"
    },
    {
      "address": "0xf203Ca1769ca8e9e8FE1DA9D147DB68B6c919817",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/wncg.jpg",
      "name": "Wrapped NCG",
      "symbol": "WNCG",
      "coingeckoId": "wrapped-ncg"
    },
    {
      "address": "0xABe580E7ee158dA464b51ee1a83Ac0289622e6be",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/xft.jpg",
      "name": "Offshift",
      "symbol": "XFT",
      "coingeckoId": "offshift"
    },
    {
      "address": "0x69fa0feE221AD11012BAb0FdB45d444D3D2Ce71c",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x69fa0feE221AD11012BAb0FdB45d444D3D2Ce71c/logo.png",
      "name": "XRUNE Token",
      "symbol": "XRUNE",
      "coingeckoId": "thorstarter"
    },
    {
      "address": "0x8798249c2E607446EfB7Ad49eC89dD1865Ff4272",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/xsushi.jpg",
      "name": "SushiBar",
      "symbol": "xSUSHI",
      "coingeckoId": "xsushi"
    },
    {
      "address": "0x0AaCfbeC6a24756c20D41914F2caba817C0d8521",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/yam.jpg",
      "name": "YAM",
      "symbol": "YAM",
      "coingeckoId": "yam-2"
    },
    {
      "address": "0x7815bDa662050D84718B988735218CFfd32f75ea",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/ethereum/assets/0x7815bDa662050D84718B988735218CFfd32f75ea/logo.png",
      "name": "YEL Token",
      "symbol": "YEL",
      "coingeckoId": "yel-finance"
    },
    {
      "address": "0x25f8087EAD173b73D6e8B84329989A8eEA16CF73",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/ygg.jpg",
      "name": "Yield Guild Games Token",
      "symbol": "YGG",
      "coingeckoId": "yield-guild-games"
    },
    {
      "address": "0x9d409a0A012CFbA9B15F6D4B36Ac57A46966Ab9a",
      "chainId": 1,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/token/yvboost.jpg",
      "name": "Yearn Compounding veCRV yVault",
      "symbol": "yvBOOST",
      "coingeckoId": "yvboost"
    },
    {
      "chainId": 250,
      "address": "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE",
      "name": "Fantom",
      "symbol": "FTM",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/3513.png",
      "coingeckoId": "fantom",
      "commonKey": "wftm-wei"
    },
    {
      "chainId": 250,
      "address": "0x1B6382DBDEa11d97f24495C9A90b7c88469134a4",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://assets.coingecko.com/coins/images/6319/thumb/USD_Coin_icon.png?1547042389",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc"
    },
    {
      "name": "USDCoin",
      "address": "0x04068DA6C83AFCFA0e13ba15A6696662335D5B75",
      "symbol": "USDC",
      "decimals": 6,
      "chainId": 250,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "usd-coin"
    },
    {
      "chainId": 250,
      "address": "0x21be370D5312f44cB42ce377BC9b8a0cEF1A4C83",
      "name": "Wrapped FTM",
      "symbol": "WFTM",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/3513.png",
      "coingeckoId": "wrapped-fantom",
      "commonKey": "wftm-wei"
    },
    {
      "name": "Dai Stablecoin",
      "symbol": "DAI",
      "address": "0x8D11eC38a3EB5E956B052f67Da8Bdc9bef8Abf3E",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/9956/thumb/4943.png?1636636734",
      "coingeckoId": "dai"
    },
    {
      "name": "SpookySwap",
      "symbol": "BOO",
      "address": "0x841FAD6EAe12c286d1Fd18d1d525DFfA75C7EFFE",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/BOO.png",
      "coingeckoId": "spookyswap"
    },
    {
      "name": "Curve DAO",
      "symbol": "CRV",
      "address": "0x1E4F97b9f9F913c46F1632781732927B9019C68b",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xD533a949740bb3306d119CC777fa900bA034cd52/logo.png",
      "coingeckoId": "curve-dao-token"
    },
    {
      "name": "Wrapped Bitcoin",
      "symbol": "wBTC",
      "address": "0x321162Cd933E2Be498Cd2267a90534A804051b11",
      "chainId": 250,
      "decimals": 8,
      "logoURI": "https://assets.spooky.fi/tokens/wBTC.png",
      "coingeckoId": "wrapped-bitcoin"
    },
    {
      "name": "Wrapped Ether",
      "symbol": "wETH",
      "address": "0x74b23882a30290451A17c44f4F05243b6b58C76d",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/wETH.png",
      "coingeckoId": "weth"
    },
    {
      "name": "Frapped USDT",
      "symbol": "fUSDT",
      "address": "0x049d68029688eAbF473097a2fC38ef61633A3C7A",
      "chainId": 250,
      "decimals": 6,
      "logoURI": "https://assets.spooky.fi/tokens/fUSDT.png",
      "coingeckoId": "fluid-usdt"
    },
    {
      "name": "miMATIC",
      "symbol": "MAI",
      "address": "0xfB98B335551a418cD0737375a2ea0ded62Ea213b",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/MAI.png",
      "coingeckoId": "mimatic"
    },
    {
      "name": "ChainLink",
      "symbol": "LINK",
      "address": "0xb3654dc3D10Ea7645f8319668E8F54d2574FBdC8",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/LINK.png",
      "coingeckoId": "chainlink"
    },
    {
      "name": "Sushi",
      "symbol": "SUSHI",
      "address": "0xae75A438b2E0cB8Bb01Ec1E1e376De11D44477CC",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/SUSHI.png",
      "coingeckoId": "sushi"
    },
    {
      "name": "Binance Coin",
      "symbol": "BNB",
      "address": "0xD67de0e0a0Fd7b15dC8348Bb9BE742F3c5850454",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/BNB.png",
      "coingeckoId": "binancecoin"
    },
    {
      "name": "Anyswap",
      "symbol": "ANY",
      "address": "0xdDcb3fFD12750B45d32E084887fdf1aABAb34239",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/ANY.png",
      "coingeckoId": "anyswap"
    },
    {
      "name": "Beefy.Finance",
      "symbol": "BIFI",
      "address": "0xd6070ae98b8069de6B494332d1A1a81B6179D960",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/BIFI.png",
      "coingeckoId": "beefy-finance"
    },
    {
      "name": "TOMB",
      "symbol": "TOMB",
      "address": "0x6c021Ae822BEa943b2E66552bDe1D2696a53fbB7",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/TOMB.png",
      "coingeckoId": "tomb"
    },
    {
      "name": "TSHARE",
      "symbol": "TSHARE",
      "address": "0x4cdF39285D7Ca8eB3f090fDA0C069ba5F4145B37",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/TSHARE.png",
      "coingeckoId": "tomb-shares"
    },
    {
      "name": "Magic Internet Money",
      "symbol": "MIM",
      "address": "0x82f0B8B456c1A451378467398982d4834b6829c1",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/MIM.png",
      "coingeckoId": "magic-internet-money"
    },
    {
      "name": "Scream",
      "symbol": "SCREAM",
      "address": "0xe0654C8e6fd4D733349ac7E09f6f23DA256bF475",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/SCREAM.png",
      "coingeckoId": "scream"
    },
    {
      "name": "Tarot",
      "symbol": "TAROT",
      "address": "0xC5e2B037D30a390e62180970B3aa4E91868764cD",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/TAROT.png",
      "coingeckoId": "tarot"
    },
    {
      "name": "Wootrade Network",
      "symbol": "WOO",
      "address": "0x6626c47c00F1D87902fc13EECfaC3ed06D5E8D8a",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/WOO.png",
      "coingeckoId": "woo-network"
    },
    {
      "name": "Treeb",
      "symbol": "TREEB",
      "address": "0xc60D7067dfBc6f2caf30523a064f416A5Af52963",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/TREEB.png",
      "coingeckoId": "treeb"
    },
    {
      "name": "Geist.Finance Protocol Token",
      "symbol": "GEIST",
      "address": "0xd8321AA83Fb0a4ECd6348D4577431310A6E0814d",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/GEIST.png",
      "coingeckoId": "geist-finance"
    },
    {
      "name": "Spell Token",
      "symbol": "SPELL",
      "address": "0x468003B688943977e6130F4F68F23aad939a1040",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/SPELL.png",
      "coingeckoId": "spell-token"
    },
    {
      "name": "Yoshi.exchange",
      "symbol": "YOSHI",
      "address": "0x3dc57B391262e3aAe37a08D91241f9bA9d58b570",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/YOSHI.png",
      "coingeckoId": "yoshi-exchange"
    },
    {
      "name": "PaintSwap Token",
      "symbol": "BRUSH",
      "address": "0x85dec8c4B2680793661bCA91a8F129607571863d",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/BRUSH.png",
      "coingeckoId": "paint-swap"
    },
    {
      "name": "Avalanche",
      "symbol": "AVAX",
      "address": "0x511D35c52a3C244E7b8bd92c0C297755FbD89212",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/AVAX.png",
      "coingeckoId": "avalanche-2"
    },
    {
      "name": "Multichain",
      "symbol": "MULTI",
      "address": "0x9Fb9a33956351cf4fa040f65A13b835A3C8764E3",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/MULTI.png",
      "coingeckoId": "multichain"
    },
    {
      "name": "Polygon",
      "symbol": "MATIC",
      "address": "0x40DF1Ae6074C35047BFF66675488Aa2f9f6384F3",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/MATIC.png",
      "coingeckoId": "matic-network"
    },
    {
      "name": "TOR",
      "symbol": "TOR",
      "address": "0x74E23dF9110Aa9eA0b6ff2fAEE01e740CA1c642e",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/TOR.png",
      "coingeckoId": "tor"
    },
    {
      "name": "Beefy Escrowed Fantom",
      "symbol": "beFTM",
      "address": "0x7381eD41F6dE418DdE5e84B55590422a57917886",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/beFTM.png",
      "coingeckoId": "beefy-escrowed-fantom"
    },
    {
      "name": "DEUS",
      "symbol": "DEUS",
      "address": "0xDE5ed76E7c05eC5e4572CfC88d1ACEA165109E44",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/DEUS.png",
      "coingeckoId": "deus-finance-2"
    },
    {
      "name": "Fantom Libero Financial Freedom",
      "symbol": "FLIBERO",
      "address": "0xC3f069D7439baf6D4D6E9478D9Cc77778E62D147",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/FLIBERO.png",
      "coingeckoId": "fantom-libero-financial"
    },
    {
      "name": "sFTMX",
      "symbol": "sFTMX",
      "address": "0xd7028092c830b5C8FcE061Af2E593413EbbC1fc1",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://assets.spooky.fi/tokens/sFTMX.png",
      "coingeckoId": "stader-sftmx"
    },
    {
      "address": "0xf16e81dce15B08F326220742020379B855B87DF9",
      "chainId": 250,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/token-logos/network/fantom/0xf16e81dce15B08F326220742020379B855B87DF9.jpg",
      "name": "IceToken",
      "symbol": "ICE",
      "coingeckoId": "decentral-games-ice"
    },
    {
      "chainId": 2222,
      "address": "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE",
      "name": "Kava",
      "symbol": "KAVA",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/4846.png",
      "coingeckoId": "kava"
    },
    {
      "chainId": 2222,
      "address": "0xc86c7C0eFbd6A49B35E8714C5f59D99De09A225b",
      "name": "Wrapped Kava",
      "symbol": "WKAVA",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/list/master/logos/native-currency-logos/kava.svg",
      "coingeckoId": "kava"
    },
    {
      "chainId": 2222,
      "address": "0xEB466342C4d449BC9f53A865D5Cb90586f405215",
      "name": "Axelar USDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc"
    },
    {
      "chainId": 2222,
      "address": "0xb829b68f57CC546dA7E5806A929e53bE32a4625D",
      "name": "Axelar ETH",
      "symbol": "axlETH",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth",
      "commonKey": "weth-wei"
    },
    {
      "chainId": 2222,
      "address": "0x5C7e299CF531eb66f2A1dF637d37AbB78e6200C7",
      "name": "Axelar DAI",
      "symbol": "axlDAI",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai",
      "commonKey": "dai-wei"
    },
    {
      "chainId": 2222,
      "address": "0x7f5373AE26c3E8FfC4c77b7255DF7eC1A9aF52a6",
      "name": "Axelar USDT",
      "symbol": "axlUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether",
      "commonKey": "uusdt"
    },
    {
      "chainId": 2222,
      "address": "0x1a35EE4640b0A3B87705B0A4B45D227Ba60Ca2ad",
      "name": "Axelar WBTC",
      "symbol": "axlWBTC",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599/logo.png",
      "coingeckoId": "wrapped-bitcoin",
      "commonKey": "wbtc-satoshi"
    },
    {
      "chainId": 1284,
      "address": "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE",
      "name": "Moonbeam",
      "symbol": "GLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "moonbeam",
      "commonKey": "wglmr-wei"
    },
    {
      "chainId": 1284,
      "address": "0xAcc15dC74880C9944775448304B263D191c6077F",
      "name": "Wrapped GLMR",
      "symbol": "WGLMR",
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/axelarnetwork/axelar-docs/1c761075a4ae672089c2b1cf25739c6368e97bb7/public/images/chains/moonbeam.svg",
      "coingeckoId": "wrapped-moonbeam",
      "commonKey": "wglmr-wei"
    },
    {
      "chainId": 1284,
      "address": "0xCa01a1D0993565291051daFF390892518ACfAD3A",
      "name": "Axelar USD Coin",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc"
    },
    {
      "name": "StellaSwap",
      "address": "0x0E358838ce72d5e61E0018a2ffaC4bEC5F4c88d2",
      "symbol": "STELLA",
      "decimals": 18,
      "chainId": 1284,
      "logoURI": "https://raw.githubusercontent.com/stellaswap/assets/main/tokenlist/0x0E358838ce72d5e61E0018a2ffaC4bEC5F4c88d2/logo.png",
      "coingeckoId": "stellaswap"
    },
    {
      "chainId": 1284,
      "name": "USD Coin | Wormhole",
      "address": "0x931715FEE2d06333043d11F658C8CE934aC61D0c",
      "symbol": "USDC.wh",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/stellaswap/assets/main/tokenlist/0x931715FEE2d06333043d11F658C8CE934aC61D0c/logo.png",
      "coingeckoId": "usd-coin"
    },
    {
      "name": "Wrapped Ether | Wormhole",
      "address": "0xab3f0245B83feB11d15AAffeFD7AD465a59817eD",
      "symbol": "WETH.wh",
      "decimals": 18,
      "chainId": 1284,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth"
    },
    {
      "name": "Wrapped BTC | Wormhole",
      "address": "0xE57eBd2d67B462E9926e04a8e33f01cD0D64346D",
      "symbol": "WBTC.wh",
      "decimals": 8,
      "chainId": 1284,
      "logoURI": "https://raw.githubusercontent.com/stellaswap/assets/main/tokenlist/0xE57eBd2d67B462E9926e04a8e33f01cD0D64346D/logo.png",
      "coingeckoId": "wrapped-bitcoin"
    },
    {
      "name": "Moonwell Artemis",
      "address": "0x511aB53F793683763E5a8829738301368a2411E3",
      "symbol": "WELL",
      "decimals": 18,
      "chainId": 1284,
      "logoURI": "https://raw.githubusercontent.com/stellaswap/assets/main/tokenlist/0x511aB53F793683763E5a8829738301368a2411E3/logo.png",
      "coingeckoId": "bitwell-token"
    },
    {
      "chainId": 1284,
      "name": "Tether USD",
      "address": "0xffffffffea09fb06d082fd1275cd48b191cbcd1d",
      "symbol": "xcUSDT",
      "decimals": 6,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether"
    },
    {
      "chainId": 1284,
      "name": "Frax",
      "address": "0x322e86852e492a7ee17f28a78c663da38fb33bfb",
      "symbol": "Frax",
      "decimals": 18,
      "logoURI": "https://moonscan.io/token/images/fraxfinancemb_32.png",
      "coingeckoId": "frax"
    },
    {
      "name": "Raresama POOP",
      "address": "0xFFfffFFecB45aFD30a637967995394Cc88C0c194",
      "symbol": "POOP",
      "decimals": 18,
      "chainId": 1284,
      "logoURI": "https://raw.githubusercontent.com/stellaswap/assets/main/tokenlist/0xFFfffFFecB45aFD30a637967995394Cc88C0c194/logo.png",
      "coingeckoId": "poochain"
    },
    {
      "name": "xcDOT",
      "address": "0xFfFFfFff1FcaCBd218EDc0EbA20Fc2308C778080",
      "symbol": "xcDOT",
      "decimals": 10,
      "chainId": 1284,
      "logoURI": "https://raw.githubusercontent.com/stellaswap/assets/main/tokenlist/0xFfFFfFff1FcaCBd218EDc0EbA20Fc2308C778080/logo.png",
      "coingeckoId": "xcdot"
    },
    {
      "name": "Acala Dollar",
      "address": "0xfFfFFFFF52C56A9257bB97f4B2b6F7B2D624ecda",
      "symbol": "xcaUSD",
      "decimals": 12,
      "chainId": 1284,
      "logoURI": "https://raw.githubusercontent.com/stellaswap/assets/main/tokenlist/0xfFfFFFFF52C56A9257bB97f4B2b6F7B2D624ecda/logo.png",
      "coingeckoId": "acala-dollar"
    },
    {
      "name": "USD Coin - Nomad",
      "address": "0x8f552a71efe5eefc207bf75485b356a0b3f01ec9",
      "symbol": "USDC | Nomad",
      "decimals": 6,
      "chainId": 1284,
      "logoURI": "https://raw.githubusercontent.com/stellaswap/assets/main/tokenlist/0x8f552a71efe5eefc207bf75485b356a0b3f01ec9/logo.png",
      "coingeckoId": "usd-coin-nomad"
    },
    {
      "chainId": 137,
      "address": "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE",
      "name": "MATIC",
      "symbol": "MATIC",
      "decimals": 18,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/3890.png",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei"
    },
    {
      "chainId": 137,
      "address": "0x0d500B1d8E8eF31E21C99d1Db9A6444d3ADf1270",
      "name": "WMATIC",
      "symbol": "WMATIC",
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/14073/small/matic.png?1628852392",
      "coingeckoId": "matic-network",
      "commonKey": "wmatic-wei"
    },
    {
      "chainId": 137,
      "address": "0x750e4C4984a9e0f12978eA6742Bc1c5D248f40ed",
      "name": "axlUSDC",
      "symbol": "axlUSDC",
      "decimals": 6,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/21420.png",
      "coingeckoId": "axlusdc",
      "commonKey": "uusdc"
    },
    {
      "name": "USD Coin",
      "address": "0x2791Bca1f2de4661ED88A30C99A7a9449Aa84174",
      "symbol": "USDC",
      "decimals": 6,
      "chainId": 137,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48/logo.png",
      "coingeckoId": "usd-coin"
    },
    {
      "name": "Tether USD",
      "address": "0xc2132D05D31c914a87C6611C10748AEb04B58e8F",
      "symbol": "USDT",
      "decimals": 6,
      "chainId": 137,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0xdAC17F958D2ee523a2206206994597C13D831ec7/logo.png",
      "coingeckoId": "tether"
    },
    {
      "name": "Wrapped Ether",
      "address": "0x7ceB23fD6bC0adD59E62ac25578270cFf1b9f619",
      "symbol": "WETH",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
      "coingeckoId": "weth"
    },
    {
      "name": "Dai Stablecoin",
      "address": "0x8f3Cf7ad23Cd3CaDbD9735AFf958023239c6A063",
      "symbol": "DAI",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/assets/0x6B175474E89094C44Da98b954EedeAC495271d0F/logo.png",
      "coingeckoId": "dai"
    },
    {
      "name": "Wrapped BTC (PoS)",
      "address": "0x1BFD67037B42Cf73acF2047067bd4F2C47D9BfD6",
      "symbol": "WBTC",
      "decimals": 8,
      "chainId": 137,
      "logoURI": "https://assets.coingecko.com/coins/images/7598/small/wrapped_bitcoin_wbtc.png?1548822744",
      "coingeckoId": "wrapped-bitcoin"
    },
    {
      "name": "Uniswap (PoS)",
      "address": "0xb33EaAd8d922B1083446DC23f610c2567fB5180f",
      "symbol": "Uniswap (PoS)",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://assets.coingecko.com/coins/images/12504/small/uniswap-uni.png?1600306604",
      "coingeckoId": "uniswap"
    },
    {
      "name": "miMATIC Stablecoin",
      "address": "0xa3Fa99A148fA48D14Ed51d610c367C61876997F1",
      "symbol": "MAI",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://assets.coingecko.com/coins/images/15264/small/mimatic-red.png?1620281018",
      "coingeckoId": "mimatic"
    },
    {
      "name": "decentral.games",
      "address": "0xef938b6da8576a896f6E0321ef80996F4890f9c4",
      "symbol": "DG",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://polygonscan.com/token/images/decentralgame_32.png?v=5",
      "coingeckoId": "decentral-games"
    },
    {
      "name": "PlanetIX",
      "address": "0xE06Bd4F5aAc8D0aA337D13eC88dB6defC6eAEefE",
      "symbol": "IXT",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://assets.coingecko.com/coins/images/20927/small/IXT_SYMBOL_SVG_RGB_BLACK.png?1637934555",
      "coingeckoId": "insurex"
    },
    {
      "name": "Lucidao",
      "address": "0xc2A45FE7d40bCAc8369371B08419DDAFd3131b4a",
      "symbol": "LCD",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://assets.coingecko.com/coins/images/23693/small/lcd-icon-color-200px.png?1645450706",
      "coingeckoId": "lucidao"
    },
    {
      "name": "MASQ",
      "address": "0xEe9A352F6aAc4aF1A5B9f467F6a93E0ffBe9Dd35",
      "symbol": "MASQ",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://assets.coingecko.com/coins/images/13699/small/MASQ_Logo_Blue_Solo_Transparent.png?1616661801",
      "coingeckoId": "masq"
    },
    {
      "name": "Liquid Staking Matic",
      "address": "0xfa68FB4628DFF1028CFEc22b4162FCcd0d45efb6",
      "symbol": "MaticX",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://i.ibb.co/9bHbFsB/2022-04-26-11-53-13.jpg",
      "coingeckoId": "stader-maticx"
    },
    {
      "name": "MCHCoin",
      "address": "0xee7666aACAEFaa6efeeF62ea40176d3eB21953B9",
      "symbol": "MCHC",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://assets.coingecko.com/coins/images/15399/small/MCHC.jpg?1620721307",
      "coingeckoId": "mch-coin"
    },
    {
      "name": "Ocean Token (PoS)",
      "address": "0x282d8efCe846A88B159800bd4130ad77443Fa1A1",
      "symbol": "mOCEAN",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://oceanprotocol.com/static/4ad704a150d436a1f32d495413fc47cd/favicon-white.png",
      "coingeckoId": "ocean-protocol"
    },
    {
      "name": "Nitro (POS)",
      "address": "0x695FC8B80F344411F34bDbCb4E621aA69AdA384b",
      "symbol": "NITRO",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://assets.coingecko.com/coins/images/21668/small/_X6vYBDM_400x400.jpg?1639705848",
      "coingeckoId": "nitro-league"
    },
    {
      "name": "Qi Dao",
      "address": "0x580A84C73811E1839F75d86d75d88cCa0c241fF4",
      "symbol": "QI",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://raw.githubusercontent.com/0xlaozi/qidao/main/images/qi.png",
      "coingeckoId": "benqi"
    },
    {
      "name": "QuickSwap(NEW)",
      "address": "0xB5C064F955D8e7F38fE0460C556a72987494eE17",
      "symbol": "QUICK(NEW)",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://i.ibb.co/HGWTLM7/Quick-Icon-V2.png",
      "coingeckoId": "quickswap"
    },
    {
      "name": "QuickSwap(OLD)",
      "address": "0x831753DD7087CaC61aB5644b308642cc1c33Dc13",
      "symbol": "QUICK(OLD)",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/8206.png",
      "coingeckoId": "quick"
    },
    {
      "name": "SAND",
      "address": "0xBbba073C31bF03b8ACf7c28EF0738DeCF3695683",
      "symbol": "SAND",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://assets.coingecko.com/coins/images/12129/small/sandbox_logo.jpg?1597397942",
      "coingeckoId": "the-sandbox"
    },
    {
      "name": "Toucan Protocol: Base Carbon Tonne",
      "address": "0x2F800Db0fdb5223b3C3f354886d907A671414A7F",
      "symbol": "BCT",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://s2.coinmarketcap.com/static/img/coins/64x64/12949.png",
      "coingeckoId": "toucan-protocol-base-carbon-tonne"
    },
    {
      "name": "Sunflower Land",
      "address": "0xD1f9c58e33933a993A3891F8acFe05a68E1afC05",
      "symbol": "SFL",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://assets.coingecko.com/coins/images/25514/small/download.png?1652164203",
      "coingeckoId": "sunflower-land"
    },
    {
      "name": "Staked MATIC (PoS) | Lido",
      "address": "0x3A58a54C066FdC0f2D55FC9C89F0415C92eBf3C4",
      "symbol": "stMATIC",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://assets.coingecko.com/coins/images/24185/small/stMATIC.png?1646789287",
      "coingeckoId": "lido-staked-matic"
    },
    {
      "name": "Telcoin",
      "address": "0xdF7837DE1F2Fa4631D716CF2502f8b230F1dcc32",
      "symbol": "TEL",
      "decimals": 2,
      "chainId": 137,
      "logoURI": "https://assets.coingecko.com/coins/images/1899/small/tel.png?1547036203",
      "coingeckoId": "telcoin"
    },
    {
      "name": "VOXEL Token",
      "address": "0xd0258a3fD00f38aa8090dfee343f10A9D4d30D3F",
      "symbol": "VOXEL",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://assets.coingecko.com/coins/images/21260/small/voxies.png?1638789903",
      "coingeckoId": "voxies"
    },
    {
      "name": "Exchange Genesis Ethlas Medium",
      "address": "0x02649C1Ff4296038De4b9bA8F491b42b940A8252",
      "symbol": "XGEM",
      "decimals": 18,
      "chainId": 137,
      "logoURI": "https://assets.coingecko.com/coins/images/22535/small/17200.png?1642023748",
      "coingeckoId": "exchange-genesis-ethlas-medium"
    },
    {
      "address": "0x34d4ab47Bee066F361fA52d792e69AC7bD05ee23",
      "chainId": 137,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/polygon/assets/0x34d4ab47Bee066F361fA52d792e69AC7bD05ee23/logo.png",
      "name": "Raider Aurum",
      "symbol": "AURUM",
      "coingeckoId": "raider-aurum"
    },
    {
      "address": "0x385Eeac5cB85A38A9a07A70c73e0a3271CfB54A7",
      "chainId": 137,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/polygon/assets/0x385Eeac5cB85A38A9a07A70c73e0a3271CfB54A7/logo.png",
      "name": "Aavegotchi GHST Token",
      "symbol": "GHST",
      "coingeckoId": "aavegotchi"
    },
    {
      "address": "0xcd7361ac3307D1C5a46b63086a90742Ff44c63B3",
      "chainId": 137,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/sushiswap/assets/master/blockchains/polygon/assets/0xcd7361ac3307D1C5a46b63086a90742Ff44c63B3/logo.png",
      "name": "Crypto Raider",
      "symbol": "RAIDER",
      "coingeckoId": "crypto-raiders"
    },
    {
      "name": "anyBNB",
      "symbol": "BNB",
      "address": "0xA649325Aa7C5093d12D6F98EB4378deAe68CE23F",
      "chainId": 137,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/ApeSwapFinance/apeswap-token-lists/main/assets/WBNB.svg",
      "coingeckoId": "binancecoin"
    },
    {
      "name": "Banana Token",
      "symbol": "BANANA",
      "address": "0x5d47baba0d66083c52009271faf3f50dcc01023c",
      "chainId": 137,
      "decimals": 18,
      "logoURI": "https://raw.githubusercontent.com/ApeSwapFinance/apeswap-token-lists/main/assets/BANANA.svg",
      "coingeckoId": "apeswap-finance"
    },
    {
      "name": "ChainLink",
      "symbol": "LINK",
      "address": "0x53e0bca35ec356bd5dddfebbd1fc0fd03fabad39",
      "chainId": 137,
      "decimals": 18,
      "logoURI": "https://assets.coingecko.com/coins/images/877/small/chainlink-new-logo.png?1547034700",
      "coingeckoId": "chainlink"
    }
  ]
}
        """
}