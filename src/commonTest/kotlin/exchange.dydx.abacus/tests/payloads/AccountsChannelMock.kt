package exchange.dydx.abacus.tests.payloads

import kollections.JsExport
import kotlinx.serialization.Serializable

@Suppress("PropertyName")
@JsExport
@Serializable
internal class AccountsChannelMock {
    internal val subscribed = """
        {
          "type": "subscribed",
          "connection_id": "c49e6d6b-d489-4140-bbaf-dc11b2fab13a",
          "message_id": 1,
          "channel": "v3_accounts",
          "id": "dace1648-c854-5aed-9879-88899bf647a3",
          "contents": {
            "orders": [
              {
                "id": "3c5193d7a49805ffcf231af1ed446188f04aaa6756bf9df7b5913568b2763d7",
                "clientId": "69967309621008383",
                "market": "ETH-USD",
                "accountId": "dace1648-c854-5aed-9879-88899bf647a3",
                "side": "BUY",
                "size": "0.1",
                "remainingSize": "0.1",
                "limitFee": "0.002",
                "price": "1500",
                "triggerPrice": null,
                "trailingPercent": null,
                "type": "LIMIT",
                "status": "OPEN",
                "signature": "06f422ea494514293c6da82b70aca83f30718a01beb942f3e877a3ce8411d8f700d227caf5a57357df3dd66b38e2faff07147f29db539696e7d4799f32063172",
                "timeInForce": "GTT",
                "postOnly": false,
                "cancelReason": null,
                "expiresAt": "2022-08-29T22:45:30.776Z",
                "unfillableAt": null,
                "updatedAt": "2022-08-01T22:25:31.139Z",
                "createdAt": "2022-08-01T22:25:31.111Z",
                "reduceOnly": false,
                "country": "JP",
                "client": "01",
                "reduceOnlySize": null
              }
            ],
            "account": {
              "starkKey": "01de24f8468f0590b80c12c87ae9e247ef3278d4683d875296051495b2ad0100",
              "positionId": "30915",
              "equity": "205935.352966",
              "freeCollateral": "187233.155294",
              "pendingDeposits": "0.000000",
              "pendingWithdrawals": "0.000000",
              "openPositions": {
                "ETH-USD": {
                  "market": "ETH-USD",
                  "status": "OPEN",
                  "side": "LONG",
                  "size": "93.57",
                  "maxSize": "100",
                  "entryPrice": "1091.812076",
                  "exitPrice": "1091.236219",
                  "unrealizedPnl": "61455.547636",
                  "realizedPnl": "-4173.521266",
                  "createdAt": "2022-06-30T01:01:10.234Z",
                  "closedAt": null,
                  "sumOpen": "218.92",
                  "sumClose": "125.35",
                  "netFunding": "-4101.337527"
                },
                "LINK-USD": {
                  "market": "LINK-USD",
                  "status": "OPEN",
                  "side": "SHORT",
                  "size": "-11",
                  "maxSize": "-11",
                  "entryPrice": "7.175000",
                  "exitPrice": "0.000000",
                  "unrealizedPnl": "-9.916131",
                  "realizedPnl": "2.022104",
                  "createdAt": "2022-07-20T18:24:29.570Z",
                  "closedAt": null,
                  "sumOpen": "11",
                  "sumClose": "0",
                  "netFunding": "2.022104"
                },
                "UNI-USD": {
                  "market": "UNI-USD",
                  "status": "OPEN",
                  "side": "LONG",
                  "size": "11548.4",
                  "maxSize": "11548.4",
                  "entryPrice": "7.065650",
                  "exitPrice": "0.000000",
                  "unrealizedPnl": "23552.454293",
                  "realizedPnl": "142.629215",
                  "createdAt": "2022-07-18T20:37:23.893Z",
                  "closedAt": null,
                  "sumOpen": "11548.4",
                  "sumClose": "0",
                  "netFunding": "142.629215"
                },
                "SUSHI-USD": {
                  "market": "SUSHI-USD",
                  "status": "OPEN",
                  "side": "LONG",
                  "size": "12",
                  "maxSize": "12",
                  "entryPrice": "1.464000",
                  "exitPrice": "0.000000",
                  "unrealizedPnl": "0.729203",
                  "realizedPnl": "0.271316",
                  "createdAt": "2022-07-18T20:36:17.165Z",
                  "closedAt": null,
                  "sumOpen": "12",
                  "sumClose": "0",
                  "netFunding": "0.271316"
                }
              },
              "accountNumber": "0",
              "id": "dace1648-c854-5aed-9879-88899bf647a3",
              "quoteBalance": "-62697.279528",
              "createdAt": "2021-04-20T18:27:38.698Z"
            },
            "transfers": {
                "id":"89586775-0646-582e-9b36-4f131715644d",
                "sender": {
                    "address":"dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg",
                    "subaccountNumber":0
                },
                "recipient":{
                    "address":"dydx1vvjr376v4hfpy5r6m3dmu4u3mu6yl6sjds3gz8"
                 },
                "size":"419.98472",
                "createdAt":"2023-08-21T21:37:53.373Z",
                "createdAtHeight":"404014",
                "symbol":"USDC",
                "type":"WITHDRAWAL",
                "transactionHash": "MOCKHASH"
            },
            "fundingPayments": [
              {
                "market": "SUSHI-USD",
                "payment": "-0.000142",
                "rate": "0.0000077675",
                "positionSize": "12",
                "price": "1.524267",
                "effectiveAt": "2022-07-29T23:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-0.446935",
                "rate": "0.0000042515",
                "positionSize": "11548.4",
                "price": "9.103159",
                "effectiveAt": "2022-07-29T23:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.002256",
                "rate": "0.0000253564",
                "positionSize": "-11",
                "price": "8.089071",
                "effectiveAt": "2022-07-29T23:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "1.058601",
                "rate": "-0.0000064931",
                "positionSize": "93.57",
                "price": "1742.397394",
                "effectiveAt": "2022-07-29T23:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.001312",
                "rate": "0.0000724070",
                "positionSize": "12",
                "price": "1.510470",
                "effectiveAt": "2022-07-29T22:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-0.638971",
                "rate": "0.0000061084",
                "positionSize": "11548.4",
                "price": "9.058049",
                "effectiveAt": "2022-07-29T22:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.00249",
                "rate": "0.0000285585",
                "positionSize": "-11",
                "price": "7.928630",
                "effectiveAt": "2022-07-29T22:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "0.277291",
                "rate": "-0.0000017273",
                "positionSize": "93.57",
                "price": "1715.680000",
                "effectiveAt": "2022-07-29T22:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "0.000632",
                "rate": "-0.0000347599",
                "positionSize": "12",
                "price": "1.515967",
                "effectiveAt": "2022-07-29T21:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "1.545884",
                "rate": "-0.0000148324",
                "positionSize": "11548.4",
                "price": "9.025000",
                "effectiveAt": "2022-07-29T21:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "-0.00045",
                "rate": "-0.0000051014",
                "positionSize": "-11",
                "price": "8.031000",
                "effectiveAt": "2022-07-29T21:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "6.788757",
                "rate": "-0.0000419762",
                "positionSize": "93.37",
                "price": "1732.130000",
                "effectiveAt": "2022-07-29T21:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "0.000118",
                "rate": "-0.0000065836",
                "positionSize": "12",
                "price": "1.501689",
                "effectiveAt": "2022-07-29T20:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-1.176198",
                "rate": "0.0000113796",
                "positionSize": "11548.4",
                "price": "8.950268",
                "effectiveAt": "2022-07-29T20:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "-0.000623",
                "rate": "-0.0000070965",
                "positionSize": "-11",
                "price": "7.988995",
                "effectiveAt": "2022-07-29T20:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "3.626283",
                "rate": "-0.0000225656",
                "positionSize": "93.37",
                "price": "1721.113000",
                "effectiveAt": "2022-07-29T20:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "0.000686",
                "rate": "-0.0000378432",
                "positionSize": "12",
                "price": "1.512000",
                "effectiveAt": "2022-07-29T19:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "4.804977",
                "rate": "-0.0000460564",
                "positionSize": "11548.4",
                "price": "9.034000",
                "effectiveAt": "2022-07-29T19:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "-0.001592",
                "rate": "-0.0000182988",
                "positionSize": "-11",
                "price": "7.913000",
                "effectiveAt": "2022-07-29T19:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "6.625776",
                "rate": "-0.0000411097",
                "positionSize": "93.37",
                "price": "1726.180000",
                "effectiveAt": "2022-07-29T19:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000108",
                "rate": "0.0000060968",
                "positionSize": "12",
                "price": "1.486786",
                "effectiveAt": "2022-07-29T18:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-0.653222",
                "rate": "0.0000063173",
                "positionSize": "11548.4",
                "price": "8.954000",
                "effectiveAt": "2022-07-29T18:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "-0.000169",
                "rate": "-0.0000019551",
                "positionSize": "-11",
                "price": "7.868188",
                "effectiveAt": "2022-07-29T18:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "2.618814",
                "rate": "-0.0000165389",
                "positionSize": "93.37",
                "price": "1695.870286",
                "effectiveAt": "2022-07-29T18:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000669",
                "rate": "0.0000375959",
                "positionSize": "12",
                "price": "1.484000",
                "effectiveAt": "2022-07-29T17:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-3.270467",
                "rate": "0.0000318272",
                "positionSize": "11548.4",
                "price": "8.898000",
                "effectiveAt": "2022-07-29T17:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.005073",
                "rate": "0.0000588251",
                "positionSize": "-11",
                "price": "7.840000",
                "effectiveAt": "2022-07-29T17:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "3.012536",
                "rate": "-0.0000190718",
                "positionSize": "93.37",
                "price": "1691.740000",
                "effectiveAt": "2022-07-29T17:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000149",
                "rate": "0.0000083045",
                "positionSize": "12",
                "price": "1.501000",
                "effectiveAt": "2022-07-29T16:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-1.111343",
                "rate": "0.0000106463",
                "positionSize": "11548.4",
                "price": "9.039318",
                "effectiveAt": "2022-07-29T16:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.000942",
                "rate": "0.0000108553",
                "positionSize": "-11",
                "price": "7.893537",
                "effectiveAt": "2022-07-29T16:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "-0.823575",
                "rate": "0.0000051296",
                "positionSize": "93.37",
                "price": "1719.578411",
                "effectiveAt": "2022-07-29T16:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000243",
                "rate": "0.0000135198",
                "positionSize": "12",
                "price": "1.501000",
                "effectiveAt": "2022-07-29T15:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-1.910837",
                "rate": "0.0000184238",
                "positionSize": "11548.4",
                "price": "8.981000",
                "effectiveAt": "2022-07-29T15:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.000708",
                "rate": "0.0000081333",
                "positionSize": "-11",
                "price": "7.916106",
                "effectiveAt": "2022-07-29T15:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "5.67333",
                "rate": "-0.0000351635",
                "positionSize": "93.37",
                "price": "1727.983797",
                "effectiveAt": "2022-07-29T15:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000204",
                "rate": "0.0000113855",
                "positionSize": "12",
                "price": "1.496842",
                "effectiveAt": "2022-07-29T14:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-0.454787",
                "rate": "0.0000043895",
                "positionSize": "11548.4",
                "price": "8.972000",
                "effectiveAt": "2022-07-29T14:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "-0.001239",
                "rate": "-0.0000145906",
                "positionSize": "-11",
                "price": "7.721238",
                "effectiveAt": "2022-07-29T14:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "3.218974",
                "rate": "-0.0000202754",
                "positionSize": "93.37",
                "price": "1700.360000",
                "effectiveAt": "2022-07-29T14:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000334",
                "rate": "0.0000190896",
                "positionSize": "12",
                "price": "1.458650",
                "effectiveAt": "2022-07-29T13:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-1.156569",
                "rate": "0.0000114723",
                "positionSize": "11548.4",
                "price": "8.729815",
                "effectiveAt": "2022-07-29T13:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.001177",
                "rate": "0.0000143704",
                "positionSize": "-11",
                "price": "7.450000",
                "effectiveAt": "2022-07-29T13:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "1.764934",
                "rate": "-0.0000113396",
                "positionSize": "93.37",
                "price": "1666.962189",
                "effectiveAt": "2022-07-29T13:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000503",
                "rate": "0.0000283026",
                "positionSize": "12",
                "price": "1.483000",
                "effectiveAt": "2022-07-29T12:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-1.913687",
                "rate": "0.0000189798",
                "positionSize": "11548.4",
                "price": "8.731000",
                "effectiveAt": "2022-07-29T12:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.002413",
                "rate": "0.0000292165",
                "positionSize": "-11",
                "price": "7.510000",
                "effectiveAt": "2022-07-29T12:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "0.186089",
                "rate": "-0.0000011832",
                "positionSize": "93.37",
                "price": "1684.592100",
                "effectiveAt": "2022-07-29T12:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000147",
                "rate": "0.0000079038",
                "positionSize": "12",
                "price": "1.550806",
                "effectiveAt": "2022-07-29T11:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-1.362721",
                "rate": "0.0000132096",
                "positionSize": "11548.4",
                "price": "8.933000",
                "effectiveAt": "2022-07-29T11:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.001439",
                "rate": "0.0000169370",
                "positionSize": "-11",
                "price": "7.728207",
                "effectiveAt": "2022-07-29T11:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "-1.712216",
                "rate": "0.0000106450",
                "positionSize": "93.37",
                "price": "1722.700000",
                "effectiveAt": "2022-07-29T11:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000244",
                "rate": "0.0000132843",
                "positionSize": "12",
                "price": "1.535049",
                "effectiveAt": "2022-07-29T10:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-0.785619",
                "rate": "0.0000076327",
                "positionSize": "11548.4",
                "price": "8.913000",
                "effectiveAt": "2022-07-29T10:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.001117",
                "rate": "0.0000131872",
                "positionSize": "-11",
                "price": "7.703375",
                "effectiveAt": "2022-07-29T10:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "-0.447288",
                "rate": "0.0000027697",
                "positionSize": "93.37",
                "price": "1729.633100",
                "effectiveAt": "2022-07-29T10:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000202",
                "rate": "0.0000110970",
                "positionSize": "12",
                "price": "1.521892",
                "effectiveAt": "2022-07-29T09:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-1.824661",
                "rate": "0.0000177630",
                "positionSize": "11548.4",
                "price": "8.895000",
                "effectiveAt": "2022-07-29T09:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.000552",
                "rate": "0.0000065910",
                "positionSize": "-11",
                "price": "7.623690",
                "effectiveAt": "2022-07-29T09:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "-0.44944",
                "rate": "0.0000027972",
                "positionSize": "93.37",
                "price": "1720.850000",
                "effectiveAt": "2022-07-29T09:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000073",
                "rate": "0.0000040426",
                "positionSize": "12",
                "price": "1.525144",
                "effectiveAt": "2022-07-29T08:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-2.925652",
                "rate": "0.0000283821",
                "positionSize": "11548.4",
                "price": "8.926000",
                "effectiveAt": "2022-07-29T08:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.001788",
                "rate": "0.0000213952",
                "positionSize": "-11",
                "price": "7.598439",
                "effectiveAt": "2022-07-29T08:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "1.183905",
                "rate": "-0.0000073901",
                "positionSize": "93.37",
                "price": "1715.777468",
                "effectiveAt": "2022-07-29T08:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.00043",
                "rate": "0.0000235313",
                "positionSize": "12",
                "price": "1.524000",
                "effectiveAt": "2022-07-29T07:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-3.718451",
                "rate": "0.0000362004",
                "positionSize": "11548.4",
                "price": "8.894667",
                "effectiveAt": "2022-07-29T07:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.002634",
                "rate": "0.0000315819",
                "positionSize": "-11",
                "price": "7.584000",
                "effectiveAt": "2022-07-29T07:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "0.094609",
                "rate": "-0.0000005895",
                "positionSize": "93.37",
                "price": "1719.017282",
                "effectiveAt": "2022-07-29T07:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000339",
                "rate": "0.0000181376",
                "positionSize": "12",
                "price": "1.561000",
                "effectiveAt": "2022-07-29T06:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-1.290607",
                "rate": "0.0000122676",
                "positionSize": "11548.4",
                "price": "9.110000",
                "effectiveAt": "2022-07-29T06:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.001206",
                "rate": "0.0000143056",
                "positionSize": "-11",
                "price": "7.666000",
                "effectiveAt": "2022-07-29T06:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "-0.023326",
                "rate": "0.0000001445",
                "positionSize": "93.37",
                "price": "1730.250000",
                "effectiveAt": "2022-07-29T06:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000296",
                "rate": "0.0000157783",
                "positionSize": "12",
                "price": "1.565987",
                "effectiveAt": "2022-07-29T05:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-1.955364",
                "rate": "0.0000184145",
                "positionSize": "11548.4",
                "price": "9.194901",
                "effectiveAt": "2022-07-29T05:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.000328",
                "rate": "0.0000038880",
                "positionSize": "-11",
                "price": "7.680861",
                "effectiveAt": "2022-07-29T05:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "-0.27309",
                "rate": "0.0000016820",
                "positionSize": "93.37",
                "price": "1738.890500",
                "effectiveAt": "2022-07-29T05:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000308",
                "rate": "0.0000164266",
                "positionSize": "12",
                "price": "1.567000",
                "effectiveAt": "2022-07-29T04:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-1.482535",
                "rate": "0.0000137604",
                "positionSize": "11548.4",
                "price": "9.329500",
                "effectiveAt": "2022-07-29T04:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "-0.000899",
                "rate": "-0.0000107255",
                "positionSize": "-11",
                "price": "7.628000",
                "effectiveAt": "2022-07-29T04:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "-0.336417",
                "rate": "0.0000020848",
                "positionSize": "93.37",
                "price": "1728.320000",
                "effectiveAt": "2022-07-29T04:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000508",
                "rate": "0.0000272284",
                "positionSize": "12",
                "price": "1.555556",
                "effectiveAt": "2022-07-29T03:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-1.766286",
                "rate": "0.0000163336",
                "positionSize": "11548.4",
                "price": "9.364000",
                "effectiveAt": "2022-07-29T03:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.001312",
                "rate": "0.0000161998",
                "positionSize": "-11",
                "price": "7.363951",
                "effectiveAt": "2022-07-29T03:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "0.593137",
                "rate": "-0.0000037151",
                "positionSize": "93.37",
                "price": "1709.967100",
                "effectiveAt": "2022-07-29T03:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000157",
                "rate": "0.0000083892",
                "positionSize": "12",
                "price": "1.561000",
                "effectiveAt": "2022-07-29T02:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-1.853135",
                "rate": "0.0000172457",
                "positionSize": "11548.4",
                "price": "9.304801",
                "effectiveAt": "2022-07-29T02:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.00246",
                "rate": "0.0000302288",
                "positionSize": "-11",
                "price": "7.400412",
                "effectiveAt": "2022-07-29T02:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "-1.304146",
                "rate": "0.0000081379",
                "positionSize": "93.37",
                "price": "1716.377000",
                "effectiveAt": "2022-07-29T02:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000295",
                "rate": "0.0000156229",
                "positionSize": "12",
                "price": "1.577000",
                "effectiveAt": "2022-07-29T01:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-1.960339",
                "rate": "0.0000182429",
                "positionSize": "11548.4",
                "price": "9.305000",
                "effectiveAt": "2022-07-29T01:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.000507",
                "rate": "0.0000061673",
                "positionSize": "-11",
                "price": "7.485674",
                "effectiveAt": "2022-07-29T01:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "1.712521",
                "rate": "-0.0000106196",
                "positionSize": "93.37",
                "price": "1727.109700",
                "effectiveAt": "2022-07-29T01:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000459",
                "rate": "0.0000243169",
                "positionSize": "12",
                "price": "1.573792",
                "effectiveAt": "2022-07-29T00:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-1.355112",
                "rate": "0.0000126474",
                "positionSize": "11548.4",
                "price": "9.278000",
                "effectiveAt": "2022-07-29T00:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.001448",
                "rate": "0.0000181998",
                "positionSize": "-11",
                "price": "7.236152",
                "effectiveAt": "2022-07-29T00:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "-0.887054",
                "rate": "0.0000055069",
                "positionSize": "93.37",
                "price": "1725.210579",
                "effectiveAt": "2022-07-29T00:00:00.000Z"
              },
              {
                "market": "SUSHI-USD",
                "payment": "-0.000517",
                "rate": "0.0000273084",
                "positionSize": "12",
                "price": "1.580000",
                "effectiveAt": "2022-07-28T23:00:00.000Z"
              },
              {
                "market": "UNI-USD",
                "payment": "-2.746819",
                "rate": "0.0000255536",
                "positionSize": "11548.4",
                "price": "9.308000",
                "effectiveAt": "2022-07-28T23:00:00.000Z"
              },
              {
                "market": "LINK-USD",
                "payment": "0.00103",
                "rate": "0.0000128557",
                "positionSize": "-11",
                "price": "7.284770",
                "effectiveAt": "2022-07-28T23:00:00.000Z"
              },
              {
                "market": "ETH-USD",
                "payment": "0.859858",
                "rate": "-0.0000052740",
                "positionSize": "93.37",
                "price": "1746.180319",
                "effectiveAt": "2022-07-28T23:00:00.000Z"
              }
            ]
          }
        }
    """.trimIndent()
    internal val channel_data = """
        {
          "type": "channel_data",
          "connection_id": "8ae0694d-d991-4afc-9999-a07006d0cfca",
          "message_id": 19,
          "id": "dace1648-c854-5aed-9879-88899bf647a3",
          "channel": "v3_accounts",
          "contents": {
            "fills": [
              {
                "market": "ETH-USD",
                "transactionId": "45929918",
                "quoteAmount": "945.3928",
                "price": "1621.600000",
                "size": "0.583",
                "liquidity": "TAKER",
                "accountId": "dace1648-c854-5aed-9879-88899bf647a3",
                "side": "BUY",
                "orderId": "025f806ef88ff3cdefaa7eea314040f77625cd71cbd837d951036b23c51f397",
                "fee": "0.472696",
                "type": "MARKET",
                "id": "c4f12741-dbe6-597c-8efc-0fd5801f99b3",
                "nonce": null,
                "forcePositionId": null,
                "updatedAt": "2022-08-01T19:53:29.686Z",
                "createdAt": "2022-08-01T19:53:29.686Z",
                "orderClientId": "66507608042289887"
              },
              {
                "market": "ETH-USD",
                "transactionId": "45929917",
                "quoteAmount": "52929.003",
                "price": "1621.500000",
                "size": "32.642",
                "liquidity": "TAKER",
                "accountId": "dace1648-c854-5aed-9879-88899bf647a3",
                "side": "BUY",
                "orderId": "025f806ef88ff3cdefaa7eea314040f77625cd71cbd837d951036b23c51f397",
                "fee": "26.464501",
                "type": "MARKET",
                "id": "33afca57-2512-5778-b8f8-78ff96cf3afe",
                "nonce": null,
                "forcePositionId": null,
                "updatedAt": "2022-08-01T19:53:29.686Z",
                "createdAt": "2022-08-01T19:53:29.686Z",
                "orderClientId": "66507608042289887"
              },
              {
                "market": "ETH-USD",
                "transactionId": "45929916",
                "quoteAmount": "19496.916",
                "price": "1621.500000",
                "size": "12.024",
                "liquidity": "TAKER",
                "accountId": "dace1648-c854-5aed-9879-88899bf647a3",
                "side": "BUY",
                "orderId": "025f806ef88ff3cdefaa7eea314040f77625cd71cbd837d951036b23c51f397",
                "fee": "9.748458",
                "type": "MARKET",
                "id": "7f39d458-5290-58db-8bb4-bceabbfa7d27",
                "nonce": null,
                "forcePositionId": null,
                "updatedAt": "2022-08-01T19:53:29.686Z",
                "createdAt": "2022-08-01T19:53:29.686Z",
                "orderClientId": "66507608042289887"
              }
            ],
            "positions": [
              {
                "id": "70af36fe-f803-5185-b2bb-69eced5d73b5",
                "accountId": "dace1648-c854-5aed-9879-88899bf647a3",
                "market": "ETH-USD",
                "side": "LONG",
                "status": "OPEN",
                "size": "192.096",
                "maxSize": "192.096",
                "entryPrice": "1314.480485",
                "exitPrice": "1265.594735",
                "openTransactionId": "45324205",
                "closeTransactionId": null,
                "lastTransactionId": "45929918",
                "closedAt": null,
                "updatedAt": "2022-06-30T01:01:10.234Z",
                "createdAt": "2022-06-30T01:01:10.234Z",
                "sumOpen": "377.245",
                "sumClose": "185.149",
                "netFunding": "-4155.221089",
                "realizedPnl": "-13206.368924"
              }
            ],
            "orders": [
              {
                "id": "025f806ef88ff3cdefaa7eea314040f77625cd71cbd837d951036b23c51f397",
                "clientId": "66507608042289887",
                "market": "ETH-USD",
                "accountId": "dace1648-c854-5aed-9879-88899bf647a3",
                "side": "BUY",
                "size": "45.249",
                "remainingSize": "0",
                "limitFee": "0.002",
                "price": "1702.7",
                "triggerPrice": null,
                "trailingPercent": null,
                "type": "MARKET",
                "status": "FILLED",
                "signature": "07c17d2804cbe11203a200c496e0fb72af4d4e824367f4a60227261b7f459ebe06f5fe893596b66a6bc7b23d05b83a99dc118fe7f967dee52792c01b8dab88bd",
                "timeInForce": "IOC",
                "postOnly": false,
                "cancelReason": null,
                "expiresAt": "2022-08-01T20:13:29.361Z",
                "unfillableAt": "2022-08-01T19:53:29.686Z",
                "updatedAt": "2022-08-01T19:53:29.686Z",
                "createdAt": "2022-08-01T19:53:29.653Z",
                "reduceOnly": false,
                "country": "JP",
                "client": "01",
                "reduceOnlySize": null
              }
            ],
            "accounts": [
              {
                "id": "dace1648-c854-5aed-9879-88899bf647a3",
                "userId": "11480ece-f71d-5012-bb7f-bf1f74b749a7",
                "accountNumber": 0,
                "starkKey": "01de24f8468f0590b80c12c87ae9e247ef3278d4683d875296051495b2ad0100",
                "quoteBalance": "-222333.2879",
                "lastTransactionId": "45929918",
                "updatedAt": "2022-08-01T19:53:29.686Z",
                "createdAt": "2021-04-20T18:27:38.698Z",
                "positionId": "30915",
                "starkKeyYCoordinate": "029b2777250c190b22d943780a884d248666030e1339544ab547b40bec0d2237"
              }
            ],
            "fundingPayments": []
          }
        }
    """.trimIndent()

