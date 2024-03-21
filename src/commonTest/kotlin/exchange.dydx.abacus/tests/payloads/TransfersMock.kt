package exchange.dydx.abacus.tests.payloads

@Suppress("PropertyName")
internal class TransfersMock {
    internal val transfer_data = """
        {
           "transfers":[
              {
                 "id":"89586775-0646-582e-9b36-4f131715644d",
                 "sender":{
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
                 "transactionHash": "MOCKHASH1"
              },
              {
                 "id":"34102591-fa1a-5a0b-ae0d-35192bbfb33a",
                 "sender":{
                    "address":"dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg",
                    "subaccountNumber":0
                 },
                 "recipient":{
                    "address":"dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg"
                 },
                 "size":"2",
                 "createdAt":"2023-08-21T21:37:41.091Z",
                 "createdAtHeight":"404003",
                 "symbol":"USDC",
                 "type":"WITHDRAWAL",
                 "transactionHash": "MOCKHASH2"
              },
              {
                 "id":"af5eafd4-4f39-5432-959e-be00c5ab1a64",
                 "sender":{
                    "address":"dydx1nzuttarf5k2j0nug5yzhr6p74t9avehn9hlh8m",
                    "subaccountNumber":0
                 },
                 "recipient":{
                    "address":"dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg",
                    "subaccountNumber":0
                 },
                 "size":"1000",
                 "createdAt":"2023-08-18T20:18:55.767Z",
                 "createdAtHeight":"164985",
                 "symbol":"USDC",
                 "type":"TRANSFER_IN",
                 "transactionHash": "MOCKHASH3"
              },
              {
                 "id":"5d3e01f8-60b2-5c74-9dc0-6b7964ac751c",
                 "sender":{
                    "address":"dydx1nzuttarf5k2j0nug5yzhr6p74t9avehn9hlh8m",
                    "subaccountNumber":0
                 },
                 "recipient":{
                    "address":"dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg",
                    "subaccountNumber":0
                 },
                 "size":"1000",
                 "createdAt":"2023-08-18T19:51:35.538Z",
                 "createdAtHeight":"163501",
                 "symbol":"USDC",
                 "type":"TRANSFER_IN",
                 "transactionHash": "MOCKHASH4"
              },
              {
                 "id":"5e297126-ee0b-588c-8a64-eb47e7370718",
                 "sender":{
                    "address":"dydx1nzuttarf5k2j0nug5yzhr6p74t9avehn9hlh8m",
                    "subaccountNumber":0
                 },
                 "recipient":{
                    "address":"dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg",
                    "subaccountNumber":0
                 },
                 "size":"1000",
                 "createdAt":"2023-08-16T21:41:24.012Z",
                 "createdAtHeight":"13627",
                 "symbol":"USDC",
                 "type":"TRANSFER_IN",
                 "transactionHash": "MOCKHASH5"
              }
           ]
        }
    """.trimIndent()

    val channel_data = """
        {
          "type": "channel_data",
          "connection_id": "7baedc3a-0f15-4a20-8e4f-30b8af67e461",
          "message_id": 87,
          "id": "dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg/0",
          "channel": "v4_subaccounts",
          "version": "2.4.0",
          "contents": {
            "transfers": {
              "sender": {
                "address": "dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg",
                "subaccountNumber": 0
              },
              "recipient": {
                "address": "dydx1pnvf90r7uyfcrgs35fh2ksc5ld4rxw8jr2gsat"
              },
              "symbol": "USDC",
              "size": "120",
              "type": "WITHDRAWAL",
              "createdAt": "2023-09-12T15:34:16.427Z",
              "createdAtHeight": "371225",
              "transactionHash": "A9758D092415E36F4E0D80D323BC4EE472644548392489309333CA55E963431B"
            }
          }
        }
    """.trimIndent()
}
