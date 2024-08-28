package exchange.dydx.abacus.tests.payloads

import kollections.JsExport
import kotlinx.serialization.Serializable

@Suppress("PropertyName")
@JsExport
@Serializable
internal class SubaccountsChannelMock {
    internal val rest_response = """
        {
            "subaccounts": [
                {
                    "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
                    "subaccountNumber": 0,
                    "equity": "606645.478485819",
                    "freeCollateral": "466361.9423099619",
                    "openPerpetualPositions": {
                        "JUP-USD": {
                            "market": "JUP-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-8320",
                            "maxSize": "-250",
                            "entryPrice": "1.07471139319878674756",
                            "exitPrice": "1.0765026408632626248",
                            "realizedPnl": "-1213.4617783458935186932",
                            "unrealizedPnl": "-454.3648520420942603008",
                            "createdAt": "2024-05-03T06:41:44.214Z",
                            "createdAtHeight": "14694808",
                            "closedAt": null,
                            "sumOpen": "685760",
                            "sumClose": "677430",
                            "netFunding": "-0.016873"
                        },
                        "JTO-USD": {
                            "market": "JTO-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-5666",
                            "maxSize": "-98",
                            "entryPrice": "3.35265453649139080142",
                            "exitPrice": "3.35381858361271410959",
                            "realizedPnl": "-1786.05868267182564988829",
                            "unrealizedPnl": "-1327.28392045977971915428",
                            "createdAt": "2024-04-19T19:20:23.643Z",
                            "createdAtHeight": "13645009",
                            "closedAt": null,
                            "sumOpen": "1541665",
                            "sumClose": "1535837",
                            "netFunding": "1.727956"
                        },
                        "UNI-USD": {
                            "market": "UNI-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-1183",
                            "maxSize": "-9",
                            "entryPrice": "7.6503614854026307347",
                            "exitPrice": "7.6519465918824082005",
                            "realizedPnl": "-57.3842328225858196994",
                            "unrealizedPnl": "90.8953583023121591501",
                            "createdAt": "2024-05-05T21:07:02.430Z",
                            "createdAtHeight": "14900409",
                            "closedAt": null,
                            "sumOpen": "37404",
                            "sumClose": "36193",
                            "netFunding": "-0.014474"
                        },
                        "APT-USD": {
                            "market": "APT-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-3009",
                            "maxSize": "-42",
                            "entryPrice": "9.13007547870782910168",
                            "exitPrice": "9.13271295435576726603",
                            "realizedPnl": "-7661.7006779105664398163",
                            "unrealizedPnl": "8.95321543185776695512",
                            "createdAt": "2024-04-16T22:52:23.946Z",
                            "createdAtHeight": "13422767",
                            "closedAt": null,
                            "sumOpen": "2909708",
                            "sumClose": "2906698",
                            "netFunding": "4.644513"
                        },
                        "SUI-USD": {
                            "market": "SUI-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-19530",
                            "maxSize": "-320",
                            "entryPrice": "1.09620804375555641442",
                            "exitPrice": "1.09627834122066643908",
                            "realizedPnl": "-144.608895236017545602",
                            "unrealizedPnl": "-17.4199054539832263774",
                            "createdAt": "2024-05-04T15:32:45.882Z",
                            "createdAtHeight": "14802798",
                            "closedAt": null,
                            "sumOpen": "2069680",
                            "sumClose": "2049700",
                            "netFunding": "-0.520181"
                        },
                        "LINK-USD": {
                            "market": "LINK-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-585",
                            "maxSize": "-9",
                            "entryPrice": "14.58359936180968027309",
                            "exitPrice": "14.58661656833040982299",
                            "realizedPnl": "-188.425756914950074229",
                            "unrealizedPnl": "-91.99313849133704024235",
                            "createdAt": "2024-05-05T12:01:59.533Z",
                            "createdAtHeight": "14870060",
                            "closedAt": null,
                            "sumOpen": "63304",
                            "sumClose": "62710",
                            "netFunding": "0.783264"
                        },
                        "XRP-USD": {
                            "market": "XRP-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-16430",
                            "maxSize": "-1070",
                            "entryPrice": "0.5336476368557018083",
                            "exitPrice": "0.53360895029051000641",
                            "realizedPnl": "15.9180123705252819025",
                            "unrealizedPnl": "-534.913023930819289631",
                            "createdAt": "2024-05-03T07:44:45.561Z",
                            "createdAtHeight": "14698571",
                            "closedAt": null,
                            "sumOpen": "406450",
                            "sumClose": "387250",
                            "netFunding": "0.93664"
                        },
                        "SEI-USD": {
                            "market": "SEI-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-29670",
                            "maxSize": "-110",
                            "entryPrice": "0.56444890162085139224",
                            "exitPrice": "0.56317442201715198753",
                            "realizedPnl": "390.2276650664964424157",
                            "unrealizedPnl": "473.2144083366608077608",
                            "createdAt": "2024-05-05T17:09:43.285Z",
                            "createdAtHeight": "14886563",
                            "closedAt": null,
                            "sumOpen": "336860",
                            "sumClose": "306670",
                            "netFunding": "-0.616995"
                        },
                        "PYTH-USD": {
                            "market": "PYTH-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-48440",
                            "maxSize": "-1580",
                            "entryPrice": "0.57385669937627342322",
                            "exitPrice": "0.57463320981914971168",
                            "realizedPnl": "-1719.2117775060069843168",
                            "unrealizedPnl": "1411.4677036306846207768",
                            "createdAt": "2024-04-18T19:39:24.130Z",
                            "createdAtHeight": "13569699",
                            "closedAt": null,
                            "sumOpen": "2242970",
                            "sumClose": "2194080",
                            "netFunding": "-15.485745"
                        },
                        "SHIB-USD": {
                            "market": "SHIB-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-953000000",
                            "maxSize": "-3000000",
                            "entryPrice": "0.000024345352041991944475",
                            "exitPrice": "0.000024347591832578033499",
                            "realizedPnl": "-483.825595522225765696",
                            "unrealizedPnl": "76.029028664323084675",
                            "createdAt": "2024-04-19T13:30:27.538Z",
                            "createdAtHeight": "13627190",
                            "closedAt": null,
                            "sumOpen": "216994000000",
                            "sumClose": "216029000000",
                            "netFunding": "0.034125"
                        },
                        "DOGE-USD": {
                            "market": "DOGE-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-115800",
                            "maxSize": "-3300",
                            "entryPrice": "0.14347782857829780416",
                            "exitPrice": "0.14353945388953950118",
                            "realizedPnl": "-908.923714366885983756",
                            "unrealizedPnl": "-1571.042267765114278272",
                            "createdAt": "2024-04-29T13:48:29.999Z",
                            "createdAtHeight": "14405058",
                            "closedAt": null,
                            "sumOpen": "14973600",
                            "sumClose": "14857800",
                            "netFunding": "6.692835"
                        },
                        "FET-USD": {
                            "market": "FET-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-16170",
                            "maxSize": "-150",
                            "entryPrice": "2.15901890685022984748",
                            "exitPrice": "2.15884669580816798514",
                            "realizedPnl": "523.3011341632815240622",
                            "unrealizedPnl": "-3247.5486788707833662484",
                            "createdAt": "2024-04-15T14:55:12.719Z",
                            "createdAtHeight": "13316471",
                            "closedAt": null,
                            "sumOpen": "2965010",
                            "sumClose": "2948830",
                            "netFunding": "15.480047"
                        },
                        "WIF-USD": {
                            "market": "WIF-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-24142",
                            "maxSize": "-311",
                            "entryPrice": "3.14386078572998775239",
                            "exitPrice": "3.37019231784582893347",
                            "realizedPnl": "-980.98012230961278786208",
                            "unrealizedPnl": "-6206.26436352063568180062",
                            "createdAt": "2024-05-03T00:07:03.134Z",
                            "createdAtHeight": "14671623",
                            "closedAt": null,
                            "sumOpen": "31843",
                            "sumClose": "7576",
                            "netFunding": "733.707565"
                        },
                        "BNB-USD": {
                            "market": "BNB-USD",
                            "status": "OPEN",
                            "side": "LONG",
                            "size": "2.26",
                            "maxSize": "15.73",
                            "entryPrice": "586.26934472934472934473",
                            "exitPrice": "587.82638151732750546363",
                            "realizedPnl": "50.165868319088319088367",
                            "unrealizedPnl": "4.8053323916809116809102",
                            "createdAt": "2024-05-06T14:59:59.225Z",
                            "createdAtHeight": "14961006",
                            "closedAt": null,
                            "sumOpen": "35.1",
                            "sumClose": "32.03",
                            "netFunding": "0.29398"
                        },
                        "TIA-USD": {
                            "market": "TIA-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-2151.5",
                            "maxSize": "-19.9",
                            "entryPrice": "10.38894159748067563702",
                            "exitPrice": "10.34821162896614543798",
                            "realizedPnl": "53.657660521042084215296",
                            "unrealizedPnl": "-102.32201059532636695147",
                            "createdAt": "2024-05-06T11:51:34.343Z",
                            "createdAtHeight": "14951109",
                            "closedAt": null,
                            "sumOpen": "3493",
                            "sumClose": "1317.4",
                            "netFunding": "0"
                        },
                        "ETH-USD": {
                            "market": "ETH-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-46.58",
                            "maxSize": "-1.063",
                            "entryPrice": "3066.40224604193291028414",
                            "exitPrice": "3068.53621352138954699323",
                            "realizedPnl": "-8115.39082929605005860740222",
                            "unrealizedPnl": "-866.9876223867650389647588",
                            "createdAt": "2024-05-02T14:09:12.293Z",
                            "createdAtHeight": "14638847",
                            "closedAt": null,
                            "sumOpen": "3944.539",
                            "sumClose": "3897.558",
                            "netFunding": "201.871192"
                        },
                        "FIL-USD": {
                            "market": "FIL-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-3850",
                            "maxSize": "-24",
                            "entryPrice": "6.01317331471531494518",
                            "exitPrice": "6.01492063517976081637",
                            "realizedPnl": "-1424.16015318134799860496",
                            "unrealizedPnl": "-470.837691496037461057",
                            "createdAt": "2024-04-13T18:24:09.183Z",
                            "createdAtHeight": "13174496",
                            "closedAt": null,
                            "sumOpen": "861531",
                            "sumClose": "857584",
                            "netFunding": "74.31392"
                        },
                        "NEAR-USD": {
                            "market": "NEAR-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-2038",
                            "maxSize": "-104",
                            "entryPrice": "7.40113763357295511947",
                            "exitPrice": "7.41124409040089442583",
                            "realizedPnl": "-750.11482439673596543952",
                            "unrealizedPnl": "269.54582614168253347986",
                            "createdAt": "2024-05-05T14:49:50.040Z",
                            "createdAtHeight": "14879002",
                            "closedAt": null,
                            "sumOpen": "77205",
                            "sumClose": "75132",
                            "netFunding": "9.20349"
                        },
                        "CRV-USD": {
                            "market": "CRV-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-9330",
                            "maxSize": "-260",
                            "entryPrice": "0.44042170342205323195",
                            "exitPrice": "0.44071769094138543517",
                            "realizedPnl": "-16.664652338403041286",
                            "unrealizedPnl": "-34.0444261932433459065",
                            "createdAt": "2024-05-06T14:49:34.184Z",
                            "createdAtHeight": "14960492",
                            "closedAt": null,
                            "sumOpen": "65750",
                            "sumClose": "56300",
                            "netFunding": "-0.000555"
                        },
                        "BLUR-USD": {
                            "market": "BLUR-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-60050",
                            "maxSize": "-4410",
                            "entryPrice": "0.40294588815227420298",
                            "exitPrice": "0.40271388183145034869",
                            "realizedPnl": "255.8326087352288306426",
                            "unrealizedPnl": "104.582848944065888949",
                            "createdAt": "2024-05-04T21:51:14.484Z",
                            "createdAtHeight": "14823651",
                            "closedAt": null,
                            "sumOpen": "1170520",
                            "sumClose": "1109940",
                            "netFunding": "-1.680487"
                        },
                        "TRX-USD": {
                            "market": "TRX-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-108800",
                            "maxSize": "-5700",
                            "entryPrice": "0.11853304615956959703",
                            "exitPrice": "0.11877997512654913601",
                            "realizedPnl": "-1689.950767190934579704",
                            "unrealizedPnl": "-59.508577838827843136",
                            "createdAt": "2024-04-19T11:19:29.636Z",
                            "createdAtHeight": "13620483",
                            "closedAt": null,
                            "sumOpen": "6988800",
                            "sumClose": "6874800",
                            "netFunding": "7.636495"
                        },
                        "WLD-USD": {
                            "market": "WLD-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-2638",
                            "maxSize": "-43",
                            "entryPrice": "4.85215216209945291122",
                            "exitPrice": "4.85221571692852862257",
                            "realizedPnl": "-41.41061734664434032805",
                            "unrealizedPnl": "-3082.89299638164322020164",
                            "createdAt": "2024-04-26T17:35:44.024Z",
                            "createdAtHeight": "14178340",
                            "closedAt": null,
                            "sumOpen": "655104",
                            "sumClose": "652443",
                            "netFunding": "0.055286"
                        },
                        "STRK-USD": {
                            "market": "STRK-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-12557",
                            "maxSize": "-9",
                            "entryPrice": "1.37019258314655949711",
                            "exitPrice": "1.36758418049553221269",
                            "realizedPnl": "169.55891377244506027282",
                            "unrealizedPnl": "637.90840998034760521027",
                            "createdAt": "2024-05-06T08:10:25.256Z",
                            "createdAtHeight": "14939189",
                            "closedAt": null,
                            "sumOpen": "77634",
                            "sumClose": "65021",
                            "netFunding": "-0.042035"
                        },
                        "MATIC-USD": {
                            "market": "MATIC-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-11780",
                            "maxSize": "-70",
                            "entryPrice": "0.71692774166740866052",
                            "exitPrice": "0.71587389215656083514",
                            "realizedPnl": "349.8554576576143356474",
                            "unrealizedPnl": "-36.0755730339259790744",
                            "createdAt": "2024-04-14T08:56:06.990Z",
                            "createdAtHeight": "13220984",
                            "closedAt": null,
                            "sumOpen": "336930",
                            "sumClose": "324730",
                            "netFunding": "7.638906"
                        },
                        "BTC-USD": {
                            "market": "BTC-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-2.3708",
                            "maxSize": "-0.0616",
                            "entryPrice": "63547.84675746875175158345",
                            "exitPrice": "63598.67527857597940423097",
                            "realizedPnl": "-769.302394967154307493623136",
                            "unrealizedPnl": "253.29417969491665265404326",
                            "createdAt": "2024-05-06T12:29:48.437Z",
                            "createdAtHeight": "14953243",
                            "closedAt": null,
                            "sumOpen": "17.841",
                            "sumClose": "15.3818",
                            "netFunding": "12.531751"
                        },
                        "ARB-USD": {
                            "market": "ARB-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-12424",
                            "maxSize": "-701",
                            "entryPrice": "1.04436521258325918279",
                            "exitPrice": "1.0447271660829645913",
                            "realizedPnl": "-1078.70515690080261635452",
                            "unrealizedPnl": "-310.45217200158791301704",
                            "createdAt": "2024-04-13T21:56:35.508Z",
                            "createdAtHeight": "13185581",
                            "closedAt": null,
                            "sumOpen": "3004023",
                            "sumClose": "2991252",
                            "netFunding": "3.988973"
                        },
                        "OP-USD": {
                            "market": "OP-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-5260",
                            "maxSize": "-406",
                            "entryPrice": "2.92684050214474046673",
                            "exitPrice": "2.92767676925480393689",
                            "realizedPnl": "-16.02347631454881748248",
                            "unrealizedPnl": "612.8191306413348549998",
                            "createdAt": "2024-05-06T01:13:18.000Z",
                            "createdAtHeight": "14914875",
                            "closedAt": null,
                            "sumOpen": "25411",
                            "sumClose": "19203",
                            "netFunding": "0.035361"
                        },
                        "INJ-USD": {
                            "market": "INJ-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-921.6",
                            "maxSize": "-6.3",
                            "entryPrice": "25.85560772962955250609",
                            "exitPrice": "25.84817307376285892704",
                            "realizedPnl": "136.29717457118464042099",
                            "unrealizedPnl": "559.827569322595589612544",
                            "createdAt": "2024-04-20T09:40:05.381Z",
                            "createdAtHeight": "13692204",
                            "closedAt": null,
                            "sumOpen": "19209.2",
                            "sumClose": "18255.8",
                            "netFunding": "0.571584"
                        },
                        "BONK-USD": {
                            "market": "BONK-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-1257800000",
                            "maxSize": "-61400000",
                            "entryPrice": "0.000026130333455998317735",
                            "exitPrice": "0.000026177261979228302711",
                            "realizedPnl": "-813.7132102730523946128",
                            "unrealizedPnl": "1186.776380954684047083",
                            "createdAt": "2024-05-02T12:56:55.063Z",
                            "createdAtHeight": "14635404",
                            "closedAt": null,
                            "sumOpen": "19022000000",
                            "sumClose": "17745300000",
                            "netFunding": "19.047513"
                        },
                        "SOL-USD": {
                            "market": "SOL-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-315.6",
                            "maxSize": "-5.2",
                            "entryPrice": "152.96769190187285676602",
                            "exitPrice": "152.12810584958217270196",
                            "realizedPnl": "33.450379277235557899754",
                            "unrealizedPnl": "21.524072079073595355912",
                            "createdAt": "2024-05-06T14:59:48.785Z",
                            "createdAtHeight": "14960997",
                            "closedAt": null,
                            "sumOpen": "379.1",
                            "sumClose": "35.9",
                            "netFunding": "3.30924"
                        },
                        "AVAX-USD": {
                            "market": "AVAX-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-255.8",
                            "maxSize": "-0.8",
                            "entryPrice": "36.84559619381861101541",
                            "exitPrice": "36.87267572809821314119",
                            "realizedPnl": "-1287.996753089430638004814",
                            "unrealizedPnl": "-172.398744477199302258122",
                            "createdAt": "2024-05-04T03:04:11.414Z",
                            "createdAtHeight": "14762066",
                            "closedAt": null,
                            "sumOpen": "47869.5",
                            "sumClose": "47586.3",
                            "netFunding": "0.618089"
                        },
                        "DOT-USD": {
                            "market": "DOT-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-614",
                            "maxSize": "-112",
                            "entryPrice": "7.04377611585944919278",
                            "exitPrice": "7.04957437200112898673",
                            "realizedPnl": "-20.85399150997150996485",
                            "unrealizedPnl": "-85.44118666429819563308",
                            "createdAt": "2024-05-01T22:18:56.761Z",
                            "createdAtHeight": "14586271",
                            "closedAt": null,
                            "sumOpen": "4212",
                            "sumClose": "3543",
                            "netFunding": "-0.31077"
                        },
                        "DYM-USD": {
                            "market": "DYM-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-12498",
                            "maxSize": "-167",
                            "entryPrice": "3.31846163630552892303",
                            "exitPrice": "3.30022944460279610408",
                            "realizedPnl": "2614.77401109619687622485",
                            "unrealizedPnl": "675.11486815450048002894",
                            "createdAt": "2024-04-27T05:31:36.768Z",
                            "createdAtHeight": "14217769",
                            "closedAt": null,
                            "sumOpen": "156450",
                            "sumClose": "143843",
                            "netFunding": "-7.79914"
                        },
                        "AEVO-USD": {
                            "market": "AEVO-USD",
                            "status": "OPEN",
                            "side": "LONG",
                            "size": "35117",
                            "maxSize": "35402",
                            "entryPrice": "1.53038537249283667626",
                            "exitPrice": "1.60134942388268156425",
                            "realizedPnl": "3231.82923116618911044032",
                            "unrealizedPnl": "-5288.65572059394556022242",
                            "createdAt": "2024-04-14T11:10:58.089Z",
                            "createdAtHeight": "13228028",
                            "closedAt": null,
                            "sumOpen": "69800",
                            "sumClose": "34368",
                            "netFunding": "792.936713"
                        },
                        "SNX-USD": {
                            "market": "SNX-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-18829",
                            "maxSize": "-65",
                            "entryPrice": "2.77864627311186033022",
                            "exitPrice": "2.81773479108032041565",
                            "realizedPnl": "-931.74234597790280760702",
                            "unrealizedPnl": "-533.87232357678184228762",
                            "createdAt": "2024-04-13T18:46:42.554Z",
                            "createdAtHeight": "13175734",
                            "closedAt": null,
                            "sumOpen": "46567",
                            "sumClose": "27714",
                            "netFunding": "151.556841"
                        },
                        "ARKM-USD": {
                            "market": "ARKM-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-8580",
                            "maxSize": "-7",
                            "entryPrice": "2.01150784020880156575",
                            "exitPrice": "2.0099710061153772741",
                            "realizedPnl": "1050.7998550084823140655",
                            "unrealizedPnl": "-4868.972718248482565865",
                            "createdAt": "2024-04-05T12:53:17.372Z",
                            "createdAtHeight": "12565923",
                            "closedAt": null,
                            "sumOpen": "689650",
                            "sumClose": "681070",
                            "netFunding": "4.108259"
                        },
                        "ORDI-USD": {
                            "market": "ORDI-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-690.8",
                            "maxSize": "-0.2",
                            "entryPrice": "39.87346429654398085537",
                            "exitPrice": "39.72794352951099677375",
                            "realizedPnl": "3450.52352550260205133263",
                            "unrealizedPnl": "728.296144960581974889596",
                            "createdAt": "2024-04-22T15:44:53.560Z",
                            "createdAtHeight": "13865438",
                            "closedAt": null,
                            "sumOpen": "24403.8",
                            "sumClose": "23711.5",
                            "netFunding": "0.007858"
                        },
                        "APE-USD": {
                            "market": "APE-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-7991",
                            "maxSize": "-422",
                            "entryPrice": "1.20428624702426176369",
                            "exitPrice": "1.20286772599813059148",
                            "realizedPnl": "162.9419632939269736138",
                            "unrealizedPnl": "-555.95522974912424635321",
                            "createdAt": "2024-04-13T09:47:30.577Z",
                            "createdAtHeight": "13145992",
                            "closedAt": null,
                            "sumOpen": "157944",
                            "sumClose": "149780",
                            "netFunding": "-49.524116"
                        },
                        "HBAR-USD": {
                            "market": "HBAR-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-43110",
                            "maxSize": "-4900",
                            "entryPrice": "0.08902716881129452161",
                            "exitPrice": "0.09048558903469035857",
                            "realizedPnl": "-1500.4527125180605978096",
                            "unrealizedPnl": "-1061.2285964000931733929",
                            "createdAt": "2024-04-13T21:43:00.710Z",
                            "createdAtHeight": "13184857",
                            "closedAt": null,
                            "sumOpen": "1071670",
                            "sumClose": "1026510",
                            "netFunding": "-3.369769"
                        },
                        "LDO-USD": {
                            "market": "LDO-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-5937",
                            "maxSize": "-141",
                            "entryPrice": "1.99496195409791073449",
                            "exitPrice": "2.00343290628554260821",
                            "realizedPnl": "-1183.42352597437305043484",
                            "unrealizedPnl": "149.51338329529603066713",
                            "createdAt": "2024-04-13T21:52:33.039Z",
                            "createdAtHeight": "13185365",
                            "closedAt": null,
                            "sumOpen": "145745",
                            "sumClose": "139797",
                            "netFunding": "0.790177"
                        },
                        "IMX-USD": {
                            "market": "IMX-USD",
                            "status": "OPEN",
                            "side": "SHORT",
                            "size": "-3315",
                            "maxSize": "-177",
                            "entryPrice": "2.17275830123868012908",
                            "exitPrice": "2.16886475198167650957",
                            "realizedPnl": "171.84949706214218763883",
                            "unrealizedPnl": "-69.7619863087753720998",
                            "createdAt": "2024-04-21T17:11:37.944Z",
                            "createdAtHeight": "13793817",
                            "closedAt": null,
                            "sumOpen": "48035",
                            "sumClose": "44533",
                            "netFunding": "-1.541932"
                        },
                        "CHZ-USD": {
                            "market": "CHZ-USD",
                            "status": "OPEN",
                            "side": "LONG",
                            "size": "370",
                            "maxSize": "370",
                            "entryPrice": "0.12421474201474201474",
                            "exitPrice": null,
                            "realizedPnl": "0",
                            "unrealizedPnl": "-0.0016843564545454538",
                            "createdAt": "2024-05-06T10:47:14.349Z",
                            "createdAtHeight": "14947659",
                            "closedAt": null,
                            "sumOpen": "4070",
                            "sumClose": "0",
                            "netFunding": "0"
                        }
                    },
                    "assetPositions": {
                        "USDC": {
                            "size": "1625586.093553",
                            "symbol": "USDC",
                            "side": "LONG",
                            "assetId": "0"
                        }
                    },
                    "marginEnabled": true
                }
            ],
            "totalTradingRewards": "36059.407411800692695451"
        }
    """.trimIndent()
    internal val subscribed = """
        {
          "type": "subscribed",
          "connection_id": "9795f210-fa8f-4dd2-9f5f-e5ea805e09ac",
          "message_id": 2,
          "channel": "v4_subaccounts",
          "id": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3/0",
          "contents": {
            "subaccount": {
              "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
              "subaccountNumber": 0,
              "equity": "606672.160584014",
              "freeCollateral": "466391.2926179764",
              "openPerpetualPositions": {
                "JUP-USD": {
                  "market": "JUP-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-8320",
                  "maxSize": "-250",
                  "entryPrice": "1.07471139319878674756",
                  "exitPrice": "1.0765026408632626248",
                  "realizedPnl": "-1213.4617783458935186932",
                  "unrealizedPnl": "-454.3648520420942603008",
                  "createdAt": "2024-05-03T06:41:44.214Z",
                  "createdAtHeight": "14694808",
                  "closedAt": null,
                  "sumOpen": "685760",
                  "sumClose": "677430",
                  "netFunding": "-0.016873"
                },
                "JTO-USD": {
                  "market": "JTO-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-5666",
                  "maxSize": "-98",
                  "entryPrice": "3.35265453649139080142",
                  "exitPrice": "3.35381858361271410959",
                  "realizedPnl": "-1786.05868267182564988829",
                  "unrealizedPnl": "-1327.28392045977971915428",
                  "createdAt": "2024-04-19T19:20:23.643Z",
                  "createdAtHeight": "13645009",
                  "closedAt": null,
                  "sumOpen": "1541665",
                  "sumClose": "1535837",
                  "netFunding": "1.727956"
                },
                "UNI-USD": {
                  "market": "UNI-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-1183",
                  "maxSize": "-9",
                  "entryPrice": "7.6503614854026307347",
                  "exitPrice": "7.6519465918824082005",
                  "realizedPnl": "-57.3842328225858196994",
                  "unrealizedPnl": "90.8953583023121591501",
                  "createdAt": "2024-05-05T21:07:02.430Z",
                  "createdAtHeight": "14900409",
                  "closedAt": null,
                  "sumOpen": "37404",
                  "sumClose": "36193",
                  "netFunding": "-0.014474"
                },
                "APT-USD": {
                  "market": "APT-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-3009",
                  "maxSize": "-42",
                  "entryPrice": "9.13007547870782910168",
                  "exitPrice": "9.13271295435576726603",
                  "realizedPnl": "-7661.7006779105664398163",
                  "unrealizedPnl": "8.95321543185776695512",
                  "createdAt": "2024-04-16T22:52:23.946Z",
                  "createdAtHeight": "13422767",
                  "closedAt": null,
                  "sumOpen": "2909708",
                  "sumClose": "2906698",
                  "netFunding": "4.644513"
                },
                "SUI-USD": {
                  "market": "SUI-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-19530",
                  "maxSize": "-320",
                  "entryPrice": "1.09620804375555641442",
                  "exitPrice": "1.09627834122066643908",
                  "realizedPnl": "-144.608895236017545602",
                  "unrealizedPnl": "-17.4199054539832263774",
                  "createdAt": "2024-05-04T15:32:45.882Z",
                  "createdAtHeight": "14802798",
                  "closedAt": null,
                  "sumOpen": "2069680",
                  "sumClose": "2049700",
                  "netFunding": "-0.520181"
                },
                "LINK-USD": {
                  "market": "LINK-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-585",
                  "maxSize": "-9",
                  "entryPrice": "14.58359936180968027309",
                  "exitPrice": "14.58661656833040982299",
                  "realizedPnl": "-188.425756914950074229",
                  "unrealizedPnl": "-91.99313849133704024235",
                  "createdAt": "2024-05-05T12:01:59.533Z",
                  "createdAtHeight": "14870060",
                  "closedAt": null,
                  "sumOpen": "63304",
                  "sumClose": "62710",
                  "netFunding": "0.783264"
                },
                "XRP-USD": {
                  "market": "XRP-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-16430",
                  "maxSize": "-1070",
                  "entryPrice": "0.5336476368557018083",
                  "exitPrice": "0.53360895029051000641",
                  "realizedPnl": "15.9180123705252819025",
                  "unrealizedPnl": "-508.230925735819289631",
                  "createdAt": "2024-05-03T07:44:45.561Z",
                  "createdAtHeight": "14698571",
                  "closedAt": null,
                  "sumOpen": "406450",
                  "sumClose": "387250",
                  "netFunding": "0.93664"
                },
                "SEI-USD": {
                  "market": "SEI-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-29670",
                  "maxSize": "-110",
                  "entryPrice": "0.56444890162085139224",
                  "exitPrice": "0.56317442201715198753",
                  "realizedPnl": "390.2276650664964424157",
                  "unrealizedPnl": "473.2144083366608077608",
                  "createdAt": "2024-05-05T17:09:43.285Z",
                  "createdAtHeight": "14886563",
                  "closedAt": null,
                  "sumOpen": "336860",
                  "sumClose": "306670",
                  "netFunding": "-0.616995"
                },
                "PYTH-USD": {
                  "market": "PYTH-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-48440",
                  "maxSize": "-1580",
                  "entryPrice": "0.57385669937627342322",
                  "exitPrice": "0.57463320981914971168",
                  "realizedPnl": "-1719.2117775060069843168",
                  "unrealizedPnl": "1411.4677036306846207768",
                  "createdAt": "2024-04-18T19:39:24.130Z",
                  "createdAtHeight": "13569699",
                  "closedAt": null,
                  "sumOpen": "2242970",
                  "sumClose": "2194080",
                  "netFunding": "-15.485745"
                },
                "SHIB-USD": {
                  "market": "SHIB-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-953000000",
                  "maxSize": "-3000000",
                  "entryPrice": "0.000024345352041991944475",
                  "exitPrice": "0.000024347591832578033499",
                  "realizedPnl": "-483.825595522225765696",
                  "unrealizedPnl": "76.029028664323084675",
                  "createdAt": "2024-04-19T13:30:27.538Z",
                  "createdAtHeight": "13627190",
                  "closedAt": null,
                  "sumOpen": "216994000000",
                  "sumClose": "216029000000",
                  "netFunding": "0.034125"
                },
                "DOGE-USD": {
                  "market": "DOGE-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-115800",
                  "maxSize": "-3300",
                  "entryPrice": "0.14347782857829780416",
                  "exitPrice": "0.14353945388953950118",
                  "realizedPnl": "-908.923714366885983756",
                  "unrealizedPnl": "-1571.042267765114278272",
                  "createdAt": "2024-04-29T13:48:29.999Z",
                  "createdAtHeight": "14405058",
                  "closedAt": null,
                  "sumOpen": "14973600",
                  "sumClose": "14857800",
                  "netFunding": "6.692835"
                },
                "FET-USD": {
                  "market": "FET-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-16170",
                  "maxSize": "-150",
                  "entryPrice": "2.15901890685022984748",
                  "exitPrice": "2.15884669580816798514",
                  "realizedPnl": "523.3011341632815240622",
                  "unrealizedPnl": "-3247.5486788707833662484",
                  "createdAt": "2024-04-15T14:55:12.719Z",
                  "createdAtHeight": "13316471",
                  "closedAt": null,
                  "sumOpen": "2965010",
                  "sumClose": "2948830",
                  "netFunding": "15.480047"
                },
                "WIF-USD": {
                  "market": "WIF-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-24142",
                  "maxSize": "-311",
                  "entryPrice": "3.14386078572998775239",
                  "exitPrice": "3.37019231784582893347",
                  "realizedPnl": "-980.98012230961278786208",
                  "unrealizedPnl": "-6206.26436352063568180062",
                  "createdAt": "2024-05-03T00:07:03.134Z",
                  "createdAtHeight": "14671623",
                  "closedAt": null,
                  "sumOpen": "31843",
                  "sumClose": "7576",
                  "netFunding": "733.707565"
                },
                "BNB-USD": {
                  "market": "BNB-USD",
                  "status": "OPEN",
                  "side": "LONG",
                  "size": "2.26",
                  "maxSize": "15.73",
                  "entryPrice": "586.26934472934472934473",
                  "exitPrice": "587.82638151732750546363",
                  "realizedPnl": "50.165868319088319088367",
                  "unrealizedPnl": "4.8053323916809116809102",
                  "createdAt": "2024-05-06T14:59:59.225Z",
                  "createdAtHeight": "14961006",
                  "closedAt": null,
                  "sumOpen": "35.1",
                  "sumClose": "32.03",
                  "netFunding": "0.29398"
                },
                "TIA-USD": {
                  "market": "TIA-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-2151.5",
                  "maxSize": "-19.9",
                  "entryPrice": "10.38894159748067563702",
                  "exitPrice": "10.34821162896614543798",
                  "realizedPnl": "53.657660521042084215296",
                  "unrealizedPnl": "-102.32201059532636695147",
                  "createdAt": "2024-05-06T11:51:34.343Z",
                  "createdAtHeight": "14951109",
                  "closedAt": null,
                  "sumOpen": "3493",
                  "sumClose": "1317.4",
                  "netFunding": "0"
                },
                "ETH-USD": {
                  "market": "ETH-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-46.58",
                  "maxSize": "-1.063",
                  "entryPrice": "3066.40224604193291028414",
                  "exitPrice": "3068.53621352138954699323",
                  "realizedPnl": "-8115.39082929605005860740222",
                  "unrealizedPnl": "-866.9876223867650389647588",
                  "createdAt": "2024-05-02T14:09:12.293Z",
                  "createdAtHeight": "14638847",
                  "closedAt": null,
                  "sumOpen": "3944.539",
                  "sumClose": "3897.558",
                  "netFunding": "201.871192"
                },
                "FIL-USD": {
                  "market": "FIL-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-3850",
                  "maxSize": "-24",
                  "entryPrice": "6.01317331471531494518",
                  "exitPrice": "6.01492063517976081637",
                  "realizedPnl": "-1424.16015318134799860496",
                  "unrealizedPnl": "-470.837691496037461057",
                  "createdAt": "2024-04-13T18:24:09.183Z",
                  "createdAtHeight": "13174496",
                  "closedAt": null,
                  "sumOpen": "861531",
                  "sumClose": "857584",
                  "netFunding": "74.31392"
                },
                "NEAR-USD": {
                  "market": "NEAR-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-2038",
                  "maxSize": "-104",
                  "entryPrice": "7.40113763357295511947",
                  "exitPrice": "7.41124409040089442583",
                  "realizedPnl": "-750.11482439673596543952",
                  "unrealizedPnl": "269.54582614168253347986",
                  "createdAt": "2024-05-05T14:49:50.040Z",
                  "createdAtHeight": "14879002",
                  "closedAt": null,
                  "sumOpen": "77205",
                  "sumClose": "75132",
                  "netFunding": "9.20349"
                },
                "CRV-USD": {
                  "market": "CRV-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-9330",
                  "maxSize": "-260",
                  "entryPrice": "0.44042170342205323195",
                  "exitPrice": "0.44071769094138543517",
                  "realizedPnl": "-16.664652338403041286",
                  "unrealizedPnl": "-34.0444261932433459065",
                  "createdAt": "2024-05-06T14:49:34.184Z",
                  "createdAtHeight": "14960492",
                  "closedAt": null,
                  "sumOpen": "65750",
                  "sumClose": "56300",
                  "netFunding": "-0.000555"
                },
                "BLUR-USD": {
                  "market": "BLUR-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-60050",
                  "maxSize": "-4410",
                  "entryPrice": "0.40294588815227420298",
                  "exitPrice": "0.40271388183145034869",
                  "realizedPnl": "255.8326087352288306426",
                  "unrealizedPnl": "104.582848944065888949",
                  "createdAt": "2024-05-04T21:51:14.484Z",
                  "createdAtHeight": "14823651",
                  "closedAt": null,
                  "sumOpen": "1170520",
                  "sumClose": "1109940",
                  "netFunding": "-1.680487"
                },
                "TRX-USD": {
                  "market": "TRX-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-108800",
                  "maxSize": "-5700",
                  "entryPrice": "0.11853304615956959703",
                  "exitPrice": "0.11877997512654913601",
                  "realizedPnl": "-1689.950767190934579704",
                  "unrealizedPnl": "-59.508577838827843136",
                  "createdAt": "2024-04-19T11:19:29.636Z",
                  "createdAtHeight": "13620483",
                  "closedAt": null,
                  "sumOpen": "6988800",
                  "sumClose": "6874800",
                  "netFunding": "7.636495"
                },
                "WLD-USD": {
                  "market": "WLD-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-2638",
                  "maxSize": "-43",
                  "entryPrice": "4.85215216209945291122",
                  "exitPrice": "4.85221571692852862257",
                  "realizedPnl": "-41.41061734664434032805",
                  "unrealizedPnl": "-3082.89299638164322020164",
                  "createdAt": "2024-04-26T17:35:44.024Z",
                  "createdAtHeight": "14178340",
                  "closedAt": null,
                  "sumOpen": "655104",
                  "sumClose": "652443",
                  "netFunding": "0.055286"
                },
                "STRK-USD": {
                  "market": "STRK-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-12557",
                  "maxSize": "-9",
                  "entryPrice": "1.37019258314655949711",
                  "exitPrice": "1.36758418049553221269",
                  "realizedPnl": "169.55891377244506027282",
                  "unrealizedPnl": "637.90840998034760521027",
                  "createdAt": "2024-05-06T08:10:25.256Z",
                  "createdAtHeight": "14939189",
                  "closedAt": null,
                  "sumOpen": "77634",
                  "sumClose": "65021",
                  "netFunding": "-0.042035"
                },
                "MATIC-USD": {
                  "market": "MATIC-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-11780",
                  "maxSize": "-70",
                  "entryPrice": "0.71692774166740866052",
                  "exitPrice": "0.71587389215656083514",
                  "realizedPnl": "349.8554576576143356474",
                  "unrealizedPnl": "-36.0755730339259790744",
                  "createdAt": "2024-04-14T08:56:06.990Z",
                  "createdAtHeight": "13220984",
                  "closedAt": null,
                  "sumOpen": "336930",
                  "sumClose": "324730",
                  "netFunding": "7.638906"
                },
                "BTC-USD": {
                  "market": "BTC-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-2.3708",
                  "maxSize": "-0.0616",
                  "entryPrice": "63547.84675746875175158345",
                  "exitPrice": "63598.67527857597940423097",
                  "realizedPnl": "-769.302394967154307493623136",
                  "unrealizedPnl": "253.29417969491665265404326",
                  "createdAt": "2024-05-06T12:29:48.437Z",
                  "createdAtHeight": "14953243",
                  "closedAt": null,
                  "sumOpen": "17.841",
                  "sumClose": "15.3818",
                  "netFunding": "12.531751"
                },
                "ARB-USD": {
                  "market": "ARB-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-12424",
                  "maxSize": "-701",
                  "entryPrice": "1.04436521258325918279",
                  "exitPrice": "1.0447271660829645913",
                  "realizedPnl": "-1078.70515690080261635452",
                  "unrealizedPnl": "-310.45217200158791301704",
                  "createdAt": "2024-04-13T21:56:35.508Z",
                  "createdAtHeight": "13185581",
                  "closedAt": null,
                  "sumOpen": "3004023",
                  "sumClose": "2991252",
                  "netFunding": "3.988973"
                },
                "OP-USD": {
                  "market": "OP-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-5260",
                  "maxSize": "-406",
                  "entryPrice": "2.92684050214474046673",
                  "exitPrice": "2.92767676925480393689",
                  "realizedPnl": "-16.02347631454881748248",
                  "unrealizedPnl": "612.8191306413348549998",
                  "createdAt": "2024-05-06T01:13:18.000Z",
                  "createdAtHeight": "14914875",
                  "closedAt": null,
                  "sumOpen": "25411",
                  "sumClose": "19203",
                  "netFunding": "0.035361"
                },
                "INJ-USD": {
                  "market": "INJ-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-921.6",
                  "maxSize": "-6.3",
                  "entryPrice": "25.85560772962955250609",
                  "exitPrice": "25.84817307376285892704",
                  "realizedPnl": "136.29717457118464042099",
                  "unrealizedPnl": "559.827569322595589612544",
                  "createdAt": "2024-04-20T09:40:05.381Z",
                  "createdAtHeight": "13692204",
                  "closedAt": null,
                  "sumOpen": "19209.2",
                  "sumClose": "18255.8",
                  "netFunding": "0.571584"
                },
                "BONK-USD": {
                  "market": "BONK-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-1257800000",
                  "maxSize": "-61400000",
                  "entryPrice": "0.000026130333455998317735",
                  "exitPrice": "0.000026177261979228302711",
                  "realizedPnl": "-813.7132102730523946128",
                  "unrealizedPnl": "1186.776380954684047083",
                  "createdAt": "2024-05-02T12:56:55.063Z",
                  "createdAtHeight": "14635404",
                  "closedAt": null,
                  "sumOpen": "19022000000",
                  "sumClose": "17745300000",
                  "netFunding": "19.047513"
                },
                "SOL-USD": {
                  "market": "SOL-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-315.6",
                  "maxSize": "-5.2",
                  "entryPrice": "152.96769190187285676602",
                  "exitPrice": "152.12810584958217270196",
                  "realizedPnl": "33.450379277235557899754",
                  "unrealizedPnl": "21.524072079073595355912",
                  "createdAt": "2024-05-06T14:59:48.785Z",
                  "createdAtHeight": "14960997",
                  "closedAt": null,
                  "sumOpen": "379.1",
                  "sumClose": "35.9",
                  "netFunding": "3.30924"
                },
                "AVAX-USD": {
                  "market": "AVAX-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-255.8",
                  "maxSize": "-0.8",
                  "entryPrice": "36.84559619381861101541",
                  "exitPrice": "36.87267572809821314119",
                  "realizedPnl": "-1287.996753089430638004814",
                  "unrealizedPnl": "-172.398744477199302258122",
                  "createdAt": "2024-05-04T03:04:11.414Z",
                  "createdAtHeight": "14762066",
                  "closedAt": null,
                  "sumOpen": "47869.5",
                  "sumClose": "47586.3",
                  "netFunding": "0.618089"
                },
                "DOT-USD": {
                  "market": "DOT-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-614",
                  "maxSize": "-112",
                  "entryPrice": "7.04377611585944919278",
                  "exitPrice": "7.04957437200112898673",
                  "realizedPnl": "-20.85399150997150996485",
                  "unrealizedPnl": "-85.44118666429819563308",
                  "createdAt": "2024-05-01T22:18:56.761Z",
                  "createdAtHeight": "14586271",
                  "closedAt": null,
                  "sumOpen": "4212",
                  "sumClose": "3543",
                  "netFunding": "-0.31077"
                },
                "DYM-USD": {
                  "market": "DYM-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-12498",
                  "maxSize": "-167",
                  "entryPrice": "3.31846163630552892303",
                  "exitPrice": "3.30022944460279610408",
                  "realizedPnl": "2614.77401109619687622485",
                  "unrealizedPnl": "675.11486815450048002894",
                  "createdAt": "2024-04-27T05:31:36.768Z",
                  "createdAtHeight": "14217769",
                  "closedAt": null,
                  "sumOpen": "156450",
                  "sumClose": "143843",
                  "netFunding": "-7.79914"
                },
                "AEVO-USD": {
                  "market": "AEVO-USD",
                  "status": "OPEN",
                  "side": "LONG",
                  "size": "35117",
                  "maxSize": "35402",
                  "entryPrice": "1.53038537249283667626",
                  "exitPrice": "1.60134942388268156425",
                  "realizedPnl": "3231.82923116618911044032",
                  "unrealizedPnl": "-5288.65572059394556022242",
                  "createdAt": "2024-04-14T11:10:58.089Z",
                  "createdAtHeight": "13228028",
                  "closedAt": null,
                  "sumOpen": "69800",
                  "sumClose": "34368",
                  "netFunding": "792.936713"
                },
                "SNX-USD": {
                  "market": "SNX-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-18829",
                  "maxSize": "-65",
                  "entryPrice": "2.77864627311186033022",
                  "exitPrice": "2.81773479108032041565",
                  "realizedPnl": "-931.74234597790280760702",
                  "unrealizedPnl": "-533.87232357678184228762",
                  "createdAt": "2024-04-13T18:46:42.554Z",
                  "createdAtHeight": "13175734",
                  "closedAt": null,
                  "sumOpen": "46567",
                  "sumClose": "27714",
                  "netFunding": "151.556841"
                },
                "ARKM-USD": {
                  "market": "ARKM-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-8580",
                  "maxSize": "-7",
                  "entryPrice": "2.01150784020880156575",
                  "exitPrice": "2.0099710061153772741",
                  "realizedPnl": "1050.7998550084823140655",
                  "unrealizedPnl": "-4868.972718248482565865",
                  "createdAt": "2024-04-05T12:53:17.372Z",
                  "createdAtHeight": "12565923",
                  "closedAt": null,
                  "sumOpen": "689650",
                  "sumClose": "681070",
                  "netFunding": "4.108259"
                },
                "ORDI-USD": {
                  "market": "ORDI-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-690.8",
                  "maxSize": "-0.2",
                  "entryPrice": "39.87346429654398085537",
                  "exitPrice": "39.72794352951099677375",
                  "realizedPnl": "3450.52352550260205133263",
                  "unrealizedPnl": "728.296144960581974889596",
                  "createdAt": "2024-04-22T15:44:53.560Z",
                  "createdAtHeight": "13865438",
                  "closedAt": null,
                  "sumOpen": "24403.8",
                  "sumClose": "23711.5",
                  "netFunding": "0.007858"
                },
                "APE-USD": {
                  "market": "APE-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-7991",
                  "maxSize": "-422",
                  "entryPrice": "1.20428624702426176369",
                  "exitPrice": "1.20286772599813059148",
                  "realizedPnl": "162.9419632939269736138",
                  "unrealizedPnl": "-555.95522974912424635321",
                  "createdAt": "2024-04-13T09:47:30.577Z",
                  "createdAtHeight": "13145992",
                  "closedAt": null,
                  "sumOpen": "157944",
                  "sumClose": "149780",
                  "netFunding": "-49.524116"
                },
                "HBAR-USD": {
                  "market": "HBAR-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-43110",
                  "maxSize": "-4900",
                  "entryPrice": "0.08902716881129452161",
                  "exitPrice": "0.09048558903469035857",
                  "realizedPnl": "-1500.4527125180605978096",
                  "unrealizedPnl": "-1061.2285964000931733929",
                  "createdAt": "2024-04-13T21:43:00.710Z",
                  "createdAtHeight": "13184857",
                  "closedAt": null,
                  "sumOpen": "1071670",
                  "sumClose": "1026510",
                  "netFunding": "-3.369769"
                },
                "LDO-USD": {
                  "market": "LDO-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-5937",
                  "maxSize": "-141",
                  "entryPrice": "1.99496195409791073449",
                  "exitPrice": "2.00343290628554260821",
                  "realizedPnl": "-1183.42352597437305043484",
                  "unrealizedPnl": "149.51338329529603066713",
                  "createdAt": "2024-04-13T21:52:33.039Z",
                  "createdAtHeight": "13185365",
                  "closedAt": null,
                  "sumOpen": "145745",
                  "sumClose": "139797",
                  "netFunding": "0.790177"
                },
                "IMX-USD": {
                  "market": "IMX-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-3315",
                  "maxSize": "-177",
                  "entryPrice": "2.17275830123868012908",
                  "exitPrice": "2.16886475198167650957",
                  "realizedPnl": "171.84949706214218763883",
                  "unrealizedPnl": "-69.7619863087753720998",
                  "createdAt": "2024-04-21T17:11:37.944Z",
                  "createdAtHeight": "13793817",
                  "closedAt": null,
                  "sumOpen": "48035",
                  "sumClose": "44533",
                  "netFunding": "-1.541932"
                },
                "CHZ-USD": {
                  "market": "CHZ-USD",
                  "status": "OPEN",
                  "side": "LONG",
                  "size": "370",
                  "maxSize": "370",
                  "entryPrice": "0.12421474201474201474",
                  "exitPrice": null,
                  "realizedPnl": "0",
                  "unrealizedPnl": "-0.0016843564545454538",
                  "createdAt": "2024-05-06T10:47:14.349Z",
                  "createdAtHeight": "14947659",
                  "closedAt": null,
                  "sumOpen": "4070",
                  "sumClose": "0",
                  "netFunding": "0"
                }
              },
              "assetPositions": {
                "USDC": {
                  "size": "1625586.093553",
                  "symbol": "USDC",
                  "side": "LONG",
                  "assetId": "0"
                }
              },
              "marginEnabled": true
            },
            "orders": [
              {
                "id": "b272a57f-b0df-5c77-b8f0-4ed52db72701",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788436",
                "clobPairId": "52",
                "side": "BUY",
                "size": "5623",
                "totalFilled": "0",
                "price": "2.19",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968042",
                "ticker": "IMX-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "e9301261-eb3c-5758-bab5-e5b0be498a2d",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34294435",
                "clobPairId": "61",
                "side": "BUY",
                "size": "4683",
                "totalFilled": "0",
                "price": "2.804",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968041",
                "ticker": "SNX-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "3e05617b-01c9-5e80-ab6d-dfb63c9cf56a",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34294436",
                "clobPairId": "61",
                "side": "SELL",
                "size": "4683",
                "totalFilled": "0",
                "price": "2.828",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968041",
                "ticker": "SNX-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "c76242b8-495e-5d8b-9710-7d41e2aab9cc",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34294437",
                "clobPairId": "30",
                "side": "SELL",
                "size": "618000000",
                "totalFilled": "0",
                "price": "0.000024335",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968041",
                "ticker": "SHIB-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "797d6c86-7bf7-52e5-a22d-89d1c18b5bf7",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000686",
                "clobPairId": "23",
                "side": "SELL",
                "size": "1646",
                "totalFilled": "0",
                "price": "9.158",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968039",
                "ticker": "APT-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "3dc35e0b-c5a9-58e7-a8ed-38e12485f532",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788386",
                "clobPairId": "64",
                "side": "SELL",
                "size": "120860",
                "totalFilled": "0",
                "price": "0.1246",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968038",
                "ticker": "CHZ-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "849360a2-4554-59d2-9e83-b75167444735",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000672",
                "clobPairId": "38",
                "side": "SELL",
                "size": "3243",
                "totalFilled": "0",
                "price": "3.597",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968038",
                "ticker": "JTO-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "6144c93d-9d1c-5b20-a248-89a212a8c5b8",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63898541",
                "clobPairId": "43",
                "side": "BUY",
                "size": "11358",
                "totalFilled": "0",
                "price": "1.315",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968038",
                "ticker": "STRK-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "6aab54d1-e26b-5b87-8244-18c6c1bc2687",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63898545",
                "clobPairId": "21",
                "side": "SELL",
                "size": "2492",
                "totalFilled": "0",
                "price": "6.047",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968038",
                "ticker": "WLD-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "0132761f-022b-59d9-966f-f00b78b36ab9",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788391",
                "clobPairId": "51",
                "side": "SELL",
                "size": "462.2",
                "totalFilled": "0",
                "price": "25.35",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968038",
                "ticker": "INJ-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "aba55d53-09f0-51f9-a2c6-4cd21e6c466a",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34294395",
                "clobPairId": "30",
                "side": "BUY",
                "size": "618000000",
                "totalFilled": "0",
                "price": "0.000024183",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968037",
                "ticker": "SHIB-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "835218be-f7ba-587b-869d-aadc23d35f54",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63898532",
                "clobPairId": "21",
                "side": "BUY",
                "size": "2492",
                "totalFilled": "0",
                "price": "5.992",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968037",
                "ticker": "WLD-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "9fbb7f10-ba4d-55e8-a128-7096cef3f49e",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788433",
                "clobPairId": "47",
                "side": "BUY",
                "size": "198600000",
                "totalFilled": "0",
                "price": "0.00002514",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968037",
                "ticker": "BONK-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "c8c37595-016a-54ba-835a-d23f8472fdd2",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34294451",
                "clobPairId": "44",
                "side": "BUY",
                "size": "2120",
                "totalFilled": "0",
                "price": "2.3527",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968037",
                "ticker": "FET-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "494a35af-2aa4-50da-a42a-a4a5b85ffb4c",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34294452",
                "clobPairId": "44",
                "side": "SELL",
                "size": "2120",
                "totalFilled": "0",
                "price": "2.3601",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968037",
                "ticker": "FET-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "930630ad-1b05-5fff-ae9c-c098f89300bf",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788441",
                "clobPairId": "39",
                "side": "SELL",
                "size": "128.6",
                "totalFilled": "0",
                "price": "38.96",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968037",
                "ticker": "ORDI-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "457b82e9-3739-5daa-878d-9c9ef3199d42",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000725",
                "clobPairId": "29",
                "side": "BUY",
                "size": "9120",
                "totalFilled": "0",
                "price": "0.5467",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968037",
                "ticker": "SEI-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "abc9a7e5-3869-5a28-8794-18bd95ffc7cf",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63898584",
                "clobPairId": "35",
                "side": "BUY",
                "size": "13280",
                "totalFilled": "0",
                "price": "1.1242",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968037",
                "ticker": "JUP-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "2c2c7ed3-64fb-50dd-bdb8-f3f93c953bdf",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788446",
                "clobPairId": "65",
                "side": "BUY",
                "size": "193",
                "totalFilled": "0",
                "price": "3.385",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968037",
                "ticker": "WIF-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "694fac3c-68f2-5b88-a29a-ea9e713e78a8",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788447",
                "clobPairId": "65",
                "side": "SELL",
                "size": "193",
                "totalFilled": "0",
                "price": "3.409",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968037",
                "ticker": "WIF-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "1cb41cab-729e-522f-a181-326556add4cf",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788448",
                "clobPairId": "13",
                "side": "BUY",
                "size": "643",
                "totalFilled": "0",
                "price": "7.55",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968037",
                "ticker": "UNI-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "5f1bbc51-d0e5-5d42-8c98-5d33771fb749",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788449",
                "clobPairId": "51",
                "side": "BUY",
                "size": "167.8",
                "totalFilled": "0",
                "price": "25.19",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968037",
                "ticker": "INJ-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "5b64fe5a-c156-5ab1-a6ab-ab7ae32e5315",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000645",
                "clobPairId": "62",
                "side": "BUY",
                "size": "5814",
                "totalFilled": "0",
                "price": "2.57",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968036",
                "ticker": "ARKM-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "cf855372-d414-5edb-85c5-77c3490caacd",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000646",
                "clobPairId": "62",
                "side": "SELL",
                "size": "5814",
                "totalFilled": "0",
                "price": "2.592",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968036",
                "ticker": "ARKM-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "0250e2a8-9c21-5f83-8068-a5a4a30a835c",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788359",
                "clobPairId": "13",
                "side": "BUY",
                "size": "1686",
                "totalFilled": "0",
                "price": "7.531",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968036",
                "ticker": "UNI-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "0f5c8329-5743-56eb-8b93-4ca3f3f5af40",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788362",
                "clobPairId": "51",
                "side": "BUY",
                "size": "442",
                "totalFilled": "0",
                "price": "25.12",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968036",
                "ticker": "INJ-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "02f8311f-4540-53c4-a742-60d5df896131",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000652",
                "clobPairId": "23",
                "side": "BUY",
                "size": "1646",
                "totalFilled": "0",
                "price": "9.082",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968036",
                "ticker": "APT-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "3e28124d-85d2-5a03-b01a-915defc5cba2",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788418",
                "clobPairId": "13",
                "side": "SELL",
                "size": "618",
                "totalFilled": "0",
                "price": "7.571",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968036",
                "ticker": "UNI-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "f88c5ae6-0ae2-53c7-826a-be56e7e834b5",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63898566",
                "clobPairId": "33",
                "side": "SELL",
                "size": "246",
                "totalFilled": "0",
                "price": "10.46",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968036",
                "ticker": "TIA-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "54634f99-adec-5ce7-a59b-9b7fae504107",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63898571",
                "clobPairId": "35",
                "side": "SELL",
                "size": "13280",
                "totalFilled": "0",
                "price": "1.1338",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968036",
                "ticker": "JUP-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "8ace4b96-3368-593b-ac51-3c61955a4d24",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63898578",
                "clobPairId": "21",
                "side": "BUY",
                "size": "831",
                "totalFilled": "0",
                "price": "6.008",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968036",
                "ticker": "WLD-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "f37eceb6-e474-5ad3-8ac2-ab31c4e77d6d",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63898579",
                "clobPairId": "21",
                "side": "SELL",
                "size": "831",
                "totalFilled": "0",
                "price": "6.031",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968036",
                "ticker": "WLD-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "3653a053-c880-52ab-b467-12fc521efd33",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788342",
                "clobPairId": "39",
                "side": "SELL",
                "size": "385.7",
                "totalFilled": "0",
                "price": "39.06",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968035",
                "ticker": "ORDI-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "f350d332-83d8-5963-86a4-0a57a5d04414",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000640",
                "clobPairId": "31",
                "side": "BUY",
                "size": "8830",
                "totalFilled": "0",
                "price": "1.0934",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968035",
                "ticker": "SUI-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "0b49d9cf-fb6b-5a64-a27f-6c818cff3cc0",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000641",
                "clobPairId": "31",
                "side": "SELL",
                "size": "8830",
                "totalFilled": "0",
                "price": "1.1026",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968035",
                "ticker": "SUI-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "2573586b-49a4-5f4d-b15d-6c88c56b993a",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788403",
                "clobPairId": "47",
                "side": "SELL",
                "size": "198600000",
                "totalFilled": "0",
                "price": "0.00002523",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968034",
                "ticker": "BONK-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "55d55c5b-8a72-5e68-873c-cf85a068670b",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47686668",
                "clobPairId": "5",
                "side": "BUY",
                "size": "0.6",
                "totalFilled": "0",
                "price": "152.51",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968034",
                "ticker": "SOL-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "6e285519-1441-510a-9bf8-d219950956d0",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47686669",
                "clobPairId": "5",
                "side": "SELL",
                "size": "0.6",
                "totalFilled": "0",
                "price": "153.26",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968034",
                "ticker": "SOL-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "c4afe09c-fc11-5d53-b3c8-85c2658fabfa",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788323",
                "clobPairId": "39",
                "side": "BUY",
                "size": "385.7",
                "totalFilled": "0",
                "price": "38.72",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968033",
                "ticker": "ORDI-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "b055b1e3-970f-5dde-968d-d53dab0d96c9",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788326",
                "clobPairId": "13",
                "side": "SELL",
                "size": "1607",
                "totalFilled": "0",
                "price": "7.596",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968033",
                "ticker": "UNI-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "2bb1a534-4894-5860-ad86-4f7316b20f53",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000673",
                "clobPairId": "25",
                "side": "BUY",
                "size": "12460",
                "totalFilled": "0",
                "price": "0.4003",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968033",
                "ticker": "BLUR-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "2819cd17-80cc-5f3b-87f0-7b1ca4372c65",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000599",
                "clobPairId": "38",
                "side": "BUY",
                "size": "2850",
                "totalFilled": "0",
                "price": "3.565",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968032",
                "ticker": "JTO-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "bb6d76d5-882b-50fe-91dc-d2f791dac9e4",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788304",
                "clobPairId": "52",
                "side": "SELL",
                "size": "6175",
                "totalFilled": "0",
                "price": "2.21",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968032",
                "ticker": "IMX-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "1ffbe4d1-0472-5842-b7b8-6e9f5cde88ad",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34294450",
                "clobPairId": "42",
                "side": "BUY",
                "size": "1534",
                "totalFilled": "0",
                "price": "3.255",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968032",
                "ticker": "DYM-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "b6b2b501-9eb2-5429-b9a0-6c7335096b02",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34294461",
                "clobPairId": "15",
                "side": "BUY",
                "size": "41900",
                "totalFilled": "0",
                "price": "0.11881",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968032",
                "ticker": "TRX-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "bfe01f9e-d8ed-5a23-801b-1c1ca9bd3e74",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63898465",
                "clobPairId": "37",
                "side": "BUY",
                "size": "25.47",
                "totalFilled": "0",
                "price": "585.9",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968031",
                "ticker": "BNB-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "5924526c-09b2-5ff3-9012-09f0c377f9fe",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63898468",
                "clobPairId": "59",
                "side": "SELL",
                "size": "10888",
                "totalFilled": "0",
                "price": "1.382",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968031",
                "ticker": "AEVO-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "c48b0b54-7b7f-5946-b294-8b0ffce7853c",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63898466",
                "clobPairId": "37",
                "side": "SELL",
                "size": "25.47",
                "totalFilled": "0",
                "price": "590.8",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968031",
                "ticker": "BNB-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "a2ced4cc-fad9-507c-b5a0-3a3cdda722e6",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000642",
                "clobPairId": "31",
                "side": "BUY",
                "size": "2970",
                "totalFilled": "0",
                "price": "1.0965",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968031",
                "ticker": "SUI-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "e0e5a27c-ad3f-5d1f-b615-ad1d106ea2da",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000643",
                "clobPairId": "31",
                "side": "SELL",
                "size": "2970",
                "totalFilled": "0",
                "price": "1.0995",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968031",
                "ticker": "SUI-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "6c48b476-dee1-5956-bb07-2136370bfde5",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000649",
                "clobPairId": "25",
                "side": "SELL",
                "size": "12460",
                "totalFilled": "0",
                "price": "0.4021",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968031",
                "ticker": "BLUR-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "5697a961-ffbb-51b6-934c-1ce813d85346",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "11380983",
                "clobPairId": "0",
                "side": "SELL",
                "size": "0.0945",
                "totalFilled": "0",
                "price": "63434",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968031",
                "ticker": "BTC-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "cecabdb7-b51e-5cac-9840-8537e4f5b6c8",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "11380984",
                "clobPairId": "0",
                "side": "BUY",
                "size": "0.0945",
                "totalFilled": "0",
                "price": "63393",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968031",
                "ticker": "BTC-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "4b73fbf2-06de-58bc-b739-37578d161d9e",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63898576",
                "clobPairId": "33",
                "side": "BUY",
                "size": "83",
                "totalFilled": "0",
                "price": "10.39",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968031",
                "ticker": "TIA-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "3c2a59dc-df03-583b-899b-aa9b3ad5c337",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788274",
                "clobPairId": "47",
                "side": "BUY",
                "size": "570400000",
                "totalFilled": "0",
                "price": "0.00002507",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968030",
                "ticker": "BONK-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "2a267797-d71b-57df-94bf-65f124f4f4bf",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788275",
                "clobPairId": "47",
                "side": "SELL",
                "size": "570400000",
                "totalFilled": "0",
                "price": "0.0000253",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968030",
                "ticker": "BONK-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "a6c99cc9-13fe-58e6-bbd3-8ab0286788da",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63898511",
                "clobPairId": "37",
                "side": "BUY",
                "size": "8.48",
                "totalFilled": "0",
                "price": "587.2",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968030",
                "ticker": "BNB-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "e94d3b37-60bd-583e-b2b2-720d4cd7fbfa",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788347",
                "clobPairId": "51",
                "side": "SELL",
                "size": "144",
                "totalFilled": "0",
                "price": "25.28",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968030",
                "ticker": "INJ-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "c5364f6c-91a9-5200-90f2-23caf79ae3f0",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788348",
                "clobPairId": "52",
                "side": "BUY",
                "size": "2188",
                "totalFilled": "0",
                "price": "2.196",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968030",
                "ticker": "IMX-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "321ec680-c83c-5dae-af19-81fd2e588a69",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34294421",
                "clobPairId": "15",
                "side": "SELL",
                "size": "41900",
                "totalFilled": "0",
                "price": "0.11927",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968030",
                "ticker": "TRX-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "2ae0cb04-089e-5a63-af08-ac6a45af1feb",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34294426",
                "clobPairId": "42",
                "side": "SELL",
                "size": "1534",
                "totalFilled": "0",
                "price": "3.266",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968030",
                "ticker": "DYM-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "e033faf1-4bec-589d-8c6a-bf584bfbe12c",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000620",
                "clobPairId": "23",
                "side": "BUY",
                "size": "549",
                "totalFilled": "0",
                "price": "9.102",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "APT-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "994c0568-d839-5180-a2d5-345f3122ae27",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788327",
                "clobPairId": "64",
                "side": "SELL",
                "size": "40280",
                "totalFilled": "0",
                "price": "0.1242",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "CHZ-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "9aeba2b8-3a32-50de-87c1-bca8662010c7",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34294359",
                "clobPairId": "30",
                "side": "BUY",
                "size": "206000000",
                "totalFilled": "0",
                "price": "0.000024232",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "SHIB-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "05ca6008-c54b-5747-a4a7-7247642cbe3c",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788334",
                "clobPairId": "39",
                "side": "BUY",
                "size": "128.6",
                "totalFilled": "0",
                "price": "38.83",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "ORDI-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "0c3326d4-f408-5a0a-a9cd-20f638756c9a",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788335",
                "clobPairId": "52",
                "side": "SELL",
                "size": "2140",
                "totalFilled": "0",
                "price": "2.204",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "IMX-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "5bb03942-7c8f-5f81-9f4e-6cf29c3e6915",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000624",
                "clobPairId": "29",
                "side": "SELL",
                "size": "9120",
                "totalFilled": "0",
                "price": "0.5491",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "SEI-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "f6d28dc3-839a-5540-aa92-4b687c48e32f",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000627",
                "clobPairId": "23",
                "side": "SELL",
                "size": "549",
                "totalFilled": "0",
                "price": "9.137",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "APT-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "c8525a08-fdb6-5bd9-bcc8-4c7f4bd56850",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34294367",
                "clobPairId": "30",
                "side": "SELL",
                "size": "206000000",
                "totalFilled": "0",
                "price": "0.000024291",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "SHIB-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "c96ab334-b898-552b-8e57-2246f4b10ae4",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47686568",
                "clobPairId": "7",
                "side": "BUY",
                "size": "65.3",
                "totalFilled": "0",
                "price": "37.47",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "AVAX-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "d80b2665-d192-59fe-80db-459fe380455c",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47686569",
                "clobPairId": "7",
                "side": "SELL",
                "size": "65.3",
                "totalFilled": "0",
                "price": "37.58",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "AVAX-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "4cff732e-4644-56b4-9e60-812422775fd8",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "11380987",
                "clobPairId": "0",
                "side": "BUY",
                "size": "0.1419",
                "totalFilled": "0",
                "price": "63379",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "BTC-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "c5161857-a5ce-54ef-b9f1-bdb82e07a964",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47686679",
                "clobPairId": "8",
                "side": "BUY",
                "size": "196",
                "totalFilled": "0",
                "price": "6.141",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "FIL-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "e53ee137-5369-52f7-8381-304e844bf61f",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788430",
                "clobPairId": "65",
                "side": "BUY",
                "size": "76",
                "totalFilled": "0",
                "price": "3.39",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "WIF-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "bbfd8b05-6a77-595a-8ecc-98ab860d4977",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63898580",
                "clobPairId": "21",
                "side": "BUY",
                "size": "332",
                "totalFilled": "0",
                "price": "6.016",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "WLD-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "fc9f083f-aa84-5fe7-b00a-17e5c6b80c2d",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000716",
                "clobPairId": "23",
                "side": "SELL",
                "size": "212",
                "totalFilled": "0",
                "price": "9.122",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "APT-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "ad81e6ec-aa40-5a18-b6c9-62c21d0b5dcc",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000715",
                "clobPairId": "23",
                "side": "BUY",
                "size": "212",
                "totalFilled": "0",
                "price": "9.118",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "APT-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "f3591cbb-5423-596b-a7ea-8a63eef3078f",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000717",
                "clobPairId": "62",
                "side": "BUY",
                "size": "776",
                "totalFilled": "0",
                "price": "2.578",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "ARKM-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "b603ef55-c8ca-5743-b566-2cf87f9fdada",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63898581",
                "clobPairId": "59",
                "side": "SELL",
                "size": "1452",
                "totalFilled": "0",
                "price": "1.376",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "AEVO-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "6b7c0961-84a9-5bb3-be13-37138e7b53ff",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47686684",
                "clobPairId": "12",
                "side": "BUY",
                "size": "74",
                "totalFilled": "0",
                "price": "7.16",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "DOT-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "82e5899d-328c-5d87-81a8-7afa1e21e3bc",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47686685",
                "clobPairId": "12",
                "side": "SELL",
                "size": "74",
                "totalFilled": "0",
                "price": "7.19",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "DOT-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "92397f02-8eb9-5262-b59e-c803e21777c6",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788432",
                "clobPairId": "39",
                "side": "SELL",
                "size": "51.4",
                "totalFilled": "0",
                "price": "38.92",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "ORDI-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "0f8b7235-7830-55df-9749-fc3dbc3a8d90",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788431",
                "clobPairId": "39",
                "side": "BUY",
                "size": "51.4",
                "totalFilled": "0",
                "price": "38.86",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "ORDI-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "d44283dd-eae8-52ab-969c-b9e7952fc4ff",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34294446",
                "clobPairId": "30",
                "side": "BUY",
                "size": "82000000",
                "totalFilled": "0",
                "price": "0.000024254",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "SHIB-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "77e17250-e921-5a6b-9d81-3e671af22662",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34294447",
                "clobPairId": "30",
                "side": "SELL",
                "size": "82000000",
                "totalFilled": "0",
                "price": "0.000024267",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "SHIB-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "98e0fd7b-6e32-5d7b-a352-307088249f88",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47686687",
                "clobPairId": "2",
                "side": "SELL",
                "size": "67",
                "totalFilled": "0",
                "price": "14.735",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "LINK-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "7de6b567-2bfe-53c1-a091-e6f742131526",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47686686",
                "clobPairId": "2",
                "side": "BUY",
                "size": "67",
                "totalFilled": "0",
                "price": "14.732",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "LINK-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "192cc1af-2751-5346-a29e-105f085a5ecf",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47686688",
                "clobPairId": "10",
                "side": "BUY",
                "size": "400",
                "totalFilled": "0",
                "price": "0.15673",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "DOGE-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "8246d007-b48a-5983-819d-375504e7e70d",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34294448",
                "clobPairId": "42",
                "side": "BUY",
                "size": "614",
                "totalFilled": "0",
                "price": "3.26",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "DYM-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "9b256840-b533-50d8-a43f-9444d1a268b7",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34294449",
                "clobPairId": "42",
                "side": "SELL",
                "size": "614",
                "totalFilled": "0",
                "price": "3.262",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "DYM-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "7d1f8fbd-3a4c-5810-a05e-fc56c11d13e8",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788434",
                "clobPairId": "64",
                "side": "BUY",
                "size": "16110",
                "totalFilled": "0",
                "price": "0.1239",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "CHZ-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "ba29358e-f0e8-5fd6-9d66-6ab57bebbadc",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788435",
                "clobPairId": "64",
                "side": "SELL",
                "size": "16110",
                "totalFilled": "0",
                "price": "0.1241",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "CHZ-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "af7fcc6a-30db-5a51-b044-9f81956f02e7",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47686689",
                "clobPairId": "16",
                "side": "BUY",
                "size": "10",
                "totalFilled": "0",
                "price": "7.255",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "NEAR-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "ed3085c8-3f8c-5e5f-a1a6-2a9b17c15866",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000719",
                "clobPairId": "38",
                "side": "SELL",
                "size": "468",
                "totalFilled": "0",
                "price": "3.583",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "JTO-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "90d3148f-8924-5a4f-93bf-d40df95b7bbd",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000718",
                "clobPairId": "38",
                "side": "BUY",
                "size": "468",
                "totalFilled": "0",
                "price": "3.58",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "JTO-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "1efded55-12ef-5c79-a298-cda137b0dd50",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47686690",
                "clobPairId": "53",
                "side": "SELL",
                "size": "3520",
                "totalFilled": "0",
                "price": "0.1135",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "HBAR-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "5e01a222-e0be-52d4-b6ad-277921a35e1d",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47686691",
                "clobPairId": "3",
                "side": "SELL",
                "size": "690",
                "totalFilled": "0",
                "price": "0.7214",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "MATIC-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "65616ba1-8376-5e95-a4d0-7c25b80028a7",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64000720",
                "clobPairId": "62",
                "side": "SELL",
                "size": "776",
                "totalFilled": "0",
                "price": "2.58",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "ARKM-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "055a99b9-427e-52ef-91f0-4e892c27ffd0",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788437",
                "clobPairId": "51",
                "side": "BUY",
                "size": "66.6",
                "totalFilled": "0",
                "price": "25.22",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "INJ-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              },
              {
                "id": "b03dc443-96e8-5a3c-a540-cba51c9d3cef",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24788438",
                "clobPairId": "51",
                "side": "SELL",
                "size": "66.6",
                "totalFilled": "0",
                "price": "25.25",
                "type": "LIMIT",
                "status": "BEST_EFFORT_OPENED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "goodTilBlock": "14968029",
                "ticker": "INJ-USD",
                "orderFlags": "0",
                "clientMetadata": "0"
              }
            ]
          }
        }
    """.trimIndent()
    internal val channel_batch_data_1 = """
        {
          "type": "channel_batch_data",
          "connection_id": "9795f210-fa8f-4dd2-9f5f-e5ea805e09ac",
          "message_id": 3,
          "id": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3/0",
          "channel": "v4_subaccounts",
          "version": "2.4.0",
          "contents": [
            {
              "orders": [
                {
                  "id": "fcd701e4-6d41-5faa-b8d2-b5a1decf294c",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686710",
                  "clobPairId": "12",
                  "side": "BUY",
                  "size": "77",
                  "price": "7.16",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "DOT-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "baf5bcb8-59a3-59b9-b200-900b2cd7b7ad",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686711",
                  "clobPairId": "12",
                  "side": "SELL",
                  "size": "77",
                  "price": "7.19",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "DOT-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "bb6d76d5-882b-50fe-91dc-d2f791dac9e4",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24788304",
                  "clobPairId": "52",
                  "side": "SELL",
                  "size": "6175",
                  "totalOptimisticFilled": "0",
                  "price": "2.21",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968032",
                  "ticker": "IMX-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "bcf680c6-7228-5e3d-a67f-d0653cff9f64",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "17443054",
                  "clobPairId": "52",
                  "side": "SELL",
                  "size": "545",
                  "price": "2.201",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "IMX-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "bcf680c6-7228-5e3d-a67f-d0653cff9f64",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "17443054",
                  "clobPairId": "52",
                  "side": "SELL",
                  "size": "545",
                  "totalOptimisticFilled": "0",
                  "price": "2.201",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "IMX-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "bb433f54-2105-5c39-b38e-2f4ce1c94c3d",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "37238037",
                  "clobPairId": "32",
                  "side": "SELL",
                  "size": "3540",
                  "price": "0.5649",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "XRP-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "bb433f54-2105-5c39-b38e-2f4ce1c94c3d",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "37238037",
                  "clobPairId": "32",
                  "side": "SELL",
                  "size": "3540",
                  "totalOptimisticFilled": "0",
                  "price": "0.5649",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "XRP-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "bcdcdd44-c2fa-5e5a-95c3-c74fbb8d9da7",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "34294462",
                  "clobPairId": "15",
                  "side": "BUY",
                  "size": "125900",
                  "price": "0.11855",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968038",
                  "ticker": "TRX-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "ad9b99e9-451e-5dda-b6ae-8a5697a4a8f5",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "34294463",
                  "clobPairId": "15",
                  "side": "SELL",
                  "size": "125900",
                  "price": "0.11953",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968038",
                  "ticker": "TRX-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "c189db80-1aa7-5442-aba5-1ab64f99da3c",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686712",
                  "clobPairId": "16",
                  "side": "SELL",
                  "size": "10",
                  "price": "7.277",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "NEAR-USD",
                  "clientMetadata": "0"
                }
              ]
            }
          ]
        }
    """.trimIndent()
    internal val channel_batch_data_2 = """
        {
          "type": "channel_batch_data",
          "connection_id": "9795f210-fa8f-4dd2-9f5f-e5ea805e09ac",
          "message_id": 4,
          "id": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3/0",
          "channel": "v4_subaccounts",
          "version": "2.4.0",
          "contents": [
            {
              "orders": [
                {
                  "id": "d33319e4-6f04-51cd-a155-1d67fe6c7b42",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686709",
                  "clobPairId": "22",
                  "side": "SELL",
                  "size": "943",
                  "totalOptimisticFilled": "0",
                  "price": "1.274",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "APE-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "abb6723c-453e-5799-98b3-d0ef1f0c1b66",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63898591",
                  "clobPairId": "21",
                  "side": "BUY",
                  "size": "332",
                  "totalOptimisticFilled": "0",
                  "price": "6.016",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "WLD-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "e94d3b37-60bd-583e-b2b2-720d4cd7fbfa",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24788347",
                  "clobPairId": "51",
                  "side": "SELL",
                  "size": "144",
                  "totalOptimisticFilled": "0",
                  "price": "25.28",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "INJ-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "9cc2d0ce-c701-5e04-a00e-1ebdc8a33355",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63898589",
                  "clobPairId": "43",
                  "side": "SELL",
                  "size": "1514",
                  "totalOptimisticFilled": "0",
                  "price": "1.322",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "STRK-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "2ae0cb04-089e-5a63-af08-ac6a45af1feb",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "34294426",
                  "clobPairId": "42",
                  "side": "SELL",
                  "size": "1534",
                  "totalOptimisticFilled": "0",
                  "price": "3.266",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "DYM-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "27b6f3cb-732e-54e3-bb3c-c59756667347",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63898586",
                  "clobPairId": "37",
                  "side": "SELL",
                  "size": "3.4",
                  "totalOptimisticFilled": "0",
                  "price": "588.4",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "BNB-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "4c2f38f2-feb6-51b7-a288-b3310971bea7",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "64000728",
                  "clobPairId": "62",
                  "side": "BUY",
                  "size": "776",
                  "totalOptimisticFilled": "0",
                  "price": "2.578",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "ARKM-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "aef25a33-ff7f-5e05-bcec-f0b87a835441",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24788450",
                  "clobPairId": "65",
                  "side": "SELL",
                  "size": "78",
                  "totalOptimisticFilled": "0",
                  "price": "3.406",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "WIF-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "46c72229-17cc-5c3c-a2e3-a77361338492",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "11380991",
                  "clobPairId": "0",
                  "side": "SELL",
                  "size": "0.0631",
                  "totalOptimisticFilled": "0",
                  "price": "63421",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "BTC-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "321ec680-c83c-5dae-af19-81fd2e588a69",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "34294421",
                  "clobPairId": "15",
                  "side": "SELL",
                  "size": "41900",
                  "totalOptimisticFilled": "0",
                  "price": "0.11927",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "TRX-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "30503cf0-ea29-56cb-ad3f-64a1815a72ef",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63898592",
                  "clobPairId": "21",
                  "side": "SELL",
                  "size": "332",
                  "totalOptimisticFilled": "0",
                  "price": "6.022",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "WLD-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "baf5bcb8-59a3-59b9-b200-900b2cd7b7ad",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686711",
                  "clobPairId": "12",
                  "side": "SELL",
                  "size": "77",
                  "totalOptimisticFilled": "0",
                  "price": "7.19",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "DOT-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "3c2a59dc-df03-583b-899b-aa9b3ad5c337",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24788274",
                  "clobPairId": "47",
                  "side": "BUY",
                  "size": "570400000",
                  "totalOptimisticFilled": "0",
                  "price": "0.00002507",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "BONK-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "1939d307-b815-5797-b0c1-515755bac38c",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686708",
                  "clobPairId": "10",
                  "side": "SELL",
                  "size": "400",
                  "totalOptimisticFilled": "0",
                  "price": "0.15699",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "DOGE-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "5cf5a4ae-5a08-598d-806a-e1c8191a3292",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24788451",
                  "clobPairId": "65",
                  "side": "BUY",
                  "size": "78",
                  "totalOptimisticFilled": "0",
                  "price": "3.39",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "WIF-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "a6c99cc9-13fe-58e6-bbd3-8ab0286788da",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63898511",
                  "clobPairId": "37",
                  "side": "BUY",
                  "size": "8.48",
                  "totalOptimisticFilled": "0",
                  "price": "587.2",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "BNB-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "516c0fa7-b7f9-53d6-a84d-9ac0a13c68d6",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686703",
                  "clobPairId": "16",
                  "side": "BUY",
                  "size": "39",
                  "totalOptimisticFilled": "0",
                  "price": "7.253",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "NEAR-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "fcd701e4-6d41-5faa-b8d2-b5a1decf294c",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686710",
                  "clobPairId": "12",
                  "side": "BUY",
                  "size": "77",
                  "totalOptimisticFilled": "0",
                  "price": "7.16",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "DOT-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "2a267797-d71b-57df-94bf-65f124f4f4bf",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24788275",
                  "clobPairId": "47",
                  "side": "SELL",
                  "size": "570400000",
                  "totalOptimisticFilled": "0",
                  "price": "0.0000253",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "BONK-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "d614c78f-e838-5073-aa84-a55bd03d3480",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63898585",
                  "clobPairId": "37",
                  "side": "BUY",
                  "size": "3.4",
                  "totalOptimisticFilled": "0",
                  "price": "588.2",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "BNB-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "c5364f6c-91a9-5200-90f2-23caf79ae3f0",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24788348",
                  "clobPairId": "52",
                  "side": "BUY",
                  "size": "2188",
                  "totalOptimisticFilled": "0",
                  "price": "2.196",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "IMX-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "c189db80-1aa7-5442-aba5-1ab64f99da3c",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686712",
                  "clobPairId": "16",
                  "side": "SELL",
                  "size": "10",
                  "totalOptimisticFilled": "0",
                  "price": "7.277",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "NEAR-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "b92bcc0d-316d-5d42-847a-d9cc6b5edf3c",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63898588",
                  "clobPairId": "43",
                  "side": "BUY",
                  "size": "1514",
                  "totalOptimisticFilled": "0",
                  "price": "1.32",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "STRK-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "a5108e35-10b8-5796-9fd3-d60b19b591dc",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "11380990",
                  "clobPairId": "0",
                  "side": "BUY",
                  "size": "0.0631",
                  "totalOptimisticFilled": "0",
                  "price": "63397",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "BTC-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "createdAtHeight": "10258167",
                  "updatedAt": "2024-03-07T16:37:14.096Z",
                  "updatedAtHeight": "10258167",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "6346842f-4308-527a-a1c4-ffae435258f4",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686704",
                  "clobPairId": "16",
                  "side": "SELL",
                  "size": "39",
                  "totalOptimisticFilled": "0",
                  "price": "7.278",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "NEAR-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "1c4893da-ff54-5e67-819b-260dfcc8e301",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686707",
                  "clobPairId": "26",
                  "side": "SELL",
                  "size": "305",
                  "totalOptimisticFilled": "0",
                  "price": "1.972",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968030",
                  "ticker": "LDO-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "0f000af5-be09-5e45-89e6-a86b5a58317d",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24788455",
                  "clobPairId": "65",
                  "side": "SELL",
                  "size": "586",
                  "price": "3.418",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968043",
                  "ticker": "WIF-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "1aaf2811-2fac-5c15-a8b1-7fd8d251cd83",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24788454",
                  "clobPairId": "65",
                  "side": "BUY",
                  "size": "586",
                  "price": "3.375",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968043",
                  "ticker": "WIF-USD",
                  "clientMetadata": "0"
                }
              ]
            }
          ]
        }
    """.trimIndent()
    internal val channel_batch_data_3 = """
        {
          "type": "channel_batch_data",
          "connection_id": "9795f210-fa8f-4dd2-9f5f-e5ea805e09ac",
          "message_id": 5,
          "id": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3/0",
          "channel": "v4_subaccounts",
          "version": "2.4.0",
          "contents": [
            {
              "orders": [
                {
                  "id": "68ba46f2-9297-5dff-995c-b5b225ea7597",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63898594",
                  "clobPairId": "59",
                  "side": "SELL",
                  "size": "3629",
                  "price": "1.378",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968038",
                  "ticker": "AEVO-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "57780287-3c4c-5e65-acf0-a6ce1550db8a",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686718",
                  "clobPairId": "8",
                  "side": "SELL",
                  "size": "205",
                  "price": "6.149",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "FIL-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "f017b428-6a53-5682-8c1a-214e06dc6ec9",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24788460",
                  "clobPairId": "64",
                  "side": "BUY",
                  "size": "40280",
                  "price": "0.1238",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968038",
                  "ticker": "CHZ-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "24cdb516-4d93-57b0-b8a9-ad33167010f6",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "11380992",
                  "clobPairId": "0",
                  "side": "SELL",
                  "size": "0.142",
                  "price": "63447",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "BTC-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "cfc18683-0028-5f47-959a-908bfc107146",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "1266448",
                  "clobPairId": "1",
                  "side": "SELL",
                  "size": "2.917",
                  "price": "3087.9",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "ETH-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "ccce00a6-347d-599d-b30b-4fa3f4ed0940",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "1266447",
                  "clobPairId": "1",
                  "side": "BUY",
                  "size": "2.917",
                  "price": "3083.9",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "ETH-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "7bdd695a-c07c-57d2-8415-e94eafd65b4f",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24788467",
                  "clobPairId": "64",
                  "side": "SELL",
                  "size": "40280",
                  "price": "0.1242",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968038",
                  "ticker": "CHZ-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "0fe49275-cc8a-5558-9ce4-f68567ff2e59",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "34294472",
                  "clobPairId": "30",
                  "side": "BUY",
                  "size": "206000000",
                  "price": "0.00002423",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968038",
                  "ticker": "SHIB-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "604f3339-d953-5b49-af26-320cb9393fe1",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24788468",
                  "clobPairId": "39",
                  "side": "BUY",
                  "size": "128.6",
                  "price": "38.83",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968039",
                  "ticker": "ORDI-USD",
                  "clientMetadata": "0"
                }
              ]
            }
          ]
        }
    """.trimIndent()
    internal val channel_batch_data_4 = """
        {
          "type": "channel_batch_data",
          "connection_id": "9795f210-fa8f-4dd2-9f5f-e5ea805e09ac",
          "message_id": 6,
          "id": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3/0",
          "channel": "v4_subaccounts",
          "version": "2.4.0",
          "contents": [
            {
              "orders": [
                {
                  "id": "46e0dd59-2614-51ba-b9f8-fe2376e713e6",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24788469",
                  "clobPairId": "52",
                  "side": "SELL",
                  "size": "1917",
                  "price": "2.204",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968039",
                  "ticker": "IMX-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "b94bdfef-6699-533f-8572-400dfbd97b63",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686724",
                  "clobPairId": "4",
                  "side": "SELL",
                  "size": "190",
                  "price": "0.4448",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "CRV-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "893bb0b3-91de-56de-9cba-ef5a679ace3f",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "34294473",
                  "clobPairId": "61",
                  "side": "BUY",
                  "size": "664",
                  "price": "2.815",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "SNX-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "927bc99a-fac9-5678-8288-88b781332677",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "34294474",
                  "clobPairId": "61",
                  "side": "SELL",
                  "size": "664",
                  "price": "2.817",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "SNX-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "7c9a335e-97a0-550d-837f-dd27e76520e0",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686726",
                  "clobPairId": "7",
                  "side": "SELL",
                  "size": "12.6",
                  "price": "37.57",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "AVAX-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "58db90c0-9386-59f6-9f89-05845a878be7",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "64000740",
                  "clobPairId": "23",
                  "side": "BUY",
                  "size": "549",
                  "price": "9.101",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968039",
                  "ticker": "APT-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "91801a51-9c45-58e1-b97b-95e6e0c00e5b",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686725",
                  "clobPairId": "7",
                  "side": "BUY",
                  "size": "12.6",
                  "price": "37.49",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "AVAX-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "f020534d-ac27-59bd-8148-28519aea5d23",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63898596",
                  "clobPairId": "35",
                  "side": "SELL",
                  "size": "1770",
                  "price": "1.1291",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "JUP-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "65e2d63b-fbb0-5d7b-a3cc-59ec62e1d3c2",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63898595",
                  "clobPairId": "35",
                  "side": "BUY",
                  "size": "1770",
                  "price": "1.1286",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "JUP-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "72cf9a34-cddd-5a14-b772-ac468e2f281c",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24788470",
                  "clobPairId": "65",
                  "side": "SELL",
                  "size": "79",
                  "price": "3.405",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "WIF-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "60201028-3e89-5936-9acf-0e090e68b776",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "11328194",
                  "clobPairId": "12",
                  "side": "SELL",
                  "size": "279",
                  "price": "7.19",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "DOT-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "f1c4b621-0c32-5f56-b716-a8f929fa0984",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686728",
                  "clobPairId": "8",
                  "side": "SELL",
                  "size": "52",
                  "price": "6.148",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "FIL-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "9b9fadca-38e6-59a2-81e7-c698bdb948a4",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686727",
                  "clobPairId": "8",
                  "side": "BUY",
                  "size": "52",
                  "price": "6.141",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "FIL-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "5ce8b1e6-736a-5af0-9e02-3f838d1e0f8f",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "22310570",
                  "clobPairId": "2",
                  "side": "SELL",
                  "size": "136",
                  "price": "14.735",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "LINK-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "5ce8b1e6-736a-5af0-9e02-3f838d1e0f8f",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "22310570",
                  "clobPairId": "2",
                  "side": "SELL",
                  "size": "136",
                  "totalOptimisticFilled": "0",
                  "price": "14.735",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "LINK-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "60201028-3e89-5936-9acf-0e090e68b776",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "11328194",
                  "clobPairId": "12",
                  "side": "SELL",
                  "size": "279",
                  "totalOptimisticFilled": "0",
                  "price": "7.19",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "DOT-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "f77c9894-95b0-57f2-a1b5-a73ece471fcd",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686729",
                  "clobPairId": "10",
                  "side": "SELL",
                  "size": "400",
                  "price": "0.15697",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "DOGE-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "5465f2dd-ed49-5268-80b0-fbd56b35c8ea",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63898599",
                  "clobPairId": "33",
                  "side": "BUY",
                  "size": "261",
                  "price": "10.36",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968039",
                  "ticker": "TIA-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "e1c06fbc-cc8f-546d-8635-9b6daaf2999c",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63898598",
                  "clobPairId": "33",
                  "side": "SELL",
                  "size": "35",
                  "price": "10.42",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "TIA-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "f3783074-5286-5301-8065-8475431c1f13",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63898597",
                  "clobPairId": "33",
                  "side": "BUY",
                  "size": "35",
                  "price": "10.4",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "TIA-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "c7e9fe8f-0d94-5e0b-bdb5-e4b8ad9ef8c2",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47686730",
                  "clobPairId": "27",
                  "side": "BUY",
                  "size": "213",
                  "price": "2.812",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14968031",
                  "ticker": "OP-USD",
                  "clientMetadata": "0"
                }
              ]
            }
          ]
        }
    """.trimIndent()
    internal val channel_batch_data_order_filled_1 = """
        {
          "type": "channel_batch_data",
          "connection_id": "a636f0fd-d238-46cd-a541-78cd747d0e09",
          "message_id": 36,
          "id": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3/0",
          "channel": "v4_subaccounts",
          "version": "2.4.0",
          "contents": [
            {
              "orders": [
                {
                  "id": "8fe0d00e-2070-5e57-9320-9fe61fea1261",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "15324164",
                  "clobPairId": "8",
                  "side": "SELL",
                  "size": "326",
                  "price": "6.147",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "FIL-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "cc9be1bb-921b-545f-b6e6-f36b723411aa",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24854739",
                  "clobPairId": "39",
                  "side": "SELL",
                  "size": "51.5",
                  "totalOptimisticFilled": "0",
                  "price": "38.78",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972962",
                  "ticker": "ORDI-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "a7fdbc3a-c0d1-5294-84b2-ae7640e04bd0",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "1282925",
                  "clobPairId": "1",
                  "side": "SELL",
                  "size": "2.477",
                  "totalOptimisticFilled": "0",
                  "price": "3080",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972962",
                  "ticker": "ETH-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "createdAtHeight": "9654026",
                  "updatedAt": "2024-02-28T20:31:10.520Z",
                  "updatedAtHeight": "9654026",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "da141f7c-e86f-5f3b-9827-fe16c9dec364",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63950079",
                  "clobPairId": "37",
                  "side": "SELL",
                  "size": "3.39",
                  "totalOptimisticFilled": "0",
                  "price": "589.1",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972962",
                  "ticker": "BNB-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "da7caa9c-44e0-5962-94ba-0d359af2fe64",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "64054754",
                  "clobPairId": "23",
                  "side": "SELL",
                  "size": "84",
                  "totalOptimisticFilled": "0",
                  "price": "9.133",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972962",
                  "ticker": "APT-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "8fe0d00e-2070-5e57-9320-9fe61fea1261",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "15324164",
                  "clobPairId": "8",
                  "side": "SELL",
                  "size": "326",
                  "totalOptimisticFilled": "0",
                  "price": "6.147",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "FIL-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "c7564ae2-543c-5633-a504-a6f9f5b8926c",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "1282924",
                  "clobPairId": "1",
                  "side": "BUY",
                  "size": "2.477",
                  "totalOptimisticFilled": "0",
                  "price": "3075.9",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972962",
                  "ticker": "ETH-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "0b0a04ad-bb1a-55e3-8f86-160e8f29b242",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785506",
                  "clobPairId": "7",
                  "side": "BUY",
                  "size": "26.7",
                  "totalOptimisticFilled": "0",
                  "price": "37.28",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972962",
                  "ticker": "AVAX-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "90df67b8-ceb7-5e73-a8fe-8b9cc973d74d",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "34352315",
                  "clobPairId": "15",
                  "side": "SELL",
                  "size": "16800",
                  "totalOptimisticFilled": "0",
                  "price": "0.11905",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972962",
                  "ticker": "TRX-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "0058b8f9-b6dc-50a3-af52-ec44448cfae0",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785500",
                  "clobPairId": "24",
                  "side": "SELL",
                  "size": "1124",
                  "totalOptimisticFilled": "0",
                  "price": "1.07",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972962",
                  "ticker": "ARB-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "f64ed412-6dd7-5641-9b1b-db2cf7598e1a",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785505",
                  "clobPairId": "12",
                  "side": "BUY",
                  "size": "83",
                  "totalOptimisticFilled": "0",
                  "price": "7.21",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972962",
                  "ticker": "DOT-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "perpetualPositions": [
                {
                  "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
                  "subaccountNumber": 0,
                  "positionId": "caecb4f6-3391-5270-a0ac-5fd3d2cf68c0",
                  "market": "APT-USD",
                  "side": "SHORT",
                  "status": "OPEN",
                  "size": "-2778",
                  "maxSize": "-42",
                  "netFunding": "4.708773",
                  "entryPrice": "9.12987054981949730355",
                  "exitPrice": "9.13249631359380487784",
                  "sumOpen": "2921286",
                  "sumClose": "2918425",
                  "realizedPnl": "-7658.38587003358249729325",
                  "unrealizedPnl": "-22.4499991354364907381"
                }
              ],
              "assetPositions": [
                {
                  "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
                  "subaccountNumber": 0,
                  "positionId": "e326796e-660a-532b-958b-1b83e7e5f895",
                  "assetId": "0",
                  "symbol": "USDC",
                  "side": "LONG",
                  "size": "1599714.624632"
                }
              ]
            },
            {
              "perpetualPositions": [
                {
                  "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
                  "subaccountNumber": 0,
                  "positionId": "caecb4f6-3391-5270-a0ac-5fd3d2cf68c0",
                  "market": "APT-USD",
                  "side": "SHORT",
                  "status": "OPEN",
                  "size": "-2776",
                  "maxSize": "-42",
                  "netFunding": "4.708773",
                  "entryPrice": "9.12987054981949730355",
                  "exitPrice": "9.13249618726287105037",
                  "sumOpen": "2921286",
                  "sumClose": "2918507",
                  "realizedPnl": "-7658.23248494838371039774",
                  "unrealizedPnl": "-22.4338364290754853452"
                }
              ],
              "assetPositions": [
                {
                  "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
                  "subaccountNumber": 0,
                  "positionId": "e326796e-660a-532b-958b-1b83e7e5f895",
                  "assetId": "0",
                  "symbol": "USDC",
                  "side": "LONG",
                  "size": "1599696.370275"
                }
              ]
            },
            {
              "fills": [
                {
                  "id": "a74830f8-d506-54b3-bf3b-1de791b8fe4e",
                  "fee": "-0.067364",
                  "side": "BUY",
                  "size": "82",
                  "type": "LIMIT",
                  "price": "9.128",
                  "eventId": "00e478220000000200000005",
                  "orderId": "f7c9cd24-57cd-5240-a98d-3c9c3c11767d",
                  "createdAt": "2024-05-06T18:41:20.606Z",
                  "liquidity": "MAKER",
                  "clobPairId": "23",
                  "quoteAmount": "748.496",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientMetadata": "0",
                  "createdAtHeight": "14972962",
                  "transactionHash": "E06299AA699592C0F60ACBDA3C241AAED74A7D6986B07A1707F425072453DD4B",
                  "market": "APT-USD"
                },
                {
                  "id": "0d473eec-93b0-5c49-94ca-b8017454d769",
                  "fee": "-0.001643",
                  "side": "BUY",
                  "size": "2",
                  "type": "LIMIT",
                  "price": "9.128",
                  "eventId": "00e47822000000020000000b",
                  "orderId": "f7c9cd24-57cd-5240-a98d-3c9c3c11767d",
                  "createdAt": "2024-05-06T18:41:20.606Z",
                  "liquidity": "MAKER",
                  "clobPairId": "23",
                  "quoteAmount": "18.256",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientMetadata": "0",
                  "createdAtHeight": "14972962",
                  "transactionHash": "E06299AA699592C0F60ACBDA3C241AAED74A7D6986B07A1707F425072453DD4B",
                  "market": "APT-USD"
                }
              ],
              "perpetualPositions": [
                {
                  "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
                  "subaccountNumber": 0,
                  "positionId": "caecb4f6-3391-5270-a0ac-5fd3d2cf68c0",
                  "market": "APT-USD",
                  "side": "SHORT",
                  "status": "OPEN",
                  "size": "-2776",
                  "maxSize": "-42",
                  "netFunding": "4.708773",
                  "entryPrice": "9.12987054981949730355",
                  "exitPrice": "9.13249618418171744566",
                  "sumOpen": "2921286",
                  "sumClose": "2918509"
                }
              ],
              "orders": [
                {
                  "id": "f7c9cd24-57cd-5240-a98d-3c9c3c11767d",
                  "side": "BUY",
                  "size": "84",
                  "type": "LIMIT",
                  "price": "9.128",
                  "status": "FILLED",
                  "clientId": "64054753",
                  "updatedAt": "2024-05-06T18:41:20.606Z",
                  "clobPairId": "23",
                  "orderFlags": "0",
                  "reduceOnly": false,
                  "timeInForce": "GTT",
                  "totalFilled": "84",
                  "goodTilBlock": "14972962",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "triggerPrice": null,
                  "clientMetadata": "0",
                  "createdAtHeight": "14972962",
                  "updatedAtHeight": "14972962",
                  "goodTilBlockTime": null,
                  "postOnly": true,
                  "ticker": "APT-USD"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "31cd45c6-ad44-5349-b4b9-90bf92ed5f11",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63950089",
                  "clobPairId": "43",
                  "side": "BUY",
                  "size": "1482",
                  "price": "1.321",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "STRK-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "b2bf0614-efde-52c6-8431-50c685e92a15",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "64054762",
                  "clobPairId": "25",
                  "side": "SELL",
                  "size": "4990",
                  "totalOptimisticFilled": "0",
                  "price": "0.4008",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972962",
                  "ticker": "BLUR-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "adf6a5dd-018b-5a18-8e8f-848eca239639",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "64054764",
                  "clobPairId": "38",
                  "side": "SELL",
                  "size": "266",
                  "totalOptimisticFilled": "0",
                  "price": "3.585",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972962",
                  "ticker": "JTO-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "46ff7334-54c1-5392-b294-c926d01c4e9c",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "34352324",
                  "clobPairId": "46",
                  "side": "SELL",
                  "size": "3670",
                  "price": "0.5447",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "PYTH-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "42bc3384-9e10-53e3-b5c2-caecae66475a",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "34352323",
                  "clobPairId": "46",
                  "side": "BUY",
                  "size": "3670",
                  "price": "0.5444",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "PYTH-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "c8d9dbe0-643b-5320-b406-0cb0a5b98226",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "26450119",
                  "clobPairId": "39",
                  "side": "SELL",
                  "size": "25.8",
                  "price": "38.77",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "ORDI-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "c8d9dbe0-643b-5320-b406-0cb0a5b98226",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "26450119",
                  "clobPairId": "39",
                  "side": "SELL",
                  "size": "25.8",
                  "totalOptimisticFilled": "0",
                  "price": "38.77",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "ORDI-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "6db68c65-119e-55e7-ab3c-ae885629245f",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "34352217",
                  "clobPairId": "15",
                  "side": "BUY",
                  "size": "125500",
                  "totalOptimisticFilled": "0",
                  "price": "0.11854",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972962",
                  "ticker": "TRX-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "3b2839d5-ebd2-50cd-87ef-3bf8fb50dbe8",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "9468916",
                  "clobPairId": "0",
                  "side": "BUY",
                  "size": "0.3158",
                  "price": "63334",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "BTC-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "3b2839d5-ebd2-50cd-87ef-3bf8fb50dbe8",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "9468916",
                  "clobPairId": "0",
                  "side": "BUY",
                  "size": "0.3158",
                  "totalOptimisticFilled": "0",
                  "price": "63334",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "BTC-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "a44e6e09-02e0-590a-90a5-5157f7b16158",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785512",
                  "clobPairId": "2",
                  "side": "BUY",
                  "size": "68",
                  "totalOptimisticFilled": "0",
                  "price": "14.687",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972962",
                  "ticker": "LINK-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "0af38935-c391-536d-9ea0-04d967d77ba4",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "34352310",
                  "clobPairId": "30",
                  "side": "BUY",
                  "size": "82000000",
                  "totalOptimisticFilled": "0",
                  "price": "0.000024172",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972962",
                  "ticker": "SHIB-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "ce730a91-0a0b-54a5-8a34-48bae019b888",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63950081",
                  "clobPairId": "59",
                  "side": "SELL",
                  "size": "1459",
                  "totalOptimisticFilled": "0",
                  "price": "1.369",
                  "type": "LIMIT",
                  "status": "CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972962",
                  "ticker": "AEVO-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "305e4882-6cf7-59ad-9b2b-98eed89f1204",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63950090",
                  "clobPairId": "43",
                  "side": "SELL",
                  "size": "1482",
                  "price": "1.323",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "STRK-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "d19ccd28-24ae-5932-b491-2009b01c74fe",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "27352201",
                  "clobPairId": "46",
                  "side": "SELL",
                  "size": "1650",
                  "price": "0.5447",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "PYTH-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "d19ccd28-24ae-5932-b491-2009b01c74fe",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "27352201",
                  "clobPairId": "46",
                  "side": "SELL",
                  "size": "1650",
                  "totalOptimisticFilled": "0",
                  "price": "0.5447",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "PYTH-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "733b0dda-8562-5f39-b95a-957f522fbeaa",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "21204931",
                  "clobPairId": "26",
                  "side": "SELL",
                  "size": "510",
                  "price": "1.962",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "LDO-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "733b0dda-8562-5f39-b95a-957f522fbeaa",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "21204931",
                  "clobPairId": "26",
                  "side": "SELL",
                  "size": "510",
                  "totalOptimisticFilled": "0",
                  "price": "1.962",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "LDO-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "8f90bf46-db89-52d6-8637-0191018a7ac4",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24854750",
                  "clobPairId": "52",
                  "side": "SELL",
                  "size": "510",
                  "price": "2.208",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "IMX-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "4259115a-a115-5553-9480-989cfbb490f6",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63950091",
                  "clobPairId": "21",
                  "side": "BUY",
                  "size": "99",
                  "price": "6.157",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "WLD-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "4c83f8ab-4ab0-5d5b-b395-593407201f73",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24854749",
                  "clobPairId": "52",
                  "side": "BUY",
                  "size": "510",
                  "price": "2.205",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "IMX-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "7654b9fd-da00-5f5d-a4a7-cd9b13f2e8b4",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63950092",
                  "clobPairId": "35",
                  "side": "BUY",
                  "size": "1780",
                  "price": "1.117",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "JUP-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "4259115a-a115-5553-9480-989cfbb490f6",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63950091",
                  "clobPairId": "21",
                  "side": "BUY",
                  "size": "99",
                  "totalOptimisticFilled": "0",
                  "price": "6.157",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "WLD-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "22e411c4-3044-5d82-8152-abb2a8f832cb",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63950093",
                  "clobPairId": "35",
                  "side": "SELL",
                  "size": "1780",
                  "price": "1.1176",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "JUP-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "1eae447f-5534-55f2-80f3-6a055496d9b8",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "11332160",
                  "clobPairId": "12",
                  "side": "SELL",
                  "size": "277",
                  "price": "7.23",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "DOT-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "1eae447f-5534-55f2-80f3-6a055496d9b8",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "11332160",
                  "clobPairId": "12",
                  "side": "SELL",
                  "size": "277",
                  "totalOptimisticFilled": "0",
                  "price": "7.23",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "DOT-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "c1f1d894-542a-5280-b0bb-830a338ecded",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "1282928",
                  "clobPairId": "1",
                  "side": "BUY",
                  "size": "1.886",
                  "price": "3076.9",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972966",
                  "ticker": "ETH-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "daf960b0-cdbd-52da-93a6-9eaeeadf4d63",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24854751",
                  "clobPairId": "64",
                  "side": "BUY",
                  "size": "16220",
                  "price": "0.1231",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "CHZ-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "20674a72-a7ac-5702-aac2-fcf1e5f3aca1",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "1282929",
                  "clobPairId": "1",
                  "side": "SELL",
                  "size": "1.886",
                  "price": "3079.1",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972966",
                  "ticker": "ETH-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "5d973cf8-6feb-5d6e-8447-8acbac1c65e5",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24854752",
                  "clobPairId": "64",
                  "side": "SELL",
                  "size": "16220",
                  "price": "0.1233",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "CHZ-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "fccc826e-babe-5ab9-9639-b9ce34c5e4d0",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "35360321",
                  "clobPairId": "13",
                  "side": "SELL",
                  "size": "132",
                  "price": "7.557",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "UNI-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "3abe0a19-31a9-5845-b8fa-65705f362037",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785518",
                  "clobPairId": "16",
                  "side": "SELL",
                  "size": "42",
                  "price": "7.343",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "NEAR-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "fa664a22-4b59-588d-a547-a283a306d798",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785520",
                  "clobPairId": "24",
                  "side": "SELL",
                  "size": "1124",
                  "price": "1.07",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "ARB-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "fccc826e-babe-5ab9-9639-b9ce34c5e4d0",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "35360321",
                  "clobPairId": "13",
                  "side": "SELL",
                  "size": "132",
                  "totalOptimisticFilled": "0",
                  "price": "7.557",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "UNI-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "9273d939-739d-5fa2-8456-61978eb92a8a",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785521",
                  "clobPairId": "12",
                  "side": "SELL",
                  "size": "83",
                  "price": "7.23",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "DOT-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "a1c486ef-f6bc-5fda-900d-5473d9ec482b",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785519",
                  "clobPairId": "24",
                  "side": "BUY",
                  "size": "1124",
                  "price": "1.068",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "ARB-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "b9f3ff47-f659-5e9d-827e-856d09f456b5",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785522",
                  "clobPairId": "4",
                  "side": "BUY",
                  "size": "80",
                  "price": "0.4366",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "CRV-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "cd38954c-8739-5c0f-a0cb-cdcab2f32dfd",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "64054771",
                  "clobPairId": "38",
                  "side": "SELL",
                  "size": "674",
                  "price": "3.589",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972971",
                  "ticker": "JTO-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "39ffd7ad-b677-51de-b2db-e55b8a8f17c4",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785523",
                  "clobPairId": "4",
                  "side": "SELL",
                  "size": "80",
                  "price": "0.4389",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "CRV-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "b29a815d-c7f6-51ef-89db-a1e6c97e6c9e",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24854753",
                  "clobPairId": "51",
                  "side": "SELL",
                  "size": "63",
                  "price": "25.3",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "INJ-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "b57e6101-05f5-50e3-90c8-1995dfade54f",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "8430577",
                  "clobPairId": "47",
                  "side": "BUY",
                  "size": "79900000",
                  "price": "0.00002501",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "BONK-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "b57e6101-05f5-50e3-90c8-1995dfade54f",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "8430577",
                  "clobPairId": "47",
                  "side": "BUY",
                  "size": "79900000",
                  "totalOptimisticFilled": "0",
                  "price": "0.00002501",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972963",
                  "ticker": "BONK-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            }
          ]
        }
    """.trimIndent()
    internal val channel_batch_data_order_filled_2 = """
        {
          "type": "channel_batch_data",
          "connection_id": "a636f0fd-d238-46cd-a541-78cd747d0e09",
          "message_id": 63,
          "id": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3/0",
          "channel": "v4_subaccounts",
          "version": "2.4.0",
          "contents": [
            {
              "orders": [
                {
                  "id": "31e2aaee-e2e9-5b59-85b3-48d6ae99dcd9",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "23163849",
                  "clobPairId": "3",
                  "side": "SELL",
                  "size": "700",
                  "price": "0.7195",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "MATIC-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "31e2aaee-e2e9-5b59-85b3-48d6ae99dcd9",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "23163849",
                  "clobPairId": "3",
                  "side": "SELL",
                  "size": "700",
                  "totalOptimisticFilled": "0",
                  "price": "0.7195",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "MATIC-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "9191ea2d-110b-5e1c-bfc3-cbfef23c0983",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "5309308",
                  "clobPairId": "7",
                  "side": "SELL",
                  "size": "13.4",
                  "price": "37.3",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "AVAX-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "9191ea2d-110b-5e1c-bfc3-cbfef23c0983",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "5309308",
                  "clobPairId": "7",
                  "side": "SELL",
                  "size": "13.4",
                  "totalOptimisticFilled": "0",
                  "price": "37.3",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "AVAX-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "0c1136dc-97a0-55ef-af51-819c4d5e853f",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63950116",
                  "clobPairId": "33",
                  "side": "BUY",
                  "size": "28",
                  "price": "10.32",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "TIA-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "b00eeb26-3f91-5e49-8761-eb0c17f7758b",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24854775",
                  "clobPairId": "51",
                  "side": "BUY",
                  "size": "66.4",
                  "price": "25.26",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "INJ-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "acaa59e1-adb4-5b87-b579-7e4ca6ce908d",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "21204934",
                  "clobPairId": "26",
                  "side": "SELL",
                  "size": "510",
                  "price": "1.962",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "LDO-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "acaa59e1-adb4-5b87-b579-7e4ca6ce908d",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "21204934",
                  "clobPairId": "26",
                  "side": "SELL",
                  "size": "510",
                  "totalOptimisticFilled": "0",
                  "price": "1.962",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "LDO-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "48cd61a6-4f68-51ed-b49a-c8cd73848d1f",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "31284947",
                  "clobPairId": "43",
                  "side": "SELL",
                  "size": "1513",
                  "price": "1.323",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "STRK-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "026be2bc-5eb2-5870-bf70-d2499f356523",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "1282935",
                  "clobPairId": "1",
                  "side": "BUY",
                  "size": "2.806",
                  "price": "3075.6",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972966",
                  "ticker": "ETH-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "48cd61a6-4f68-51ed-b49a-c8cd73848d1f",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "31284947",
                  "clobPairId": "43",
                  "side": "SELL",
                  "size": "1513",
                  "totalOptimisticFilled": "0",
                  "price": "1.323",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "STRK-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "3884e597-6376-5ee8-a115-8e2507bd875c",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "1282934",
                  "clobPairId": "1",
                  "side": "BUY",
                  "size": "1.029",
                  "price": "3076.9",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "ETH-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "e75c65cf-1ba3-5e87-a554-b1d4dd751058",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "64054794",
                  "clobPairId": "23",
                  "side": "SELL",
                  "size": "200",
                  "price": "9.144",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972973",
                  "ticker": "APT-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "82c9154e-5773-5fde-a6ed-d6106d628f33",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "27352204",
                  "clobPairId": "46",
                  "side": "SELL",
                  "size": "1650",
                  "price": "0.5446",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "PYTH-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "82c9154e-5773-5fde-a6ed-d6106d628f33",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "27352204",
                  "clobPairId": "46",
                  "side": "SELL",
                  "size": "1650",
                  "totalOptimisticFilled": "0",
                  "price": "0.5446",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "PYTH-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "026be2bc-5eb2-5870-bf70-d2499f356523",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "1282935",
                  "clobPairId": "1",
                  "side": "BUY",
                  "size": "2.806",
                  "totalOptimisticFilled": "0",
                  "price": "3075.6",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972966",
                  "ticker": "ETH-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "7be42526-696e-5e81-9f63-786b4eebb01c",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "32397787",
                  "clobPairId": "31",
                  "side": "SELL",
                  "size": "910",
                  "price": "1.094",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "SUI-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "7be42526-696e-5e81-9f63-786b4eebb01c",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "32397787",
                  "clobPairId": "31",
                  "side": "SELL",
                  "size": "910",
                  "totalOptimisticFilled": "0",
                  "price": "1.094",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "SUI-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "aeb246ea-172f-5af8-acb3-b0826d8cdf99",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "20430991",
                  "clobPairId": "35",
                  "side": "SELL",
                  "size": "890",
                  "price": "1.1171",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "JUP-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "aeb246ea-172f-5af8-acb3-b0826d8cdf99",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "20430991",
                  "clobPairId": "35",
                  "side": "SELL",
                  "size": "890",
                  "totalOptimisticFilled": "0",
                  "price": "1.1171",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "JUP-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "aa670abc-7e36-5dee-afe1-bdeb104b6af1",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785563",
                  "clobPairId": "2",
                  "side": "BUY",
                  "size": "68",
                  "price": "14.685",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "LINK-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "1603a773-2395-54bd-9858-0e414cbc9989",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785562",
                  "clobPairId": "5",
                  "side": "SELL",
                  "size": "9.6",
                  "price": "152.85",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972971",
                  "ticker": "SOL-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "a003e35c-2006-5e7d-bf19-f074514b7fb3",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "14454043",
                  "clobPairId": "44",
                  "side": "SELL",
                  "size": "840",
                  "price": "2.3766",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "FET-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "a003e35c-2006-5e7d-bf19-f074514b7fb3",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "14454043",
                  "clobPairId": "44",
                  "side": "SELL",
                  "size": "840",
                  "totalOptimisticFilled": "0",
                  "price": "2.3766",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "FET-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "90bda234-3a22-5cc3-8c05-227f7da9160c",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785564",
                  "clobPairId": "2",
                  "side": "SELL",
                  "size": "68",
                  "price": "14.69",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "LINK-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "4ca89415-a434-5941-a68c-3a3c402062d5",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785565",
                  "clobPairId": "8",
                  "side": "BUY",
                  "size": "469",
                  "price": "6.14",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972966",
                  "ticker": "FIL-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "5c22e605-a31d-5803-ba82-e2ab9c1e5805",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24854776",
                  "clobPairId": "65",
                  "side": "BUY",
                  "size": "109",
                  "price": "3.374",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "WIF-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "072d5f15-e8c2-5b15-91ed-cbd363c11df1",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785566",
                  "clobPairId": "16",
                  "side": "BUY",
                  "size": "44",
                  "price": "7.325",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "NEAR-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "f90e114e-42c3-5c43-91fc-4bc71bd2a742",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "30906388",
                  "clobPairId": "5",
                  "side": "SELL",
                  "size": "32.7",
                  "price": "152.8",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "SOL-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "f90e114e-42c3-5c43-91fc-4bc71bd2a742",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "30906388",
                  "clobPairId": "5",
                  "side": "SELL",
                  "size": "32.7",
                  "totalOptimisticFilled": "0",
                  "price": "152.8",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "SOL-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "2b95adfa-5510-5247-a2ae-ee43b3d6a91d",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "11398529",
                  "clobPairId": "0",
                  "side": "SELL",
                  "size": "0.0364",
                  "price": "63353",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "BTC-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "4ccdd095-ead4-5b0c-869a-dbe4791f5215",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "11398528",
                  "clobPairId": "0",
                  "side": "BUY",
                  "size": "0.0364",
                  "price": "63326",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "BTC-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "bf87ee54-6658-5b30-a401-a95d24756e75",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24385164",
                  "clobPairId": "16",
                  "side": "SELL",
                  "size": "136",
                  "price": "7.334",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "NEAR-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "bf87ee54-6658-5b30-a401-a95d24756e75",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24385164",
                  "clobPairId": "16",
                  "side": "SELL",
                  "size": "136",
                  "totalOptimisticFilled": "0",
                  "price": "7.334",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "NEAR-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "072d5f15-e8c2-5b15-91ed-cbd363c11df1",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785566",
                  "clobPairId": "16",
                  "side": "BUY",
                  "size": "44",
                  "totalOptimisticFilled": "0",
                  "price": "7.325",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "NEAR-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "ffc5f43e-7c58-5d7d-8a29-046fae3e567e",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24854777",
                  "clobPairId": "47",
                  "side": "BUY",
                  "size": "68100000",
                  "price": "0.000025",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "BONK-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "87667ad6-d688-5b2b-85f5-691e18336659",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "10201624",
                  "clobPairId": "4",
                  "side": "BUY",
                  "size": "4570",
                  "price": "0.4376",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "CRV-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "4ca89415-a434-5941-a68c-3a3c402062d5",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785565",
                  "clobPairId": "8",
                  "side": "BUY",
                  "size": "469",
                  "totalOptimisticFilled": "0",
                  "price": "6.14",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972966",
                  "ticker": "FIL-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "02aaa7a3-d2f7-5662-a3f9-f57170fc06c4",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785551",
                  "clobPairId": "8",
                  "side": "BUY",
                  "size": "116",
                  "totalOptimisticFilled": "0",
                  "price": "6.142",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "FIL-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "7e7bb369-0dd8-5966-8c76-496fe73e3b2f",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "24854778",
                  "clobPairId": "47",
                  "side": "SELL",
                  "size": "68100000",
                  "price": "0.00002503",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "BONK-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "84a06dee-76c4-5016-a089-0cef146996cc",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "34352294",
                  "clobPairId": "44",
                  "side": "BUY",
                  "size": "2100",
                  "totalOptimisticFilled": "0",
                  "price": "2.3727",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972969",
                  "ticker": "FET-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "b46f4d57-d976-599b-ab39-9ee85123eee0",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "1282936",
                  "clobPairId": "1",
                  "side": "SELL",
                  "size": "1.034",
                  "price": "3078.4",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "ETH-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "4ff1e0df-963a-5c99-b9b3-1ad5c41ba03f",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "34352350",
                  "clobPairId": "44",
                  "side": "BUY",
                  "size": "2100",
                  "price": "2.3718",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972973",
                  "ticker": "FET-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "822f2c81-9912-503c-b2f9-2cb964a88b32",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "10201623",
                  "clobPairId": "4",
                  "side": "BUY",
                  "size": "4570",
                  "totalOptimisticFilled": "4570",
                  "price": "0.4376",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "CRV-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "1603a773-2395-54bd-9858-0e414cbc9989",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "47785562",
                  "clobPairId": "5",
                  "side": "SELL",
                  "size": "9.6",
                  "totalOptimisticFilled": "0",
                  "price": "152.85",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972971",
                  "ticker": "SOL-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "8ac1a6a2-db80-569f-89a5-38c1530fcb84",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "26450122",
                  "clobPairId": "39",
                  "side": "SELL",
                  "size": "25.8",
                  "price": "38.77",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "ORDI-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "923caeed-cc94-5528-b29e-2eae12f3b324",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "28331617",
                  "clobPairId": "29",
                  "side": "SELL",
                  "size": "2180",
                  "price": "0.5482",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "SEI-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "923caeed-cc94-5528-b29e-2eae12f3b324",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "28331617",
                  "clobPairId": "29",
                  "side": "SELL",
                  "size": "2180",
                  "totalOptimisticFilled": "0",
                  "price": "0.5482",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "SEI-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "9e5abbbf-2c06-5c20-8295-5f1f96924c74",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "4289210",
                  "clobPairId": "24",
                  "side": "SELL",
                  "size": "936",
                  "price": "1.069",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "ARB-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "9e5abbbf-2c06-5c20-8295-5f1f96924c74",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "4289210",
                  "clobPairId": "24",
                  "side": "SELL",
                  "size": "936",
                  "totalOptimisticFilled": "0",
                  "price": "1.069",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "ARB-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "e4dc295c-2204-5088-a826-b08b5c0c69e3",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "8430580",
                  "clobPairId": "47",
                  "side": "SELL",
                  "size": "79900000",
                  "price": "0.00002503",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "BONK-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "e4dc295c-2204-5088-a826-b08b5c0c69e3",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "8430580",
                  "clobPairId": "47",
                  "side": "SELL",
                  "size": "79900000",
                  "totalOptimisticFilled": "0",
                  "price": "0.00002503",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "BONK-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "50a1fecc-d2fc-5efe-bda9-164990325dac",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "64054796",
                  "clobPairId": "62",
                  "side": "SELL",
                  "size": "762",
                  "price": "2.625",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "ARKM-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "7d09e37c-3e50-5276-9922-c50bf268048f",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "64054795",
                  "clobPairId": "62",
                  "side": "BUY",
                  "size": "762",
                  "price": "2.624",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "ARKM-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "8ac1a6a2-db80-569f-89a5-38c1530fcb84",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "26450122",
                  "clobPairId": "39",
                  "side": "SELL",
                  "size": "25.8",
                  "totalOptimisticFilled": "0",
                  "price": "38.77",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "ORDI-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "d7ffbefb-0d6d-5de2-838d-39404adab13d",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "63950117",
                  "clobPairId": "21",
                  "side": "SELL",
                  "size": "257",
                  "price": "6.18",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "GTT",
                  "postOnly": true,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972973",
                  "ticker": "WLD-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "tradingReward": {
                "tradingReward": "0.120213656910332891",
                "createdAtHeight": "14972965",
                "createdAt": "2024-05-06T18:41:23.452Z"
              }
            },
            {
              "perpetualPositions": [
                {
                  "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
                  "subaccountNumber": 0,
                  "positionId": "b27fe5e5-9411-5a46-8ff4-82eff972a665",
                  "market": "CRV-USD",
                  "side": "SHORT",
                  "status": "OPEN",
                  "size": "-5180",
                  "maxSize": "-480",
                  "netFunding": "-0.004246",
                  "entryPrice": "0.43801138333224734478",
                  "exitPrice": "0.43662955658134004757",
                  "sumOpen": "153470",
                  "sumClose": "142980",
                  "realizedPnl": "197.5693428447253550858",
                  "unrealizedPnl": "1.1243429950412459604"
                }
              ],
              "assetPositions": [
                {
                  "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
                  "subaccountNumber": 0,
                  "positionId": "e326796e-660a-532b-958b-1b83e7e5f895",
                  "assetId": "0",
                  "symbol": "USDC",
                  "side": "LONG",
                  "size": "1595143.629373"
                }
              ]
            },
            {
              "perpetualPositions": [
                {
                  "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
                  "subaccountNumber": 0,
                  "positionId": "b27fe5e5-9411-5a46-8ff4-82eff972a665",
                  "market": "CRV-USD",
                  "side": "SHORT",
                  "status": "OPEN",
                  "size": "-1880",
                  "maxSize": "-480",
                  "netFunding": "-0.004246",
                  "entryPrice": "0.43801138333224734478",
                  "exitPrice": "0.43663633968804159446",
                  "sumOpen": "153470",
                  "sumClose": "144250",
                  "realizedPnl": "198.34579967667948366",
                  "unrealizedPnl": "0.4080627086250081864"
                }
              ],
              "assetPositions": [
                {
                  "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
                  "subaccountNumber": 0,
                  "positionId": "e326796e-660a-532b-958b-1b83e7e5f895",
                  "assetId": "0",
                  "symbol": "USDC",
                  "side": "LONG",
                  "size": "1593699.848518"
                }
              ]
            },
            {
              "fills": [
                {
                  "id": "d9a769be-0403-585a-a6b7-733aa5353811",
                  "fee": "0.138875",
                  "side": "BUY",
                  "size": "1270",
                  "type": "LIMIT",
                  "price": "0.4374",
                  "eventId": "00e47825000000020000000e",
                  "orderId": "87667ad6-d688-5b2b-85f5-691e18336659",
                  "createdAt": "2024-05-06T18:41:23.452Z",
                  "liquidity": "TAKER",
                  "clobPairId": "4",
                  "quoteAmount": "555.498",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientMetadata": "0",
                  "createdAtHeight": "14972965",
                  "transactionHash": "1F41C92B9FC15B94F7DC55A0D55EDBB59122D5B7D96CEB9924B9CE2EE2FE3D0F",
                  "ticker": "CRV-USD"
                },
                {
                  "id": "a15ab1f0-1d06-5f47-bad7-a048f5696c5f",
                  "fee": "0.360855",
                  "side": "BUY",
                  "size": "3300",
                  "type": "LIMIT",
                  "price": "0.4374",
                  "eventId": "00e478250000000200000011",
                  "orderId": "87667ad6-d688-5b2b-85f5-691e18336659",
                  "createdAt": "2024-05-06T18:41:23.452Z",
                  "liquidity": "TAKER",
                  "clobPairId": "4",
                  "quoteAmount": "1443.42",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientMetadata": "0",
                  "createdAtHeight": "14972965",
                  "transactionHash": "1F41C92B9FC15B94F7DC55A0D55EDBB59122D5B7D96CEB9924B9CE2EE2FE3D0F",
                  "ticker": "CRV-USD"
                }
              ],
              "perpetualPositions": [
                {
                  "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
                  "subaccountNumber": 0,
                  "positionId": "b27fe5e5-9411-5a46-8ff4-82eff972a665",
                  "market": "CRV-USD",
                  "side": "SHORT",
                  "status": "OPEN",
                  "size": "-1880",
                  "maxSize": "-480",
                  "netFunding": "-0.004246",
                  "entryPrice": "0.43801138333224734478",
                  "exitPrice": "0.43665341917993900373",
                  "sumOpen": "153470",
                  "sumClose": "147550"
                }
              ],
              "orders": [
                {
                  "id": "87667ad6-d688-5b2b-85f5-691e18336659",
                  "side": "BUY",
                  "size": "4570",
                  "type": "LIMIT",
                  "price": "0.4376",
                  "status": "FILLED",
                  "clientId": "10201624",
                  "updatedAt": "2024-05-06T18:41:23.452Z",
                  "clobPairId": "4",
                  "orderFlags": "0",
                  "reduceOnly": false,
                  "timeInForce": "IOC",
                  "totalFilled": "4570",
                  "goodTilBlock": "14972965",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "triggerPrice": null,
                  "clientMetadata": "0",
                  "createdAtHeight": "14972965",
                  "updatedAtHeight": "14972965",
                  "goodTilBlockTime": null,
                  "postOnly": false,
                  "ticker": "CRV-USD"
                }
              ]
            },
            {
              "perpetualPositions": [
                {
                  "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
                  "subaccountNumber": 0,
                  "positionId": "3316ca2e-baa4-594c-a353-900b8a139df2",
                  "market": "INJ-USD",
                  "side": "SHORT",
                  "status": "OPEN",
                  "size": "-957.3",
                  "maxSize": "-6.3",
                  "netFunding": "0.571584",
                  "entryPrice": "25.84818550028599549619",
                  "exitPrice": "25.84011068408081729257",
                  "sumOpen": "19405.9",
                  "sumClose": "18421.8",
                  "realizedPnl": "149.324233168551831446916",
                  "unrealizedPnl": "468.334432236783488502687"
                }
              ],
              "assetPositions": [
                {
                  "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
                  "subaccountNumber": 0,
                  "positionId": "e326796e-660a-532b-958b-1b83e7e5f895",
                  "assetId": "0",
                  "symbol": "USDC",
                  "side": "LONG",
                  "size": "1593826.216918"
                }
              ]
            },
            {
              "fills": [
                {
                  "id": "a39be804-d529-5e35-a585-24a15fc5ef13",
                  "fee": "0.0316",
                  "side": "SELL",
                  "size": "5",
                  "type": "LIMIT",
                  "price": "25.28",
                  "eventId": "00e478250000000200000020",
                  "orderId": "e068abcc-db2e-550f-a747-bbbd8465b813",
                  "createdAt": "2024-05-06T18:41:23.452Z",
                  "liquidity": "TAKER",
                  "clobPairId": "51",
                  "quoteAmount": "126.4",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientMetadata": "0",
                  "createdAtHeight": "14972965",
                  "transactionHash": "1F41C92B9FC15B94F7DC55A0D55EDBB59122D5B7D96CEB9924B9CE2EE2FE3D0F",
                  "ticker": "INJ-USD"
                }
              ],
              "perpetualPositions": [
                {
                  "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
                  "subaccountNumber": 0,
                  "positionId": "3316ca2e-baa4-594c-a353-900b8a139df2",
                  "market": "INJ-USD",
                  "side": "SHORT",
                  "status": "OPEN",
                  "size": "-957.3",
                  "maxSize": "-6.3",
                  "netFunding": "0.571584",
                  "entryPrice": "25.84803914295576196877",
                  "exitPrice": "25.84011068408081729257",
                  "sumOpen": "19410.9",
                  "sumClose": "18421.8"
                }
              ],
              "orders": [
                {
                  "id": "e068abcc-db2e-550f-a747-bbbd8465b813",
                  "side": "SELL",
                  "size": "39.6",
                  "type": "LIMIT",
                  "price": "25.28",
                  "status": "CANCELED",
                  "clientId": "18334330",
                  "updatedAt": "2024-05-06T18:41:23.452Z",
                  "clobPairId": "51",
                  "orderFlags": "0",
                  "reduceOnly": false,
                  "timeInForce": "IOC",
                  "totalFilled": "5",
                  "goodTilBlock": "14972965",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "triggerPrice": null,
                  "clientMetadata": "0",
                  "createdAtHeight": "14972965",
                  "updatedAtHeight": "14972965",
                  "goodTilBlockTime": null,
                  "postOnly": false,
                  "ticker": "INJ-USD"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "b3791a71-9578-5b2b-b763-88d0de37b112",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "16211736",
                  "clobPairId": "53",
                  "side": "SELL",
                  "size": "8750",
                  "price": "0.1143",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "HBAR-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "b3791a71-9578-5b2b-b763-88d0de37b112",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "16211736",
                  "clobPairId": "53",
                  "side": "SELL",
                  "size": "8750",
                  "totalOptimisticFilled": "0",
                  "price": "0.1143",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "HBAR-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "e068abcc-db2e-550f-a747-bbbd8465b813",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "18334330",
                  "clobPairId": "51",
                  "side": "SELL",
                  "size": "39.6",
                  "price": "25.28",
                  "status": "BEST_EFFORT_OPENED",
                  "type": "LIMIT",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "INJ-USD",
                  "clientMetadata": "0"
                }
              ]
            },
            {
              "orders": [
                {
                  "id": "e068abcc-db2e-550f-a747-bbbd8465b813",
                  "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                  "clientId": "18334330",
                  "clobPairId": "51",
                  "side": "SELL",
                  "size": "39.6",
                  "totalOptimisticFilled": "0",
                  "price": "25.28",
                  "type": "LIMIT",
                  "status": "BEST_EFFORT_CANCELED",
                  "timeInForce": "IOC",
                  "postOnly": false,
                  "reduceOnly": false,
                  "orderFlags": "0",
                  "goodTilBlock": "14972965",
                  "ticker": "INJ-USD",
                  "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                  "createdAtHeight": "14972965",
                  "updatedAt": "2024-05-06T18:41:23.452Z",
                  "updatedAtHeight": "14972965",
                  "clientMetadata": "0"
                }
              ]
            }
          ]
        }
    """.trimIndent()
    internal val channel_batch_data_order_filled_3 = """
        [
          {
            "orders": [
              {
                "id": "b6647251-0e89-587c-a621-a77381974cb7",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24854872",
                "clobPairId": "47",
                "side": "SELL",
                "size": "79900000",
                "price": "0.00002503",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "BONK-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "688da090-189a-5177-b83b-83dd312116f0",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785706",
                "clobPairId": "8",
                "side": "SELL",
                "size": "229",
                "totalOptimisticFilled": "0",
                "price": "6.147",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972974",
                "ticker": "FIL-USD",
                "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "f12d6138-1e9d-5bca-ace6-eeece9d4408c",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785713",
                "clobPairId": "8",
                "side": "BUY",
                "size": "230",
                "totalOptimisticFilled": "0",
                "price": "6.139",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972974",
                "ticker": "FIL-USD",
                "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "6f83d10e-5b0a-5f85-96a3-a4d66b6feb17",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "17448033",
                "clobPairId": "52",
                "side": "SELL",
                "size": "544",
                "price": "2.207",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "IMX-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "6f83d10e-5b0a-5f85-96a3-a4d66b6feb17",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "17448033",
                "clobPairId": "52",
                "side": "SELL",
                "size": "544",
                "totalOptimisticFilled": "0",
                "price": "2.207",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "IMX-USD",
                "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "af7294fe-e800-5429-b0d7-4a77d8701cb4",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34352434",
                "clobPairId": "30",
                "side": "SELL",
                "size": "82000000",
                "price": "0.000024177",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "SHIB-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "bf718c87-182d-515a-899f-c309504b1c9e",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24854873",
                "clobPairId": "13",
                "side": "BUY",
                "size": "1986",
                "price": "7.522",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972986",
                "ticker": "UNI-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "7eb2dc15-4479-51cd-b4b6-b0f8a25bea61",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "8430588",
                "clobPairId": "47",
                "side": "BUY",
                "size": "79900000",
                "price": "0.000025",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "BONK-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "7eb2dc15-4479-51cd-b4b6-b0f8a25bea61",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "8430588",
                "clobPairId": "47",
                "side": "BUY",
                "size": "79900000",
                "totalOptimisticFilled": "0",
                "price": "0.000025",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "BONK-USD",
                "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "6377f1bd-694a-53ee-bac3-9dd1bb314420",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "15324171",
                "clobPairId": "8",
                "side": "BUY",
                "size": "326",
                "price": "6.142",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "FIL-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "6377f1bd-694a-53ee-bac3-9dd1bb314420",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "15324171",
                "clobPairId": "8",
                "side": "BUY",
                "size": "326",
                "totalOptimisticFilled": "0",
                "price": "6.142",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "FIL-USD",
                "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "cda2d1c7-c6ee-5869-8976-340223112633",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785714",
                "clobPairId": "4",
                "side": "SELL",
                "size": "100",
                "price": "0.4383",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "CRV-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "afde3348-4f22-5ac3-a1d2-5216dde78d94",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34352433",
                "clobPairId": "61",
                "side": "BUY",
                "size": "1448",
                "price": "2.808",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972981",
                "ticker": "SNX-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "0944c9e5-2ee6-5c7c-af1f-b88cb138a8b9",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "23163854",
                "clobPairId": "3",
                "side": "SELL",
                "size": "700",
                "price": "0.7195",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "MATIC-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "0944c9e5-2ee6-5c7c-af1f-b88cb138a8b9",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "23163854",
                "clobPairId": "3",
                "side": "SELL",
                "size": "700",
                "totalOptimisticFilled": "0",
                "price": "0.7195",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "MATIC-USD",
                "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "b6e3d5b2-b5b3-5656-9489-df2975f7bfcb",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950194",
                "clobPairId": "35",
                "side": "BUY",
                "size": "1790",
                "price": "1.1164",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "JUP-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "3309bc27-8eee-5ff8-8b1c-5ee2192e3922",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785717",
                "clobPairId": "24",
                "side": "SELL",
                "size": "1124",
                "price": "1.069",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "ARB-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "b6e3d5b2-b5b3-5656-9489-df2975f7bfcb",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950194",
                "clobPairId": "35",
                "side": "BUY",
                "size": "1790",
                "totalOptimisticFilled": "0",
                "price": "1.1164",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "JUP-USD",
                "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "afc75964-29c6-5975-9a10-5eebef4e958a",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950181",
                "clobPairId": "35",
                "side": "SELL",
                "size": "4470",
                "totalOptimisticFilled": "0",
                "price": "1.1184",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972975",
                "ticker": "JUP-USD",
                "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "03aa66bf-9156-588f-84d1-db9bfe4f1411",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950195",
                "clobPairId": "35",
                "side": "SELL",
                "size": "1790",
                "price": "1.117",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "JUP-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "f1cb848a-6769-5a76-ab88-3fef5f8d23e8",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785715",
                "clobPairId": "53",
                "side": "SELL",
                "size": "1900",
                "price": "0.1144",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "HBAR-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "4f353258-a1fa-5edc-b347-f18145775014",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785716",
                "clobPairId": "24",
                "side": "BUY",
                "size": "1124",
                "price": "1.068",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "ARB-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "d980e4ce-9d58-5cec-b1fb-1d292a50c866",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "20430997",
                "clobPairId": "35",
                "side": "SELL",
                "size": "890",
                "price": "1.1169",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "JUP-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "698797b9-2237-5b75-8f12-3e99c18e54b2",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785718",
                "clobPairId": "12",
                "side": "BUY",
                "size": "83",
                "price": "7.21",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "DOT-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "1e9e99d1-1f3c-5de9-adea-23183cc5dbd9",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950180",
                "clobPairId": "35",
                "side": "BUY",
                "size": "4470",
                "totalOptimisticFilled": "0",
                "price": "1.1152",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972975",
                "ticker": "JUP-USD",
                "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "03aa66bf-9156-588f-84d1-db9bfe4f1411",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950195",
                "clobPairId": "35",
                "side": "SELL",
                "size": "1790",
                "totalOptimisticFilled": "0",
                "price": "1.117",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "JUP-USD",
                "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "d980e4ce-9d58-5cec-b1fb-1d292a50c866",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "20430997",
                "clobPairId": "35",
                "side": "SELL",
                "size": "890",
                "totalOptimisticFilled": "0",
                "price": "1.1169",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "JUP-USD",
                "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "40e55f38-9e5c-5635-83b8-b00698c73f9a",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "11398549",
                "clobPairId": "0",
                "side": "SELL",
                "size": "0.1031",
                "totalOptimisticFilled": "0",
                "price": "63375",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "BTC-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "1cc31c00-afeb-5eba-b43f-df476cd6f221",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785705",
                "clobPairId": "22",
                "side": "SELL",
                "size": "943",
                "totalOptimisticFilled": "0",
                "price": "1.274",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "APE-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "5f7db51e-88ec-5559-872c-0c24c82c8444",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24854860",
                "clobPairId": "47",
                "side": "BUY",
                "size": "78400000",
                "totalOptimisticFilled": "0",
                "price": "0.000025",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "BONK-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "a066a133-a779-51ec-8465-a2b39df72512",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785694",
                "clobPairId": "27",
                "side": "SELL",
                "size": "214",
                "totalOptimisticFilled": "0",
                "price": "2.81",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "OP-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "389d3960-181c-5e79-9971-4dccfe5ea6e8",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34352426",
                "clobPairId": "15",
                "side": "BUY",
                "size": "16800",
                "totalOptimisticFilled": "0",
                "price": "0.119",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "TRX-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "00774af6-2c75-56a1-933a-c87b4fa91b45",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950186",
                "clobPairId": "21",
                "side": "SELL",
                "size": "118",
                "totalOptimisticFilled": "0",
                "price": "6.172",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "WLD-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "cf69b472-cdac-53ad-8bc8-98a41ab5ce20",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950103",
                "clobPairId": "33",
                "side": "SELL",
                "size": "207",
                "totalOptimisticFilled": "0",
                "price": "10.38",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "TIA-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "8735b996-0f90-5ec6-9961-cb585f49cf91",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950191",
                "clobPairId": "43",
                "side": "SELL",
                "size": "1514",
                "totalOptimisticFilled": "0",
                "price": "1.322",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "STRK-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "46de9008-4015-54d9-9635-ad3b2b985731",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950185",
                "clobPairId": "21",
                "side": "BUY",
                "size": "118",
                "totalOptimisticFilled": "0",
                "price": "6.156",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "WLD-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "0aab35dd-ba6d-5137-bb95-79fa05121301",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950159",
                "clobPairId": "33",
                "side": "BUY",
                "size": "77",
                "totalOptimisticFilled": "0",
                "price": "10.3",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "TIA-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "f56aaaec-1f20-52ef-8959-10ca700ef6a3",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64054862",
                "clobPairId": "31",
                "side": "SELL",
                "size": "1820",
                "totalOptimisticFilled": "0",
                "price": "1.0941",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "SUI-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "38a20fdf-fb9a-582b-af9b-fa8e53c5e720",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785698",
                "clobPairId": "8",
                "side": "SELL",
                "size": "57",
                "totalOptimisticFilled": "0",
                "price": "6.146",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "FIL-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "7960f213-2571-54a9-8af6-69dbd9f71211",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64054864",
                "clobPairId": "29",
                "side": "SELL",
                "size": "2690",
                "totalOptimisticFilled": "0",
                "price": "0.5478",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "SEI-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "fc93c0a0-07b0-559b-8313-183eca8cd08d",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64054861",
                "clobPairId": "31",
                "side": "BUY",
                "size": "1820",
                "totalOptimisticFilled": "0",
                "price": "1.0938",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "SUI-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "5308c450-a6b5-58d9-aa4e-75ce09ec743a",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34352424",
                "clobPairId": "61",
                "side": "BUY",
                "size": "568",
                "totalOptimisticFilled": "0",
                "price": "2.811",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "SNX-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "999a4a4b-40b5-5cad-80cb-8c71ba9adccf",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34352335",
                "clobPairId": "15",
                "side": "BUY",
                "size": "126000",
                "totalOptimisticFilled": "0",
                "price": "0.11853",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "TRX-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "e148d9df-c458-579e-99e4-dea13441bdcc",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785719",
                "clobPairId": "12",
                "side": "SELL",
                "size": "83",
                "price": "7.23",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "DOT-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "020b69b8-7d6c-5327-978d-5e0bb43849ae",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785693",
                "clobPairId": "27",
                "side": "BUY",
                "size": "214",
                "totalOptimisticFilled": "0",
                "price": "2.805",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "OP-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "0f79f409-0be1-5802-9a49-9153ad3e2055",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950190",
                "clobPairId": "43",
                "side": "BUY",
                "size": "1514",
                "totalOptimisticFilled": "0",
                "price": "1.321",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "STRK-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "91b22b65-0bf3-54ed-9da9-f4670acb1a02",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24854865",
                "clobPairId": "13",
                "side": "BUY",
                "size": "264",
                "totalOptimisticFilled": "0",
                "price": "7.551",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "UNI-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "353671c7-457c-55b0-badc-74fb67311afe",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785696",
                "clobPairId": "5",
                "side": "SELL",
                "size": "2.2",
                "totalOptimisticFilled": "0",
                "price": "152.78",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "SOL-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "4eb78ead-9238-558a-ba76-514574d8c7b7",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785695",
                "clobPairId": "5",
                "side": "BUY",
                "size": "2.2",
                "totalOptimisticFilled": "0",
                "price": "152.55",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "SOL-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "9cf15f8f-75eb-547c-8977-3506cc86c4d5",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785703",
                "clobPairId": "32",
                "side": "SELL",
                "size": "310",
                "totalOptimisticFilled": "0",
                "price": "0.5469",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "XRP-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "98967f6f-31fa-5410-b42d-f8cf768f46fd",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785704",
                "clobPairId": "3",
                "side": "SELL",
                "size": "690",
                "totalOptimisticFilled": "0",
                "price": "0.7195",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "MATIC-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "441af756-4863-5f19-9229-061da9c4ec82",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785697",
                "clobPairId": "8",
                "side": "BUY",
                "size": "57",
                "totalOptimisticFilled": "0",
                "price": "6.14",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "FIL-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "a9204db3-9d3e-5e76-b7fa-838535630a03",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24854866",
                "clobPairId": "13",
                "side": "SELL",
                "size": "264",
                "totalOptimisticFilled": "0",
                "price": "7.555",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "UNI-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "4fb9552d-6f57-5a15-861e-295e3b436cfb",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64054868",
                "clobPairId": "23",
                "side": "SELL",
                "size": "101",
                "totalOptimisticFilled": "0",
                "price": "9.125",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "APT-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "c9161c6e-5500-5613-bb64-ab63044faaaa",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34352430",
                "clobPairId": "30",
                "side": "BUY",
                "size": "82000000",
                "totalOptimisticFilled": "0",
                "price": "0.000024166",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "SHIB-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "eba636b1-4a55-5ac7-b6db-df71b0779786",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24854863",
                "clobPairId": "39",
                "side": "BUY",
                "size": "51.5",
                "totalOptimisticFilled": "0",
                "price": "38.74",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "ORDI-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "5169019f-02e4-5f27-9208-3b877c13780a",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24854694",
                "clobPairId": "52",
                "side": "SELL",
                "size": "3525",
                "totalOptimisticFilled": "0",
                "price": "2.218",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "IMX-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "2628a9cf-dbb1-5066-9903-2c0b9e81daee",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "11398548",
                "clobPairId": "0",
                "side": "BUY",
                "size": "0.1031",
                "totalOptimisticFilled": "0",
                "price": "63292",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "BTC-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "createdAtHeight": "4366782",
                "updatedAt": "2023-12-26T15:37:48.178Z",
                "updatedAtHeight": "4366782",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "14c4e01e-2dec-59eb-9ecf-f35064fd8caa",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785699",
                "clobPairId": "26",
                "side": "SELL",
                "size": "306",
                "totalOptimisticFilled": "0",
                "price": "1.962",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "LDO-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "e0a6c2e6-0db2-586e-82ff-13221abd639b",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34352425",
                "clobPairId": "61",
                "side": "SELL",
                "size": "568",
                "totalOptimisticFilled": "0",
                "price": "2.813",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "SNX-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "96f257c8-247c-544b-82b6-b7ac03ff4db7",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "25446576",
                "clobPairId": "27",
                "side": "BUY",
                "size": "1425",
                "price": "2.806",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "OP-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "96f257c8-247c-544b-82b6-b7ac03ff4db7",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "25446576",
                "clobPairId": "27",
                "side": "BUY",
                "size": "1425",
                "totalOptimisticFilled": "0",
                "price": "2.806",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "OP-USD",
                "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "tradingReward": {
              "tradingReward": "0.009290434492556074",
              "createdAtHeight": "14972972",
              "createdAt": "2024-05-06T18:41:30.747Z"
            }
          },
          {
            "perpetualPositions": [
              {
                "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
                "subaccountNumber": 0,
                "positionId": "3316ca2e-baa4-594c-a353-900b8a139df2",
                "market": "INJ-USD",
                "side": "SHORT",
                "status": "OPEN",
                "size": "-950.8",
                "maxSize": "-6.3",
                "netFunding": "0.571584",
                "entryPrice": "25.84803914295576196877",
                "exitPrice": "25.84011068408081729257",
                "sumOpen": "19410.9",
                "sumClose": "18421.8",
                "realizedPnl": "146.62806770245583602116",
                "unrealizedPnl": "465.015317670338479906516"
              }
            ],
            "assetPositions": [
              {
                "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
                "subaccountNumber": 0,
                "positionId": "e326796e-660a-532b-958b-1b83e7e5f895",
                "assetId": "0",
                "symbol": "USDC",
                "side": "LONG",
                "size": "1594658.823566"
              }
            ]
          },
          {
            "fills": [
              {
                "id": "e112f5c2-b77d-5c63-845b-cbc65a2f7c37",
                "fee": "0.041064",
                "side": "BUY",
                "size": "6.5",
                "type": "LIMIT",
                "price": "25.27",
                "eventId": "00e4782c0000000200000005",
                "orderId": "c58b5007-374f-544d-b99e-6a6b0f775cc7",
                "createdAt": "2024-05-06T18:41:30.747Z",
                "liquidity": "TAKER",
                "clobPairId": "51",
                "quoteAmount": "164.255",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientMetadata": "0",
                "createdAtHeight": "14972972",
                "transactionHash": "C4728A71264AEFA06E08AC3FA799ACB85B53916A97787F639BDB51C538D2FE27",
                "ticker": "INJ-USD"
              }
            ],
            "perpetualPositions": [
              {
                "address": "dydx18p7nz5rqezkyscdz9pv9rchnsesjyjjyfe92t3",
                "subaccountNumber": 0,
                "positionId": "3316ca2e-baa4-594c-a353-900b8a139df2",
                "market": "INJ-USD",
                "side": "SHORT",
                "status": "OPEN",
                "size": "-950.8",
                "maxSize": "-6.3",
                "netFunding": "0.571584",
                "entryPrice": "25.84803914295576196877",
                "exitPrice": "25.83990959556768665586",
                "sumOpen": "19410.9",
                "sumClose": "18428.3"
              }
            ],
            "orders": [
              {
                "id": "c58b5007-374f-544d-b99e-6a6b0f775cc7",
                "side": "BUY",
                "size": "39.6",
                "type": "LIMIT",
                "price": "25.27",
                "status": "CANCELED",
                "clientId": "18334336",
                "updatedAt": "2024-05-06T18:41:30.747Z",
                "clobPairId": "51",
                "orderFlags": "0",
                "reduceOnly": false,
                "timeInForce": "IOC",
                "totalFilled": "6.5",
                "goodTilBlock": "14972972",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "triggerPrice": null,
                "clientMetadata": "0",
                "createdAtHeight": "14972972",
                "updatedAtHeight": "14972972",
                "goodTilBlockTime": null,
                "postOnly": false,
                "ticker": "INJ-USD"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "defe1e7d-778c-5911-8138-93330fea79b2",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950188",
                "clobPairId": "33",
                "side": "SELL",
                "size": "32",
                "totalOptimisticFilled": "0",
                "price": "10.34",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "TIA-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "c5133b63-8c26-5140-96e3-ee5880af61c4",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785702",
                "clobPairId": "32",
                "side": "BUY",
                "size": "310",
                "totalOptimisticFilled": "0",
                "price": "0.5462",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "XRP-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "6334eebc-7c64-59f3-acfb-fe351bffc27e",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64054870",
                "clobPairId": "25",
                "side": "BUY",
                "size": "4990",
                "price": "0.4002",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "BLUR-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "3bf888e4-3328-5eb1-819a-7e3355a958de",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34352435",
                "clobPairId": "15",
                "side": "SELL",
                "size": "16800",
                "price": "0.11905",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "TRX-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "61d6fb1f-e25a-54ca-8e58-60f08a86d6d0",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64054860",
                "clobPairId": "62",
                "side": "SELL",
                "size": "763",
                "totalOptimisticFilled": "0",
                "price": "2.624",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "ARKM-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "b2234066-d546-5814-8da8-7a2b6a328b14",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24854874",
                "clobPairId": "65",
                "side": "BUY",
                "size": "128",
                "price": "3.372",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "WIF-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "a1752004-a36a-599f-8b02-8cd958396bc6",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34352336",
                "clobPairId": "15",
                "side": "SELL",
                "size": "126000",
                "totalOptimisticFilled": "0",
                "price": "0.11953",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "TRX-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "b3468577-6787-5c08-b1be-3b60e2da4762",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950189",
                "clobPairId": "59",
                "side": "SELL",
                "size": "1459",
                "totalOptimisticFilled": "0",
                "price": "1.369",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "AEVO-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "05929b2c-ae2d-5f97-9ff3-0e5329e0217e",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64054867",
                "clobPairId": "23",
                "side": "BUY",
                "size": "101",
                "totalOptimisticFilled": "0",
                "price": "9.12",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "APT-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "46f47606-edab-5a43-b99e-5090a72b6dc7",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950187",
                "clobPairId": "33",
                "side": "BUY",
                "size": "32",
                "totalOptimisticFilled": "0",
                "price": "10.32",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "TIA-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "b791b9af-72e2-545e-a9da-26044d42931e",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24854864",
                "clobPairId": "39",
                "side": "SELL",
                "size": "51.5",
                "totalOptimisticFilled": "0",
                "price": "38.77",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "ORDI-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "8b093d3b-12f5-5089-82ab-261329a6b907",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64054866",
                "clobPairId": "38",
                "side": "SELL",
                "size": "325",
                "totalOptimisticFilled": "0",
                "price": "3.584",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "JTO-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "65b1636c-0884-5f6c-897f-3365c116e05e",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64054865",
                "clobPairId": "38",
                "side": "BUY",
                "size": "325",
                "totalOptimisticFilled": "0",
                "price": "3.581",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "JTO-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "f371a841-06ce-5c3c-b40c-39d12a20e0e6",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950160",
                "clobPairId": "33",
                "side": "SELL",
                "size": "77",
                "totalOptimisticFilled": "0",
                "price": "10.35",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "TIA-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "6c300110-8218-51ab-a8e9-b1d7c00d9e28",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "28331625",
                "clobPairId": "29",
                "side": "SELL",
                "size": "2190",
                "price": "0.5476",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "SEI-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "6c300110-8218-51ab-a8e9-b1d7c00d9e28",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "28331625",
                "clobPairId": "29",
                "side": "SELL",
                "size": "2190",
                "totalOptimisticFilled": "0",
                "price": "0.5476",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "SEI-USD",
                "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "732f5218-1caa-5128-8b6b-d03212a7b194",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785701",
                "clobPairId": "10",
                "side": "SELL",
                "size": "6400",
                "totalOptimisticFilled": "0",
                "price": "0.15617",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "DOGE-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "1276c028-1ffb-58a6-834e-6733522f8c45",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24854875",
                "clobPairId": "47",
                "side": "BUY",
                "size": "79900000",
                "price": "0.000025",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "BONK-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "86c19d84-4a4c-584c-b3b3-c314daf8ed2c",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64054859",
                "clobPairId": "62",
                "side": "BUY",
                "size": "763",
                "totalOptimisticFilled": "0",
                "price": "2.622",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "ARKM-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "4239b51a-9302-5e93-a6a0-ea3f6bd9259f",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785700",
                "clobPairId": "10",
                "side": "BUY",
                "size": "6400",
                "totalOptimisticFilled": "0",
                "price": "0.15611",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "DOGE-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "ac1a1e3b-09d8-5ea1-a317-3804f2291d06",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950196",
                "clobPairId": "21",
                "side": "BUY",
                "size": "121",
                "price": "6.156",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "WLD-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "7c8463b1-012c-56fa-8068-18922ee8849e",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34352437",
                "clobPairId": "46",
                "side": "SELL",
                "size": "3670",
                "price": "0.5445",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "PYTH-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "1a482dbb-d61a-5051-b65c-fe049d538d4a",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950197",
                "clobPairId": "21",
                "side": "SELL",
                "size": "121",
                "price": "6.172",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "WLD-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "db79eadf-b326-5837-aa6d-1dcdaffbb12a",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "26450128",
                "clobPairId": "39",
                "side": "SELL",
                "size": "25.8",
                "price": "38.76",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "ORDI-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "db79eadf-b326-5837-aa6d-1dcdaffbb12a",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "26450128",
                "clobPairId": "39",
                "side": "SELL",
                "size": "25.8",
                "totalOptimisticFilled": "0",
                "price": "38.76",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "ORDI-USD",
                "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "a3ada32e-7f25-5a5d-9fa5-788e3a57fbbb",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "36474019",
                "clobPairId": "21",
                "side": "BUY",
                "size": "162",
                "price": "6.16",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "WLD-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "a3ada32e-7f25-5a5d-9fa5-788e3a57fbbb",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "36474019",
                "clobPairId": "21",
                "side": "BUY",
                "size": "162",
                "totalOptimisticFilled": "0",
                "price": "6.16",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "WLD-USD",
                "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "0e91f4b2-9385-57ef-a5c9-393ae1b9bb73",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "21204937",
                "clobPairId": "26",
                "side": "SELL",
                "size": "511",
                "price": "1.962",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "LDO-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "0e91f4b2-9385-57ef-a5c9-393ae1b9bb73",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "21204937",
                "clobPairId": "26",
                "side": "SELL",
                "size": "511",
                "totalOptimisticFilled": "0",
                "price": "1.962",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "LDO-USD",
                "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "8dbab4e8-169a-5139-9f78-b4299171ebe7",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64054863",
                "clobPairId": "29",
                "side": "BUY",
                "size": "2690",
                "totalOptimisticFilled": "0",
                "price": "0.5469",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "SEI-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "e2ba057f-b9f1-538e-badc-757740f275b6",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64054782",
                "clobPairId": "25",
                "side": "SELL",
                "size": "12480",
                "totalOptimisticFilled": "0",
                "price": "0.4014",
                "type": "LIMIT",
                "status": "CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972972",
                "ticker": "BLUR-USD",
                "removalReason": "ORDER_REMOVAL_REASON_EXPIRED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "e6461978-3982-523a-83ef-24753bdd7ced",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34352438",
                "clobPairId": "42",
                "side": "BUY",
                "size": "614",
                "price": "3.256",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "DYM-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "9b08334f-eee8-5828-98f0-6d309cbf6372",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "3344908",
                "clobPairId": "23",
                "side": "SELL",
                "size": "219",
                "price": "9.125",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "APT-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "3be51e79-6230-56b2-8231-5ed58709c296",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24854871",
                "clobPairId": "51",
                "side": "SELL",
                "size": "23.5",
                "totalOptimisticFilled": "0",
                "price": "25.3",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "INJ-USD",
                "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "bc33fcf2-05a2-50bf-bef6-c2fcc8865b82",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "34352436",
                "clobPairId": "46",
                "side": "BUY",
                "size": "3670",
                "price": "0.5442",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "PYTH-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "fe444731-cfc3-5186-ae0e-fba2e4174d89",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "7248890",
                "clobPairId": "37",
                "side": "SELL",
                "size": "5.09",
                "price": "589.1",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "BNB-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "fe444731-cfc3-5186-ae0e-fba2e4174d89",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "7248890",
                "clobPairId": "37",
                "side": "SELL",
                "size": "5.09",
                "totalOptimisticFilled": "0",
                "price": "589.1",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "BNB-USD",
                "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "6b64ef8e-a07b-5a0d-b5a0-cb9a01ae9103",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "64054871",
                "clobPairId": "25",
                "side": "SELL",
                "size": "4990",
                "price": "0.4007",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "BLUR-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "ee422fd6-6f81-5917-be34-cd03ae5933e4",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950200",
                "clobPairId": "33",
                "side": "SELL",
                "size": "33",
                "price": "10.34",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "TIA-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "97f0f697-b459-5f9c-9463-f10701c6f5dc",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785722",
                "clobPairId": "5",
                "side": "BUY",
                "size": "2.2",
                "price": "152.54",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "SOL-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "44e7c2bc-c2f0-55b7-90cf-2fdab8ef2723",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "47785720",
                "clobPairId": "27",
                "side": "BUY",
                "size": "214",
                "price": "2.805",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "OP-USD",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "9b08334f-eee8-5828-98f0-6d309cbf6372",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "3344908",
                "clobPairId": "23",
                "side": "SELL",
                "size": "219",
                "totalOptimisticFilled": "0",
                "price": "9.125",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "IOC",
                "postOnly": false,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "APT-USD",
                "removalReason": "ORDER_REMOVAL_REASON_IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "ac1a1e3b-09d8-5ea1-a317-3804f2291d06",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950196",
                "clobPairId": "21",
                "side": "BUY",
                "size": "121",
                "totalOptimisticFilled": "0",
                "price": "6.156",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "WLD-USD",
                "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "1a482dbb-d61a-5051-b65c-fe049d538d4a",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "63950197",
                "clobPairId": "21",
                "side": "SELL",
                "size": "121",
                "totalOptimisticFilled": "0",
                "price": "6.172",
                "type": "LIMIT",
                "status": "BEST_EFFORT_CANCELED",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972973",
                "ticker": "WLD-USD",
                "removalReason": "ORDER_REMOVAL_REASON_USER_CANCELED",
                "clientMetadata": "0"
              }
            ]
          },
          {
            "orders": [
              {
                "id": "2691b03c-b75a-5508-954e-43b9974eaeaa",
                "subaccountId": "174aff47-4e1d-5979-b5ba-f36d393519bc",
                "clientId": "24854877",
                "clobPairId": "51",
                "side": "SELL",
                "size": "59.4",
                "price": "25.33",
                "status": "BEST_EFFORT_OPENED",
                "type": "LIMIT",
                "timeInForce": "GTT",
                "postOnly": true,
                "reduceOnly": false,
                "orderFlags": "0",
                "goodTilBlock": "14972981",
                "ticker": "INJ-USD",
                "clientMetadata": "0"
              }
            ]
          }
        ]
    """.trimIndent()
}