    internal val simpleSubscribed = """
        {
          "type": "subscribed",
          "connection_id": "c49e6d6b-d489-4140-bbaf-dc11b2fab13a",
          "message_id": 1,
          "channel": "v3_accounts",
          "id": "dace1648-c854-5aed-9879-88899bf647a3",
          "contents": {
            "account": {
              "starkKey": "01de24f8468f0590b80c12c87ae9e247ef3278d4683d875296051495b2ad0100",
              "positionId": "30915",
              "equity": "205935.352966",
              "freeCollateral": "187233.155294",
              "pendingDeposits": "0.000000",
              "pendingWithdrawals": "0.000000",
              "accountNumber": "0",
              "id": "dace1648-c854-5aed-9879-88899bf647a3",
              "quoteBalance": "10000.00",
              "createdAt": "2021-04-20T18:27:38.698Z"
            }
          }
        }
    """.trimIndent()

    internal val fillsReceived = """
        {
            "fills": [
              {
                "market": "ETH-USD",
                "transactionId": "45929918",
                "quoteAmount": "945.3928",
                "price": "1621.600000",
                "size": "0.583",
                "liquidity": "TAKER",
                "accountId": "dace1648-c854-5aed-9879-88899bf647a3",
                "side": "BUY",
                "orderId": "025f806ef88ff3cdefaa7eea314040f77625cd71cbd837d951036b23c51f397",
                "fee": "0.472696",
                "type": "MARKET",
                "id": "c4f12741-dbe6-597c-8efc-0fd5801f99b3",
                "nonce": null,
                "forcePositionId": null,
                "updatedAt": "2022-08-01T19:53:29.686Z",
                "createdAt": "2022-08-01T19:53:29.686Z",
                "orderClientId": "66507608042289887"
              },
              {
                "market": "ETH-USD",
                "transactionId": "45929917",
                "quoteAmount": "52929.003",
                "price": "1621.500000",
                "size": "32.642",
                "liquidity": "TAKER",
                "accountId": "dace1648-c854-5aed-9879-88899bf647a3",
                "side": "BUY",
                "orderId": "025f806ef88ff3cdefaa7eea314040f77625cd71cbd837d951036b23c51f397",
                "fee": "26.464501",
                "type": "MARKET",
                "id": "33afca57-2512-5778-b8f8-78ff96cf3afe",
                "nonce": null,
                "forcePositionId": null,
                "updatedAt": "2022-08-01T19:53:29.686Z",
                "createdAt": "2022-08-01T19:53:29.686Z",
                "orderClientId": "66507608042289887"
              },
              {
                "market": "ETH-USD",
                "transactionId": "45929916",
                "quoteAmount": "19496.916",
                "price": "1621.500000",
                "size": "12.024",
                "liquidity": "TAKER",
                "accountId": "dace1648-c854-5aed-9879-88899bf647a3",
                "side": "BUY",
                "orderId": "025f806ef88ff3cdefaa7eea314040f77625cd71cbd837d951036b23c51f397",
                "fee": "9.748458",
                "type": "MARKET",
                "id": "7f39d458-5290-58db-8bb4-bceabbfa7d27",
                "nonce": null,
                "forcePositionId": null,
                "updatedAt": "2022-08-01T19:53:29.686Z",
                "createdAt": "2022-08-01T19:53:29.686Z",
                "orderClientId": "66507608042289887"
              }
            ]
        }
    """.trimIndent()

