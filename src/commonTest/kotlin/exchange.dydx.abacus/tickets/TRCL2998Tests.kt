package exchange.dydx.abacus.tickets

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.payload.v4.V4BaseTests
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.model.tradeInMarket
import exchange.dydx.abacus.tests.extensions.socket
import kotlin.test.Test
import kotlin.test.assertEquals

open class TRCL2998Tests : V4BaseTests() {
    private val marketsMock = """
        {
           "type":"subscribed",
           "connection_id":"e488609d-f4fd-4abb-bfbb-eafcc51458a8",
           "message_id":1,
           "channel":"v4_markets",
           "contents":{
              "markets":{
                 "BTC-USD":{
                    "clobPairId":"0",
                    "ticker":"BTC-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"34880.45064",
                    "priceChange24H":"-173.53144",
                    "volume24H":"1725671.4952",
                    "trades24H":42035,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.05",
                    "maintenanceMarginFraction":"0.03",
                    "basePositionNotional":"1000000",
                    "openInterest":"110.8625",
                    "atomicResolution":-10,
                    "quantumConversionExponent":-9,
                    "tickSize":"1",
                    "stepSize":"0.0001",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":100000
                 },
                 "ETH-USD":{
                    "clobPairId":"1",
                    "ticker":"ETH-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"1889.556867",
                    "priceChange24H":"-7.167125",
                    "volume24H":"2047871.0782",
                    "trades24H":75615,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.05",
                    "maintenanceMarginFraction":"0.03",
                    "basePositionNotional":"1000000",
                    "openInterest":"1413.144",
                    "atomicResolution":-9,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.1",
                    "stepSize":"0.001",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":100000
                 },
                 "LINK-USD":{
                    "clobPairId":"2",
                    "ticker":"LINK-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"12.593368766",
                    "priceChange24H":"0.67461177",
                    "volume24H":"1655509.149",
                    "trades24H":33964,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"11731",
                    "atomicResolution":-6,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.001",
                    "stepSize":"1",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "MATIC-USD":{
                    "clobPairId":"3",
                    "ticker":"MATIC-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"0.7073687651",
                    "priceChange24H":"0.0132683341",
                    "volume24H":"1631169.194",
                    "trades24H":36426,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"323620",
                    "atomicResolution":-5,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.0001",
                    "stepSize":"10",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "CRV-USD":{
                    "clobPairId":"4",
                    "ticker":"CRV-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"0.5584245457",
                    "priceChange24H":"-0.0144590293",
                    "volume24H":"1627517.820",
                    "trades24H":37556,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"286480",
                    "atomicResolution":-5,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.0001",
                    "stepSize":"10",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "SOL-USD":{
                    "clobPairId":"5",
                    "ticker":"SOL-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"40.11459778",
                    "priceChange24H":"-0.74665761",
                    "volume24H":"1659978.359",
                    "trades24H":39722,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"4115.8",
                    "atomicResolution":-7,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.01",
                    "stepSize":"0.1",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "ADA-USD":{
                    "clobPairId":"6",
                    "ticker":"ADA-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"0.3570091262",
                    "priceChange24H":"0.0103916149",
                    "volume24H":"1648705.763",
                    "trades24H":40079,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"255390",
                    "atomicResolution":-5,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.0001",
                    "stepSize":"10",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "AVAX-USD":{
                    "clobPairId":"7",
                    "ticker":"AVAX-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"12.83430278",
                    "priceChange24H":"0.4322468",
                    "volume24H":"1783109.042",
                    "trades24H":45999,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"12062.0",
                    "atomicResolution":-7,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.01",
                    "stepSize":"0.1",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "FIL-USD":{
                    "clobPairId":"8",
                    "ticker":"FIL-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"4.284608688",
                    "priceChange24H":"0.18273738",
                    "volume24H":"1633758.842",
                    "trades24H":39021,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"19616",
                    "atomicResolution":-6,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.001",
                    "stepSize":"1",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "LTC-USD":{
                    "clobPairId":"9",
                    "ticker":"LTC-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"73.49905591",
                    "priceChange24H":"1.98896792",
                    "volume24H":"1625708.852",
                    "trades24H":36111,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"1382.9",
                    "atomicResolution":-7,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.01",
                    "stepSize":"0.1",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "DOGE-USD":{
                    "clobPairId":"10",
                    "ticker":"DOGE-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"0.07572848118",
                    "priceChange24H":"0.00474052593",
                    "volume24H":"1645160.102",
                    "trades24H":36672,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"1161200",
                    "atomicResolution":-4,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.00001",
                    "stepSize":"100",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "ATOM-USD":{
                    "clobPairId":"11",
                    "ticker":"ATOM-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"8.488645303",
                    "priceChange24H":"0.12819299",
                    "volume24H":"1628291.241",
                    "trades24H":35529,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"16693",
                    "atomicResolution":-6,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.001",
                    "stepSize":"1",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "DOT-USD":{
                    "clobPairId":"12",
                    "ticker":"DOT-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"4.84289505",
                    "priceChange24H":"0.02470862",
                    "volume24H":"1652675.576",
                    "trades24H":39005,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"19239",
                    "atomicResolution":-6,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.001",
                    "stepSize":"1",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "UNI-USD":{
                    "clobPairId":"13",
                    "ticker":"UNI-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"5.022350532",
                    "priceChange24H":"0.252021665",
                    "volume24H":"1640589.979",
                    "trades24H":38470,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"18394",
                    "atomicResolution":-6,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.001",
                    "stepSize":"1",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "BCH-USD":{
                    "clobPairId":"14",
                    "ticker":"BCH-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"239.6730464",
                    "priceChange24H":"3.1629909",
                    "volume24H":"1651681.406",
                    "trades24H":41246,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"415.06",
                    "atomicResolution":-8,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.1",
                    "stepSize":"0.01",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "TRX-USD":{
                    "clobPairId":"15",
                    "ticker":"TRX-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"0.09777106186",
                    "priceChange24H":"-0.00054807214",
                    "volume24H":"1618383.104",
                    "trades24H":34278,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"1120400",
                    "atomicResolution":-4,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.00001",
                    "stepSize":"100",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "NEAR-USD":{
                    "clobPairId":"16",
                    "ticker":"NEAR-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"1.570344448",
                    "priceChange24H":"-0.058662435",
                    "volume24H":"1750595.156",
                    "trades24H":44762,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"132003",
                    "atomicResolution":-6,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.001",
                    "stepSize":"1",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "MKR-USD":{
                    "clobPairId":"17",
                    "ticker":"MKR-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"1325.995273",
                    "priceChange24H":"-4.761507",
                    "volume24H":"1757067.372",
                    "trades24H":45306,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.2",
                    "maintenanceMarginFraction":"0.1",
                    "basePositionNotional":"100000",
                    "openInterest":"70.346",
                    "atomicResolution":-9,
                    "quantumConversionExponent":-9,
                    "tickSize":"1",
                    "stepSize":"0.001",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "XLM-USD":{
                    "clobPairId":"18",
                    "ticker":"XLM-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"0.1301119863",
                    "priceChange24H":"0.0048995839",
                    "volume24H":"1789583.480",
                    "trades24H":46391,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"1043310",
                    "atomicResolution":-5,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.0001",
                    "stepSize":"10",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "ETC-USD":{
                    "clobPairId":"19",
                    "ticker":"ETC-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"18.26184158",
                    "priceChange24H":"0.14637184",
                    "volume24H":"1712030.586",
                    "trades24H":43525,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"5161.7",
                    "atomicResolution":-7,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.01",
                    "stepSize":"0.1",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "COMP-USD":{
                    "clobPairId":"20",
                    "ticker":"COMP-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"50.46862521",
                    "priceChange24H":"0.88255634",
                    "volume24H":"1628461.407",
                    "trades24H":38046,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.2",
                    "maintenanceMarginFraction":"0.1",
                    "basePositionNotional":"100000",
                    "openInterest":"2514.0",
                    "atomicResolution":-7,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.01",
                    "stepSize":"0.1",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "WLD-USD":{
                    "clobPairId":"21",
                    "ticker":"WLD-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"1.787168587",
                    "priceChange24H":"-0.032138377",
                    "volume24H":"1707262.634",
                    "trades24H":43369,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"52523",
                    "atomicResolution":-6,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.001",
                    "stepSize":"1",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "APE-USD":{
                    "clobPairId":"22",
                    "ticker":"APE-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"1.463071632",
                    "priceChange24H":"0.032107054",
                    "volume24H":"1762431.797",
                    "trades24H":45389,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.2",
                    "maintenanceMarginFraction":"0.1",
                    "basePositionNotional":"100000",
                    "openInterest":"88245",
                    "atomicResolution":-6,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.001",
                    "stepSize":"1",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "APT-USD":{
                    "clobPairId":"23",
                    "ticker":"APT-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"7.228926598",
                    "priceChange24H":"0.27763235",
                    "volume24H":"1649952.320",
                    "trades24H":36875,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"13576",
                    "atomicResolution":-6,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.001",
                    "stepSize":"1",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "ARB-USD":{
                    "clobPairId":"24",
                    "ticker":"ARB-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"1.105759808",
                    "priceChange24H":"-0.001088034",
                    "volume24H":"1823416.386",
                    "trades24H":47361,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"160649",
                    "atomicResolution":-6,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.001",
                    "stepSize":"1",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "BLUR-USD":{
                    "clobPairId":"25",
                    "ticker":"BLUR-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"0.3639862521",
                    "priceChange24H":"0.0869747779",
                    "volume24H":"1696655.724",
                    "trades24H":41896,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.2",
                    "maintenanceMarginFraction":"0.1",
                    "basePositionNotional":"100000",
                    "openInterest":"378010",
                    "atomicResolution":-5,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.0001",
                    "stepSize":"10",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "LDO-USD":{
                    "clobPairId":"26",
                    "ticker":"LDO-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"2.007652352",
                    "priceChange24H":"-0.01124365",
                    "volume24H":"1702730.963",
                    "trades24H":43235,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.2",
                    "maintenanceMarginFraction":"0.1",
                    "basePositionNotional":"100000",
                    "openInterest":"42198",
                    "atomicResolution":-6,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.001",
                    "stepSize":"1",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "OP-USD":{
                    "clobPairId":"27",
                    "ticker":"OP-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"1.541430507",
                    "priceChange24H":"0.011268249",
                    "volume24H":"1722531.101",
                    "trades24H":44198,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"94330",
                    "atomicResolution":-6,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.001",
                    "stepSize":"1",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "PEPE-USD":{
                    "clobPairId":"28",
                    "ticker":"PEPE-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"0.0000011553466039",
                    "priceChange24H":"0.000000031464105",
                    "volume24H":"1659009.682",
                    "trades24H":34279,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"180040000000",
                    "atomicResolution":1,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.0000000001",
                    "stepSize":"10000000",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "SEI-USD":{
                    "clobPairId":"29",
                    "ticker":"SEI-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"0.1210374394",
                    "priceChange24H":"0.00475325",
                    "volume24H":"1814004.367",
                    "trades24H":47288,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.2",
                    "maintenanceMarginFraction":"0.1",
                    "basePositionNotional":"100000",
                    "openInterest":"1271140",
                    "atomicResolution":-5,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.0001",
                    "stepSize":"10",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "SHIB-USD":{
                    "clobPairId":"30",
                    "ticker":"SHIB-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"0.000008388882015",
                    "priceChange24H":"0.00000023831144",
                    "volume24H":"1661105.758",
                    "trades24H":36336,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"9661000000",
                    "atomicResolution":0,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.000000001",
                    "stepSize":"1000000",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "SUI-USD":{
                    "clobPairId":"31",
                    "ticker":"SUI-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"0.5116001246",
                    "priceChange24H":"0.0191537295",
                    "volume24H":"1648115.204",
                    "trades24H":38645,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"191450",
                    "atomicResolution":-5,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.0001",
                    "stepSize":"10",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 },
                 "XRP-USD":{
                    "clobPairId":"32",
                    "ticker":"XRP-USD",
                    "status":"ACTIVE",
                    "lastPrice":"0",
                    "oraclePrice":"0.709905813",
                    "priceChange24H":"0.060935813",
                    "volume24H":"1665818.742",
                    "trades24H":37581,
                    "nextFundingRate":"0",
                    "initialMarginFraction":"0.1",
                    "maintenanceMarginFraction":"0.05",
                    "basePositionNotional":"250000",
                    "openInterest":"147200",
                    "atomicResolution":-5,
                    "quantumConversionExponent":-9,
                    "tickSize":"0.0001",
                    "stepSize":"10",
                    "stepBaseQuantums":1000000,
                    "subticksPerTick":1000000
                 }
              }
           }
        }
    """.trimIndent()

