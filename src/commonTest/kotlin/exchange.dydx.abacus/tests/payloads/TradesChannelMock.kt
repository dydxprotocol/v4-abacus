package exchange.dydx.abacus.tests.payloads

@Suppress("PropertyName")
internal class TradesChannelMock {
    internal val subscribed = """
        {
          "type": "subscribed",
          "connection_id": "3936dbcc-fe3f-4598-ba07-fa8656c455b1",
          "message_id": 8,
          "channel": "v3_trades",
          "id": "ETH-USD",
          "contents": {
            "trades": [
              {
                "side": "BUY",
                "size": "0.01",
                "price": "1656.2",
                "createdAt": "2022-08-01T16:58:12.989Z",
                "liquidation": true
              },
              {
                "side": "SELL",
                "size": "0.01",
                "price": "1654.3",
                "createdAt": "2022-08-01T16:58:06.163Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "0.01",
                "price": "1656.4",
                "createdAt": "2022-08-01T16:50:35.297Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "0.01",
                "price": "1659.2",
                "createdAt": "2022-08-01T16:50:25.767Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "16.776",
                "price": "1659",
                "createdAt": "2022-08-01T16:46:08.438Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "30.988",
                "price": "1659",
                "createdAt": "2022-08-01T16:46:08.059Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "8.858",
                "price": "1657.9",
                "createdAt": "2022-08-01T16:45:31.776Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "2.911",
                "price": "1657.9",
                "createdAt": "2022-08-01T16:45:26.936Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "13.345",
                "price": "1657.6",
                "createdAt": "2022-08-01T16:45:26.936Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "10.537",
                "price": "1657.6",
                "createdAt": "2022-08-01T16:45:24.166Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "17.438",
                "price": "1655.5",
                "createdAt": "2022-08-01T16:44:47.864Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "1.897",
                "price": "1659.2",
                "createdAt": "2022-08-01T16:34:51.197Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "8.503",
                "price": "1659.2",
                "createdAt": "2022-08-01T16:34:50.975Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "3.229",
                "price": "1659.1",
                "createdAt": "2022-08-01T16:34:50.975Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "16.077",
                "price": "1659.1",
                "createdAt": "2022-08-01T16:34:47.388Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "11.729",
                "price": "1659.1",
                "createdAt": "2022-08-01T16:34:45.168Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "8.929",
                "price": "1658.9",
                "createdAt": "2022-08-01T16:34:39.578Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "15.715",
                "price": "1658.9",
                "createdAt": "2022-08-01T16:34:38.902Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "9.113",
                "price": "1658.9",
                "createdAt": "2022-08-01T16:34:38.902Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "2.607",
                "price": "1658.9",
                "createdAt": "2022-08-01T16:34:37.447Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "20.164",
                "price": "1658.8",
                "createdAt": "2022-08-01T16:34:37.447Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "2.953",
                "price": "1658.6",
                "createdAt": "2022-08-01T16:34:37.447Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "13.161",
                "price": "1658.6",
                "createdAt": "2022-08-01T16:34:34.881Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "0.157",
                "price": "1657",
                "createdAt": "2022-08-01T16:19:27.189Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "11.742",
                "price": "1658.8",
                "createdAt": "2022-08-01T16:16:10.014Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "20.698",
                "price": "1659.1",
                "createdAt": "2022-08-01T16:16:04.640Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "1.714",
                "price": "1659.1",
                "createdAt": "2022-08-01T16:16:04.640Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "13.215",
                "price": "1659.1",
                "createdAt": "2022-08-01T16:16:03.842Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "1.182",
                "price": "1659.1",
                "createdAt": "2022-08-01T16:15:48.783Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "4.268",
                "price": "1659.6",
                "createdAt": "2022-08-01T16:14:50.217Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "11.745",
                "price": "1659.6",
                "createdAt": "2022-08-01T16:14:45.264Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "13.571",
                "price": "1660",
                "createdAt": "2022-08-01T16:14:40.682Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "17.415",
                "price": "1660",
                "createdAt": "2022-08-01T16:14:33.302Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "11.716",
                "price": "1660.1",
                "createdAt": "2022-08-01T16:14:25.220Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "1.182",
                "price": "1663.5",
                "createdAt": "2022-08-01T16:13:37.481Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "1.232",
                "price": "1665.9",
                "createdAt": "2022-08-01T16:11:04.593Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "19.325",
                "price": "1665.9",
                "createdAt": "2022-08-01T16:11:04.237Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "10.331",
                "price": "1665.4",
                "createdAt": "2022-08-01T16:11:03.913Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "4.292",
                "price": "1663.1",
                "createdAt": "2022-08-01T16:08:19.523Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "19.272",
                "price": "1663",
                "createdAt": "2022-08-01T16:08:19.523Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "11.62",
                "price": "1663.1",
                "createdAt": "2022-08-01T16:08:00.620Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "1.987",
                "price": "1667.1",
                "createdAt": "2022-08-01T16:05:00.527Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "18.448",
                "price": "1667.1",
                "createdAt": "2022-08-01T16:04:59.693Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "5.056",
                "price": "1667.3",
                "createdAt": "2022-08-01T16:04:59.693Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "19.199",
                "price": "1667.3",
                "createdAt": "2022-08-01T16:04:57.347Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "11.497",
                "price": "1668",
                "createdAt": "2022-08-01T16:04:52.693Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "11.526",
                "price": "1675",
                "createdAt": "2022-08-01T15:47:37.482Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "24.228",
                "price": "1673.8",
                "createdAt": "2022-08-01T15:46:50.084Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "19.86",
                "price": "1672.8",
                "createdAt": "2022-08-01T15:46:39.407Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "15.99",
                "price": "1671.2",
                "createdAt": "2022-08-01T15:35:48.599Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "10.287",
                "price": "1672.9",
                "createdAt": "2022-08-01T15:31:19.466Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "6.238",
                "price": "1675.5",
                "createdAt": "2022-08-01T15:27:49.756Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "19.057",
                "price": "1675.5",
                "createdAt": "2022-08-01T15:27:45.975Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "10.187",
                "price": "1675.8",
                "createdAt": "2022-08-01T15:17:19.151Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "0.185",
                "price": "1675.8",
                "createdAt": "2022-08-01T15:17:19.151Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "7.308",
                "price": "1676",
                "createdAt": "2022-08-01T15:17:13.635Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "5.675",
                "price": "1676",
                "createdAt": "2022-08-01T15:17:12.352Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "11.048",
                "price": "1676",
                "createdAt": "2022-08-01T15:17:12.352Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "3.651",
                "price": "1676.1",
                "createdAt": "2022-08-01T15:17:08.609Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "21.567",
                "price": "1676.1",
                "createdAt": "2022-08-01T15:17:07.784Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "11.435",
                "price": "1679.8",
                "createdAt": "2022-08-01T15:15:57.645Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "16.812",
                "price": "1681.8",
                "createdAt": "2022-08-01T15:15:56.495Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "7.449",
                "price": "1686.4",
                "createdAt": "2022-08-01T15:01:39.654Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "15.894",
                "price": "1686.4",
                "createdAt": "2022-08-01T15:01:11.084Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "15.903",
                "price": "1686.8",
                "createdAt": "2022-08-01T15:00:50.060Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "5.718",
                "price": "1685.7",
                "createdAt": "2022-08-01T14:55:51.128Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "15.077",
                "price": "1686.1",
                "createdAt": "2022-08-01T14:55:51.128Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "8.046",
                "price": "1686.1",
                "createdAt": "2022-08-01T14:55:47.282Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "9.063",
                "price": "1686.3",
                "createdAt": "2022-08-01T14:55:47.282Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "10.838",
                "price": "1686.3",
                "createdAt": "2022-08-01T14:55:31.398Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "2.895",
                "price": "1686.2",
                "createdAt": "2022-08-01T14:53:21.555Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "13.095",
                "price": "1686.3",
                "createdAt": "2022-08-01T14:53:21.555Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "9.143",
                "price": "1683.8",
                "createdAt": "2022-08-01T14:47:56.577Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "9.098",
                "price": "1683.8",
                "createdAt": "2022-08-01T14:47:56.068Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "6.775",
                "price": "1683.9",
                "createdAt": "2022-08-01T14:47:56.068Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "17.293",
                "price": "1683.9",
                "createdAt": "2022-08-01T14:47:55.600Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "3.113",
                "price": "1683.9",
                "createdAt": "2022-08-01T14:47:55.600Z",
                "liquidation": false
              },
              {
                "side": "SELL",
                "size": "19.927",
                "price": "1683.9",
                "createdAt": "2022-08-01T14:47:55.330Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "13.033",
                "price": "1685.3",
                "createdAt": "2022-08-01T14:47:02.543Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "4.251",
                "price": "1685.3",
                "createdAt": "2022-08-01T14:47:01.742Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "21.938",
                "price": "1684.5",
                "createdAt": "2022-08-01T14:47:01.742Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "18.161",
                "price": "1684.5",
                "createdAt": "2022-08-01T14:46:57.854Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "0.099",
                "price": "1684.4",
                "createdAt": "2022-08-01T14:46:57.854Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "22.989",
                "price": "1684.4",
                "createdAt": "2022-08-01T14:46:55.321Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "1.185",
                "price": "1684.4",
                "createdAt": "2022-08-01T14:46:54.161Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "9.304",
                "price": "1684.1",
                "createdAt": "2022-08-01T14:46:54.161Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "20.399",
                "price": "1684.1",
                "createdAt": "2022-08-01T14:46:53.216Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "15.808",
                "price": "1683.9",
                "createdAt": "2022-08-01T14:46:50.956Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "10.525",
                "price": "1683.2",
                "createdAt": "2022-08-01T14:46:34.580Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "13.126",
                "price": "1683.2",
                "createdAt": "2022-08-01T14:46:30.758Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "14.263",
                "price": "1683.2",
                "createdAt": "2022-08-01T14:46:28.072Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "2.879",
                "price": "1683",
                "createdAt": "2022-08-01T14:46:28.072Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "15.84",
                "price": "1683",
                "createdAt": "2022-08-01T14:46:26.151Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "1.331",
                "price": "1683",
                "createdAt": "2022-08-01T14:46:26.145Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "18.413",
                "price": "1682.4",
                "createdAt": "2022-08-01T14:46:26.145Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "6.458",
                "price": "1682.3",
                "createdAt": "2022-08-01T14:46:26.145Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "10.399",
                "price": "1682.3",
                "createdAt": "2022-08-01T14:46:26.065Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "10.581",
                "price": "1682.2",
                "createdAt": "2022-08-01T14:46:26.065Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "11.209",
                "price": "1681.8",
                "createdAt": "2022-08-01T14:46:26.063Z",
                "liquidation": false
              },
              {
                "side": "BUY",
                "size": "11.957",
                "price": "1681.8",
                "createdAt": "2022-08-01T14:46:22.600Z",
                "liquidation": false
              }
            ]
          }
        }
    """.trimIndent()
    internal val channel_data = """
        {
          "type": "channel_data",
          "connection_id": "205e994e-676a-451a-af17-ba012781508b",
          "message_id": 129,
          "id": "ETH-USD",
          "channel": "v3_trades",
          "contents": {
            "trades": [
              {
                "size": "24.243",
                "side": "SELL",
                "price": "1645.7",
                "createdAt": "2022-08-01T17:05:28.592Z",
                "liquidation": false
              },
              {
                "size": "1.498",
                "side": "SELL",
                "price": "1645.5",
                "createdAt": "2022-08-01T17:05:28.592Z",
                "liquidation": false
              }
            ]
          }
        }
    """.trimIndent()