    internal val v4accountsReceived = """
        {
            "totalTradingRewards": "2800.8",
        	"subaccounts": [{
        		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
        		"subaccountNumber": 0,
        		"equity": "100000.000000",
        		"freeCollateral": "100000.000000",
        		"openPerpetualPositions": {},
        		"marginEnabled": true,
                "assetPositions":{
                    "USDC":{
                       "symbol":"USDC",
                       "side":"LONG",
                       "size":"100000.000000",
                       "assetId":"0"
                    }
                 }
        	}, {
        		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
        		"subaccountNumber": 1,
        		"equity": "100000.000000",
        		"freeCollateral": "100000.000000",
        		"openPerpetualPositions": {},
        		"quoteBalance": "100000.000000",
        		"marginEnabled": true
        	}, {
        		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
        		"subaccountNumber": 2,
        		"equity": "100000.000000",
        		"freeCollateral": "100000.000000",
        		"openPerpetualPositions": {},
        		"quoteBalance": "100000.000000",
        		"marginEnabled": true
        	}, {
        		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
        		"subaccountNumber": 3,
        		"equity": "100000.000000",
        		"freeCollateral": "100000.000000",
        		"openPerpetualPositions": {},
        		"quoteBalance": "100000.000000",
        		"marginEnabled": true
        	}, {
        		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
        		"subaccountNumber": 4,
        		"equity": "100000.000000",
        		"freeCollateral": "100000.000000",
        		"openPerpetualPositions": {},
        		"quoteBalance": "100000.000000",
        		"marginEnabled": true
        	}, {
        		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
        		"subaccountNumber": 5,
        		"equity": "100000.000000",
        		"freeCollateral": "100000.000000",
        		"openPerpetualPositions": {},
        		"quoteBalance": "100000.000000",
        		"marginEnabled": true
        	}, {
        		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
        		"subaccountNumber": 6,
        		"equity": "100000.000000",
        		"freeCollateral": "100000.000000",
        		"openPerpetualPositions": {},
        		"quoteBalance": "100000.000000",
        		"marginEnabled": true
        	}, {
        		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
        		"subaccountNumber": 7,
        		"equity": "100000.000000",
        		"freeCollateral": "100000.000000",
        		"openPerpetualPositions": {},
        		"quoteBalance": "100000.000000",
        		"marginEnabled": true
        	}, {
        		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
        		"subaccountNumber": 8,
        		"equity": "100000.000000",
        		"freeCollateral": "100000.000000",
        		"openPerpetualPositions": {},
        		"quoteBalance": "100000.000000",
        		"marginEnabled": true
        	}, {
        		"address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
        		"subaccountNumber": 9,
        		"equity": "100000.000000",
        		"freeCollateral": "100000.000000",
        		"openPerpetualPositions": {},
        		"quoteBalance": "100000.000000",
        		"marginEnabled": true
        	}]
        }
    """.trimIndent()

    internal val v4accountsReceivedWithPositions = """
        {
            "totalTradingRewards": "2800.8",
        	"subaccounts": [{
        		"address": "cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f",
        		"subaccountNumber": 0,
        		"equity": "99872.368956",
        		"freeCollateral": "99872.368956",
                "assetPositions":{
                    "USDC":{
                       "symbol":"USDC",
                       "side":"LONG",
                       "size":"99872.368956",
                       "assetId":"0"
                    }
                },
        		"openPerpetualPositions": {
        			"BTC-USD": {
        				"market": "BTC-USD",
        				"status": "OPEN",
        				"side": "SHORT",
        				"size": "-0.442371112",
        				"maxSize": "0.442388027",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:14:15.883Z",
        				"createdAtHeight": "862",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			},
        			"ETH-USD": {
        				"market": "ETH-USD",
        				"status": "OPEN",
        				"side": "LONG",
        				"size": "10.771577",
        				"maxSize": "12.287588",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:11:55.085Z",
        				"createdAtHeight": "837",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			}
        		},
        		"marginEnabled": true
        	}, {
        		"address": "cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f",
        		"subaccountNumber": 1,
        		"equity": "91885.406754",
        		"freeCollateral": "91885.406754",
        		"openPerpetualPositions": {
        			"ETH-USD": {
        				"market": "ETH-USD",
        				"status": "OPEN",
        				"side": "LONG",
        				"size": "17.193236",
        				"maxSize": "17.193236",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:14:37.966Z",
        				"createdAtHeight": "866",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			},
        			"BTC-USD": {
        				"market": "BTC-USD",
        				"status": "OPEN",
        				"side": "SHORT",
        				"size": "-0.827489986",
        				"maxSize": "0.827501132",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:13:09.083Z",
        				"createdAtHeight": "850",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			}
        		},
        		"marginEnabled": true
        	}, {
        		"address": "cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f",
        		"subaccountNumber": 2,
        		"equity": "115561.717334",
        		"freeCollateral": "115561.717334",
        		"openPerpetualPositions": {
        			"BTC-USD": {
        				"market": "BTC-USD",
        				"status": "OPEN",
        				"side": "SHORT",
        				"size": "-0.627197613",
        				"maxSize": "0.627197613",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:12:52.009Z",
        				"createdAtHeight": "847",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			},
        			"ETH-USD": {
        				"market": "ETH-USD",
        				"status": "OPEN",
        				"side": "SHORT",
        				"size": "-11.993303",
        				"maxSize": "11.993303",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:13:14.539Z",
        				"createdAtHeight": "851",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			}
        		},
        		"marginEnabled": true
        	}, {
        		"address": "cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f",
        		"subaccountNumber": 3,
        		"equity": "92022.400711",
        		"freeCollateral": "92022.400711",
        		"openPerpetualPositions": {
        			"BTC-USD": {
        				"market": "BTC-USD",
        				"status": "OPEN",
        				"side": "SHORT",
        				"size": "0.325948194",
        				"maxSize": "0.325948194",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:13:14.539Z",
        				"createdAtHeight": "851",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			},
        			"ETH-USD": {
        				"market": "ETH-USD",
        				"status": "OPEN",
        				"side": "LONG",
        				"size": "7.647789",
        				"maxSize": "7.648114",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:12:52.009Z",
        				"createdAtHeight": "847",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			}
        		},
        		"marginEnabled": true
        	}, {
        		"address": "cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f",
        		"subaccountNumber": 4,
        		"equity": "71624.827260",
        		"freeCollateral": "71624.827260",
        		"openPerpetualPositions": {
        			"BTC-USD": {
        				"market": "BTC-USD",
        				"status": "OPEN",
        				"side": "LONG",
        				"size": "0.603015874",
        				"maxSize": "0.687982338",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:12:57.783Z",
        				"createdAtHeight": "848",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			},
        			"ETH-USD": {
        				"market": "ETH-USD",
        				"status": "OPEN",
        				"side": "LONG",
        				"size": "15.783232",
        				"maxSize": "15.783278",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:13:42.265Z",
        				"createdAtHeight": "856",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			}
        		},
        		"marginEnabled": true
        	}, {
        		"address": "cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f",
        		"subaccountNumber": 5,
        		"equity": "123711.079307",
        		"freeCollateral": "123711.079307",
        		"openPerpetualPositions": {
        			"ETH-USD": {
        				"market": "ETH-USD",
        				"status": "OPEN",
        				"side": "SHORT",
        				"size": "8.37203",
        				"maxSize": "8.37203",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:12:57.783Z",
        				"createdAtHeight": "848",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			},
        			"BTC-USD": {
        				"market": "BTC-USD",
        				"status": "OPEN",
        				"side": "SHORT",
        				"size": "1.203507471",
        				"maxSize": "1.230299568",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:13:42.265Z",
        				"createdAtHeight": "856",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			}
        		},
        		"marginEnabled": true
        	}, {
        		"address": "cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f",
        		"subaccountNumber": 6,
        		"equity": "101425.205827",
        		"freeCollateral": "101425.205827",
        		"openPerpetualPositions": {
        			"ETH-USD": {
        				"market": "ETH-USD",
        				"status": "OPEN",
        				"side": "LONG",
        				"size": "6.29579",
        				"maxSize": "6.29579",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:12:57.783Z",
        				"createdAtHeight": "848",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			},
        			"BTC-USD": {
        				"market": "BTC-USD",
        				"status": "OPEN",
        				"side": "SHORT",
        				"size": "0.773364932",
        				"maxSize": "0.859632568",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:13:20.126Z",
        				"createdAtHeight": "852",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			}
        		},
        		"marginEnabled": true
        	}, {
        		"address": "cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f",
        		"subaccountNumber": 7,
        		"equity": "122832.442063",
        		"freeCollateral": "122832.442063",
        		"openPerpetualPositions": {
        			"ETH-USD": {
        				"market": "ETH-USD",
        				"status": "OPEN",
        				"side": "LONG",
        				"size": "2.130128",
        				"maxSize": "2.130128",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:13:03.315Z",
        				"createdAtHeight": "849",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			},
        			"BTC-USD": {
        				"market": "BTC-USD",
        				"status": "OPEN",
        				"side": "LONG",
        				"size": "0.221702469",
        				"maxSize": "0.221702469",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:13:25.672Z",
        				"createdAtHeight": "853",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			}
        		},
        		"marginEnabled": true
        	}, {
        		"address": "cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f",
        		"subaccountNumber": 8,
        		"equity": "103936.326009",
        		"freeCollateral": "103936.326009",
        		"openPerpetualPositions": {
        			"BTC-USD": {
        				"market": "BTC-USD",
        				"status": "OPEN",
        				"side": "LONG",
        				"size": "0.71912606",
        				"maxSize": "0.71912606",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:13:03.315Z",
        				"createdAtHeight": "849",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			},
        			"ETH-USD": {
        				"market": "ETH-USD",
        				"status": "OPEN",
        				"side": "SHORT",
        				"size": "6.570326",
        				"maxSize": "6.589682",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:13:47.933Z",
        				"createdAtHeight": "857",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			}
        		},
        		"marginEnabled": true
        	}, {
        		"address": "cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f",
        		"subaccountNumber": 9,
        		"equity": "90767.289899",
        		"freeCollateral": "90767.289899",
        		"openPerpetualPositions": {
        			"ETH-USD": {
        				"market": "ETH-USD",
        				"status": "OPEN",
        				"side": "LONG",
        				"size": "10.212553",
        				"maxSize": "10.380502",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:14:10.306Z",
        				"createdAtHeight": "861",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			},
        			"BTC-USD": {
        				"market": "BTC-USD",
        				"status": "OPEN",
        				"side": "LONG",
        				"size": "0.435364347",
        				"maxSize": "0.435364347",
        				"entryPrice": "0.000000",
        				"exitPrice": null,
        				"realizedPnl": "0.000000",
        				"unrealizedPnl": "0.000000",
        				"createdAt": "2022-12-02T16:13:09.083Z",
        				"createdAtHeight": "850",
        				"closedAt": null,
        				"sumOpen": "0",
        				"sumClose": "0",
        				"netFunding": "0"
        			}
        		},
        		"marginEnabled": true
        	}]
        }
    """.trimIndent()