    private val subaccountMock = """
        {
           "type":"subscribed",
           "connection_id":"e488609d-f4fd-4abb-bfbb-eafcc51458a8",
           "message_id":2,
           "channel":"v4_subaccounts",
           "id":"dydx1lzr023hy3x7muerfwz3e7a80xvwd6cv7rxtz8h/0",
           "contents":{
              "subaccount":{
                 "address":"dydx1lzr023hy3x7muerfwz3e7a80xvwd6cv7rxtz8h",
                 "subaccountNumber":0,
                 "equity":"99.887770734",
                 "freeCollateral":"98.7685900473",
                 "openPerpetualPositions":{
                    "BTC-USD":{
                       "market":"BTC-USD",
                       "status":"OPEN",
                       "side":"LONG",
                       "size":"0.0001",
                       "maxSize":"0.0001",
                       "entryPrice":"34996",
                       "exitPrice":null,
                       "realizedPnl":"0",
                       "unrealizedPnl":"-0.011554936",
                       "createdAt":"2023-11-06T18:15:24.478Z",
                       "createdAtHeight":"1204080",
                       "closedAt":null,
                       "sumOpen":"0.0001",
                       "sumClose":"0",
                       "netFunding":"0"
                    },
                    "ETH-USD":{
                       "market":"ETH-USD",
                       "status":"OPEN",
                       "side":"LONG",
                       "size":"0.01",
                       "maxSize":"0.01",
                       "entryPrice":"1898.5",
                       "exitPrice":null,
                       "realizedPnl":"0",
                       "unrealizedPnl":"-0.08943133",
                       "createdAt":"2023-11-06T18:14:56.141Z",
                       "createdAtHeight":"1204059",
                       "closedAt":null,
                       "sumOpen":"0.01",
                       "sumClose":"0",
                       "netFunding":"0"
                    }
                 },
                 "assetPositions":{
                    "USDC":{
                       "size":"77.504157",
                       "symbol":"USDC",
                       "side":"LONG",
                       "assetId":"0"
                    }
                 },
                 "marginEnabled":true
              },
              "orders":[
                 
              ]
           }
        }
    """.trimIndent()

