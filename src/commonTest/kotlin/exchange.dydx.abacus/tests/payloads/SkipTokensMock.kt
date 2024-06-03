package exchange.dydx.abacus.tests.payloads

class SkipTokensMock {
    internal val defaultChainIdAssets = """{
            "assets": [
                {
                    "denom": "0x97e6E0a40a3D02F12d1cEC30ebfbAE04e37C119E",
                    "chain_id": "1",
                    "origin_denom": "0x97e6E0a40a3D02F12d1cEC30ebfbAE04e37C119E",
                    "origin_chain_id": "1",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "YieldUSD",
                    "name": "Real Yield USD",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/yieldusd.svg",
                    "decimals": 18,
                    "token_contract": "0x97e6E0a40a3D02F12d1cEC30ebfbAE04e37C119E",
                    "recommended_symbol": "YieldUSD"
                },
                {
                    "denom": "0x1aBaEA1f7C830bD89Acc67eC4af516284b1bC33c",
                    "chain_id": "1",
                    "origin_denom": "0x1aBaEA1f7C830bD89Acc67eC4af516284b1bC33c",
                    "origin_chain_id": "1",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "EUROC",
                    "name": "Euro Coin",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/euroc.svg",
                    "decimals": 6,
                    "token_contract": "0x1aBaEA1f7C830bD89Acc67eC4af516284b1bC33c",
                    "coingecko_id": "euro-coin",
                    "recommended_symbol": "EUROC"
                },
                {
                    "denom": "0x923e030f951A2401426a3407a9bcc7EB715d9a0b",
                    "chain_id": "1",
                    "origin_denom": "0x923e030f951A2401426a3407a9bcc7EB715d9a0b",
                    "origin_chain_id": "1",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "UMEE",
                    "name": "Umee native token",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/umee.svg",
                    "decimals": 6,
                    "token_contract": "0x923e030f951A2401426a3407a9bcc7EB715d9a0b",
                    "coingecko_id": "umee",
                    "recommended_symbol": "UMEE"
                }
            ]
        }"""
    internal val payload = """{
    "chain_to_assets_map": {
        "1": $defaultChainIdAssets,
        "5": {
            "assets": [
                {
                    "denom": "0xB4FBF271143F4FBf7B91A5ded31805e42b2208d6",
                    "chain_id": "5",
                    "origin_denom": "0xB4FBF271143F4FBf7B91A5ded31805e42b2208d6",
                    "origin_chain_id": "5",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "WETH",
                    "name": "weth",
                    "logo_uri": "https://raw.githubusercontent.com/cosmostation/chainlist/main/chain/ethereum/asset/weth.png",
                    "decimals": 18,
                    "token_contract": "0xB4FBF271143F4FBf7B91A5ded31805e42b2208d6",
                    "coingecko_id": "weth",
                    "recommended_symbol": "WETH"
                },
                {
                    "denom": "0x9c3C9283D3e44854697Cd22D3Faa240Cfb032889",
                    "chain_id": "5",
                    "origin_denom": "0x9c3C9283D3e44854697Cd22D3Faa240Cfb032889",
                    "origin_chain_id": "5",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "WMATIC",
                    "name": "wmatic-wei",
                    "logo_uri": "https://raw.githubusercontent.com/cosmostation/chainlist/main/chain/polygon/asset/wmatic.png",
                    "decimals": 18,
                    "token_contract": "0x9c3C9283D3e44854697Cd22D3Faa240Cfb032889",
                    "coingecko_id": "matic-network",
                    "recommended_symbol": "WMATIC"
                },
                {
                    "denom": "",
                    "chain_id": "5",
                    "origin_denom": "",
                    "origin_chain_id": "5",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "ETH",
                    "name": "eth",
                    "logo_uri": "https://raw.githubusercontent.com/cosmostation/chainlist/main/chain/ethereum/asset/eth.png",
                    "decimals": 18,
                    "coingecko_id": "ethereum",
                    "recommended_symbol": "ETH"
                }
            ]
        },
        "10": {
            "assets": [
                {
                    "denom": "0x789CbBE5d19f04F38Ec9790b28Ecb07ba5617f61",
                    "chain_id": "10",
                    "origin_denom": "0x789CbBE5d19f04F38Ec9790b28Ecb07ba5617f61",
                    "origin_chain_id": "10",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "stTIA.axl",
                    "name": "Stride Staked Tia",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/sttia.svg",
                    "decimals": 6,
                    "token_contract": "0x789CbBE5d19f04F38Ec9790b28Ecb07ba5617f61",
                    "coingecko_id": "stride-staked-tia",
                    "recommended_symbol": "stTIA.axl"
                },
                {
                    "denom": "0xb829b68f57CC546dA7E5806A929e53bE32a4625D",
                    "chain_id": "10",
                    "origin_denom": "0xb829b68f57CC546dA7E5806A929e53bE32a4625D",
                    "origin_chain_id": "10",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "axlETH",
                    "name": "Axelar Wrapped ETH",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/weth.svg",
                    "decimals": 18,
                    "token_contract": "0xb829b68f57CC546dA7E5806A929e53bE32a4625D",
                    "coingecko_id": "weth",
                    "recommended_symbol": "ETH.axl"
                },
                {
                    "denom": "0x4200000000000000000000000000000000000042",
                    "chain_id": "10",
                    "origin_denom": "0x4200000000000000000000000000000000000042",
                    "origin_chain_id": "10",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "OP",
                    "name": "Optimism",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/op.svg",
                    "decimals": 18,
                    "token_contract": "0x4200000000000000000000000000000000000042",
                    "coingecko_id": "optimism",
                    "recommended_symbol": "OP"
                }
            ]
        },
        "56": {
            "assets": [
                {
                    "denom": "0x43a8cab15D06d3a5fE5854D714C37E7E9246F170",
                    "chain_id": "56",
                    "origin_denom": "0x43a8cab15D06d3a5fE5854D714C37E7E9246F170",
                    "origin_chain_id": "56",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "ORBS",
                    "name": "Orbs",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/orbs.svg",
                    "decimals": 18,
                    "token_contract": "0x43a8cab15D06d3a5fE5854D714C37E7E9246F170",
                    "coingecko_id": "orbs",
                    "recommended_symbol": "ORBS"
                },
                {
                    "denom": "0x7C8DbFdB185C088E73999770C93b885295805739",
                    "chain_id": "56",
                    "origin_denom": "0x7C8DbFdB185C088E73999770C93b885295805739",
                    "origin_chain_id": "56",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "MOON",
                    "name": "Moonflow",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/moon.svg",
                    "decimals": 18,
                    "token_contract": "0x7C8DbFdB185C088E73999770C93b885295805739",
                    "recommended_symbol": "MOON"
                },
                {
                    "denom": "0xF700D4c708C2be1463E355F337603183D20E0808",
                    "chain_id": "56",
                    "origin_denom": "0xF700D4c708C2be1463E355F337603183D20E0808",
                    "origin_chain_id": "56",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "GQ",
                    "name": "Galactic Quadrant",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/gq.svg",
                    "decimals": 18,
                    "token_contract": "0xF700D4c708C2be1463E355F337603183D20E0808",
                    "recommended_symbol": "GQ"
                }
            ]
        },
        "137": {
            "assets": [
                {
                    "denom": "0x1ED2B2b097E92B2Fe95a172dd29840c71294F1d6",
                    "chain_id": "137",
                    "origin_denom": "0x1ED2B2b097E92B2Fe95a172dd29840c71294F1d6",
                    "origin_chain_id": "137",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "sFRAX",
                    "name": "Staked FRAX",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/sfrax.svg",
                    "decimals": 18,
                    "token_contract": "0x1ED2B2b097E92B2Fe95a172dd29840c71294F1d6",
                    "recommended_symbol": "sFRAX"
                },
                {
                    "denom": "0x779661872e9C891027099C9E3fd101DCc8B96433",
                    "chain_id": "137",
                    "origin_denom": "0x779661872e9C891027099C9E3fd101DCc8B96433",
                    "origin_chain_id": "137",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "axlWBTC",
                    "name": "Axelar Wrapped WBTC",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/wbtc.svg",
                    "decimals": 8,
                    "token_contract": "0x779661872e9C891027099C9E3fd101DCc8B96433",
                    "coingecko_id": "wrapped-bitcoin",
                    "recommended_symbol": "WBTC.axl"
                },
                {
                    "denom": "0x0294D8eB7857D43FEb1210Db72456d41481f9Ede",
                    "chain_id": "137",
                    "origin_denom": "0x0294D8eB7857D43FEb1210Db72456d41481f9Ede",
                    "origin_chain_id": "137",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "axlLqdr",
                    "name": "Axelar Wrapped Lqdr",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/lqdr.svg",
                    "decimals": 18,
                    "token_contract": "0x0294D8eB7857D43FEb1210Db72456d41481f9Ede",
                    "coingecko_id": "liquiddriver",
                    "recommended_symbol": "Lqdr.axl"
                }
            ]
        },
        "169": {
            "assets": [
                {
                    "denom": "0x6Fae4D9935E2fcb11fC79a64e917fb2BF14DaFaa",
                    "chain_id": "169",
                    "origin_denom": "0x6Fae4D9935E2fcb11fC79a64e917fb2BF14DaFaa",
                    "origin_chain_id": "169",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "TIA.n",
                    "name": "TIA.n",
                    "logo_uri": "https://raw.githubusercontent.com/cosmos/chain-registry/master/celestia/images/celestia.png",
                    "decimals": 6,
                    "token_contract": "0x6Fae4D9935E2fcb11fC79a64e917fb2BF14DaFaa",
                    "coingecko_id": "bridged-tia-hyperlane",
                    "recommended_symbol": "TIA.n"
                }
            ]
        },
        "250": {
            "assets": [
                {
                    "denom": "0x3bB68cb55Fc9C22511467c18E42D14E8c959c4dA",
                    "chain_id": "250",
                    "origin_denom": "0x3bB68cb55Fc9C22511467c18E42D14E8c959c4dA",
                    "origin_chain_id": "250",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "axlATOM",
                    "name": "Axelar Wrapped ATOM",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/atom.svg",
                    "decimals": 6,
                    "token_contract": "0x3bB68cb55Fc9C22511467c18E42D14E8c959c4dA",
                    "coingecko_id": "cosmos",
                    "recommended_symbol": "ATOM.axl"
                },
                {
                    "denom": "0x11eDFA12d70e8AC9e94DE019eBa278430873f8C3",
                    "chain_id": "250",
                    "origin_denom": "0x11eDFA12d70e8AC9e94DE019eBa278430873f8C3",
                    "origin_chain_id": "250",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "TORI",
                    "name": "Teritori",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/tori.svg",
                    "decimals": 6,
                    "token_contract": "0x11eDFA12d70e8AC9e94DE019eBa278430873f8C3",
                    "recommended_symbol": "TORI"
                },
                {
                    "denom": "0x05E7857Cb748F0018C0CBCe3dfd575B0d8677aeF",
                    "chain_id": "250",
                    "origin_denom": "0x05E7857Cb748F0018C0CBCe3dfd575B0d8677aeF",
                    "origin_chain_id": "250",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "FXS",
                    "name": "Frax Share",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/fxs.svg",
                    "decimals": 18,
                    "token_contract": "0x05E7857Cb748F0018C0CBCe3dfd575B0d8677aeF",
                    "recommended_symbol": "FXS"
                }
            ]
        },
        "314": {
            "assets": [
                {
                    "denom": "0xEB466342C4d449BC9f53A865D5Cb90586f405215",
                    "chain_id": "314",
                    "origin_denom": "0xEB466342C4d449BC9f53A865D5Cb90586f405215",
                    "origin_chain_id": "314",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "axlUSDC",
                    "name": "Axelar Wrapped USDC",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/usdc.svg",
                    "decimals": 6,
                    "token_contract": "0xEB466342C4d449BC9f53A865D5Cb90586f405215",
                    "coingecko_id": "usd-coin",
                    "recommended_symbol": "USDC.axl"
                },
                {
                    "denom": "0x4AA81D7AB59C775fe6F9F45E6941A0FB8cD692a6",
                    "chain_id": "314",
                    "origin_denom": "0x4AA81D7AB59C775fe6F9F45E6941A0FB8cD692a6",
                    "origin_chain_id": "314",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "milkTIA",
                    "name": "milkTIA",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/milktia.svg",
                    "decimals": 6,
                    "token_contract": "0x4AA81D7AB59C775fe6F9F45E6941A0FB8cD692a6",
                    "coingecko_id": "milkyway-staked-tia",
                    "recommended_symbol": "milkTIA"
                },
                {
                    "denom": "filecoin-native",
                    "chain_id": "314",
                    "origin_denom": "filecoin-native",
                    "origin_chain_id": "314",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "FIL",
                    "name": "FIL",
                    "logo_uri": "https://assets.coingecko.com/coins/images/12817/standard/filecoin.png?1696512609",
                    "decimals": 18,
                    "coingecko_id": "filecoin",
                    "recommended_symbol": "FIL"
                }
            ]
        },
        "1284": {
            "assets": [
                {
                    "denom": "0x151904806a266EEe52700E195D2937891fb8eD59",
                    "chain_id": "1284",
                    "origin_denom": "0x151904806a266EEe52700E195D2937891fb8eD59",
                    "origin_chain_id": "1284",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "FXS",
                    "name": "Frax Share",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/fxs.svg",
                    "decimals": 18,
                    "token_contract": "0x151904806a266EEe52700E195D2937891fb8eD59",
                    "recommended_symbol": "FXS"
                },
                {
                    "denom": "0x5Ac3aD1acC0A3EFd6fB89791967656128e86d8C5",
                    "chain_id": "1284",
                    "origin_denom": "0x5Ac3aD1acC0A3EFd6fB89791967656128e86d8C5",
                    "origin_chain_id": "1284",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "axlKNC",
                    "name": "Axelar Wrapped KNC",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/knc.svg",
                    "decimals": 18,
                    "token_contract": "0x5Ac3aD1acC0A3EFd6fB89791967656128e86d8C5",
                    "coingecko_id": "kyber-network-crystal",
                    "recommended_symbol": "KNC.axl"
                },
                {
                    "denom": "0xF2605EaB29c67d06E71372CA9dfA8aDfd2d34BbF",
                    "chain_id": "1284",
                    "origin_denom": "0xF2605EaB29c67d06E71372CA9dfA8aDfd2d34BbF",
                    "origin_chain_id": "1284",
                    "trace": "",
                    "is_cw20": false,
                    "is_evm": true,
                    "is_svm": false,
                    "symbol": "axlSTARS",
                    "name": "Axelar Wrapped STARS",
                    "logo_uri": "https://raw.githubusercontent.com/axelarnetwork/axelar-configs/main/images/tokens/stars.svg",
                    "decimals": 6,
                    "token_contract": "0xF2605EaB29c67d06E71372CA9dfA8aDfd2d34BbF",
                    "coingecko_id": "stargaze",
                    "recommended_symbol": "STARS.axl"
                }
            ]
        }
    }
}
        
    """.trimMargin()
}