    internal val v4_subscribed = """
        {
           "type":"subscribed",
           "connection_id":"d8caff8c-0ee8-4eb0-b124-20c2d3d956ba",
           "message_id":5,
           "channel":"v4_subaccounts",
           "id":"cosmos1jtpspgllck9z4ghkqhupum35q55xt99sg3guxn/0",
           "contents":{
              "subaccount":{
                 "address":"cosmos1jtpspgllck9z4ghkqhupum35q55xt99sg3guxn",
                 "subaccountNumber":0,
                 "equity":"68257.215192",
                 "freeCollateral":"68257.215192",
                 "openPerpetualPositions":{
                    "ETH-USD":{
                       "market":"ETH-USD",
                       "status":"OPEN",
                       "side":"SHORT",
                       "size":"106.17985",
                       "maxSize":"106.180627",
                       "entryPrice":"1266.094016",
                       "exitPrice":"1266.388746",
                       "realizedPnl":"-102.716895",
                       "unrealizedPnl":"134433.672665",
                       "createdAt":"2022-12-11T17:29:39.792Z",
                       "createdAtHeight":"45",
                       "closedAt":null,
                       "sumOpen":"454.689628",
                       "sumClose":"348.51174",
                       "netFunding":"0"
                    },
                    "BTC-USD":{
                       "market":"BTC-USD",
                       "status":"OPEN",
                       "side":"LONG",
                       "size":"9.974575029",
                       "maxSize":"9.974575029",
                       "entryPrice":"17101.489388",
                       "exitPrice":"17106.497989",
                       "realizedPnl":"126.640212",
                       "unrealizedPnl":"-170580.089008",
                       "createdAt":"2022-12-11T17:27:36.351Z",
                       "createdAtHeight":"23",
                       "closedAt":null,
                       "sumOpen":"35.035107647",
                       "sumClose":"25.284547806",
                       "netFunding":"0"
                    }
                 },
                 "quoteBalance":"68257.215192",
                 "marginEnabled":true
              },
              "orders":[
                 {
                    "id":"b812bea8-29d3-5841-9549-caa072f6f8a8",
                    "subaccountId":"660efb4c-5472-5119-8c17-65cf702ccaea",
                    "clientId":"2194126268",
                    "clobPairId":"1",
                    "side":"SELL",
                    "size":"1.653451",
                    "totalFilled":"0.682633",
                    "price":"1255.927",
                    "type":"LIMIT",
                    "status":"BEST_EFFORT_OPENED",
                    "timeInForce":"GTT",
                    "postOnly":false,
                    "reduceOnly":false,
                    "goodTilBlock":"5837"
                 },
                 {
                    "id":"b812bea8-29d3-5841-9549-caa072f6f8a9",
                    "subaccountId":"660efb4c-5472-5119-8c17-65cf702ccaea",
                    "clientId":"2194126269",
                    "clobPairId":"1",
                    "side":"SELL",
                    "size":"1.653451",
                    "totalFilled":"0.682633",
                    "price":"1255.927",
                    "type":"LIMIT",
                    "status":"BEST_EFFORT_CANCELED",
                    "timeInForce":"GTT",
                    "postOnly":false,
                    "reduceOnly":false,
                    "goodTilBlock":"5837"
                 }
              ],
              "tradingReward": {
                "tradingReward": "0.02",
                "createdAtHeight": "2422",
                "createdAt": "2023-08-09T20:00:00.000Z"
              }
           }
        }
    """.trimIndent()

    internal val v4_channel_data = """
        {
           "type":"channel_data",
           "connection_id":"d8caff8c-0ee8-4eb0-b124-20c2d3d956ba",
           "message_id":213,
           "id":"cosmos1jtpspgllck9z4ghkqhupum35q55xt99sg3guxn/0",
           "channel":"v4_subaccounts",
           "contents":{
              "subaccounts":{
                 "address":"cosmos1jtpspgllck9z4ghkqhupum35q55xt99sg3guxn",
                 "subaccountNumber":0,
                 "quoteBalance":"68257.016598"
              },
              "positions":[
                 {
                    "address":"cosmos1jtpspgllck9z4ghkqhupum35q55xt99sg3guxn",
                    "subaccountNumber":0,
                    "positionId":"bd871b78-5cbe-5c0d-8708-cef3f8f1a5dd",
                    "market":"0",
                    "side":"LONG",
                    "status":"OPEN",
                    "size":"9.97458676",
                    "maxSize":"9.97458676"
                 }
              ],
                "tradingReward": {
                    "tradingReward": "0.01",
                    "createdAtHeight": "2501",
                    "createdAt": "2023-08-09T20:11:00.000Z"
                }
           }
        }
    """.trimIndent()

    internal val v4_error = """
        {
           "type":"error",
           "message":"Internal error, could not fetch data for subscription: v4_subaccounts",
           "connection_id":"18f9241d-1a2c-4d35-b639-bed57fa4d5a8",
           "message_id":4
        }
    """.trimIndent()

    internal val v4_channel_data_with_orders = """
        {
           "type":"channel_data",
           "connection_id":"d8caff8c-0ee8-4eb0-b124-20c2d3d956ba",
           "message_id":412,
           "id":"cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm/0",
           "channel":"v4_subaccounts",
           "contents":{
              "orders":[
                 {
                    "id":"b812bea8-29d3-5841-9549-caa072f6f8a8",
                    "market": "ETH-USD",
                    "subaccountId":"660efb4c-5472-5119-8c17-65cf702ccaea",
                    "clientId":"2194126268",
                    "clobPairId":"1",
                    "side":"SELL",
                    "size":"1.653451",
                    "totalFilled":"78.682633",
                    "price":"1255.927",
                    "type":"LIMIT",
                    "status":"FILLED",
                    "timeInForce":"GTT",
                    "postOnly":false,
                    "reduceOnly":false,
                    "goodTilBlock":"5837"
                 },
                 {
                    "id": "31d7d484-8685-570c-aa62-c2589cb6c8d8",
                    "subaccountId": "e470a747-3aa0-543e-aafa-0bd27d568901",
                    "clientId": "798778860",
                    "clobPairId": "1",
                    "side": "BUY",
                    "size": "0.01",
                    "totalFilled": "0",
                    "price": "2100",
                    "type": "STOP_LIMIT",
                    "status": "UNTRIGGERED",
                    "timeInForce": "GTT",
                    "reduceOnly": false,
                    "orderFlags": "32",
                    "goodTilBlockTime": "2024-04-17T16:54:49.000Z",
                    "createdAtHeight": "8864616",
                    "clientMetadata": "0",
                    "triggerPrice": "3300",
                    "updatedAt": "2024-03-20T16:54:49.788Z",
                    "updatedAtHeight": "8864616",
                    "postOnly": false,
                    "ticker": "ETH-USD"
                },
                {
                    "id": "0ae98da9-4fdc-5f08-b880-2449464b6b45",
                    "subaccountId": "e470a747-3aa0-543e-aafa-0bd27d568901",
                    "clientId": "847907069",
                    "clobPairId": "1",
                    "side": "BUY",
                    "size": "0.01",
                    "totalFilled": "0",
                    "price": "2000",
                    "type": "LIMIT",
                    "status": "OPEN",
                    "timeInForce": "GTT",
                    "reduceOnly": false,
                    "orderFlags": "64",
                    "goodTilBlockTime": "2024-04-17T16:54:30.000Z",
                    "createdAtHeight": "8864600",
                    "clientMetadata": "0",
                    "updatedAt": "2024-03-20T16:54:29.971Z",
                    "updatedAtHeight": "8864600",
                    "postOnly": false,
                    "ticker": "ETH-USD"
                },
                {
                    "id": "734617f4-29ba-50fe-878d-391ad4e4fbd1",
                    "subaccountId": "e470a747-3aa0-543e-aafa-0bd27d568901",
                    "clientId": "1597910963",
                    "clobPairId": "1",
                    "side": "BUY",
                    "size": "0.1",
                    "totalFilled": "0",
                    "price": "2100",
                    "type": "LIMIT",
                    "status": "OPEN",
                    "timeInForce": "GTT",
                    "reduceOnly": true,
                    "orderFlags": "64",
                    "goodTilBlockTime": "2024-04-02T15:57:16.000Z",
                    "createdAtHeight": "8489771",
                    "clientMetadata": "0",
                    "updatedAt": "2024-03-15T06:37:01.731Z",
                    "updatedAtHeight": "8489771",
                    "postOnly": false,
                    "ticker": "ETH-USD"
                },
                {
                    "id": "770933a5-0293-5aca-8a01-d9c4030d776d",
                    "subaccountId": "e470a747-3aa0-543e-aafa-0bd27d568901",
                    "clientId": "852785216",
                    "clobPairId": "1",
                    "side": "SELL",
                    "size": "0.01",
                    "price": "3406.4",
                    "status": "BEST_EFFORT_OPENED",
                    "type": "LIMIT",
                    "timeInForce": "IOC",
                    "postOnly": false,
                    "reduceOnly": false,
                    "orderFlags": "0",
                    "goodTilBlock": "8864635",
                    "ticker": "ETH-USD",
                    "clientMetadata": "1"
                }
              ],
              "fills":[
                 {
                    "id":"a9adbcde-dbf2-5e44-8630-fbb9891bc004",
                    "subaccountId":"660efb4c-5472-5119-8c17-65cf702ccaea",
                    "market": "ETH-USD",
                    "side":"SELL",
                    "liquidity":"MAKER",
                    "type":"LIMIT",
                    "clobPairId":"1",
                    "orderId":"b812bea8-29d3-5841-9549-caa072f6f8a8",
                    "size":"1.471414",
                    "price":"1255.927",
                    "quoteAmount":"1847.988570778",
                    "eventId":"a2189e39-c161-5db5-848c-f08af35d9820",
                    "transactionHash":"F79CD00CCDBC78C74820F00C9CE2BBCBA2BE8C88FFE05A992358829F8444B2E1",
                    "createdAt":"2023-01-19T02:28:20.460Z",
                    "createdAtHeight":"5820"
                 }
              ],
              "positions":[
                 {
                    "address":"cosmos1jtpspgllck9z4ghkqhupum35q55xt99sg3guxn",
                    "subaccountNumber":0,
                    "positionId":"93145a6e-7c51-5fd2-9463-3670a794eb32",
                    "market":"BTC-USD",
                    "side":"SHORT",
                    "status":"OPEN",
                    "size":"1.792239322",
                    "maxSize":"1.792239322"
                 }
              ]
           }
        }
    """.trimIndent()

    internal val v4_faucet_succeeded = """
        {
           "address":"cosmos1yugfd9ku0dazad9k22rfc40u6tn0pg2znmvt5h",
           "subaccountNumber":0,
           "amount":2000
        }
    """.trimIndent()

    internal val v4_subaccounts_failed = """
        {
           "errors":[
              {
                 "msg":"No subaccounts found for address cosmos1urenpf55s0pxr29s2l9wm25fc0j7h9xdxc05tx"
              }
           ]
        }
    """.trimIndent()

    internal val v4_subaccounts_update_1 = """
        {
           "type":"channel_data",
           "connection_id":"f7a21cc6-4a9f-42d7-8bbd-215f11614571",
           "message_id":119,
           "id":"cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm/0",
           "channel":"v4_subaccounts",
           "contents":{
              "subaccounts":{
                 "address":"cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                 "subaccountNumber":0
              },
              "perpetualPositions":[
                 {
                    "address":"cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                    "subaccountNumber":0,
                    "positionId":"1bb14a35-db8b-57c0-a39c-dc6b80b995e0",
                    "market":"ETH-USD",
                    "side":"SHORT",
                    "status":"OPEN",
                    "size":"2.043254",
                    "maxSize":"2.043254"
                 }
              ],
              "assetPositions":[
                 {
                    "address":"cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                    "subaccountNumber":0,
                    "positionId":"24a26508-9d45-5b4c-a13b-31f6e9780ecc",
                    "assetId":"0",
                    "denom":"USDC",
                    "side":"LONG",
                    "size":"7250.506704"
                 }
              ]
           }
        }
    """.trimIndent()