    override fun loadMarkets(): StateResponse {
        return test({
            perp.socket(mock.socketUrl, marketsMock, 0, null)
        }, null)
    }

    override fun loadSubaccounts(): StateResponse {
        return test({
            perp.socket(mock.socketUrl, subaccountMock, 0, null)
        }, null)
    }

    @Test
    fun test() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        if (perp.staticTyping) {
            perp.socket(mock.socketUrl, subaccountMock, 0, null)

            val subaccount = perp.internalState.wallet.account.subaccounts[0]!!
            val btcPosition = subaccount.positions?.get("BTC-USD")!!
            assertEquals(null, btcPosition.calculated[CalculationPeriod.post]?.liquidationPrice)
            val ethPosition = subaccount.positions?.get("ETH-USD")!!
            assertEquals(null, ethPosition.calculated[CalculationPeriod.post]?.liquidationPrice)
        } else {
            test(
                {
                    perp.socket(mock.socketUrl, subaccountMock, 0, null)
                },
                """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "positions": {
                                    "BTC-USD": {
                                        "liquidationPrice": {
                                            "postOrder": null
                                        }
                                    },
                                    "ETH-USD": {
                                        "liquidationPrice": {
                                            "postOrder": null
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.tradeInMarket("BTC-USD", 0)

            val subaccount = perp.internalState.wallet.account.subaccounts[0]!!
            val btcPosition = subaccount.positions?.get("BTC-USD")!!
            assertEquals(null, btcPosition.calculated[CalculationPeriod.post]?.liquidationPrice)
            val ethPosition = subaccount.positions?.get("ETH-USD")!!
            assertEquals(null, ethPosition.calculated[CalculationPeriod.post]?.liquidationPrice)
        } else {
            test(
                {
                    perp.tradeInMarket("BTC-USD", 0)
                },
                """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "positions": {
                                    "BTC-USD": {
                                        "liquidationPrice": {
                                            "postOrder": null
                                        }
                                    },
                                    "ETH-USD": {
                                        "liquidationPrice": {
                                            "postOrder": null
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.tradeInMarket("ETH-USD", 0)

            val subaccount = perp.internalState.wallet.account.subaccounts[0]!!
            val btcPosition = subaccount.positions?.get("BTC-USD")!!
            assertEquals(null, btcPosition.calculated[CalculationPeriod.post]?.liquidationPrice)
            val ethPosition = subaccount.positions?.get("ETH-USD")!!
            assertEquals(null, ethPosition.calculated[CalculationPeriod.post]?.liquidationPrice)
        } else {
            test(
                {
                    perp.tradeInMarket("ETH-USD", 0)
                },
                """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "positions": {
                                    "BTC-USD": {
                                        "liquidationPrice": {
                                            "postOrder": null
                                        }
                                    },
                                    "ETH-USD": {
                                        "liquidationPrice": {
                                            "postOrder": null
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }
    }
}
