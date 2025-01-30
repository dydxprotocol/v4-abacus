package exchange.dydx.abacus.tests.payloads

internal class MarketConfigurationsMock {
    internal val configurations = """
   {
       "BTC": {
           "name": "Bitcoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BTC.png",
           "urls": {
               "website": "https://bitcoin.org/",
               "technical_doc": "https://bitcoin.org/bitcoin.pdf",
               "cmc": "https://coinmarketcap.com/currencies/bitcoin"
           },
           "sector_tags": [
               "mineable",
               "pow",
               "sha-256",
               "store-of-value",
               "state-channel",
               "coinbase-ventures-portfolio",
               "three-arrows-capital-portfolio",
               "polychain-capital-portfolio",
               "binance-labs-portfolio",
               "blockchain-capital-portfolio",
               "boostvc-portfolio",
               "cms-holdings-portfolio",
               "dcg-portfolio",
               "dragonfly-capital-portfolio",
               "electric-capital-portfolio",
               "fabric-ventures-portfolio",
               "framework-ventures-portfolio",
               "galaxy-digital-portfolio",
               "huobi-capital-portfolio",
               "alameda-research-portfolio",
               "a16z-portfolio",
               "1confirmation-portfolio",
               "winklevoss-capital-portfolio",
               "usv-portfolio",
               "placeholder-ventures-portfolio",
               "pantera-capital-portfolio",
               "multicoin-capital-portfolio",
               "paradigm-portfolio",
               "bitcoin-ecosystem",
               "ftx-bankruptcy-estate",
               "2017-2018-alt-season"
           ],
           "exchanges": []
       },
       "LTC": {
           "name": "Litecoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LTC.png",
           "urls": {
               "website": "https://litecoin.org/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/litecoin"
           },
           "sector_tags": [
               "mineable",
               "pow",
               "scrypt",
               "medium-of-exchange",
               "heco-ecosystem",
               "2017-2018-alt-season",
               "hoo-smart-chain-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "DGB": {
           "name": "DigiByte",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DGB.png",
           "urls": {
               "website": "https://digibyte.org/",
               "technical_doc": "https://www.digibyte.org/docs/infopaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/digibyte"
           },
           "sector_tags": [
               "mineable",
               "pow",
               "multiple-algorithms",
               "medium-of-exchange",
               "collectibles-nfts",
               "iot",
               "payments",
               "made-in-america"
           ],
           "exchanges": []
       },
       "DASH": {
           "name": "Dash",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DASH.png",
           "urls": {
               "website": "https://www.dash.org/",
               "technical_doc": "https://docs.dash.org/",
               "cmc": "https://coinmarketcap.com/currencies/dash"
           },
           "sector_tags": [
               "mineable",
               "hybrid-pow-pos",
               "x11",
               "medium-of-exchange",
               "privacy",
               "masternodes",
               "dao",
               "governance",
               "alleged-sec-securities",
               "2017-2018-alt-season",
               "made-in-america"
           ],
           "exchanges": []
       },
       "ETH": {
           "name": "Ethereum",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ETH.png",
           "urls": {
               "website": "https://www.ethereum.org/",
               "technical_doc": "https://github.com/ethereum/wiki/wiki/White-Paper",
               "cmc": "https://coinmarketcap.com/currencies/ethereum"
           },
           "sector_tags": [
               "pos",
               "smart-contracts",
               "ethereum-ecosystem",
               "coinbase-ventures-portfolio",
               "three-arrows-capital-portfolio",
               "polychain-capital-portfolio",
               "heco-ecosystem",
               "binance-labs-portfolio",
               "solana-ecosystem",
               "blockchain-capital-portfolio",
               "boostvc-portfolio",
               "cms-holdings-portfolio",
               "dcg-portfolio",
               "dragonfly-capital-portfolio",
               "electric-capital-portfolio",
               "fabric-ventures-portfolio",
               "framework-ventures-portfolio",
               "hashkey-capital-portfolio",
               "kenetic-capital-portfolio",
               "huobi-capital-portfolio",
               "alameda-research-portfolio",
               "a16z-portfolio",
               "1confirmation-portfolio",
               "winklevoss-capital-portfolio",
               "usv-portfolio",
               "placeholder-ventures-portfolio",
               "pantera-capital-portfolio",
               "multicoin-capital-portfolio",
               "paradigm-portfolio",
               "tezos-ecosystem",
               "near-protocol-ecosystem",
               "velas-ecosystem",
               "ethereum-pow-ecosystem",
               "osmosis-ecosystem",
               "layer-1",
               "ftx-bankruptcy-estate",
               "zksync-era-ecosystem",
               "viction-ecosystem",
               "klaytn-ecosystem",
               "sora-ecosystem",
               "rsk-rbtc-ecosystem",
               "starknet-ecosystem"
           ],
           "exchanges": []
       },
       "SC": {
           "name": "Siacoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SC.png",
           "urls": {
               "website": "https://sia.tech/",
               "technical_doc": "https://sia.tech/sia.pdf",
               "cmc": "https://coinmarketcap.com/currencies/siacoin"
           },
           "sector_tags": [
               "mineable",
               "pow",
               "blake2b",
               "platform",
               "distributed-computing",
               "filesharing",
               "storage",
               "dragonfly-capital-portfolio",
               "fenbushi-capital-portfolio",
               "paradigm-portfolio",
               "web3",
               "near-protocol-ecosystem",
               "depin"
           ],
           "exchanges": []
       },
       "LSK": {
           "name": "Lisk",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LSK.png",
           "urls": {
               "website": "https://Lisk.com/",
               "technical_doc": "https://documentation.lisk.com",
               "cmc": "https://coinmarketcap.com/currencies/lisk"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "layer-2",
               "real-world-assets",
               "depin"
           ],
           "exchanges": []
       },
       "STEEM": {
           "name": "Steem",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/STEEM.png",
           "urls": {
               "website": "https://steem.com/",
               "technical_doc": "https://steem.io/SteemWhitePaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/steem"
           },
           "sector_tags": [
               "media",
               "content-creation",
               "web3"
           ],
           "exchanges": []
       },
       "WAVES": {
           "name": "Waves",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/WAVES.png",
           "urls": {
               "website": "https://waves.tech/",
               "technical_doc": "https://docs.waves.tech/en/",
               "cmc": "https://coinmarketcap.com/currencies/waves"
           },
           "sector_tags": [
               "lpos",
               "platform",
               "smart-contracts",
               "ethereum-ecosystem",
               "waves-ecosystem",
               "solana-ecosystem",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "ETC": {
           "name": "Ethereum Classic",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ETC.png",
           "urls": {
               "website": "https://ethereumclassic.org/",
               "technical_doc": "https://ethereumclassic.org/knowledge/foundation",
               "cmc": "https://coinmarketcap.com/currencies/ethereum-classic"
           },
           "sector_tags": [
               "mineable",
               "pow",
               "ethash",
               "platform",
               "smart-contracts",
               "dcg-portfolio",
               "2017-2018-alt-season",
               "ethereum-classic-ecosystem"
           ],
           "exchanges": []
       },
       "NEO": {
           "name": "Neo",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/NEO.png",
           "urls": {
               "website": "https://neo.org/",
               "technical_doc": "https://docs.neo.org/docs/en-us/index.html",
               "cmc": "https://coinmarketcap.com/currencies/neo"
           },
           "sector_tags": [
               "platform",
               "enterprise-solutions",
               "smart-contracts",
               "2017-2018-alt-season"
           ],
           "exchanges": []
       },
       "ZEC": {
           "name": "Zcash",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ZEC.png",
           "urls": {
               "website": "https://z.cash/",
               "technical_doc": "https://github.com/zcash/zips/blob/master/protocol/protocol.pdf",
               "cmc": "https://coinmarketcap.com/currencies/zcash"
           },
           "sector_tags": [
               "mineable",
               "pow",
               "equihash",
               "medium-of-exchange",
               "privacy",
               "zero-knowledge-proofs",
               "binance-chain",
               "boostvc-portfolio",
               "dcg-portfolio",
               "electric-capital-portfolio",
               "fenbushi-capital-portfolio",
               "hashkey-capital-portfolio",
               "winklevoss-capital-portfolio",
               "placeholder-ventures-portfolio",
               "pantera-capital-portfolio",
               "bnb-chain-ecosystem",
               "standard-crypto-portfolio",
               "2017-2018-alt-season",
               "made-in-america"
           ],
           "exchanges": []
       },
       "GLM": {
           "name": "Golem",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/GLM.png",
           "urls": {
               "website": "https://golem.network/",
               "technical_doc": "https://golem.network/doc/Golemwhitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/golem-network-tokens"
           },
           "sector_tags": [
               "platform",
               "ai-big-data",
               "distributed-computing",
               "payments",
               "ethereum-ecosystem",
               "polygon-ecosystem",
               "web3",
               "depin"
           ],
           "exchanges": []
       },
       "MKR": {
           "name": "Maker",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MKR.png",
           "urls": {
               "website": "https://makerdao.com/",
               "technical_doc": "https://makerdao.com/en/whitepaper/#overview-of-the-dai-stablecoin-system",
               "cmc": "https://coinmarketcap.com/currencies/maker"
           },
           "sector_tags": [
               "store-of-value",
               "defi",
               "dao",
               "ethereum-ecosystem",
               "polychain-capital-portfolio",
               "governance",
               "lending-borowing",
               "dragonfly-capital-portfolio",
               "electric-capital-portfolio",
               "a16z-portfolio",
               "1confirmation-portfolio",
               "placeholder-ventures-portfolio",
               "pantera-capital-portfolio",
               "paradigm-portfolio",
               "near-protocol-ecosystem",
               "arbitrum-ecosystem",
               "spartan-group",
               "bnb-chain-ecosystem",
               "real-world-assets",
               "standard-crypto-portfolio",
               "made-in-america"
           ],
           "exchanges": []
       },
       "XNO": {
           "name": "Nano",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/XNO.png",
           "urls": {
               "website": "http://nano.org/en",
               "technical_doc": "https://nano.org/en/whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/nano"
           },
           "sector_tags": [
               "dag",
               "blake2b",
               "medium-of-exchange",
               "store-of-value",
               "payments",
               "made-in-america"
           ],
           "exchanges": []
       },
       "ARK": {
           "name": "Ark",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ARK.png",
           "urls": {
               "website": "http://ark.io/",
               "technical_doc": "https://arkscic.com/Whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/ark"
           },
           "sector_tags": [
               "dpos",
               "marketplace",
               "payments",
               "state-channel",
               "polygon-ecosystem"
           ],
           "exchanges": []
       },
       "RLC": {
           "name": "iExec RLC",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/RLC.png",
           "urls": {
               "website": "https://iex.ec/",
               "technical_doc": "https://iex.ec/app/uploads/2017/04/iExec-WPv2.0-English.pdf",
               "cmc": "https://coinmarketcap.com/currencies/rlc"
           },
           "sector_tags": [
               "marketing",
               "art",
               "marketplace",
               "platform",
               "services",
               "ai-big-data",
               "enterprise-solutions",
               "distributed-computing",
               "collectibles-nfts",
               "content-creation",
               "defi",
               "privacy",
               "filesharing",
               "interoperability",
               "oracles",
               "payments",
               "research",
               "scaling",
               "ethereum-ecosystem",
               "substrate",
               "polkadot",
               "storage",
               "polygon-ecosystem",
               "web3",
               "token",
               "generative-ai",
               "depin"
           ],
           "exchanges": []
       },
       "GNO": {
           "name": "Gnosis",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/GNO.png",
           "urls": {
               "website": "https://gnosis.io/",
               "technical_doc": "https://github.com/gnosis/research/blob/master/gnosis-whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/gnosis-gno"
           },
           "sector_tags": [
               "services",
               "decentralized-exchange-dex-token",
               "defi",
               "prediction-markets",
               "ethereum-ecosystem",
               "kenetic-capital-portfolio",
               "arbitrum-ecosystem",
               "layer-1",
               "gnosis-chain-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "QTUM": {
           "name": "Qtum",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/QTUM.png",
           "urls": {
               "website": "https://qtum.org/",
               "technical_doc": "https://qtumorg.s3.ap-northeast-2.amazonaws.com/Qtum_New_Whitepaper_en.pdf",
               "cmc": "https://coinmarketcap.com/currencies/qtum"
           },
           "sector_tags": [
               "platform",
               "smart-contracts",
               "kenetic-capital-portfolio",
               "bitcoin-ecosystem"
           ],
           "exchanges": []
       },
       "BAT": {
           "name": "Basic Attention Token",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BAT.png",
           "urls": {
               "website": "https://basicattentiontoken.org/",
               "technical_doc": "https://basicattentiontoken.org/BasicAttentionTokenWhitePaper-4.pdf",
               "cmc": "https://coinmarketcap.com/currencies/basic-attention-token"
           },
           "sector_tags": [
               "marketing",
               "ai-big-data",
               "content-creation",
               "defi",
               "interoperability",
               "payments",
               "smart-contracts",
               "staking",
               "ethereum-ecosystem",
               "governance",
               "solana-ecosystem",
               "dcg-portfolio",
               "1confirmation-portfolio",
               "pantera-capital-portfolio",
               "web3",
               "near-protocol-ecosystem",
               "bnb-chain-ecosystem",
               "gnosis-chain-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "ZEN": {
           "name": "Horizen",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ZEN.png",
           "urls": {
               "website": "https://www.horizen.io/",
               "technical_doc": "https://www.horizen.io/research/",
               "cmc": "https://coinmarketcap.com/currencies/horizen"
           },
           "sector_tags": [
               "mineable",
               "pow",
               "medium-of-exchange",
               "platform",
               "enterprise-solutions",
               "content-creation",
               "zero-knowledge-proofs",
               "masternodes",
               "scaling",
               "smart-contracts",
               "staking",
               "sidechain",
               "dcg-portfolio",
               "web3",
               "made-in-america"
           ],
           "exchanges": []
       },
       "IOTA": {
           "name": "IOTA",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/IOTA.png",
           "urls": {
               "website": "https://www.iota.org/",
               "technical_doc": "https://iota.org/IOTA_Whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/iota"
           },
           "sector_tags": [
               "dag",
               "medium-of-exchange",
               "iot",
               "sharing-economy",
               "real-world-assets",
               "dwf-labs-portfolio",
               "depin",
               "2017-2018-alt-season"
           ],
           "exchanges": []
       },
       "NMR": {
           "name": "Numeraire",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/NMR.png",
           "urls": {
               "website": "https://numer.ai/",
               "technical_doc": "https://numer.ai/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/numeraire"
           },
           "sector_tags": [
               "asset-management",
               "ai-big-data",
               "defi",
               "payments",
               "research",
               "ethereum-ecosystem",
               "coinfund-portfolio",
               "usv-portfolio",
               "placeholder-ventures-portfolio",
               "paradigm-portfolio",
               "generative-ai"
           ],
           "exchanges": []
       },
       "SNT": {
           "name": "Status",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SNT.png",
           "urls": {
               "website": "http://status.im/",
               "technical_doc": "https://status.im/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/status"
           },
           "sector_tags": [
               "media",
               "content-creation",
               "ethereum-ecosystem",
               "fabric-ventures-portfolio",
               "kenetic-capital-portfolio"
           ],
           "exchanges": []
       },
       "EOS": {
           "name": "EOS",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/EOS.png",
           "urls": {
               "website": "https://eosnetwork.com/",
               "technical_doc": "https://eosnetwork.com/blog/category/eos-blue-papers/",
               "cmc": "https://coinmarketcap.com/currencies/eos"
           },
           "sector_tags": [
               "medium-of-exchange",
               "enterprise-solutions",
               "smart-contracts",
               "eos-ecosystem",
               "heco-ecosystem",
               "fenbushi-capital-portfolio",
               "galaxy-digital-portfolio",
               "dwf-labs-portfolio",
               "2017-2018-alt-season",
               "made-in-america"
           ],
           "exchanges": []
       },
       "ADX": {
           "name": "AdEx",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ADX.png",
           "urls": {
               "website": "https://www.adex.network/",
               "technical_doc": "https://github.com/AmbireTech/adex-protocol",
               "cmc": "https://coinmarketcap.com/currencies/adx-net"
           },
           "sector_tags": [
               "marketing",
               "smart-contracts",
               "ethereum-ecosystem",
               "polygon-ecosystem",
               "web3",
               "bnb-chain-ecosystem",
               "generative-ai",
               "account-abstraction"
           ],
           "exchanges": []
       },
       "STORJ": {
           "name": "Storj",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/STORJ.png",
           "urls": {
               "website": "https://storj.io/",
               "technical_doc": "https://storj.io/storj.pdf",
               "cmc": "https://coinmarketcap.com/currencies/storj"
           },
           "sector_tags": [
               "platform",
               "distributed-computing",
               "filesharing",
               "ethereum-ecosystem",
               "storage",
               "web3",
               "depin"
           ],
           "exchanges": []
       },
       "GAS": {
           "name": "Gas",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/GAS.png",
           "urls": {
               "website": "https://neo.org/",
               "technical_doc": "http://docs.neo.org/en-us/",
               "cmc": "https://coinmarketcap.com/currencies/gas"
           },
           "sector_tags": [
               "medium-of-exchange",
               "payments",
               "smart-contracts",
               "neo-ecosystem"
           ],
           "exchanges": []
       },
       "OMG": {
           "name": "OMG Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/OMG.png",
           "urls": {
               "website": "https://omg.network/",
               "technical_doc": "https://docs.omg.network/",
               "cmc": "https://coinmarketcap.com/currencies/omg"
           },
           "sector_tags": [
               "medium-of-exchange",
               "payments",
               "scaling",
               "state-channel",
               "ethereum-ecosystem",
               "pantera-capital-portfolio",
               "alleged-sec-securities"
           ],
           "exchanges": []
       },
       "CVC": {
           "name": "Civic",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CVC.png",
           "urls": {
               "website": "https://www.civic.com/",
               "technical_doc": "https://www.allcryptowhitepapers.com/civic-whitepaper/",
               "cmc": "https://coinmarketcap.com/currencies/civic"
           },
           "sector_tags": [
               "enterprise-solutions",
               "identity",
               "ethereum-ecosystem",
               "solana-ecosystem",
               "dcg-portfolio",
               "pantera-capital-portfolio",
               "web3"
           ],
           "exchanges": []
       },
       "BCH": {
           "name": "Bitcoin Cash",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BCH.png",
           "urls": {
               "website": "http://bch.info",
               "technical_doc": "https://bch.info/bitcoin.pdf",
               "cmc": "https://coinmarketcap.com/currencies/bitcoin-cash"
           },
           "sector_tags": [
               "mineable",
               "pow",
               "sha-256",
               "marketplace",
               "medium-of-exchange",
               "store-of-value",
               "enterprise-solutions",
               "payments",
               "heco-ecosystem",
               "bitcoin-ecosystem",
               "layer-1",
               "2017-2018-alt-season",
               "made-in-america"
           ],
           "exchanges": []
       },
       "BNB": {
           "name": "BNB",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BNB.png",
           "urls": {
               "website": "https://bnbchain.org/en",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/bnb"
           },
           "sector_tags": [
               "marketplace",
               "centralized-exchange",
               "payments",
               "smart-contracts",
               "ethereum-ecosystem",
               "alameda-research-portfolio",
               "multicoin-capital-portfolio",
               "bnb-chain-ecosystem",
               "layer-1",
               "alleged-sec-securities",
               "celsius-bankruptcy-estate"
           ],
           "exchanges": []
       },
       "ZRX": {
           "name": "0x Protocol",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ZRX.png",
           "urls": {
               "website": "http://0xprotocol.org/",
               "technical_doc": "https://0xproject.com/pdfs/0x_white_paper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/0x"
           },
           "sector_tags": [
               "platform",
               "decentralized-exchange-dex-token",
               "defi",
               "scaling",
               "dao",
               "ethereum-ecosystem",
               "substrate",
               "polkadot",
               "polychain-capital-portfolio",
               "governance",
               "blockchain-capital-portfolio",
               "boostvc-portfolio",
               "fabric-ventures-portfolio",
               "kenetic-capital-portfolio",
               "placeholder-ventures-portfolio",
               "pantera-capital-portfolio",
               "celsius-bankruptcy-estate",
               "made-in-america",
               "defi"
           ],
           "exchanges": []
       },
       "LRC": {
           "name": "Loopring",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LRC.png",
           "urls": {
               "website": "https://loopring.org",
               "technical_doc": "https://github.com/Loopring/protocols/blob/master/packages/loopring_v3/DESIGN.md",
               "cmc": "https://coinmarketcap.com/currencies/loopring"
           },
           "sector_tags": [
               "marketplace",
               "decentralized-exchange-dex-token",
               "defi",
               "zero-knowledge-proofs",
               "scaling",
               "smart-contracts",
               "wallet",
               "ethereum-ecosystem",
               "amm",
               "dex",
               "layer-2",
               "rollups",
               "solana-ecosystem",
               "red-packets",
               "web3",
               "token",
               "arbitrum-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "TRX": {
           "name": "TRON",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TRX.png",
           "urls": {
               "website": "https://trondao.org/",
               "technical_doc": "https://developers.tron.network/docs",
               "cmc": "https://coinmarketcap.com/currencies/tron"
           },
           "sector_tags": [
               "media",
               "payments",
               "ethereum-ecosystem",
               "tron-ecosystem",
               "layer-1",
               "dwf-labs-portfolio",
               "alleged-sec-securities",
               "2017-2018-alt-season",
               "tron20-ecosystem"
           ],
           "exchanges": []
       },
       "MANA": {
           "name": "Decentraland",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MANA.png",
           "urls": {
               "website": "https://decentraland.org/",
               "technical_doc": "https://decentraland.org/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/decentraland"
           },
           "sector_tags": [
               "platform",
               "collectibles-nfts",
               "gaming",
               "payments",
               "ethereum-ecosystem",
               "solana-ecosystem",
               "metaverse",
               "boostvc-portfolio",
               "dcg-portfolio",
               "fabric-ventures-portfolio",
               "kenetic-capital-portfolio",
               "polygon-ecosystem",
               "play-to-earn",
               "alleged-sec-securities",
               "gnosis-chain-ecosystem",
               "fusion-network-ecosystem"
           ],
           "exchanges": []
       },
       "LINK": {
           "name": "Chainlink",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LINK.png",
           "urls": {
               "website": "https://chain.link/",
               "technical_doc": "https://chain.link/whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/chainlink"
           },
           "sector_tags": [
               "platform",
               "defi",
               "oracles",
               "smart-contracts",
               "ethereum-ecosystem",
               "substrate",
               "polkadot",
               "heco-ecosystem",
               "avalanche-ecosystem",
               "solana-ecosystem",
               "framework-ventures-portfolio",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "cardano-ecosystem",
               "web3",
               "near-protocol-ecosystem",
               "arbitrum-ecosystem",
               "cardano",
               "injective-ecosystem",
               "optimism-ecosystem",
               "real-world-assets",
               "celsius-bankruptcy-estate",
               "gnosis-chain-ecosystem",
               "sora-ecosystem",
               "hoo-smart-chain-ecosystem",
               "milkomeda-ecosystem",
               "energi-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "ADA": {
           "name": "Cardano",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ADA.png",
           "urls": {
               "website": "https://www.cardano.org",
               "technical_doc": "https://docs.cardano.org/en/latest/",
               "cmc": "https://coinmarketcap.com/currencies/cardano"
           },
           "sector_tags": [
               "dpos",
               "pos",
               "platform",
               "research",
               "smart-contracts",
               "staking",
               "cardano-ecosystem",
               "cardano",
               "layer-1",
               "alleged-sec-securities",
               "2017-2018-alt-season",
               "made-in-america"
           ],
           "exchanges": []
       },
       "XTZ": {
           "name": "Tezos",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/XTZ.png",
           "urls": {
               "website": "https://www.tezos.com/",
               "technical_doc": "https://tezos.com/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/tezos"
           },
           "sector_tags": [
               "pos",
               "platform",
               "enterprise-solutions",
               "collectibles-nfts",
               "defi",
               "smart-contracts",
               "polychain-capital-portfolio",
               "boostvc-portfolio",
               "winklevoss-capital-portfolio",
               "tezos-ecosystem",
               "layer-1",
               "2017-2018-alt-season",
               "made-in-america"
           ],
           "exchanges": []
       },
       "REQ": {
           "name": "Request",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/REQ.png",
           "urls": {
               "website": "https://request.network/",
               "technical_doc": "https://docs.request.network/",
               "cmc": "https://coinmarketcap.com/currencies/request"
           },
           "sector_tags": [
               "medium-of-exchange",
               "defi",
               "payments",
               "smart-contracts",
               "ethereum-ecosystem",
               "polygon-ecosystem"
           ],
           "exchanges": []
       },
       "ATA": {
           "name": "Automata Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ATA.png",
           "urls": {
               "website": "https://ata.network",
               "technical_doc": "https://docs.ata.network/research/lightpaper",
               "cmc": "https://coinmarketcap.com/currencies/automata-network"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "polkadot-ecosystem",
               "binance-launchpool",
               "rollups",
               "polygon-ecosystem",
               "jump-crypto",
               "eigenlayer-ecosystem"
           ],
           "exchanges": []
       },
       "BABYDOGE": {
           "name": "Baby Doge Coin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BABYDOGE.png",
           "urls": {
               "website": "https://www.babydoge.com",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/baby-doge-coin"
           },
           "sector_tags": [
               "marketing",
               "art",
               "marketplace",
               "media",
               "philanthropy",
               "services",
               "collectibles-nfts",
               "loyalty",
               "content-creation",
               "decentralized-exchange-dex-token",
               "defi",
               "gaming",
               "entertainment",
               "memes",
               "payments",
               "ethereum-ecosystem",
               "binance-smart-chain",
               "launchpad",
               "solana-ecosystem",
               "doggone-doggerel",
               "web3",
               "cross-chain",
               "bnb-chain-ecosystem",
               "generative-ai",
               "tap-to-earn",
               "defi"
           ],
           "exchanges": []
       },
       "SUN": {
           "name": "Sun [New]",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SUN.png",
           "urls": {
               "website": "https://sun.io/",
               "technical_doc": "https://sun.io/docs/SUN_V2_Whitepaper_en.pdf",
               "cmc": "https://coinmarketcap.com/currencies/sun-token"
           },
           "sector_tags": [
               "tron-ecosystem",
               "tron20-ecosystem"
           ],
           "exchanges": []
       },
       "IMX": {
           "name": "Immutable",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/IMX.png",
           "urls": {
               "website": "https://www.immutable.com/",
               "technical_doc": "https://support.immutable.com/hc/en-us/articles/4405227590799",
               "cmc": "https://coinmarketcap.com/currencies/immutable-x"
           },
           "sector_tags": [
               "collectibles-nfts",
               "zero-knowledge-proofs",
               "gaming",
               "scaling",
               "ethereum-ecosystem",
               "layer-2",
               "rollups",
               "arrington-xrp-capital-portfolio",
               "alameda-research-portfolio"
           ],
           "exchanges": []
       },
       "GODS": {
           "name": "Gods Unchained",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/GODS.png",
           "urls": {
               "website": "https://godsunchained.com/",
               "technical_doc": "https://images.godsunchained.com/misc/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/gods-unchained"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "entertainment",
               "ethereum-ecosystem",
               "metaverse",
               "play-to-earn",
               "okx-ventures-portfolio"
           ],
           "exchanges": []
       },
       "YGG": {
           "name": "Yield Guild Games",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/YGG.png",
           "urls": {
               "website": "https://yieldguild.io/",
               "technical_doc": "https://storage.googleapis.com/external_communication/YGG-GuildProtocol-ConceptPaper-2024Sept.pdf",
               "cmc": "https://coinmarketcap.com/currencies/yield-guild-games"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "entertainment",
               "dao",
               "ethereum-ecosystem",
               "solana-ecosystem",
               "metaverse",
               "a16z-portfolio",
               "polygon-ecosystem",
               "play-to-earn",
               "animoca-brands-portfolio",
               "gaming-guild",
               "harmony-ecosystem",
               "okx-ventures-portfolio",
               "dwf-labs-portfolio",
               "base-ecosystem",
               "ronin-ecosystem"
           ],
           "exchanges": []
       },
       "FLOKI": {
           "name": "FLOKI",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FLOKI.png",
           "urls": {
               "website": "https://floki.com/",
               "technical_doc": "https://docs.floki.com/",
               "cmc": "https://coinmarketcap.com/currencies/floki-inu"
           },
           "sector_tags": [
               "gaming",
               "memes",
               "ethereum-ecosystem",
               "metaverse",
               "doggone-doggerel",
               "play-to-earn",
               "bnb-chain-ecosystem",
               "dwf-labs-portfolio",
               "paal-ecosystem",
               "tokenfi-launchpad"
           ],
           "exchanges": []
       },
       "C98": {
           "name": "Coin98",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/C98.png",
           "urls": {
               "website": "https://www.coin98.com/",
               "technical_doc": "https://docs.coin98.com/",
               "cmc": "https://coinmarketcap.com/currencies/coin98"
           },
           "sector_tags": [
               "wallet",
               "ethereum-ecosystem",
               "binance-launchpad",
               "solana-ecosystem",
               "polygon-ecosystem",
               "celo-ecosystem",
               "spartan-group",
               "injective-ecosystem",
               "bnb-chain-ecosystem",
               "dwf-labs-portfolio",
               "viction-ecosystem"
           ],
           "exchanges": []
       },
       "ORCA": {
           "name": "Orca",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ORCA.png",
           "urls": {
               "website": "https://www.orca.so/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/orca"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "amm",
               "dex",
               "three-arrows-capital-portfolio",
               "solana-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "HIGH": {
           "name": "Highstreet",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/HIGH.png",
           "urls": {
               "website": "https://highstreet.market",
               "technical_doc": "https://highstreet.gitbook.io/highstreet-whitepaper/",
               "cmc": "https://coinmarketcap.com/currencies/highstreet"
           },
           "sector_tags": [
               "vr-ar",
               "ethereum-ecosystem",
               "binance-launchpool",
               "solana-ecosystem",
               "metaverse",
               "bullperks-launchpad",
               "animoca-brands-portfolio",
               "skyvision-capital-portfolio",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "SPELL": {
           "name": "Spell Token",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SPELL.png",
           "urls": {
               "website": "https://abracadabra.money/",
               "technical_doc": "https://docs.abracadabra.money/",
               "cmc": "https://coinmarketcap.com/currencies/spell-token"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "solana-ecosystem",
               "fantom-ecosystem",
               "olympus-pro-ecosystem",
               "arbitrum-ecosystem",
               "defi-2",
               "protocol-owned-liquidity",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "RARE": {
           "name": "SuperRare",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/RARE.png",
           "urls": {
               "website": "https://superrare.com/",
               "technical_doc": "https://docs.superrare.com/",
               "cmc": "https://coinmarketcap.com/currencies/superrare"
           },
           "sector_tags": [
               "marketplace",
               "collectibles-nfts",
               "dao",
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "RACA": {
           "name": "RACA",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/RACA.png",
           "urls": {
               "website": "https://www.raca3.com/",
               "technical_doc": "https://www.raca3.com/whitePaper",
               "cmc": "https://coinmarketcap.com/currencies/radio-caca"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "ethereum-ecosystem",
               "metaverse",
               "play-to-earn",
               "mvb",
               "bnb-chain-ecosystem",
               "dwf-labs-portfolio",
               "okexchain-ecosystem"
           ],
           "exchanges": []
       },
       "JOE": {
           "name": "JOE",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/JOE.png",
           "urls": {
               "website": "https://www.lfj.gg/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/joe"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "defi",
               "staking",
               "yield-farming",
               "amm",
               "dex",
               "lp-tokens",
               "avalanche-ecosystem",
               "arbitrum-ecosystem",
               "mantle-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "TON": {
           "name": "Toncoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TON.png",
           "urls": {
               "website": "https://ton.org/",
               "technical_doc": "https://ton.org/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/toncoin"
           },
           "sector_tags": [
               "pos",
               "ethereum-ecosystem",
               "layer-1",
               "ftx-bankruptcy-estate",
               "dwf-labs-portfolio",
               "toncoin-ecosystem",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "AGLD": {
           "name": "Adventure Gold",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AGLD.png",
           "urls": {
               "website": "https://adventurelayer.xyz",
               "technical_doc": "https://whitepaper.adventurelayer.xyz",
               "cmc": "https://coinmarketcap.com/currencies/adventure-gold"
           },
           "sector_tags": [
               "gaming",
               "ethereum-ecosystem",
               "solana-ecosystem",
               "dwf-labs-portfolio",
               "made-in-america"
           ],
           "exchanges": []
       },
       "OP": {
           "name": "Optimism",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/OP.png",
           "urls": {
               "website": "https://www.optimism.io/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/optimism-ethereum"
           },
           "sector_tags": [
               "layer-2",
               "optimism-ecosystem",
               "modular-blockchain",
               "made-in-america"
           ],
           "exchanges": []
       },
       "ARB": {
           "name": "Arbitrum",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ARB.png",
           "urls": {
               "website": "https://arbitrum.foundation",
               "technical_doc": "https://github.com/OffchainLabs",
               "cmc": "https://coinmarketcap.com/currencies/arbitrum"
           },
           "sector_tags": [
               "scaling",
               "dao",
               "dapp",
               "ethereum-ecosystem",
               "polychain-capital-portfolio",
               "layer-2",
               "rollups",
               "pantera-capital-portfolio",
               "arbitrum-ecosystem",
               "osmosis-ecosystem",
               "modular-blockchain",
               "egirl-capital-portfolio"
           ],
           "exchanges": []
       },
       "GMX": {
           "name": "GMX",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/GMX.png",
           "urls": {
               "website": "https://gmx.io/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/gmx"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "derivatives",
               "dex",
               "avalanche-ecosystem",
               "arbitrum-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "XRD": {
           "name": "Radix",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/XRD.png",
           "urls": {
               "website": "https://www.radixdlt.com/",
               "technical_doc": "https://www.radixdlt.com/whitepapers/defi",
               "cmc": "https://coinmarketcap.com/currencies/radix-protocol"
           },
           "sector_tags": [
               "platform",
               "layer-1",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "AZERO": {
           "name": "Aleph Zero",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AZERO.png",
           "urls": {
               "website": "https://www.alephzero.org",
               "technical_doc": "https://docs.alephzero.org/",
               "cmc": "https://coinmarketcap.com/currencies/aleph-zero"
           },
           "sector_tags": [
               "platform",
               "privacy",
               "zero-knowledge-proofs",
               "smart-contracts",
               "staking",
               "substrate",
               "polkadot",
               "polkadot-ecosystem",
               "exnetwork-capital-portfolio",
               "petrock-capital-portfolio",
               "layer-1"
           ],
           "exchanges": []
       },
       "SYN": {
           "name": "Synapse",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SYN.png",
           "urls": {
               "website": "https://synapseprotocol.com",
               "technical_doc": "https://docs.synapseprotocol.com/",
               "cmc": "https://coinmarketcap.com/currencies/synapse-2"
           },
           "sector_tags": [
               "interoperability",
               "ethereum-ecosystem",
               "three-arrows-capital-portfolio",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "olympus-pro-ecosystem",
               "arbitrum-ecosystem",
               "harmony-ecosystem",
               "moonriver-ecosystem",
               "optimism-ecosystem",
               "boba-network-ecosystem",
               "aurora-ecosystem"
           ],
           "exchanges": []
       },
       "SOLV": {
           "name": "Solv Protocol",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SOLV.png",
           "urls": {
               "website": "https://Solv.finance/?chl=CMC",
               "technical_doc": "https://docs.solv.finance/solv-documentation/?chl=CMC",
               "cmc": "https://coinmarketcap.com/currencies/solv-protocol"
           },
           "sector_tags": [
               "spartan-group",
               "btcfi"
           ],
           "exchanges": []
       },
       "OSMO": {
           "name": "Osmosis",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/OSMO.png",
           "urls": {
               "website": "https://osmosis.zone/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/osmosis"
           },
           "sector_tags": [
               "cosmos-ecosystem",
               "ethereum-ecosystem",
               "polygon-ecosystem",
               "injective-ecosystem",
               "osmosis-ecosystem",
               "modular-blockchain"
           ],
           "exchanges": []
       },
       "CPOOL": {
           "name": "Clearpool",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CPOOL.png",
           "urls": {
               "website": "https://clearpool.finance/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/clearpool"
           },
           "sector_tags": [
               "defi",
               "ethereum-ecosystem",
               "real-world-assets",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "SD": {
           "name": "Stader",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SD.png",
           "urls": {
               "website": "https://staderlabs.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/stader"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "near-protocol-ecosystem",
               "liquid-staking-derivatives",
               "binance-alpha",
               "aurora-ecosystem"
           ],
           "exchanges": []
       },
       "ASTR": {
           "name": "Astar",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ASTR.png",
           "urls": {
               "website": "https://astar.network/",
               "technical_doc": "https://docs.astar.network/",
               "cmc": "https://coinmarketcap.com/currencies/astar"
           },
           "sector_tags": [
               "polkadot",
               "polkadot-ecosystem",
               "okx-ventures-portfolio",
               "layer-1"
           ],
           "exchanges": []
       },
       "SSV": {
           "name": "ssv.network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SSV.png",
           "urls": {
               "website": "https://ssv.network/",
               "technical_doc": "https://docs.ssv.network/",
               "cmc": "https://coinmarketcap.com/currencies/ssv-network"
           },
           "sector_tags": [
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "WLD": {
           "name": "Worldcoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/WLD.png",
           "urls": {
               "website": "https://worldcoin.org/",
               "technical_doc": "https://whitepaper.worldcoin.org/",
               "cmc": "https://coinmarketcap.com/currencies/worldcoin-org"
           },
           "sector_tags": [
               "privacy",
               "zero-knowledge-proofs",
               "identity",
               "ethereum-ecosystem",
               "governance",
               "blockchain-capital-portfolio",
               "a16z-portfolio",
               "token",
               "optimism-ecosystem",
               "world-chain-mainnet-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "MANTA": {
           "name": "Manta Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MANTA.png",
           "urls": {
               "website": "https://manta.network/",
               "technical_doc": "https://mantanetwork.medium.com/the-manta-pacific-roadmap-d09a9d918553",
               "cmc": "https://coinmarketcap.com/currencies/manta-network"
           },
           "sector_tags": [
               "zero-knowledge-proofs",
               "ethereum-ecosystem",
               "polkadot-ecosystem",
               "binance-launchpool",
               "polychain-capital-portfolio",
               "layer-2",
               "binance-labs-portfolio",
               "web3",
               "skyvision-capital-portfolio",
               "spartan-group",
               "modular-blockchain",
               "manta-pacific-ecosystem"
           ],
           "exchanges": []
       },
       "MNDE": {
           "name": "Marinade",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MNDE.png",
           "urls": {
               "website": "https://marinade.finance/",
               "technical_doc": "https://docs.marinade.finance/",
               "cmc": "https://coinmarketcap.com/currencies/mnde"
           },
           "sector_tags": [
               "collectibles-nfts",
               "dao",
               "solana-ecosystem"
           ],
           "exchanges": []
       },
       "ENS": {
           "name": "Ethereum Name Service",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ENS.png",
           "urls": {
               "website": "https://ens.domains/",
               "technical_doc": "https://docs.ens.domains/",
               "cmc": "https://coinmarketcap.com/currencies/ethereum-name-service"
           },
           "sector_tags": [
               "dao",
               "ethereum-ecosystem",
               "governance",
               "solana-ecosystem",
               "web3"
           ],
           "exchanges": []
       },
       "PHB": {
           "name": "Phoenix",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PHB.png",
           "urls": {
               "website": "https://www.phoenix.global/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/phoenix-global-new"
           },
           "sector_tags": [
               "retail",
               "ai-big-data",
               "enterprise-solutions",
               "web3",
               "depin"
           ],
           "exchanges": []
       },
       "RON": {
           "name": "Ronin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/RON.png",
           "urls": {
               "website": "https://roninchain.com",
               "technical_doc": "https://docs.roninchain.com/basics/white-paper",
               "cmc": "https://coinmarketcap.com/currencies/ronin"
           },
           "sector_tags": [
               "platform",
               "gaming",
               "staking",
               "layer-1",
               "ronin-ecosystem"
           ],
           "exchanges": []
       },
       "BOBA": {
           "name": "Boba Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BOBA.png",
           "urls": {
               "website": "https://boba.network/",
               "technical_doc": "https://docs.boba.network/",
               "cmc": "https://coinmarketcap.com/currencies/boba-network"
           },
           "sector_tags": [
               "scaling",
               "ethereum-ecosystem",
               "layer-2",
               "rollups",
               "boba-network-ecosystem"
           ],
           "exchanges": []
       },
       "PEAQ": {
           "name": "peaq",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PEAQ.png",
           "urls": {
               "website": "https://www.peaq.network/",
               "technical_doc": "https://docs.peaq.network/",
               "cmc": "https://coinmarketcap.com/currencies/peaq"
           },
           "sector_tags": [
               "depin"
           ],
           "exchanges": []
       },
       "MAGIC": {
           "name": "Treasure",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MAGIC.png",
           "urls": {
               "website": "https://www.treasure.lol",
               "technical_doc": "https://docs.treasure.lol/",
               "cmc": "https://coinmarketcap.com/currencies/magic-token"
           },
           "sector_tags": [
               "gaming",
               "ethereum-ecosystem",
               "metaverse",
               "arbitrum-ecosystem"
           ],
           "exchanges": []
       },
       "AURORA": {
           "name": "Aurora",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AURORA.png",
           "urls": {
               "website": "https://aurora.dev/",
               "technical_doc": "https://doc.aurora.dev/",
               "cmc": "https://coinmarketcap.com/currencies/aurora-near"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "near-protocol-ecosystem",
               "okx-ventures-portfolio",
               "injective-ecosystem",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "PEOPLE": {
           "name": "ConstitutionDAO",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PEOPLE.png",
           "urls": {
               "website": "https://www.constitutiondao.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/constitutiondao"
           },
           "sector_tags": [
               "dao",
               "ethereum-ecosystem",
               "solana-ecosystem",
               "political-memes",
               "made-in-america"
           ],
           "exchanges": []
       },
       "SANTOS": {
           "name": "Santos FC Fan Token",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SANTOS.png",
           "urls": {
               "website": "https://www.santosfc.com.br/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/santos-fc-fan-token"
           },
           "sector_tags": [
               "fan-token",
               "binance-launchpool"
           ],
           "exchanges": []
       },
       "VOXEL": {
           "name": "Voxies",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/VOXEL.png",
           "urls": {
               "website": "https://voxies.io/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/voxies"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "binance-launchpad",
               "polygon-ecosystem",
               "play-to-earn"
           ],
           "exchanges": []
       },
       "BTT": {
           "name": "BitTorrent [New]",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BTT.png",
           "urls": {
               "website": "https://bt.io/",
               "technical_doc": "https://www.bittorrent.com/btt/btt-docs/BitTorrent_(BTT)_White_Paper_v0.8.7_Feb_2019.pdf",
               "cmc": "https://coinmarketcap.com/currencies/bittorrent-new"
           },
           "sector_tags": [
               "platform",
               "distributed-computing",
               "filesharing",
               "interoperability",
               "staking",
               "ethereum-ecosystem",
               "tron-ecosystem",
               "storage",
               "binance-launchpad",
               "binance-labs-portfolio",
               "web3",
               "bnb-chain-ecosystem",
               "alleged-sec-securities",
               "depin",
               "tron20-ecosystem"
           ],
           "exchanges": []
       },
       "LOOKS": {
           "name": "LooksRare",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LOOKS.png",
           "urls": {
               "website": "https://looksrare.org/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/looksrare"
           },
           "sector_tags": [
               "collectibles-nfts",
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "NYM": {
           "name": "NYM",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/NYM.png",
           "urls": {
               "website": "https://nym.com/",
               "technical_doc": "https://nym.com/nym-whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/nym"
           },
           "sector_tags": [
               "distributed-computing",
               "privacy",
               "interoperability",
               "smart-contracts",
               "wallet",
               "ethereum-ecosystem",
               "polychain-capital-portfolio",
               "governance",
               "fenbushi-capital-portfolio",
               "hashkey-capital-portfolio",
               "web3",
               "osmosis-ecosystem",
               "layer-1",
               "depin"
           ],
           "exchanges": []
       },
       "T": {
           "name": "Threshold",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/T.png",
           "urls": {
               "website": "https://threshold.network/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/threshold"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "bitcoin-ecosystem"
           ],
           "exchanges": []
       },
       "AXL": {
           "name": "Axelar",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AXL.png",
           "urls": {
               "website": "https://axelar.network/",
               "technical_doc": "https://axelar.network/wp-content/uploads/2021/07/axelar_whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/axelar"
           },
           "sector_tags": [
               "interoperability",
               "ethereum-ecosystem",
               "coinbase-ventures-portfolio",
               "polychain-capital-portfolio",
               "binance-labs-portfolio",
               "galaxy-digital-portfolio",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "optimism-ecosystem",
               "osmosis-ecosystem",
               "real-world-assets",
               "base-ecosystem",
               "linea-ecosystem",
               "sei-ecosystem",
               "mantle-ecosystem",
               "scroll-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "GMT": {
           "name": "GMT",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/GMT.png",
           "urls": {
               "website": "https://www.stepn.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/green-metaverse-token"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "ethereum-ecosystem",
               "binance-smart-chain",
               "binance-launchpad",
               "solana-ecosystem",
               "polygon-ecosystem",
               "bnb-chain-ecosystem",
               "move-to-earn"
           ],
           "exchanges": []
       },
       "XCN": {
           "name": "Onyxcoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/XCN.png",
           "urls": {
               "website": "https://onyx.org",
               "technical_doc": "https://onyx.org/Whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/onyxcoin"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "A8": {
           "name": "Ancient8",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/A8.png",
           "urls": {
               "website": "https://ancient8.gg",
               "technical_doc": "https://docs.ancient8.gg/",
               "cmc": "https://coinmarketcap.com/currencies/ancient8"
           },
           "sector_tags": [
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "APE": {
           "name": "ApeCoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/APE.png",
           "urls": {
               "website": "http://apecoin.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/apecoin-ape"
           },
           "sector_tags": [
               "collectibles-nfts",
               "content-creation",
               "gaming",
               "entertainment",
               "dao",
               "ethereum-ecosystem",
               "governance",
               "metaverse",
               "animoca-brands-portfolio",
               "ip-memes"
           ],
           "exchanges": []
       },
       "STG": {
           "name": "Stargate Finance",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/STG.png",
           "urls": {
               "website": "https://stargate.finance/",
               "technical_doc": "https://www.dropbox.com/s/gf3606jedromp61/Delta-Solving.The.Bridging-Trilemma.pdf",
               "cmc": "https://coinmarketcap.com/currencies/stargate-finance"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "avalanche-ecosystem",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "arbitrum-ecosystem",
               "cross-chain",
               "optimism-ecosystem"
           ],
           "exchanges": []
       },
       "AITECH": {
           "name": "Solidus Ai Tech",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AITECH.png",
           "urls": {
               "website": "https://aitech.io",
               "technical_doc": "https://docs.aitech.io",
               "cmc": "https://coinmarketcap.com/currencies/solidus-ai-tech"
           },
           "sector_tags": [
               "ai-big-data",
               "binance-chain",
               "launchpad",
               "seedify",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "COW": {
           "name": "CoW Protocol",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/COW.png",
           "urls": {
               "website": "https://cow.fi",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/cow-protocol"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "ethereum-ecosystem",
               "intent",
               "gnosis-chain-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "DMAIL": {
           "name": "DMAIL Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DMAIL.png",
           "urls": {
               "website": "https://dmail.ai/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/dmail-network"
           },
           "sector_tags": [
               "communications-social-media",
               "ethereum-ecosystem",
               "storage",
               "binance-smart-chain",
               "social-money",
               "web3",
               "internet-computer-ecosystem",
               "zksync-era-ecosystem",
               "linea-ecosystem",
               "depin"
           ],
           "exchanges": []
       },
       "REI": {
           "name": "REI Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/REI.png",
           "urls": {
               "website": "https://rei.network/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/rei-network"
           },
           "sector_tags": [],
           "exchanges": []
       },
       "LUNA": {
           "name": "Terra",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LUNA.png",
           "urls": {
               "website": "https://terra.money/",
               "technical_doc": "https://docs.terra.money/",
               "cmc": "https://coinmarketcap.com/currencies/terra-luna-v2"
           },
           "sector_tags": [
               "osmosis-ecosystem",
               "alleged-sec-securities"
           ],
           "exchanges": []
       },
       "POLYX": {
           "name": "Polymesh",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/POLYX.png",
           "urls": {
               "website": "https://polymesh.network/",
               "technical_doc": "https://polymesh.network/resources",
               "cmc": "https://coinmarketcap.com/currencies/polymesh"
           },
           "sector_tags": [
               "real-world-assets"
           ],
           "exchanges": []
       },
       "KAS": {
           "name": "Kaspa",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/KAS.png",
           "urls": {
               "website": "https://www.kaspa.org",
               "technical_doc": "https://eprint.iacr.org/2018/104.pdf",
               "cmc": "https://coinmarketcap.com/currencies/kaspa"
           },
           "sector_tags": [
               "mineable",
               "dag",
               "pow",
               "store-of-value",
               "polychain-capital-portfolio",
               "layer-1"
           ],
           "exchanges": []
       },
       "TAI": {
           "name": "TARS AI",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TAI.png",
           "urls": {
               "website": "https://tars.pro/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/tars-protocol"
           },
           "sector_tags": [
               "ai-big-data",
               "solana-ecosystem",
               "ai-agents",
               "ai-agent-launchpad"
           ],
           "exchanges": []
       },
       "EURC": {
           "name": "EURC",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/EURC.png",
           "urls": {
               "website": "https://www.circle.com",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/euro-coin"
           },
           "sector_tags": [
               "stablecoin",
               "ethereum-ecosystem",
               "stellar-ecosystem",
               "solana-ecosystem",
               "eur-stablecoin",
               "base-ecosystem",
               "fiat-stablecoin"
           ],
           "exchanges": []
       },
       "KCS": {
           "name": "KuCoin Token",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/KCS.png",
           "urls": {
               "website": "https://www.kucoin.com/",
               "technical_doc": "https://www.kcs.foundation/kcs-whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/kucoin-token"
           },
           "sector_tags": [
               "marketplace",
               "centralized-exchange",
               "discount-token",
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "ICX": {
           "name": "ICON",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ICX.png",
           "urls": {
               "website": "https://icon.community",
               "technical_doc": "https://icondev.io/",
               "cmc": "https://coinmarketcap.com/currencies/icon"
           },
           "sector_tags": [
               "platform",
               "cosmos-ecosystem",
               "enterprise-solutions",
               "interoperability",
               "smart-contracts",
               "ethereum-ecosystem",
               "binance-smart-chain",
               "kenetic-capital-portfolio",
               "pantera-capital-portfolio",
               "cross-chain"
           ],
           "exchanges": []
       },
       "ENJ": {
           "name": "Enjin Coin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ENJ.png",
           "urls": {
               "website": "https://enjin.io/",
               "technical_doc": "https://docs.enjin.io/",
               "cmc": "https://coinmarketcap.com/currencies/enjin-coin"
           },
           "sector_tags": [
               "media",
               "vr-ar",
               "collectibles-nfts",
               "gaming",
               "polkadot-ecosystem",
               "metaverse",
               "arrington-xrp-capital-portfolio",
               "layer-1"
           ],
           "exchanges": []
       },
       "POWR": {
           "name": "Powerledger",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/POWR.png",
           "urls": {
               "website": "https://powerledger.io/",
               "technical_doc": "https://www.powerledger.io/company/power-ledger-whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/power-ledger"
           },
           "sector_tags": [
               "energy",
               "sharing-economy",
               "ethereum-ecosystem",
               "alleged-sec-securities"
           ],
           "exchanges": []
       },
       "FIL": {
           "name": "Filecoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FIL.png",
           "urls": {
               "website": "https://filecoin.io/",
               "technical_doc": "https://docs.filecoin.io/",
               "cmc": "https://coinmarketcap.com/currencies/filecoin"
           },
           "sector_tags": [
               "mineable",
               "ai-big-data",
               "distributed-computing",
               "filesharing",
               "storage",
               "polychain-capital-portfolio",
               "heco-ecosystem",
               "blockchain-capital-portfolio",
               "boostvc-portfolio",
               "dcg-portfolio",
               "hashkey-capital-portfolio",
               "a16z-portfolio",
               "winklevoss-capital-portfolio",
               "pantera-capital-portfolio",
               "web3",
               "filecoin-ecosystem",
               "alleged-sec-securities",
               "depin",
               "2017-2018-alt-season",
               "hoo-smart-chain-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "STMX": {
           "name": "StormX",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/STMX.png",
           "urls": {
               "website": "https://stormx.io/",
               "technical_doc": "https://s3.amazonaws.com/cakecodes/pdf/storm_web/STORM_Token_White_Paper_Market_Research_Network_Development_vFINAL_.pdf",
               "cmc": "https://coinmarketcap.com/currencies/stormx"
           },
           "sector_tags": [
               "media",
               "loyalty",
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "ELF": {
           "name": "aelf",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ELF.png",
           "urls": {
               "website": "http://aelf.com/",
               "technical_doc": "https://docs.aelf.com/resources/whitepaper-2/",
               "cmc": "https://coinmarketcap.com/currencies/aelf"
           },
           "sector_tags": [
               "dpos",
               "interoperability",
               "smart-contracts",
               "dao",
               "dapp",
               "ethereum-ecosystem",
               "cross-chain",
               "bnb-chain-ecosystem",
               "layer-1"
           ],
           "exchanges": []
       },
       "UTK": {
           "name": "xMoney",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/UTK.png",
           "urls": {
               "website": "https://www.xmoney.com/",
               "technical_doc": "https://utrust.com/static/UTRUST-whitepaper-en-2017-11-02-2ae02efb0e0203893bdba1f54000f2b4.pdf",
               "cmc": "https://coinmarketcap.com/currencies/utrust"
           },
           "sector_tags": [
               "cybersecurity",
               "payments",
               "smart-contracts",
               "ethereum-ecosystem",
               "elrond-ecosystem",
               "multiversx-ecosystem"
           ],
           "exchanges": []
       },
       "IOST": {
           "name": "IOST",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/IOST.png",
           "urls": {
               "website": "http://iost.io/",
               "technical_doc": "https://whitepaper.io/document/28/iostoken-whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/iostoken"
           },
           "sector_tags": [
               "hardware",
               "iot",
               "huobi-capital-portfolio"
           ],
           "exchanges": []
       },
       "THETA": {
           "name": "Theta Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/THETA.png",
           "urls": {
               "website": "https://www.thetatoken.org/",
               "technical_doc": "https://s3.us-east-2.amazonaws.com/assets.thetatoken.org/Theta-white-paper-latest.pdf?v=1553657855.509",
               "cmc": "https://coinmarketcap.com/currencies/theta-network"
           },
           "sector_tags": [
               "media",
               "vr-ar",
               "ai-big-data",
               "distributed-computing",
               "content-creation",
               "entertainment",
               "sharing-economy",
               "smart-contracts",
               "video",
               "huobi-capital-portfolio",
               "web3",
               "layer-1",
               "depin",
               "2017-2018-alt-season",
               "made-in-america"
           ],
           "exchanges": []
       },
       "TRAC": {
           "name": "OriginTrail",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TRAC.png",
           "urls": {
               "website": "https://origintrail.io/",
               "technical_doc": "https://origintrail.io/ecosystem/whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/origintrail"
           },
           "sector_tags": [
               "marketplace",
               "logistics",
               "ai-big-data",
               "enterprise-solutions",
               "distributed-computing",
               "data-provenance",
               "privacy",
               "interoperability",
               "smart-contracts",
               "ethereum-ecosystem",
               "polkadot-ecosystem",
               "polygon-ecosystem",
               "web3",
               "open-source",
               "desci",
               "generative-ai",
               "real-world-assets",
               "layer-1",
               "depin",
               "ai-agents",
               "gnosis-chain-ecosystem"
           ],
           "exchanges": []
       },
       "ZIL": {
           "name": "Zilliqa",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ZIL.png",
           "urls": {
               "website": "https://www.zilliqa.com/",
               "technical_doc": "https://docs.zilliqa.com/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/zilliqa"
           },
           "sector_tags": [
               "mineable",
               "platform",
               "payments",
               "smart-contracts",
               "ethereum-ecosystem",
               "polychain-capital-portfolio",
               "metaverse",
               "kenetic-capital-portfolio",
               "zilliqa-ecosystem",
               "bnb-chain-ecosystem"
           ],
           "exchanges": []
       },
       "REN": {
           "name": "Ren",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/REN.png",
           "urls": {
               "website": "https://renprotocol.org/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/ren"
           },
           "sector_tags": [
               "marketplace",
               "defi",
               "ethereum-ecosystem",
               "avalanche-ecosystem",
               "solana-ecosystem",
               "kenetic-capital-portfolio",
               "huobi-capital-portfolio",
               "alameda-research-portfolio",
               "fantom-ecosystem",
               "near-protocol-ecosystem",
               "bitcoin-ecosystem",
               "gnosis-chain-ecosystem",
               "sora-ecosystem"
           ],
           "exchanges": []
       },
       "TUSD": {
           "name": "TrueUSD",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TUSD.png",
           "urls": {
               "website": "https://tusd.io/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/trueusd"
           },
           "sector_tags": [
               "store-of-value",
               "stablecoin",
               "asset-backed-stablecoin",
               "ethereum-ecosystem",
               "avalanche-ecosystem",
               "arbitrum-ecosystem",
               "bnb-chain-ecosystem",
               "usd-stablecoin",
               "fiat-stablecoin",
               "tron20-ecosystem"
           ],
           "exchanges": []
       },
       "ONT": {
           "name": "Ontology",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ONT.png",
           "urls": {
               "website": "https://ont.io/",
               "technical_doc": "https://docs.ont.io",
               "cmc": "https://coinmarketcap.com/currencies/ontology"
           },
           "sector_tags": [
               "enterprise-solutions",
               "identity",
               "ethereum-ecosystem",
               "metaverse",
               "huobi-capital-portfolio",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "web3",
               "near-protocol-ecosystem",
               "bnb-chain-ecosystem",
               "ont-ecosystem"
           ],
           "exchanges": []
       },
       "RVN": {
           "name": "Ravencoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/RVN.png",
           "urls": {
               "website": "https://ravencoin.org/",
               "technical_doc": "https://ravencoin.org/whitepaper/",
               "cmc": "https://coinmarketcap.com/currencies/ravencoin"
           },
           "sector_tags": [
               "mineable",
               "pow",
               "platform",
               "crowdfunding",
               "made-in-america"
           ],
           "exchanges": []
       },
       "SNX": {
           "name": "Synthetix",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SNX.png",
           "urls": {
               "website": "https://www.synthetix.io/",
               "technical_doc": "https://docs.synthetix.io/",
               "cmc": "https://coinmarketcap.com/currencies/synthetix"
           },
           "sector_tags": [
               "services",
               "decentralized-exchange-dex-token",
               "defi",
               "derivatives",
               "dao",
               "ethereum-ecosystem",
               "yield-farming",
               "coinbase-ventures-portfolio",
               "three-arrows-capital-portfolio",
               "governance",
               "heco-ecosystem",
               "solana-ecosystem",
               "synthetics",
               "defiance-capital-portfolio",
               "framework-ventures-portfolio",
               "alameda-research-portfolio",
               "parafi-capital",
               "paradigm-portfolio",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "near-protocol-ecosystem",
               "spartan-group",
               "bnb-chain-ecosystem",
               "optimism-ecosystem",
               "real-world-assets",
               "dwf-labs-portfolio",
               "celsius-bankruptcy-estate",
               "defi"
           ],
           "exchanges": []
       },
       "WAN": {
           "name": "Wanchain",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/WAN.png",
           "urls": {
               "website": "https://wanchain.org/",
               "technical_doc": "https://docs.wanchain.org/",
               "cmc": "https://coinmarketcap.com/currencies/wanchain"
           },
           "sector_tags": [
               "marketplace",
               "enterprise-solutions",
               "defi",
               "interoperability",
               "cross-chain"
           ],
           "exchanges": []
       },
       "XDC": {
           "name": "XDC Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/XDC.png",
           "urls": {
               "website": "https://www.xdc.org/",
               "technical_doc": "https://xinfin.org/docs/whitepaper-tech.pdf",
               "cmc": "https://coinmarketcap.com/currencies/xdc-network"
           },
           "sector_tags": [
               "enterprise-solutions",
               "masternodes",
               "smart-contracts",
               "xdc-ecosystem",
               "real-world-assets",
               "layer-1"
           ],
           "exchanges": []
       },
       "NEXO": {
           "name": "Nexo",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/NEXO.png",
           "urls": {
               "website": "https://nexo.com",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/nexo"
           },
           "sector_tags": [
               "services",
               "payments",
               "ethereum-ecosystem",
               "polygon-ecosystem",
               "cardano-ecosystem",
               "bnb-chain-ecosystem",
               "alleged-sec-securities"
           ],
           "exchanges": []
       },
       "XYO": {
           "name": "XYO",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/XYO.png",
           "urls": {
               "website": "https://xyo.network/",
               "technical_doc": "https://docs.xyo.network/XYO-White-Paper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/xyo"
           },
           "sector_tags": [
               "logistics",
               "distributed-computing",
               "oracles",
               "ethereum-ecosystem",
               "solana-ecosystem",
               "metaverse",
               "play-to-earn",
               "web3",
               "alleged-sec-securities",
               "depin",
               "made-in-america"
           ],
           "exchanges": []
       },
       "AVA": {
           "name": "AVA (Travala)",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AVA.png",
           "urls": {
               "website": "https://www.avafoundation.org",
               "technical_doc": "https://www.avafoundation.org/ava-2-0-whitepaper/",
               "cmc": "https://coinmarketcap.com/currencies/ava"
           },
           "sector_tags": [
               "hospitality",
               "marketplace",
               "medium-of-exchange",
               "payments",
               "ethereum-ecosystem",
               "solana-ecosystem",
               "tourism",
               "fantom-ecosystem",
               "bnb-chain-ecosystem"
           ],
           "exchanges": []
       },
       "IOTX": {
           "name": "IoTeX",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/IOTX.png",
           "urls": {
               "website": "https://www.iotex.io/",
               "technical_doc": "https://iotex.io/2.0",
               "cmc": "https://coinmarketcap.com/currencies/iotex"
           },
           "sector_tags": [
               "platform",
               "distributed-computing",
               "iot",
               "ethereum-ecosystem",
               "hashkey-capital-portfolio",
               "kenetic-capital-portfolio",
               "polygon-ecosystem",
               "iotex-ecosystem",
               "bnb-chain-ecosystem",
               "base-ecosystem",
               "depin",
               "made-in-america"
           ],
           "exchanges": []
       },
       "NKN": {
           "name": "NKN",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/NKN.png",
           "urls": {
               "website": "https://nkn.org/",
               "technical_doc": "https://nkn.org/wp-content/uploads/2020/10/NKN_Whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/nkn"
           },
           "sector_tags": [
               "mineable",
               "platform",
               "enterprise-solutions",
               "distributed-computing",
               "iot",
               "web3",
               "depin"
           ],
           "exchanges": []
       },
       "BRL": {
           "name": "Brazilian Real",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BRL.png",
           "urls": {
               "website": null,
               "technical_doc": null,
               "cmc": null
           },
           "sector_tags": [
               "fiat"
           ],
           "exchanges": []
       },
       "EUR": {
           "name": "Euro",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/EUR.png",
           "urls": {
               "website": null,
               "technical_doc": null,
               "cmc": null
           },
           "sector_tags": [
               "fiat"
           ],
           "exchanges": []
       },
       "TRY": {
           "name": "Turkish Lira",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TRY.png",
           "urls": {
               "website": null,
               "technical_doc": null,
               "cmc": null
           },
           "sector_tags": [
               "fiat"
           ],
           "exchanges": []
       },
       "QKC": {
           "name": "QuarkChain",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/QKC.png",
           "urls": {
               "website": "https://quarkchain.io/",
               "technical_doc": "https://quarkchain.io/wp-content/uploads/2018/11/QUARK-CHAIN-Public-Version-0.3.5.pdf",
               "cmc": "https://coinmarketcap.com/currencies/quarkchain"
           },
           "sector_tags": [
               "marketplace",
               "payments",
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "LEVER": {
           "name": "LeverFi",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LEVER.png",
           "urls": {
               "website": "https://www.leverfi.io/",
               "technical_doc": "https://docs.leverfi.io/",
               "cmc": "https://coinmarketcap.com/currencies/lever"
           },
           "sector_tags": [
               "defi",
               "ethereum-ecosystem",
               "iost-ecosystem",
               "solana-ecosystem",
               "arrington-xrp-capital-portfolio",
               "alameda-research-portfolio",
               "parafi-capital",
               "bitcoin-ecosystem",
               "dwf-labs-portfolio",
               "brc-20"
           ],
           "exchanges": []
       },
       "SUI": {
           "name": "Sui",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SUI.png",
           "urls": {
               "website": "https://sui.io/#",
               "technical_doc": "https://docs.sui.io/",
               "cmc": "https://coinmarketcap.com/currencies/sui"
           },
           "sector_tags": [
               "binance-launchpool",
               "coinbase-ventures-portfolio",
               "binance-labs-portfolio",
               "electric-capital-portfolio",
               "a16z-portfolio",
               "sui-ecosystem",
               "layer-1",
               "move-vm",
               "cmc-crypto-yearbook-2024-2025",
               "made-in-america"
           ],
           "exchanges": []
       },
       "RDNT": {
           "name": "Radiant Capital",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/RDNT.png",
           "urls": {
               "website": "https://radiant.capital/",
               "technical_doc": "https://docs.radiant.capital/radiant/",
               "cmc": "https://coinmarketcap.com/currencies/radiant-capital"
           },
           "sector_tags": [
               "defi",
               "ethereum-ecosystem",
               "binance-launchpool",
               "binance-labs-portfolio",
               "lending-borowing",
               "arbitrum-ecosystem",
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "ONDO": {
           "name": "Ondo",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ONDO.png",
           "urls": {
               "website": "https://ondo.foundation/",
               "technical_doc": "https://docs.ondo.foundation/ondo-token",
               "cmc": "https://coinmarketcap.com/currencies/ondo-finance"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "real-world-assets",
               "cmc-crypto-yearbook-2024-2025",
               "made-in-america"
           ],
           "exchanges": []
       },
       "ZETA": {
           "name": "ZetaChain",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ZETA.png",
           "urls": {
               "website": "https://zetachain.com",
               "technical_doc": "https://www.zetachain.com/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/zetachain"
           },
           "sector_tags": [
               "interoperability",
               "smart-contracts",
               "ethereum-ecosystem",
               "cross-chain",
               "bitcoin-ecosystem",
               "layer-1",
               "made-in-america"
           ],
           "exchanges": []
       },
       "ETHW": {
           "name": "EthereumPoW",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ETHW.png",
           "urls": {
               "website": "https://ethereumpow.org/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/ethereum-pow"
           },
           "sector_tags": [
               "pow"
           ],
           "exchanges": []
       },
       "SWEAT": {
           "name": "Sweat Economy",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SWEAT.png",
           "urls": {
               "website": "https://www.sweateconomy.com",
               "technical_doc": "https://drive.google.com/file/d/1IPklRcEQvgJkCaeYvGh43yjWl-Dj5_6i/view",
               "cmc": "https://coinmarketcap.com/currencies/sweat-economy"
           },
           "sector_tags": [
               "sports",
               "collectibles-nfts",
               "gaming",
               "staking",
               "ethereum-ecosystem",
               "near-protocol-ecosystem",
               "move-to-earn"
           ],
           "exchanges": []
       },
       "LISTA": {
           "name": "Lista DAO",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LISTA.png",
           "urls": {
               "website": "https://lista.org/",
               "technical_doc": "https://docs.bsc.lista.org/",
               "cmc": "https://coinmarketcap.com/currencies/lista-dao"
           },
           "sector_tags": [
               "defi",
               "staking",
               "dao",
               "binance-smart-chain",
               "lending-borowing"
           ],
           "exchanges": []
       },
       "SAFE": {
           "name": "Safe",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SAFE.png",
           "urls": {
               "website": "https://safe.global/",
               "technical_doc": "https://docs.gnosis-safe.io/",
               "cmc": "https://coinmarketcap.com/currencies/safe1"
           },
           "sector_tags": [
               "staking",
               "wallet",
               "ethereum-ecosystem",
               "account-abstraction"
           ],
           "exchanges": []
       },
       "APT": {
           "name": "Aptos",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/APT.png",
           "urls": {
               "website": "https://aptosfoundation.org",
               "technical_doc": "https://aptosfoundation.org/whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/aptos"
           },
           "sector_tags": [
               "binance-labs-portfolio",
               "aptos-ecosystem",
               "circle-ventures-portfolio",
               "layer-1",
               "ftx-bankruptcy-estate",
               "move-vm",
               "made-in-america"
           ],
           "exchanges": []
       },
       "ID": {
           "name": "SPACE ID",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ID.png",
           "urls": {
               "website": "https://space.id",
               "technical_doc": "https://docs.space.id",
               "cmc": "https://coinmarketcap.com/currencies/space-id"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "binance-launchpad",
               "mvb",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "MPLX": {
           "name": "Metaplex",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MPLX.png",
           "urls": {
               "website": "https://www.metaplex.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/metaplex"
           },
           "sector_tags": [
               "solana-ecosystem"
           ],
           "exchanges": []
       },
       "ALEX": {
           "name": "ALEX Lab",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ALEX.png",
           "urls": {
               "website": "https://alexgo.io",
               "technical_doc": "https://medium.com/alexgobtc/whitepaper/home",
               "cmc": "https://coinmarketcap.com/currencies/alex-lab"
           },
           "sector_tags": [
               "bitcoin-ecosystem",
               "dwf-labs-portfolio",
               "stacks-ecosystem",
               "btcfi"
           ],
           "exchanges": []
       },
       "VRTX": {
           "name": "Vertex Protocol",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/VRTX.png",
           "urls": {
               "website": "https://vertexprotocol.com/",
               "technical_doc": "https://vertex-protocol.gitbook.io/docs/getting-started/overview",
               "cmc": "https://coinmarketcap.com/currencies/vertex-protocol"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "ethereum-ecosystem",
               "amm",
               "dex",
               "arbitrum-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "HFT": {
           "name": "Hashflow",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/HFT.png",
           "urls": {
               "website": "https://hashflow.com/",
               "technical_doc": "https://docs.hashflow.com/",
               "cmc": "https://coinmarketcap.com/currencies/hashflow"
           },
           "sector_tags": [
               "collectibles-nfts",
               "decentralized-exchange-dex-token",
               "defi",
               "gaming",
               "interoperability",
               "dao",
               "ethereum-ecosystem",
               "binance-smart-chain",
               "dex",
               "binance-launchpool",
               "coinbase-ventures-portfolio",
               "avalanche-ecosystem",
               "solana-ecosystem",
               "dcg-portfolio",
               "dragonfly-capital-portfolio",
               "electric-capital-portfolio",
               "fabric-ventures-portfolio",
               "galaxy-digital-portfolio",
               "polygon-ecosystem",
               "arbitrum-ecosystem",
               "cross-chain",
               "optimism-ecosystem",
               "jump-crypto",
               "dwf-labs-portfolio",
               "defi"
           ],
           "exchanges": []
       },
       "ACX": {
           "name": "Across Protocol",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ACX.png",
           "urls": {
               "website": "https://across.to/",
               "technical_doc": "https://docs.across.to/v2/",
               "cmc": "https://coinmarketcap.com/currencies/across-protocol"
           },
           "sector_tags": [
               "interoperability",
               "ethereum-ecosystem",
               "cross-chain",
               "optimism-ecosystem",
               "intent"
           ],
           "exchanges": []
       },
       "STRK": {
           "name": "Starknet",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/STRK.png",
           "urls": {
               "website": "https://starknet.io/",
               "technical_doc": "https://starknet.io/docs/",
               "cmc": "https://coinmarketcap.com/currencies/starknet-token"
           },
           "sector_tags": [
               "zero-knowledge-proofs",
               "dapp",
               "ethereum-ecosystem",
               "layer-2",
               "rollups",
               "starknet-ecosystem"
           ],
           "exchanges": []
       },
       "HOOK": {
           "name": "Hooked Protocol",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/HOOK.png",
           "urls": {
               "website": "https://hooked.io/",
               "technical_doc": "https://hooked-protocol.gitbook.io/hooked-protocol-whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/hooked-protocol"
           },
           "sector_tags": [
               "education",
               "ai-big-data",
               "gaming",
               "binance-smart-chain",
               "binance-launchpad",
               "binance-labs-portfolio",
               "metaverse",
               "bnb-chain-ecosystem"
           ],
           "exchanges": []
       },
       "HONEY": {
           "name": "Hivemapper",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/HONEY.png",
           "urls": {
               "website": "https://hivemapper.com/",
               "technical_doc": "https://docs.hivemapper.com/",
               "cmc": "https://coinmarketcap.com/currencies/hivemapper"
           },
           "sector_tags": [
               "distributed-computing",
               "iot",
               "solana-ecosystem",
               "depin",
               "made-in-america"
           ],
           "exchanges": []
       },
       "TIA": {
           "name": "Celestia",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TIA.png",
           "urls": {
               "website": "https://www.celestia.org",
               "technical_doc": "https://arxiv.org/abs/1905.09274",
               "cmc": "https://coinmarketcap.com/currencies/celestia"
           },
           "sector_tags": [
               "platform",
               "cosmos-ecosystem",
               "injective-ecosystem",
               "osmosis-ecosystem",
               "modular-blockchain",
               "egirl-capital-portfolio",
               "data-availability",
               "cmc-crypto-awards-2024"
           ],
           "exchanges": []
       },
       "TAO": {
           "name": "Bittensor",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TAO.png",
           "urls": {
               "website": "https://bittensor.com",
               "technical_doc": "https://drive.google.com/file/d/1VnsobL6lIAAqcA1_Tbm8AYIQscfJV4KU/view?usp=sharing",
               "cmc": "https://coinmarketcap.com/currencies/bittensor"
           },
           "sector_tags": [
               "ai-big-data",
               "distributed-computing",
               "oracles",
               "dao",
               "polkadot-ecosystem",
               "polychain-capital-portfolio",
               "dcg-portfolio",
               "open-source",
               "generative-ai",
               "depin",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "HIFI": {
           "name": "Hifi Finance",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/HIFI.png",
           "urls": {
               "website": "https://hifi.finance/",
               "technical_doc": "https://docs.hifi.finance/",
               "cmc": "https://coinmarketcap.com/currencies/hifi-finance"
           },
           "sector_tags": [
               "real-estate",
               "defi",
               "dao",
               "ethereum-ecosystem",
               "governance",
               "lending-borowing",
               "arrington-xrp-capital-portfolio",
               "kenetic-capital-portfolio",
               "real-world-assets",
               "made-in-america"
           ],
           "exchanges": []
       },
       "BONK": {
           "name": "Bonk",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BONK.png",
           "urls": {
               "website": "https://www.bonkcoin.com/",
               "technical_doc": "https://assets-global.website-files.com/63d9862f53dc8e65d16eb0e0/63de6fb910d0b94a933c4a2f_BONK-PAPER-040223.pdf",
               "cmc": "https://coinmarketcap.com/currencies/bonk1"
           },
           "sector_tags": [
               "memes",
               "ethereum-ecosystem",
               "solana-ecosystem",
               "polygon-ecosystem",
               "doggone-doggerel",
               "injective-ecosystem",
               "aptos-ecosystem",
               "sui-ecosystem",
               "dwf-labs-portfolio",
               "base-ecosystem",
               "cmc-crypto-awards-2024"
           ],
           "exchanges": []
       },
       "BLUR": {
           "name": "Blur",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BLUR.png",
           "urls": {
               "website": "https://blur.io/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/blur-token"
           },
           "sector_tags": [
               "collectibles-nfts",
               "ethereum-ecosystem",
               "egirl-capital-portfolio",
               "made-in-america"
           ],
           "exchanges": []
       },
       "SEI": {
           "name": "Sei",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SEI.png",
           "urls": {
               "website": "https://www.sei.io/",
               "technical_doc": "https://github.com/sei-protocol/sei-chain/blob/main/whitepaper/Sei_Whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/sei"
           },
           "sector_tags": [
               "cosmos-ecosystem",
               "coinbase-ventures-portfolio",
               "multicoin-capital-portfolio",
               "osmosis-ecosystem",
               "layer-1",
               "jump-crypto",
               "sei-ecosystem",
               "parallel-evm",
               "made-in-america"
           ],
           "exchanges": []
       },
       "ACS": {
           "name": "Access Protocol",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ACS.png",
           "urls": {
               "website": "https://www.accessprotocol.co/",
               "technical_doc": "https://www.accessprotocol.co/Whitepaper_Access.pdf",
               "cmc": "https://coinmarketcap.com/currencies/access-protocol"
           },
           "sector_tags": [
               "solana-ecosystem"
           ],
           "exchanges": []
       },
       "CORE": {
           "name": "Core",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CORE.png",
           "urls": {
               "website": "https://www.coredao.org/",
               "technical_doc": "https://docs.coredao.org/core-white-paper-v1.0.5/",
               "cmc": "https://coinmarketcap.com/currencies/core-dao"
           },
           "sector_tags": [],
           "exchanges": []
       },
       "THE": {
           "name": "THENA",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/THE.png",
           "urls": {
               "website": "https://thena.fi/",
               "technical_doc": "https://thena.gitbook.io/thena/",
               "cmc": "https://coinmarketcap.com/currencies/thena"
           },
           "sector_tags": [
               "platform",
               "services",
               "store-of-value",
               "decentralized-exchange-dex-token",
               "defi",
               "smart-contracts",
               "staking",
               "dapp",
               "yield-farming",
               "binance-chain",
               "amm",
               "dex",
               "yield-aggregator",
               "lp-tokens",
               "software",
               "defi-index",
               "analytics",
               "web3",
               "token",
               "opbnb-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "PRIME": {
           "name": "Echelon Prime",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PRIME.png",
           "urls": {
               "website": "https://echelon.io/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/echelon-prime"
           },
           "sector_tags": [
               "ai-big-data",
               "collectibles-nfts",
               "gaming",
               "ethereum-ecosystem",
               "paradigm-portfolio",
               "generative-ai",
               "base-ecosystem",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "CGPT": {
           "name": "ChainGPT",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CGPT.png",
           "urls": {
               "website": "https://www.chaingpt.org",
               "technical_doc": "https://docs.chaingpt.org",
               "cmc": "https://coinmarketcap.com/currencies/chaingpt"
           },
           "sector_tags": [
               "media",
               "platform",
               "ai-big-data",
               "collectibles-nfts",
               "content-creation",
               "defi",
               "research",
               "smart-contracts",
               "dao",
               "ethereum-ecosystem",
               "binance-smart-chain",
               "governance",
               "launchpad",
               "seedify",
               "generative-ai",
               "telegram-bot",
               "discord-bots",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "AGI": {
           "name": "Delysium",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AGI.png",
           "urls": {
               "website": "https://www.delysium.com/",
               "technical_doc": "https://www.delysium.com/whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/delysium"
           },
           "sector_tags": [
               "platform",
               "ai-big-data",
               "ethereum-ecosystem",
               "solana-ecosystem",
               "generative-ai"
           ],
           "exchanges": []
       },
       "FUEL": {
           "name": "Fuel Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FUEL.png",
           "urls": {
               "website": "https://www.fuel.network/",
               "technical_doc": "https://docs.fuel.network/docs/intro/what-is-fuel/",
               "cmc": "https://coinmarketcap.com/currencies/fuel-network"
           },
           "sector_tags": [
               "scaling",
               "staking",
               "ethereum-ecosystem",
               "modular-blockchain"
           ],
           "exchanges": []
       },
       "ZK": {
           "name": "ZKsync",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ZK.png",
           "urls": {
               "website": "https://zksync.io/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/zksync"
           },
           "sector_tags": [
               "zero-knowledge-proofs",
               "layer-2",
               "blockchain-capital-portfolio",
               "okx-ventures-portfolio",
               "zksync-era-ecosystem",
               "modular-blockchain",
               "egirl-capital-portfolio"
           ],
           "exchanges": []
       },
       "PEPE": {
           "name": "Pepe",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PEPE.png",
           "urls": {
               "website": "https://www.pepe.vip/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/pepe"
           },
           "sector_tags": [
               "memes",
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "EDU": {
           "name": "Open Campus",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/EDU.png",
           "urls": {
               "website": "https://www.opencampus.xyz/",
               "technical_doc": "https://open-campus.gitbook.io/open-campus-protocol-whitepaper/",
               "cmc": "https://coinmarketcap.com/currencies/open-campus"
           },
           "sector_tags": [
               "education",
               "ethereum-ecosystem",
               "binance-launchpad",
               "polygon-ecosystem",
               "animoca-brands-portfolio"
           ],
           "exchanges": []
       },
       "CYBER": {
           "name": "Cyber",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CYBER.png",
           "urls": {
               "website": "https://cyber.co/",
               "technical_doc": "https://docs.cyber.co/build-on-cyber/contract-deployment",
               "cmc": "https://coinmarketcap.com/currencies/cyberconnect"
           },
           "sector_tags": [
               "platform",
               "communications-social-media",
               "collectibles-nfts",
               "interoperability",
               "reputation",
               "smart-contracts",
               "ethereum-ecosystem",
               "binance-smart-chain",
               "binance-launchpool",
               "governance",
               "binance-labs-portfolio",
               "dragonfly-capital-portfolio",
               "multicoin-capital-portfolio",
               "polygon-ecosystem",
               "web3",
               "arbitrum-ecosystem",
               "animoca-brands-portfolio",
               "spartan-group",
               "bnb-chain-ecosystem",
               "optimism-ecosystem",
               "polygon-ventures-portfolio",
               "dwf-labs-portfolio",
               "base-ecosystem",
               "linea-ecosystem",
               "cyber-ecosystem"
           ],
           "exchanges": []
       },
       "TURBO": {
           "name": "Turbo",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TURBO.png",
           "urls": {
               "website": "https://turbotoken.io/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/turbo"
           },
           "sector_tags": [
               "art",
               "ai-big-data",
               "memes",
               "ethereum-ecosystem",
               "solana-ecosystem",
               "web3",
               "dwf-labs-portfolio",
               "ai-memes"
           ],
           "exchanges": []
       },
       "SWELL": {
           "name": "Swell Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SWELL.png",
           "urls": {
               "website": "https://www.swellnetwork.io/",
               "technical_doc": "https://docs.swellnetwork.io/swell/what-is-swell",
               "cmc": "https://coinmarketcap.com/currencies/swell-network"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "liquid-staking-derivatives",
               "restaking",
               "eigenlayer-ecosystem"
           ],
           "exchanges": []
       },
       "ORDI": {
           "name": "ORDI",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ORDI.png",
           "urls": {
               "website": null,
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/ordi"
           },
           "sector_tags": [
               "bitcoin-ecosystem",
               "brc-20",
               "inscriptions",
               "ordinals-brc20-ecosystem",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "CETUS": {
           "name": "Cetus Protocol",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CETUS.png",
           "urls": {
               "website": "https://www.cetus.zone/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/cetus-protocol"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "defi",
               "sui-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "FDUSD": {
           "name": "First Digital USD",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FDUSD.png",
           "urls": {
               "website": "https://firstdigitallabs.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/first-digital-usd"
           },
           "sector_tags": [
               "stablecoin",
               "ethereum-ecosystem",
               "binance-smart-chain",
               "solana-ecosystem",
               "usd-stablecoin",
               "sui-ecosystem",
               "fiat-stablecoin"
           ],
           "exchanges": []
       },
       "ZRO": {
           "name": "LayerZero",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ZRO.png",
           "urls": {
               "website": "https://layerzero.foundation/",
               "technical_doc": "https://layerzero.network/publications/LayerZero_Whitepaper_V2.1.0.pdf",
               "cmc": "https://coinmarketcap.com/currencies/layerzero"
           },
           "sector_tags": [
               "interoperability",
               "ethereum-ecosystem",
               "coinbase-ventures-portfolio",
               "binance-labs-portfolio",
               "a16z-portfolio",
               "multicoin-capital-portfolio",
               "polygon-ecosystem",
               "cross-chain",
               "optimism-ecosystem",
               "sino-global-capital",
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "SCR": {
           "name": "Scroll",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SCR.png",
           "urls": {
               "website": "https://scroll.io/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/scroll"
           },
           "sector_tags": [
               "zero-knowledge-proofs",
               "binance-launchpool",
               "layer-2",
               "modular-blockchain",
               "scroll-ecosystem"
           ],
           "exchanges": []
       },
       "MNT": {
           "name": "Mantle",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MNT.png",
           "urls": {
               "website": "https://www.mantle.xyz/",
               "technical_doc": "https://docs.mantle.xyz/network/introduction/overview",
               "cmc": "https://coinmarketcap.com/currencies/mantle"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "layer-2",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "ARKM": {
           "name": "Arkham",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ARKM.png",
           "urls": {
               "website": "https://arkm.com/",
               "technical_doc": "https://info.arkm.com/whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/arkham"
           },
           "sector_tags": [
               "marketplace",
               "ai-big-data",
               "research",
               "ethereum-ecosystem",
               "governance",
               "binance-launchpad",
               "binance-labs-portfolio",
               "analytics",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "LYX": {
           "name": "LUKSO",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LYX.png",
           "urls": {
               "website": "https://lukso.network/",
               "technical_doc": "https://whitepaper.lukso.network/",
               "cmc": "https://coinmarketcap.com/currencies/lukso-network"
           },
           "sector_tags": [
               "communications-social-media",
               "collectibles-nfts",
               "identity",
               "smart-contracts",
               "metaverse",
               "layer-1"
           ],
           "exchanges": []
       },
       "MOG": {
           "name": "Mog Coin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MOG.png",
           "urls": {
               "website": "https://www.mogcoin.xyz",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/mog-coin"
           },
           "sector_tags": [
               "memes",
               "ethereum-ecosystem",
               "base-ecosystem",
               "cat-themed",
               "animal-memes"
           ],
           "exchanges": []
       },
       "PYUSD": {
           "name": "PayPal USD",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PYUSD.png",
           "urls": {
               "website": "https://www.paypal.com/pyusd",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/paypal-usd"
           },
           "sector_tags": [
               "stablecoin",
               "asset-backed-stablecoin",
               "ethereum-ecosystem",
               "solana-ecosystem",
               "usd-stablecoin",
               "fiat-stablecoin"
           ],
           "exchanges": []
       },
       "BANANA": {
           "name": "Banana Gun",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BANANA.png",
           "urls": {
               "website": "https://bananagun.io/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/banana-gun"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "telegram-bot"
           ],
           "exchanges": []
       },
       "SPX": {
           "name": "SPX6900",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SPX.png",
           "urls": {
               "website": "https://www.spx6900.com",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/spx6900"
           },
           "sector_tags": [
               "memes",
               "ethereum-ecosystem",
               "solana-ecosystem",
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "PYTH": {
           "name": "Pyth Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PYTH.png",
           "urls": {
               "website": "https://pyth.network/",
               "technical_doc": "https://pyth.network/whitepaper_v2.pdf",
               "cmc": "https://coinmarketcap.com/currencies/pyth-network"
           },
           "sector_tags": [
               "oracles",
               "solana-ecosystem",
               "cross-chain",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "SATS": {
           "name": "SATS (Ordinals)",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SATS.png",
           "urls": {
               "website": "https://satscoin.vip/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/sats-ordinals"
           },
           "sector_tags": [
               "bitcoin-ecosystem",
               "brc-20",
               "inscriptions",
               "ordinals-brc20-ecosystem"
           ],
           "exchanges": []
       },
       "BIGTIME": {
           "name": "Big Time",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BIGTIME.png",
           "urls": {
               "website": "https://bigtime.gg/",
               "technical_doc": "https://wiki.bigtime.gg/big-time-getting-started/welcome-to-big-time-wiki",
               "cmc": "https://coinmarketcap.com/currencies/big-time"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "ethereum-ecosystem",
               "gaming-guild",
               "okx-ventures-portfolio"
           ],
           "exchanges": []
       },
       "BEAM": {
           "name": "Beam",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BEAM.png",
           "urls": {
               "website": "https://www.onbeam.com/",
               "technical_doc": "https://docs.onbeam.com/",
               "cmc": "https://coinmarketcap.com/currencies/onbeam"
           },
           "sector_tags": [
               "gaming",
               "ethereum-ecosystem",
               "binance-chain",
               "binance-launchpool",
               "governance",
               "play-to-earn",
               "token",
               "gaming-guild",
               "spartan-group",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "TOKEN": {
           "name": "TokenFi",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TOKEN.png",
           "urls": {
               "website": "https://tokenfi.com",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/tokenfi"
           },
           "sector_tags": [
               "education",
               "retail",
               "real-estate",
               "ai-big-data",
               "defi",
               "memes",
               "research",
               "smart-contracts",
               "dao",
               "ethereum-ecosystem",
               "binance-chain",
               "binance-smart-chain",
               "social-token",
               "tokenized-stock",
               "governance",
               "launchpad",
               "analytics",
               "generative-ai",
               "real-world-assets",
               "telegram-bot",
               "dwf-labs-portfolio",
               "discord-bots",
               "paal-ecosystem",
               "tokenfi-launchpad"
           ],
           "exchanges": []
       },
       "MEME": {
           "name": "Memecoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MEME.png",
           "urls": {
               "website": "https://www.memecoin.org/",
               "technical_doc": "https://www.memecoin.org/whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/meme"
           },
           "sector_tags": [
               "collectibles-nfts",
               "memes",
               "ethereum-ecosystem",
               "binance-launchpool"
           ],
           "exchanges": []
       },
       "POL": {
           "name": "POL (ex-MATIC)",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/POL.png",
           "urls": {
               "website": "https://polygon.technology/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/polygon-ecosystem-token"
           },
           "sector_tags": [
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "DYDX": {
           "name": "dYdX (Native)",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DYDX.png",
           "urls": {
               "website": "https://dydx.trade/?utm_source=cmc&utm_medium=media&utm_campaign=cmc-feed",
               "technical_doc": "https://docs.dydx.community/dydx-token-migration/start-here/introduction",
               "cmc": "https://coinmarketcap.com/currencies/dydx-chain"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "osmosis-ecosystem",
               "made-in-america",
               "defi"
           ],
           "exchanges": []
       },
       "MYRO": {
           "name": "Myro",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MYRO.png",
           "urls": {
               "website": "https://myrothedog.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/myro"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "doggone-doggerel"
           ],
           "exchanges": []
       },
       "BLAST": {
           "name": "Blast",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BLAST.png",
           "urls": {
               "website": "https://blast.io/en",
               "technical_doc": "https://docs.blast.io/about-blast",
               "cmc": "https://coinmarketcap.com/currencies/blast"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "layer-2",
               "rollups",
               "paradigm-portfolio",
               "standard-crypto-portfolio",
               "egirl-capital-portfolio",
               "blast-ecosystem"
           ],
           "exchanges": []
       },
       "JTO": {
           "name": "Jito",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/JTO.png",
           "urls": {
               "website": "https://www.jito.network/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/jito"
           },
           "sector_tags": [
               "defi",
               "staking",
               "dao",
               "solana-ecosystem",
               "liquid-staking-derivatives",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "BCUT": {
           "name": "bitsCrunch",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BCUT.png",
           "urls": {
               "website": "https://bitscrunch.com/",
               "technical_doc": "https://bitscrunch.com/bitsCrunch_Whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/bitscrunch"
           },
           "sector_tags": [
               "ai-big-data",
               "distributed-computing",
               "collectibles-nfts",
               "dapp",
               "ethereum-ecosystem",
               "binance-smart-chain",
               "coinbase-ventures-portfolio",
               "avalanche-ecosystem",
               "solana-ecosystem",
               "hashkey-capital-portfolio",
               "polygon-ecosystem",
               "web3",
               "animoca-brands-portfolio",
               "cross-chain",
               "shima-capital",
               "polygon-ventures-portfolio",
               "depin"
           ],
           "exchanges": []
       },
       "ACE": {
           "name": "Fusionist",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ACE.png",
           "urls": {
               "website": "https://ace.fusionist.io/",
               "technical_doc": "https://www.fusionist.io/doc/Fusionist_Endurance_WhitePaper_Ver1.0_Publish.pdf",
               "cmc": "https://coinmarketcap.com/currencies/fusionist"
           },
           "sector_tags": [
               "gaming",
               "social-token",
               "binance-launchpool"
           ],
           "exchanges": []
       },
       "WIF": {
           "name": "dogwifhat",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/WIF.png",
           "urls": {
               "website": "https://dogwifcoin.org/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/dogwifhat"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "doggone-doggerel",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "NFP": {
           "name": "NFPrompt",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/NFP.png",
           "urls": {
               "website": "https://nfprompt.io/",
               "technical_doc": "https://docs.nfprompt.io/",
               "cmc": "https://coinmarketcap.com/currencies/nfprompt"
           },
           "sector_tags": [
               "ai-big-data",
               "content-creation",
               "payments",
               "staking",
               "binance-smart-chain",
               "binance-launchpool",
               "governance",
               "web3",
               "generative-ai"
           ],
           "exchanges": []
       },
       "POPCAT": {
           "name": "Popcat (SOL)",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/POPCAT.png",
           "urls": {
               "website": "https://www.popcatsolana.xyz/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/popcat-sol"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "cat-themed",
               "animal-memes",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "MAVIA": {
           "name": "Heroes of Mavia",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MAVIA.png",
           "urls": {
               "website": "https://mavia.com",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/heroes-of-mavia"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "ethereum-ecosystem",
               "binance-labs-portfolio",
               "exnetwork-capital-portfolio",
               "animoca-brands-portfolio",
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "AI": {
           "name": "Sleepless AI",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AI.png",
           "urls": {
               "website": "https://www.sleeplessailab.com",
               "technical_doc": "https://sleepless-ai.gitbook.io/him/",
               "cmc": "https://coinmarketcap.com/currencies/sleepless-ai"
           },
           "sector_tags": [
               "ai-big-data",
               "binance-smart-chain",
               "binance-launchpool",
               "web3",
               "generative-ai"
           ],
           "exchanges": []
       },
       "NOT": {
           "name": "Notcoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/NOT.png",
           "urls": {
               "website": "https://notco.in/",
               "technical_doc": "https://cdn.joincommunity.xyz/notcoin/Notcoin_Whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/notcoin"
           },
           "sector_tags": [
               "gaming",
               "memes",
               "binance-launchpool",
               "play-to-earn",
               "web3",
               "telegram-bot",
               "toncoin-ecosystem",
               "tap-to-earn",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "DYM": {
           "name": "Dymension",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DYM.png",
           "urls": {
               "website": "https://dymension.xyz/",
               "technical_doc": "https://docs.dymension.xyz/",
               "cmc": "https://coinmarketcap.com/currencies/dymension"
           },
           "sector_tags": [
               "cosmos-ecosystem",
               "osmosis-ecosystem",
               "modular-blockchain"
           ],
           "exchanges": []
       },
       "XAI": {
           "name": "Xai",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/XAI.png",
           "urls": {
               "website": "https://xai.games/",
               "technical_doc": "https://xai-foundation.gitbook.io/xai-network/xai-blockchain/welcome-to-xai",
               "cmc": "https://coinmarketcap.com/currencies/xai-games"
           },
           "sector_tags": [
               "platform",
               "gaming",
               "binance-launchpool",
               "cms-holdings-portfolio",
               "arbitrum-ecosystem",
               "animoca-brands-portfolio"
           ],
           "exchanges": []
       },
       "METH": {
           "name": "Mantle Staked Ether",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/METH.png",
           "urls": {
               "website": "https://www.mantle.xyz/meth",
               "technical_doc": "https://docs.mantle.xyz/meth",
               "cmc": "https://coinmarketcap.com/currencies/mantle-staked-ether"
           },
           "sector_tags": [
               "defi",
               "staking",
               "ethereum-ecosystem",
               "liquid-staking-derivatives",
               "eigenlayer-ecosystem",
               "rehypothecated-crypto",
               "mantle-ecosystem"
           ],
           "exchanges": []
       },
       "XMR": {
           "name": "Monero",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/XMR.png",
           "urls": {
               "website": "https://www.getmonero.org/",
               "technical_doc": "https://github.com/monero-project/research-lab/blob/master/whitepaper/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/monero"
           },
           "sector_tags": [
               "mineable",
               "pow",
               "medium-of-exchange",
               "privacy",
               "ringct",
               "boostvc-portfolio",
               "electric-capital-portfolio",
               "galaxy-digital-portfolio",
               "2017-2018-alt-season"
           ],
           "exchanges": []
       },
       "RPL": {
           "name": "Rocket Pool",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/RPL.png",
           "urls": {
               "website": "https://www.rocketpool.net/",
               "technical_doc": "https://docs.rocketpool.net",
               "cmc": "https://coinmarketcap.com/currencies/rocket-pool"
           },
           "sector_tags": [
               "defi",
               "ethereum-ecosystem",
               "liquid-staking-derivatives"
           ],
           "exchanges": []
       },
       "KRL": {
           "name": "Kryll",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/KRL.png",
           "urls": {
               "website": "https://kryll.io/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/kryll"
           },
           "sector_tags": [
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "VTHO": {
           "name": "VeThor Token",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/VTHO.png",
           "urls": {
               "website": "https://www.vechain.org/",
               "technical_doc": "https://www.vechain.org/assets/whitepaper/whitepaper-3-0.pdf",
               "cmc": "https://coinmarketcap.com/currencies/vethor-token"
           },
           "sector_tags": [
               "vechain-ecosystem"
           ],
           "exchanges": []
       },
       "FLUX": {
           "name": "Flux",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FLUX.png",
           "urls": {
               "website": "https://runonflux.com/",
               "technical_doc": "https://fluxwhitepaper.app.runonflux.io/",
               "cmc": "https://coinmarketcap.com/currencies/zel"
           },
           "sector_tags": [
               "mineable",
               "pow",
               "ai-big-data",
               "distributed-computing",
               "filesharing",
               "interoperability",
               "masternodes",
               "smart-contracts",
               "dao",
               "dapp",
               "ethereum-ecosystem",
               "storage",
               "governance",
               "algorand-ecosystem",
               "solana-ecosystem",
               "polygon-ecosystem",
               "web3",
               "cross-chain",
               "generative-ai",
               "kadena-ecosystem",
               "base-ecosystem",
               "depin",
               "tron20-ecosystem"
           ],
           "exchanges": []
       },
       "VET": {
           "name": "VeChain",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/VET.png",
           "urls": {
               "website": "https://www.vechain.org/",
               "technical_doc": "https://www.vechain.org/whitepaper/#bit_65sv8",
               "cmc": "https://coinmarketcap.com/currencies/vechain"
           },
           "sector_tags": [
               "logistics",
               "data-provenance",
               "iot",
               "smart-contracts",
               "fenbushi-capital-portfolio",
               "real-world-assets"
           ],
           "exchanges": []
       },
       "QNT": {
           "name": "Quant",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/QNT.png",
           "urls": {
               "website": "https://quant.network/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/quant"
           },
           "sector_tags": [
               "platform",
               "interoperability",
               "ethereum-ecosystem",
               "real-world-assets"
           ],
           "exchanges": []
       },
       "ONG": {
           "name": "Ontology Gas",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ONG.png",
           "urls": {
               "website": "https://ont.io/",
               "technical_doc": "https://docs.ont.io/",
               "cmc": "https://coinmarketcap.com/currencies/ontology-gas"
           },
           "sector_tags": [
               "ontology-ecosystem",
               "ont-ecosystem"
           ],
           "exchanges": []
       },
       "ALT": {
           "name": "Altlayer",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ALT.png",
           "urls": {
               "website": "https://altlayer.io/",
               "technical_doc": "https://docs.altlayer.io/",
               "cmc": "https://coinmarketcap.com/currencies/altlayer"
           },
           "sector_tags": [
               "zero-knowledge-proofs",
               "interoperability",
               "staking",
               "ethereum-ecosystem",
               "binance-launchpool",
               "polychain-capital-portfolio",
               "rollups",
               "governance",
               "binance-labs-portfolio",
               "jump-crypto",
               "restaking",
               "rollups-as-a-service"
           ],
           "exchanges": []
       },
       "PONKE": {
           "name": "Ponke",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PONKE.png",
           "urls": {
               "website": "https://www.ponke.xyz/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/ponke"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem"
           ],
           "exchanges": []
       },
       "HTX": {
           "name": "HTX",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/HTX.png",
           "urls": {
               "website": "https://www.htxdao.com",
               "technical_doc": "https://htxdao.gitbook.io/whitepaper/",
               "cmc": "https://coinmarketcap.com/currencies/htx"
           },
           "sector_tags": [
               "centralized-exchange",
               "ethereum-ecosystem",
               "tron-ecosystem",
               "tron20-ecosystem"
           ],
           "exchanges": []
       },
       "WEN": {
           "name": "Wen",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/WEN.png",
           "urls": {
               "website": "https://www.wenwencoin.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/wen"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "cat-themed"
           ],
           "exchanges": []
       },
       "LRDS": {
           "name": "BLOCKLORDS",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LRDS.png",
           "urls": {
               "website": "https://lordchain.blocklords.com/",
               "technical_doc": "https://wiki.blocklords.com/",
               "cmc": "https://coinmarketcap.com/currencies/blocklords"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "ethereum-ecosystem",
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "JUP": {
           "name": "Jupiter",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/JUP.png",
           "urls": {
               "website": "https://jup.ag/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/jupiter-ag"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "defi",
               "derivatives",
               "amm",
               "solana-ecosystem",
               "cross-chain",
               "defi"
           ],
           "exchanges": []
       },
       "GME": {
           "name": "GmeStop",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/GME.png",
           "urls": {
               "website": "https://www.gmestop.co",
               "technical_doc": "https://cdn.prod.website-files.com/66bc6d9142186c9f6f398b6f/6730e2e6e15d01712afe528c_GME%20WHITEPAPER.pdf",
               "cmc": "https://coinmarketcap.com/currencies/gme"
           },
           "sector_tags": [
               "gaming",
               "memes",
               "solana-ecosystem",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "AERO": {
           "name": "Aerodrome Finance",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AERO.png",
           "urls": {
               "website": "https://aerodrome.finance/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/aerodrome-finance"
           },
           "sector_tags": [
               "defi",
               "amm",
               "base-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "PANDORA,UNISWAP_V3,0X9E9FBDE7C7A83C43913BDDC8779158F1368F0413": {
           "name": "Pandora",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PANDORA,UNISWAP_V3,0X9E9FBDE7C7A83C43913BDDC8779158F1368F0413.png",
           "urls": {
               "website": "https://www.pandora.build/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/pandora-coin"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "liquid-staking-derivatives",
               "erc-404",
               "hybrid-token-standard"
           ],
           "exchanges": []
       },
       "PIXEL": {
           "name": "Pixels",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PIXEL.png",
           "urls": {
               "website": "https://www.pixels.xyz/",
               "technical_doc": "https://whitepaper.pixels.xyz/",
               "cmc": "https://coinmarketcap.com/currencies/pixels"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "ethereum-ecosystem",
               "binance-launchpool",
               "governance",
               "play-to-earn",
               "web3",
               "animoca-brands-portfolio",
               "gaming-guild",
               "ronin-ecosystem"
           ],
           "exchanges": []
       },
       "VIRTUAL": {
           "name": "Virtuals Protocol",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/VIRTUAL.png",
           "urls": {
               "website": "https://www.virtuals.io/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/virtual-protocol"
           },
           "sector_tags": [
               "ai-big-data",
               "ethereum-ecosystem",
               "metaverse",
               "generative-ai",
               "base-ecosystem",
               "ai-agents",
               "virtuals-protocol-ecosystem",
               "ai-agent-launchpad"
           ],
           "exchanges": []
       },
       "USDE": {
           "name": "Ethena USDe",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/USDE.png",
           "urls": {
               "website": "https://www.ethena.fi/",
               "technical_doc": "https://ethena-labs.gitbook.io/ethena-labs/",
               "cmc": "https://coinmarketcap.com/currencies/ethena-usde"
           },
           "sector_tags": [
               "stablecoin",
               "asset-backed-stablecoin",
               "ethereum-ecosystem",
               "binance-labs-portfolio",
               "dragonfly-capital-portfolio",
               "usd-stablecoin",
               "optimism-ecosystem",
               "linea-ecosystem",
               "eigenlayer-ecosystem",
               "kava-ecosystem",
               "metis-andromeda-ecosystem",
               "mantle-ecosystem",
               "manta-pacific-ecosystem",
               "scroll-ecosystem"
           ],
           "exchanges": []
       },
       "SUSDE,UNISWAP_V3,0X9D39A5DE30E57443BFF2A8307A4256C8797A3497": {
           "name": "Ethena Staked USDe",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SUSDE,UNISWAP_V3,0X9D39A5DE30E57443BFF2A8307A4256C8797A3497.png",
           "urls": {
               "website": "https://www.ethena.fi/",
               "technical_doc": "https://ethena-labs.gitbook.io/ethena-labs/",
               "cmc": "https://coinmarketcap.com/currencies/ethena-staked-usde"
           },
           "sector_tags": [
               "stablecoin",
               "asset-backed-stablecoin",
               "staking",
               "ethereum-ecosystem",
               "usd-stablecoin",
               "optimism-ecosystem",
               "rehypothecated-crypto",
               "kava-ecosystem",
               "metis-andromeda-ecosystem",
               "mantle-ecosystem",
               "manta-pacific-ecosystem",
               "scroll-ecosystem"
           ],
           "exchanges": []
       },
       "MASA": {
           "name": "Masa",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MASA.png",
           "urls": {
               "website": "https://www.masa.ai",
               "technical_doc": "https://github.com/masa-finance/whitepaper/blob/main/masa-whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/masa-network"
           },
           "sector_tags": [
               "ai-big-data",
               "data-provenance",
               "privacy",
               "zero-knowledge-proofs",
               "identity",
               "ethereum-ecosystem",
               "avalanche-ecosystem",
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "PORTAL": {
           "name": "Portal",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PORTAL.png",
           "urls": {
               "website": "https://www.portalgaming.com/",
               "technical_doc": "https://portalcoin.xyz/whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/portal-gaming"
           },
           "sector_tags": [
               "gaming",
               "ethereum-ecosystem",
               "binance-launchpool"
           ],
           "exchanges": []
       },
       "W": {
           "name": "Wormhole",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/W.png",
           "urls": {
               "website": "https://wormhole.com",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/wormhole"
           },
           "sector_tags": [
               "collectibles-nfts",
               "defi",
               "interoperability",
               "dao",
               "ethereum-ecosystem",
               "coinbase-ventures-portfolio",
               "governance",
               "solana-ecosystem",
               "arrington-xrp-capital-portfolio",
               "multicoin-capital-portfolio",
               "web3",
               "cross-chain",
               "optimism-ecosystem",
               "jump-crypto",
               "base-ecosystem",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "AEVO": {
           "name": "Aevo",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AEVO.png",
           "urls": {
               "website": "https://www.aevo.xyz/",
               "technical_doc": "https://docs.aevo.xyz",
               "cmc": "https://coinmarketcap.com/currencies/aevo"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "dex",
               "binance-launchpool",
               "layer-2"
           ],
           "exchanges": []
       },
       "BODEN": {
           "name": "Jeo Boden",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BODEN.png",
           "urls": {
               "website": "https://bodenonsol.xyz/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/jeo-boden"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "political-memes"
           ],
           "exchanges": []
       },
       "ZRC": {
           "name": "Zircuit",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ZRC.png",
           "urls": {
               "website": "https://www.zircuit.com/",
               "technical_doc": "https://docs.zircuit.com/",
               "cmc": "https://coinmarketcap.com/currencies/zircuit"
           },
           "sector_tags": [
               "zero-knowledge-proofs",
               "ethereum-ecosystem",
               "binance-labs-portfolio",
               "dragonfly-capital-portfolio",
               "pantera-capital-portfolio",
               "nomad-capital"
           ],
           "exchanges": []
       },
       "TREMP": {
           "name": "Doland Tremp",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TREMP.png",
           "urls": {
               "website": "https://www.tremp.xyz/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/doland-tremp"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "political-memes"
           ],
           "exchanges": []
       },
       "BRETT": {
           "name": "Brett (Based)",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BRETT.png",
           "urls": {
               "website": "https://www.basedbrett.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/based-brett"
           },
           "sector_tags": [
               "memes",
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "ZKJ": {
           "name": "Polyhedra Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ZKJ.png",
           "urls": {
               "website": "https://polyhedra.network/",
               "technical_doc": "https://dl.acm.org/doi/10.1145/3548606.3560652",
               "cmc": "https://coinmarketcap.com/currencies/polyhedra-network"
           },
           "sector_tags": [
               "interoperability",
               "ethereum-ecosystem",
               "polychain-capital-portfolio",
               "binance-labs-portfolio",
               "hashkey-capital-portfolio",
               "animoca-brands-portfolio",
               "okx-ventures-portfolio",
               "real-world-assets"
           ],
           "exchanges": []
       },
       "ETHFI": {
           "name": "ether.fi",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ETHFI.png",
           "urls": {
               "website": "https://www.ether.fi/",
               "technical_doc": "https://etherfi.gitbook.io/etherfi/ether.fi-whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/ether-fi-ethfi"
           },
           "sector_tags": [
               "defi",
               "staking",
               "ethereum-ecosystem",
               "binance-launchpool",
               "eigenlayer-ecosystem",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "IO": {
           "name": "io.net",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/IO.png",
           "urls": {
               "website": "https://io.net/",
               "technical_doc": "https://docs.io.net",
               "cmc": "https://coinmarketcap.com/currencies/io-net"
           },
           "sector_tags": [
               "ai-big-data",
               "distributed-computing",
               "binance-launchpool",
               "solana-ecosystem",
               "depin",
               "made-in-america"
           ],
           "exchanges": []
       },
       "BOME": {
           "name": "BOOK OF MEME",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BOME.png",
           "urls": {
               "website": "https://llwapirxnupqu7xw2fspfidormcfar7ek2yp65nu7k5opjwhdywq.arweave.net/WuwHojdtHwp-9tFk8qBuiwRQR-RWsP91tPq656bHHi0",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/book-of-meme"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem"
           ],
           "exchanges": []
       },
       "SLERF": {
           "name": "SLERF",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SLERF.png",
           "urls": {
               "website": "https://www.slerf.wtf/raids",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/slerf"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "presale-memes"
           ],
           "exchanges": []
       },
       "APU": {
           "name": "Apu Apustaja",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/APU.png",
           "urls": {
               "website": "https://apu.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/apu-apustaja"
           },
           "sector_tags": [
               "memes",
               "ethereum-ecosystem",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "GIGA": {
           "name": "Gigachad (gigachadsolana.com)",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/GIGA.png",
           "urls": {
               "website": "https://www.gigachadsolana.com",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/gigachad-meme"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem"
           ],
           "exchanges": []
       },
       "ATH": {
           "name": "Aethir",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ATH.png",
           "urls": {
               "website": "https://www.aethir.com/",
               "technical_doc": "https://aethir.gitbook.io/aethir/ljvx8d8ee4ElPliP31K1",
               "cmc": "https://coinmarketcap.com/currencies/aethir"
           },
           "sector_tags": [
               "ai-big-data",
               "distributed-computing",
               "ethereum-ecosystem",
               "depin",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "DEGEN": {
           "name": "Degen",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DEGEN.png",
           "urls": {
               "website": "https://www.degen.tips/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/degen-base"
           },
           "sector_tags": [
               "memes",
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "MEW": {
           "name": "cat in a dogs world",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MEW.png",
           "urls": {
               "website": "https://mew.xyz/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/mew"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "doggone-doggerel",
               "cat-themed",
               "animal-memes",
               "ip-memes",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "ENA": {
           "name": "Ethena",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ENA.png",
           "urls": {
               "website": "https://www.ethena.fi/",
               "technical_doc": "https://ethena-labs.gitbook.io/ethena-labs/",
               "cmc": "https://coinmarketcap.com/currencies/ethena"
           },
           "sector_tags": [
               "defi",
               "ethereum-ecosystem",
               "binance-labs-portfolio",
               "okx-ventures-portfolio",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "OMNI": {
           "name": "Omni Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/OMNI.png",
           "urls": {
               "website": "https://omni.network/",
               "technical_doc": "https://docs.omni.network/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/omni-network"
           },
           "sector_tags": [
               "interoperability",
               "ethereum-ecosystem",
               "binance-launchpool",
               "coinbase-ventures-portfolio",
               "rollups",
               "pantera-capital-portfolio",
               "cross-chain",
               "spartan-group",
               "layer-1",
               "jump-crypto",
               "made-in-america"
           ],
           "exchanges": []
       },
       "SAGA": {
           "name": "Saga",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SAGA.png",
           "urls": {
               "website": "https://www.saga.xyz/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/saga"
           },
           "sector_tags": [
               "ai-big-data",
               "gaming",
               "entertainment",
               "binance-launchpool",
               "layer-1"
           ],
           "exchanges": []
       },
       "ZEUS": {
           "name": "Zeus Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ZEUS.png",
           "urls": {
               "website": "https://zeusnetwork.xyz/",
               "technical_doc": "https://docs.zeusnetwork.xyz/",
               "cmc": "https://coinmarketcap.com/currencies/zeus-network"
           },
           "sector_tags": [
               "interoperability",
               "solana-ecosystem",
               "animoca-brands-portfolio",
               "cross-chain",
               "spartan-group",
               "okx-ventures-portfolio",
               "bitcoin-ecosystem"
           ],
           "exchanges": []
       },
       "TNSR": {
           "name": "Tensor",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TNSR.png",
           "urls": {
               "website": "https://www.tensor.foundation",
               "technical_doc": "https://docs.tensor.foundation/",
               "cmc": "https://coinmarketcap.com/currencies/tensor"
           },
           "sector_tags": [
               "collectibles-nfts",
               "solana-ecosystem"
           ],
           "exchanges": []
       },
       "EIGEN": {
           "name": "EigenLayer",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/EIGEN.png",
           "urls": {
               "website": "https://www.eigenlayer.xyz/",
               "technical_doc": "https://docs.eigenlayer.xyz/",
               "cmc": "https://coinmarketcap.com/currencies/eigenlayer"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "restaking",
               "cmc-crypto-yearbook-2024-2025",
               "made-in-america"
           ],
           "exchanges": []
       },
       "FOXY": {
           "name": "Foxy",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FOXY.png",
           "urls": {
               "website": "https://www.welikethefox.io",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/foxy"
           },
           "sector_tags": [
               "memes",
               "linea-ecosystem"
           ],
           "exchanges": []
       },
       "ZBCN": {
           "name": "Zebec Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ZBCN.png",
           "urls": {
               "website": "https://zebec.io/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/zebec-network"
           },
           "sector_tags": [
               "defi",
               "payments",
               "solana-ecosystem",
               "real-world-assets",
               "depin"
           ],
           "exchanges": []
       },
       "POWSCHE,RAYDIUM,8CKISHHJDHJV4LUOIRMLUHQG58CUKBYJRTCP4Z3MCXNF": {
           "name": "Powsche",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/POWSCHE,RAYDIUM,8CKISHHJDHJV4LUOIRMLUHQG58CUKBYJRTCP4Z3MCXNF.png",
           "urls": {
               "website": "https://powsche.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/powsche"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem"
           ],
           "exchanges": []
       },
       "PRCL": {
           "name": "Parcl",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PRCL.png",
           "urls": {
               "website": "https://www.parcl.co/",
               "technical_doc": "https://docs.parcl.co/",
               "cmc": "https://coinmarketcap.com/currencies/parcl"
           },
           "sector_tags": [
               "real-estate",
               "coinbase-ventures-portfolio",
               "governance",
               "solana-ecosystem",
               "dragonfly-capital-portfolio",
               "real-world-assets",
               "shima-capital",
               "made-in-america"
           ],
           "exchanges": []
       },
       "MERL": {
           "name": "Merlin Chain",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MERL.png",
           "urls": {
               "website": "https://merlinchain.io/",
               "technical_doc": "https://docs.merlinchain.io/merlin-docs",
               "cmc": "https://coinmarketcap.com/currencies/merlin-chain"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "layer-2",
               "bitcoin-ecosystem",
               "merlin-ecosystem"
           ],
           "exchanges": []
       },
       "BB": {
           "name": "BounceBit",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BB.png",
           "urls": {
               "website": "https://bouncebit.io/",
               "technical_doc": "https://docs.bouncebit.io/",
               "cmc": "https://coinmarketcap.com/currencies/bouncebit"
           },
           "sector_tags": [
               "pos",
               "defi",
               "ethereum-ecosystem",
               "binance-labs-portfolio",
               "cms-holdings-portfolio",
               "defiance-capital-portfolio",
               "okx-ventures-portfolio",
               "bitcoin-ecosystem",
               "nomad-capital",
               "restaking",
               "btcfi"
           ],
           "exchanges": []
       },
       "KARRAT": {
           "name": "KARRAT",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/KARRAT.png",
           "urls": {
               "website": "https://www.karratcoin.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/karrat"
           },
           "sector_tags": [
               "gaming",
               "ethereum-ecosystem",
               "governance"
           ],
           "exchanges": []
       },
       "REZ": {
           "name": "Renzo",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/REZ.png",
           "urls": {
               "website": "https://www.renzoprotocol.com/",
               "technical_doc": "https://docs.renzoprotocol.com/docs",
               "cmc": "https://coinmarketcap.com/currencies/renzo"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "binance-launchpool",
               "binance-labs-portfolio",
               "solana-ecosystem",
               "okx-ventures-portfolio",
               "restaking",
               "eigenlayer-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "MANEKI": {
           "name": "MANEKI",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MANEKI.png",
           "urls": {
               "website": "https://manekineko.world",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/maneki-coin"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "cat-themed"
           ],
           "exchanges": []
       },
       "MICHI": {
           "name": "michi (SOL)",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MICHI.png",
           "urls": {
               "website": "https://michisolana.org/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/michi"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "cat-themed",
               "animal-memes",
               "pump-fun-ecosystem"
           ],
           "exchanges": []
       },
       "ZENT": {
           "name": "Zentry",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ZENT.png",
           "urls": {
               "website": "https://zentry.com/",
               "technical_doc": "https://zentry.com/whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/zentry"
           },
           "sector_tags": [
               "gaming",
               "ethereum-ecosystem",
               "gaming-guild"
           ],
           "exchanges": []
       },
       "KMNO": {
           "name": "Kamino Finance",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/KMNO.png",
           "urls": {
               "website": "https://app.kamino.finance/",
               "technical_doc": "https://docs.kamino.finance/kamino-lend-litepaper",
               "cmc": "https://coinmarketcap.com/currencies/kamino-finance"
           },
           "sector_tags": [
               "defi",
               "solana-ecosystem",
               "lending-borowing"
           ],
           "exchanges": []
       },
       "MODE": {
           "name": "Mode",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MODE.png",
           "urls": {
               "website": "https://www.mode.network/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/mode"
           },
           "sector_tags": [
               "ai-big-data",
               "defai",
               "mode-ecosystem"
           ],
           "exchanges": []
       },
       "ZERO": {
           "name": "ZeroLend",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ZERO.png",
           "urls": {
               "website": "https://zerolend.xyz/",
               "technical_doc": "https://docs.zerolend.xyz/",
               "cmc": "https://coinmarketcap.com/currencies/zerolend"
           },
           "sector_tags": [
               "defi",
               "layer-2",
               "lending-borowing",
               "zksync-era-ecosystem",
               "linea-ecosystem"
           ],
           "exchanges": []
       },
       "DRIFT": {
           "name": "Drift",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DRIFT.png",
           "urls": {
               "website": "https://www.drift.trade/",
               "technical_doc": "https://docs.drift.trade/",
               "cmc": "https://coinmarketcap.com/currencies/drift"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "derivatives",
               "polychain-capital-portfolio",
               "governance",
               "solana-ecosystem",
               "multicoin-capital-portfolio",
               "jump-crypto",
               "defi"
           ],
           "exchanges": []
       },
       "DOP": {
           "name": "Data Ownership Protocol",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DOP.png",
           "urls": {
               "website": "https://dop.org",
               "technical_doc": "https://dop.org/DOP_Whitepaper_V2.1.pdf",
               "cmc": "https://coinmarketcap.com/currencies/data-ownership-protocol"
           },
           "sector_tags": [
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "MOTHER": {
           "name": "Mother Iggy",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MOTHER.png",
           "urls": {
               "website": "https://www.mother.fun/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/mother-iggy"
           },
           "sector_tags": [
               "adult",
               "memes",
               "solana-ecosystem",
               "dwf-labs-portfolio",
               "celebrity-memes",
               "pump-fun-ecosystem",
               "ip-memes"
           ],
           "exchanges": []
       },
       "TAIKO": {
           "name": "Taiko",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TAIKO.png",
           "urls": {
               "website": "https://taiko.xyz/",
               "technical_doc": "https://docs.taiko.xyz/",
               "cmc": "https://coinmarketcap.com/currencies/taiko"
           },
           "sector_tags": [
               "zero-knowledge-proofs",
               "ethereum-ecosystem",
               "layer-2",
               "rollups",
               "taiko-ecosystem"
           ],
           "exchanges": []
       },
       "MOCA": {
           "name": "Moca Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MOCA.png",
           "urls": {
               "website": "https://www.mocaverse.xyz/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/mocaverse"
           },
           "sector_tags": [
               "collectibles-nfts",
               "ethereum-ecosystem",
               "metaverse"
           ],
           "exchanges": []
       },
       "DBR": {
           "name": "deBridge",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DBR.png",
           "urls": {
               "website": "https://debridge.finance/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/debridge"
           },
           "sector_tags": [
               "interoperability",
               "governance",
               "solana-ecosystem",
               "cross-chain",
               "intent"
           ],
           "exchanges": []
       },
       "PIRATE": {
           "name": "Pirate Nation",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PIRATE.png",
           "urls": {
               "website": "https://piratenation.game/",
               "technical_doc": "https://www.piratenation.foundation/PN%20Foundation%20Litepaper_v1.1.02_June2024.pdf",
               "cmc": "https://coinmarketcap.com/currencies/pirate-nation"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "ethereum-ecosystem",
               "a16z-portfolio",
               "made-in-america"
           ],
           "exchanges": []
       },
       "COOKIE": {
           "name": "Cookie DAO",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/COOKIE.png",
           "urls": {
               "website": "https://www.cookie.fun/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/cookie"
           },
           "sector_tags": [
               "marketing",
               "ai-big-data",
               "governance",
               "animoca-brands-portfolio",
               "spartan-group",
               "base-ecosystem",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "RETARDIO": {
           "name": "RETARDIO",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/RETARDIO.png",
           "urls": {
               "website": "https://retardio.xyz",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/retardio"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "cmc-community-vote-winners"
           ],
           "exchanges": []
       },
       "HAWKTUAH,RAYDIUM,4GFE6MBDORSY5BLBIUMRGETR6PZCJYFXMDM5EHSGPUMP": {
           "name": "Hawk Tuah (hawktuah.vip)",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/HAWKTUAH,RAYDIUM,4GFE6MBDORSY5BLBIUMRGETR6PZCJYFXMDM5EHSGPUMP.png",
           "urls": {
               "website": "https://hawktuah.vip/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/hawk-tuah"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem"
           ],
           "exchanges": []
       },
       "XION": {
           "name": "XION",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/XION.png",
           "urls": {
               "website": "https://xion.burnt.com",
               "technical_doc": "https://xion.burnt.com/whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/xion"
           },
           "sector_tags": [
               "platform",
               "multicoin-capital-portfolio",
               "animoca-brands-portfolio",
               "circle-ventures-portfolio",
               "layer-1",
               "made-in-america"
           ],
           "exchanges": []
       },
       "MAD,RAYDIUM,MADHPJRN6BD8T78RSY7NUSUNWWA2HU8BYPOBZPRHBHV": {
           "name": "MAD",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MAD,RAYDIUM,MADHPJRN6BD8T78RSY7NUSUNWWA2HU8BYPOBZPRHBHV.png",
           "urls": {
               "website": "https://www.madcoin.vip/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/mad-token"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem"
           ],
           "exchanges": []
       },
       "G": {
           "name": "Gravity",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/G.png",
           "urls": {
               "website": "https://gravity.xyz",
               "technical_doc": "https://docs.gravity.xyz/",
               "cmc": "https://coinmarketcap.com/currencies/gravity-token"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "binance-launchpool",
               "spartan-group",
               "dwf-labs-portfolio",
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "ALEO": {
           "name": "Aleo",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ALEO.png",
           "urls": {
               "website": "https://aleo.org/",
               "technical_doc": "https://developer.aleo.org/guides/introduction/getting_started/",
               "cmc": "https://coinmarketcap.com/currencies/aleo"
           },
           "sector_tags": [
               "zero-knowledge-proofs",
               "made-in-america"
           ],
           "exchanges": []
       },
       "HMSTR": {
           "name": "Hamster Kombat",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/HMSTR.png",
           "urls": {
               "website": "https://hamsterkombat.io/",
               "technical_doc": "https://hamsterkombatgame.io/docs/HK_WP_03.pdf",
               "cmc": "https://coinmarketcap.com/currencies/hamster-kombat"
           },
           "sector_tags": [
               "gaming",
               "toncoin-ecosystem",
               "tap-to-earn"
           ],
           "exchanges": []
       },
       "ME": {
           "name": "Magic Eden",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ME.png",
           "urls": {
               "website": "https://mefoundation.com/",
               "technical_doc": "https://blog.mefoundation.com/blog/me-tokenomics/",
               "cmc": "https://coinmarketcap.com/currencies/magiceden"
           },
           "sector_tags": [
               "marketplace",
               "collectibles-nfts",
               "wallet",
               "dapp",
               "solana-ecosystem",
               "bitcoin-ecosystem",
               "runes"
           ],
           "exchanges": []
       },
       "UXLINK": {
           "name": "UXLINK",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/UXLINK.png",
           "urls": {
               "website": "https://www.uxlink.io",
               "technical_doc": "https://docs.uxlink.io/uxuy-labs-api/whitepaper/white-paper",
               "cmc": "https://coinmarketcap.com/currencies/uxlink"
           },
           "sector_tags": [
               "social-token",
               "arbitrum-ecosystem"
           ],
           "exchanges": []
       },
       "CLOUD": {
           "name": "Cloud",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CLOUD.png",
           "urls": {
               "website": "https://www.sanctum.so/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/sanctum-cloud"
           },
           "sector_tags": [
               "governance",
               "solana-ecosystem"
           ],
           "exchanges": []
       },
       "PUFFER": {
           "name": "Puffer",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PUFFER.png",
           "urls": {
               "website": "https://www.puffer.fi/",
               "technical_doc": "https://docs.puffer.fi/",
               "cmc": "https://coinmarketcap.com/currencies/puffer"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "restaking",
               "eigenlayer-ecosystem"
           ],
           "exchanges": []
       },
       "AVAIL": {
           "name": "Avail",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AVAIL.png",
           "urls": {
               "website": "https://www.availproject.org/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/avail"
           },
           "sector_tags": [
               "zero-knowledge-proofs",
               "ethereum-ecosystem",
               "polkadot-ecosystem",
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "MOVE": {
           "name": "Movement",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MOVE.png",
           "urls": {
               "website": "https://www.movementnetwork.xyz",
               "technical_doc": "https://docs.movementnetwork.xyz/",
               "cmc": "https://coinmarketcap.com/currencies/movement"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "binance-labs-portfolio",
               "layer-1",
               "move-vm",
               "made-in-america"
           ],
           "exchanges": []
       },
       "NEIRO": {
           "name": "Neiro Ethereum",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/NEIRO.png",
           "urls": {
               "website": "https://Neirocoin.com",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/neiro-eth"
           },
           "sector_tags": [
               "memes",
               "ethereum-ecosystem",
               "doggone-doggerel"
           ],
           "exchanges": []
       },
       "L3": {
           "name": "Layer3",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/L3.png",
           "urls": {
               "website": "https://layer3.xyz",
               "technical_doc": "https://docs.layer3foundation.org/tokenomics",
               "cmc": "https://coinmarketcap.com/currencies/layer3-xyz"
           },
           "sector_tags": [
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "DINERO,UNISWAP_V3,0X6DF0E641FC9847C0C6FDE39BE6253045440C14D3": {
           "name": "Dinero",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DINERO,UNISWAP_V3,0X6DF0E641FC9847C0C6FDE39BE6253045440C14D3.png",
           "urls": {
               "website": "https://dinero.xyz/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/dinero-xyz"
           },
           "sector_tags": [
               "staking",
               "ethereum-ecosystem",
               "defi-2"
           ],
           "exchanges": []
       },
       "MAX": {
           "name": "Matr1x",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MAX.png",
           "urls": {
               "website": "matr1x.io",
               "technical_doc": "https://matr1x.gitbook.io/matr1x-whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/matr1x"
           },
           "sector_tags": [
               "gaming",
               "entertainment",
               "ethereum-ecosystem",
               "hashkey-capital-portfolio",
               "polygon-ecosystem",
               "animoca-brands-portfolio",
               "okx-ventures-portfolio"
           ],
           "exchanges": []
       },
       "CIG,RAYDIUM,EXRQAUXS967FDKYMNXO4EQZEQIHEUBSTBCXPXXWPUMP": {
           "name": "cig",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CIG,RAYDIUM,EXRQAUXS967FDKYMNXO4EQZEQIHEUBSTBCXPXXWPUMP.png",
           "urls": {
               "website": "https://www.cigonsol.com",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/cig"
           },
           "sector_tags": [
               "solana-ecosystem"
           ],
           "exchanges": []
       },
       "S": {
           "name": "Sonic (prev. FTM)",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/S.png",
           "urls": {
               "website": "https://www.soniclabs.com/",
               "technical_doc": "https://www.soniclabs.com/litepaper",
               "cmc": "https://coinmarketcap.com/currencies/sonic"
           },
           "sector_tags": [
               "staking",
               "layer-1"
           ],
           "exchanges": []
       },
       "DOGS": {
           "name": "DOGS",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DOGS.png",
           "urls": {
               "website": "https://t.me/dogshouse_bot",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/dogs"
           },
           "sector_tags": [
               "memes",
               "doggone-doggerel",
               "telegram-bot",
               "toncoin-ecosystem",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "SUNDOG": {
           "name": "SUNDOG",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SUNDOG.png",
           "urls": {
               "website": "https://www.sundog.meme",
               "technical_doc": "https://www.sundog.meme",
               "cmc": "https://coinmarketcap.com/currencies/sundog"
           },
           "sector_tags": [
               "memes",
               "tron-ecosystem",
               "tron-memes",
               "sun-pump-ecosystem",
               "tron20-ecosystem"
           ],
           "exchanges": []
       },
       "CAT": {
           "name": "Simon's Cat",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CAT.png",
           "urls": {
               "website": "https://www.simons.cat/",
               "technical_doc": "https://docs.simons.cat",
               "cmc": "https://coinmarketcap.com/currencies/simonscat"
           },
           "sector_tags": [
               "binance-smart-chain",
               "solana-ecosystem",
               "cat-themed",
               "tokenfi-launchpad",
               "ip-memes"
           ],
           "exchanges": []
       },
       "ORDER": {
           "name": "Orderly Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ORDER.png",
           "urls": {
               "website": "https://orderly.network/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/orderly-network"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "layer-2",
               "polygon-ecosystem",
               "optimism-ecosystem",
               "base-ecosystem",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "KAIA": {
           "name": "Kaia",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/KAIA.png",
           "urls": {
               "website": "https://www.kaia.io/",
               "technical_doc": "https://docs.kaia.io/kaiatech/kaia-white-paper/",
               "cmc": "https://coinmarketcap.com/currencies/kaia"
           },
           "sector_tags": [
               "platform",
               "enterprise-solutions",
               "layer-1",
               "klaytn-ecosystem"
           ],
           "exchanges": []
       },
       "SPEC": {
           "name": "Spectral",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SPEC.png",
           "urls": {
               "website": "https://www.spectrallabs.xyz/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/spectral"
           },
           "sector_tags": [
               "ai-big-data",
               "ethereum-ecosystem",
               "base-ecosystem",
               "ai-agents",
               "ai-agent-launchpad"
           ],
           "exchanges": []
       },
       "NS": {
           "name": "Sui Name Service",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/NS.png",
           "urls": {
               "website": "https://www.suins.io",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/sui-name-service"
           },
           "sector_tags": [
               "sui-ecosystem"
           ],
           "exchanges": []
       },
       "GRASS": {
           "name": "Grass",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/GRASS.png",
           "urls": {
               "website": "https://www.getgrass.io/",
               "technical_doc": "https://grass-foundation.gitbook.io/grass-docs",
               "cmc": "https://coinmarketcap.com/currencies/grass"
           },
           "sector_tags": [
               "ai-big-data",
               "solana-ecosystem",
               "depin"
           ],
           "exchanges": []
       },
       "CATI": {
           "name": "Catizen",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CATI.png",
           "urls": {
               "website": "https://catizen.ai/",
               "technical_doc": "https://docs.catizen.ai/",
               "cmc": "https://coinmarketcap.com/currencies/catizen"
           },
           "sector_tags": [
               "gaming",
               "toncoin-ecosystem",
               "tap-to-earn"
           ],
           "exchanges": []
       },
       "MOODENG": {
           "name": "Moo Deng (moodengsol.com)",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MOODENG.png",
           "urls": {
               "website": "https://www.moodengsol.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/moo-deng-solana"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "animal-memes",
               "pump-fun-ecosystem"
           ],
           "exchanges": []
       },
       "X": {
           "name": "X Empire",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/X.png",
           "urls": {
               "website": "https://xempire.io/",
               "technical_doc": "https://x.com/xempiregame/status/1835026441349800357?lang=en",
               "cmc": "https://coinmarketcap.com/currencies/x-empire"
           },
           "sector_tags": [
               "gaming",
               "dwf-labs-portfolio",
               "toncoin-ecosystem",
               "tap-to-earn"
           ],
           "exchanges": []
       },
       "MAJOR": {
           "name": "Major",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MAJOR.png",
           "urls": {
               "website": "https://major.bot/",
               "technical_doc": "https://drive.google.com/file/d/1KoxVr_I4QLnTOPciZeVHcYlHVBXbxY-3/view",
               "cmc": "https://coinmarketcap.com/currencies/major"
           },
           "sector_tags": [
               "gaming",
               "telegram-bot",
               "toncoin-ecosystem",
               "tap-to-earn"
           ],
           "exchanges": []
       },
       "HIPPO": {
           "name": "sudeng",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/HIPPO.png",
           "urls": {
               "website": "https://www.hippocto.meme/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/sudeng"
           },
           "sector_tags": [
               "memes",
               "sui-ecosystem",
               "animal-memes"
           ],
           "exchanges": []
       },
       "FWOG": {
           "name": "Fwog (SOL)",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FWOG.png",
           "urls": {
               "website": "https://fwogsol.xyz/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/fwog-solana"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "cmc-community-vote-winners",
               "pump-fun-ecosystem",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "XRP": {
           "name": "XRP",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/XRP.png",
           "urls": {
               "website": "https://xrpl.org/",
               "technical_doc": "https://ripple.com/files/ripple_consensus_whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/xrp"
           },
           "sector_tags": [
               "medium-of-exchange",
               "enterprise-solutions",
               "xrp-ecosystem",
               "arrington-xrp-capital-portfolio",
               "galaxy-digital-portfolio",
               "a16z-portfolio",
               "pantera-capital-portfolio",
               "ftx-bankruptcy-estate",
               "2017-2018-alt-season",
               "klaytn-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "XLM": {
           "name": "Stellar",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/XLM.png",
           "urls": {
               "website": "https://www.stellar.org",
               "technical_doc": "https://www.stellar.org/papers/stellar-consensus-protocol.pdf",
               "cmc": "https://coinmarketcap.com/currencies/stellar"
           },
           "sector_tags": [
               "medium-of-exchange",
               "enterprise-solutions",
               "decentralized-exchange-dex-token",
               "smart-contracts",
               "hashkey-capital-portfolio",
               "2017-2018-alt-season",
               "made-in-america",
               "defi"
           ],
           "exchanges": []
       },
       "BSV": {
           "name": "Bitcoin SV",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BSV.png",
           "urls": {
               "website": "https://www.bsvblockchain.org/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/bitcoin-sv"
           },
           "sector_tags": [
               "mineable",
               "pow",
               "sha-256",
               "medium-of-exchange",
               "store-of-value",
               "state-channel",
               "bitcoin-ecosystem",
               "2017-2018-alt-season"
           ],
           "exchanges": []
       },
       "CRO": {
           "name": "Cronos",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CRO.png",
           "urls": {
               "website": "https://cronos.org/",
               "technical_doc": "https://whitepaper.cronos.org/",
               "cmc": "https://coinmarketcap.com/currencies/cronos"
           },
           "sector_tags": [
               "medium-of-exchange",
               "cosmos-ecosystem",
               "centralized-exchange",
               "mobile",
               "payments",
               "ethereum-ecosystem",
               "solana-ecosystem",
               "injective-ecosystem",
               "osmosis-ecosystem",
               "layer-1"
           ],
           "exchanges": []
       },
       "AERGO": {
           "name": "Aergo",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AERGO.png",
           "urls": {
               "website": "https://www.aergo.io/",
               "technical_doc": "https://paper.aergo.io/AERGO_Whitepaper_v5.2.pdf",
               "cmc": "https://coinmarketcap.com/currencies/aergo"
           },
           "sector_tags": [
               "platform",
               "enterprise-solutions",
               "smart-contracts",
               "ethereum-ecosystem",
               "bnb-chain-ecosystem"
           ],
           "exchanges": []
       },
       "LPT": {
           "name": "Livepeer",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LPT.png",
           "urls": {
               "website": "https://livepeer.org/",
               "technical_doc": "https://github.com/livepeer/wiki/blob/master/WHITEPAPER.md",
               "cmc": "https://coinmarketcap.com/currencies/livepeer"
           },
           "sector_tags": [
               "media",
               "ai-big-data",
               "distributed-computing",
               "video",
               "ethereum-ecosystem",
               "coinfund-portfolio",
               "dcg-portfolio",
               "pantera-capital-portfolio",
               "multicoin-capital-portfolio",
               "web3",
               "arbitrum-ecosystem",
               "harmony-ecosystem",
               "generative-ai",
               "depin"
           ],
           "exchanges": []
       },
       "WBTC": {
           "name": "Wrapped Bitcoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/WBTC.png",
           "urls": {
               "website": "https://wbtc.network",
               "technical_doc": "https://www.wbtc.network/assets/wrapped-tokens-whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/wrapped-bitcoin"
           },
           "sector_tags": [
               "medium-of-exchange",
               "defi",
               "ethereum-ecosystem",
               "waves-ecosystem",
               "solana-ecosystem",
               "wrapped-tokens",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "near-protocol-ecosystem",
               "arbitrum-ecosystem",
               "iotex-ecosystem",
               "zilliqa-ecosystem",
               "harmony-ecosystem",
               "moonriver-ecosystem",
               "moonbeam-ecosystem",
               "everscale-ecosystem",
               "velas-ecosystem",
               "ethereum-pow-ecosystem",
               "optimism-ecosystem",
               "osmosis-ecosystem",
               "bitcoin-ecosystem",
               "ftx-bankruptcy-estate",
               "zksync-era-ecosystem",
               "toncoin-ecosystem",
               "rehypothecated-crypto",
               "viction-ecosystem",
               "conflux-ecosystem",
               "kcc-ecosystem",
               "telos-ecosystem",
               "aurora-ecosystem",
               "metis-andromeda-ecosystem",
               "milkomeda-ecosystem",
               "stacks-ecosystem",
               "starknet-ecosystem",
               "merlin-ecosystem",
               "b\u00b2-network-ecosystem"
           ],
           "exchanges": []
       },
       "ANKR": {
           "name": "Ankr",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ANKR.png",
           "urls": {
               "website": "https://www.ankr.com/",
               "technical_doc": "https://www.ankr.com/ankr-whitepaper-2.0.pdf",
               "cmc": "https://coinmarketcap.com/currencies/ankr"
           },
           "sector_tags": [
               "platform",
               "cosmos-ecosystem",
               "enterprise-solutions",
               "distributed-computing",
               "defi",
               "filesharing",
               "staking",
               "ethereum-ecosystem",
               "substrate",
               "solana-ecosystem",
               "pantera-capital-portfolio",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "celo-ecosystem",
               "injective-ecosystem",
               "bnb-chain-ecosystem",
               "liquid-staking-derivatives",
               "optimism-ecosystem",
               "rollups-as-a-service",
               "blast-ecosystem",
               "depin",
               "polygon-zkevm-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "ATOM": {
           "name": "Cosmos",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ATOM.png",
           "urls": {
               "website": "https://cosmos.network/",
               "technical_doc": "https://cosmos.network/resources/whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/cosmos"
           },
           "sector_tags": [
               "platform",
               "cosmos-ecosystem",
               "content-creation",
               "interoperability",
               "ethereum-ecosystem",
               "polychain-capital-portfolio",
               "dragonfly-capital-portfolio",
               "hashkey-capital-portfolio",
               "1confirmation-portfolio",
               "paradigm-portfolio",
               "exnetwork-capital-portfolio",
               "polygon-ecosystem",
               "injective-ecosystem",
               "canto-ecosystem",
               "osmosis-ecosystem",
               "layer-1",
               "alleged-sec-securities"
           ],
           "exchanges": []
       },
       "CELR": {
           "name": "Celer Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CELR.png",
           "urls": {
               "website": "https://www.celer.network/#",
               "technical_doc": "https://im-docs.celer.network/developer/celer-im-overview",
               "cmc": "https://coinmarketcap.com/currencies/celer-network"
           },
           "sector_tags": [
               "platform",
               "enterprise-solutions",
               "zero-knowledge-proofs",
               "interoperability",
               "scaling",
               "state-channel",
               "ethereum-ecosystem",
               "substrate",
               "binance-launchpad",
               "arrington-xrp-capital-portfolio",
               "pantera-capital-portfolio",
               "arbitrum-ecosystem",
               "cross-chain",
               "bnb-chain-ecosystem",
               "oasis-ecosystem",
               "moonbeam-ecosystem",
               "sei-ecosystem",
               "metis-andromeda-ecosystem"
           ],
           "exchanges": []
       },
       "TFUEL": {
           "name": "Theta Fuel",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TFUEL.png",
           "urls": {
               "website": "https://www.thetatoken.org",
               "technical_doc": "https://s3.us-east-2.amazonaws.com/assets.thetatoken.org/Theta-white-paper-latest.pdf?v=1553657855.509",
               "cmc": "https://coinmarketcap.com/currencies/theta-fuel"
           },
           "sector_tags": [
               "media",
               "vr-ar",
               "distributed-computing",
               "content-creation",
               "defi",
               "entertainment",
               "payments",
               "sharing-economy",
               "smart-contracts",
               "depin",
               "made-in-america"
           ],
           "exchanges": []
       },
       "ORBS": {
           "name": "Orbs",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ORBS.png",
           "urls": {
               "website": "https://www.orbs.com/",
               "technical_doc": "https://www.orbs.com/white-papers",
               "cmc": "https://coinmarketcap.com/currencies/orbs"
           },
           "sector_tags": [
               "services",
               "defi",
               "ethereum-ecosystem",
               "solana-ecosystem",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "harmony-ecosystem",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "IDEX": {
           "name": "IDEX",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/IDEX.png",
           "urls": {
               "website": "https://idex.io/",
               "technical_doc": "https://docs.idex.io/",
               "cmc": "https://coinmarketcap.com/currencies/idex"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "defi",
               "ethereum-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "ONE": {
           "name": "Harmony",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ONE.png",
           "urls": {
               "website": "https://www.harmony.one/",
               "technical_doc": "https://harmony.one/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/harmony"
           },
           "sector_tags": [
               "platform",
               "enterprise-solutions",
               "scaling",
               "smart-contracts",
               "binance-launchpad",
               "binance-labs-portfolio",
               "hashkey-capital-portfolio",
               "harmony-ecosystem",
               "injective-ecosystem",
               "bnb-chain-ecosystem",
               "layer-1"
           ],
           "exchanges": []
       },
       "RSR": {
           "name": "Reserve Rights",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/RSR.png",
           "urls": {
               "website": "https://reserve.org/",
               "technical_doc": "https://reserve.org/protocol/introduction/",
               "cmc": "https://coinmarketcap.com/currencies/reserve-rights"
           },
           "sector_tags": [
               "store-of-value",
               "defi",
               "ethereum-ecosystem",
               "coinbase-ventures-portfolio",
               "dcg-portfolio",
               "real-world-assets",
               "base-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "CHR": {
           "name": "Chromia",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CHR.png",
           "urls": {
               "website": "https://chromia.com/",
               "technical_doc": "https://chromia.com/documents/Chromia-_-Platform-white-paper2019.pdf",
               "cmc": "https://coinmarketcap.com/currencies/chromia"
           },
           "sector_tags": [
               "platform",
               "ai-big-data",
               "enterprise-solutions",
               "defi",
               "ethereum-ecosystem",
               "metaverse",
               "chromia-ecosystem",
               "web3",
               "bnb-chain-ecosystem",
               "real-world-assets",
               "layer-1",
               "modular-blockchain"
           ],
           "exchanges": []
       },
       "COTI": {
           "name": "COTI",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/COTI.png",
           "urls": {
               "website": "https://coti.io/",
               "technical_doc": "https://coti.io/files/COTI-technical-whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/coti"
           },
           "sector_tags": [
               "defi",
               "ethereum-ecosystem",
               "fantom-ecosystem",
               "cardano-ecosystem",
               "arbitrum-ecosystem",
               "cardano",
               "bnb-chain-ecosystem",
               "dwf-labs-portfolio",
               "alleged-sec-securities"
           ],
           "exchanges": []
       },
       "STPT": {
           "name": "STP",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/STPT.png",
           "urls": {
               "website": "https://stp.network/",
               "technical_doc": "https://www.stp.network/Verse%20Network%20WP.pdf",
               "cmc": "https://coinmarketcap.com/currencies/standard-tokenization-protocol"
           },
           "sector_tags": [
               "defi",
               "payments",
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "ALGO": {
           "name": "Algorand",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ALGO.png",
           "urls": {
               "website": "http://algorand.foundation",
               "technical_doc": "https://developer.algorand.org/",
               "cmc": "https://coinmarketcap.com/currencies/algorand"
           },
           "sector_tags": [
               "pos",
               "platform",
               "research",
               "smart-contracts",
               "algorand-ecosystem",
               "arrington-xrp-capital-portfolio",
               "kenetic-capital-portfolio",
               "usv-portfolio",
               "multicoin-capital-portfolio",
               "exnetwork-capital-portfolio",
               "real-world-assets",
               "layer-1",
               "dwf-labs-portfolio",
               "alleged-sec-securities",
               "made-in-america"
           ],
           "exchanges": []
       },
       "AMPL": {
           "name": "Ampleforth",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AMPL.png",
           "urls": {
               "website": "https://www.ampleforth.org/",
               "technical_doc": "https://docs.ampleforth.org/",
               "cmc": "https://coinmarketcap.com/currencies/ampleforth"
           },
           "sector_tags": [
               "defi",
               "algorithmic-stablecoin",
               "ethereum-ecosystem",
               "rebase",
               "avalanche-ecosystem",
               "arrington-xrp-capital-portfolio",
               "alameda-research-portfolio",
               "pantera-capital-portfolio",
               "spartan-group",
               "meter-ecosystem"
           ],
           "exchanges": []
       },
       "CHZ": {
           "name": "Chiliz",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CHZ.png",
           "urls": {
               "website": "https://www.chiliz.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/chiliz"
           },
           "sector_tags": [
               "sports",
               "collectibles-nfts",
               "content-creation",
               "payments",
               "ethereum-ecosystem",
               "layer-1",
               "alleged-sec-securities"
           ],
           "exchanges": []
       },
       "DUSK": {
           "name": "Dusk",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DUSK.png",
           "urls": {
               "website": "https://www.dusk.network",
               "technical_doc": "https://dusk-cms.ams3.digitaloceanspaces.com/Dusk_Whitepaper_2024_4db72f92a1.pdf",
               "cmc": "https://coinmarketcap.com/currencies/dusk"
           },
           "sector_tags": [
               "pos",
               "marketplace",
               "enterprise-solutions",
               "privacy",
               "zero-knowledge-proofs",
               "smart-contracts",
               "ethereum-ecosystem",
               "bnb-chain-ecosystem",
               "real-world-assets",
               "layer-1"
           ],
           "exchanges": []
       },
       "PROM": {
           "name": "Prom",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PROM.png",
           "urls": {
               "website": "https://prom.io/",
               "technical_doc": "https://prom.io/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/prom"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "bnb-chain-ecosystem",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "RUNE": {
           "name": "THORChain",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/RUNE.png",
           "urls": {
               "website": "https://thorchain.org/",
               "technical_doc": "https://github.com/thorchain/Resources/blob/master/Whitepapers/THORChain-Whitepaper-May2020.pdf",
               "cmc": "https://coinmarketcap.com/currencies/thorchain"
           },
           "sector_tags": [
               "cosmos-ecosystem",
               "decentralized-exchange-dex-token",
               "defi",
               "dex",
               "multicoin-capital-portfolio",
               "exnetwork-capital-portfolio",
               "injective-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "LUNC": {
           "name": "Terra Classic",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LUNC.png",
           "urls": {
               "website": "https://commonwealth.im/terra-luna-classic-lunc",
               "technical_doc": "https://classic-docs.terra.money",
               "cmc": "https://coinmarketcap.com/currencies/terra-luna"
           },
           "sector_tags": [
               "cosmos-ecosystem",
               "store-of-value",
               "defi",
               "payments",
               "ethereum-ecosystem",
               "coinbase-ventures-portfolio",
               "binance-labs-portfolio",
               "solana-ecosystem",
               "arrington-xrp-capital-portfolio",
               "hashkey-capital-portfolio",
               "kenetic-capital-portfolio",
               "huobi-capital-portfolio",
               "pantera-capital-portfolio",
               "polygon-ecosystem",
               "terra-ecosystem",
               "injective-ecosystem",
               "osmosis-ecosystem",
               "alleged-sec-securities",
               "secret-ecosystem"
           ],
           "exchanges": []
       },
       "FTT": {
           "name": "FTX Token",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FTT.png",
           "urls": {
               "website": null,
               "technical_doc": "https://docs.google.com/document/d/1u5MOkENoWP8PGcjuoKqRkNP5Gl1LLRB9JvAHwffQ7ec/view",
               "cmc": "https://coinmarketcap.com/currencies/ftx-token"
           },
           "sector_tags": [
               "marketplace",
               "centralized-exchange",
               "derivatives",
               "ethereum-ecosystem",
               "solana-ecosystem",
               "cms-holdings-portfolio",
               "kenetic-capital-portfolio",
               "alameda-research-portfolio",
               "pantera-capital-portfolio",
               "exnetwork-capital-portfolio",
               "bnb-chain-ecosystem",
               "ftx-bankruptcy-estate",
               "alleged-sec-securities",
               "celsius-bankruptcy-estate"
           ],
           "exchanges": []
       },
       "WIN": {
           "name": "WINkLink",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/WIN.png",
           "urls": {
               "website": "https://winklink.org/",
               "technical_doc": "https://winklink.org/WinkLink%20white%20paper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/wink"
           },
           "sector_tags": [
               "oracles",
               "tron-ecosystem",
               "binance-launchpad",
               "binance-labs-portfolio",
               "bnb-chain-ecosystem",
               "tron20-ecosystem"
           ],
           "exchanges": []
       },
       "SXP": {
           "name": "Solar",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SXP.png",
           "urls": {
               "website": "https://solar.org",
               "technical_doc": "https://docs.solar.org/project/whitepaper/",
               "cmc": "https://coinmarketcap.com/currencies/sxp"
           },
           "sector_tags": [
               "medium-of-exchange",
               "defi",
               "payments",
               "wallet",
               "binance-labs-portfolio",
               "alameda-research-portfolio",
               "bnb-chain-ecosystem"
           ],
           "exchanges": []
       },
       "FLOW": {
           "name": "Flow",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FLOW.png",
           "urls": {
               "website": "https://flow.com",
               "technical_doc": "https://www.flow.com/technical-paper",
               "cmc": "https://coinmarketcap.com/currencies/flow"
           },
           "sector_tags": [
               "platform",
               "collectibles-nfts",
               "staking",
               "ethereum-ecosystem",
               "coinbase-ventures-portfolio",
               "coinfund-portfolio",
               "dcg-portfolio",
               "ledgerprime-portfolio",
               "a16z-portfolio",
               "animoca-brands-portfolio",
               "layer-1",
               "alleged-sec-securities",
               "flow-ecosystem"
           ],
           "exchanges": []
       },
       "HBAR": {
           "name": "Hedera",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/HBAR.png",
           "urls": {
               "website": "https://www.hedera.com/",
               "technical_doc": "https://www.hedera.com/papers",
               "cmc": "https://coinmarketcap.com/currencies/hedera"
           },
           "sector_tags": [
               "dag",
               "marketplace",
               "enterprise-solutions",
               "defi",
               "payments",
               "dcg-portfolio",
               "hedera-hashgraph-ecosystem",
               "real-world-assets",
               "layer-1",
               "made-in-america"
           ],
           "exchanges": []
       },
       "TLOS": {
           "name": "Telos",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TLOS.png",
           "urls": {
               "website": "https://telos.net/",
               "technical_doc": "https://telos.net/whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/telos"
           },
           "sector_tags": [
               "medium-of-exchange",
               "services",
               "enterprise-solutions",
               "collectibles-nfts",
               "defi",
               "zero-knowledge-proofs",
               "smart-contracts",
               "ethereum-ecosystem",
               "polygon-ecosystem",
               "bnb-chain-ecosystem",
               "layer-1"
           ],
           "exchanges": []
       },
       "BAND": {
           "name": "Band Protocol",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BAND.png",
           "urls": {
               "website": "https://bandprotocol.com/",
               "technical_doc": "https://bandprotocol.com/whitepaper-3.0.1.pdf",
               "cmc": "https://coinmarketcap.com/currencies/band-protocol"
           },
           "sector_tags": [
               "cosmos-ecosystem",
               "defi",
               "oracles",
               "ethereum-ecosystem",
               "binance-chain",
               "binance-launchpad",
               "binance-labs-portfolio",
               "solana-ecosystem",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "web3",
               "near-protocol-ecosystem",
               "spartan-group",
               "injective-ecosystem",
               "bnb-chain-ecosystem",
               "gnosis-chain-ecosystem"
           ],
           "exchanges": []
       },
       "PAXG": {
           "name": "PAX Gold",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PAXG.png",
           "urls": {
               "website": "https://www.paxos.com/paxgold/",
               "technical_doc": "https://www.paxos.com/pax-gold-whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/pax-gold"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "solana-ecosystem",
               "bnb-chain-ecosystem",
               "tokenized-gold"
           ],
           "exchanges": []
       },
       "KAVA": {
           "name": "Kava",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/KAVA.png",
           "urls": {
               "website": "https://www.kava.io/",
               "technical_doc": "https://docsend.com/view/gwbwpc3",
               "cmc": "https://coinmarketcap.com/currencies/kava"
           },
           "sector_tags": [
               "platform",
               "cosmos-ecosystem",
               "defi",
               "ethereum-ecosystem",
               "binance-launchpad",
               "binance-labs-portfolio",
               "lending-borowing",
               "arrington-xrp-capital-portfolio",
               "framework-ventures-portfolio",
               "hashkey-capital-portfolio",
               "polygon-ecosystem",
               "web3",
               "injective-ecosystem",
               "bnb-chain-ecosystem",
               "osmosis-ecosystem",
               "layer-1",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "STX": {
           "name": "Stacks",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/STX.png",
           "urls": {
               "website": "https://stacks.co",
               "technical_doc": "https://gaia.blockstack.org/hub/1AxyPunHHAHiEffXWESKfbvmBpGQv138Fp/stacks.pdf",
               "cmc": "https://coinmarketcap.com/currencies/stacks"
           },
           "sector_tags": [
               "mineable",
               "platform",
               "collectibles-nfts",
               "defi",
               "smart-contracts",
               "metaverse",
               "arrington-xrp-capital-portfolio",
               "blockchain-capital-portfolio",
               "dcg-portfolio",
               "fabric-ventures-portfolio",
               "hashkey-capital-portfolio",
               "huobi-capital-portfolio",
               "usv-portfolio",
               "web3",
               "injective-ecosystem",
               "bitcoin-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "DAI": {
           "name": "Dai",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DAI.png",
           "urls": {
               "website": "https://makerdao.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/multi-collateral-dai"
           },
           "sector_tags": [
               "defi",
               "stablecoin",
               "asset-backed-stablecoin",
               "ethereum-ecosystem",
               "avalanche-ecosystem",
               "solana-ecosystem",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "near-protocol-ecosystem",
               "arbitrum-ecosystem",
               "harmony-ecosystem",
               "moonriver-ecosystem",
               "bnb-chain-ecosystem",
               "moonbeam-ecosystem",
               "usd-stablecoin",
               "everscale-ecosystem",
               "optimism-ecosystem",
               "osmosis-ecosystem",
               "pulsechain-ecosystem",
               "toncoin-ecosystem",
               "gnosis-chain-ecosystem",
               "klaytn-ecosystem",
               "sora-ecosystem",
               "boba-network-ecosystem",
               "aurora-ecosystem",
               "metis-andromeda-ecosystem",
               "bitgert-ecosystem",
               "energi-ecosystem",
               "starknet-ecosystem"
           ],
           "exchanges": []
       },
       "TRB": {
           "name": "Tellor",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TRB.png",
           "urls": {
               "website": "https://tellor.io/",
               "technical_doc": "https://docs.tellor.io",
               "cmc": "https://coinmarketcap.com/currencies/tellor"
           },
           "sector_tags": [
               "mineable",
               "defi",
               "oracles",
               "ethereum-ecosystem",
               "framework-ventures-portfolio",
               "polygon-ecosystem",
               "web3",
               "optimism-ecosystem",
               "gnosis-chain-ecosystem"
           ],
           "exchanges": []
       },
       "CKB": {
           "name": "Nervos Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CKB.png",
           "urls": {
               "website": "http://nervos.org/",
               "technical_doc": "https://github.com/nervosnetwork/rfcs/blob/master/rfcs/0002-ckb/0002-ckb.md",
               "cmc": "https://coinmarketcap.com/currencies/nervos-network"
           },
           "sector_tags": [
               "mineable",
               "pow",
               "platform",
               "collectibles-nfts",
               "defi",
               "interoperability",
               "quantum-resistant",
               "research",
               "scaling",
               "smart-contracts",
               "dao",
               "polychain-capital-portfolio",
               "rollups",
               "blockchain-capital-portfolio",
               "cms-holdings-portfolio",
               "dragonfly-capital-portfolio",
               "electric-capital-portfolio",
               "hashkey-capital-portfolio",
               "huobi-capital-portfolio",
               "1confirmation-portfolio",
               "multicoin-capital-portfolio",
               "web3"
           ],
           "exchanges": []
       },
       "TROY": {
           "name": "TROY",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TROY.png",
           "urls": {
               "website": "https://troytrade.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/troy"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "binance-launchpad",
               "bnb-chain-ecosystem"
           ],
           "exchanges": []
       },
       "OXT": {
           "name": "Orchid",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/OXT.png",
           "urls": {
               "website": "https://www.orchid.com/",
               "technical_doc": "https://www.orchid.com/assets/whitepaper/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/orchid"
           },
           "sector_tags": [
               "distributed-computing",
               "ethereum-ecosystem",
               "polychain-capital-portfolio",
               "blockchain-capital-portfolio",
               "fabric-ventures-portfolio",
               "kenetic-capital-portfolio",
               "a16z-portfolio",
               "web3",
               "depin"
           ],
           "exchanges": []
       },
       "KSM": {
           "name": "Kusama",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/KSM.png",
           "urls": {
               "website": "https://kusama.network/",
               "technical_doc": "https://guide.kusama.network/docs/kusama-getting-started/",
               "cmc": "https://coinmarketcap.com/currencies/kusama"
           },
           "sector_tags": [
               "substrate",
               "polkadot-ecosystem",
               "cms-holdings-portfolio",
               "kenetic-capital-portfolio",
               "1confirmation-portfolio",
               "vbc-ventures-portfolio"
           ],
           "exchanges": []
       },
       "OGN": {
           "name": "Origin Protocol",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/OGN.png",
           "urls": {
               "website": "https://www.originprotocol.com",
               "technical_doc": "https://www.originprotocol.com/litepaper",
               "cmc": "https://coinmarketcap.com/currencies/origin-protocol"
           },
           "sector_tags": [
               "collectibles-nfts",
               "defi",
               "ethereum-ecosystem",
               "kenetic-capital-portfolio",
               "pantera-capital-portfolio",
               "base-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "CTC": {
           "name": "Creditcoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CTC.png",
           "urls": {
               "website": "https://creditcoin.org",
               "technical_doc": "https://www.creditcoin.org/white-paper",
               "cmc": "https://coinmarketcap.com/currencies/creditcoin"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "real-world-assets"
           ],
           "exchanges": []
       },
       "OG": {
           "name": "OG Fan Token",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/OG.png",
           "urls": {
               "website": "https://www.socios.com/og/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/og-fan-token"
           },
           "sector_tags": [
               "sports",
               "fan-token",
               "binance-launchpool",
               "chiliz-chain-ecosystem",
               "soccer"
           ],
           "exchanges": []
       },
       "HIVE": {
           "name": "Hive",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/HIVE.png",
           "urls": {
               "website": "https://hive.io/",
               "technical_doc": "https://hive.io/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/hive-blockchain"
           },
           "sector_tags": [
               "dpos",
               "media",
               "content-creation",
               "scaling",
               "web3",
               "layer-1"
           ],
           "exchanges": []
       },
       "PRQ": {
           "name": "PARSIQ",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PRQ.png",
           "urls": {
               "website": "https://reactive.network/",
               "technical_doc": "http://www.parsiq.io/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/parsiq"
           },
           "sector_tags": [
               "services",
               "ai-big-data",
               "defi",
               "smart-contracts",
               "ethereum-ecosystem",
               "algorand-ecosystem",
               "solana-ecosystem",
               "analytics",
               "web3",
               "bnb-chain-ecosystem"
           ],
           "exchanges": []
       },
       "SOL": {
           "name": "Solana",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SOL.png",
           "urls": {
               "website": "https://solana.com",
               "technical_doc": "https://solana.com/solana-whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/solana"
           },
           "sector_tags": [
               "pos",
               "platform",
               "solana-ecosystem",
               "cms-holdings-portfolio",
               "kenetic-capital-portfolio",
               "alameda-research-portfolio",
               "multicoin-capital-portfolio",
               "okx-ventures-portfolio",
               "layer-1",
               "ftx-bankruptcy-estate",
               "alleged-sec-securities",
               "cmc-crypto-awards-2024",
               "made-in-america"
           ],
           "exchanges": []
       },
       "CARV": {
           "name": "CARV",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CARV.png",
           "urls": {
               "website": "https://carv.io",
               "technical_doc": "https://docs.carv.io/",
               "cmc": "https://coinmarketcap.com/currencies/carv"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "solana-ecosystem",
               "animoca-brands-portfolio",
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "DEEP": {
           "name": "DeepBook Protocol",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DEEP.png",
           "urls": {
               "website": "https://deepbook.tech/",
               "technical_doc": "https://drive.google.com/file/d/12JaXqshtbIgyvWIxV8j2GVRCINafqYqe/view",
               "cmc": "https://coinmarketcap.com/currencies/deepbook-protocol"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "defi",
               "sui-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "KOMA": {
           "name": "Koma Inu",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/KOMA.png",
           "urls": {
               "website": "https://komabnb.com",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/koma-inu"
           },
           "sector_tags": [
               "memes",
               "binance-smart-chain",
               "dwf-labs-portfolio",
               "binance-alpha",
               "made-in-america"
           ],
           "exchanges": []
       },
       "LUMIA": {
           "name": "Lumia",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LUMIA.png",
           "urls": {
               "website": "https://lumia.org/",
               "technical_doc": "https://docs.lumia.org/",
               "cmc": "https://coinmarketcap.com/currencies/lumia"
           },
           "sector_tags": [
               "atomic-swaps",
               "zero-knowledge-proofs",
               "masternodes",
               "dao",
               "ethereum-ecosystem",
               "binance-smart-chain",
               "layer-2",
               "governance",
               "cross-chain-dex-aggregator",
               "polygon-ecosystem",
               "cross-chain",
               "real-world-assets",
               "dwf-labs-portfolio",
               "account-abstraction",
               "data-availability",
               "intent"
           ],
           "exchanges": []
       },
       "GOAT": {
           "name": "Goatseus Maximus",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/GOAT.png",
           "urls": {
               "website": "https://pump.fun/CzLSujWBLFsSjncfkh59rUFqvafWcY5tzedWJSuypump",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/goatseus-maximus"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "ai-memes",
               "pump-fun-ecosystem",
               "terminal-of-truths"
           ],
           "exchanges": []
       },
       "MEMEFI": {
           "name": "MemeFi",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MEMEFI.png",
           "urls": {
               "website": "https://www.memefi.club/",
               "technical_doc": "https://docs.memefi.club/",
               "cmc": "https://coinmarketcap.com/currencies/meme-fi"
           },
           "sector_tags": [
               "gaming",
               "sui-ecosystem",
               "telegram-bot",
               "tap-to-earn"
           ],
           "exchanges": []
       },
       "ACT": {
           "name": "Act I : The AI Prophecy",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ACT.png",
           "urls": {
               "website": "https://actsol.xyz",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/act-i-the-ai-prophecy"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "ai-memes",
               "pump-fun-ecosystem"
           ],
           "exchanges": []
       },
       "FARTCOIN,RAYDIUM,9BB6NFECJBCTNNLFKO2FQVQBQ8HHM13KCYYCDQBGPUMP": {
           "name": "Fartcoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FARTCOIN,RAYDIUM,9BB6NFECJBCTNNLFKO2FQVQBQ8HHM13KCYYCDQBGPUMP.png",
           "urls": {
               "website": "https://www.infinitebackrooms.com/dreams/conversation-1721540624-scenario-terminal-of-truths-txt",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/fartcoin"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "ai-memes",
               "pump-fun-ecosystem",
               "terminal-of-truths",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "LUCE": {
           "name": "LUCE",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LUCE.png",
           "urls": {
               "website": "https://pump.fun/CBdCxKo9QavR9hfShgpEBG3zekorAeD7W1jfq2o3pump",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/luce"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "pump-fun-ecosystem",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "PNUT": {
           "name": "Peanut the Squirrel",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PNUT.png",
           "urls": {
               "website": "https://www.pnutsol.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/peanut-the-squirrel"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "political-memes",
               "animal-memes",
               "pump-fun-ecosystem"
           ],
           "exchanges": []
       },
       "SMILE": {
           "name": "bitSmiley",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SMILE.png",
           "urls": {
               "website": "https://www.bitsmiley.io/",
               "technical_doc": "https://github.com/bitSmiley-protocol/whitepaper/blob/main/BitSmiley_White_Paper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/bitsmiley"
           },
           "sector_tags": [
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "SYRUP": {
           "name": "Maple Finance",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SYRUP.png",
           "urls": {
               "website": "https://syrup.fi/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/maple-finance"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "HSK": {
           "name": "HashKey Platform Token",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/HSK.png",
           "urls": {
               "website": "https://hsk.hashkey.com",
               "technical_doc": "https://e9bfc29f-9778-4595-845d-eeaa28f4cd68.usrfiles.com/ugd/e9bfc2_9247a755877c4468b64ed0a2059fb386.pdf",
               "cmc": "https://coinmarketcap.com/currencies/hashkey-platform-token"
           },
           "sector_tags": [
               "centralized-exchange",
               "ethereum-ecosystem",
               "governance"
           ],
           "exchanges": []
       },
       "BAN": {
           "name": "Comedian",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BAN.png",
           "urls": {
               "website": "https://banart.art/pc/index.html",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/comedian"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "pump-fun-ecosystem"
           ],
           "exchanges": []
       },
       "USUAL": {
           "name": "Usual",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/USUAL.png",
           "urls": {
               "website": "https://usual.money/",
               "technical_doc": "https://docs.usual.money/start-here/why-usual",
               "cmc": "https://coinmarketcap.com/currencies/usual"
           },
           "sector_tags": [
               "staking",
               "ethereum-ecosystem",
               "binance-launchpool",
               "governance"
           ],
           "exchanges": []
       },
       "AI16Z": {
           "name": "ai16z",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AI16Z.png",
           "urls": {
               "website": "https://ai16z.ai/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/ai16z"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "ai-memes",
               "ai-agents",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "OL": {
           "name": "Open Loot",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/OL.png",
           "urls": {
               "website": "https://openloot.com/",
               "technical_doc": "https://wiki.openloot.com/",
               "cmc": "https://coinmarketcap.com/currencies/open-loot"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "ZEREBRO": {
           "name": "Zerebro",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ZEREBRO.png",
           "urls": {
               "website": "https://zerebro.org",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/zerebro"
           },
           "sector_tags": [
               "solana-ecosystem",
               "ai-memes",
               "ai-agents",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "AIXBT": {
           "name": "aixbt by Virtuals",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AIXBT.png",
           "urls": {
               "website": "https://app.virtuals.io/virtuals/1199",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/aixbt"
           },
           "sector_tags": [
               "base-ecosystem",
               "ai-agents",
               "virtuals-protocol-ecosystem",
               "binance-alpha",
               "defai"
           ],
           "exchanges": []
       },
       "MORPHO": {
           "name": "Morpho",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MORPHO.png",
           "urls": {
               "website": "https://morpho.org/",
               "technical_doc": "https://docs.morpho.org/whitepapers/",
               "cmc": "https://coinmarketcap.com/currencies/morpho"
           },
           "sector_tags": [
               "defi",
               "ethereum-ecosystem",
               "coinbase-ventures-portfolio",
               "lending-borowing",
               "fenbushi-capital-portfolio",
               "pantera-capital-portfolio",
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "CHILLGUY": {
           "name": "Just a chill guy",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CHILLGUY.png",
           "urls": {
               "website": "https://www.chillguy.io/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/just-a-chill-guy"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "pump-fun-ecosystem"
           ],
           "exchanges": []
       },
       "SUPRA": {
           "name": "SUPRA",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SUPRA.png",
           "urls": {
               "website": "https://supra.com/",
               "technical_doc": "https://supra.com/research/#whitepapers",
               "cmc": "https://coinmarketcap.com/currencies/supra"
           },
           "sector_tags": [
               "interoperability",
               "oracles",
               "coinbase-ventures-portfolio",
               "hashkey-capital-portfolio",
               "huobi-capital-portfolio",
               "animoca-brands-portfolio",
               "layer-1",
               "move-vm",
               "made-in-america"
           ],
           "exchanges": []
       },
       "F": {
           "name": "SynFutures",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/F.png",
           "urls": {
               "website": "https://www.synfutures.com/",
               "technical_doc": "https://www.synfutures.com/v3-whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/synfutures"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "defi",
               "derivatives",
               "ethereum-ecosystem",
               "polychain-capital-portfolio",
               "cms-holdings-portfolio",
               "dragonfly-capital-portfolio",
               "hashkey-capital-portfolio",
               "pantera-capital-portfolio",
               "base-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "FAI,UNISWAP_V3_BASE,0XB33FF54B9F7242EF1593D2C9BCD8F9DF46C77935": {
           "name": "Freysa",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FAI,UNISWAP_V3_BASE,0XB33FF54B9F7242EF1593D2C9BCD8F9DF46C77935.png",
           "urls": {
               "website": "https://www.freysa.ai/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/freysa-ai"
           },
           "sector_tags": [
               "base-ecosystem",
               "ai-agents",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "MEOW,RAYDIUM,BUHS5COXET9HCXN3JSPGYUWSKBNO96RSKU52LCMO12RF": {
           "name": "Meow",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MEOW,RAYDIUM,BUHS5COXET9HCXN3JSPGYUWSKBNO96RSKU52LCMO12RF.png",
           "urls": {
               "website": "https://meowfi.io/home",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/meowfi"
           },
           "sector_tags": [
               "solana-ecosystem",
               "cat-themed"
           ],
           "exchanges": []
       },
       "PENGU": {
           "name": "Pudgy Penguins",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PENGU.png",
           "urls": {
               "website": "https://www.pudgypenguins.com",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/pudgy-penguins"
           },
           "sector_tags": [
               "collectibles-nfts",
               "memes",
               "solana-ecosystem",
               "animal-memes",
               "ip-memes",
               "made-in-america"
           ],
           "exchanges": []
       },
       "MNRY": {
           "name": "Moonray",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MNRY.png",
           "urls": {
               "website": "https://www.moonray.studio/",
               "technical_doc": "https://docs.google.com/presentation/d/1-6UNqvFW1yKfZKwDMJG92d0-RVHP9gJp/edit?usp=sharing&ouid=101706349485834694094&rtpof=true&sd=true",
               "cmc": "https://coinmarketcap.com/currencies/moonray"
           },
           "sector_tags": [
               "gaming",
               "entertainment",
               "ethereum-ecosystem",
               "coinbase-ventures-portfolio",
               "huobi-capital-portfolio",
               "animoca-brands-portfolio",
               "polygon-ventures-portfolio"
           ],
           "exchanges": []
       },
       "CULT,UNISWAP_V3,0X0000000000C5DC95539589FBD24BE07C6C14ECA4": {
           "name": "Milady Cult Coin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CULT,UNISWAP_V3,0X0000000000C5DC95539589FBD24BE07C6C14ECA4.png",
           "urls": {
               "website": "https://cult.inc/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/milady-cult-coin"
           },
           "sector_tags": [
               "memes",
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "SEND": {
           "name": "Suilend",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SEND.png",
           "urls": {
               "website": "https://suilend.fi/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/suilend"
           },
           "sector_tags": [
               "defi",
               "lending-borowing",
               "sui-ecosystem"
           ],
           "exchanges": []
       },
       "VANA": {
           "name": "Vana",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/VANA.png",
           "urls": {
               "website": "https://www.vana.org/",
               "technical_doc": "https://docs.vana.org/docs/vana-whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/vana"
           },
           "sector_tags": [
               "platform",
               "ai-big-data",
               "ethereum-ecosystem",
               "coinbase-ventures-portfolio",
               "polychain-capital-portfolio",
               "paradigm-portfolio",
               "layer-1"
           ],
           "exchanges": []
       },
       "LINGO": {
           "name": "Lingo",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LINGO.png",
           "urls": {
               "website": "https://lingocoin.io/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/lingo"
           },
           "sector_tags": [
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "LMT,RAYDIUM,86T88W3MKT38HCHTBKBWEEB1RW1METCEAW68QY2VPUMP": {
           "name": "Limitus",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LMT,RAYDIUM,86T88W3MKT38HCHTBKBWEEB1RW1METCEAW68QY2VPUMP.png",
           "urls": {
               "website": "https://www.limitus.ai/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/limitus"
           },
           "sector_tags": [
               "ai-big-data",
               "solana-ecosystem",
               "pump-fun-ecosystem",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "GRIFFAIN": {
           "name": "GRIFFAIN",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/GRIFFAIN.png",
           "urls": {
               "website": "https://griffain.com/",
               "technical_doc": "https://griffain.com/docs",
               "cmc": "https://coinmarketcap.com/currencies/griffain"
           },
           "sector_tags": [
               "solana-ecosystem",
               "ai-agents",
               "binance-alpha",
               "ai-agent-launchpad",
               "defai"
           ],
           "exchanges": []
       },
       "BIO": {
           "name": "Bio Protocol",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BIO.png",
           "urls": {
               "website": "https://bio.xyz",
               "technical_doc": "https://docs.bio.xyz/bio/the-bioconomy/bio-protocol",
               "cmc": "https://coinmarketcap.com/currencies/bio"
           },
           "sector_tags": [
               "dao",
               "ethereum-ecosystem",
               "binance-labs-portfolio",
               "solana-ecosystem",
               "desci"
           ],
           "exchanges": []
       },
       "UFD,RAYDIUM,EL5FUXJ2J4CIQSMW85K5FG9DVUQJJUOBHOQBI2KPUMP": {
           "name": "Unicorn Fart Dust",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/UFD,RAYDIUM,EL5FUXJ2J4CIQSMW85K5FG9DVUQJJUOBHOQBI2KPUMP.png",
           "urls": {
               "website": "https://x.com/BasementRon",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/unicorn-fart-dust"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "pump-fun-ecosystem",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "ALCH": {
           "name": "Alchemist AI",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ALCH.png",
           "urls": {
               "website": "https://www.alchemistai.app/",
               "technical_doc": "https://docs.alchemistai.app/docs",
               "cmc": "https://coinmarketcap.com/currencies/alchemist-ai"
           },
           "sector_tags": [
               "solana-ecosystem",
               "desci",
               "pump-fun-ecosystem",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "ARC,RAYDIUM,61V8VBAQAGMPGDQI4JCAWO1DMBGHSYHZODCPQNEVPUMP": {
           "name": "AI Rig Complex",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ARC,RAYDIUM,61V8VBAQAGMPGDQI4JCAWO1DMBGHSYHZODCPQNEVPUMP.png",
           "urls": {
               "website": "https://www.arc.fun/index.html",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/ai-rig-complex"
           },
           "sector_tags": [
               "solana-ecosystem",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "NC": {
           "name": "Nodecoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/NC.png",
           "urls": {
               "website": "https://www.nodepay.ai/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/node-coin"
           },
           "sector_tags": [
               "entertainment",
               "dapp",
               "solana-ecosystem"
           ],
           "exchanges": []
       },
       "FLOCK": {
           "name": "FLock.io",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FLOCK.png",
           "urls": {
               "website": "https://flock.io",
               "technical_doc": "https://www.flock.io/whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/flock-io"
           },
           "sector_tags": [
               "ai-big-data",
               "base-ecosystem",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "SWARMS": {
           "name": "Swarms",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SWARMS.png",
           "urls": {
               "website": "https://swarms.world/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/swarms"
           },
           "sector_tags": [
               "solana-ecosystem",
               "ai-agents",
               "pump-fun-ecosystem",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "SONIC": {
           "name": "Sonic SVM",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SONIC.png",
           "urls": {
               "website": "https://www.sonic.game/",
               "technical_doc": "https://github.com/mirrorworld-universe/reports/blob/master/Sonic%20SVM%20%E2%80%93%20A%20HyperGrid%20Scaling%20Future%20of%20Solana.pdf",
               "cmc": "https://coinmarketcap.com/currencies/sonic-svm"
           },
           "sector_tags": [
               "gaming",
               "solana-ecosystem"
           ],
           "exchanges": []
       },
       "PIPPIN,RAYDIUM,DFH5DZRGSVVCFDOYC2CITKMRBDFRKYBA4SOFBPMAPUMP": {
           "name": "pippin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PIPPIN,RAYDIUM,DFH5DZRGSVVCFDOYC2CITKMRBDFRKYBA4SOFBPMAPUMP.png",
           "urls": {
               "website": "https://pippin.love",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/pippin"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "pump-fun-ecosystem",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "J": {
           "name": "Jambo",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/J.png",
           "urls": {
               "website": "https://www.jambo.technology/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/jambo"
           },
           "sector_tags": [
               "coinbase-ventures-portfolio",
               "solana-ecosystem",
               "pantera-capital-portfolio",
               "paradigm-portfolio"
           ],
           "exchanges": []
       },
       "BUZZ,RAYDIUM,9DHE3PYCTUYMFK4H4BBPOAJ4HQRR2KALDF6J6AAKPUMP": {
           "name": "Hive AI",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BUZZ,RAYDIUM,9DHE3PYCTUYMFK4H4BBPOAJ4HQRR2KALDF6J6AAKPUMP.png",
           "urls": {
               "website": "https://www.askthehive.ai/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/hive-ai"
           },
           "sector_tags": [
               "solana-ecosystem",
               "pump-fun-ecosystem",
               "binance-alpha",
               "defai"
           ],
           "exchanges": []
       },
       "CLUSTR,UNISWAP_V3_BASE,0X4B361E60CF256B926BA15F157D69CAC9CD037426": {
           "name": "Clustr Labs",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CLUSTR,UNISWAP_V3_BASE,0X4B361E60CF256B926BA15F157D69CAC9CD037426.png",
           "urls": {
               "website": "https://clustr.network/",
               "technical_doc": "https://clustr.gitbook.io/clustr-labs/",
               "cmc": "https://coinmarketcap.com/currencies/clustr-labs"
           },
           "sector_tags": [
               "base-ecosystem",
               "ai-agent-launchpad"
           ],
           "exchanges": []
       },
       "GPS": {
           "name": "GoPlus Security",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/GPS.png",
           "urls": {
               "website": "https://gopluslabs.io",
               "technical_doc": "https://whitepaper.gopluslabs.io/goplus-network",
               "cmc": "https://coinmarketcap.com/currencies/goplus-security"
           },
           "sector_tags": [
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "DUCK": {
           "name": "DuckChain",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DUCK.png",
           "urls": {
               "website": "https://duckchain.io/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/duckchain"
           },
           "sector_tags": [
               "toncoin-ecosystem"
           ],
           "exchanges": []
       },
       "ANIME": {
           "name": "Animecoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ANIME.png",
           "urls": {
               "website": "https://www.anime.xyz",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/anime"
           },
           "sector_tags": [
               "collectibles-nfts",
               "entertainment",
               "ethereum-ecosystem",
               "arbitrum-ecosystem"
           ],
           "exchanges": []
       },
       "TRUMP": {
           "name": "OFFICIAL TRUMP",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TRUMP.png",
           "urls": {
               "website": "https://gettrumpmemes.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/official-trump"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "political-memes",
               "celebrity-memes",
               "made-in-america"
           ],
           "exchanges": []
       },
       "MELANIA": {
           "name": "Official Melania Meme",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MELANIA.png",
           "urls": {
               "website": "https://melaniameme.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/melania-meme"
           },
           "sector_tags": [
               "memes",
               "solana-ecosystem",
               "celebrity-memes",
               "made-in-america"
           ],
           "exchanges": []
       },
       "PLUME": {
           "name": "Plume",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PLUME.png",
           "urls": {
               "website": "https://plumenetwork.xyz",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/plume"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "real-world-assets",
               "made-in-america"
           ],
           "exchanges": []
       },
       "USDA,UNISWAP_V3,0X8A60E489004CA22D775C5F2C657598278D17D9C2": {
           "name": "USDa (Avalon Labs)",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/USDA,UNISWAP_V3,0X8A60E489004CA22D775C5F2C657598278D17D9C2.png",
           "urls": {
               "website": "https://www.avalonfinance.xyz/",
               "technical_doc": "https://docs.avalonfinance.xyz/",
               "cmc": "https://coinmarketcap.com/currencies/usda-avalon-labs"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "mvb",
               "mantle-ecosystem"
           ],
           "exchanges": []
       },
       "VINE": {
           "name": "Vine Coin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/VINE.png",
           "urls": {
               "website": null,
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/vine-coin"
           },
           "sector_tags": [
               "solana-ecosystem",
               "pump-fun-ecosystem",
               "binance-alpha"
           ],
           "exchanges": []
       },
       "DOGE": {
           "name": "Dogecoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DOGE.png",
           "urls": {
               "website": "http://dogecoin.com/",
               "technical_doc": "https://github.com/dogecoin/dogecoin/blob/master/README.md",
               "cmc": "https://coinmarketcap.com/currencies/dogecoin"
           },
           "sector_tags": [
               "mineable",
               "pow",
               "scrypt",
               "medium-of-exchange",
               "memes",
               "payments",
               "doggone-doggerel",
               "bnb-chain-ecosystem",
               "ftx-bankruptcy-estate",
               "made-in-america"
           ],
           "exchanges": []
       },
       "XVG": {
           "name": "Verge",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/XVG.png",
           "urls": {
               "website": "http://vergecurrency.com/",
               "technical_doc": "https://vergecurrency.com/static/blackpaper/verge-blackpaper-v5.0.pdf",
               "cmc": "https://coinmarketcap.com/currencies/verge"
           },
           "sector_tags": [
               "mineable",
               "multiple-algorithms",
               "medium-of-exchange",
               "2017-2018-alt-season"
           ],
           "exchanges": []
       },
       "USDT": {
           "name": "Tether USDt",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/USDT.png",
           "urls": {
               "website": "https://tether.to",
               "technical_doc": "https://tether.to/wp-content/uploads/2016/06/TetherWhitePaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/tether"
           },
           "sector_tags": [
               "stablecoin",
               "asset-backed-stablecoin",
               "ethereum-ecosystem",
               "waves-ecosystem",
               "bitcoin-cash-ecosystem",
               "heco-ecosystem",
               "algorand-ecosystem",
               "avalanche-ecosystem",
               "solana-ecosystem",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "tezos-ecosystem",
               "near-protocol-ecosystem",
               "arbitrum-ecosystem",
               "celo-ecosystem",
               "iotex-ecosystem",
               "zilliqa-ecosystem",
               "harmony-ecosystem",
               "moonriver-ecosystem",
               "injective-ecosystem",
               "bnb-chain-ecosystem",
               "oasis-ecosystem",
               "moonbeam-ecosystem",
               "usd-stablecoin",
               "xdc-ecosystem",
               "everscale-ecosystem",
               "velas-ecosystem",
               "ethereum-pow-ecosystem",
               "aptos-ecosystem",
               "sui-ecosystem",
               "optimism-ecosystem",
               "canto-ecosystem",
               "osmosis-ecosystem",
               "zksync-era-ecosystem",
               "pulsechain-ecosystem",
               "sei-ecosystem",
               "toncoin-ecosystem",
               "fiat-stablecoin",
               "viction-ecosystem",
               "gnosis-chain-ecosystem",
               "klaytn-ecosystem",
               "okexchain-ecosystem",
               "conflux-ecosystem",
               "kcc-ecosystem",
               "tron20-ecosystem",
               "kardiachain-ecosystem",
               "rsk-rbtc-ecosystem",
               "telos-ecosystem",
               "boba-network-ecosystem",
               "fusion-network-ecosystem",
               "hoo-smart-chain-ecosystem",
               "secret-ecosystem",
               "aurora-ecosystem",
               "metis-andromeda-ecosystem",
               "meter-ecosystem",
               "fuse-ecosystem",
               "syscoin-ecosystem",
               "milkomeda-ecosystem",
               "bitgert-ecosystem",
               "astar-ecosystem",
               "cube-network-ecosystem",
               "thundercore-ecosystem",
               "redlight-chain-ecosystem",
               "core-ecosystem",
               "polygon-zkevm-ecosystem",
               "eos-evm-ecosystem",
               "starknet-ecosystem",
               "mantle-ecosystem",
               "neon-evm-ecosystem",
               "manta-pacific-ecosystem",
               "scroll-ecosystem",
               "x-layer-ecosystem"
           ],
           "exchanges": []
       },
       "CTSI": {
           "name": "Cartesi",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CTSI.png",
           "urls": {
               "website": "https://cartesi.io/",
               "technical_doc": "https://cartesi.io/docs/",
               "cmc": "https://coinmarketcap.com/currencies/cartesi"
           },
           "sector_tags": [
               "pos",
               "platform",
               "cosmos-ecosystem",
               "scaling",
               "smart-contracts",
               "staking",
               "dapp",
               "ethereum-ecosystem",
               "binance-smart-chain",
               "layer-2",
               "rollups",
               "sidechain",
               "binance-launchpad",
               "binance-labs-portfolio",
               "polygon-ecosystem",
               "arbitrum-ecosystem",
               "injective-ecosystem",
               "bnb-chain-ecosystem",
               "optimism-ecosystem",
               "base-ecosystem",
               "modular-blockchain"
           ],
           "exchanges": []
       },
       "JST": {
           "name": "JUST",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/JST.png",
           "urls": {
               "website": "https://just.network/#/",
               "technical_doc": "https://www.just.network/docs/white_paper_en.pdf",
               "cmc": "https://coinmarketcap.com/currencies/just"
           },
           "sector_tags": [
               "defi",
               "tron-ecosystem",
               "tron20-ecosystem"
           ],
           "exchanges": []
       },
       "CELO": {
           "name": "Celo",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CELO.png",
           "urls": {
               "website": "https://celo.org/",
               "technical_doc": "http://docs.celo.org/",
               "cmc": "https://coinmarketcap.com/currencies/celo"
           },
           "sector_tags": [
               "pos",
               "zero-knowledge-proofs",
               "mobile",
               "payments",
               "smart-contracts",
               "coinbase-ventures-portfolio",
               "polychain-capital-portfolio",
               "dragonfly-capital-portfolio",
               "electric-capital-portfolio",
               "a16z-portfolio",
               "near-protocol-ecosystem",
               "celo-ecosystem",
               "dwf-labs-portfolio",
               "made-in-america"
           ],
           "exchanges": []
       },
       "SCRT": {
           "name": "Secret",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SCRT.png",
           "urls": {
               "website": "https://scrt.network",
               "technical_doc": "https://docs.scrt.network/",
               "cmc": "https://coinmarketcap.com/currencies/secret"
           },
           "sector_tags": [
               "cosmos-ecosystem",
               "defi",
               "privacy",
               "injective-ecosystem",
               "osmosis-ecosystem",
               "secret-ecosystem"
           ],
           "exchanges": []
       },
       "UMA": {
           "name": "UMA",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/UMA.png",
           "urls": {
               "website": "https://umaproject.org/",
               "technical_doc": "https://github.com/UMAprotocol/whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/uma"
           },
           "sector_tags": [
               "defi",
               "derivatives",
               "oracles",
               "dao",
               "ethereum-ecosystem",
               "coinbase-ventures-portfolio",
               "governance",
               "synthetics",
               "blockchain-capital-portfolio",
               "dragonfly-capital-portfolio",
               "placeholder-ventures-portfolio",
               "polygon-ecosystem",
               "injective-ecosystem"
           ],
           "exchanges": []
       },
       "AR": {
           "name": "Arweave",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AR.png",
           "urls": {
               "website": "https://www.arweave.org",
               "technical_doc": "https://ar-io.net/azo-0qw6bb9u5doGdMR-atcIRV_ylJCV4K4Kwv85GO4",
               "cmc": "https://coinmarketcap.com/currencies/arweave"
           },
           "sector_tags": [
               "distributed-computing",
               "filesharing",
               "storage",
               "coinbase-ventures-portfolio",
               "solana-ecosystem",
               "arrington-xrp-capital-portfolio",
               "blockchain-capital-portfolio",
               "a16z-portfolio",
               "multicoin-capital-portfolio",
               "web3",
               "egirl-capital-portfolio",
               "depin",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "KDA": {
           "name": "Kadena",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/KDA.png",
           "urls": {
               "website": "http://kadena.io/",
               "technical_doc": "https://docs.kadena.io/basics/whitepapers/overview",
               "cmc": "https://coinmarketcap.com/currencies/kadena"
           },
           "sector_tags": [
               "mineable",
               "pow",
               "blake2s",
               "platform",
               "ai-big-data",
               "collectibles-nfts",
               "defi",
               "gaming",
               "smart-contracts",
               "dao",
               "wallet",
               "launchpad",
               "coinfund-portfolio",
               "multicoin-capital-portfolio",
               "web3",
               "defi-2",
               "layer-1",
               "kadena-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "HNT": {
           "name": "Helium",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/HNT.png",
           "urls": {
               "website": "https://www.helium.com/",
               "technical_doc": "http://whitepaper.helium.com/",
               "cmc": "https://coinmarketcap.com/currencies/helium"
           },
           "sector_tags": [
               "mineable",
               "enterprise-solutions",
               "distributed-computing",
               "iot",
               "solana-ecosystem",
               "usv-portfolio",
               "multicoin-capital-portfolio",
               "web3",
               "depin",
               "made-in-america"
           ],
           "exchanges": []
       },
       "RENDER": {
           "name": "Render",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/RENDER.png",
           "urls": {
               "website": "https://rendernetwork.com/",
               "technical_doc": "https://know.rendernetwork.com/",
               "cmc": "https://coinmarketcap.com/currencies/render"
           },
           "sector_tags": [
               "art",
               "media",
               "vr-ar",
               "ai-big-data",
               "distributed-computing",
               "collectibles-nfts",
               "scaling",
               "video",
               "ethereum-ecosystem",
               "solana-ecosystem",
               "metaverse",
               "alameda-research-portfolio",
               "multicoin-capital-portfolio",
               "polygon-ecosystem",
               "web3",
               "generative-ai",
               "depin",
               "cmc-crypto-yearbook-2024-2025",
               "made-in-america"
           ],
           "exchanges": []
       },
       "SKL": {
           "name": "SKALE",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SKL.png",
           "urls": {
               "website": "https://skale.space/?utm_medium=website&utm_source=coinmarketcap&utm_campaign=skalepage",
               "technical_doc": "https://skale.space/docs/?utm_medium=website&utm_source=coinmarketcap&utm_campaign=skalepage",
               "cmc": "https://coinmarketcap.com/currencies/skale-network"
           },
           "sector_tags": [
               "platform",
               "gaming",
               "scaling",
               "smart-contracts",
               "staking",
               "ethereum-ecosystem",
               "storage",
               "layer-2",
               "arrington-xrp-capital-portfolio",
               "boostvc-portfolio",
               "galaxy-digital-portfolio",
               "hashkey-capital-portfolio",
               "multicoin-capital-portfolio",
               "play-to-earn",
               "layer-1",
               "modular-blockchain",
               "made-in-america"
           ],
           "exchanges": []
       },
       "COMP": {
           "name": "Compound",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/COMP.png",
           "urls": {
               "website": "https://compoundlabs.xyz/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/compound"
           },
           "sector_tags": [
               "defi",
               "dao",
               "ethereum-ecosystem",
               "yield-farming",
               "coinbase-ventures-portfolio",
               "three-arrows-capital-portfolio",
               "polychain-capital-portfolio",
               "solana-ecosystem",
               "lending-borowing",
               "dragonfly-capital-portfolio",
               "alameda-research-portfolio",
               "a16z-portfolio",
               "pantera-capital-portfolio",
               "paradigm-portfolio",
               "near-protocol-ecosystem",
               "arbitrum-ecosystem",
               "bnb-chain-ecosystem",
               "gnosis-chain-ecosystem",
               "sora-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "BAL": {
           "name": "Balancer",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BAL.png",
           "urls": {
               "website": "https://balancer.fi/",
               "technical_doc": "https://docs.balancer.fi/",
               "cmc": "https://coinmarketcap.com/currencies/balancer"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "defi",
               "dao",
               "ethereum-ecosystem",
               "yield-farming",
               "amm",
               "three-arrows-capital-portfolio",
               "governance",
               "heco-ecosystem",
               "coinfund-portfolio",
               "alameda-research-portfolio",
               "placeholder-ventures-portfolio",
               "pantera-capital-portfolio",
               "polygon-ecosystem",
               "near-protocol-ecosystem",
               "arbitrum-ecosystem",
               "optimism-ecosystem",
               "base-ecosystem",
               "gnosis-chain-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "AVAX": {
           "name": "Avalanche",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AVAX.png",
           "urls": {
               "website": "https://avax.network/",
               "technical_doc": "https://www.avalabs.org/whitepapers",
               "cmc": "https://coinmarketcap.com/currencies/avalanche"
           },
           "sector_tags": [
               "defi",
               "smart-contracts",
               "three-arrows-capital-portfolio",
               "polychain-capital-portfolio",
               "avalanche-ecosystem",
               "cms-holdings-portfolio",
               "dragonfly-capital-portfolio",
               "moonbeam-ecosystem",
               "real-world-assets",
               "layer-1",
               "zksync-era-ecosystem",
               "klaytn-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "SLP": {
           "name": "Smooth Love Potion",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SLP.png",
           "urls": {
               "website": "https://axieinfinity.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/smooth-love-potion"
           },
           "sector_tags": [
               "collectibles-nfts",
               "ethereum-ecosystem",
               "solana-ecosystem",
               "play-to-earn",
               "ronin-ecosystem"
           ],
           "exchanges": []
       },
       "YFI": {
           "name": "yearn.finance",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/YFI.png",
           "urls": {
               "website": "https://yearn.finance/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/yearn-finance"
           },
           "sector_tags": [
               "defi",
               "dao",
               "ethereum-ecosystem",
               "yield-farming",
               "yield-aggregator",
               "yearn-partnerships",
               "three-arrows-capital-portfolio",
               "polychain-capital-portfolio",
               "governance",
               "heco-ecosystem",
               "solana-ecosystem",
               "blockchain-capital-portfolio",
               "framework-ventures-portfolio",
               "alameda-research-portfolio",
               "parafi-capital",
               "fantom-ecosystem",
               "near-protocol-ecosystem",
               "arbitrum-ecosystem",
               "bnb-chain-ecosystem",
               "standard-crypto-portfolio",
               "gnosis-chain-ecosystem"
           ],
           "exchanges": []
       },
       "RARI": {
           "name": "RARI",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/RARI.png",
           "urls": {
               "website": "https://rari.foundation",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/rarible"
           },
           "sector_tags": [
               "collectibles-nfts",
               "dao",
               "ethereum-ecosystem",
               "coinbase-ventures-portfolio",
               "governance",
               "coinfund-portfolio"
           ],
           "exchanges": []
       },
       "CSPR": {
           "name": "Casper",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CSPR.png",
           "urls": {
               "website": "https://casper.network/",
               "technical_doc": "https://casper.network/network/developers/getting-started",
               "cmc": "https://coinmarketcap.com/currencies/casper"
           },
           "sector_tags": [
               "ai-big-data",
               "enterprise-solutions",
               "arrington-xrp-capital-portfolio",
               "hashkey-capital-portfolio",
               "web3",
               "okx-ventures-portfolio",
               "layer-1"
           ],
           "exchanges": []
       },
       "TWT": {
           "name": "Trust Wallet Token",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TWT.png",
           "urls": {
               "website": "https://trustwallet.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/trust-wallet-token"
           },
           "sector_tags": [
               "wallet",
               "solana-ecosystem",
               "polygon-ecosystem",
               "bnb-chain-ecosystem"
           ],
           "exchanges": []
       },
       "SHIB": {
           "name": "Shiba Inu",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SHIB.png",
           "urls": {
               "website": "https://shibatoken.com/",
               "technical_doc": "https://github.com/shytoshikusama/woofwoofpaper/raw/main/SHIBA_INU_WOOF_WOOF.pdf",
               "cmc": "https://coinmarketcap.com/currencies/shiba-inu"
           },
           "sector_tags": [
               "memes",
               "ethereum-ecosystem",
               "doggone-doggerel",
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "DIA": {
           "name": "DIA",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DIA.png",
           "urls": {
               "website": "https://diadata.org/",
               "technical_doc": "https://docs.diadata.org",
               "cmc": "https://coinmarketcap.com/currencies/dia"
           },
           "sector_tags": [
               "platform",
               "ai-big-data",
               "defi",
               "oracles",
               "smart-contracts",
               "ethereum-ecosystem",
               "substrate",
               "avalanche-ecosystem",
               "solana-ecosystem",
               "exnetwork-capital-portfolio",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "web3",
               "near-protocol-ecosystem",
               "arbitrum-ecosystem",
               "injective-ecosystem",
               "bnb-chain-ecosystem"
           ],
           "exchanges": []
       },
       "SAND": {
           "name": "The Sandbox",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SAND.png",
           "urls": {
               "website": "https://www.sandbox.game/en/",
               "technical_doc": "http://www.sandbox.game/The_Sandbox_Whitepaper_2020.pdf",
               "cmc": "https://coinmarketcap.com/currencies/the-sandbox"
           },
           "sector_tags": [
               "collectibles-nfts",
               "content-creation",
               "gaming",
               "ethereum-ecosystem",
               "binance-launchpad",
               "metaverse",
               "polygon-ecosystem",
               "play-to-earn",
               "animoca-brands-portfolio",
               "alleged-sec-securities"
           ],
           "exchanges": []
       },
       "NEAR": {
           "name": "NEAR Protocol",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/NEAR.png",
           "urls": {
               "website": "https://near.org/",
               "technical_doc": "https://near.org/papers/the-official-near-white-paper",
               "cmc": "https://coinmarketcap.com/currencies/near-protocol"
           },
           "sector_tags": [
               "platform",
               "ai-big-data",
               "staking",
               "coinbase-ventures-portfolio",
               "three-arrows-capital-portfolio",
               "arrington-xrp-capital-portfolio",
               "coinfund-portfolio",
               "electric-capital-portfolio",
               "fabric-ventures-portfolio",
               "kenetic-capital-portfolio",
               "near-protocol-ecosystem",
               "cross-chain",
               "injective-ecosystem",
               "circle-ventures-portfolio",
               "layer-1",
               "alleged-sec-securities",
               "account-abstraction",
               "data-availability",
               "cmc-crypto-yearbook-2024-2025",
               "made-in-america"
           ],
           "exchanges": []
       },
       "OM": {
           "name": "MANTRA",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/OM.png",
           "urls": {
               "website": "https://www.mantrachain.io",
               "technical_doc": "https://docs.mantrachain.io/",
               "cmc": "https://coinmarketcap.com/currencies/mantra"
           },
           "sector_tags": [
               "cosmos-ecosystem",
               "defi",
               "dao",
               "ethereum-ecosystem",
               "kenetic-capital-portfolio",
               "exnetwork-capital-portfolio",
               "polygon-ecosystem",
               "bnb-chain-ecosystem",
               "osmosis-ecosystem",
               "real-world-assets",
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "CRV": {
           "name": "Curve DAO Token",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CRV.png",
           "urls": {
               "website": "https://www.curve.fi/",
               "technical_doc": "https://docs.curve.fi/",
               "cmc": "https://coinmarketcap.com/currencies/curve-dao-token"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "defi",
               "dao",
               "ethereum-ecosystem",
               "yield-farming",
               "amm",
               "governance",
               "framework-ventures-portfolio",
               "alameda-research-portfolio",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "arbitrum-ecosystem",
               "optimism-ecosystem",
               "dwf-labs-portfolio",
               "base-ecosystem",
               "gnosis-chain-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "DOT": {
           "name": "Polkadot",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DOT.png",
           "urls": {
               "website": "https://polkadot.com",
               "technical_doc": "https://polkadot.com/papers/Polkadot-whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/polkadot-new"
           },
           "sector_tags": [
               "substrate",
               "polkadot",
               "polkadot-ecosystem",
               "three-arrows-capital-portfolio",
               "polychain-capital-portfolio",
               "heco-ecosystem",
               "arrington-xrp-capital-portfolio",
               "blockchain-capital-portfolio",
               "boostvc-portfolio",
               "cms-holdings-portfolio",
               "coinfund-portfolio",
               "fabric-ventures-portfolio",
               "fenbushi-capital-portfolio",
               "hashkey-capital-portfolio",
               "kenetic-capital-portfolio",
               "1confirmation-portfolio",
               "placeholder-ventures-portfolio",
               "pantera-capital-portfolio",
               "exnetwork-capital-portfolio",
               "web3",
               "spartan-group",
               "osmosis-ecosystem",
               "layer-1"
           ],
           "exchanges": []
       },
       "GRT": {
           "name": "The Graph",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/GRT.png",
           "urls": {
               "website": "https://thegraph.com",
               "technical_doc": "https://thegraph.com/docs/",
               "cmc": "https://coinmarketcap.com/currencies/the-graph"
           },
           "sector_tags": [
               "ai-big-data",
               "enterprise-solutions",
               "defi",
               "ethereum-ecosystem",
               "coinbase-ventures-portfolio",
               "solana-ecosystem",
               "analytics",
               "coinfund-portfolio",
               "dcg-portfolio",
               "fabric-ventures-portfolio",
               "framework-ventures-portfolio",
               "ledgerprime-portfolio",
               "multicoin-capital-portfolio",
               "parafi-capital",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "web3",
               "near-protocol-ecosystem",
               "arbitrum-ecosystem",
               "spartan-group",
               "injective-ecosystem",
               "sora-ecosystem"
           ],
           "exchanges": []
       },
       "ACA": {
           "name": "Acala Token",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ACA.png",
           "urls": {
               "website": "https://acala.network/",
               "technical_doc": "https://wiki.acala.network/",
               "cmc": "https://coinmarketcap.com/currencies/acala"
           },
           "sector_tags": [
               "defi",
               "substrate",
               "polkadot",
               "polkadot-ecosystem",
               "arrington-xrp-capital-portfolio",
               "spartan-group",
               "injective-ecosystem"
           ],
           "exchanges": []
       },
       "SUSHI": {
           "name": "SushiSwap",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SUSHI.png",
           "urls": {
               "website": "https://sushi.com/",
               "technical_doc": "https://docs.sushi.com/pdf/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/sushiswap"
           },
           "sector_tags": [
               "collectibles-nfts",
               "decentralized-exchange-dex-token",
               "defi",
               "dao",
               "ethereum-ecosystem",
               "yield-farming",
               "amm",
               "yearn-partnerships",
               "governance",
               "avalanche-ecosystem",
               "metaverse",
               "blockchain-capital-portfolio",
               "defiance-capital-portfolio",
               "alameda-research-portfolio",
               "pantera-capital-portfolio",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "arbitrum-ecosystem",
               "celo-ecosystem",
               "harmony-ecosystem",
               "injective-ecosystem",
               "bnb-chain-ecosystem",
               "optimism-ecosystem",
               "base-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "AXS": {
           "name": "Axie Infinity",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AXS.png",
           "urls": {
               "website": "https://axieinfinity.com/",
               "technical_doc": "https://whitepaper.axieinfinity.com/",
               "cmc": "https://coinmarketcap.com/currencies/axie-infinity"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "ethereum-ecosystem",
               "binance-launchpad",
               "solana-ecosystem",
               "metaverse",
               "defiance-capital-portfolio",
               "play-to-earn",
               "animoca-brands-portfolio",
               "harmony-ecosystem",
               "alleged-sec-securities",
               "ronin-ecosystem"
           ],
           "exchanges": []
       },
       "LIT": {
           "name": "Litentry",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LIT.png",
           "urls": {
               "website": "https://www.litentry.com/",
               "technical_doc": "https://docs.litentry.com/",
               "cmc": "https://coinmarketcap.com/currencies/litentry"
           },
           "sector_tags": [
               "defi",
               "dapp",
               "ethereum-ecosystem",
               "substrate",
               "polkadot",
               "polkadot-ecosystem",
               "binance-launchpool",
               "web3",
               "injective-ecosystem",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "GLMR": {
           "name": "Moonbeam",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/GLMR.png",
           "urls": {
               "website": "https://moonbeam.network/",
               "technical_doc": "https://docs.moonbeam.network/",
               "cmc": "https://coinmarketcap.com/currencies/moonbeam"
           },
           "sector_tags": [
               "smart-contracts",
               "substrate",
               "polkadot",
               "polkadot-ecosystem",
               "binance-labs-portfolio",
               "arrington-xrp-capital-portfolio",
               "injective-ecosystem"
           ],
           "exchanges": []
       },
       "PHA": {
           "name": "Phala Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PHA.png",
           "urls": {
               "website": "https://phala.network/",
               "technical_doc": "https://docs.phala.network/introduction/readme",
               "cmc": "https://coinmarketcap.com/currencies/phala-network"
           },
           "sector_tags": [
               "ai-big-data",
               "distributed-computing",
               "privacy",
               "zero-knowledge-proofs",
               "interoperability",
               "oracles",
               "scaling",
               "smart-contracts",
               "ethereum-ecosystem",
               "substrate",
               "polkadot",
               "polkadot-ecosystem",
               "rollups",
               "metaverse",
               "polygon-ecosystem",
               "web3",
               "generative-ai",
               "dwf-labs-portfolio",
               "account-abstraction",
               "data-availability",
               "depin",
               "ai-agents"
           ],
           "exchanges": []
       },
       "RAD": {
           "name": "Radworks",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/RAD.png",
           "urls": {
               "website": "https://radworks.org/",
               "technical_doc": "https://docs.radworks.org/",
               "cmc": "https://coinmarketcap.com/currencies/radworks"
           },
           "sector_tags": [
               "dao",
               "ethereum-ecosystem",
               "coinbase-ventures-portfolio",
               "fabric-ventures-portfolio",
               "placeholder-ventures-portfolio",
               "web3"
           ],
           "exchanges": []
       },
       "FARM": {
           "name": "Harvest Finance",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FARM.png",
           "urls": {
               "website": "https://harvest.finance/",
               "technical_doc": "https://medium.com/harvest-finance/the-harvest-finance-project-338c3e5806fc",
               "cmc": "https://coinmarketcap.com/currencies/harvest-finance"
           },
           "sector_tags": [
               "defi",
               "dao",
               "ethereum-ecosystem",
               "yield-farming",
               "yield-aggregator",
               "governance",
               "polygon-ecosystem"
           ],
           "exchanges": []
       },
       "EGLD": {
           "name": "MultiversX",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/EGLD.png",
           "urls": {
               "website": "https://multiversx.com/",
               "technical_doc": "https://files.multiversx.com/multiversx-whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/multiversx-egld"
           },
           "sector_tags": [
               "pos",
               "enterprise-solutions",
               "distributed-computing",
               "loyalty",
               "defi",
               "gaming",
               "identity",
               "payments",
               "scaling",
               "smart-contracts",
               "software",
               "binance-launchpad",
               "binance-labs-portfolio",
               "metaverse",
               "electric-capital-portfolio",
               "exnetwork-capital-portfolio",
               "mobile-payment",
               "web3",
               "elrond-ecosystem",
               "injective-ecosystem",
               "bnb-chain-ecosystem",
               "layer-1",
               "dwf-labs-portfolio",
               "multiversx-ecosystem",
               "depin"
           ],
           "exchanges": []
       },
       "AMP": {
           "name": "Amp",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AMP.png",
           "urls": {
               "website": "https://amp.xyz",
               "technical_doc": "https://docs.ampera.xyz/ecosystem/amp-token/whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/amp"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "alleged-sec-securities"
           ],
           "exchanges": []
       },
       "PERP": {
           "name": "Perpetual Protocol",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PERP.png",
           "urls": {
               "website": "https://perp.com",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/perpetual-protocol"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "defi",
               "derivatives",
               "scaling",
               "ethereum-ecosystem",
               "amm",
               "three-arrows-capital-portfolio",
               "solana-ecosystem",
               "cms-holdings-portfolio",
               "alameda-research-portfolio",
               "multicoin-capital-portfolio",
               "arbitrum-ecosystem",
               "optimism-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "FXS": {
           "name": "Frax Share",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FXS.png",
           "urls": {
               "website": "https://frax.finance",
               "technical_doc": "https://docs.frax.finance",
               "cmc": "https://coinmarketcap.com/currencies/frax-share"
           },
           "sector_tags": [
               "defi",
               "ethereum-ecosystem",
               "amm",
               "seigniorage",
               "avalanche-ecosystem",
               "solana-ecosystem",
               "exnetwork-capital-portfolio",
               "fantom-ecosystem",
               "arbitrum-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "ACH": {
           "name": "Alchemy Pay",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ACH.png",
           "urls": {
               "website": "https://alchemypay.org/",
               "technical_doc": "https://file.alchemytech.io/Alchemy_WP-EN.pdf",
               "cmc": "https://coinmarketcap.com/currencies/alchemy-pay"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "GHST": {
           "name": "Aavegotchi",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/GHST.png",
           "urls": {
               "website": "https://aavegotchi.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/aavegotchi"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "dao",
               "ethereum-ecosystem",
               "governance",
               "metaverse",
               "polygon-ecosystem",
               "play-to-earn",
               "base-ecosystem"
           ],
           "exchanges": []
       },
       "BAKE": {
           "name": "BakeryToken",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BAKE.png",
           "urls": {
               "website": "https://www.bakeryswap.org/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/bakerytoken"
           },
           "sector_tags": [
               "collectibles-nfts",
               "defi",
               "gaming",
               "binance-chain",
               "amm",
               "dex",
               "bnb-chain-ecosystem"
           ],
           "exchanges": []
       },
       "GALA": {
           "name": "Gala",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/GALA.png",
           "urls": {
               "website": "https://gala.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/gala"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "ethereum-ecosystem",
               "binance-smart-chain",
               "polygon-ecosystem",
               "play-to-earn",
               "dwf-labs-portfolio",
               "made-in-america"
           ],
           "exchanges": []
       },
       "UNI": {
           "name": "Uniswap",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/UNI.png",
           "urls": {
               "website": "https://uniswap.org/blog/uni/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/uniswap"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "defi",
               "dao",
               "ethereum-ecosystem",
               "yield-farming",
               "amm",
               "coinbase-ventures-portfolio",
               "three-arrows-capital-portfolio",
               "governance",
               "heco-ecosystem",
               "solana-ecosystem",
               "blockchain-capital-portfolio",
               "defiance-capital-portfolio",
               "alameda-research-portfolio",
               "a16z-portfolio",
               "pantera-capital-portfolio",
               "parafi-capital",
               "paradigm-portfolio",
               "polygon-ecosystem",
               "near-protocol-ecosystem",
               "arbitrum-ecosystem",
               "injective-ecosystem",
               "optimism-ecosystem",
               "cmc-crypto-awards-2024",
               "gnosis-chain-ecosystem",
               "sora-ecosystem",
               "hoo-smart-chain-ecosystem",
               "made-in-america",
               "defi"
           ],
           "exchanges": []
       },
       "DEGO": {
           "name": "Dego Finance",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DEGO.png",
           "urls": {
               "website": "https://dego.finance/",
               "technical_doc": "https://docs.dego.finance/",
               "cmc": "https://coinmarketcap.com/currencies/dego-finance"
           },
           "sector_tags": [
               "collectibles-nfts",
               "defi",
               "dao",
               "ethereum-ecosystem",
               "governance",
               "binance-launchpad",
               "solana-ecosystem",
               "bnb-chain-ecosystem",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "LINA": {
           "name": "Linear Finance",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LINA.png",
           "urls": {
               "website": "https://linear.finance",
               "technical_doc": "https://linear.finance/defi/Linear_Whitepaper_EN.pdf",
               "cmc": "https://coinmarketcap.com/currencies/linear"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "defi",
               "ethereum-ecosystem",
               "yield-farming",
               "polkadot-ecosystem",
               "binance-launchpad",
               "cms-holdings-portfolio",
               "kenetic-capital-portfolio",
               "alameda-research-portfolio",
               "bnb-chain-ecosystem",
               "dwf-labs-portfolio",
               "defi"
           ],
           "exchanges": []
       },
       "VELO": {
           "name": "Velo",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/VELO.png",
           "urls": {
               "website": "https://velo.org/",
               "technical_doc": "https://velo.org/doc/Velo_Whitepaper_EN.pdf",
               "cmc": "https://coinmarketcap.com/currencies/velo"
           },
           "sector_tags": [
               "stellar-ecosystem",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "FLM": {
           "name": "Flamingo",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FLM.png",
           "urls": {
               "website": "https://flamingo.finance/",
               "technical_doc": "https://docs.flamingo.finance/",
               "cmc": "https://coinmarketcap.com/currencies/flamingo"
           },
           "sector_tags": [
               "defi",
               "dao",
               "neo-ecosystem",
               "yield-farming",
               "binance-chain",
               "binance-launchpool"
           ],
           "exchanges": []
       },
       "CAKE": {
           "name": "PancakeSwap",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CAKE.png",
           "urls": {
               "website": "https://pancakeswap.finance/",
               "technical_doc": "https://docs.pancakeswap.finance/developers",
               "cmc": "https://coinmarketcap.com/currencies/pancakeswap"
           },
           "sector_tags": [
               "collectibles-nfts",
               "decentralized-exchange-dex-token",
               "defi",
               "smart-contracts",
               "ethereum-ecosystem",
               "yield-farming",
               "amm",
               "binance-smart-chain",
               "dex",
               "governance",
               "aptos-ecosystem",
               "zksync-era-ecosystem",
               "base-ecosystem",
               "linea-ecosystem",
               "polygon-zkevm-ecosystem",
               "opbnb-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "INJ": {
           "name": "Injective",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/INJ.png",
           "urls": {
               "website": "https://injective.com",
               "technical_doc": "https://docs.injectiveprotocol.com/#introduction",
               "cmc": "https://coinmarketcap.com/currencies/injective"
           },
           "sector_tags": [
               "pos",
               "platform",
               "cosmos-ecosystem",
               "ai-big-data",
               "defi",
               "smart-contracts",
               "ethereum-ecosystem",
               "cosmos",
               "binance-smart-chain",
               "binance-launchpad",
               "binance-labs-portfolio",
               "cms-holdings-portfolio",
               "pantera-capital-portfolio",
               "web3",
               "injective-ecosystem",
               "osmosis-ecosystem",
               "layer-1",
               "made-in-america"
           ],
           "exchanges": []
       },
       "ALPHA": {
           "name": "Stella",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ALPHA.png",
           "urls": {
               "website": "https://stellaxyz.io/",
               "technical_doc": "https://docs.stellaxyz.io/",
               "cmc": "https://coinmarketcap.com/currencies/alpha-finance-lab"
           },
           "sector_tags": [
               "defi",
               "ethereum-ecosystem",
               "yield-farming",
               "yield-aggregator",
               "binance-launchpool",
               "binance-launchpad",
               "defiance-capital-portfolio",
               "alameda-research-portfolio",
               "multicoin-capital-portfolio",
               "spartan-group",
               "injective-ecosystem",
               "bnb-chain-ecosystem",
               "optimism-ecosystem"
           ],
           "exchanges": []
       },
       "AAVE": {
           "name": "Aave",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AAVE.png",
           "urls": {
               "website": "https://aave.com/",
               "technical_doc": "https://github.com/aave/aave-protocol/blob/master/docs/Aave_Protocol_Whitepaper_v1_0.pdf",
               "cmc": "https://coinmarketcap.com/currencies/aave"
           },
           "sector_tags": [
               "defi",
               "dao",
               "ethereum-ecosystem",
               "yield-farming",
               "three-arrows-capital-portfolio",
               "governance",
               "heco-ecosystem",
               "solana-ecosystem",
               "lending-borowing",
               "blockchain-capital-portfolio",
               "defiance-capital-portfolio",
               "framework-ventures-portfolio",
               "alameda-research-portfolio",
               "pantera-capital-portfolio",
               "parafi-capital",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "near-protocol-ecosystem",
               "optimism-ecosystem",
               "standard-crypto-portfolio",
               "gnosis-chain-ecosystem",
               "sora-ecosystem",
               "made-in-america"
           ],
           "exchanges": []
       },
       "XVS": {
           "name": "Venus",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/XVS.png",
           "urls": {
               "website": "https://venus.io/",
               "technical_doc": "https://venus.io/Whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/venus"
           },
           "sector_tags": [
               "defi",
               "ethereum-ecosystem",
               "yield-farming",
               "binance-launchpool",
               "lending-borowing",
               "bnb-chain-ecosystem",
               "optimism-ecosystem",
               "real-world-assets",
               "zksync-era-ecosystem",
               "base-ecosystem",
               "opbnb-ecosystem"
           ],
           "exchanges": []
       },
       "DEXE": {
           "name": "DeXe",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/DEXE.png",
           "urls": {
               "website": "https://dexe.network/",
               "technical_doc": "https://whitepaper.dexe.network/",
               "cmc": "https://coinmarketcap.com/currencies/dexe"
           },
           "sector_tags": [
               "asset-management",
               "ai-big-data",
               "defi",
               "dao",
               "ethereum-ecosystem",
               "bnb-chain-ecosystem"
           ],
           "exchanges": []
       },
       "CFX": {
           "name": "Conflux",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CFX.png",
           "urls": {
               "website": "https://confluxnetwork.org/",
               "technical_doc": "https://confluxnetwork.org/files/Conflux_Economic_Paper_20201230.pdf",
               "cmc": "https://coinmarketcap.com/currencies/conflux-network"
           },
           "sector_tags": [
               "mineable",
               "pow",
               "platform",
               "collectibles-nfts",
               "defi",
               "payments",
               "research",
               "smart-contracts",
               "staking",
               "hybrid-pow-dpos",
               "huobi-capital-portfolio",
               "web3",
               "injective-ecosystem",
               "bnb-chain-ecosystem",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "LQTY": {
           "name": "Liquity",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LQTY.png",
           "urls": {
               "website": "https://www.liquity.org/",
               "technical_doc": "https://docsend.com/view/bwiczmy",
               "cmc": "https://coinmarketcap.com/currencies/liquity"
           },
           "sector_tags": [
               "defi",
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "AKT": {
           "name": "Akash Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AKT.png",
           "urls": {
               "website": "https://akash.network",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/akash-network"
           },
           "sector_tags": [
               "cosmos-ecosystem",
               "ai-big-data",
               "distributed-computing",
               "web3",
               "injective-ecosystem",
               "osmosis-ecosystem",
               "generative-ai",
               "depin",
               "made-in-america"
           ],
           "exchanges": []
       },
       "AUDIO": {
           "name": "Audius",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AUDIO.png",
           "urls": {
               "website": "https://audius.co/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/audius"
           },
           "sector_tags": [
               "music",
               "dao",
               "ethereum-ecosystem",
               "coinbase-ventures-portfolio",
               "binance-labs-portfolio",
               "solana-ecosystem",
               "pantera-capital-portfolio",
               "multicoin-capital-portfolio",
               "web3",
               "standard-crypto-portfolio",
               "gnosis-chain-ecosystem"
           ],
           "exchanges": []
       },
       "POND": {
           "name": "Marlin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/POND.png",
           "urls": {
               "website": "https://www.marlin.org",
               "technical_doc": "https://docs.marlin.org",
               "cmc": "https://coinmarketcap.com/currencies/marlin"
           },
           "sector_tags": [
               "ai-big-data",
               "distributed-computing",
               "defi",
               "zero-knowledge-proofs",
               "scaling",
               "ethereum-ecosystem",
               "binance-labs-portfolio",
               "solana-ecosystem",
               "arrington-xrp-capital-portfolio",
               "electric-capital-portfolio",
               "polygon-ecosystem",
               "web3",
               "arbitrum-ecosystem",
               "injective-ecosystem",
               "generative-ai",
               "dwf-labs-portfolio",
               "modular-blockchain",
               "depin"
           ],
           "exchanges": []
       },
       "WOO": {
           "name": "WOO",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/WOO.png",
           "urls": {
               "website": "https://learn.woo.org/wooooo-the-token/what-is-woo",
               "technical_doc": "https://learn.woo.org",
               "cmc": "https://coinmarketcap.com/currencies/wootrade"
           },
           "sector_tags": [
               "centralized-exchange",
               "decentralized-exchange-dex-token",
               "defi",
               "ethereum-ecosystem",
               "yield-farming",
               "amm",
               "three-arrows-capital-portfolio",
               "exnetwork-capital-portfolio",
               "polygon-ecosystem",
               "near-protocol-ecosystem",
               "arbitrum-ecosystem",
               "mvb",
               "vbc-ventures-portfolio",
               "injective-ecosystem",
               "bnb-chain-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "ORAI": {
           "name": "Oraichain",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ORAI.png",
           "urls": {
               "website": "https://orai.io/",
               "technical_doc": "https://docs.orai.io/",
               "cmc": "https://coinmarketcap.com/currencies/oraichain-token"
           },
           "sector_tags": [
               "cosmos-ecosystem",
               "ai-big-data",
               "defi",
               "oracles",
               "research",
               "ethereum-ecosystem",
               "injective-ecosystem",
               "bnb-chain-ecosystem",
               "generative-ai",
               "real-world-assets",
               "layer-1",
               "dwf-labs-portfolio",
               "ai-agents"
           ],
           "exchanges": []
       },
       "ROSE": {
           "name": "Oasis",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ROSE.png",
           "urls": {
               "website": "https://oasisprotocol.org/",
               "technical_doc": "https://docs.oasis.io/",
               "cmc": "https://coinmarketcap.com/currencies/oasis-network"
           },
           "sector_tags": [
               "cosmos-ecosystem",
               "ai-big-data",
               "privacy",
               "scaling",
               "smart-contracts",
               "ethereum-ecosystem",
               "polychain-capital-portfolio",
               "binance-labs-portfolio",
               "solana-ecosystem",
               "arrington-xrp-capital-portfolio",
               "blockchain-capital-portfolio",
               "dragonfly-capital-portfolio",
               "electric-capital-portfolio",
               "kenetic-capital-portfolio",
               "huobi-capital-portfolio",
               "a16z-portfolio",
               "winklevoss-capital-portfolio",
               "pantera-capital-portfolio",
               "injective-ecosystem",
               "oasis-ecosystem",
               "layer-1"
           ],
           "exchanges": []
       },
       "TRU": {
           "name": "TrueFi",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/TRU.png",
           "urls": {
               "website": "https://truefi.io/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/truefi-token"
           },
           "sector_tags": [
               "defi",
               "ethereum-ecosystem",
               "yield-farming",
               "avalanche-ecosystem",
               "lending-borowing",
               "real-world-assets"
           ],
           "exchanges": []
       },
       "API3": {
           "name": "API3",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/API3.png",
           "urls": {
               "website": "https://api3.org/",
               "technical_doc": "https://drive.google.com/file/d/1JMVwk9pkGF7hvjkuu6ABA0-FrhRTzAwF/view?usp=sharing",
               "cmc": "https://coinmarketcap.com/currencies/api3"
           },
           "sector_tags": [
               "defi",
               "oracles",
               "ethereum-ecosystem",
               "coinfund-portfolio",
               "dcg-portfolio",
               "placeholder-ventures-portfolio",
               "pantera-capital-portfolio",
               "polygon-ecosystem",
               "fantom-ecosystem",
               "cardano-ecosystem",
               "web3",
               "cardano",
               "injective-ecosystem"
           ],
           "exchanges": []
       },
       "BADGER": {
           "name": "Badger DAO",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BADGER.png",
           "urls": {
               "website": "https://app.badger.finance/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/badger-dao"
           },
           "sector_tags": [
               "defi",
               "dao",
               "ethereum-ecosystem",
               "yield-farming",
               "yield-aggregator",
               "governance",
               "blockchain-capital-portfolio",
               "arbitrum-ecosystem",
               "bitcoin-ecosystem",
               "gnosis-chain-ecosystem"
           ],
           "exchanges": []
       },
       "FLR": {
           "name": "Flare",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FLR.png",
           "urls": {
               "website": "https://flare.network",
               "technical_doc": "https://docs.flare.network/",
               "cmc": "https://coinmarketcap.com/currencies/flare"
           },
           "sector_tags": [
               "layer-1",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "FIDA": {
           "name": "Solana Name Service",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FIDA.png",
           "urls": {
               "website": "https://www.sns.id/",
               "technical_doc": "https://www.bonfida.org/white-paper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/solana-name-service"
           },
           "sector_tags": [
               "defi",
               "three-arrows-capital-portfolio",
               "solana-ecosystem",
               "cms-holdings-portfolio",
               "kenetic-capital-portfolio",
               "ftx-bankruptcy-estate",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "LDO": {
           "name": "Lido DAO",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/LDO.png",
           "urls": {
               "website": "https://lido.fi/",
               "technical_doc": "https://lido.fi/static/Lido:Ethereum-Liquid-Staking.pdf",
               "cmc": "https://coinmarketcap.com/currencies/lido-dao"
           },
           "sector_tags": [
               "defi",
               "dao",
               "ethereum-ecosystem",
               "three-arrows-capital-portfolio",
               "solana-ecosystem",
               "paradigm-portfolio",
               "arbitrum-ecosystem",
               "liquid-staking-derivatives",
               "optimism-ecosystem",
               "standard-crypto-portfolio",
               "egirl-capital-portfolio",
               "cmc-crypto-yearbook-2024-2025"
           ],
           "exchanges": []
       },
       "VANRY": {
           "name": "Vanar Chain",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/VANRY.png",
           "urls": {
               "website": "https://vanarchain.com/",
               "technical_doc": "https://cdn.vanarchain.com/vanarchain/vanar_whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/vanar"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "exnetwork-capital-portfolio",
               "polygon-ecosystem",
               "layer-1"
           ],
           "exchanges": []
       },
       "1INCH": {
           "name": "1inch Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/1INCH.png",
           "urls": {
               "website": "https://1inch.io/",
               "technical_doc": "https://docs.1inch.io/",
               "cmc": "https://coinmarketcap.com/currencies/1inch"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "defi",
               "wallet",
               "ethereum-ecosystem",
               "amm",
               "binance-labs-portfolio",
               "solana-ecosystem",
               "blockchain-capital-portfolio",
               "dragonfly-capital-portfolio",
               "fabric-ventures-portfolio",
               "alameda-research-portfolio",
               "parafi-capital",
               "near-protocol-ecosystem",
               "spartan-group",
               "bnb-chain-ecosystem",
               "celsius-bankruptcy-estate",
               "gnosis-chain-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "SUPER": {
           "name": "SuperVerse",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/SUPER.png",
           "urls": {
               "website": "https://superverse.co/",
               "technical_doc": "https://docs.superverse.cool/",
               "cmc": "https://coinmarketcap.com/currencies/superverse"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "ethereum-ecosystem",
               "launchpad",
               "polkastarter",
               "exnetwork-capital-portfolio",
               "superstarter",
               "polygon-ecosystem",
               "animoca-brands-portfolio",
               "injective-ecosystem",
               "bnb-chain-ecosystem"
           ],
           "exchanges": []
       },
       "CLV": {
           "name": "CLV",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CLV.png",
           "urls": {
               "website": "https://clv.org/",
               "technical_doc": "https://docs.clv.org/",
               "cmc": "https://coinmarketcap.com/currencies/clover"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "substrate",
               "fantom-ecosystem",
               "okx-ventures-portfolio",
               "bnb-chain-ecosystem"
           ],
           "exchanges": []
       },
       "JASMY": {
           "name": "JasmyCoin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/JASMY.png",
           "urls": {
               "website": "https://www.jasmy.co.jp/en_company.html",
               "technical_doc": "https://www.jasmy.co.jp/images/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/jasmy"
           },
           "sector_tags": [
               "iot",
               "ethereum-ecosystem",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "RAY": {
           "name": "Raydium",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/RAY.png",
           "urls": {
               "website": "https://raydium.io/#/",
               "technical_doc": "https://raydium.io/Raydium-Litepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/raydium"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "defi",
               "amm",
               "dex",
               "solana-ecosystem",
               "petrock-capital-portfolio",
               "defi"
           ],
           "exchanges": []
       },
       "MASK": {
           "name": "Mask Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MASK.png",
           "urls": {
               "website": "https://www.mask.io",
               "technical_doc": "https://masknetwork.medium.com/introducing-mask-network-maskbook-the-future-of-the-internet-5a973d874edd",
               "cmc": "https://coinmarketcap.com/currencies/mask-network"
           },
           "sector_tags": [
               "communications-social-media",
               "collectibles-nfts",
               "content-creation",
               "ethereum-ecosystem",
               "metaverse",
               "polygon-ecosystem",
               "web3",
               "animoca-brands-portfolio",
               "injective-ecosystem",
               "bnb-chain-ecosystem",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "AUCTION": {
           "name": "Bounce Token",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AUCTION.png",
           "urls": {
               "website": "https://bounce.finance/",
               "technical_doc": "https://docs.bounce.finance/",
               "cmc": "https://coinmarketcap.com/currencies/bounce-token"
           },
           "sector_tags": [
               "defi",
               "ethereum-ecosystem",
               "bounce-launchpad",
               "bnb-chain-ecosystem",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "ALCX": {
           "name": "Alchemix",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ALCX.png",
           "urls": {
               "website": "https://alchemix.fi/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/alchemix"
           },
           "sector_tags": [
               "defi",
               "ethereum-ecosystem",
               "yield-farming",
               "governance",
               "lending-borowing",
               "olympus-pro-ecosystem",
               "near-protocol-ecosystem",
               "defi-2",
               "protocol-owned-liquidity",
               "spartan-group",
               "egirl-capital-portfolio"
           ],
           "exchanges": []
       },
       "ERN": {
           "name": "Ethernity Chain",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ERN.png",
           "urls": {
               "website": "https://ethernity.io/",
               "technical_doc": "https://drive.google.com/file/d/1o74ZBmYMIUa8QUwvLOK7KhoXP9-LCsPf/view",
               "cmc": "https://coinmarketcap.com/currencies/ethernity-chain"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "ethereum-ecosystem",
               "layer-2",
               "metaverse",
               "polkastarter",
               "polygon-ecosystem"
           ],
           "exchanges": []
       },
       "MINA": {
           "name": "Mina",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MINA.png",
           "urls": {
               "website": "https://minaprotocol.com/",
               "technical_doc": "https://minaprotocol.com/docs",
               "cmc": "https://coinmarketcap.com/currencies/mina"
           },
           "sector_tags": [
               "pos",
               "zero-knowledge-proofs",
               "staking",
               "coinbase-ventures-portfolio",
               "three-arrows-capital-portfolio",
               "polychain-capital-portfolio"
           ],
           "exchanges": []
       },
       "ILV": {
           "name": "Illuvium",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ILV.png",
           "urls": {
               "website": "https://illuvium.io/",
               "technical_doc": "https://docs.illuvium.io/illuvium-whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/illuvium"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "dao",
               "ethereum-ecosystem",
               "metaverse",
               "play-to-earn"
           ],
           "exchanges": []
       },
       "BLUE": {
           "name": "Bluefin",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BLUE.png",
           "urls": {
               "website": "https://bluefin.io/",
               "technical_doc": "https://learn.bluefin.io/bluefin",
               "cmc": "https://coinmarketcap.com/currencies/bluefin"
           },
           "sector_tags": [
               "decentralized-exchange-dex-token",
               "defi",
               "derivatives",
               "substrate",
               "polkadot",
               "dex",
               "sui-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "ALICE": {
           "name": "MyNeighborAlice",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ALICE.png",
           "urls": {
               "website": "https://www.myneighboralice.com/",
               "technical_doc": "https://whitepaper.myneighboralice.com/",
               "cmc": "https://coinmarketcap.com/currencies/myneighboralice"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "entertainment",
               "ethereum-ecosystem",
               "binance-launchpool",
               "solana-ecosystem",
               "metaverse",
               "dao-maker",
               "exnetwork-capital-portfolio",
               "chromia-ecosystem",
               "play-to-earn"
           ],
           "exchanges": []
       },
       "ICP": {
           "name": "Internet Computer",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/ICP.png",
           "urls": {
               "website": "https://internetcomputer.org/",
               "technical_doc": "https://internetcomputer.org/whitepaper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/internet-computer"
           },
           "sector_tags": [
               "platform",
               "ai-big-data",
               "smart-contracts",
               "polychain-capital-portfolio",
               "electric-capital-portfolio",
               "fenbushi-capital-portfolio",
               "hashkey-capital-portfolio",
               "a16z-portfolio",
               "multicoin-capital-portfolio",
               "exnetwork-capital-portfolio",
               "icp-ecosystem",
               "internet-computer-ecosystem",
               "layer-1",
               "alleged-sec-securities"
           ],
           "exchanges": []
       },
       "COOK": {
           "name": "Cook Finance",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/COOK.png",
           "urls": {
               "website": "https://www.cook.finance/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/cook-protocol"
           },
           "sector_tags": [
               "asset-management",
               "defi",
               "ethereum-ecosystem",
               "duckstarter",
               "poolz-finance-portfolio"
           ],
           "exchanges": []
       },
       "PUNDIX": {
           "name": "Pundi X (New)",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PUNDIX.png",
           "urls": {
               "website": "https://pundix.com/",
               "technical_doc": "https://pundix.com/pdf/PundiX_Whitepaper_EN_Ver.pdf",
               "cmc": "https://coinmarketcap.com/currencies/pundix-new"
           },
           "sector_tags": [
               "ethereum-ecosystem"
           ],
           "exchanges": []
       },
       "OHM,UNISWAP_V3,0X64AA3364F17A4D01C6F1751FD97C2BD3D7E7F1D5": {
           "name": "Olympus v2",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/OHM,UNISWAP_V3,0X64AA3364F17A4D01C6F1751FD97C2BD3D7E7F1D5.png",
           "urls": {
               "website": "https://olympusdao.finance/#/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/olympus"
           },
           "sector_tags": [
               "ethereum-ecosystem",
               "solana-ecosystem",
               "olympus-pro-ecosystem",
               "defi-2",
               "protocol-owned-liquidity"
           ],
           "exchanges": []
       },
       "AIOZ": {
           "name": "AIOZ Network",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/AIOZ.png",
           "urls": {
               "website": "https://aioz.network/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/aioz-network"
           },
           "sector_tags": [
               "media",
               "platform",
               "ai-big-data",
               "distributed-computing",
               "smart-contracts",
               "video",
               "ethereum-ecosystem",
               "storage",
               "web3",
               "bnb-chain-ecosystem",
               "generative-ai",
               "layer-1",
               "dwf-labs-portfolio",
               "depin"
           ],
           "exchanges": []
       },
       "MBOX": {
           "name": "MOBOX",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MBOX.png",
           "urls": {
               "website": "https://www.mobox.io/#/",
               "technical_doc": "https://faqen.mobox.io",
               "cmc": "https://coinmarketcap.com/currencies/mobox"
           },
           "sector_tags": [
               "collectibles-nfts",
               "defi",
               "gaming",
               "yield-farming",
               "binance-launchpool",
               "metaverse",
               "play-to-earn",
               "bnb-chain-ecosystem",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "XCH": {
           "name": "Chia",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/XCH.png",
           "urls": {
               "website": "https://www.chia.net/",
               "technical_doc": "https://www.chia.net/whitepaper/",
               "cmc": "https://coinmarketcap.com/currencies/chia-network"
           },
           "sector_tags": [
               "mineable",
               "real-world-assets",
               "layer-1"
           ],
           "exchanges": []
       },
       "MOVR": {
           "name": "Moonriver",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/MOVR.png",
           "urls": {
               "website": "https://moonbeam.network/networks/moonriver/",
               "technical_doc": "https://docs.moonbeam.network/",
               "cmc": "https://coinmarketcap.com/currencies/moonriver"
           },
           "sector_tags": [
               "interoperability",
               "smart-contracts",
               "polkadot-ecosystem",
               "moonriver-ecosystem",
               "layer-1"
           ],
           "exchanges": []
       },
       "QI": {
           "name": "BENQI",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/QI.png",
           "urls": {
               "website": "https://benqi.fi/",
               "technical_doc": "https://docs.benqi.fi/",
               "cmc": "https://coinmarketcap.com/currencies/benqi"
           },
           "sector_tags": [
               "defi",
               "binance-launchpool",
               "avalanche-ecosystem",
               "lending-borowing",
               "arrington-xrp-capital-portfolio",
               "spartan-group",
               "liquid-staking-derivatives"
           ],
           "exchanges": []
       },
       "PYR": {
           "name": "Vulcan Forged (PYR)",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PYR.png",
           "urls": {
               "website": "https://vulcanforged.com/",
               "technical_doc": "https://vulcan-forged.gitbook.io/whitepaper",
               "cmc": "https://coinmarketcap.com/currencies/vulcan-forged-pyr"
           },
           "sector_tags": [
               "collectibles-nfts",
               "gaming",
               "entertainment",
               "ethereum-ecosystem",
               "metaverse",
               "polygon-ecosystem",
               "dwf-labs-portfolio"
           ],
           "exchanges": []
       },
       "FORTH": {
           "name": "Ampleforth Governance Token",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/FORTH.png",
           "urls": {
               "website": "https://www.ampleforth.org/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/ampleforth-governance-token"
           },
           "sector_tags": [
               "dao",
               "ethereum-ecosystem",
               "governance",
               "near-protocol-ecosystem"
           ],
           "exchanges": []
       },
       "KNC": {
           "name": "Kyber Network Crystal v2",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/KNC.png",
           "urls": {
               "website": "https://kyberswap.com/",
               "technical_doc": "https://docs.kyberswap.com/introduction",
               "cmc": "https://coinmarketcap.com/currencies/kyber-network-crystal-v2"
           },
           "sector_tags": [
               "marketplace",
               "decentralized-exchange-dex-token",
               "defi",
               "dao",
               "ethereum-ecosystem",
               "amm",
               "three-arrows-capital-portfolio",
               "governance",
               "kenetic-capital-portfolio",
               "pantera-capital-portfolio",
               "parafi-capital",
               "polygon-ecosystem",
               "arbitrum-ecosystem",
               "optimism-ecosystem",
               "defi"
           ],
           "exchanges": []
       },
       "PENDLE": {
           "name": "Pendle",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/PENDLE.png",
           "urls": {
               "website": "https://pendle.finance/",
               "technical_doc": "https://docs.pendle.finance/resources/lite-paper",
               "cmc": "https://coinmarketcap.com/currencies/pendle"
           },
           "sector_tags": [
               "defi",
               "ethereum-ecosystem",
               "olympus-pro-ecosystem",
               "spartan-group",
               "real-world-assets",
               "restaking",
               "eigenlayer-ecosystem",
               "cmc-crypto-yearbook-2024-2025",
               "btcfi"
           ],
           "exchanges": []
       },
       "BICO": {
           "name": "Biconomy",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/BICO.png",
           "urls": {
               "website": "https://biconomy.io",
               "technical_doc": "https://docs.biconomy.io",
               "cmc": "https://coinmarketcap.com/currencies/biconomy"
           },
           "sector_tags": [
               "platform",
               "interoperability",
               "ethereum-ecosystem",
               "coinbase-ventures-portfolio",
               "binance-labs-portfolio",
               "fenbushi-capital-portfolio",
               "mvb",
               "dwf-labs-portfolio",
               "account-abstraction"
           ],
           "exchanges": []
       },
       "METIS": {
           "name": "Metis",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/METIS.png",
           "urls": {
               "website": "https://www.metis.io",
               "technical_doc": "https://drive.google.com/file/d/1LS7CmKFt-FkfVXxSNu06hNgoZXxMzTC-/view",
               "cmc": "https://coinmarketcap.com/currencies/metisdao"
           },
           "sector_tags": [
               "zero-knowledge-proofs",
               "scaling",
               "ethereum-ecosystem",
               "layer-2",
               "rollups",
               "okx-ventures-portfolio",
               "metisdao-ecosystem",
               "dwf-labs-portfolio",
               "metis-andromeda-ecosystem"
           ],
           "exchanges": []
       },
       "NFT": {
           "name": "APENFT",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/NFT.png",
           "urls": {
               "website": "http://apenft.org/",
               "technical_doc": "https://foundation.apenft.io/book/APENFT%20White%20Paper.pdf",
               "cmc": "https://coinmarketcap.com/currencies/apenft"
           },
           "sector_tags": [
               "art",
               "collectibles-nfts",
               "ethereum-ecosystem",
               "tron-ecosystem",
               "heco-ecosystem",
               "tron20-ecosystem"
           ],
           "exchanges": []
       },
       "CVX": {
           "name": "Convex Finance",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/CVX.png",
           "urls": {
               "website": "https://www.convexfinance.com/",
               "technical_doc": null,
               "cmc": "https://coinmarketcap.com/currencies/convex-finance"
           },
           "sector_tags": [
               "defi",
               "ethereum-ecosystem",
               "yield-aggregator",
               "solana-ecosystem"
           ],
           "exchanges": []
       },
       "HARRISWINPOP": {
           "name": "Harris Popular Vote",
           "logo": "https://mainnet-metadata-service-logos.s3.ap-northeast-1.amazonaws.com/HARRISWINPOP.png",
           "urls": {
               "website": "https://polymarket.com/event/presidential-election-popular-vote-winner-2024",
               "technical_doc": null,
               "cmc": null
           },
           "sector_tags": [
               "prediction-market"
           ],
           "exchanges": [],
           "enabled": true
       }
   }
    """.trimIndent()
}