    internal val v4_subaccounts_update_2 = """
        {
           "type":"channel_data",
           "connection_id":"f7a21cc6-4a9f-42d7-8bbd-215f11614571",
           "message_id":120,
           "id":"cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm/0",
           "channel":"v4_subaccounts",
           "contents":{
              "subaccounts":{
                 "address":"cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                 "subaccountNumber":0
              },
              "perpetualPositions":[
                 {
                    "address":"cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                    "subaccountNumber":0,
                    "positionId":"1bb14a35-db8b-57c0-a39c-dc6b80b995e0",
                    "market":"ETH-USD",
                    "side":"SHORT",
                    "status":"OPEN",
                    "size":"3",
                    "maxSize":"3"
                 }
              ],
              "assetPositions":[
                 {
                    "address":"cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                    "subaccountNumber":0,
                    "positionId":"24a26508-9d45-5b4c-a13b-31f6e9780ecc",
                    "assetId":"0",
                    "denom":"USDC",
                    "side":"LONG",
                    "size":"8772.436277"
                 }
              ]
           }
        }
    """.trimIndent()

    internal val v4_subaccounts_update_3 = """
        {
           "type":"channel_data",
           "connection_id":"f7a21cc6-4a9f-42d7-8bbd-215f11614571",
           "message_id":121,
           "id":"cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm/0",
           "channel":"v4_subaccounts",
           "contents":{
              "orders":[
                 {
                    "id":"2caebf6b-35d3-512c-a4d9-e438445d8dba",
                    "subaccountId":"c1286a19-c341-5cc6-9523-1be5618119f1",
                    "clientId":"1067413651",
                    "clobPairId":"1",
                    "side":"SELL",
                    "size":"1",
                    "totalFilled":"0.043254",
                    "price":"1",
                    "type":"LIMIT",
                    "status":"OPEN",
                    "timeInForce":"GTT",
                    "postOnly":false,
                    "reduceOnly":false,
                    "goodTilBlock":"1110",
                    "ticker":"ETH-USD"
                 },
                 {
                    "id":"0ef4a74e-6916-5a1e-9744-baa9c37d5ce3",
                    "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                    "clientId":"2130017314",
                    "clobPairId":"1",
                    "side":"BUY",
                    "size":"0.1",
                    "totalFilled":"0",
                    "price":"500",
                    "type":"LIMIT",
                    "status":"OPEN",
                    "timeInForce":"GTT",
                    "reduceOnly":false,
                    "orderFlags":"64",
                    "goodTilBlockTime":"2023-06-01T21:12:07.000Z",
                    "postOnly":false,
                    "ticker":"ETH-USD"
                 }
              ],
              "fills":[
                 {
                    "id":"1036efdc-ae4a-5ed9-9587-a677e8667858",
                    "subaccountId":"c1286a19-c341-5cc6-9523-1be5618119f1",
                    "side":"SELL",
                    "liquidity":"TAKER",
                    "type":"LIMIT",
                    "clobPairId":"1",
                    "orderId":"2caebf6b-35d3-512c-a4d9-e438445d8dba",
                    "size":"0.043254",
                    "price":"1591.571",
                    "quoteAmount":"68.841812034",
                    "eventId":"575a8b7b-1cc1-54ab-a73c-de4fe45d55d2",
                    "transactionHash":"A00F878647877DFA6E45EC4D441525063D1522905845E807ADA38C43F9F843DA",
                    "createdAt":"2023-01-26T18:01:26.970Z",
                    "createdAtHeight":"1465",
                    "ticker":"ETH-USD"
                 }
              ]
           }
        }
    """.trimIndent()

    internal val v4_subaccounts_update_4 = """
        {
           "type":"channel_data",
           "connection_id":"f7a21cc6-4a9f-42d7-8bbd-215f11614571",
           "message_id":122,
           "id":"cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm/0",
           "channel":"v4_subaccounts",
           "contents":{
              "orders":[
                 {
                    "id":"2caebf6b-35d3-512c-a4d9-e438445d8dba",
                    "subaccountId":"c1286a19-c341-5cc6-9523-1be5618119f1",
                    "clientId":"1067413651",
                    "clobPairId":"1",
                    "side":"SELL",
                    "size":"1",
                    "totalFilled":"1",
                    "price":"1",
                    "type":"LIMIT",
                    "status":"FILLED",
                    "timeInForce":"GTT",
                    "postOnly":false,
                    "reduceOnly":false,
                    "goodTilBlock":"0",
                    "ticker":"ETH-USD"
                 }
              ],
              "fills":[
                 {
                    "id":"1c8547ea-8043-5ba2-8d6e-f9610005dea0",
                    "subaccountId":"c1286a19-c341-5cc6-9523-1be5618119f1",
                    "side":"SELL",
                    "liquidity":"TAKER",
                    "type":"LIMIT",
                    "clobPairId":"1",
                    "orderId":"2caebf6b-35d3-512c-a4d9-e438445d8dba",
                    "size":"0.956746",
                    "price":"1591.531",
                    "quoteAmount":"1522.690918126",
                    "eventId":"7bb3f2fd-3f32-5b21-8194-978d48f57cd1",
                    "transactionHash":"A00F878647877DFA6E45EC4D441525063D1522905845E807ADA38C43F9F843DA",
                    "createdAt":"2023-01-26T18:01:26.970Z",
                    "createdAtHeight":"1465",
                    "ticker":"ETH-USD"
                 }
              ]
           }
        }
    """.trimIndent()

    internal val v4_subaccounts_update_5 = """
        {
           "type":"channel_data",
           "connection_id":"f7a21cc6-4a9f-42d7-8bbd-215f11614571",
           "message_id":156,
           "id":"cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm/0",
           "channel":"v4_subaccounts",
           "contents":{
              "orders":[
                 {
                    "subaccountId":"c1286a19-c341-5cc6-9523-1be5618119f1",
                    "clientId":"1067413651",
                    "clobPairId":"1",
                    "side":"SELL",
                    "size":"1",
                    "price":"1",
                    "status":"OPEN",
                    "type":"LIMIT",
                    "goodTilBlock":0,
                    "ticker":"ETH-USD"
                 }
              ]
           }
        }
    """.trimIndent()

    internal val v4_subaccounts_update_6 = """
        {
           "type":"channel_data",
           "connection_id":"f7a21cc6-4a9f-42d7-8bbd-215f11614571",
           "message_id":157,
           "id":"cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm/0",
           "channel":"v4_subaccounts",
           "contents":{
              "orders":[
                 {
                    "subaccountId":"c1286a19-c341-5cc6-9523-1be5618119f1",
                    "clientId":"1067413651",
                    "clobPairId":"1",
                    "side":"SELL",
                    "size":"1",
                    "totalOptimisticFilled":"1",
                    "price":"1",
                    "goodTilBlock":0,
                    "ticker":"ETH-USD"
                 }
              ]
           }
        }
    """.trimIndent()

    internal val v4_subaccounts_update_7 = """
        {
           "type":"channel_data",
           "connection_id":"f7a21cc6-4a9f-42d7-8bbd-215f11614571",
           "message_id":157,
           "id":"cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm/0",
           "channel":"v4_subaccounts",
           "contents":{
              "orders":[
                 {
                    "id":"80133551-6d61-573b-9788-c1488e11027a",
                    "subaccountId":"8db381df-80e1-5a74-9d70-bb40d98f5375",
                    "clientId":"659473483",
                    "clobPairId":"1",
                    "side":"BUY",
                    "size":"1",
                    "totalOptimisticFilled":"0",
                    "price":"1",
                    "type":"LIMIT",
                    "status":"OPEN",
                    "timeInForce":"IOC",
                    "postOnly":false,
                    "reduceOnly":false,
                    "orderFlags":"0",
                    "goodTilBlock":"16950",
                    "ticker":"ETH-USD",
                    "totalFilled":"0.4",
                    "removalReason":"IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK"
                 }
              ]
           }
        }
    """.trimIndent()

    internal val v4_best_effort_cancelled = """
        {
           "type":"channel_data",
           "connection_id":"d034dabf-0406-4509-879a-1d9e161c5182",
           "message_id":55,
           "id":"cosmos144e2my9gwngm2zgrr83egskzzf0u8eyzrudmly/0",
           "channel":"v4_subaccounts",
           "version":"2.1.0",
           "contents":{
              "orders":[
                 {
                    "id":"80133551-6d61-573b-9788-c1488e11027a",
                    "subaccountId":"8db381df-80e1-5a74-9d70-bb40d98f5375",
                    "clientId":"659473483",
                    "clobPairId":"1",
                    "side":"BUY",
                    "size":"1",
                    "totalOptimisticFilled":"0",
                    "price":"1",
                    "type":"LIMIT",
                    "status":"BEST_EFFORT_CANCELED",
                    "timeInForce":"IOC",
                    "postOnly":false,
                    "reduceOnly":false,
                    "orderFlags":"0",
                    "goodTilBlock":"16950",
                    "ticker":"ETH-USD",
                    "removalReason":"IMMEDIATE_OR_CANCEL_WOULD_REST_ON_BOOK"
                 }
              ]
           }
        }
    """.trimIndent()

    internal val v4_accounts_received_for_calculation = """
        {
            "totalTradingRewards": "2800.8",
            "subaccounts": [{
                 "address":"cosmos1jtpspgllck9z4ghkqhupum35q55xt99sg3guxn",
                 "subaccountNumber":0,
                 "openPerpetualPositions":{
                    "ETH-USD":{
                       "market":"ETH-USD",
                       "status":"OPEN",
                       "side":"LONG",
                       "size":"1.0",
                       "maxSize":"1.0",
                       "entryPrice":"1000.0",
                       "createdAt":"2022-12-11T17:29:39.792Z",
                       "createdAtHeight":"45",
                       "closedAt":null,
                       "sumOpen":"1000.0",
                       "netFunding":"0"
                    }
                 },
                "assetPositions":{
                    "USDC":{
                       "symbol":"USDC",
                       "side":"SHORT",
                       "size":"900",
                       "assetId":"0"
                    }
                },
        		"marginEnabled": true,
                "tradingRewards": [
                    {
                        "tradingReward": "0.02",
                        "createdAtHeight": "2422",
                        "createdAt": "2023-08-09T20:00:00.000Z"
                    },
                    {
                        "tradingReward": "0.01",
                        "createdAtHeight": "2500",
                        "createdAt": "2023-08-09T20:10:00.000Z"
                    }
                ]
            }]
        }
    """.trimIndent()

    internal val v4_accounts_received_for_calculation_2 = """
    {
        "totalTradingRewards": "2800.8",
        "subaccounts": [{
             "address":"cosmos1jtpspgllck9z4ghkqhupum35q55xt99sg3guxn",
             "subaccountNumber":0,
             "openPerpetualPositions":{
                "ETH-USD":{
                   "market":"ETH-USD",
                   "status":"OPEN",
                   "side":"LONG",
                   "size":"1.0",
                   "maxSize":"1.0",
                   "entryPrice":"1000.0",
                   "createdAt":"2022-12-11T17:29:39.792Z",
                   "createdAtHeight":"45",
                   "closedAt":null,
                   "sumOpen":"1000.0",
                   "netFunding":"0"
                },
                "BTC-USD":{
                    "market":"BTC-USD",
                    "status":"OPEN",
                    "side":"LONG",
                    "size":"1.0",
                    "maxSize":"1.0",
                    "entryPrice":"34996.0",
                    "createdAt":"2022-12-12T17:29:39.792Z",
                    "createdAtHeight":"85",
                    "closedAt":null,
                    "sumOpen":"1000.0",
                    "netFunding":"0"
                 }
             },
            "assetPositions":{
                "USDC":{
                   "symbol":"USDC",
                   "side":"SHORT",
                   "size":"900",
                   "assetId":"0"
                }
            },
            "marginEnabled": true,
            "tradingRewards": [
                {
                    "tradingReward": "0.02",
                    "createdAtHeight": "2422",
                    "createdAt": "2023-08-09T20:00:00.000Z"
                },
                {
                    "tradingReward": "0.01",
                    "createdAtHeight": "2500",
                    "createdAt": "2023-08-09T20:10:00.000Z"
                }
            ]
        }]
    }
    """.trimIndent()

    internal val v4_subscribed_for_calculation = """
        {
           "type":"subscribed",
           "connection_id":"d8caff8c-0ee8-4eb0-b124-20c2d3d956ba",
           "message_id":5,
           "channel":"v4_subaccounts",
           "id":"cosmos1jtpspgllck9z4ghkqhupum35q55xt99sg3guxn/0",
           "contents":{
              "subaccount":{
                 "address":"cosmos1jtpspgllck9z4ghkqhupum35q55xt99sg3guxn",
                 "subaccountNumber":0,
                 "openPerpetualPositions":{
                    "ETH-USD":{
                       "market":"ETH-USD",
                       "status":"OPEN",
                       "side":"SHORT",
                       "size":"1.0",
                       "maxSize":"1.0",
                       "entryPrice":"1000.0",
                       "createdAt":"2022-12-11T17:29:39.792Z",
                       "createdAtHeight":"45",
                       "closedAt":null,
                       "sumOpen":"1000.0",
                       "netFunding":"0"
                    }
                 },
                "assetPositions":{
                    "USDC":{
                       "symbol":"USDC",
                       "side":"LONG",
                       "size":"1100",
                       "assetId":"0"
                    }
                },
                 "marginEnabled":true,
                "tradingRewards": [
                    {
                        "tradingReward": "0.01",
                        "createdAtHeight": "2501",
                        "createdAt": "2023-08-09T20:11:00.000Z"
                    }
                ]
              }
           }
        }
    """.trimIndent()