    internal val v4_subscribed = """
        {
           "type":"subscribed",
           "connection_id":"d8caff8c-0ee8-4eb0-b124-20c2d3d956ba",
           "message_id":3,
           "channel":"v4_trades",
           "id":"ETH-USD",
           "contents":{
              "trades":[
                 {
                    "id":"1",
                    "side":"SELL",
                    "size":"0.00095",
                    "price":"1255.98",
                    "liquidation": false,
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"2",
                    "side":"BUY",
                    "size":"0.000661",
                    "price":"1256.387",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"3",
                    "side":"SELL",
                    "size":"0.000598",
                    "price":"1256.087",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"4",
                    "side":"SELL",
                    "size":"0.000118",
                    "price":"1255.98",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"5",
                    "side":"SELL",
                    "size":"0.001356",
                    "price":"1256.005",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"6",
                    "side":"SELL",
                    "size":"0.126606",
                    "price":"1255.588",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"7",
                    "side":"BUY",
                    "size":"0.000038",
                    "price":"1256.39",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"8",
                    "side":"SELL",
                    "size":"1.585665",
                    "price":"1256.108",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"9",
                    "side":"SELL",
                    "size":"0.000268",
                    "price":"1255.98",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"10",
                    "side":"SELL",
                    "size":"0.000272",
                    "price":"1255.98",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"11",
                    "side":"SELL",
                    "size":"0.001137",
                    "price":"1255.602",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"12",
                    "side":"BUY",
                    "size":"0.00032",
                    "price":"1256.302",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"13",
                    "side":"SELL",
                    "size":"0.000874",
                    "price":"1255.502",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"14",
                    "side":"SELL",
                    "size":"0.001584",
                    "price":"1255.579",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"15",
                    "side":"BUY",
                    "size":"0.001026",
                    "price":"1256.327",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"16",
                    "side":"BUY",
                    "size":"0.000115",
                    "price":"1256.39",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"17",
                    "side":"SELL",
                    "size":"1.329001",
                    "price":"1255.528",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"18",
                    "side":"SELL",
                    "size":"0.000336",
                    "price":"1256.108",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"19",
                    "side":"SELL",
                    "size":"0.840569",
                    "price":"1255.98",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"20",
                    "side":"SELL",
                    "size":"0.000542",
                    "price":"1255.98",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"21",
                    "side":"BUY",
                    "size":"0.001137",
                    "price":"1255.694",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"22",
                    "side":"SELL",
                    "size":"0.000702",
                    "price":"1255.98",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"23",
                    "side":"BUY",
                    "size":"0.000306",
                    "price":"1256.302",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"24",
                    "side":"BUY",
                    "size":"0.000072",
                    "price":"1255.727",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"25",
                    "side":"BUY",
                    "size":"0.000956",
                    "price":"1255.85",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"26",
                    "side":"SELL",
                    "size":"0.000913",
                    "price":"1255.602",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"27",
                    "side":"BUY",
                    "size":"0.624335",
                    "price":"1256.306",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"28",
                    "side":"SELL",
                    "size":"0.144818",
                    "price":"1256.025",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"29",
                    "side":"BUY",
                    "size":"1.198397",
                    "price":"1256.391",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"30",
                    "side":"SELL",
                    "size":"0.000752",
                    "price":"1255.564",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"31",
                    "side":"BUY",
                    "size":"0.000036",
                    "price":"1256.306",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"32",
                    "side":"BUY",
                    "size":"1.69957",
                    "price":"1256.311",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"33",
                    "side":"BUY",
                    "size":"0.1011",
                    "price":"1256.334",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"34",
                    "side":"BUY",
                    "size":"0.874452",
                    "price":"1256.39",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"35",
                    "side":"SELL",
                    "size":"1.410025",
                    "price":"1255.564",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"36",
                    "side":"SELL",
                    "size":"0.303303",
                    "price":"1255.564",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"37",
                    "side":"SELL",
                    "size":"0.00117",
                    "price":"1255.528",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"38",
                    "side":"SELL",
                    "size":"0.000591",
                    "price":"1255.468",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"39",
                    "side":"SELL",
                    "size":"0.000357",
                    "price":"1256.025",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"40",
                    "side":"SELL",
                    "size":"0.000633",
                    "price":"1255.997",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"41",
                    "side":"SELL",
                    "size":"0.000863",
                    "price":"1255.98",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"42",
                    "side":"SELL",
                    "size":"0.001263",
                    "price":"1255.98",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"43",
                    "side":"SELL",
                    "size":"0.0005",
                    "price":"1255.616",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"44",
                    "side":"SELL",
                    "size":"0.00081",
                    "price":"1255.616",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"45",
                    "side":"BUY",
                    "size":"0.000913",
                    "price":"1255.636",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"46",
                    "side":"BUY",
                    "size":"0.000546",
                    "price":"1255.706",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"47",
                    "side":"BUY",
                    "size":"0.00138",
                    "price":"1255.75",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"48",
                    "side":"BUY",
                    "size":"0.000554",
                    "price":"1255.685",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"49",
                    "side":"BUY",
                    "size":"0.000115",
                    "price":"1255.715",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"50",
                    "side":"BUY",
                    "size":"0.001214",
                    "price":"1255.716",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"51",
                    "side":"BUY",
                    "size":"0.001042",
                    "price":"1255.762",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"52",
                    "side":"BUY",
                    "size":"0.001332",
                    "price":"1255.762",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"53",
                    "side":"BUY",
                    "size":"0.000506",
                    "price":"1255.854",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"54",
                    "side":"BUY",
                    "size":"0.00006",
                    "price":"1255.86",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"55",
                    "side":"BUY",
                    "size":"0.000579",
                    "price":"1256.302",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"56",
                    "side":"BUY",
                    "size":"1.037983",
                    "price":"1256.302",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"57",
                    "side":"SELL",
                    "size":"1.455303",
                    "price":"1256.025",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"58",
                    "side":"SELL",
                    "size":"0.000019",
                    "price":"1256.025",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"59",
                    "side":"BUY",
                    "size":"0.001999",
                    "price":"1256.305",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"60",
                    "side":"SELL",
                    "size":"0.113976",
                    "price":"1255.528",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"61",
                    "side":"BUY",
                    "size":"0.720382",
                    "price":"1256.39",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"62",
                    "side":"BUY",
                    "size":"0.000613",
                    "price":"1256.39",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"63",
                    "side":"BUY",
                    "size":"0.000759",
                    "price":"1256.306",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"64",
                    "side":"SELL",
                    "size":"0.000072",
                    "price":"1255.602",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"65",
                    "side":"BUY",
                    "size":"0.117781",
                    "price":"1256.306",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"66",
                    "side":"BUY",
                    "size":"0.000457",
                    "price":"1256.307",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"67",
                    "side":"BUY",
                    "size":"0.000627",
                    "price":"1256.328",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"68",
                    "side":"BUY",
                    "size":"0.000744",
                    "price":"1256.332",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"69",
                    "side":"BUY",
                    "size":"0.00073",
                    "price":"1256.378",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"70",
                    "side":"BUY",
                    "size":"0.000563",
                    "price":"1256.364",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"71",
                    "side":"BUY",
                    "size":"0.000421",
                    "price":"1256.225",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"72",
                    "side":"BUY",
                    "size":"0.960609",
                    "price":"1256.225",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"73",
                    "side":"SELL",
                    "size":"0.001351",
                    "price":"1255.46",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"74",
                    "side":"SELL",
                    "size":"0.0005",
                    "price":"1255.602",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"75",
                    "side":"BUY",
                    "size":"0.67952",
                    "price":"1256.35",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"76",
                    "side":"BUY",
                    "size":"0.261397",
                    "price":"1256.391",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"77",
                    "side":"BUY",
                    "size":"0.000557",
                    "price":"1256.264",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"78",
                    "side":"SELL",
                    "size":"0.001647",
                    "price":"1255.46",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"79",
                    "side":"BUY",
                    "size":"0.000243",
                    "price":"1256.391",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"80",
                    "side":"SELL",
                    "size":"0.000236",
                    "price":"1255.591",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"81",
                    "side":"BUY",
                    "size":"0.732272",
                    "price":"1256.354",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"82",
                    "side":"BUY",
                    "size":"0.00143",
                    "price":"1256.334",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"83",
                    "side":"BUY",
                    "size":"0.000292",
                    "price":"1256.354",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"84",
                    "side":"BUY",
                    "size":"0.803842",
                    "price":"1256.354",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"85",
                    "side":"SELL",
                    "size":"0.001137",
                    "price":"1255.46",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"86",
                    "side":"SELL",
                    "size":"0.000072",
                    "price":"1255.46",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"87",
                    "side":"SELL",
                    "size":"0.000068",
                    "price":"1255.602",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"88",
                    "side":"BUY",
                    "size":"0.000506",
                    "price":"1256.225",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"89",
                    "side":"BUY",
                    "size":"0.000035",
                    "price":"1256.415",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"90",
                    "side":"BUY",
                    "size":"1.426809",
                    "price":"1256.408",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"91",
                    "side":"BUY",
                    "size":"0.000608",
                    "price":"1256.393",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"92",
                    "side":"SELL",
                    "size":"0.119099",
                    "price":"1255.588",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"93",
                    "side":"SELL",
                    "size":"0.00117",
                    "price":"1255.602",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"94",
                    "side":"BUY",
                    "size":"0.000435",
                    "price":"1256.296",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"95",
                    "side":"BUY",
                    "size":"0.000622",
                    "price":"1256.35",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"96",
                    "side":"BUY",
                    "size":"0.865902",
                    "price":"1256.35",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"97",
                    "side":"BUY",
                    "size":"1.02526",
                    "price":"1256.334",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"98",
                    "side":"SELL",
                    "size":"0.000419",
                    "price":"1255.602",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"99",
                    "side":"SELL",
                    "size":"0.001351",
                    "price":"1255.602",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 },
                 {
                    "id":"100",
                    "side":"SELL",
                    "size":"1.473096",
                    "price":"1255.602",
                    "createdAt":"2022-12-12T02:28:09.282Z",
                    "createdAtHeight":"5818"
                 }
              ]
           }
        }
    """.trimIndent()