    internal val v4_subscribed_for_orders_sorting = """
        {
           "type":"subscribed",
           "connection_id":"30f1f2f5-939b-401b-afaa-76d4ea4f815f",
           "message_id":2,
           "channel":"v4_subaccounts",
           "id":"dydx1mffenzm82q8e0v9fu5t53e5wdxu8zvmaempqg2/0",
           "contents":{
              "subaccount":{
                 "address":"dydx1mffenzm82q8e0v9fu5t53e5wdxu8zvmaempqg2",
                 "subaccountNumber":0,
                 "equity":"998.560616",
                 "freeCollateral":"988.926234",
                 "openPerpetualPositions":{
                    "ETH-USD":{
                       "market":"ETH-USD",
                       "status":"OPEN",
                       "side":"LONG",
                       "size":"0.1",
                       "maxSize":"0.1",
                       "entryPrice":"1940.300000",
                       "exitPrice":null,
                       "realizedPnl":"0.000000",
                       "unrealizedPnl":"-1.342369",
                       "createdAt":"2023-06-30T17:27:07.619Z",
                       "createdAtHeight":"108545",
                       "closedAt":null,
                       "sumOpen":"0.1",
                       "sumClose":"0",
                       "netFunding":"0.000000"
                    }
                 },
                 "assetPositions":{
                    "USDC":{
                       "size":"805.872985",
                       "symbol":"USDC",
                       "side":"LONG",
                       "assetId":"0"
                    }
                 },
                 "marginEnabled":true
              },
              "orders":[
                 {
                    "id":"d9036795-a5f9-5537-9aec-fe89a47d274f",
                    "subaccountId":"bdaa4811-0f78-5a59-a453-5a90663b66c2",
                    "clientId":"297485732",
                    "clobPairId":"1",
                    "side":"BUY",
                    "size":"0.2",
                    "price":"2020.7",
                    "status":"BEST_EFFORT_OPENED",
                    "type":"LIMIT",
                    "timeInForce":"IOC",
                    "postOnly":false,
                    "reduceOnly":false,
                    "orderFlags":"0",
                    "goodTilBlock":"110442",
                    "ticker":"ETH-USD",
                    "clientMetadata":"1"
                 },
                 {
                    "id":"276506fc-17ef-5342-bbfb-816052d440be",
                    "subaccountId":"bdaa4811-0f78-5a59-a453-5a90663b66c2",
                    "clientId":"593060931",
                    "clobPairId":"1",
                    "side":"BUY",
                    "size":"0.1",
                    "totalFilled":"0",
                    "price":"1200",
                    "type":"LIMIT",
                    "status":"OPEN",
                    "timeInForce":"GTT",
                    "reduceOnly":false,
                    "orderFlags":"64",
                    "goodTilBlockTime":"2023-07-28T17:26:25.000Z",
                    "createdAtHeight":"108556",
                    "clientMetadata":"0",
                    "postOnly":false,
                    "ticker":"ETH-USD"
                 },
                 {
                    "id":"c101eeca-eb74-54d4-ba08-fde22ad15545",
                    "subaccountId":"bdaa4811-0f78-5a59-a453-5a90663b66c2",
                    "clientId":"379523559",
                    "clobPairId":"1",
                    "side":"BUY",
                    "size":"0.1",
                    "totalFilled":"0",
                    "price":"1100",
                    "type":"LIMIT",
                    "status":"OPEN",
                    "timeInForce":"GTT",
                    "reduceOnly":false,
                    "orderFlags":"64",
                    "goodTilBlockTime":"2023-07-28T17:25:50.000Z",
                    "createdAtHeight":"108534",
                    "clientMetadata":"0",
                    "postOnly":false,
                    "ticker":"ETH-USD"
                 },
                 {
                     "id": "d11c89ad-ee7a-51a8-84dd-4c164bf29960",
                     "subaccountId": "d7cff466-fd84-5dec-81ce-b89b47cfc342",
                     "clientId": "2059737429",
                     "clobPairId": "1",
                     "side": "BUY",
                     "size": "0.01",
                     "price": "3479.9",
                     "status": "BEST_EFFORT_OPENED",
                     "type": "LIMIT",
                     "timeInForce": "IOC",
                     "postOnly": false,
                     "reduceOnly": false,
                     "orderFlags": "0",
                     "goodTilBlock": "9354485",
                     "ticker": "ETH-USD",
                     "clientMetadata": "0"
                 },
                 {
                     "id": "9f06b7e1-5e7f-578a-a1a4-8c4a5635e7db",
                     "subaccountId": "d7cff466-fd84-5dec-81ce-b89b47cfc342",
                     "clientId": "652702815",
                     "clobPairId": "1",
                     "side": "BUY",
                     "size": "0.02",
                     "price": "3514.6",
                     "status": "BEST_EFFORT_OPENED",
                     "type": "LIMIT",
                     "timeInForce": "IOC",
                     "postOnly": false,
                     "reduceOnly": false,
                     "orderFlags": "0",
                     "goodTilBlock": "9354487",
                     "ticker": "ETH-USD",
                     "clientMetadata": "1"
                 }
              ]
           }
        }
    """.trimIndent()

    internal val v4_batched = """
        {
           "type":"channel_batch_data",
           "connection_id":"aaf8f307-c8d9-4273-9df6-1418e3940ac1",
           "message_id":3,
           "id":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art/0",
           "channel":"v4_subaccounts",
           "version":"2.2.0",
           "contents":[
              {
                 "perpetualPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"675ee00d-e2a4-554c-bc14-327e3a05d715",
                       "market":"ETH-USD",
                       "side":"SHORT",
                       "status":"CLOSED",
                       "size":"0",
                       "maxSize":"-0.01",
                       "netFunding":"0",
                       "entryPrice":"1878.2",
                       "exitPrice":null,
                       "sumOpen":"0.01",
                       "sumClose":"0",
                       "realizedPnl":"0.000000",
                       "unrealizedPnl":"0.000000"
                    }
                 ],
                 "assetPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"430f172a-8662-5b95-816c-6372d3ffd336",
                       "assetId":"0",
                       "symbol":"USDC",
                       "side":"LONG",
                       "size":"1999.989222"
                    }
                 ]
              },
              {
                 "fills":[
                    {
                       "id":"1275aabc-f617-585d-a736-6bfbcf28f275",
                       "side":"BUY",
                       "size":"0.01",
                       "type":"LIMIT",
                       "price":"1877.4",
                       "eventId":"0001781f0000000200000002",
                       "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "createdAt":"2023-07-07T17:20:10.369Z",
                       "liquidity":"TAKER",
                       "clobPairId":"1",
                       "quoteAmount":"18.774",
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "createdAtHeight":"96287",
                       "transactionHash":"49AFD6A9FA9D4F973F2CF764FD0A297358F2C3B557252C51E9D4E8794543E1B2",
                       "ticker":"ETH-USD"
                    }
                 ],
                 "orders":[
                    {
                       "id":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "side":"BUY",
                       "size":"0.1",
                       "type":"LIMIT",
                       "price":"10000",
                       "status":"OPEN",
                       "clientId":"9000717",
                       "clobPairId":"1",
                       "orderFlags":"64",
                       "reduceOnly":false,
                       "timeInForce":"GTT",
                       "totalFilled":"0.01",
                       "goodTilBlock":null,
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "clientMetadata":"0",
                       "createdAtHeight":"96286",
                       "goodTilBlockTime":"2023-07-07T17:21:09.000Z",
                       "postOnly":false,
                       "ticker":"ETH-USD"
                    }
                 ]
              },
              {
                 "perpetualPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"2bc9c966-cbd6-5a04-b35c-02dc7b06d671",
                       "market":"ETH-USD",
                       "side":"LONG",
                       "status":"OPEN",
                       "size":"0.01",
                       "maxSize":"0.01",
                       "netFunding":"0",
                       "entryPrice":"0",
                       "exitPrice":null,
                       "sumOpen":"0",
                       "sumClose":"0",
                       "realizedPnl":"0.000000",
                       "unrealizedPnl":"18.714708"
                    }
                 ],
                 "assetPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"430f172a-8662-5b95-816c-6372d3ffd336",
                       "assetId":"0",
                       "symbol":"USDC",
                       "side":"LONG",
                       "size":"1981.204835"
                    }
                 ]
              },
              {
                 "fills":[
                    {
                       "id":"012f27d8-34d9-532d-b3ea-0093c70d7204",
                       "side":"BUY",
                       "size":"0.01",
                       "type":"LIMIT",
                       "price":"1877.5",
                       "eventId":"0001781f0000000200000005",
                       "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "createdAt":"2023-07-07T17:20:10.369Z",
                       "liquidity":"TAKER",
                       "clobPairId":"1",
                       "quoteAmount":"18.775",
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "createdAtHeight":"96287",
                       "transactionHash":"49AFD6A9FA9D4F973F2CF764FD0A297358F2C3B557252C51E9D4E8794543E1B2",
                       "ticker":"ETH-USD"
                    }
                 ],
                 "orders":[
                    {
                       "id":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "side":"BUY",
                       "size":"0.1",
                       "type":"LIMIT",
                       "price":"10000",
                       "status":"OPEN",
                       "clientId":"9000717",
                       "clobPairId":"1",
                       "orderFlags":"64",
                       "reduceOnly":false,
                       "timeInForce":"GTT",
                       "totalFilled":"0.02",
                       "goodTilBlock":null,
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "clientMetadata":"0",
                       "createdAtHeight":"96286",
                       "goodTilBlockTime":"2023-07-07T17:21:09.000Z",
                       "postOnly":false,
                       "ticker":"ETH-USD"
                    }
                 ]
              },
              {
                 "perpetualPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"2bc9c966-cbd6-5a04-b35c-02dc7b06d671",
                       "market":"ETH-USD",
                       "side":"LONG",
                       "status":"OPEN",
                       "size":"0.02",
                       "maxSize":"0.02",
                       "netFunding":"0",
                       "entryPrice":"0",
                       "exitPrice":null,
                       "sumOpen":"0",
                       "sumClose":"0",
                       "realizedPnl":"0.000000",
                       "unrealizedPnl":"37.429416"
                    }
                 ],
                 "assetPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"430f172a-8662-5b95-816c-6372d3ffd336",
                       "assetId":"0",
                       "symbol":"USDC",
                       "side":"LONG",
                       "size":"1962.418447"
                    }
                 ]
              },
              {
                 "fills":[
                    {
                       "id":"feb8fd0c-5001-5d40-8d60-7b8e17d4080f",
                       "side":"BUY",
                       "size":"0.01",
                       "type":"LIMIT",
                       "price":"1877.7",
                       "eventId":"0001781f0000000200000008",
                       "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "createdAt":"2023-07-07T17:20:10.369Z",
                       "liquidity":"TAKER",
                       "clobPairId":"1",
                       "quoteAmount":"18.777",
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "createdAtHeight":"96287",
                       "transactionHash":"49AFD6A9FA9D4F973F2CF764FD0A297358F2C3B557252C51E9D4E8794543E1B2",
                       "ticker":"ETH-USD"
                    }
                 ],
                 "orders":[
                    {
                       "id":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "side":"BUY",
                       "size":"0.1",
                       "type":"LIMIT",
                       "price":"10000",
                       "status":"OPEN",
                       "clientId":"9000717",
                       "clobPairId":"1",
                       "orderFlags":"64",
                       "reduceOnly":false,
                       "timeInForce":"GTT",
                       "totalFilled":"0.03",
                       "goodTilBlock":null,
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "clientMetadata":"0",
                       "createdAtHeight":"96286",
                       "goodTilBlockTime":"2023-07-07T17:21:09.000Z",
                       "postOnly":false,
                       "ticker":"ETH-USD"
                    }
                 ]
              },
              {
                 "perpetualPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"2bc9c966-cbd6-5a04-b35c-02dc7b06d671",
                       "market":"ETH-USD",
                       "side":"LONG",
                       "status":"OPEN",
                       "size":"0.03",
                       "maxSize":"0.03",
                       "netFunding":"0",
                       "entryPrice":"0",
                       "exitPrice":null,
                       "sumOpen":"0",
                       "sumClose":"0",
                       "realizedPnl":"0.000000",
                       "unrealizedPnl":"56.144124"
                    }
                 ],
                 "assetPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"430f172a-8662-5b95-816c-6372d3ffd336",
                       "assetId":"0",
                       "symbol":"USDC",
                       "side":"LONG",
                       "size":"1943.632059"
                    }
                 ]
              },
              {
                 "fills":[
                    {
                       "id":"623be8bd-d5a1-5366-a073-7f0b1b873f6d",
                       "side":"BUY",
                       "size":"0.01",
                       "type":"LIMIT",
                       "price":"1877.7",
                       "eventId":"0001781f000000020000000b",
                       "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "createdAt":"2023-07-07T17:20:10.369Z",
                       "liquidity":"TAKER",
                       "clobPairId":"1",
                       "quoteAmount":"18.777",
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "createdAtHeight":"96287",
                       "transactionHash":"49AFD6A9FA9D4F973F2CF764FD0A297358F2C3B557252C51E9D4E8794543E1B2",
                       "ticker":"ETH-USD"
                    }
                 ],
                 "orders":[
                    {
                       "id":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "side":"BUY",
                       "size":"0.1",
                       "type":"LIMIT",
                       "price":"10000",
                       "status":"OPEN",
                       "clientId":"9000717",
                       "clobPairId":"1",
                       "orderFlags":"64",
                       "reduceOnly":false,
                       "timeInForce":"GTT",
                       "totalFilled":"0.04",
                       "goodTilBlock":null,
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "clientMetadata":"0",
                       "createdAtHeight":"96286",
                       "goodTilBlockTime":"2023-07-07T17:21:09.000Z",
                       "postOnly":false,
                       "ticker":"ETH-USD"
                    }
                 ]
              },
              {
                 "perpetualPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"2bc9c966-cbd6-5a04-b35c-02dc7b06d671",
                       "market":"ETH-USD",
                       "side":"LONG",
                       "status":"OPEN",
                       "size":"0.04",
                       "maxSize":"0.04",
                       "netFunding":"0",
                       "entryPrice":"1877.7",
                       "exitPrice":null,
                       "sumOpen":"0.01",
                       "sumClose":"0",
                       "realizedPnl":"0.000000",
                       "unrealizedPnl":"-0.249169"
                    }
                 ],
                 "assetPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"430f172a-8662-5b95-816c-6372d3ffd336",
                       "assetId":"0",
                       "symbol":"USDC",
                       "side":"LONG",
                       "size":"1924.84367"
                    }
                 ]
              },
              {
                 "fills":[
                    {
                       "id":"595bcf25-423e-5bec-a395-3e4524d3d2e8",
                       "side":"BUY",
                       "size":"0.01",
                       "type":"LIMIT",
                       "price":"1877.9",
                       "eventId":"0001781f000000020000000e",
                       "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "createdAt":"2023-07-07T17:20:10.369Z",
                       "liquidity":"TAKER",
                       "clobPairId":"1",
                       "quoteAmount":"18.779",
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "createdAtHeight":"96287",
                       "transactionHash":"49AFD6A9FA9D4F973F2CF764FD0A297358F2C3B557252C51E9D4E8794543E1B2",
                       "ticker":"ETH-USD"
                    }
                 ],
                 "orders":[
                    {
                       "id":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "side":"BUY",
                       "size":"0.1",
                       "type":"LIMIT",
                       "price":"10000",
                       "status":"OPEN",
                       "clientId":"9000717",
                       "clobPairId":"1",
                       "orderFlags":"64",
                       "reduceOnly":false,
                       "timeInForce":"GTT",
                       "totalFilled":"0.05",
                       "goodTilBlock":null,
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "clientMetadata":"0",
                       "createdAtHeight":"96286",
                       "goodTilBlockTime":"2023-07-07T17:21:09.000Z",
                       "postOnly":false,
                       "ticker":"ETH-USD"
                    }
                 ]
              },
              {
                 "perpetualPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"2bc9c966-cbd6-5a04-b35c-02dc7b06d671",
                       "market":"ETH-USD",
                       "side":"LONG",
                       "status":"OPEN",
                       "size":"0.05",
                       "maxSize":"0.05",
                       "netFunding":"0",
                       "entryPrice":"1877.8",
                       "exitPrice":null,
                       "sumOpen":"0.02",
                       "sumClose":"0",
                       "realizedPnl":"0.000000",
                       "unrealizedPnl":"-0.316461"
                    }
                 ],
                 "assetPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"430f172a-8662-5b95-816c-6372d3ffd336",
                       "assetId":"0",
                       "symbol":"USDC",
                       "side":"LONG",
                       "size":"1906.05428"
                    }
                 ]
              },
              {
                 "fills":[
                    {
                       "id":"180c2462-eb3b-5985-a702-32c503462a37",
                       "side":"BUY",
                       "size":"0.01",
                       "type":"LIMIT",
                       "price":"1878",
                       "eventId":"0001781f0000000200000011",
                       "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "createdAt":"2023-07-07T17:20:10.369Z",
                       "liquidity":"TAKER",
                       "clobPairId":"1",
                       "quoteAmount":"18.78",
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "createdAtHeight":"96287",
                       "transactionHash":"49AFD6A9FA9D4F973F2CF764FD0A297358F2C3B557252C51E9D4E8794543E1B2",
                       "ticker":"ETH-USD"
                    }
                 ],
                 "orders":[
                    {
                       "id":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "side":"BUY",
                       "size":"0.1",
                       "type":"LIMIT",
                       "price":"10000",
                       "status":"OPEN",
                       "clientId":"9000717",
                       "clobPairId":"1",
                       "orderFlags":"64",
                       "reduceOnly":false,
                       "timeInForce":"GTT",
                       "totalFilled":"0.06",
                       "goodTilBlock":null,
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "clientMetadata":"0",
                       "createdAtHeight":"96286",
                       "goodTilBlockTime":"2023-07-07T17:21:09.000Z",
                       "postOnly":false,
                       "ticker":"ETH-USD"
                    }
                 ]
              },
              {
                 "perpetualPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"2bc9c966-cbd6-5a04-b35c-02dc7b06d671",
                       "market":"ETH-USD",
                       "side":"LONG",
                       "status":"OPEN",
                       "size":"0.06",
                       "maxSize":"0.06",
                       "netFunding":"0",
                       "entryPrice":"1877.86666666666666666667",
                       "exitPrice":null,
                       "sumOpen":"0.03",
                       "sumClose":"0",
                       "realizedPnl":"0.000000",
                       "unrealizedPnl":"-0.383753"
                    }
                 ],
                 "assetPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"430f172a-8662-5b95-816c-6372d3ffd336",
                       "assetId":"0",
                       "symbol":"USDC",
                       "side":"LONG",
                       "size":"1887.261889"
                    }
                 ]
              },
              {
                 "fills":[
                    {
                       "id":"c7a2c673-66f4-5fe3-92f8-6dae7e5b1bcd",
                       "side":"BUY",
                       "size":"0.01",
                       "type":"LIMIT",
                       "price":"1878.3",
                       "eventId":"0001781f0000000200000014",
                       "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "createdAt":"2023-07-07T17:20:10.369Z",
                       "liquidity":"TAKER",
                       "clobPairId":"1",
                       "quoteAmount":"18.783",
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "createdAtHeight":"96287",
                       "transactionHash":"49AFD6A9FA9D4F973F2CF764FD0A297358F2C3B557252C51E9D4E8794543E1B2",
                       "ticker":"ETH-USD"
                    }
                 ],
                 "orders":[
                    {
                       "id":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "side":"BUY",
                       "size":"0.1",
                       "type":"LIMIT",
                       "price":"10000",
                       "status":"OPEN",
                       "clientId":"9000717",
                       "clobPairId":"1",
                       "orderFlags":"64",
                       "reduceOnly":false,
                       "timeInForce":"GTT",
                       "totalFilled":"0.07",
                       "goodTilBlock":null,
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "clientMetadata":"0",
                       "createdAtHeight":"96286",
                       "goodTilBlockTime":"2023-07-07T17:21:09.000Z",
                       "postOnly":false,
                       "ticker":"ETH-USD"
                    }
                 ]
              },
              {
                 "perpetualPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"2bc9c966-cbd6-5a04-b35c-02dc7b06d671",
                       "market":"ETH-USD",
                       "side":"LONG",
                       "status":"OPEN",
                       "size":"0.07",
                       "maxSize":"0.07",
                       "netFunding":"0",
                       "entryPrice":"1877.975",
                       "exitPrice":null,
                       "sumOpen":"0.04",
                       "sumClose":"0",
                       "realizedPnl":"0.000000",
                       "unrealizedPnl":"-0.455295"
                    }
                 ],
                 "assetPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"430f172a-8662-5b95-816c-6372d3ffd336",
                       "assetId":"0",
                       "symbol":"USDC",
                       "side":"LONG",
                       "size":"1868.467497"
                    }
                 ]
              },
              {
                 "fills":[
                    {
                       "id":"140fed65-77d9-525e-9d22-3ae35c4c05a7",
                       "side":"BUY",
                       "size":"0.01",
                       "type":"LIMIT",
                       "price":"1878.5",
                       "eventId":"0001781f0000000200000017",
                       "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "createdAt":"2023-07-07T17:20:10.369Z",
                       "liquidity":"TAKER",
                       "clobPairId":"1",
                       "quoteAmount":"18.785",
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "createdAtHeight":"96287",
                       "transactionHash":"49AFD6A9FA9D4F973F2CF764FD0A297358F2C3B557252C51E9D4E8794543E1B2",
                       "ticker":"ETH-USD"
                    }
                 ],
                 "orders":[
                    {
                       "id":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "side":"BUY",
                       "size":"0.1",
                       "type":"LIMIT",
                       "price":"10000",
                       "status":"OPEN",
                       "clientId":"9000717",
                       "clobPairId":"1",
                       "orderFlags":"64",
                       "reduceOnly":false,
                       "timeInForce":"GTT",
                       "totalFilled":"0.08",
                       "goodTilBlock":null,
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "clientMetadata":"0",
                       "createdAtHeight":"96286",
                       "goodTilBlockTime":"2023-07-07T17:21:09.000Z",
                       "postOnly":false,
                       "ticker":"ETH-USD"
                    }
                 ]
              },
              {
                 "perpetualPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"2bc9c966-cbd6-5a04-b35c-02dc7b06d671",
                       "market":"ETH-USD",
                       "side":"LONG",
                       "status":"OPEN",
                       "size":"0.08",
                       "maxSize":"0.08",
                       "netFunding":"0",
                       "entryPrice":"1878.08",
                       "exitPrice":null,
                       "sumOpen":"0.05",
                       "sumClose":"0",
                       "realizedPnl":"0.000000",
                       "unrealizedPnl":"-0.528737"
                    }
                 ],
                 "assetPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"430f172a-8662-5b95-816c-6372d3ffd336",
                       "assetId":"0",
                       "symbol":"USDC",
                       "side":"LONG",
                       "size":"1849.672104"
                    }
                 ]
              },
              {
                 "fills":[
                    {
                       "id":"7b8dabee-1249-51fc-aa02-fa00a5d85e39",
                       "side":"BUY",
                       "size":"0.01",
                       "type":"LIMIT",
                       "price":"1878.6",
                       "eventId":"0001781f000000020000001a",
                       "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "createdAt":"2023-07-07T17:20:10.369Z",
                       "liquidity":"TAKER",
                       "clobPairId":"1",
                       "quoteAmount":"18.786",
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "createdAtHeight":"96287",
                       "transactionHash":"49AFD6A9FA9D4F973F2CF764FD0A297358F2C3B557252C51E9D4E8794543E1B2",
                       "ticker":"ETH-USD"
                    }
                 ],
                 "orders":[
                    {
                       "id":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "side":"BUY",
                       "size":"0.1",
                       "type":"LIMIT",
                       "price":"10000",
                       "status":"OPEN",
                       "clientId":"9000717",
                       "clobPairId":"1",
                       "orderFlags":"64",
                       "reduceOnly":false,
                       "timeInForce":"GTT",
                       "totalFilled":"0.09",
                       "goodTilBlock":null,
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "clientMetadata":"0",
                       "createdAtHeight":"96286",
                       "goodTilBlockTime":"2023-07-07T17:21:09.000Z",
                       "postOnly":false,
                       "ticker":"ETH-USD"
                    }
                 ]
              },
              {
                 "perpetualPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"2bc9c966-cbd6-5a04-b35c-02dc7b06d671",
                       "market":"ETH-USD",
                       "side":"LONG",
                       "status":"OPEN",
                       "size":"0.09",
                       "maxSize":"0.09",
                       "netFunding":"0",
                       "entryPrice":"1878.16666666666666666667",
                       "exitPrice":null,
                       "sumOpen":"0.06",
                       "sumClose":"0",
                       "realizedPnl":"0.000000",
                       "unrealizedPnl":"-0.602629"
                    }
                 ],
                 "assetPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"430f172a-8662-5b95-816c-6372d3ffd336",
                       "assetId":"0",
                       "symbol":"USDC",
                       "side":"LONG",
                       "size":"1830.87371"
                    }
                 ]
              },
              {
                 "fills":[
                    {
                       "id":"cc8c89e7-2975-5387-b6da-a38609761432",
                       "side":"BUY",
                       "size":"0.01",
                       "type":"LIMIT",
                       "price":"1878.9",
                       "eventId":"0001781f000000020000001d",
                       "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "createdAt":"2023-07-07T17:20:10.369Z",
                       "liquidity":"TAKER",
                       "clobPairId":"1",
                       "quoteAmount":"18.789",
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "createdAtHeight":"96287",
                       "transactionHash":"49AFD6A9FA9D4F973F2CF764FD0A297358F2C3B557252C51E9D4E8794543E1B2",
                       "ticker":"ETH-USD"
                    }
                 ],
                 "orders":[
                    {
                       "id":"1118c548-1715-5a72-9c41-f4388518c6e2",
                       "side":"BUY",
                       "size":"0.1",
                       "type":"LIMIT",
                       "price":"10000",
                       "status":"OPEN",
                       "clientId":"9000717",
                       "clobPairId":"1",
                       "orderFlags":"64",
                       "reduceOnly":false,
                       "timeInForce":"GTT",
                       "totalFilled":"0.009",
                       "goodTilBlock":null,
                       "subaccountId":"8586bcf6-1f58-5ec9-a0bc-e53db273e7b0",
                       "clientMetadata":"0",
                       "createdAtHeight":"96286",
                       "goodTilBlockTime":"2023-07-07T17:21:09.000Z",
                       "postOnly":false,
                       "ticker":"ETH-USD"
                    }
                 ]
              }
           ]
        }
    """.trimIndent()