    internal val v4_channel_data = """
        {
           "type":"channel_data",
           "connection_id":"d8caff8c-0ee8-4eb0-b124-20c2d3d956ba",
           "message_id":6,
           "id":"ETH-USD",
           "channel":"v4_trades",
           "contents":{
              "trades":[
                 {
                    "id": "8ee6d90d-272d-5edd-bf0f-2e4d6ae3d3b7",
                    "size":"1.593707",
                    "price":"1255.949",
                    "side":"BUY",
                    "createdAt":"2022-12-12T02:28:14.859Z"
                 }
              ]
           }
        }
    """.trimIndent()

    internal val v4_channel_batch_data = """
        {
           "type":"channel_batch_data",
           "connection_id":"e35a0dcc-8704-4e6b-8b0f-4d8f00c6a573",
           "message_id":5,
           "id":"ETH-USD",
           "channel":"v4_trades",
           "contents":[
              {
                 "trades":[
                    {
                       "id":"101",
                       "size":"0.00038",
                       "price":"1291.05",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"102",
                       "size":"0.001061",
                       "price":"1291.026",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"103",
                       "size":"0.000151",
                       "price":"1291.05",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"104",
                       "size":"0.00014",
                       "price":"1291.05",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"105",
                       "size":"0.000868",
                       "price":"1291.052",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"106",
                       "size":"0.000182",
                       "price":"1291.162",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"107",
                       "size":"0.00034",
                       "price":"1291.162",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"108",
                       "size":"0.00204",
                       "price":"1291.026",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"109",
                       "size":"0.00021",
                       "price":"1291.026",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"110",
                       "size":"0.000777",
                       "price":"1291.026",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"111",
                       "size":"0.000263",
                       "price":"1291.162",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"112",
                       "size":"0.000276",
                       "price":"1291.162",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"113",
                       "size":"0.000829",
                       "price":"1291.193",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"114",
                       "size":"0.000885",
                       "price":"1291.026",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"115",
                       "size":"0.000255",
                       "price":"1291.193",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"116",
                       "size":"0.001369",
                       "price":"1291.026",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"117",
                       "size":"0.00076",
                       "price":"1291.026",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"118",
                       "size":"1.450008",
                       "price":"1291.026",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"119",
                       "size":"0.090702",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"120",
                       "size":"1.073614",
                       "price":"1291.193",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"121",
                       "size":"0.001043",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"122",
                       "size":"0.001686",
                       "price":"1291.193",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"123",
                       "size":"0.00079",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"124",
                       "size":"0.000144",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"125",
                       "size":"0.000177",
                       "price":"1291.193",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"126",
                       "size":"0.001786",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"127",
                       "size":"0.001061",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"128",
                       "size":"0.00204",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"129",
                       "size":"0.00021",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"130",
                       "size":"1.245273",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"131",
                       "size":"0.000309",
                       "price":"1291.193",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"132",
                       "size":"0.000774",
                       "price":"1291.193",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"133",
                       "size":"0.001876",
                       "price":"1291.193",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"134",
                       "size":"0.436212",
                       "price":"1291.193",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"135",
                       "size":"0.001814",
                       "price":"1291.208",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"136",
                       "size":"0.000285",
                       "price":"1291.275",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"137",
                       "size":"1.027468",
                       "price":"1291.289",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"138",
                       "size":"0.001798",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"139",
                       "size":"0.000795",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"140",
                       "size":"0.001875",
                       "price":"1291.289",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"141",
                       "size":"0.000885",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"142",
                       "size":"0.000701",
                       "price":"1291.289",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"143",
                       "size":"0.000207",
                       "price":"1291.289",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"144",
                       "size":"0.001657",
                       "price":"1291.289",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"145",
                       "size":"0.00041",
                       "price":"1291.289",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"146",
                       "size":"0.001105",
                       "price":"1291.289",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"147",
                       "size":"0.000255",
                       "price":"1291.289",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"148",
                       "size":"0.003268",
                       "price":"1291.289",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"149",
                       "size":"0.234861",
                       "price":"1291.289",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"150",
                       "size":"0.000219",
                       "price":"1291.339",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"151",
                       "size":"0.838534",
                       "price":"1291.346",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"152",
                       "size":"0.303145",
                       "price":"1291.346",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"153",
                       "size":"0.001162",
                       "price":"1291.363",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"154",
                       "size":"0.000604",
                       "price":"1291.395",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"155",
                       "size":"0.000159",
                       "price":"1291.396",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"156",
                       "size":"0.000326",
                       "price":"1291.422",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"157",
                       "size":"0.000505",
                       "price":"1291.43",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"158",
                       "size":"1.409194",
                       "price":"1291.434",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"159",
                       "size":"0.000375",
                       "price":"1291.472",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"160",
                       "size":"0.172366",
                       "price":"1291.488",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"161",
                       "size":"0.00175",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"162",
                       "size":"0.002299",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"163",
                       "size":"0.00057",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"164",
                       "size":"0.000083",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"165",
                       "size":"0.000498",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"166",
                       "size":"0.000978",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"167",
                       "size":"0.002395",
                       "price":"1291.488",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"168",
                       "size":"0.000468",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"169",
                       "size":"0.000681",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"170",
                       "size":"0.000177",
                       "price":"1291.488",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"171",
                       "size":"0.002084",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"172",
                       "size":"0.000832",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"173",
                       "size":"0.00348",
                       "price":"1291.488",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"174",
                       "size":"0.000414",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"175",
                       "size":"0.00038",
                       "price":"1291.488",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"176",
                       "size":"0.112642",
                       "price":"1291.02",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"177",
                       "size":"0.323473",
                       "price":"1291.008",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"178",
                       "size":"0.000104",
                       "price":"1291.003",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"179",
                       "size":"0.90841",
                       "price":"1290.979",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"180",
                       "size":"0.000563",
                       "price":"1290.979",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"181",
                       "size":"1.259539",
                       "price":"1291.488",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"182",
                       "size":"0.649674",
                       "price":"1290.979",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"183",
                       "size":"0.000206",
                       "price":"1290.964",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"184",
                       "size":"0.774692",
                       "price":"1290.958",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"185",
                       "size":"0.001655",
                       "price":"1290.958",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"186",
                       "size":"0.00021",
                       "price":"1290.958",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"187",
                       "size":"0.000219",
                       "price":"1290.958",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"188",
                       "size":"0.431288",
                       "price":"1290.958",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"189",
                       "size":"0.710391",
                       "price":"1290.953",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"190",
                       "size":"0.000159",
                       "price":"1290.953",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"191",
                       "size":"0.000505",
                       "price":"1290.953",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"192",
                       "size":"0.000889",
                       "price":"1290.953",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"193",
                       "size":"0.580335",
                       "price":"1290.953",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"194",
                       "size":"1.108181",
                       "price":"1290.952",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"195",
                       "size":"0.250179",
                       "price":"1291.488",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"196",
                       "size":"0.000212",
                       "price":"1291.51",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"197",
                       "size":"1.07069",
                       "price":"1291.528",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"239",
                       "size":"0.002117",
                       "price":"1291.528",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"198",
                       "size":"0.000777",
                       "price":"1290.952",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"199",
                       "size":"0.000638",
                       "price":"1291.528",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"200",
                       "size":"0.000375",
                       "price":"1290.952",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"201",
                       "size":"0.889535",
                       "price":"1291.528",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"202",
                       "size":"0.000421",
                       "price":"1291.529",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"203",
                       "size":"0.000532",
                       "price":"1291.576",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"204",
                       "size":"0.575291",
                       "price":"1291.589",
                       "side":"BUY",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"205",
                       "size":"0.000716",
                       "price":"1290.952",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"206",
                       "size":"0.000795",
                       "price":"1290.952",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"207",
                       "size":"0.000212",
                       "price":"1291.51",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"208",
                       "size":"0.77926",
                       "price":"1290.952",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"209",
                       "size":"0.000552",
                       "price":"1290.951",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"210",
                       "size":"0.574991",
                       "price":"1290.946",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"211",
                       "size":"0.000446",
                       "price":"1291.493",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"212",
                       "size":"0.001563",
                       "price":"1291.434",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"213",
                       "size":"0.001693",
                       "price":"1291.434",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"214",
                       "size":"0.000732",
                       "price":"1291.434",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"215",
                       "size":"1.116469",
                       "price":"1291.434",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"216",
                       "size":"0.001051",
                       "price":"1291.434",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"217",
                       "size":"0.001736",
                       "price":"1291.434",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"218",
                       "size":"1.54071",
                       "price":"1291.434",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"219",
                       "size":"0.00038",
                       "price":"1291.434",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"220",
                       "size":"0.002053",
                       "price":"1291.434",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"221",
                       "size":"0.000308",
                       "price":"1291.434",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"222",
                       "size":"0.023085",
                       "price":"1291.434",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"223",
                       "size":"1.95232",
                       "price":"1291.43",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"224",
                       "size":"0.001875",
                       "price":"1291.42",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"225",
                       "size":"0.000604",
                       "price":"1291.395",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"226",
                       "size":"0.00022",
                       "price":"1291.365",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"227",
                       "size":"0.000701",
                       "price":"1291.351",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"228",
                       "size":"0.105217",
                       "price":"1291.35",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"229",
                       "size":"0.097208",
                       "price":"1291.325",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"230",
                       "size":"1.59517",
                       "price":"1291.325",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"231",
                       "size":"0.161708",
                       "price":"1291.304",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"232",
                       "size":"0.000919",
                       "price":"1291.304",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"233",
                       "size":"1.23975",
                       "price":"1291.304",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"234",
                       "size":"0.000186",
                       "price":"1291.275",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"235",
                       "size":"0.312544",
                       "price":"1291.255",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"236",
                       "size":"0.002084",
                       "price":"1291.255",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"237",
                       "size":"0.000412",
                       "price":"1291.255",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              },
              {
                 "trades":[
                    {
                       "id":"238",
                       "size":"0.000102",
                       "price":"1291.255",
                       "side":"SELL",
                       "createdAt":"2022-12-15T05:42:33.507Z"
                    }
                 ]
              }
           ]
        }
    """.trimIndent()
}