    internal val v4_parent_subaccounts_partially_filled_and_canceled_orders = """
        {
          "type": "channel_batch_data",
          "connection_id": "4eb5c0e7-de17-4a5c-beaa-17b674293b69",
          "message_id": 100,
          "id":"dydx1v7xhjjgyyhcjtetrm6f2g0r5gcax9zs380yv6q/0",
          "channel": "v4_parent_subaccounts",
          "version": "3.0.0",
          "contents": [
            {
              "fills": [
                    {
                      "id": "7ebd919a-33e0-56ba-bf43-f0768eed90ab",
                      "fee": "0.01668",
                      "side": "BUY",
                      "size": "16",
                      "type": "LIMIT",
                      "price": "2.085",
                      "eventId": "00e0249e0000000200000002",
                      "orderId": "a4586c75-c3f5-5bf5-877a-b3f2c8ff32a7",
                      "createdAt": "2024-06-17T16:29:01.795Z",
                      "liquidity": "TAKER",
                      "clobPairId": "26",
                      "quoteAmount": "33.36",
                      "subaccountId": "e470a747-3aa0-543e-aafa-0bd27d568901",
                      "clientMetadata": "0",
                      "createdAtHeight": "14689438",
                      "transactionHash": "AEFB668CC357F5A1DCCE3A3B34CB6ACC99406058CA296C91DE534BAED0511250",
                      "ticker": "LDO-USD"
                    },
                    {
                      "id": "aa394a06-478b-5c55-960e-f667a7871794",
                      "fee": "0.017152",
                      "side": "BUY",
                      "size": "16",
                      "type": "LIMIT",
                      "price": "2.144",
                      "eventId": "00e026ba0000000200000002",
                      "orderId": "3a8c6f8f-d8dd-54b5-a3a1-d318f586a80c",
                      "createdAt": "2024-06-17T16:40:57.007Z",
                      "liquidity": "TAKER",
                      "clobPairId": "26",
                      "quoteAmount": "34.304",
                      "subaccountId": "e470a747-3aa0-543e-aafa-0bd27d568901",
                      "clientMetadata": "0",
                      "createdAtHeight": "14689438",
                      "transactionHash": "E16EFED4E63D2D9BD4AAEC4F1C459EF04CB7320013DF5E4F5CCCFA98F8ABBCF6",
                      "ticker": "LDO-USD"
                    }
              ],
              "blockHeight": "14689438",
              "orders": [
                    {
                      "id": "a4586c75-c3f5-5bf5-877a-b3f2c8ff32a7",
                      "side": "BUY",
                      "size": "30",
                      "type": "LIMIT",
                      "price": "2.085",
                      "status": "OPEN",
                      "clientId": "1736188335",
                      "updatedAt": "2024-06-17T16:29:01.795Z",
                      "clobPairId": "26",
                      "orderFlags": "64",
                      "reduceOnly": false,
                      "timeInForce": "GTT",
                      "totalFilled": "16",
                      "goodTilBlock": null,
                      "subaccountId": "e470a747-3aa0-543e-aafa-0bd27d568901",
                      "triggerPrice": null,
                      "clientMetadata": "0",
                      "createdAtHeight": "14689437",
                      "updatedAtHeight": "14689438",
                      "goodTilBlockTime": "2024-07-15T16:28:56.000Z",
                      "postOnly": false,
                      "ticker": "LDO-USD"
                    },
                    {
                      "id": "3a8c6f8f-d8dd-54b5-a3a1-d318f586a80c",
                      "side": "BUY",
                      "size": "30",
                      "type": "LIMIT",
                      "price": "2.144",
                      "status": "CANCELED",
                      "clientId": "1138384266",
                      "updatedAt": "2024-06-17T16:40:57.007Z",
                      "clobPairId": "26",
                      "orderFlags": "0",
                      "reduceOnly": false,
                      "timeInForce": "IOC",
                      "totalFilled": "16",
                      "goodTilBlock": "14689446",
                      "subaccountId": "e470a747-3aa0-543e-aafa-0bd27d568901",
                      "triggerPrice": null,
                      "clientMetadata": "0",
                      "createdAtHeight": "14689437",
                      "updatedAtHeight": "14689438",
                      "goodTilBlockTime": null,
                      "postOnly": false,
                      "ticker": "LDO-USD"
                    }
              ]
            }
          ],
          "subaccountNumber": 0
        }
    """.trimIndent()

    internal val v4_parent_subaccounts_subscribed_with_trigger_orders_and_open_positions = """
        {
            "type": "subscribed",
            "connection_id": "30017867-db65-4e45-a5c2-554b7b7604e1",
            "message_id": 2,
            "channel": "v4_parent_subaccounts",
            "id": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm/0",
            "contents": {
                "subaccount": {
                    "address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                    "parentSubaccountNumber": 0,
                    "equity": "304.024595538",
                    "freeCollateral": "199.8930868412",
                    "childSubaccounts": [
                        {
                            "address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                            "subaccountNumber": 0,
                            "equity": "277.216100822",
                            "freeCollateral": "175.7565800968",
                            "openPerpetualPositions": {
                                "ETH-USD": {
                                    "market": "ETH-USD",
                                    "status": "OPEN",
                                    "side": "LONG",
                                    "size": "10",
                                    "maxSize": "10",
                                    "entryPrice": "2.078",
                                    "exitPrice": null,
                                    "realizedPnl": "0",
                                    "unrealizedPnl": "-0.55451457",
                                    "createdAt": "2024-06-18T14:52:26.909Z",
                                    "createdAtHeight": "14754316",
                                    "closedAt": null,
                                    "sumOpen": "10",
                                    "sumClose": "0",
                                    "netFunding": "0",
                                    "subaccountNumber": 0
                                },
                                "BTC-USD": {
                                    "market": "BTC-USD",
                                    "status": "OPEN",
                                    "side": "LONG",
                                    "size": "7.2",
                                    "maxSize": "7.2",
                                    "entryPrice": "149.94944444444444444444",
                                    "exitPrice": null,
                                    "realizedPnl": "0",
                                    "unrealizedPnl": "-105.491763607999999999968",
                                    "createdAt": "2024-06-12T00:45:53.637Z",
                                    "createdAtHeight": "14292826",
                                    "closedAt": null,
                                    "sumOpen": "7.2",
                                    "sumClose": "0",
                                    "netFunding": "0",
                                    "subaccountNumber": 0
                                },
                                "APE-USD": {
                                    "market": "APE-USD",
                                    "status": "OPEN",
                                    "side": "LONG",
                                    "size": "2",
                                    "maxSize": "2",
                                    "entryPrice": "13.385",
                                    "exitPrice": null,
                                    "realizedPnl": "0",
                                    "unrealizedPnl": "-0.050120284",
                                    "createdAt": "2024-06-18T14:58:11.667Z",
                                    "createdAtHeight": "14754597",
                                    "closedAt": null,
                                    "sumOpen": "2",
                                    "sumClose": "0",
                                    "netFunding": "0",
                                    "subaccountNumber": 128
                                }
                            }
                        }
                    ]
                },
                "orders": [
                    {
                        "id": "f581f56c-9f1b-54e0-97d6-5f934dd0eb67",
                        "subaccountId": "1849cc9b-24fa-51f8-842e-db6d50a1004a",
                        "clientId": "1567250430",
                        "clobPairId": "2",
                        "side": "SELL",
                        "size": "2",
                        "totalFilled": "0",
                        "price": "11.776",
                        "type": "TAKE_PROFIT",
                        "status": "UNTRIGGERED",
                        "timeInForce": "IOC",
                        "reduceOnly": true,
                        "orderFlags": "32",
                        "goodTilBlockTime": "2024-09-16T14:58:32.000Z",
                        "createdAtHeight": "14754610",
                        "clientMetadata": "1",
                        "triggerPrice": "14.72",
                        "updatedAt": "2024-06-18T14:58:31.445Z",
                        "updatedAtHeight": "14754610",
                        "postOnly": false,
                        "ticker": "APE-USD",
                        "subaccountNumber": 0
                    },
                    {
                        "id": "aeb40307-861a-52c1-9568-2a95468e8687",
                        "subaccountId": "d7cff466-fd84-5dec-81ce-b89b47cfc342",
                        "clientId": "1579898688",
                        "clobPairId": "26",
                        "side": "SELL",
                        "size": "10",
                        "totalFilled": "0",
                        "price": "1.496",
                        "type": "STOP_LIMIT",
                        "status": "UNTRIGGERED",
                        "timeInForce": "IOC",
                        "reduceOnly": true,
                        "orderFlags": "32",
                        "goodTilBlockTime": "2024-09-16T14:52:56.000Z",
                        "createdAtHeight": "14754340",
                        "clientMetadata": "1",
                        "triggerPrice": "1.871",
                        "updatedAt": "2024-06-18T14:52:55.687Z",
                        "updatedAtHeight": "14754340",
                        "postOnly": false,
                        "ticker": "ETH-USD",
                        "subaccountNumber": 0
                    },
                    {
                        "id": "832db39d-0397-5793-b8e6-5f87e6540bcc",
                        "subaccountId": "d7cff466-fd84-5dec-81ce-b89b47cfc342",
                        "clientId": "1440113412",
                        "clobPairId": "5",
                        "side": "SELL",
                        "size": "7.2",
                        "totalFilled": "0",
                        "price": "122.27",
                        "type": "TAKE_PROFIT",
                        "status": "UNTRIGGERED",
                        "timeInForce": "IOC",
                        "reduceOnly": true,
                        "orderFlags": "32",
                        "goodTilBlockTime": "2024-09-16T14:52:45.000Z",
                        "createdAtHeight": "14754332",
                        "clientMetadata": "1",
                        "triggerPrice": "152.84",
                        "updatedAt": "2024-06-18T14:52:45.635Z",
                        "updatedAtHeight": "14754332",
                        "postOnly": false,
                        "ticker": "BTC-USD",
                        "subaccountNumber": 0
                    }
                ],
                "blockHeight": "14754614"
            }
        }
    """.trimIndent()

    internal val v4_parent_subaccounts_batched_closed_and_flipped_positions = """
        {
          "type": "channel_batch_data",
          "connection_id": "30017867-db65-4e45-a5c2-554b7b7604e1",
          "message_id": 3,
          "id": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm/0",
          "channel": "v4_parent_subaccounts",
          "version": "3.0.0",
          "contents": [
            {
              "perpetualPositions": [
                {
                    "address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                    "subaccountNumber": 0,
                    "positionId": "a5946d78-fe73-5c3b-ad2f-88c07b3bf5be",
                    "market": "APE-USD",
                    "side": "LONG",
                    "status": "CLOSED",
                    "size": "0",
                    "maxSize": "2",
                    "netFunding": "0",
                    "entryPrice": "13.385",
                    "exitPrice": null,
                    "sumOpen": "2",
                    "sumClose": "0",
                    "realizedPnl": "0",
                    "unrealizedPnl": "0"
                },
                {
                  "address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                  "subaccountNumber": 0,
                  "positionId": "2fdb618b-289c-51a9-83ce-cdfe4c622d62",
                  "market": "ETH-USD",
                  "side": "LONG",
                  "status": "CLOSED",
                  "size": "0",
                  "maxSize": "10",
                  "netFunding": "0",
                  "entryPrice": "2.078",
                  "exitPrice": null,
                  "sumOpen": "10",
                  "sumClose": "0",
                  "realizedPnl": "0",
                  "unrealizedPnl": "0"
                },
                {
                  "address": "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                  "subaccountNumber": 0,
                  "positionId": "82cd3212-2f93-5df2-bb41-90822d024766",
                  "market": "ETH-USD",
                  "side": "SHORT",
                  "status": "OPEN",
                  "size": "-2",
                  "maxSize": "-2",
                  "netFunding": "0",
                  "entryPrice": "0",
                  "exitPrice": null,
                  "sumOpen": "0",
                  "sumClose": "0",
                  "realizedPnl": "0",
                  "unrealizedPnl": "-4.129295848"
                }
              ]
            }
          ]
        }
    """.trimIndent()

    internal val v4_position_closed = """
        {
           "type":"channel_batch_data",
           "connection_id":"aaf8f307-c8d9-4273-9df6-1418e3940ac1",
           "message_id":3,
           "id":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art/0",
           "channel":"v4_subaccounts",
           "version":"2.2.0",
           "contents":[
              {
                 "perpetualPositions":[
                    {
                       "address":"dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
                       "subaccountNumber":0,
                       "positionId":"2bc9c966-cbd6-5a04-b35c-02dc7b06d671",
                       "market":"ETH-USD",
                       "side":"LONG",
                       "status":"CLOSED",
                       "size":"0.00",
                       "maxSize":"0.00",
                       "netFunding":"0",
                       "entryPrice":"1878.08",
                       "exitPrice":null,
                       "sumOpen":"0.05",
                       "sumClose":"0",
                       "realizedPnl":"0.000000",
                       "unrealizedPnl":"0.0",
                       "createdAt":"2023-06-30T17:27:07.619Z",
                       "createdAtHeight":"108545",
                       "closedAt":"2023-07-30T17:27:07.619Z"
                    }
                 ]
              }
           ]
        }
    """.trimIndent()
}
